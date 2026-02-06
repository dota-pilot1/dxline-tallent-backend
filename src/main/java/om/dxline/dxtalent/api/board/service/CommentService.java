package om.dxline.dxtalent.api.board.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.dto.CommentResponse;
import om.dxline.dxtalent.api.board.dto.CreateCommentRequest;
import om.dxline.dxtalent.domain.board.entity.Post;
import om.dxline.dxtalent.domain.board.entity.PostComment;
import om.dxline.dxtalent.domain.board.repository.PostCommentRepository;
import om.dxline.dxtalent.domain.board.repository.PostRepository;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return commentRepository.findByPostIdWithAuthor(postId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request, User author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        PostComment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));
        }

        PostComment comment = PostComment.builder()
                .post(post)
                .author(author)
                .content(request.getContent())
                .parent(parent)
                .build();

        PostComment saved = commentRepository.save(comment);
        post.incrementCommentCount();

        return CommentResponse.from(saved);
    }

    @Transactional
    public CommentResponse updateComment(Long id, String content, User currentUser) {
        PostComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다");
        }

        comment.update(content);
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long id, User currentUser) {
        PostComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("삭제 권한이 없습니다");
        }

        comment.softDelete();
        comment.getPost().decrementCommentCount();
    }
}
