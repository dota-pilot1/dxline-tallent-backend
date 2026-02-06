package om.dxline.dxtalent.communication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;

/**
 * 참여자 추가 도메인 이벤트
 *
 * 채팅방에 새로운 참여자가 추가되었을 때 발행되는 이벤트입니다.
 */
public class ParticipantJoinedEvent implements DomainEvent {

    private final UUID eventId;
    private final ChatRoomId chatRoomId;
    private final UserId participantId;
    private final UserId invitedBy;
    private final LocalDateTime occurredOn;

    public ParticipantJoinedEvent(
        ChatRoomId chatRoomId,
        UserId participantId,
        UserId invitedBy,
        LocalDateTime occurredOn
    ) {
        this.eventId = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.participantId = participantId;
        this.invitedBy = invitedBy;
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

    public UserId getParticipantId() {
        return participantId;
    }

    public UserId getInvitedBy() {
        return invitedBy;
    }

    @Override
    public String toString() {
        return (
            "ParticipantJoinedEvent{" +
            "chatRoomId=" +
            chatRoomId +
            ", participantId=" +
            participantId +
            ", invitedBy=" +
            invitedBy +
            ", occurredOn=" +
            occurredOn +
            '}'
        );
    }
}
