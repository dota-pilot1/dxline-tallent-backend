package om.dxline.dxtalent.api.chatroom.dto;

import jakarta.validation.constraints.NotNull;

public record InviteUserRequest(
    @NotNull(message = "초대할 사용자 ID는 필수입니다")
    Long userId
) {}
