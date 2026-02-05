package om.dxline.dxtalent.domain.chatroom.repository;

import om.dxline.dxtalent.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "JOIN cr.participants p " +
           "WHERE p.user.id = :userId " +
           "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT cr FROM ChatRoom cr " +
           "JOIN cr.participants p1 " +
           "JOIN cr.participants p2 " +
           "WHERE cr.type = 'DIRECT' " +
           "AND p1.user.id = :userId1 " +
           "AND p2.user.id = :userId2")
    ChatRoom findDirectChatRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
