package om.dxline.dxtalent.communication.application.command;

/**
 * 1:1 채팅방 생성 Command
 *
 * 두 사용자 간의 1:1 채팅방을 생성하거나
 * 기존 채팅방이 있으면 반환하기 위한 커맨드입니다.
 */
public class CreateDirectChatCommand {

    private final Long user1Id;
    private final Long user2Id;
    private final String chatRoomName;

    public CreateDirectChatCommand(Long user1Id, Long user2Id, String chatRoomName) {
        if (user1Id == null) {
            throw new IllegalArgumentException("user1Id는 필수입니다");
        }
        if (user2Id == null) {
            throw new IllegalArgumentException("user2Id는 필수입니다");
        }
        if (user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다");
        }

        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.chatRoomName = chatRoomName != null ? chatRoomName : "Direct Chat";
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    @Override
    public String toString() {
        return "CreateDirectChatCommand{" +
            "user1Id=" + user1Id +
            ", user2Id=" + user2Id +
            ", chatRoomName='" + chatRoomName + '\'' +
            '}';
    }
}
