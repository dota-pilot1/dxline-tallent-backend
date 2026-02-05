package om.dxline.dxtalent.api.chatroom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.api.chatroom.dto.MessageResponse;
import om.dxline.dxtalent.api.chatroom.dto.SendMessageRequest;
import om.dxline.dxtalent.api.chatroom.service.ChatMessageService;
import om.dxline.dxtalent.domain.user.entity.User;
import om.dxline.dxtalent.domain.user.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(
        @DestinationVariable UUID roomId,
        @Payload SendMessageRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Unauthorized WebSocket message attempt");
            return;
        }

        User user = userRepository.findByEmail(principal.getName())
            .orElse(null);

        if (user == null) {
            log.warn("User not found: {}", principal.getName());
            return;
        }

        try {
            MessageResponse response = chatMessageService.sendMessage(roomId, request, user.getId());
            log.info("Message sent to room {}: {}", roomId, response.id());
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }
}
