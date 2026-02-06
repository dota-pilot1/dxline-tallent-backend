package om.dxline.dxtalent.identity.application.dto;

import om.dxline.dxtalent.identity.domain.model.Role;
import om.dxline.dxtalent.identity.domain.model.User;
import om.dxline.dxtalent.identity.domain.model.UserId;

/**
 * 로그인 결과
 *
 * DDD에서 Result는 작업의 결과를 표현합니다.
 * Command가 입력이라면, Result는 출력입니다.
 *
 * 불변 객체로 설계하여 안정성을 보장합니다.
 */
public record LoginResult(
    String token,
    long expiresIn,
    Long userId,
    String email,
    String name,
    String role
) {
    /**
     * User 도메인 모델로부터 LoginResult 생성
     *
     * @param user 사용자 도메인 모델
     * @param token JWT 토큰
     * @param expiresIn 토큰 만료 시간 (초)
     * @return LoginResult 인스턴스
     */
    public static LoginResult from(User user, String token, long expiresIn) {
        return new LoginResult(
            token,
            expiresIn,
            user.getId().getValue(),
            user.getEmail().getValue(),
            user.getName().getValue(),
            user.getRole().name()
        );
    }

    /**
     * 기존 LoginResponse 형식으로 생성 (하위 호환성)
     */
    public static LoginResult of(
        String token,
        long expiresIn,
        Long userId,
        String email,
        String name,
        String role
    ) {
        return new LoginResult(token, expiresIn, userId, email, name, role);
    }
}
