package om.dxline.dxtalent.communication.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import om.dxline.dxtalent.communication.domain.event.*;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.model.BaseEntity;

/**
 * ChatRoom 애그리게이트 루트 (ChatRoom Aggregate Root)
 *
 * 채팅방과 관련된 모든 비즈니스 로직을 담당하는 애그리게이트 루트입니다.
 *
 * 애그리게이트 구성:
 * - ChatRoom (Root): 채팅방 기본 정보
 * - Participant (내부 엔티티): 참여자 목록
 * - Message (내부 엔티티): 메시지 목록
 *
 * 비즈니스 규칙:
 * - DIRECT 채팅방은 정확히 2명의 참여자만 가능
 * - GROUP 채팅방은 2명 이상 가능
 * - 참여자만 메시지 전송 가능
 * - 중복 참여 불가
 * - 읽음 상태 추적
 */
public class ChatRoom extends BaseEntity<ChatRoomId> {

    // ========== 기본 정보 ==========
    private ChatRoomName name;
    private ChatRoomType type;
    private UserId createdBy;

    // ========== 참여자 및 메시지 (애그리게이트 내부 엔티티) ==========
    private List<Participant> participants;
    private List<Message> messages;

    // ========== 타임스탬프 ==========
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * JPA를 위한 기본 생성자
     */
    protected ChatRoom() {
        super();
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    /**
     * 완전한 생성자 (재구성용)
     */
    private ChatRoom(
        ChatRoomId id,
        ChatRoomName name,
        ChatRoomType type,
        UserId createdBy,
        List<Participant> participants,
        List<Message> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(id);
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // 정적 팩토리 메서드 (Static Factory Methods)
    // ============================================================

    /**
     * 1:1 채팅방 생성
     *
     * @param name 채팅방 이름
     * @param createdBy 생성자 ID
     * @param participant1 참여자1 ID
     * @param participant2 참여자2 ID
     * @return 새로운 ChatRoom 인스턴스
     */
    public static ChatRoom createDirect(
        ChatRoomName name,
        UserId createdBy,
        UserId participant1,
        UserId participant2
    ) {
        // 비즈니스 규칙 검증
        if (participant1.equals(participant2)) {
            throw new IllegalArgumentException("1:1 채팅방은 서로 다른 사용자여야 합니다");
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(ChatRoomId.newId());
        chatRoom.name = name;
        chatRoom.type = ChatRoomType.DIRECT;
        chatRoom.createdBy = createdBy;
        chatRoom.createdAt = LocalDateTime.now();
        chatRoom.updatedAt = LocalDateTime.now();

        // 참여자 추가
        chatRoom.participants.add(new Participant(participant1, LocalDateTime.now()));
        chatRoom.participants.add(new Participant(participant2, LocalDateTime.now()));

        // 도메인 이벤트 발행
        chatRoom.addDomainEvent(new ChatRoomCreatedEvent(
            chatRoom.getId(),
            chatRoom.name,
            chatRoom.type,
            createdBy,
            chatRoom.createdAt
        ));

        return chatRoom;
    }

    /**
     * 그룹 채팅방 생성
     *
     * @param name 채팅방 이름
     * @param createdBy 생성자 ID
     * @param initialParticipants 초기 참여자 목록
     * @return 새로운 ChatRoom 인스턴스
     */
    public static ChatRoom createGroup(
        ChatRoomName name,
        UserId createdBy,
        List<UserId> initialParticipants
    ) {
        // 비즈니스 규칙 검증
        if (initialParticipants == null || initialParticipants.size() < 2) {
            throw new IllegalArgumentException("그룹 채팅방은 최소 2명 이상이어야 합니다");
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(ChatRoomId.newId());
        chatRoom.name = name;
        chatRoom.type = ChatRoomType.GROUP;
        chatRoom.createdBy = createdBy;
        chatRoom.createdAt = LocalDateTime.now();
        chatRoom.updatedAt = LocalDateTime.now();

        // 참여자 추가
        LocalDateTime joinedAt = LocalDateTime.now();
        for (UserId participantId : initialParticipants) {
            chatRoom.participants.add(new Participant(participantId, joinedAt));
        }

        // 도메인 이벤트 발행
        chatRoom.addDomainEvent(new ChatRoomCreatedEvent(
            chatRoom.getId(),
            chatRoom.name,
            chatRoom.type,
            createdBy,
            chatRoom.createdAt
        ));

        return chatRoom;
    }

    /**
     * Repository에서 재구성
     */
    public static ChatRoom reconstitute(
        ChatRoomId id,
        ChatRoomName name,
        ChatRoomType type,
        UserId createdBy,
        List<Participant> participants,
        List<Message> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ChatRoom(
            id, name, type, createdBy, participants, messages,
            createdAt, updatedAt
        );
    }

    // ============================================================
    // 비즈니스 메서드 (Business Methods)
    // ============================================================

    /**
     * 메시지 전송
     *
     * @param senderId 발신자 ID
     * @param content 메시지 내용
     * @return 생성된 메시지
     */
    public Message sendMessage(UserId senderId, MessageContent content) {
        // 비즈니스 규칙 검증: 참여자만 메시지 전송 가능
        if (!isParticipant(senderId)) {
            throw new IllegalStateException("채팅방 참여자만 메시지를 전송할 수 있습니다");
        }

        // 메시지 생성
        Message message = new Message(
            MessageId.newId(),
            senderId,
            content,
            MessageType.TEXT,
            LocalDateTime.now()
        );

        messages.add(message);
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(new MessageSentEvent(
            this.getId(),
            message.getId(),
            senderId,
            content,
            message.getSentAt()
        ));

        return message;
    }

    /**
     * 이미지 메시지 전송
     *
     * @param senderId 발신자 ID
     * @param content 메시지 내용 (설명)
     * @param imageUrl 이미지 URL
     * @return 생성된 메시지
     */
    public Message sendImageMessage(UserId senderId, MessageContent content, String imageUrl) {
        // 비즈니스 규칙 검증
        if (!isParticipant(senderId)) {
            throw new IllegalStateException("채팅방 참여자만 메시지를 전송할 수 있습니다");
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다");
        }

        // 메시지 생성
        Message message = new Message(
            MessageId.newId(),
            senderId,
            content,
            MessageType.IMAGE,
            LocalDateTime.now(),
            imageUrl
        );

        messages.add(message);
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(new MessageSentEvent(
            this.getId(),
            message.getId(),
            senderId,
            content,
            message.getSentAt()
        ));

        return message;
    }

    /**
     * 참여자 추가
     *
     * @param userId 추가할 사용자 ID
     * @param invitedBy 초대한 사용자 ID
     */
    public void addParticipant(UserId userId, UserId invitedBy) {
        // 비즈니스 규칙 검증: DIRECT 채팅방은 참여자 추가 불가
        if (type == ChatRoomType.DIRECT) {
            throw new IllegalStateException("1:1 채팅방에는 참여자를 추가할 수 없습니다");
        }

        // 비즈니스 규칙 검증: 초대자는 참여자여야 함
        if (!isParticipant(invitedBy)) {
            throw new IllegalStateException("채팅방 참여자만 다른 사용자를 초대할 수 있습니다");
        }

        // 비즈니스 규칙 검증: 중복 참여 방지
        if (isParticipant(userId)) {
            throw new IllegalArgumentException("이미 채팅방에 참여 중인 사용자입니다");
        }

        // 참여자 추가
        participants.add(new Participant(userId, LocalDateTime.now()));
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(new ParticipantJoinedEvent(
            this.getId(),
            userId,
            invitedBy,
            LocalDateTime.now()
        ));
    }

    /**
     * 참여자 제거
     *
     * @param userId 제거할 사용자 ID
     */
    public void removeParticipant(UserId userId) {
        // 비즈니스 규칙 검증: DIRECT 채팅방은 참여자 제거 불가
        if (type == ChatRoomType.DIRECT) {
            throw new IllegalStateException("1:1 채팅방에서는 나갈 수 없습니다");
        }

        // 비즈니스 규칙 검증: 참여자여야 제거 가능
        if (!isParticipant(userId)) {
            throw new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다");
        }

        // 참여자 제거
        participants.removeIf(p -> p.getUserId().equals(userId));
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(new ParticipantLeftEvent(
            this.getId(),
            userId,
            LocalDateTime.now()
        ));

        // 참여자가 모두 나가면 채팅방 종료
        if (participants.isEmpty()) {
            // TODO: 채팅방 종료 로직
        }
    }

    /**
     * 메시지 읽음 처리
     *
     * @param messageId 메시지 ID
     * @param userId 읽은 사용자 ID
     */
    public void markMessageAsRead(MessageId messageId, UserId userId) {
        // 비즈니스 규칙 검증
        if (!isParticipant(userId)) {
            throw new IllegalStateException("채팅방 참여자만 메시지를 읽을 수 있습니다");
        }

        // 메시지 찾기
        Message message = messages.stream()
            .filter(m -> m.getId().equals(messageId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));

        // 자신이 보낸 메시지는 읽음 처리 불필요
        if (message.getSenderId().equals(userId)) {
            return;
        }

        message.markAsReadBy(userId);

        // 도메인 이벤트 발행
        addDomainEvent(new MessageReadEvent(
            this.getId(),
            messageId,
            userId,
            LocalDateTime.now()
        ));
    }

    /**
     * 특정 사용자의 안 읽은 메시지 개수
     *
     * @param userId 사용자 ID
     * @return 안 읽은 메시지 개수
     */
    public int getUnreadCount(UserId userId) {
        // 참여자인지 확인
        if (!isParticipant(userId)) {
            return 0;
        }

        return (int) messages.stream()
            .filter(m -> !m.getSenderId().equals(userId)) // 자신이 보낸 메시지 제외
            .filter(m -> !m.isReadBy(userId)) // 안 읽은 메시지만
            .count();
    }

    /**
     * 최근 메시지 조회
     *
     * @param limit 개수 제한
     * @return 최근 메시지 목록
     */
    public List<Message> getRecentMessages(int limit) {
        return messages.stream()
            .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 마지막 메시지 조회
     *
     * @return 마지막 메시지 (없으면 null)
     */
    public Message getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.stream()
            .max((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()))
            .orElse(null);
    }

    // ============================================================
    // 조회 메서드 (Query Methods)
    // ============================================================

    /**
     * 참여자 여부 확인
     *
     * @param userId 사용자 ID
     * @return 참여자이면 true
     */
    public boolean isParticipant(UserId userId) {
        return participants.stream()
            .anyMatch(p -> p.getUserId().equals(userId));
    }

    /**
     * 참여자 수 반환
     *
     * @return 참여자 수
     */
    public int getParticipantCount() {
        return participants.size();
    }

    /**
     * 참여자 목록 반환 (불변)
     *
     * @return 참여자 목록
     */
    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    /**
     * 메시지 목록 반환 (불변)
     *
     * @return 메시지 목록
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    // Getters
    public ChatRoomName getName() {
        return name;
    }

    public ChatRoomType getType() {
        return type;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
