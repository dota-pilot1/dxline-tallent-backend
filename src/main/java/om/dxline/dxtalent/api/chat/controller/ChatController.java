package om.dxline.dxtalent.api.chat.controller;

import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chat.dto.ChatRequest;
import om.dxline.dxtalent.api.chat.dto.ChatResponse;
import om.dxline.dxtalent.api.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
