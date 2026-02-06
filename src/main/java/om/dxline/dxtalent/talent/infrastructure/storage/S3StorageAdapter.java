package om.dxline.dxtalent.talent.infrastructure.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.common.service.S3UploadService;
import om.dxline.dxtalent.talent.domain.model.FileName;
import om.dxline.dxtalent.talent.domain.model.FileSize;
import om.dxline.dxtalent.talent.domain.model.FileType;
import om.dxline.dxtalent.talent.domain.port.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 Storage Adapter
 *
 * FileStoragePort를 구현한 AWS S3 기반 파일 스토리지 어댑터입니다.
 * 기존 S3UploadService를 래핑하여 도메인 레이어의 Port 인터페이스를 구현합니다.
 *
 * 책임:
 * - 도메인 Port 인터페이스 구현
 * - 기존 S3UploadService 위임
 * - 도메인 모델 ↔ 인프라 객체 변환
 * - 예외 변환 (인프라 예외 → 도메인 예외)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements FileStoragePort {

    private final S3UploadService s3UploadService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * 파일 업로드
     *
     * @param inputStream 파일 입력 스트림
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param fileType 파일 타입
     * @param userId 사용자 ID (폴더 구분용)
     * @return 파일 업로드 결과
     * @throws FileStorageException 업로드 실패 시
     */
    @Override
    public FileUploadResult uploadFile(
        InputStream inputStream,
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        Long userId
    ) throws FileStorageException {
        log.info("Uploading file to S3: fileName={}, userId={}",
            fileName.getValue(), userId);

        try {
            // 고유한 저장 파일명 생성
            String storedName = generateStoredFileName(fileName);
            String folder = String.format("resumes/user-%d", userId);

            // MultipartFile 어댑터 생성
            MultipartFile multipartFile = new InputStreamMultipartFile(
                inputStream,
                fileName.getValue(),
                fileType.getMimeType(),
                fileSize.getBytes()
            );

            // S3에 업로드
            String s3Url = s3UploadService.uploadFile(multipartFile, folder, storedName);
            String storageKey = folder + "/" + storedName;

            log.info("File uploaded successfully to S3: {}", storageKey);

            return new FileUploadResult(
                storageKey,
                s3Url,
                fileSize.getBytes()
            );

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new FileStorageException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new FileStorageException("파일 업로드 중 예상치 못한 오류가 발생했습니다", e);
        }
    }

    /**
     * 파일 다운로드
     *
     * @param storageKey 저장소 키 (S3 키)
     * @return 파일 입력 스트림
     * @throws FileStorageException 다운로드 실패 시
     */
    @Override
    public InputStream downloadFile(String storageKey) throws FileStorageException {
        log.debug("Downloading file from S3: {}", storageKey);

        try {
            String s3Url = constructS3Url(storageKey);
            return s3UploadService.downloadFile(s3Url);
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", storageKey, e);
            throw new FileStorageException("파일 다운로드에 실패했습니다", e);
        }
    }

    /**
     * 파일 삭제
     *
     * @param storageKey 저장소 키
     * @throws FileStorageException 삭제 실패 시
     */
    @Override
    public void deleteFile(String storageKey) throws FileStorageException {
        log.info("Deleting file from S3: {}", storageKey);

        try {
            String s3Url = constructS3Url(storageKey);
            s3UploadService.deleteFile(s3Url);
            log.info("File deleted successfully: {}", storageKey);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", storageKey, e);
            throw new FileStorageException("파일 삭제에 실패했습니다", e);
        }
    }

    /**
     * 파일 존재 여부 확인
     *
     * @param storageKey 저장소 키
     * @return 존재하면 true
     */
    @Override
    public boolean fileExists(String storageKey) {
        try {
            InputStream inputStream = downloadFile(storageKey);
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 접근 URL 생성
     *
     * @param storageKey 저장소 키
     * @param expirationMinutes 만료 시간 (분)
     * @return 접근 가능한 URL
     * @throws FileStorageException URL 생성 실패 시
     */
    @Override
    public String generateAccessUrl(String storageKey, int expirationMinutes)
        throws FileStorageException {
        // 현재는 public URL 반환 (presigned URL은 추후 구현)
        return constructS3Url(storageKey);
    }

    /**
     * 파일 메타데이터 조회
     *
     * @param storageKey 저장소 키
     * @return 파일 메타데이터
     * @throws FileStorageException 조회 실패 시
     */
    @Override
    public FileMetadata getFileMetadata(String storageKey)
        throws FileStorageException {
        log.debug("Getting file metadata: {}", storageKey);

        try {
            // 간단한 메타데이터 반환 (실제로는 S3 HeadObject API 사용 가능)
            String[] parts = storageKey.split("/");
            String fileName = parts[parts.length - 1];

            return new FileMetadata(
                storageKey,
                fileName,
                0L, // 크기는 별도 조회 필요
                "application/octet-stream",
                null
            );
        } catch (Exception e) {
            log.error("Failed to get file metadata: {}", storageKey, e);
            throw new FileStorageException("파일 메타데이터 조회에 실패했습니다", e);
        }
    }

    /**
     * 고유한 저장 파일명 생성
     *
     * @param fileName 원본 파일명
     * @return UUID가 포함된 고유 파일명
     */
    private String generateStoredFileName(FileName fileName) {
        String extension = fileName.getExtension();
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    /**
     * Storage Key로부터 S3 URL 구성
     *
     * @param storageKey 저장소 키
     * @return S3 URL
     */
    private String constructS3Url(String storageKey) {
        return String.format(
            "https://%s.s3.%s.amazonaws.com/%s",
            bucketName,
            region,
            storageKey
        );
    }

    /**
     * InputStream을 MultipartFile로 래핑하는 어댑터 클래스
     */
    private static class InputStreamMultipartFile implements MultipartFile {

        private final InputStream inputStream;
        private final String originalFilename;
        private final String contentType;
        private final long size;
        private byte[] cachedBytes;

        public InputStreamMultipartFile(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            long size
        ) {
            this.inputStream = inputStream;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.size = size;
        }

        @Override
        public String getName() {
            return originalFilename;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() throws IOException {
            if (cachedBytes == null) {
                cachedBytes = inputStream.readAllBytes();
            }
            return cachedBytes;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (cachedBytes != null) {
                return new ByteArrayInputStream(cachedBytes);
            }
            return inputStream;
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("transferTo is not supported");
        }
    }
}
