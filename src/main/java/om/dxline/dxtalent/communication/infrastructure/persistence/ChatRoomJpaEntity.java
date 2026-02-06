package om.dxline.dxtalent.communication.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ChatRoom JPA Entity (인프라 레이어)
 *
 * ChatRoom 도메인 모델을 데이터베이스에 영속화하기 위한 JPA 엔티티입니다.
 *
 * 테이블 구조:
 * - 기본 정보: name, type, created_by
 * - 참여자/메시지: participants (JSON), messages (JSON)
 * - 타임스탬프: created_at, updated_at
 *
 * 주의사항:
 * - 이 클래스는 인프라 레이어에만 존재하며 도메인 레이어에 노출되지 않습니다.
 * - Participant, Message는 별도 테이블이 아닌 JSON으로 직렬화하여 저장합니다.
 * - 도메인 모델과의 변환은 ChatRoomMapper가 담당합니다.
 *
 * 설계 결정:
 * - Participant와 Message를 별도 테이블로 분리하지 않고 JSON으로 저장하는 이유:
 *   1. ChatRoom은 애그리게이트 루트이며, Participant/Message는 내부 엔티티
 *   2. 항상 ChatRoom과 함께 로드/저장되므로 별도 테이블 필요 없음
 *   3. 조회 성능 향상 (JOIN 불필요)
 *   4. 트랜잭션 경계가 명확함
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 기본 정보 ==========
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChatRoomTypeJpa type;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // ========== 참여자 및 메시지 (JSON 직렬화) ==========
    /**
     * 참여자 목록 (JSON)
     *
     * 형식: [{"userId": 1, "joinedAt": "2024-02-06T10:00:00"}, ...]
     */
    @Column(name = "participants", columnDefinition = "TEXT", nullable = false)
    private String participants;

    /**
     * 메시지 목록 (JSON)
     *
     * 형식: [{
     *   "id": "uuid",
     *   "senderId": 1,
     *   "content": "메시지 내용",
     *   "type": "TEXT",
     *   "sentAt": "2024-02-06T10:00:00",
     *   "imageUrl": null,
     *   "readBy": [1, 2]
     * }, ...]
     */
    @Column(name = "messages", columnDefinition = "TEXT")
    private String messages;

    // ========== 타임스탬프 ==========
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== 인덱스 ==========
    // - created_by: 생성자로 조회
    // - type: 채팅방 타입으로 필터링
    // - updated_at: 최근 활동 순으로 정렬

    /**
     * 생성 시 자동으로 타임스탬프 설정
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 업데이트 시 자동으로 타임스탬프 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== 정적 팩토리 메서드 ==========

    public static ChatRoomJpaEntity create(
        String name,
        ChatRoomTypeJpa type,
        Long createdBy,
        String participants,
        String messages
    ) {
        ChatRoomJpaEntity entity = new ChatRoomJpaEntity();
        entity.name = name;
        entity.type = type;
        entity.createdBy = createdBy;
        entity.participants = participants;
        entity.messages = messages;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }
}

/**
 * ChatRoomType Enum for JPA
 */
enum ChatRoomTypeJpa {
    DIRECT,  // 1:1 채팅
    GROUP    // 그룹 채팅
}
