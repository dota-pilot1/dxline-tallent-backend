package om.dxline.dxtalent.identity.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

import java.util.regex.Pattern;

/**
 * Password 값 객체 (Password Value Object)
 *
 * 비밀번호를 표현하는 값 객체입니다.
 * 암호화된 비밀번호만 저장하며, 원본 비밀번호는 메모리에 남기지 않습니다.
 *
 * 보안 특징:
 * 1. 원본 비밀번호는 저장하지 않음
 * 2. 암호화 로직을 캡슐화
 * 3. 불변성 보장
 * 4. toString()에서 비밀번호 노출 방지
 *
 * 사용 예시:
 * <pre>
 * // 신규 비밀번호 생성 (암호화)
 * Password password = Password.fromRaw("password123", passwordEncoder);
 *
 * // 기존 암호화된 비밀번호 재구성
 * Password password = Password.fromEncrypted("$2a$10$...");
 *
 * // 비밀번호 검증
 * boolean matches = password.matches("password123", passwordEncoder);
 * </pre>
 */
public class Password extends BaseValueObject {

    /**
     * 비밀번호 최소 길이
     */
    private static final int MIN_LENGTH = 8;

    /**
     * 비밀번호 최대 길이
     */
    private static final int MAX_LENGTH = 100;

    /**
     * 숫자 포함 패턴
     */
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    /**
     * 영문자 포함 패턴
     */
    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*");

    /**
     * 암호화된 비밀번호
     * 원본 비밀번호는 절대 저장하지 않음!
     */
    private final String encrypted;

    /**
     * Private 생성자 - 외부에서 직접 생성 불가
     * 정적 팩토리 메서드를 통해서만 생성
     *
     * @param encrypted 암호화된 비밀번호
     */
    private Password(String encrypted) {
        this.encrypted = requireNonEmpty(encrypted, "Encrypted password");
    }

    /**
     * 원본 비밀번호로부터 Password 생성 (신규 사용자 등록, 비밀번호 변경 시)
     *
     * @param rawPassword 원본 비밀번호
     * @param encoder 비밀번호 인코더
     * @return 암호화된 Password 인스턴스
     * @throws IllegalArgumentException 비밀번호 규칙을 만족하지 않는 경우
     */
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        validateRawPassword(rawPassword);
        String encrypted = encoder.encode(rawPassword);
        return new Password(encrypted);
    }

    /**
     * 암호화된 비밀번호로부터 Password 생성 (DB에서 조회 시, 재구성)
     *
     * @param encrypted 이미 암호화된 비밀번호
     * @return Password 인스턴스
     */
    public static Password fromEncrypted(String encrypted) {
        return new Password(encrypted);
    }

    /**
     * 원본 비밀번호 유효성 검증
     *
     * 비밀번호 규칙:
     * 1. 8자 이상 100자 이하
     * 2. 최소 1개의 숫자 포함
     * 3. 최소 1개의 영문자 포함
     *
     * @param rawPassword 검증할 원본 비밀번호
     * @throws IllegalArgumentException 규칙을 만족하지 않는 경우
     */
    private static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }

        // 길이 검증
        if (rawPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("비밀번호는 최소 %d자 이상이어야 합니다", MIN_LENGTH)
            );
        }

        if (rawPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("비밀번호는 최대 %d자 이하여야 합니다", MAX_LENGTH)
            );
        }

        // 숫자 포함 검증
        if (!DIGIT_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 최소 1개의 숫자를 포함해야 합니다");
        }

        // 영문자 포함 검증
        if (!LETTER_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 최소 1개의 영문자를 포함해야 합니다");
        }

        // 공백 포함 검증
        if (rawPassword.contains(" ")) {
            throw new IllegalArgumentException("비밀번호는 공백을 포함할 수 없습니다");
        }
    }

    /**
     * 입력된 원본 비밀번호가 이 비밀번호와 일치하는지 확인
     *
     * @param rawPassword 확인할 원본 비밀번호
     * @param encoder 비밀번호 인코더
     * @return 일치하면 true
     */
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, this.encrypted);
    }

    /**
     * 암호화된 비밀번호 값 반환
     * 주의: 이 값은 암호화된 값이므로 안전하게 저장/전송 가능
     *
     * @return 암호화된 비밀번호
     */
    public String getValue() {
        return encrypted;
    }

    /**
     * 비밀번호 강도 평가 (선택적 기능)
     *
     * @param rawPassword 평가할 원본 비밀번호
     * @return 강도 레벨 (WEAK, MEDIUM, STRONG)
     */
    public static PasswordStrength evaluateStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 길이 점수
        if (rawPassword.length() >= 12) score += 2;
        else if (rawPassword.length() >= 10) score += 1;

        // 복잡도 점수
        if (DIGIT_PATTERN.matcher(rawPassword).matches()) score += 1;
        if (LETTER_PATTERN.matcher(rawPassword).matches()) score += 1;
        if (rawPassword.matches(".*[A-Z].*")) score += 1; // 대문자
        if (rawPassword.matches(".*[a-z].*")) score += 1; // 소문자
        if (rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 2; // 특수문자

        if (score >= 6) return PasswordStrength.STRONG;
        if (score >= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{encrypted};
    }

    /**
     * 문자열 표현
     * 보안을 위해 비밀번호 값은 노출하지 않음!
     */
    @Override
    public String toString() {
        return "Password{[PROTECTED]}";
    }

    /**
     * 비밀번호 강도 열거형
     */
    public enum PasswordStrength {
        WEAK("약함"),
        MEDIUM("보통"),
        STRONG("강함");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 비밀번호 인코더 인터페이스
     * 도메인 서비스로 인프라 구현체와 분리
     */
    public interface PasswordEncoder {
        /**
         * 원본 비밀번호를 암호화
         *
         * @param rawPassword 원본 비밀번호
         * @return 암호화된 비밀번호
         */
        String encode(String rawPassword);

        /**
         * 원본 비밀번호와 암호화된 비밀번호가 일치하는지 확인
         *
         * @param rawPassword 원본 비밀번호
         * @param encodedPassword 암호화된 비밀번호
         * @return 일치하면 true
         */
        boolean matches(String rawPassword, String encodedPassword);
    }
}
