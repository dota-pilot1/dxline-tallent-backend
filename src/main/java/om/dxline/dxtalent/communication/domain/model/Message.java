package om.dxline.dxtalent.communication.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import om.dxline.dxtalent.identity.domain.model.UserId;

public class Message {
    private final MessageId id;
    private final UserId senderId;
    private final MessageContent content;
    private final MessageType type;
    private final LocalDateTime sentAt;
    private final String imageUrl;
    private final Set<UserId> readBy;

    public Message(MessageId id, UserId senderId, MessageContent content, MessageType type, LocalDateTime sentAt) {
        this(id, senderId, content, type, sentAt, null);
    }

    public Message(MessageId id, UserId senderId, MessageContent content, MessageType type, LocalDateTime sentAt, String imageUrl) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.sentAt = sentAt;
        this.imageUrl = imageUrl;
        this.readBy = new HashSet<>();
    }

    public void markAsReadBy(UserId userId) {
        readBy.add(userId);
    }

    public boolean isReadBy(UserId userId) {
        return readBy.contains(userId);
    }

    public MessageId getId() { return id; }
    public UserId getSenderId() { return senderId; }
    public MessageContent getContent() { return content; }
    public MessageType getType() { return type; }
    public LocalDateTime getSentAt() { return sentAt; }
    public String getImageUrl() { return imageUrl; }
}
