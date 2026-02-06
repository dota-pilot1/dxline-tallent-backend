package om.dxline.dxtalent.communication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.communication.domain.model.MessageId;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;

/**
 * 메시지 읽음 도메인 이벤트
 *
 * 사용자가 메시지를 읽었을 때 발행되는 이벤트입니다.
 */
public class MessageReadEvent implements DomainEvent {

    private final UUID eventId;
    private final ChatRoomId chatRoomId;
    private final MessageId messageId;
    private final UserId userId;
    private final LocalDateTime occurredOn;

    public MessageReadEvent(
        ChatRoomId chatRoomId,
        MessageId messageId,
        UserId userId,
        LocalDateTime occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.messageId = messageId;
        this.userId = userId;
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

    public MessageId getMessageId() {
        return messageId;
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return (
            "MessageReadEvent{" +
            "chatRoomId=" +
            chatRoomId +
            ", messageId=" +
            messageId +
            ", userId=" +
            userId +
            ", occurredOn=" +
            occurredOn +
            '}'
        );
    }
}
