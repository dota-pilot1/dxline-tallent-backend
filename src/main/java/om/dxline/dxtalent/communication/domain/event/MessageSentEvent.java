package om.dxline.dxtalent.communication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.communication.domain.model.MessageContent;
import om.dxline.dxtalent.communication.domain.model.MessageId;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;

/**
 * 메시지 전송 도메인 이벤트
 *
 * 채팅방에서 메시지가 전송되었을 때 발행되는 이벤트입니다.
 * 이 이벤트를 통해 WebSocket 알림, 푸시 알림 등을 처리할 수 있습니다.
 */
public class MessageSentEvent implements DomainEvent {

    private final UUID eventId;
    private final ChatRoomId chatRoomId;
    private final MessageId messageId;
    private final UserId senderId;
    private final MessageContent content;
    private final LocalDateTime occurredOn;

    public MessageSentEvent(
        ChatRoomId chatRoomId,
        MessageId messageId,
        UserId senderId,
        MessageContent content,
        LocalDateTime occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
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

    public UserId getSenderId() {
        return senderId;
    }

    public MessageContent getContent() {
        return content;
    }

    @Override
    public String toString() {
        return (
            "MessageSentEvent{" +
            "chatRoomId=" +
            chatRoomId +
            ", messageId=" +
            messageId +
            ", senderId=" +
            senderId +
            ", content=" +
            content +
            ", occurredOn=" +
            occurredOn +
            '}'
        );
    }
}
