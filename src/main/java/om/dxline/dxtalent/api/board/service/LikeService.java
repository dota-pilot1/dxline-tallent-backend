package om.dxline.dxtalent.api.board.service;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.domain.board.entity.Post;
import om.dxline.dxtalent.domain.board.entity.PostLike;
import om.dxline.dxtalent.domain.board.repository.PostLikeRepository;
import om.dxline.dxtalent.domain.board.repository.PostRepository;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;

    @Transactional
    public Map<String, Object> toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        Optional<PostLike> existing = likeRepository.findByPostIdAndUserId(postId, user.getId());

        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            post.decrementLikeCount();
            liked = false;
        } else {
            PostLike like = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            post.incrementLikeCount();
            liked = true;
        }

        return Map.of(
                "liked", liked,
                "likeCount", post.getLikeCount()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLikeStatus(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        boolean liked = likeRepository.existsByPostIdAndUserId(postId, user.getId());

        return Map.of(
                "liked", liked,
                "likeCount", post.getLikeCount()
        );
    }
}
