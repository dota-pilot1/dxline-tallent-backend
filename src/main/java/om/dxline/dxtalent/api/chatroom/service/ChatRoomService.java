package om.dxline.dxtalent.api.chatroom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.chatroom.dto.*;
import om.dxline.dxtalent.domain.chatroom.entity.*;
import om.dxline.dxtalent.domain.chatroom.repository.*;
import om.dxline.dxtalent.domain.user.entity.User;
import om.dxline.dxtalent.domain.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ChatRoomResponse> getMyChatRooms(Long userId) {
        // 모든 채팅방 조회 (공개 채팅방)
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        return chatRooms
            .stream()
            .map(room -> {
                MessageResponse lastMessage = messageRepository
                    .findLastMessageByRoomId(room.getId())
                    .map(MessageResponse::from)
                    .orElse(null);

                int unreadCount = 0;

                return ChatRoomResponse.from(room, lastMessage, unreadCount);
            })
            .toList();
    }

    public ChatRoomResponse getChatRoom(UUID roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방을 찾을 수 없습니다")
            );

        MessageResponse lastMessage = messageRepository
            .findLastMessageByRoomId(roomId)
            .map(MessageResponse::from)
            .orElse(null);

        return ChatRoomResponse.from(chatRoom, lastMessage, 0);
    }

    @Transactional
    public ChatRoomResponse createChatRoom(
        CreateChatRoomRequest request,
        Long creatorId
    ) {
        User creator = userRepository
            .findById(creatorId)
            .orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다")
            );

        // 1:1 채팅인 경우 기존 방이 있는지 확인
        if (
            request.type() == ChatRoomType.DIRECT &&
            request.participantIds() != null &&
            request.participantIds().size() == 1
        ) {
            Long otherUserId = request.participantIds().get(0);
            ChatRoom existingRoom = chatRoomRepository.findDirectChatRoom(
                creatorId,
                otherUserId
            );
            if (existingRoom != null) {
                return ChatRoomResponse.from(existingRoom, null, 0);
            }
        }

        // 채팅방 이름 설정
        String roomName = request.name();
        if (roomName == null || roomName.isBlank()) {
            if (
                request.type() == ChatRoomType.DIRECT &&
                request.participantIds() != null &&
                !request.participantIds().isEmpty()
            ) {
                User otherUser = userRepository
                    .findById(request.participantIds().get(0))
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "상대방을 찾을 수 없습니다"
                        )
                    );
                roomName = otherUser.getName();
            } else {
                roomName = "새 그룹 채팅";
            }
        }

        ChatRoom chatRoom = ChatRoom.builder()
            .name(roomName)
            .type(request.type())
            .createdBy(creator)
            .build();

        chatRoomRepository.save(chatRoom);

        // 생성자를 참여자로 추가
        ChatRoomParticipant creatorParticipant = ChatRoomParticipant.builder()
            .chatRoom(chatRoom)
            .user(creator)
            .build();
        participantRepository.save(creatorParticipant);
        chatRoom.addParticipant(creatorParticipant);

        // 다른 참여자들 추가
        if (request.participantIds() != null) {
            for (Long participantId : request.participantIds()) {
                if (!participantId.equals(creatorId)) {
                    User participant = userRepository
                        .findById(participantId)
                        .orElseThrow(() ->
                            new IllegalArgumentException(
                                "참여자를 찾을 수 없습니다: " + participantId
                            )
                        );

                    ChatRoomParticipant roomParticipant =
                        ChatRoomParticipant.builder()
                            .chatRoom(chatRoom)
                            .user(participant)
                            .build();
                    participantRepository.save(roomParticipant);
                    chatRoom.addParticipant(roomParticipant);
                }
            }
        }

        return ChatRoomResponse.from(chatRoom, null, 0);
    }

    @Transactional
    public void leaveChatRoom(UUID roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방을 찾을 수 없습니다")
            );

        ChatRoomParticipant participant = participantRepository
            .findByRoomIdAndUserId(roomId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방에 참여하고 있지 않습니다")
            );

        User user = participant.getUser();

        chatRoom.removeParticipant(participant);
        participantRepository.delete(participant);

        // 채팅방에 참여자가 없으면 채팅방 삭제
        if (chatRoom.getParticipants().isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        } else {
            // 다른 참여자들에게 퇴장 이벤트 전송
            ChatRoomEvent event = ChatRoomEvent.userLeft(
                roomId,
                UserSimpleResponse.from(user)
            );
            messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/events",
                event
            );
        }
    }

    @Transactional
    public void updateLastReadAt(UUID roomId, Long userId) {
        ChatRoomParticipant participant = participantRepository
            .findByRoomIdAndUserId(roomId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방에 참여하고 있지 않습니다")
            );

        participant.updateLastReadAt();
    }

    @Transactional
    public void inviteUser(UUID roomId, Long inviterId, Long inviteeId) {
        ChatRoom chatRoom = chatRoomRepository
            .findById(roomId)
            .orElseThrow(() ->
                new IllegalArgumentException("채팅방을 찾을 수 없습니다")
            );

        // 초대하는 사람이 참여자인지 확인
        if (
            !participantRepository.existsByChatRoomIdAndUserId(
                roomId,
                inviterId
            )
        ) {
            throw new IllegalArgumentException(
                "채팅방에 참여하고 있지 않습니다"
            );
        }

        // 이미 참여 중인지 확인
        if (
            participantRepository.existsByChatRoomIdAndUserId(roomId, inviteeId)
        ) {
            throw new IllegalArgumentException(
                "이미 채팅방에 참여 중인 사용자입니다"
            );
        }

        User invitee = userRepository
            .findById(inviteeId)
            .orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다")
            );

        ChatRoomParticipant participant = ChatRoomParticipant.builder()
            .chatRoom(chatRoom)
            .user(invitee)
            .build();
        participantRepository.save(participant);

        // 입장 이벤트 브로드캐스트
        ChatRoomEvent event = ChatRoomEvent.userJoined(
            roomId,
            UserSimpleResponse.from(invitee)
        );
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/events",
            event
        );
    }
}
