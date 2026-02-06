package om.dxline.dxtalent.communication.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * MessageContent 값 객체 테스트
 */
class MessageContentTest {

    @Nested
    @DisplayName("생성 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 내용으로 생성 성공")
        void createWithValidContent() {
            // Given
            String validContent = "안녕하세요!";

            // When
            MessageContent messageContent = new MessageContent(validContent);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(validContent);
        }

        @Test
        @DisplayName("null 내용으로 생성 실패")
        void createWithNullContent() {
            // When & Then
            assertThatThrownBy(() -> new MessageContent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지 내용");
        }

        @Test
        @DisplayName("빈 문자열로 생성 실패")
        void createWithEmptyContent() {
            // When & Then
            assertThatThrownBy(() -> new MessageContent(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지 내용");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 실패")
        void createWithBlankContent() {
            // When & Then
            assertThatThrownBy(() -> new MessageContent("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지 내용");
        }

        @Test
        @DisplayName("너무 긴 내용으로 생성 실패 (5000자 초과)")
        void createWithTooLongContent() {
            // Given
            String tooLongContent = "a".repeat(5001);

            // When & Then
            assertThatThrownBy(() -> new MessageContent(tooLongContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5000자");
        }

        @Test
        @DisplayName("최대 길이 내용으로 생성 성공 (5000자)")
        void createWithMaxLengthContent() {
            // Given
            String maxLengthContent = "a".repeat(5000);

            // When
            MessageContent messageContent = new MessageContent(maxLengthContent);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).hasSize(5000);
        }

        @Test
        @DisplayName("1자 내용으로 생성 성공")
        void createWithSingleCharContent() {
            // Given
            String singleChar = "A";

            // When
            MessageContent messageContent = new MessageContent(singleChar);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(singleChar);
        }

        @Test
        @DisplayName("줄바꿈 포함 내용으로 생성 성공")
        void createWithNewlines() {
            // Given
            String contentWithNewlines = "첫 번째 줄\n두 번째 줄\n세 번째 줄";

            // When
            MessageContent messageContent = new MessageContent(contentWithNewlines);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(contentWithNewlines);
        }

        @Test
        @DisplayName("이모지 포함 내용으로 생성 성공")
        void createWithEmoji() {
            // Given
            String contentWithEmoji = "안녕하세요! 😊👍🎉";

            // When
            MessageContent messageContent = new MessageContent(contentWithEmoji);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(contentWithEmoji);
        }

        @Test
        @DisplayName("특수문자 포함 내용으로 생성 성공")
        void createWithSpecialCharacters() {
            // Given
            String contentWithSpecialChars = "Hello! @#$%^&*()_+-=[]{}|;:',.<>?/";

            // When
            MessageContent messageContent = new MessageContent(contentWithSpecialChars);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(contentWithSpecialChars);
        }

        @Test
        @DisplayName("URL 포함 내용으로 생성 성공")
        void createWithUrl() {
            // Given
            String contentWithUrl = "참고 자료: https://example.com/document";

            // When
            MessageContent messageContent = new MessageContent(contentWithUrl);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(contentWithUrl);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 내용은 같은 객체")
        void sameContentShouldBeEqual() {
            // Given
            MessageContent content1 = new MessageContent("안녕하세요");
            MessageContent content2 = new MessageContent("안녕하세요");

            // When & Then
            assertThat(content1).isEqualTo(content2);
            assertThat(content1.hashCode()).isEqualTo(content2.hashCode());
        }

        @Test
        @DisplayName("다른 내용은 다른 객체")
        void differentContentShouldNotBeEqual() {
            // Given
            MessageContent content1 = new MessageContent("안녕하세요");
            MessageContent content2 = new MessageContent("반갑습니다");

            // When & Then
            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        @DisplayName("대소문자 구분")
        void caseSensitive() {
            // Given
            MessageContent content1 = new MessageContent("Hello");
            MessageContent content2 = new MessageContent("hello");

            // When & Then
            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        @DisplayName("공백 차이 구분")
        void whitespaceSensitive() {
            // Given
            MessageContent content1 = new MessageContent("안녕 하세요");
            MessageContent content2 = new MessageContent("안녕하세요");

            // When & Then
            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        @DisplayName("null과 비교 시 false")
        void notEqualToNull() {
            // Given
            MessageContent content = new MessageContent("안녕하세요");

            // When & Then
            assertThat(content).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교 시 false")
        void notEqualToDifferentType() {
            // Given
            MessageContent content = new MessageContent("안녕하세요");

            // When & Then
            assertThat(content).isNotEqualTo("안녕하세요");
        }

        @Test
        @DisplayName("자기 자신과 비교 시 true")
        void equalToItself() {
            // Given
            MessageContent content = new MessageContent("안녕하세요");

            // When & Then
            assertThat(content).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 값을 반환")
        void toStringShouldReturnValue() {
            // Given
            String contentValue = "테스트 메시지";
            MessageContent content = new MessageContent(contentValue);

            // When
            String result = content.toString();

            // Then
            assertThat(result).contains(contentValue);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("생성 후 값 변경 불가능")
        void valueIsImmutable() {
            // Given
            String originalContent = "원본 메시지";
            MessageContent content = new MessageContent(originalContent);

            // When
            String retrievedValue = content.getValue();

            // Then
            assertThat(retrievedValue).isEqualTo(originalContent);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("4999자 내용 생성 성공")
        void create4999CharContent() {
            // Given
            String content4999 = "a".repeat(4999);

            // When
            MessageContent messageContent = new MessageContent(content4999);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).hasSize(4999);
        }

        @Test
        @DisplayName("2자 내용 생성 성공")
        void create2CharContent() {
            // Given
            String content2 = "Hi";

            // When
            MessageContent messageContent = new MessageContent(content2);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).isEqualTo(content2);
        }

        @Test
        @DisplayName("10자 내용 생성 성공")
        void create10CharContent() {
            // Given
            String content10 = "1234567890";

            // When
            MessageContent messageContent = new MessageContent(content10);

            // Then
            assertThat(messageContent).isNotNull();
            assertThat(messageContent.getValue()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("실제 사용 사례 테스트")
    class RealWorldUseCaseTest {

        @Test
        @DisplayName("짧은 인사 메시지")
        void shortGreetingMessage() {
            // Given & When
            MessageContent content = new MessageContent("안녕하세요!");

            // Then
            assertThat(content.getValue()).isEqualTo("안녕하세요!");
        }

        @Test
        @DisplayName("긴 설명 메시지")
        void longExplanationMessage() {
            // Given
            String longMessage = "이 프로젝트는 DDD 아키텍처를 적용하여 " +
                "도메인 중심의 설계를 구현하고 있습니다. " +
                "각 바운디드 컨텍스트는 명확한 책임을 가지며, " +
                "도메인 이벤트를 통해 느슨한 결합을 유지합니다.";

            // When
            MessageContent content = new MessageContent(longMessage);

            // Then
            assertThat(content.getValue()).isEqualTo(longMessage);
        }

        @Test
        @DisplayName("코드 스니펫 메시지")
        void codeSnippetMessage() {
            // Given
            String codeMessage = "다음 코드를 확인해주세요:\n" +
                "```java\n" +
                "public class Example {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello\");\n" +
                "    }\n" +
                "}\n" +
                "```";

            // When
            MessageContent content = new MessageContent(codeMessage);

            // Then
            assertThat(content.getValue()).isEqualTo(codeMessage);
        }

        @Test
        @DisplayName("질문 메시지")
        void questionMessage() {
            // Given & When
            MessageContent content = new MessageContent("이 기능은 어떻게 구현하나요?");

            // Then
            assertThat(content.getValue()).isEqualTo("이 기능은 어떻게 구현하나요?");
        }

        @Test
        @DisplayName("링크 공유 메시지")
        void linkSharingMessage() {
            // Given
            String linkMessage = "참고 문서입니다: https://docs.example.com/guide";

            // When
            MessageContent content = new MessageContent(linkMessage);

            // Then
            assertThat(content.getValue()).isEqualTo(linkMessage);
        }

        @Test
        @DisplayName("멀티라인 메시지")
        void multilineMessage() {
            // Given
            String multilineMessage = "회의 안건:\n" +
                "1. 프로젝트 진행 상황\n" +
                "2. 다음 스프린트 계획\n" +
                "3. 이슈 논의";

            // When
            MessageContent content = new MessageContent(multilineMessage);

            // Then
            assertThat(content.getValue()).isEqualTo(multilineMessage);
            assertThat(content.getValue()).contains("\n");
        }

        @Test
        @DisplayName("이모지만 있는 메시지")
        void emojiOnlyMessage() {
            // Given & When
            MessageContent content = new MessageContent("👍");

            // Then
            assertThat(content.getValue()).isEqualTo("👍");
        }

        @Test
        @DisplayName("혼합 언어 메시지")
        void mixedLanguageMessage() {
            // Given
            String mixedMessage = "Hello, 안녕하세요, こんにちは";

            // When
            MessageContent content = new MessageContent(mixedMessage);

            // Then
            assertThat(content.getValue()).isEqualTo(mixedMessage);
        }
    }

    @Nested
    @DisplayName("메시지 길이 검증 테스트")
    class LengthValidationTest {

        @Test
        @DisplayName("100자 메시지 생성 성공")
        void create100CharMessage() {
            // Given
            String content100 = "a".repeat(100);

            // When
            MessageContent content = new MessageContent(content100);

            // Then
            assertThat(content.getValue()).hasSize(100);
        }

        @Test
        @DisplayName("1000자 메시지 생성 성공")
        void create1000CharMessage() {
            // Given
            String content1000 = "a".repeat(1000);

            // When
            MessageContent content = new MessageContent(content1000);

            // Then
            assertThat(content.getValue()).hasSize(1000);
        }

        @Test
        @DisplayName("일반적인 메시지 길이 (50자) 생성 성공")
        void createTypicalLengthMessage() {
            // Given
            String typicalMessage = "이것은 일반적인 길이의 채팅 메시지입니다. 약 50자 정도 됩니다.";

            // When
            MessageContent content = new MessageContent(typicalMessage);

            // Then
            assertThat(content.getValue()).hasSize(typicalMessage.length());
        }
    }
}
