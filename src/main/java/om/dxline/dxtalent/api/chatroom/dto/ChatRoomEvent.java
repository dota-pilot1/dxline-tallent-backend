package om.dxline.dxtalent.api.chatroom.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomEvent(
    EventType eventType,
    UUID roomId,
    UserSimpleResponse user,
    LocalDateTime timestamp
) {
    public enum EventType {
        USER_JOINED,
        USER_LEFT,
        MESSAGE_DELETED
    }

    public static ChatRoomEvent userJoined(UUID roomId, UserSimpleResponse user) {
        return new ChatRoomEvent(EventType.USER_JOINED, roomId, user, LocalDateTime.now());
    }

    public static ChatRoomEvent userLeft(UUID roomId, UserSimpleResponse user) {
        return new ChatRoomEvent(EventType.USER_LEFT, roomId, user, LocalDateTime.now());
    }

    public static ChatRoomEvent messageDeleted(UUID roomId, UserSimpleResponse user) {
        return new ChatRoomEvent(EventType.MESSAGE_DELETED, roomId, user, LocalDateTime.now());
    }
}
