package om.dxline.dxtalent.communication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;

/**
 * 참여자 퇴장 도메인 이벤트
 *
 * 사용자가 채팅방을 나갔을 때 발행되는 이벤트입니다.
 */
public class ParticipantLeftEvent implements DomainEvent {

    private final UUID eventId;
    private final ChatRoomId chatRoomId;
    private final UserId userId;
    private final LocalDateTime occurredOn;

    public ParticipantLeftEvent(
        ChatRoomId chatRoomId,
        UserId userId,
        LocalDateTime occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
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

    public UserId getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return (
            "ParticipantLeftEvent{" +
            "chatRoomId=" +
            chatRoomId +
            ", userId=" +
            userId +
            ", occurredOn=" +
            occurredOn +
            '}'
        );
    }
}
