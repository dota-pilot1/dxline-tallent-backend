package om.dxline.dxtalent.api.chatroom.dto;

import om.dxline.dxtalent.domain.chatroom.entity.ChatMessage;
import om.dxline.dxtalent.domain.chatroom.entity.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
    Long id,
    UUID roomId,
    UserSimpleResponse sender,
    String content,
    MessageType type,
    String imageUrl,
    LocalDateTime createdAt
) {
    public static MessageResponse from(ChatMessage message) {
        return new MessageResponse(
            message.getId(),
            message.getChatRoom().getId(),
            UserSimpleResponse.from(message.getSender()),
            message.getContent(),
            message.getType(),
            message.getImageUrl(),
            message.getCreatedAt()
        );
    }
}
