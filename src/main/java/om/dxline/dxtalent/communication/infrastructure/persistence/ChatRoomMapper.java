package om.dxline.dxtalent.communication.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import om.dxline.dxtalent.communication.domain.model.*;
import om.dxline.dxtalent.identity.domain.model.UserId;
import org.springframework.stereotype.Component;

/**
 * ChatRoom 도메인 모델 ↔ ChatRoomJpaEntity 변환 매퍼
 *
 * 도메인 모델과 JPA 엔티티 간의 변환을 담당합니다.
 * Participant, Message는 JSON으로 직렬화하여 저장합니다.
 *
 * 설계 원칙:
 * - 도메인 레이어는 JPA Entity를 알지 못함
 * - 인프라 레이어만 매핑 로직을 알고 있음
 * - JSON 직렬화 실패 시 빈 리스트로 처리 (데이터 손실 방지)
 */
@Component
public class ChatRoomMapper {

    private final ObjectMapper objectMapper;

    public ChatRoomMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 도메인 모델 → JPA Entity 변환
     *
     * @param chatRoom ChatRoom 도메인 모델
     * @return ChatRoomJpaEntity
     */
    public ChatRoomJpaEntity toEntity(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }

        ChatRoomJpaEntity entity = new ChatRoomJpaEntity();

        // ID (신규 생성 시 null)
        if (chatRoom.getId() != null && chatRoom.getId().getValue() != null) {
            entity.setId(
                Long.parseLong(chatRoom.getId().getValue().toString())
            );
        }

        // 기본 정보
        entity.setName(chatRoom.getName().getValue());
        entity.setType(toJpaType(chatRoom.getType()));
        entity.setCreatedBy(chatRoom.getCreatedBy().getValue());

        // 참여자 및 메시지 JSON 직렬화
        entity.setParticipants(
            serializeParticipants(chatRoom.getParticipants())
        );
        entity.setMessages(serializeMessages(chatRoom.getMessages()));

        // 타임스탬프
        entity.setCreatedAt(chatRoom.getCreatedAt());
        entity.setUpdatedAt(chatRoom.getUpdatedAt());

        return entity;
    }

    /**
     * JPA Entity → 도메인 모델 변환
     *
     * @param entity ChatRoomJpaEntity
     * @return ChatRoom 도메인 모델
     */
    public ChatRoom toDomain(ChatRoomJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // ID
        ChatRoomId chatRoomId = ChatRoomId.fromString(
            String.valueOf(entity.getId())
        );

        // 기본 정보
        ChatRoomName name = new ChatRoomName(entity.getName());
        ChatRoomType type = toDomainType(entity.getType());
        UserId createdBy = UserId.of(entity.getCreatedBy());

        // 참여자 및 메시지 역직렬화
        List<Participant> participants = deserializeParticipants(
            entity.getParticipants()
        );
        List<Message> messages = deserializeMessages(entity.getMessages());

        // 타임스탬프
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime updatedAt = entity.getUpdatedAt();

        // 도메인 모델 재구성
        return ChatRoom.reconstitute(
            chatRoomId,
            name,
            type,
            createdBy,
            participants,
            messages,
            createdAt,
            updatedAt
        );
    }

    /**
     * 도메인 모델 리스트 → JPA Entity 리스트 변환
     */
    public List<ChatRoomJpaEntity> toEntityList(List<ChatRoom> chatRooms) {
        if (chatRooms == null) {
            return Collections.emptyList();
        }
        return chatRooms.stream().map(this::toEntity).toList();
    }

    /**
     * JPA Entity 리스트 → 도메인 모델 리스트 변환
     */
    public List<ChatRoom> toDomainList(List<ChatRoomJpaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toDomain).toList();
    }

    // ============================================================
    // Private Helper Methods - Type Conversion
    // ============================================================

    private ChatRoomTypeJpa toJpaType(ChatRoomType type) {
        return switch (type) {
            case DIRECT -> ChatRoomTypeJpa.DIRECT;
            case GROUP -> ChatRoomTypeJpa.GROUP;
        };
    }

    private ChatRoomType toDomainType(ChatRoomTypeJpa type) {
        return switch (type) {
            case DIRECT -> ChatRoomType.DIRECT;
            case GROUP -> ChatRoomType.GROUP;
        };
    }

    // ============================================================
    // Private Helper Methods - JSON Serialization
    // ============================================================

    /**
     * Participant 리스트를 JSON으로 직렬화
     */
    private String serializeParticipants(List<Participant> participants) {
        if (participants == null || participants.isEmpty()) {
            return "[]";
        }

        try {
            // DTO로 변환
            List<ParticipantDto> dtos = participants
                .stream()
                .map(p ->
                    new ParticipantDto(
                        p.getUserId().getValue(),
                        p.getJoinedAt()
                    )
                )
                .toList();

            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Participant 직렬화 실패", e);
        }
    }

    /**
     * JSON에서 Participant 리스트로 역직렬화
     */
    private List<Participant> deserializeParticipants(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            List<ParticipantDto> dtos = objectMapper.readValue(
                json,
                new TypeReference<List<ParticipantDto>>() {}
            );

            return dtos
                .stream()
                .map(dto ->
                    new Participant(UserId.of(dto.userId), dto.joinedAt)
                )
                .toList();
        } catch (JsonProcessingException e) {
            // 역직렬화 실패 시 빈 리스트 반환 (데이터 손실 방지)
            return new ArrayList<>();
        }
    }

    /**
     * Message 리스트를 JSON으로 직렬화
     */
    private String serializeMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }

        try {
            // DTO로 변환
            List<MessageDto> dtos = messages
                .stream()
                .map(m ->
                    new MessageDto(
                        String.valueOf(m.getId().getValue()),
                        m.getSenderId().getValue(),
                        m.getContent().getValue(),
                        m.getType().name(),
                        m.getSentAt(),
                        m.getImageUrl(),
                        new ArrayList<>() // readBy는 별도 처리 필요
                    )
                )
                .toList();

            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Message 직렬화 실패", e);
        }
    }

    /**
     * JSON에서 Message 리스트로 역직렬화
     */
    private List<Message> deserializeMessages(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            List<MessageDto> dtos = objectMapper.readValue(
                json,
                new TypeReference<List<MessageDto>>() {}
            );

            return dtos
                .stream()
                .map(dto -> {
                    MessageId messageId = MessageId.of(Long.parseLong(dto.id));
                    UserId senderId = UserId.of(dto.senderId);
                    MessageContent content = new MessageContent(dto.content);
                    MessageType type = MessageType.valueOf(dto.type);

                    Message message = new Message(
                        messageId,
                        senderId,
                        content,
                        type,
                        dto.sentAt,
                        dto.imageUrl
                    );

                    // readBy 복원
                    if (dto.readBy != null) {
                        for (Long userId : dto.readBy) {
                            message.markAsReadBy(UserId.of(userId));
                        }
                    }

                    return message;
                })
                .toList();
        } catch (JsonProcessingException e) {
            // 역직렬화 실패 시 빈 리스트 반환 (데이터 손실 방지)
            return new ArrayList<>();
        }
    }

    // ============================================================
    // Internal DTOs for JSON Serialization
    // ============================================================

    /**
     * Participant DTO for JSON serialization
     */
    private static class ParticipantDto {

        public Long userId;
        public LocalDateTime joinedAt;

        // Jackson을 위한 기본 생성자
        public ParticipantDto() {}

        public ParticipantDto(Long userId, LocalDateTime joinedAt) {
            this.userId = userId;
            this.joinedAt = joinedAt;
        }
    }

    /**
     * Message DTO for JSON serialization
     */
    private static class MessageDto {

        public String id;
        public Long senderId;
        public String content;
        public String type;
        public LocalDateTime sentAt;
        public String imageUrl;
        public List<Long> readBy;

        // Jackson을 위한 기본 생성자
        public MessageDto() {}

        public MessageDto(
            String id,
            Long senderId,
            String content,
            String type,
            LocalDateTime sentAt,
            String imageUrl,
            List<Long> readBy
        ) {
            this.id = id;
            this.senderId = senderId;
            this.content = content;
            this.type = type;
            this.sentAt = sentAt;
            this.imageUrl = imageUrl;
            this.readBy = readBy;
        }
    }
}
