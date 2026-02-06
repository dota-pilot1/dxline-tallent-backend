package om.dxline.dxtalent.talent.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * 파일 타입 열거형 (FileType Enum)
 *
 * 이력서에서 지원하는 파일 형식을 정의합니다.
 *
 * 지원 형식:
 * - PDF: 가장 일반적인 이력서 형식
 * - DOC: Microsoft Word 구버전
 * - DOCX: Microsoft Word 신버전
 *
 * 사용 예시:
 * <pre>
 * FileType type = FileType.fromMimeType("application/pdf");
 * FileType type2 = FileType.fromExtension(".pdf");
 * boolean supported = type.isSupported(); // true
 * </pre>
 */
public enum FileType {

    PDF(
        "application/pdf",
        ".pdf",
        "PDF 문서",
        true
    ),

    DOC(
        "application/msword",
        ".doc",
        "Microsoft Word 문서 (구버전)",
        true
    ),

    DOCX(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        ".docx",
        "Microsoft Word 문서",
        true
    ),

    // 지원하지 않는 형식 (검증용)
    UNSUPPORTED(
        "application/octet-stream",
        "",
        "지원하지 않는 형식",
        false
    );

    private final String mimeType;
    private final String extension;
    private final String description;
    private final boolean supported;

    /**
     * FileType 생성자
     *
     * @param mimeType MIME 타입
     * @param extension 파일 확장자
     * @param description 설명
     * @param supported 지원 여부
     */
    FileType(String mimeType, String extension, String description, boolean supported) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.description = description;
        this.supported = supported;
    }

    /**
     * MIME 타입 반환
     *
     * @return MIME 타입 문자열
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일 확장자 반환
     *
     * @return 확장자 문자열 (점 포함)
     */
    public String getExtension() {
        return extension;
    }

    /**
     * 설명 반환
     *
     * @return 파일 타입 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 지원 여부 확인
     *
     * @return 지원하면 true
     */
    public boolean isSupported() {
        return supported;
    }

    /**
     * PDF 타입인지 확인
     *
     * @return PDF이면 true
     */
    public boolean isPdf() {
        return this == PDF;
    }

    /**
     * Word 문서인지 확인
     *
     * @return Word 문서이면 true
     */
    public boolean isWordDocument() {
        return this == DOC || this == DOCX;
    }

    /**
     * MIME 타입으로 FileType 찾기
     *
     * @param mimeType MIME 타입
     * @return FileType (없으면 UNSUPPORTED)
     */
    public static FileType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return UNSUPPORTED;
        }

        return Arrays.stream(values())
            .filter(type -> type.mimeType.equalsIgnoreCase(mimeType))
            .findFirst()
            .orElse(UNSUPPORTED);
    }

    /**
     * 확장자로 FileType 찾기
     *
     * @param extension 파일 확장자 (점 포함 또는 제외)
     * @return FileType (없으면 UNSUPPORTED)
     */
    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNSUPPORTED;
        }

        // 점이 없으면 추가
        String ext = extension.startsWith(".") ? extension : "." + extension;

        return Arrays.stream(values())
            .filter(type -> type.extension.equalsIgnoreCase(ext))
            .findFirst()
            .orElse(UNSUPPORTED);
    }

    /**
     * 파일명으로 FileType 찾기
     *
     * @param fileName 파일명
     * @return FileType (없으면 UNSUPPORTED)
     */
    public static FileType fromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return UNSUPPORTED;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex);
            return fromExtension(extension);
        }

        return UNSUPPORTED;
    }

    /**
     * MIME 타입이 지원되는지 확인
     *
     * @param mimeType MIME 타입
     * @return 지원하면 true
     */
    public static boolean isSupportedMimeType(String mimeType) {
        FileType type = fromMimeType(mimeType);
        return type.isSupported();
    }

    /**
     * 확장자가 지원되는지 확인
     *
     * @param extension 파일 확장자
     * @return 지원하면 true
     */
    public static boolean isSupportedExtension(String extension) {
        FileType type = fromExtension(extension);
        return type.isSupported();
    }

    /**
     * 지원하는 모든 확장자 목록 반환
     *
     * @return 확장자 배열
     */
    public static String[] getSupportedExtensions() {
        return Arrays.stream(values())
            .filter(FileType::isSupported)
            .map(FileType::getExtension)
            .toArray(String[]::new);
    }

    /**
     * 지원하는 모든 MIME 타입 목록 반환
     *
     * @return MIME 타입 배열
     */
    public static String[] getSupportedMimeTypes() {
        return Arrays.stream(values())
            .filter(FileType::isSupported)
            .map(FileType::getMimeType)
            .toArray(String[]::new);
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", description, extension);
    }
}
