package om.dxline.dxtalent.api.chatroom.dto;

import jakarta.validation.constraints.NotBlank;
import om.dxline.dxtalent.domain.chatroom.entity.MessageType;

public record SendMessageRequest(
    @NotBlank(message = "메시지 내용은 필수입니다")
    String content,
    MessageType type,
    String imageUrl
) {
    public SendMessageRequest {
        if (type == null) {
            type = MessageType.TEXT;
        }
    }
}
