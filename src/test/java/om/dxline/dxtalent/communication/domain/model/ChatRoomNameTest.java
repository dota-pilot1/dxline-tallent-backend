package om.dxline.dxtalent.communication.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ChatRoomName 값 객체 테스트
 */
class ChatRoomNameTest {

    @Nested
    @DisplayName("생성 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 이름으로 생성 성공")
        void createWithValidName() {
            // Given
            String validName = "프로젝트 팀 채팅방";

            // When
            ChatRoomName chatRoomName = new ChatRoomName(validName);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).isEqualTo(validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패")
        void createWithNullName() {
            // When & Then
            assertThatThrownBy(() -> new ChatRoomName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("채팅방 이름");
        }

        @Test
        @DisplayName("빈 문자열로 생성 실패")
        void createWithEmptyName() {
            // When & Then
            assertThatThrownBy(() -> new ChatRoomName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("채팅방 이름");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 실패")
        void createWithBlankName() {
            // When & Then
            assertThatThrownBy(() -> new ChatRoomName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("채팅방 이름");
        }

        @Test
        @DisplayName("너무 긴 이름으로 생성 실패")
        void createWithTooLongName() {
            // Given
            String tooLongName = "a".repeat(101);

            // When & Then
            assertThatThrownBy(() -> new ChatRoomName(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100자");
        }

        @Test
        @DisplayName("최대 길이 이름으로 생성 성공")
        void createWithMaxLengthName() {
            // Given
            String maxLengthName = "a".repeat(100);

            // When
            ChatRoomName chatRoomName = new ChatRoomName(maxLengthName);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).hasSize(100);
        }

        @Test
        @DisplayName("1자 이름으로 생성 성공")
        void createWithSingleCharName() {
            // Given
            String singleChar = "A";

            // When
            ChatRoomName chatRoomName = new ChatRoomName(singleChar);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).isEqualTo(singleChar);
        }

        @Test
        @DisplayName("특수문자 포함 이름으로 생성 성공")
        void createWithSpecialCharacters() {
            // Given
            String nameWithSpecialChars = "팀 채팅방 #1 (개발팀)";

            // When
            ChatRoomName chatRoomName = new ChatRoomName(nameWithSpecialChars);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).isEqualTo(nameWithSpecialChars);
        }

        @Test
        @DisplayName("이모지 포함 이름으로 생성 성공")
        void createWithEmoji() {
            // Given
            String nameWithEmoji = "팀 채팅방 🚀";

            // When
            ChatRoomName chatRoomName = new ChatRoomName(nameWithEmoji);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).isEqualTo(nameWithEmoji);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 이름은 같은 객체")
        void sameNameShouldBeEqual() {
            // Given
            ChatRoomName name1 = new ChatRoomName("프로젝트 팀");
            ChatRoomName name2 = new ChatRoomName("프로젝트 팀");

            // When & Then
            assertThat(name1).isEqualTo(name2);
            assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
        }

        @Test
        @DisplayName("다른 이름은 다른 객체")
        void differentNameShouldNotBeEqual() {
            // Given
            ChatRoomName name1 = new ChatRoomName("프로젝트 팀");
            ChatRoomName name2 = new ChatRoomName("개발 팀");

            // When & Then
            assertThat(name1).isNotEqualTo(name2);
        }

        @Test
        @DisplayName("대소문자 구분")
        void caseSensitive() {
            // Given
            ChatRoomName name1 = new ChatRoomName("Team Chat");
            ChatRoomName name2 = new ChatRoomName("team chat");

            // When & Then
            assertThat(name1).isNotEqualTo(name2);
        }

        @Test
        @DisplayName("공백 차이 구분")
        void whitespaceSensitive() {
            // Given
            ChatRoomName name1 = new ChatRoomName("팀 채팅방");
            ChatRoomName name2 = new ChatRoomName("팀  채팅방");

            // When & Then
            assertThat(name1).isNotEqualTo(name2);
        }

        @Test
        @DisplayName("null과 비교 시 false")
        void notEqualToNull() {
            // Given
            ChatRoomName name = new ChatRoomName("팀 채팅방");

            // When & Then
            assertThat(name).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교 시 false")
        void notEqualToDifferentType() {
            // Given
            ChatRoomName name = new ChatRoomName("팀 채팅방");

            // When & Then
            assertThat(name).isNotEqualTo("팀 채팅방");
        }

        @Test
        @DisplayName("자기 자신과 비교 시 true")
        void equalToItself() {
            // Given
            ChatRoomName name = new ChatRoomName("팀 채팅방");

            // When & Then
            assertThat(name).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 값을 반환")
        void toStringShouldReturnValue() {
            // Given
            String nameValue = "프로젝트 팀 채팅방";
            ChatRoomName name = new ChatRoomName(nameValue);

            // When
            String result = name.toString();

            // Then
            assertThat(result).contains(nameValue);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("생성 후 값 변경 불가능")
        void valueIsImmutable() {
            // Given
            String originalName = "원본 채팅방";
            ChatRoomName name = new ChatRoomName(originalName);

            // When
            String retrievedValue = name.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(originalName);
            // getValue()로 얻은 값을 변경해도 원본에 영향 없음
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("99자 이름 생성 성공")
        void create99CharName() {
            // Given
            String name99 = "a".repeat(99);

            // When
            ChatRoomName chatRoomName = new ChatRoomName(name99);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).hasSize(99);
        }

        @Test
        @DisplayName("2자 이름 생성 성공")
        void create2CharName() {
            // Given
            String name2 = "AB";

            // When
            ChatRoomName chatRoomName = new ChatRoomName(name2);

            // Then
            assertThat(chatRoomName).isNotNull();
            assertThat(chatRoomName.getValue()).isEqualTo(name2);
        }
    }

    @Nested
    @DisplayName("실제 사용 사례 테스트")
    class RealWorldUseCaseTest {

        @Test
        @DisplayName("영어 채팅방 이름")
        void englishChatRoomName() {
            // Given & When
            ChatRoomName name = new ChatRoomName("Development Team");

            // Then
            assertThat(name.getValue()).isEqualTo("Development Team");
        }

        @Test
        @DisplayName("한글 채팅방 이름")
        void koreanChatRoomName() {
            // Given & When
            ChatRoomName name = new ChatRoomName("개발팀 채팅방");

            // Then
            assertThat(name.getValue()).isEqualTo("개발팀 채팅방");
        }

        @Test
        @DisplayName("혼합 언어 채팅방 이름")
        void mixedLanguageChatRoomName() {
            // Given & When
            ChatRoomName name = new ChatRoomName("DX Talent 프로젝트 팀");

            // Then
            assertThat(name.getValue()).isEqualTo("DX Talent 프로젝트 팀");
        }

        @Test
        @DisplayName("숫자 포함 채팅방 이름")
        void nameWithNumbers() {
            // Given & When
            ChatRoomName name = new ChatRoomName("2024 프로젝트 팀");

            // Then
            assertThat(name.getValue()).isEqualTo("2024 프로젝트 팀");
        }

        @Test
        @DisplayName("Direct Chat 기본 이름")
        void directChatDefaultName() {
            // Given & When
            ChatRoomName name = new ChatRoomName("Direct Chat");

            // Then
            assertThat(name.getValue()).isEqualTo("Direct Chat");
        }
    }
}
