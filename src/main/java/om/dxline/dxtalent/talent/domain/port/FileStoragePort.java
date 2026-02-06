package om.dxline.dxtalent.talent.domain.port;

import java.io.InputStream;
import om.dxline.dxtalent.talent.domain.model.FileName;
import om.dxline.dxtalent.talent.domain.model.FileSize;
import om.dxline.dxtalent.talent.domain.model.FileType;

/**
 * 파일 스토리지 포트 인터페이스 (Domain Layer)
 *
 * 파일 저장소(S3, Local Storage 등)에 대한 추상화 인터페이스입니다.
 * 실제 구현은 인프라 레이어에서 제공됩니다.
 *
 * 포트 패턴 (Hexagonal Architecture):
 * - 도메인 레이어가 특정 스토리지 기술에 의존하지 않도록 추상화
 * - 구현체는 인프라 레이어에 위치 (S3StorageAdapter 등)
 * - 도메인 모델만 사용
 *
 * 사용 예시:
 * <pre>
 * FileUploadResult result = fileStoragePort.uploadFile(
 *     inputStream,
 *     fileName,
 *     fileSize,
 *     fileType,
 *     userId
 * );
 * String s3Key = result.getStorageKey();
 * </pre>
 */
public interface FileStoragePort {

    /**
     * 파일 업로드
     *
     * 파일을 스토리지에 업로드하고 저장 위치(키)를 반환합니다.
     *
     * @param inputStream 파일 입력 스트림
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param fileType 파일 타입
     * @param userId 사용자 ID (폴더 구분용)
     * @return 파일 업로드 결과
     * @throws FileStorageException 업로드 실패 시
     */
    FileUploadResult uploadFile(
        InputStream inputStream,
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        Long userId
    ) throws FileStorageException;

    /**
     * 파일 다운로드
     *
     * 저장된 파일을 다운로드합니다.
     *
     * @param storageKey 저장소 키 (S3 키 등)
     * @return 파일 입력 스트림
     * @throws FileStorageException 다운로드 실패 시
     */
    InputStream downloadFile(String storageKey) throws FileStorageException;

    /**
     * 파일 삭제
     *
     * 저장소에서 파일을 삭제합니다.
     *
     * @param storageKey 저장소 키
     * @throws FileStorageException 삭제 실패 시
     */
    void deleteFile(String storageKey) throws FileStorageException;

    /**
     * 파일 존재 여부 확인
     *
     * @param storageKey 저장소 키
     * @return 존재하면 true
     */
    boolean fileExists(String storageKey);

    /**
     * 파일 URL 생성
     *
     * 파일에 접근할 수 있는 URL을 생성합니다.
     * (S3의 경우 presigned URL)
     *
     * @param storageKey 저장소 키
     * @param expirationMinutes 만료 시간 (분)
     * @return 접근 가능한 URL
     * @throws FileStorageException URL 생성 실패 시
     */
    String generateAccessUrl(String storageKey, int expirationMinutes)
        throws FileStorageException;

    /**
     * 파일 메타데이터 조회
     *
     * @param storageKey 저장소 키
     * @return 파일 메타데이터
     * @throws FileStorageException 조회 실패 시
     */
    FileMetadata getFileMetadata(String storageKey)
        throws FileStorageException;

    /**
     * 파일 업로드 결과
     */
    class FileUploadResult {

        private final String storageKey;
        private final String accessUrl;
        private final long uploadedSize;

        public FileUploadResult(
            String storageKey,
            String accessUrl,
            long uploadedSize
        ) {
            this.storageKey = storageKey;
            this.accessUrl = accessUrl;
            this.uploadedSize = uploadedSize;
        }

        /**
         * 저장소 키 반환 (S3 키 등)
         */
        public String getStorageKey() {
            return storageKey;
        }

        /**
         * 접근 URL 반환
         */
        public String getAccessUrl() {
            return accessUrl;
        }

        /**
         * 업로드된 파일 크기 반환
         */
        public long getUploadedSize() {
            return uploadedSize;
        }
    }

    /**
     * 파일 메타데이터
     */
    class FileMetadata {

        private final String storageKey;
        private final String fileName;
        private final long fileSize;
        private final String contentType;
        private final String lastModified;

        public FileMetadata(
            String storageKey,
            String fileName,
            long fileSize,
            String contentType,
            String lastModified
        ) {
            this.storageKey = storageKey;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.contentType = contentType;
            this.lastModified = lastModified;
        }

        public String getStorageKey() {
            return storageKey;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getContentType() {
            return contentType;
        }

        public String getLastModified() {
            return lastModified;
        }
    }

    /**
     * 파일 스토리지 예외
     */
    class FileStorageException extends RuntimeException {

        public FileStorageException(String message) {
            super(message);
        }

        public FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
