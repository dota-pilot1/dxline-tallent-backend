package om.dxline.dxtalent.communication.domain.repository;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.communication.domain.model.ChatRoom;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.identity.domain.model.UserId;

/**
 * ChatRoom Repository 인터페이스 (도메인 레이어)
 *
 * 이 인터페이스는 도메인 레이어에 위치하며,
 * 인프라 레이어에서 구현됩니다 (DIP: Dependency Inversion Principle)
 *
 * Repository는 애그리게이트 루트 단위로만 존재합니다.
 * Message, Participant 등의 내부 엔티티는 별도 Repository가 없습니다.
 */
public interface ChatRoomRepository {

    /**
     * 채팅방 저장
     *
     * @param chatRoom 저장할 채팅방
     * @return 저장된 채팅방
     */
    ChatRoom save(ChatRoom chatRoom);

    /**
     * ID로 채팅방 조회
     *
     * @param id 채팅방 ID
     * @return 채팅방 (없으면 Optional.empty())
     */
    Optional<ChatRoom> findById(ChatRoomId id);

    /**
     * 모든 채팅방 조회
     *
     * @return 모든 채팅방 목록
     */
    List<ChatRoom> findAll();

    /**
     * 특정 사용자가 참여 중인 모든 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    List<ChatRoom> findByParticipant(UserId userId);

    /**
     * 두 사용자 간의 1:1 채팅방 찾기
     *
     * DIRECT 타입 채팅방 중에서
     * 두 사용자가 모두 참여 중인 채팅방을 찾습니다.
     *
     * 사용자 순서는 상관없습니다.
     * (user1, user2) 와 (user2, user1)은 같은 결과를 반환해야 합니다.
     *
     * @param user1 첫 번째 사용자
     * @param user2 두 번째 사용자
     * @return 1:1 채팅방 (없으면 Optional.empty())
     */
    Optional<ChatRoom> findDirectChatBetweenUsers(UserId user1, UserId user2);

    /**
     * 특정 사용자들이 모두 참여 중인 그룹 채팅방 찾기
     *
     * GROUP 타입 채팅방 중에서
     * 주어진 모든 사용자가 참여 중인 채팅방을 찾습니다.
     *
     * @param userIds 사용자 ID 목록
     * @return 해당하는 그룹 채팅방 목록
     */
    List<ChatRoom> findGroupChatRoomsWithAllParticipants(List<UserId> userIds);

    /**
     * 채팅방 삭제
     *
     * @param chatRoom 삭제할 채팅방
     */
    void delete(ChatRoom chatRoom);

    /**
     * ID로 채팅방 삭제
     *
     * @param id 채팅방 ID
     */
    void deleteById(ChatRoomId id);

    /**
     * 채팅방 존재 여부 확인
     *
     * @param id 채팅방 ID
     * @return 존재하면 true
     */
    boolean existsById(ChatRoomId id);

    /**
     * 채팅방 개수 조회
     *
     * @return 전체 채팅방 개수
     */
    long count();
}
