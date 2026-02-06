package om.dxline.dxtalent.communication.application;

import java.util.List;
import java.util.stream.Collectors;
import om.dxline.dxtalent.communication.application.command.CreateDirectChatCommand;
import om.dxline.dxtalent.communication.application.command.CreateGroupChatCommand;
import om.dxline.dxtalent.communication.application.command.SendMessageCommand;
import om.dxline.dxtalent.communication.application.result.ChatRoomResult;
import om.dxline.dxtalent.communication.application.result.ChatRoomResult.MessageInfo;
import om.dxline.dxtalent.communication.application.result.ChatRoomResult.ParticipantInfo;
import om.dxline.dxtalent.communication.application.result.MessageResult;
import om.dxline.dxtalent.communication.domain.model.*;
import om.dxline.dxtalent.communication.domain.repository.ChatRoomRepository;
import om.dxline.dxtalent.communication.domain.service.ChatRoomDomainService;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChatRoom Application Service
 *
 * 채팅방과 관련된 유스케이스를 처리하는 애플리케이션 서비스입니다.
 *
 * 역할:
 * - 유스케이스 오케스트레이션
 * - 트랜잭션 경계 설정
 * - 도메인 이벤트 발행
 * - 도메인 모델 → DTO 변환
 *
 * 주의사항:
 * - 비즈니스 로직은 도메인 레이어에 위치
 * - 이 서비스는 도메인 객체를 조율하는 역할만 수행
 * - 도메인 모델을 외부에 직접 노출하지 않음 (Result 객체 사용)
 */
@Service
@Transactional(readOnly = true)
public class ChatRoomApplicationService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomDomainService chatRoomDomainService;
    private final DomainEventPublisher eventPublisher;

    public ChatRoomApplicationService(
        ChatRoomRepository chatRoomRepository,
        ChatRoomDomainService chatRoomDomainService,
        DomainEventPublisher eventPublisher
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomDomainService = chatRoomDomainService;
        this.eventPublisher = eventPublisher;
    }

    // ============================================================
    // 채팅방 생성 유스케이스
    // ============================================================

    /**
     * 1:1 채팅방 생성 또는 조회
     *
     * 두 사용자 간의 1:1 채팅방이 이미 있으면 기존 채팅방을 반환하고,
     * 없으면 새로 생성합니다.
     *
     * @param command 1:1 채팅방 생성 커맨드
     * @return 생성되거나 조회된 채팅방 정보
     */
    @Transactional
    public ChatRoomResult createOrGetDirectChat(
        CreateDirectChatCommand command
    ) {
        // Command 검증
        if (command == null) {
            throw new IllegalArgumentException(
                "CreateDirectChatCommand는 필수입니다"
            );
        }

        // UserId 변환
        UserId user1 = UserId.of(command.getUser1Id());
        UserId user2 = UserId.of(command.getUser2Id());

        // 도메인 서비스를 통해 1:1 채팅방 찾기 또는 생성
        ChatRoom chatRoom = chatRoomDomainService.findOrCreateDirectChat(
            user1,
            user2
        );

        // 도메인 이벤트 발행
        chatRoom.getDomainEvents().forEach(eventPublisher::publish);
        chatRoom.clearDomainEvents();

        // 도메인 모델 → Result 변환
        return toChatRoomResult(chatRoom, user1);
    }

    /**
     * 그룹 채팅방 생성
     *
     * @param command 그룹 채팅방 생성 커맨드
     * @return 생성된 채팅방 정보
     */
    @Transactional
    public ChatRoomResult createGroupChat(CreateGroupChatCommand command) {
        // Command 검증
        if (command == null) {
            throw new IllegalArgumentException(
                "CreateGroupChatCommand는 필수입니다"
            );
        }
        if (
            command.getChatRoomName() == null ||
            command.getChatRoomName().trim().isEmpty()
        ) {
            throw new IllegalArgumentException("채팅방 이름은 필수입니다");
        }
        if (
            command.getParticipantIds() == null ||
            command.getParticipantIds().size() < 2
        ) {
            throw new IllegalArgumentException(
                "그룹 채팅방은 최소 2명 이상이어야 합니다"
            );
        }

        // 도메인 객체 생성
        ChatRoomName name = new ChatRoomName(command.getChatRoomName());
        UserId createdBy = UserId.of(command.getCreatedBy());
        List<UserId> participantIds = command
            .getParticipantIds()
            .stream()
            .map(UserId::of)
            .collect(Collectors.toList());

        // 애그리게이트 생성
        ChatRoom chatRoom = ChatRoom.createGroup(
            name,
            createdBy,
            participantIds
        );

        // 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 도메인 이벤트 발행
        savedChatRoom.getDomainEvents().forEach(eventPublisher::publish);
        savedChatRoom.clearDomainEvents();

        // Result 변환
        return toChatRoomResult(savedChatRoom, createdBy);
    }

    // ============================================================
    // 메시지 전송 유스케이스
    // ============================================================

    /**
     * 메시지 전송
     *
     * @param command 메시지 전송 커맨드
     * @return 전송된 메시지 정보
     */
    @Transactional
    public MessageResult sendMessage(SendMessageCommand command) {
        // Command 검증
        if (command == null) {
            throw new IllegalArgumentException(
                "SendMessageCommand는 필수입니다"
            );
        }
        if (
            command.getContent() == null ||
            command.getContent().trim().isEmpty()
        ) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }

        // ChatRoom 조회
        ChatRoomId chatRoomId = ChatRoomId.fromString(command.getChatRoomId());
        ChatRoom chatRoom = chatRoomRepository
            .findById(chatRoomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + command.getChatRoomId()
                )
            );

        // UserId, MessageContent 생성
        UserId senderId = UserId.of(command.getSenderId());
        MessageContent content = new MessageContent(command.getContent());

        // 메시지 전송 (도메인 로직)
        Message message;
        if (command.isImageMessage()) {
            message = chatRoom.sendImageMessage(
                senderId,
                content,
                command.getImageUrl()
            );
        } else {
            message = chatRoom.sendMessage(senderId, content);
        }

        // 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 도메인 이벤트 발행
        savedChatRoom.getDomainEvents().forEach(eventPublisher::publish);
        savedChatRoom.clearDomainEvents();

        // Result 변환
        return toMessageResult(message);
    }

    // ============================================================
    // 참여자 관리 유스케이스
    // ============================================================

    /**
     * 채팅방에 참여자 추가
     *
     * @param chatRoomId 채팅방 ID
     * @param inviterId 초대하는 사용자 ID
     * @param inviteeId 초대받는 사용자 ID
     * @return 업데이트된 채팅방 정보
     */
    @Transactional
    public ChatRoomResult addParticipant(
        String chatRoomId,
        Long inviterId,
        Long inviteeId
    ) {
        // 검증
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }
        if (inviterId == null || inviteeId == null) {
            throw new IllegalArgumentException(
                "초대자 및 초대받는 사용자 ID는 필수입니다"
            );
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // UserId 변환
        UserId inviter = UserId.of(inviterId);
        UserId invitee = UserId.of(inviteeId);

        // 참여자 추가 (도메인 로직)
        chatRoom.addParticipant(invitee, inviter);

        // 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 도메인 이벤트 발행
        savedChatRoom.getDomainEvents().forEach(eventPublisher::publish);
        savedChatRoom.clearDomainEvents();

        // Result 변환
        return toChatRoomResult(savedChatRoom, inviter);
    }

    /**
     * 채팅방에서 나가기
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 나가는 사용자 ID
     */
    @Transactional
    public void leaveChat(String chatRoomId, Long userId) {
        // 검증
        if (chatRoomId == null || userId == null) {
            throw new IllegalArgumentException(
                "채팅방 ID와 사용자 ID는 필수입니다"
            );
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // 참여자 제거 (도메인 로직)
        UserId user = UserId.of(userId);
        chatRoom.removeParticipant(user);

        // 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 도메인 이벤트 발행
        savedChatRoom.getDomainEvents().forEach(eventPublisher::publish);
        savedChatRoom.clearDomainEvents();
    }

    // ============================================================
    // 메시지 읽음 처리 유스케이스
    // ============================================================

    /**
     * 메시지 읽음 처리
     *
     * @param chatRoomId 채팅방 ID
     * @param messageId 메시지 ID
     * @param userId 읽은 사용자 ID
     */
    @Transactional
    public void markMessageAsRead(
        String chatRoomId,
        String messageId,
        Long userId
    ) {
        // 검증
        if (chatRoomId == null || messageId == null || userId == null) {
            throw new IllegalArgumentException(
                "채팅방 ID, 메시지 ID, 사용자 ID는 필수입니다"
            );
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // 메시지 읽음 처리 (도메인 로직)
        MessageId msgId = MessageId.of(Long.parseLong(messageId));
        UserId user = UserId.of(userId);
        chatRoom.markMessageAsRead(msgId, user);

        // 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 도메인 이벤트 발행
        savedChatRoom.getDomainEvents().forEach(eventPublisher::publish);
        savedChatRoom.clearDomainEvents();
    }

    // ============================================================
    // 조회 유스케이스
    // ============================================================

    /**
     * 채팅방 상세 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 조회하는 사용자 ID
     * @return 채팅방 정보
     */
    public ChatRoomResult getChatRoom(String chatRoomId, Long userId) {
        // 검증
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // 접근 권한 확인
        UserId user = UserId.of(userId);
        if (!chatRoom.isParticipant(user)) {
            throw new IllegalArgumentException("채팅방에 접근할 수 없습니다");
        }

        // Result 변환
        return toChatRoomResult(chatRoom, user);
    }

    /**
     * 사용자가 참여 중인 모든 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 채팅방 목록
     */
    public List<ChatRoomResult> getUserChatRooms(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }

        UserId user = UserId.of(userId);
        List<ChatRoom> chatRooms = chatRoomDomainService.findUserChatRooms(
            user
        );

        return chatRooms
            .stream()
            .map(chatRoom -> toChatRoomResult(chatRoom, user))
            .collect(Collectors.toList());
    }

    /**
     * 채팅방의 최근 메시지 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 조회하는 사용자 ID
     * @param limit 조회할 메시지 개수
     * @return 메시지 목록
     */
    public List<MessageResult> getRecentMessages(
        String chatRoomId,
        Long userId,
        int limit
    ) {
        // 검증
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // 접근 권한 확인
        UserId user = UserId.of(userId);
        if (!chatRoom.isParticipant(user)) {
            throw new IllegalArgumentException("채팅방에 접근할 수 없습니다");
        }

        // 최근 메시지 조회
        List<Message> messages = chatRoom.getRecentMessages(limit);

        return messages
            .stream()
            .map(this::toMessageResult)
            .collect(Collectors.toList());
    }

    /**
     * 안 읽은 메시지 개수 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 안 읽은 메시지 개수
     */
    public int getUnreadCount(String chatRoomId, Long userId) {
        // 검증
        if (chatRoomId == null || userId == null) {
            throw new IllegalArgumentException(
                "채팅방 ID와 사용자 ID는 필수입니다"
            );
        }

        // ChatRoom 조회
        ChatRoomId roomId = ChatRoomId.fromString(chatRoomId);
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "채팅방을 찾을 수 없습니다: " + chatRoomId
                )
            );

        // 안 읽은 메시지 개수
        UserId user = UserId.of(userId);
        return chatRoom.getUnreadCount(user);
    }

    // ============================================================
    // Private Helper Methods - DTO Conversion
    // ============================================================

    /**
     * ChatRoom 도메인 모델 → ChatRoomResult 변환
     */
    private ChatRoomResult toChatRoomResult(
        ChatRoom chatRoom,
        UserId currentUser
    ) {
        // 참여자 정보
        List<ParticipantInfo> participants = chatRoom
            .getParticipants()
            .stream()
            .map(p ->
                new ParticipantInfo(p.getUserId().getValue(), p.getJoinedAt())
            )
            .collect(Collectors.toList());

        // 마지막 메시지
        Message lastMessage = chatRoom.getLastMessage();
        MessageInfo lastMessageInfo = null;
        if (lastMessage != null) {
            lastMessageInfo = new MessageInfo(
                String.valueOf(lastMessage.getId().getValue()),
                lastMessage.getSenderId().getValue(),
                lastMessage.getContent().getValue(),
                lastMessage.getType().name(),
                lastMessage.getImageUrl(),
                lastMessage.getSentAt()
            );
        }

        // 안 읽은 메시지 개수
        int unreadCount = chatRoom.getUnreadCount(currentUser);

        return new ChatRoomResult(
            chatRoom.getId().getValue().toString(),
            chatRoom.getName().getValue(),
            chatRoom.getType().name(),
            chatRoom.getCreatedBy().getValue(),
            participants,
            lastMessageInfo,
            unreadCount,
            chatRoom.getCreatedAt(),
            chatRoom.getUpdatedAt()
        );
    }

    /**
     * Message 도메인 모델 → MessageResult 변환
     */
    private MessageResult toMessageResult(Message message) {
        return new MessageResult(
            String.valueOf(message.getId().getValue()),
            message.getSenderId().getValue(),
            message.getContent().getValue(),
            message.getType().name(),
            message.getSentAt(),
            message.getImageUrl(),
            0 // readCount는 별도 계산 필요
        );
    }
}
