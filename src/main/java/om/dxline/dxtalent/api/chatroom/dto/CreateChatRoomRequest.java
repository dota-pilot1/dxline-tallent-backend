package om.dxline.dxtalent.api.chatroom.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import om.dxline.dxtalent.domain.chatroom.entity.ChatRoomType;

public record CreateChatRoomRequest(
    String name,
    @NotNull(message = "채팅방 유형은 필수입니다") ChatRoomType type,
    List<Long> participantIds
) {}
