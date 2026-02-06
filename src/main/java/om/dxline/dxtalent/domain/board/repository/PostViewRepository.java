package om.dxline.dxtalent.domain.board.repository;

import java.time.LocalDateTime;
import om.dxline.dxtalent.domain.board.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByPostIdAndIpAddressAndViewedAtAfter(Long postId, String ipAddress, LocalDateTime after);

    boolean existsByPostIdAndUserIdAndViewedAtAfter(Long postId, Long userId, LocalDateTime after);
}
