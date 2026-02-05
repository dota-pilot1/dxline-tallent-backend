package om.dxline.dxtalent.api.chatroom.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chatroom.dto.*;
import om.dxline.dxtalent.domain.chatroom.entity.*;
import om.dxline.dxtalent.domain.chatroom.repository.*;
import om.dxline.dxtalent.domain.user.entity.User;
import om.dxline.dxtalent.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<MessageResponse> getMessages(
        UUID roomId,
        Long userId,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository.findByRoomId(
            roomId,
            pageable
        );

        return messages.map(MessageResponse::from);
    }

    @Transactional
    public MessageResponse sendMessage(
        UUID roomId,
        SendMessageRequest request,
        Long senderId
    ) {
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방을 찾을 수 없습니다")
            );

        User sender = userRepository
            .findById(senderId)
            .orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다")
            );

        ChatMessage message = ChatMessage.builder()
            .chatRoom(chatRoom)
            .sender(sender)
            .content(request.content())
            .type(request.type())
            .imageUrl(request.imageUrl())
            .build();

        messageRepository.save(message);

        // 채팅방 업데이트 시간 갱신
        chatRoom.updateTimestamp();

        MessageResponse response = MessageResponse.from(message);

        // WebSocket으로 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId, response);

        return response;
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = messageRepository
            .findById(messageId)
            .orElseThrow(() ->
                new IllegalArgumentException("메시지를 찾을 수 없습니다")
            );

        // 본인 메시지인지 확인
        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException(
                "본인의 메시지만 삭제할 수 있습니다"
            );
        }

        message.delete();

        // 삭제 이벤트 브로드캐스트
        ChatRoomEvent event = ChatRoomEvent.messageDeleted(
            message.getChatRoom().getId(),
            UserSimpleResponse.from(message.getSender())
        );
        messagingTemplate.convertAndSend(
            "/topic/room/" + message.getChatRoom().getId() + "/events",
            event
        );
    }
}
