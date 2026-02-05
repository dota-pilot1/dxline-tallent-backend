package om.dxline.dxtalent.domain.chatroom.repository;

import om.dxline.dxtalent.domain.chatroom.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByRoomId(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT 1")
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId " +
           "AND m.deletedAt IS NULL " +
           "AND m.createdAt > :lastReadAt")
    int countUnreadMessages(@Param("roomId") UUID roomId, @Param("lastReadAt") LocalDateTime lastReadAt);
}
