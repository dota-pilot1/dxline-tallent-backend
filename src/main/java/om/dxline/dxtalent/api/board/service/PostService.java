package om.dxline.dxtalent.api.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.dto.*;
import om.dxline.dxtalent.domain.board.entity.Board;
import om.dxline.dxtalent.domain.board.entity.Post;
import om.dxline.dxtalent.domain.board.entity.PostStatus;
import om.dxline.dxtalent.domain.board.entity.PostView;
import om.dxline.dxtalent.domain.board.repository.BoardRepository;
import om.dxline.dxtalent.domain.board.repository.PostRepository;
import om.dxline.dxtalent.domain.board.repository.PostViewRepository;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final PostViewRepository postViewRepository;

    @Transactional(readOnly = true)
    public PagedResponse<PostListResponse> getPostsByBoard(String boardCode, int page, int limit,
                                                            String sortBy, String sortOrder, String search) {
        Board board = boardRepository.findByCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다: " + boardCode));

        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<Post> postPage;
        if (search != null && !search.isBlank()) {
            postPage = postRepository.findByBoardIdAndStatusAndTitleContaining(
                    board.getId(), PostStatus.PUBLISHED, search, pageable);
        } else {
            postPage = postRepository.findByBoardIdAndStatus(board.getId(), PostStatus.PUBLISHED, pageable);
        }

        List<PostListResponse> data = postPage.getContent().stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());

        return PagedResponse.from(postPage, data);
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPinnedPosts(String boardCode) {
        Board board = boardRepository.findByCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다: " + boardCode));

        return postRepository.findByBoardIdAndIsPinnedTrueAndStatusOrderByCreatedAtDesc(
                        board.getId(), PostStatus.PUBLISHED)
                .stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostDetailResponse getPost(Long id, String ipAddress, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        // 조회수 증가 (24시간 내 중복 방지)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        boolean alreadyViewed = false;

        if (currentUser != null) {
            alreadyViewed = postViewRepository.existsByPostIdAndUserIdAndViewedAtAfter(
                    id, currentUser.getId(), oneDayAgo);
        }
        if (!alreadyViewed && ipAddress != null) {
            alreadyViewed = postViewRepository.existsByPostIdAndIpAddressAndViewedAtAfter(
                    id, ipAddress, oneDayAgo);
        }

        if (!alreadyViewed) {
            post.incrementViewCount();
            PostView view = PostView.builder()
                    .post(post)
                    .user(currentUser)
                    .ipAddress(ipAddress)
                    .build();
            postViewRepository.save(view);
        }

        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostDetailResponse createPost(String boardCode, CreatePostRequest request, User author) {
        Board board = boardRepository.findByCode(boardCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다: " + boardCode));

        Post post = Post.builder()
                .board(board)
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .build();

        return PostDetailResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostDetailResponse updatePost(Long id, UpdatePostRequest request, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다");
        }

        post.update(request.getTitle(), request.getContent());
        return PostDetailResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new IllegalArgumentException("삭제 권한이 없습니다");
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostDetailResponse togglePin(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));
        post.togglePin();
        return PostDetailResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostListResponse> getMyPosts(User user, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByAuthorIdAndStatus(
                user.getId(), PostStatus.PUBLISHED, pageable);

        List<PostListResponse> data = postPage.getContent().stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());

        return PagedResponse.from(postPage, data);
    }
}
