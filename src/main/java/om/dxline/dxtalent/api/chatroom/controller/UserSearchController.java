package om.dxline.dxtalent.api.chatroom.controller;

import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chatroom.dto.UserSimpleResponse;
import om.dxline.dxtalent.domain.user.entity.User;
import om.dxline.dxtalent.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<List<UserSimpleResponse>> searchUsers(
        @RequestParam String q,
        @AuthenticationPrincipal User user
    ) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<UserSimpleResponse> users = userRepository.searchByKeyword(q.trim(), user.getId())
            .stream()
            .map(UserSimpleResponse::from)
            .limit(20)
            .toList();

        return ResponseEntity.ok(users);
    }
}
