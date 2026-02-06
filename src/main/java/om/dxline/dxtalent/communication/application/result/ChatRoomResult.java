package om.dxline.dxtalent.communication.application.result;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ChatRoom Result DTO
 *
 * Application Service에서 반환하는 ChatRoom 정보입니다.
 * 도메인 모델을 외부에 직접 노출하지 않기 위한 DTO입니다.
 */
public class ChatRoomResult {

    private final String chatRoomId;
    private final String name;
    private final String type;
    private final Long createdBy;
    private final List<ParticipantInfo> participants;
    private final MessageInfo lastMessage;
    private final int unreadCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ChatRoomResult(
        String chatRoomId,
        String name,
        String type,
        Long createdBy,
        List<ParticipantInfo> participants,
        MessageInfo lastMessage,
        int unreadCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.chatRoomId = chatRoomId;
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getChatRoomId() {
        return chatRoomId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public MessageInfo getLastMessage() {
        return lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 참여자 정보
     */
    public static class ParticipantInfo {
        private final Long userId;
        private final LocalDateTime joinedAt;

        public ParticipantInfo(Long userId, LocalDateTime joinedAt) {
            this.userId = userId;
            this.joinedAt = joinedAt;
        }

        public Long getUserId() {
            return userId;
        }

        public LocalDateTime getJoinedAt() {
            return joinedAt;
        }
    }

    /**
     * 메시지 정보
     */
    public static class MessageInfo {
        private final String messageId;
        private final Long senderId;
        private final String content;
        private final String type;
        private final String imageUrl;
        private final LocalDateTime sentAt;

        public MessageInfo(
            String messageId,
            Long senderId,
            String content,
            String type,
            String imageUrl,
            LocalDateTime sentAt
        ) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.content = content;
            this.type = type;
            this.imageUrl = imageUrl;
            this.sentAt = sentAt;
        }

        public String getMessageId() {
            return messageId;
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

        public String getImageUrl() {
            return imageUrl;
        }

        public LocalDateTime getSentAt() {
            return sentAt;
        }
    }

    @Override
    public String toString() {
        return "ChatRoomResult{" +
            "chatRoomId='" + chatRoomId + '\'' +
            ", name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", createdBy=" + createdBy +
            ", participantCount=" + (participants != null ? participants.size() : 0) +
            ", unreadCount=" + unreadCount +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
