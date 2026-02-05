package om.dxline.dxtalent.api.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {

        private Long id;
        private String email;
        private String name;
        private String role;
    }

    public static LoginResponse of(
        String accessToken,
        long expiresIn,
        Long id,
        String email,
        String name,
        String role
    ) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .user(
                UserInfo.builder()
                    .id(id)
                    .email(email)
                    .name(name)
                    .role(role)
                    .build()
            )
            .build();
    }
}
