package om.dxline.dxtalent.identity.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

import java.util.regex.Pattern;

/**
 * UserName 값 객체 (User Name Value Object)
 *
 * 사용자 이름을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * 1. 1자 이상 100자 이하
 * 2. 앞뒤 공백 자동 제거
 * 3. null 또는 빈 값 불가
 * 4. 특수문자 제한 (선택적)
 *
 * 사용 예시:
 * <pre>
 * // 사용자 이름 생성
 * UserName name = new UserName("홍길동");
 * UserName name = UserName.of("김철수");
 *
 * // 자동으로 공백 제거
 * UserName name = new UserName("  이영희  ");
 * System.out.println(name.getValue()); // "이영희"
 * </pre>
 */
public class UserName extends BaseValueObject {

    /**
     * 이름 최소 길이
     */
    private static final int MIN_LENGTH = 1;

    /**
     * 이름 최대 길이
     */
    private static final int MAX_LENGTH = 100;

    /**
     * 허용되지 않는 특수문자 패턴 (선택적 검증)
     * 이모지, 제어 문자 등을 차단
     */
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile(
        "[\\p{C}\\p{So}\\p{Sk}]" // 제어 문자, 기타 기호, 수정 기호
    );

    /**
     * 정규화된 이름 값 (앞뒤 공백 제거)
     */
    private final String value;

    /**
     * UserName 생성자
     *
     * @param value 사용자 이름
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public UserName(String value) {
        this.value = normalize(validate(value));
    }

    /**
     * 이름 유효성 검증
     *
     * @param value 검증할 이름
     * @return 검증된 이름
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private String validate(String value) {
        // null 및 빈 값 체크
        requireNonEmpty(value, "User name");

        String trimmed = value.trim();

        // 길이 검증
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("이름은 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("이름은 최대 %d자 이하여야 합니다", MAX_LENGTH)
            );
        }

        // 특수문자 검증 (선택적)
        if (INVALID_CHARS_PATTERN.matcher(trimmed).find()) {
            throw new IllegalArgumentException(
                "이름에 사용할 수 없는 문자가 포함되어 있습니다"
            );
        }

        return trimmed;
    }

    /**
     * 이름 정규화 (앞뒤 공백 제거, 연속된 공백을 하나로)
     *
     * @param value 정규화할 이름
     * @return 정규화된 이름
     */
    private String normalize(String value) {
        // 앞뒤 공백 제거
        String trimmed = value.trim();

        // 연속된 공백을 하나로 변환
        return trimmed.replaceAll("\\s+", " ");
    }

    /**
     * 이름 값 반환
     *
     * @return 정규화된 이름
     */
    public String getValue() {
        return value;
    }

    /**
     * 이름 길이 반환
     *
     * @return 이름의 문자 개수
     */
    public int length() {
        return value.length();
    }

    /**
     * 성(First Name) 추출 (한국식 이름 가정)
     * 예: "홍길동" -> "홍"
     *
     * @return 성
     */
    public String getLastName() {
        if (value.isEmpty()) {
            return "";
        }
        return String.valueOf(value.charAt(0));
    }

    /**
     * 이름(Given Name) 추출 (한국식 이름 가정)
     * 예: "홍길동" -> "길동"
     *
     * @return 이름
     */
    public String getFirstName() {
        if (value.length() <= 1) {
            return "";
        }
        return value.substring(1);
    }

    /**
     * 이니셜 반환
     * 예: "홍길동" -> "홍", "John Doe" -> "JD"
     *
     * @return 이니셜
     */
    public String getInitials() {
        if (value.isEmpty()) {
            return "";
        }

        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            // 한 단어인 경우 첫 글자만
            return String.valueOf(value.charAt(0));
        }

        // 여러 단어인 경우 각 단어의 첫 글자
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString();
    }

    /**
     * 마스킹된 이름 반환 (개인정보 보호)
     * 예: "홍길동" -> "홍*동", "John Doe" -> "J*** D**"
     *
     * @return 마스킹된 이름
     */
    public String getMasked() {
        if (value.length() <= 2) {
            return value.charAt(0) + "*";
        }

        String[] parts = value.split("\\s+");
        if (parts.length == 1) {
            // 한 단어인 경우: 첫글자 + * + 마지막글자
            return value.charAt(0) + "*" + value.charAt(value.length() - 1);
        }

        // 여러 단어인 경우: 각 단어를 마스킹
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() <= 1) {
                masked.append(part);
            } else if (part.length() == 2) {
                masked.append(part.charAt(0)).append("*");
            } else {
                masked.append(part.charAt(0))
                      .append("*".repeat(part.length() - 2))
                      .append(part.charAt(part.length() - 1));
            }

            if (i < parts.length - 1) {
                masked.append(" ");
            }
        }
        return masked.toString();
    }

    /**
     * 공백이 포함되어 있는지 확인 (성과 이름이 분리되어 있는지)
     *
     * @return 공백이 있으면 true
     */
    public boolean hasWhitespace() {
        return value.contains(" ");
    }

    /**
     * 단어 개수 반환
     *
     * @return 공백으로 구분된 단어 개수
     */
    public int getWordCount() {
        return value.split("\\s+").length;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{value};
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * 편의 메서드: 문자열로부터 UserName 생성
     *
     * @param value 이름 문자열
     * @return UserName 인스턴스
     */
    public static UserName of(String value) {
        return new UserName(value);
    }

    /**
     * 편의 메서드: 성과 이름으로부터 UserName 생성
     *
     * @param lastName 성
     * @param firstName 이름
     * @return UserName 인스턴스
     */
    public static UserName of(String lastName, String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new UserName(lastName);
        }
        return new UserName(lastName + " " + firstName);
    }
}
