package om.dxline.dxtalent.api.chatroom.dto;

import om.dxline.dxtalent.domain.chatroom.entity.ChatRoom;
import om.dxline.dxtalent.domain.chatroom.entity.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatRoomResponse(
    UUID id,
    String name,
    ChatRoomType type,
    List<UserSimpleResponse> participants,
    MessageResponse lastMessage,
    int unreadCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom, MessageResponse lastMessage, int unreadCount) {
        List<UserSimpleResponse> participants = chatRoom.getParticipants().stream()
            .map(p -> UserSimpleResponse.from(p.getUser()))
            .toList();

        return new ChatRoomResponse(
            chatRoom.getId(),
            chatRoom.getName(),
            chatRoom.getType(),
            participants,
            lastMessage,
            unreadCount,
            chatRoom.getCreatedAt(),
            chatRoom.getUpdatedAt()
        );
    }
}
