package om.dxline.dxtalent.api.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.dto.*;
import om.dxline.dxtalent.api.board.service.PostService;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시판별 게시글 목록")
    @GetMapping("/api/boards/{boardCode}/posts")
    public ResponseEntity<PagedResponse<PostListResponse>> getPostsByBoard(
            @PathVariable String boardCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(postService.getPostsByBoard(boardCode, page, limit, sortBy, sortOrder, search));
    }

    @Operation(summary = "상단 고정 게시글 목록")
    @GetMapping("/api/boards/{boardCode}/posts/pinned")
    public ResponseEntity<List<PostListResponse>> getPinnedPosts(@PathVariable String boardCode) {
        return ResponseEntity.ok(postService.getPinnedPosts(boardCode));
    }

    @Operation(summary = "게시글 작성")
    @PostMapping("/api/boards/{boardCode}/posts")
    public ResponseEntity<PostDetailResponse> createPost(
            @PathVariable String boardCode,
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(boardCode, request, user));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/api/posts/{id}")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long id,
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(postService.getPost(id, ipAddress, user));
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("/api/posts/{id}")
    public ResponseEntity<PostDetailResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.updatePost(id, request, user));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        postService.deletePost(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상단 고정 토글 (관리자)")
    @PatchMapping("/api/posts/{id}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostDetailResponse> togglePin(@PathVariable Long id) {
        return ResponseEntity.ok(postService.togglePin(id));
    }

    @Operation(summary = "내 게시글 목록")
    @GetMapping("/api/posts/my")
    public ResponseEntity<PagedResponse<PostListResponse>> getMyPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getMyPosts(user, page, limit));
    }
}
