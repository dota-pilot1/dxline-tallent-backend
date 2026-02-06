package om.dxline.dxtalent.api.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.service.LikeService;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 토글")
    @PostMapping("/api/posts/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(likeService.toggleLike(postId, user));
    }

    @Operation(summary = "좋아요 상태 확인")
    @GetMapping("/api/posts/{postId}/like/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(likeService.getLikeStatus(postId, user));
    }
}
