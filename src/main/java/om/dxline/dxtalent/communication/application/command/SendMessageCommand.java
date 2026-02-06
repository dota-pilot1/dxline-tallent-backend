package om.dxline.dxtalent.communication.application.command;

/**
 * 메시지 전송 커맨드
 *
 * 채팅방에 메시지를 전송하기 위한 커맨드 객체입니다.
 */
public class SendMessageCommand {

    private final String chatRoomId;
    private final Long senderId;
    private final String content;
    private final String messageType; // TEXT, IMAGE
    private final String imageUrl; // IMAGE 타입일 경우

    public SendMessageCommand(
        String chatRoomId,
        Long senderId,
        String content,
        String messageType,
        String imageUrl
    ) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType != null ? messageType : "TEXT";
        this.imageUrl = imageUrl;
    }

    public SendMessageCommand(String chatRoomId, Long senderId, String content) {
        this(chatRoomId, senderId, content, "TEXT", null);
    }

    // Getters
    public String getChatRoomId() {
        return chatRoomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isImageMessage() {
        return "IMAGE".equals(messageType) && imageUrl != null;
    }

    @Override
    public String toString() {
        return "SendMessageCommand{" +
            "chatRoomId='" + chatRoomId + '\'' +
            ", senderId=" + senderId +
            ", content='" + content + '\'' +
            ", messageType='" + messageType + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            '}';
    }
}
