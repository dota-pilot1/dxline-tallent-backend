package om.dxline.dxtalent.communication.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import om.dxline.dxtalent.identity.domain.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ChatRoom 애그리게이트 테스트
 *
 * 채팅방의 비즈니스 로직을 검증합니다.
 */
class ChatRoomTest {

    @Nested
    @DisplayName("1:1 채팅방 생성")
    class CreateDirectChatTest {

        @Test
        @DisplayName("두 사용자 간의 1:1 채팅방을 생성할 수 있다")
        void createDirectChat() {
            // Given
            ChatRoomName name = new ChatRoomName("Direct Chat");
            UserId user1 = UserId.of(1L);
            UserId user2 = UserId.of(2L);

            // When
            ChatRoom chatRoom = ChatRoom.createDirect(
                name,
                user1,
                user1,
                user2
            );

            // Then
            assertThat(chatRoom).isNotNull();
            assertThat(chatRoom.getName()).isEqualTo(name);
            assertThat(chatRoom.getType()).isEqualTo(ChatRoomType.DIRECT);
            assertThat(chatRoom.getParticipantCount()).isEqualTo(2);
            assertThat(chatRoom.isParticipant(user1)).isTrue();
            assertThat(chatRoom.isParticipant(user2)).isTrue();
        }

        @Test
        @DisplayName("같은 사용자끼리는 1:1 채팅방을 만들 수 없다")
        void cannotCreateDirectChatWithSameUser() {
            // Given
            ChatRoomName name = new ChatRoomName("Direct Chat");
            UserId user = UserId.of(1L);

            // When & Then
            assertThatThrownBy(() ->
                ChatRoom.createDirect(name, user, user, user)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("서로 다른 사용자");
        }
    }

    @Nested
    @DisplayName("그룹 채팅방 생성")
    class CreateGroupChatTest {

        @Test
        @DisplayName("여러 사용자로 그룹 채팅방을 생성할 수 있다")
        void createGroupChat() {
            // Given
            ChatRoomName name = new ChatRoomName("Study Group");
            UserId creator = UserId.of(1L);
            List<UserId> participants = Arrays.asList(
                UserId.of(1L),
                UserId.of(2L),
                UserId.of(3L)
            );

            // When
            ChatRoom chatRoom = ChatRoom.createGroup(
                name,
                creator,
                participants
            );

            // Then
            assertThat(chatRoom).isNotNull();
            assertThat(chatRoom.getName()).isEqualTo(name);
            assertThat(chatRoom.getType()).isEqualTo(ChatRoomType.GROUP);
            assertThat(chatRoom.getParticipantCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("참여자가 2명 미만이면 그룹 채팅방을 만들 수 없다")
        void cannotCreateGroupChatWithLessThanTwoParticipants() {
            // Given
            ChatRoomName name = new ChatRoomName("Invalid Group");
            UserId creator = UserId.of(1L);
            List<UserId> participants = Arrays.asList(UserId.of(1L));

            // When & Then
            assertThatThrownBy(() ->
                ChatRoom.createGroup(name, creator, participants)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 2명");
        }
    }

    @Nested
    @DisplayName("메시지 전송")
    class SendMessageTest {

        @Test
        @DisplayName("참여자는 텍스트 메시지를 전송할 수 있다")
        void sendTextMessage() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            MessageContent content = new MessageContent("Hello!");

            // When
            Message message = chatRoom.sendMessage(sender, content);

            // Then
            assertThat(message).isNotNull();
            assertThat(message.getSenderId()).isEqualTo(sender);
            assertThat(message.getContent()).isEqualTo(content);
            assertThat(message.getType()).isEqualTo(MessageType.TEXT);
            assertThat(chatRoom.getMessages()).hasSize(1);
        }

        @Test
        @DisplayName("참여자는 이미지 메시지를 전송할 수 있다")
        void sendImageMessage() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            MessageContent content = new MessageContent("Check this out");
            String imageUrl = "https://example.com/image.jpg";

            // When
            Message message = chatRoom.sendImageMessage(
                sender,
                content,
                imageUrl
            );

            // Then
            assertThat(message).isNotNull();
            assertThat(message.getType()).isEqualTo(MessageType.IMAGE);
            assertThat(message.getImageUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("참여자가 아니면 메시지를 전송할 수 없다")
        void cannotSendMessageIfNotParticipant() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId nonParticipant = UserId.of(999L);
            MessageContent content = new MessageContent("Hello!");

            // When & Then
            assertThatThrownBy(() ->
                chatRoom.sendMessage(nonParticipant, content)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여자만");
        }
    }

    @Nested
    @DisplayName("참여자 관리")
    class ParticipantManagementTest {

        @Test
        @DisplayName("그룹 채팅방에 참여자를 추가할 수 있다")
        void addParticipantToGroupChat() {
            // Given
            ChatRoom chatRoom = createSampleGroupChat();
            UserId inviter = UserId.of(1L);
            UserId newUser = UserId.of(4L);

            int originalCount = chatRoom.getParticipantCount();

            // When
            chatRoom.addParticipant(newUser, inviter);

            // Then
            assertThat(chatRoom.getParticipantCount()).isEqualTo(
                originalCount + 1
            );
            assertThat(chatRoom.isParticipant(newUser)).isTrue();
        }

        @Test
        @DisplayName("1:1 채팅방에는 참여자를 추가할 수 없다")
        void cannotAddParticipantToDirectChat() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId inviter = UserId.of(1L);
            UserId newUser = UserId.of(3L);

            // When & Then
            assertThatThrownBy(() -> chatRoom.addParticipant(newUser, inviter))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1:1 채팅방");
        }

        @Test
        @DisplayName("참여자가 아닌 사용자는 다른 사용자를 초대할 수 없다")
        void cannotInviteIfNotParticipant() {
            // Given
            ChatRoom chatRoom = createSampleGroupChat();
            UserId nonParticipant = UserId.of(999L);
            UserId newUser = UserId.of(4L);

            // When & Then
            assertThatThrownBy(() ->
                chatRoom.addParticipant(newUser, nonParticipant)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여자만");
        }

        @Test
        @DisplayName("이미 참여 중인 사용자는 다시 추가할 수 없다")
        void cannotAddDuplicateParticipant() {
            // Given
            ChatRoom chatRoom = createSampleGroupChat();
            UserId inviter = UserId.of(1L);
            UserId existingUser = UserId.of(2L);

            // When & Then
            assertThatThrownBy(() ->
                chatRoom.addParticipant(existingUser, inviter)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 채팅방에 참여 중인 사용자입니다");
        }

        @Test
        @DisplayName("그룹 채팅방에서 참여자가 나갈 수 있다")
        void removeParticipantFromGroupChat() {
            // Given
            ChatRoom chatRoom = createSampleGroupChat();
            UserId userToRemove = UserId.of(2L);
            int originalCount = chatRoom.getParticipantCount();

            // When
            chatRoom.removeParticipant(userToRemove);

            // Then
            assertThat(chatRoom.getParticipantCount()).isEqualTo(
                originalCount - 1
            );
            assertThat(chatRoom.isParticipant(userToRemove)).isFalse();
        }

        @Test
        @DisplayName("1:1 채팅방에서는 나갈 수 없다")
        void cannotLeaveDirectChat() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId user = UserId.of(1L);

            // When & Then
            assertThatThrownBy(() -> chatRoom.removeParticipant(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1:1 채팅방");
        }
    }

    @Nested
    @DisplayName("메시지 읽음 처리")
    class MessageReadTest {

        @Test
        @DisplayName("참여자는 메시지를 읽음 처리할 수 있다")
        void markMessageAsRead() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            UserId receiver = UserId.of(2L);
            MessageContent content = new MessageContent("Hello!");
            Message message = chatRoom.sendMessage(sender, content);

            // When
            chatRoom.markMessageAsRead(message.getId(), receiver);

            // Then
            assertThat(message.isReadBy(receiver)).isTrue();
        }

        @Test
        @DisplayName("참여자가 아니면 메시지를 읽음 처리할 수 없다")
        void cannotMarkAsReadIfNotParticipant() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            MessageContent content = new MessageContent("Hello!");
            Message message = chatRoom.sendMessage(sender, content);
            UserId nonParticipant = UserId.of(999L);

            // When & Then
            assertThatThrownBy(() ->
                chatRoom.markMessageAsRead(message.getId(), nonParticipant)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여자만");
        }
    }

    @Nested
    @DisplayName("안 읽은 메시지 개수")
    class UnreadCountTest {

        @Test
        @DisplayName("안 읽은 메시지 개수를 조회할 수 있다")
        void getUnreadCount() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            UserId receiver = UserId.of(2L);

            chatRoom.sendMessage(sender, new MessageContent("Message 1"));
            chatRoom.sendMessage(sender, new MessageContent("Message 2"));
            chatRoom.sendMessage(sender, new MessageContent("Message 3"));

            // When
            int unreadCount = chatRoom.getUnreadCount(receiver);

            // Then
            assertThat(unreadCount).isEqualTo(3);
        }

        @Test
        @DisplayName("자신이 보낸 메시지는 안 읽은 메시지에 포함되지 않는다")
        void ownMessagesNotCountedAsUnread() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId user = UserId.of(1L);

            chatRoom.sendMessage(user, new MessageContent("My message"));

            // When
            int unreadCount = chatRoom.getUnreadCount(user);

            // Then
            assertThat(unreadCount).isEqualTo(0);
        }

        @Test
        @DisplayName("읽은 메시지는 안 읽은 메시지에서 제외된다")
        void readMessagesNotCountedAsUnread() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);
            UserId receiver = UserId.of(2L);

            Message message1 = chatRoom.sendMessage(
                sender,
                new MessageContent("Message 1")
            );
            chatRoom.sendMessage(sender, new MessageContent("Message 2"));

            // When
            chatRoom.markMessageAsRead(message1.getId(), receiver);
            int unreadCount = chatRoom.getUnreadCount(receiver);

            // Then
            assertThat(unreadCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("최근 메시지 조회")
    class RecentMessagesTest {

        @Test
        @DisplayName("최근 메시지를 제한된 개수만큼 조회할 수 있다")
        void getRecentMessages() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);

            for (int i = 1; i <= 10; i++) {
                chatRoom.sendMessage(
                    sender,
                    new MessageContent("Message " + i)
                );
            }

            // When
            List<Message> recentMessages = chatRoom.getRecentMessages(5);

            // Then
            assertThat(recentMessages).hasSize(5);
        }

        @Test
        @DisplayName("마지막 메시지를 조회할 수 있다")
        void getLastMessage() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();
            UserId sender = UserId.of(1L);

            chatRoom.sendMessage(sender, new MessageContent("First"));
            chatRoom.sendMessage(sender, new MessageContent("Second"));
            Message lastMessage = chatRoom.sendMessage(
                sender,
                new MessageContent("Last")
            );

            // When
            Message retrievedLastMessage = chatRoom.getLastMessage();

            // Then
            assertThat(retrievedLastMessage).isNotNull();
            assertThat(retrievedLastMessage.getId()).isEqualTo(
                lastMessage.getId()
            );
            assertThat(retrievedLastMessage.getContent().getValue()).isEqualTo(
                "Last"
            );
        }

        @Test
        @DisplayName("메시지가 없으면 null을 반환한다")
        void getLastMessageReturnsNullWhenEmpty() {
            // Given
            ChatRoom chatRoom = createSampleDirectChat();

            // When
            Message lastMessage = chatRoom.getLastMessage();

            // Then
            assertThat(lastMessage).isNull();
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private ChatRoom createSampleDirectChat() {
        ChatRoomName name = new ChatRoomName("Direct Chat");
        UserId user1 = UserId.of(1L);
        UserId user2 = UserId.of(2L);
        return ChatRoom.createDirect(name, user1, user1, user2);
    }

    private ChatRoom createSampleGroupChat() {
        ChatRoomName name = new ChatRoomName("Group Chat");
        UserId creator = UserId.of(1L);
        List<UserId> participants = Arrays.asList(
            UserId.of(1L),
            UserId.of(2L),
            UserId.of(3L)
        );
        return ChatRoom.createGroup(name, creator, participants);
    }
}
