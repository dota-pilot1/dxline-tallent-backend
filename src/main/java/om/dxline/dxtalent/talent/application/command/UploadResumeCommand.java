package om.dxline.dxtalent.talent.application.command;

import java.io.InputStream;

/**
 * 이력서 업로드 커맨드
 *
 * 이력서 파일을 업로드하는 요청을 표현하는 커맨드 객체입니다.
 *
 * 책임:
 * - 업로드 요청 데이터 캡슐화
 * - 입력 데이터 유효성 검증
 * - 불변성 보장
 *
 * 사용 예시:
 * <pre>
 * UploadResumeCommand command = new UploadResumeCommand(
 *     userId,
 *     inputStream,
 *     "홍길동_이력서.pdf",
 *     2_097_152L,
 *     "application/pdf"
 * );
 * </pre>
 */
public class UploadResumeCommand {

    private final Long userId;
    private final InputStream fileInputStream;
    private final String originalFileName;
    private final Long fileSize;
    private final String mimeType;

    /**
     * 이력서 업로드 커맨드 생성
     *
     * @param userId 사용자 ID
     * @param fileInputStream 파일 입력 스트림
     * @param originalFileName 원본 파일명
     * @param fileSize 파일 크기 (바이트)
     * @param mimeType MIME 타입
     * @throws IllegalArgumentException 유효하지 않은 입력인 경우
     */
    public UploadResumeCommand(
        Long userId,
        InputStream fileInputStream,
        String originalFileName,
        Long fileSize,
        String mimeType
    ) {
        validateInputs(userId, fileInputStream, originalFileName, fileSize);

        this.userId = userId;
        this.fileInputStream = fileInputStream;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    /**
     * 입력 데이터 유효성 검증
     */
    private void validateInputs(
        Long userId,
        InputStream fileInputStream,
        String originalFileName,
        Long fileSize
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다");
        }

        if (fileInputStream == null) {
            throw new IllegalArgumentException("파일 입력 스트림이 null입니다");
        }

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 비어있습니다");
        }

        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("유효하지 않은 파일 크기입니다");
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (fileSize > maxSize) {
            throw new IllegalArgumentException(
                String.format("파일 크기가 너무 큽니다. 최대 %dMB까지 업로드 가능합니다", maxSize / 1024 / 1024)
            );
        }

        // 최소 파일 크기 (1KB)
        long minSize = 1024; // 1KB
        if (fileSize < minSize) {
            throw new IllegalArgumentException("파일 크기가 너무 작습니다");
        }
    }

    public Long getUserId() {
        return userId;
    }

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return String.format(
            "UploadResumeCommand[userId=%d, fileName=%s, fileSize=%d, mimeType=%s]",
            userId,
            originalFileName,
            fileSize,
            mimeType
        );
    }
}
