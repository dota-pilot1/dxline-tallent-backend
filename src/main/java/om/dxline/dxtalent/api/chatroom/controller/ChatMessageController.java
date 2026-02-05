package om.dxline.dxtalent.api.chatroom.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chatroom.dto.MessageResponse;
import om.dxline.dxtalent.api.chatroom.dto.SendMessageRequest;
import om.dxline.dxtalent.api.chatroom.service.ChatMessageService;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
        @PathVariable UUID roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @AuthenticationPrincipal User user
    ) {
        Page<MessageResponse> messages = chatMessageService.getMessages(roomId, user.getId(), page, size);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
        @PathVariable UUID roomId,
        @Valid @RequestBody SendMessageRequest request,
        @AuthenticationPrincipal User user
    ) {
        MessageResponse message = chatMessageService.sendMessage(roomId, request, user.getId());
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @PathVariable Long messageId,
        @AuthenticationPrincipal User user
    ) {
        chatMessageService.deleteMessage(messageId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
