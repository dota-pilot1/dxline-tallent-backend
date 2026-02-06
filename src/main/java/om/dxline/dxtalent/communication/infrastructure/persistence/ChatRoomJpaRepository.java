package om.dxline.dxtalent.communication.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ChatRoom JPA Repository (인프라 레이어)
 *
 * Spring Data JPA를 사용하여 ChatRoomJpaEntity를 영속화합니다.
 *
 * 이 인터페이스는 인프라 레이어에 속하며,
 * ChatRoomRepositoryAdapter에서 사용됩니다.
 */
@Repository
public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomJpaEntity, Long> {

    /**
     * 생성자 ID로 채팅방 조회
     *
     * @param createdBy 생성자 ID
     * @return 생성자가 만든 채팅방 목록
     */
    List<ChatRoomJpaEntity> findByCreatedBy(Long createdBy);

    /**
     * 채팅방 타입으로 조회
     *
     * @param type 채팅방 타입
     * @return 해당 타입의 채팅방 목록
     */
    List<ChatRoomJpaEntity> findByType(ChatRoomTypeJpa type);

    /**
     * 특정 사용자가 참여 중인 채팅방 조회
     *
     * participants JSON 필드를 검색합니다.
     *
     * @param userId 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    @Query("SELECT c FROM ChatRoomJpaEntity c WHERE c.participants LIKE %:userIdPattern%")
    List<ChatRoomJpaEntity> findByParticipantUserId(@Param("userIdPattern") String userIdPattern);

    /**
     * 두 사용자 간의 DIRECT 채팅방 찾기
     *
     * @param type DIRECT 타입
     * @param userId1Pattern 첫 번째 사용자 ID 패턴
     * @param userId2Pattern 두 번째 사용자 ID 패턴
     * @return DIRECT 채팅방
     */
    @Query("SELECT c FROM ChatRoomJpaEntity c WHERE c.type = :type " +
           "AND c.participants LIKE %:userId1Pattern% " +
           "AND c.participants LIKE %:userId2Pattern%")
    Optional<ChatRoomJpaEntity> findDirectChatBetweenUsers(
        @Param("type") ChatRoomTypeJpa type,
        @Param("userId1Pattern") String userId1Pattern,
        @Param("userId2Pattern") String userId2Pattern
    );

    /**
     * 최근 업데이트된 채팅방 조회
     *
     * @return 최근 업데이트 순으로 정렬된 채팅방 목록
     */
    List<ChatRoomJpaEntity> findAllByOrderByUpdatedAtDesc();

    /**
     * 생성자가 특정 사용자이고 타입이 일치하는 채팅방 조회
     *
     * @param createdBy 생성자 ID
     * @param type 채팅방 타입
     * @return 해당하는 채팅방 목록
     */
    List<ChatRoomJpaEntity> findByCreatedByAndType(Long createdBy, ChatRoomTypeJpa type);
}
