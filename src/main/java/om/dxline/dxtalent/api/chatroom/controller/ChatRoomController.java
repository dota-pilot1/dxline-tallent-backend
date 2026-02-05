package om.dxline.dxtalent.api.chatroom.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chatroom.dto.ChatRoomResponse;
import om.dxline.dxtalent.api.chatroom.dto.CreateChatRoomRequest;
import om.dxline.dxtalent.api.chatroom.dto.InviteUserRequest;
import om.dxline.dxtalent.api.chatroom.service.ChatRoomService;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(
        @AuthenticationPrincipal User user
    ) {
        List<ChatRoomResponse> chatRooms = chatRoomService.getMyChatRooms(
            user.getId()
        );
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
        @PathVariable UUID roomId,
        @AuthenticationPrincipal User user
    ) {
        ChatRoomResponse chatRoom = chatRoomService.getChatRoom(
            roomId,
            user.getId()
        );
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(
        @Valid @RequestBody CreateChatRoomRequest request,
        @AuthenticationPrincipal User user
    ) {
        ChatRoomResponse chatRoom = chatRoomService.createChatRoom(
            request,
            user.getId()
        );
        return ResponseEntity.ok(chatRoom);
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
        @PathVariable UUID roomId,
        @AuthenticationPrincipal User user
    ) {
        chatRoomService.leaveChatRoom(roomId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable UUID roomId,
        @AuthenticationPrincipal User user
    ) {
        chatRoomService.updateLastReadAt(roomId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
