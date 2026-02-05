package om.dxline.dxtalent.domain.chatroom.repository;

import om.dxline.dxtalent.domain.chatroom.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @Query("SELECT p FROM ChatRoomParticipant p " +
           "WHERE p.chatRoom.id = :roomId AND p.user.id = :userId")
    Optional<ChatRoomParticipant> findByRoomIdAndUserId(
        @Param("roomId") UUID roomId,
        @Param("userId") Long userId
    );

    boolean existsByChatRoomIdAndUserId(UUID roomId, Long userId);

    void deleteByChatRoomIdAndUserId(UUID roomId, Long userId);
}
