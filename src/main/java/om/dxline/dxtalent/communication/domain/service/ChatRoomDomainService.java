package om.dxline.dxtalent.communication.domain.service;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.communication.domain.model.ChatRoom;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.communication.domain.model.ChatRoomName;
import om.dxline.dxtalent.communication.domain.model.ChatRoomType;
import om.dxline.dxtalent.communication.domain.repository.ChatRoomRepository;
import om.dxline.dxtalent.identity.domain.model.UserId;
import org.springframework.stereotype.Service;

/**
 * ChatRoom 도메인 서비스
 *
 * 여러 애그리게이트에 걸쳐있거나, 단일 애그리게이트로 표현하기 어려운
 * 복잡한 비즈니스 로직을 처리합니다.
 *
 * 주요 책임:
 * - 1:1 채팅방 중복 체크 및 찾기/생성
 * - 채팅방 접근 권한 검증
 * - 복잡한 채팅방 검색 로직
 */
@Service
public class ChatRoomDomainService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomDomainService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    /**
     * 1:1 채팅방 찾기 또는 생성
     *
     * 두 사용자 간의 기존 1:1 채팅방이 있으면 반환하고,
     * 없으면 새로 생성합니다.
     *
     * 비즈니스 규칙:
     * - 동일한 두 사용자 간의 1:1 채팅방은 하나만 존재해야 함
     * - 사용자 순서에 관계없이 동일한 채팅방을 찾아야 함
     *   (user1-user2 와 user2-user1은 같은 채팅방)
     *
     * @param user1 첫 번째 사용자
     * @param user2 두 번째 사용자
     * @return 기존 또는 새로 생성된 1:1 채팅방
     */
    public ChatRoom findOrCreateDirectChat(UserId user1, UserId user2) {
        if (user1.equals(user2)) {
            throw new IllegalArgumentException(
                "자기 자신과는 채팅할 수 없습니다"
            );
        }

        // 기존 1:1 채팅방 찾기
        Optional<ChatRoom> existingRoom =
            chatRoomRepository.findDirectChatBetweenUsers(user1, user2);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // 새로운 1:1 채팅방 생성
        ChatRoomName chatRoomName = new ChatRoomName("Direct Chat");
        ChatRoom newRoom = ChatRoom.createDirect(
            chatRoomName,
            user1, // 생성자
            user1,
            user2
        );

        return chatRoomRepository.save(newRoom);
    }

    /**
     * 사용자가 채팅방에 접근할 수 있는지 확인
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 접근 가능하면 true
     */
    public boolean canUserAccessRoom(ChatRoomId chatRoomId, UserId userId) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(
            chatRoomId
        );

        if (chatRoomOpt.isEmpty()) {
            return false;
        }

        ChatRoom chatRoom = chatRoomOpt.get();
        return chatRoom.isParticipant(userId);
    }

    /**
     * 사용자가 참여 중인 모든 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    public List<ChatRoom> findUserChatRooms(UserId userId) {
        return chatRoomRepository.findByParticipant(userId);
    }

    /**
     * 사용자의 안 읽은 메시지가 있는 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 안 읽은 메시지가 있는 채팅방 목록
     */
    public List<ChatRoom> findChatRoomsWithUnreadMessages(UserId userId) {
        List<ChatRoom> userChatRooms = findUserChatRooms(userId);

        return userChatRooms
            .stream()
            .filter(chatRoom -> chatRoom.getUnreadCount(userId) > 0)
            .toList();
    }

    /**
     * 채팅방이 비어있는지 확인 (참여자가 없는지)
     *
     * @param chatRoomId 채팅방 ID
     * @return 비어있으면 true
     */
    public boolean isChatRoomEmpty(ChatRoomId chatRoomId) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(
            chatRoomId
        );

        if (chatRoomOpt.isEmpty()) {
            return true;
        }

        return chatRoomOpt.get().getParticipantCount() == 0;
    }

    /**
     * 그룹 채팅방에서 사용자 초대 가능 여부 확인
     *
     * @param chatRoomId 채팅방 ID
     * @param inviterId 초대하는 사용자 ID
     * @param inviteeId 초대받는 사용자 ID
     * @return 초대 가능하면 true
     */
    public boolean canInviteUser(
        ChatRoomId chatRoomId,
        UserId inviterId,
        UserId inviteeId
    ) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(
            chatRoomId
        );

        if (chatRoomOpt.isEmpty()) {
            return false;
        }

        ChatRoom chatRoom = chatRoomOpt.get();

        // DIRECT 타입은 초대 불가
        if (chatRoom.getType() == ChatRoomType.DIRECT) {
            return false;
        }

        // 초대자가 참여자여야 함
        if (!chatRoom.isParticipant(inviterId)) {
            return false;
        }

        // 초대받는 사람이 이미 참여 중이면 불가
        if (chatRoom.isParticipant(inviteeId)) {
            return false;
        }

        return true;
    }

    /**
     * 특정 사용자들이 모두 참여 중인 그룹 채팅방 찾기
     *
     * @param userIds 사용자 ID 목록
     * @return 해당하는 채팅방 목록
     */
    public List<ChatRoom> findGroupChatRoomsWithAllUsers(List<UserId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException(
                "사용자 목록은 비어있을 수 없습니다"
            );
        }

        return chatRoomRepository.findGroupChatRoomsWithAllParticipants(
            userIds
        );
    }
}
