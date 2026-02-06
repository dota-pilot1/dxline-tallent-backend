package om.dxline.dxtalent.talent.domain.model;

import java.util.Arrays;
import java.util.List;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 파일명 값 객체 (FileName Value Object)
 *
 * 이력서 파일의 이름을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 최대 길이: 255자
 * - 최소 길이: 1자
 * - 허용된 확장자: .pdf, .doc, .docx
 * - 확장자 필수
 * - 특수문자 제한 (파일 시스템 안전성)
 *
 * 사용 예시:
 * <pre>
 * FileName fileName = new FileName("홍길동_이력서.pdf");
 * String extension = fileName.getExtension(); // "pdf"
 * String nameOnly = fileName.getNameWithoutExtension(); // "홍길동_이력서"
 * </pre>
 */
public class FileName extends BaseValueObject {

    private static final int MAX_LENGTH = 255;
    private static final int MIN_LENGTH = 1;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");

    // 파일 시스템에서 위험한 문자들
    private static final String FORBIDDEN_CHARS_PATTERN = "[<>:\"|?*\\\\]";

    private final String value;

    /**
     * 파일명으로 FileName 생성
     *
     * @param value 파일명
     * @throws IllegalArgumentException 유효하지 않은 파일명인 경우
     */
    public FileName(String value) {
        this.value = requireNonNull(value, "파일명");
        validateFileName(value);
    }

    /**
     * 파일명 검증
     */
    private void validateFileName(String value) {
        // 길이 검증
        validate(
            value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH,
            String.format("파일명은 %d자 이상 %d자 이하여야 합니다", MIN_LENGTH, MAX_LENGTH)
        );

        // 공백만 있는지 검증
        validate(
            !value.trim().isEmpty(),
            "파일명은 공백만으로 구성될 수 없습니다"
        );

        // 확장자 존재 여부
        validate(
            value.contains("."),
            "파일명에 확장자가 필요합니다"
        );

        // 확장자 유효성
        String extension = getExtension().toLowerCase();
        validate(
            ALLOWED_EXTENSIONS.contains(extension),
            String.format("허용되지 않은 파일 형식입니다. 허용: %s", ALLOWED_EXTENSIONS)
        );

        // 위험한 문자 검증
        validate(
            !value.matches(".*" + FORBIDDEN_CHARS_PATTERN + ".*"),
            "파일명에 사용할 수 없는 특수문자가 포함되어 있습니다: < > : \" | ? * \\"
        );

        // 연속된 점 방지
        validate(
            !value.contains(".."),
            "파일명에 연속된 점(..)을 사용할 수 없습니다"
        );

        // 점으로 시작하는 파일명 방지
        validate(
            !value.startsWith("."),
            "파일명은 점(.)으로 시작할 수 없습니다"
        );
    }

    /**
     * 파일 확장자 반환
     *
     * @return 확장자 (소문자, 점 제외)
     */
    public String getExtension() {
        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < value.length() - 1) {
            return value.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 확장자를 제외한 파일명 반환
     *
     * @return 확장자 없는 파일명
     */
    public String getNameWithoutExtension() {
        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return value.substring(0, lastDotIndex);
        }
        return value;
    }

    /**
     * 파일명이 PDF인지 확인
     *
     * @return PDF이면 true
     */
    public boolean isPdf() {
        return "pdf".equalsIgnoreCase(getExtension());
    }

    /**
     * 파일명이 Word 문서인지 확인
     *
     * @return Word 문서이면 true
     */
    public boolean isWordDocument() {
        String ext = getExtension();
        return "doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext);
    }

    /**
     * 파일명 값 반환
     *
     * @return 파일명 문자열
     */
    public String getValue() {
        return value;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value.toLowerCase() }; // 대소문자 구분 없이
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * 새 확장자로 파일명 변경
     *
     * @param newExtension 새 확장자 (점 없이)
     * @return 새로운 FileName 인스턴스
     */
    public FileName withExtension(String newExtension) {
        return new FileName(getNameWithoutExtension() + "." + newExtension);
    }

    /**
     * 파일명 부분만 변경 (확장자 유지)
     *
     * @param newName 새 파일명 (확장자 제외)
     * @return 새로운 FileName 인스턴스
     */
    public FileName withName(String newName) {
        return new FileName(newName + "." + getExtension());
    }
}
