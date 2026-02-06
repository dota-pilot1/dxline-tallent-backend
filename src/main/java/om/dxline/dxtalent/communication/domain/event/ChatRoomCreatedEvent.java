package om.dxline.dxtalent.communication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.communication.domain.model.ChatRoomName;
import om.dxline.dxtalent.communication.domain.model.ChatRoomType;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;

/**
 * 채팅방 생성 도메인 이벤트
 *
 * 새로운 채팅방이 생성되었을 때 발행되는 이벤트입니다.
 */
public class ChatRoomCreatedEvent implements DomainEvent {

    private final UUID eventId;
    private final ChatRoomId chatRoomId;
    private final ChatRoomName chatRoomName;
    private final ChatRoomType chatRoomType;
    private final UserId createdBy;
    private final LocalDateTime occurredOn;

    public ChatRoomCreatedEvent(
        ChatRoomId chatRoomId,
        ChatRoomName chatRoomName,
        ChatRoomType chatRoomType,
        UserId createdBy,
        LocalDateTime occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.chatRoomType = chatRoomType;
        this.createdBy = createdBy;
        this.occurredOn = occurredOn;
    }

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    public ChatRoomId getChatRoomId() {
        return chatRoomId;
    }

    public ChatRoomName getChatRoomName() {
        return chatRoomName;
    }

    public ChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    @Override
    public String toString() {
        return (
            "ChatRoomCreatedEvent{" +
            "chatRoomId=" +
            chatRoomId +
            ", chatRoomName=" +
            chatRoomName +
            ", chatRoomType=" +
            chatRoomType +
            ", createdBy=" +
            createdBy +
            ", occurredOn=" +
            occurredOn +
            '}'
        );
    }
}
