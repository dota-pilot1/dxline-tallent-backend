package om.dxline.dxtalent.domain.board.repository;

import java.util.List;
import om.dxline.dxtalent.domain.board.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("SELECT c FROM PostComment c LEFT JOIN FETCH c.author " +
           "WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<PostComment> findByPostIdWithAuthor(@Param("postId") Long postId);

    long countByPostIdAndIsDeletedFalse(Long postId);
}
