package om.dxline.dxtalent.domain.board.repository;

import java.util.List;
import om.dxline.dxtalent.domain.board.entity.Post;
import om.dxline.dxtalent.domain.board.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByBoardIdAndStatus(Long boardId, PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.board.id = :boardId AND p.status = :status " +
           "AND LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Post> findByBoardIdAndStatusAndTitleContaining(
            @Param("boardId") Long boardId,
            @Param("status") PostStatus status,
            @Param("search") String search,
            Pageable pageable);

    List<Post> findByBoardIdAndIsPinnedTrueAndStatusOrderByCreatedAtDesc(Long boardId, PostStatus status);

    Page<Post> findByAuthorIdAndStatus(Long authorId, PostStatus status, Pageable pageable);
}
