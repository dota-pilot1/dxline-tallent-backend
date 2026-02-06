package om.dxline.dxtalent.identity.application.dto;

/**
 * 회원가입 커맨드
 *
 * DDD에서 Command는 시스템에 무언가를 "하라"고 지시하는 의도를 표현합니다.
 * DTO와 달리 의도가 명확하게 드러납니다.
 *
 * 차이점:
 * - DTO (Data Transfer Object): 데이터 전송
 * - Command: 의도와 행위 표현 ("회원가입을 하라")
 *
 * Record를 사용하여 불변성을 보장합니다.
 */
public record SignupCommand(
    String email,
    String password,
    String name
) {
    /**
     * 유효성 검증 메서드
     * Controller에서 호출하여 기본 검증 수행
     */
    public void validate() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
    }

    /**
     * 기존 SignupRequest로부터 생성
     */
    public static SignupCommand from(String email, String password, String name) {
        return new SignupCommand(email, password, name);
    }
}
