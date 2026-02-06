package om.dxline.dxtalent.communication.application.result;

import java.time.LocalDateTime;

/**
 * Message Result DTO
 *
 * 메시지 정보를 Application 레이어에서 Presentation 레이어로 전달하기 위한 DTO입니다.
 */
public class MessageResult {

    private final String id;
    private final Long senderId;
    private final String content;
    private final String type; // TEXT, IMAGE
    private final LocalDateTime sentAt;
    private final String imageUrl;
    private final int readCount;

    public MessageResult(
        String id,
        Long senderId,
        String content,
        String type,
        LocalDateTime sentAt,
        String imageUrl,
        int readCount
    ) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.sentAt = sentAt;
        this.imageUrl = imageUrl;
        this.readCount = readCount;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getReadCount() {
        return readCount;
    }

    @Override
    public String toString() {
        return "MessageResult{" +
            "id='" + id + '\'' +
            ", senderId=" + senderId +
            ", content='" + content + '\'' +
            ", type='" + type + '\'' +
            ", sentAt=" + sentAt +
            ", imageUrl='" + imageUrl + '\'' +
            ", readCount=" + readCount +
            '}';
    }
}
