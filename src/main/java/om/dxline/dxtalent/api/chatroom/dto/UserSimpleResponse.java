package om.dxline.dxtalent.api.chatroom.dto;

import om.dxline.dxtalent.domain.user.entity.User;

public record UserSimpleResponse(
    Long id,
    String name,
    String email
) {
    public static UserSimpleResponse from(User user) {
        return new UserSimpleResponse(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }
}
