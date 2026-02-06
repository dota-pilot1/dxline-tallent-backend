package om.dxline.dxtalent.communication.application.command;

import java.util.List;

/**
 * 그룹 채팅방 생성 Command
 *
 * Application Service에서 그룹 채팅방을 생성하기 위한 Command 객체입니다.
 */
public class CreateGroupChatCommand {

    private final String chatRoomName;
    private final Long createdBy;
    private final List<Long> participantIds;

    public CreateGroupChatCommand(
        String chatRoomName,
        Long createdBy,
        List<Long> participantIds
    ) {
        this.chatRoomName = chatRoomName;
        this.createdBy = createdBy;
        this.participantIds = participantIds;
    }

    // Getters
    public String getChatRoomName() {
        return chatRoomName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    @Override
    public String toString() {
        return "CreateGroupChatCommand{" +
            "chatRoomName='" + chatRoomName + '\'' +
            ", createdBy=" + createdBy +
            ", participantIds=" + participantIds +
            '}';
    }
}
