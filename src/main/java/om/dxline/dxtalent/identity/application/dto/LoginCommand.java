package om.dxline.dxtalent.identity.application.dto;

/**
 * 로그인 커맨드
 *
 * 사용자 로그인 의도를 표현하는 Command 객체입니다.
 */
public record LoginCommand(
    String email,
    String password
) {
    /**
     * 유효성 검증
     */
    public void validate() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
    }

    /**
     * 기존 LoginRequest로부터 생성
     */
    public static LoginCommand from(String email, String password) {
        return new LoginCommand(email, password);
    }
}
