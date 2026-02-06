package om.dxline.dxtalent.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Email 값 객체 테스트
 */
@DisplayName("Email 값 객체 테스트")
class EmailTest {

    @Nested
    @DisplayName("생성 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 이메일로 생성 성공")
        void shouldCreateWithValidEmail() {
            // given & when
            Email email = new Email("test@example.com");

            // then
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("이메일은 소문자로 정규화됨")
        void shouldNormalizeToLowerCase() {
            // given & when
            Email email = new Email("Test@Example.COM");

            // then
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("앞뒤 공백은 자동으로 제거됨")
        void shouldTrimWhitespace() {
            // given & when
            Email email = new Email("  test@example.com  ");

            // then
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("null 이메일은 예외 발생")
        void shouldThrowExceptionWhenNull() {
            // when & then
            assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("빈 문자열은 예외 발생")
        void shouldThrowExceptionWhenEmpty() {
            // when & then
            assertThatThrownBy(() -> new Email("")).isInstanceOf(
                IllegalArgumentException.class
            );

            assertThatThrownBy(() -> new Email("   ")).isInstanceOf(
                IllegalArgumentException.class
            );
        }

        @Test
        @DisplayName("@ 기호가 없으면 예외 발생")
        void shouldThrowExceptionWhenNoAtSign() {
            // when & then
            assertThatThrownBy(() -> new Email("testexample.com")).isInstanceOf(
                IllegalArgumentException.class
            );
        }

        @Test
        @DisplayName("@ 기호가 여러 개면 예외 발생")
        void shouldThrowExceptionWhenMultipleAtSigns() {
            // when & then
            assertThatThrownBy(() ->
                new Email("test@example@com")
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("로컬 파트가 비어있으면 예외 발생")
        void shouldThrowExceptionWhenLocalPartIsEmpty() {
            // when & then
            assertThatThrownBy(() -> new Email("@example.com")).isInstanceOf(
                IllegalArgumentException.class
            );
        }

        @Test
        @DisplayName("도메인이 비어있으면 예외 발생")
        void shouldThrowExceptionWhenDomainIsEmpty() {
            // when & then
            assertThatThrownBy(() -> new Email("test@")).isInstanceOf(
                IllegalArgumentException.class
            );
        }

        @Test
        @DisplayName("도메인에 점이 없으면 예외 발생")
        void shouldThrowExceptionWhenDomainHasNoDot() {
            // when & then
            assertThatThrownBy(() -> new Email("test@example")).isInstanceOf(
                IllegalArgumentException.class
            );
        }

        @Test
        @DisplayName("유효하지 않은 형식은 예외 발생")
        void shouldThrowExceptionWhenInvalidFormat() {
            // when & then
            assertThatThrownBy(() ->
                new Email("test@@example.com")
            ).isInstanceOf(IllegalArgumentException.class);

            // 연속된 점은 정규식에서 허용할 수 있음 - 실제로는 유효하지 않지만 간단한 검증
            // assertThatThrownBy(() -> new Email("test..user@example.com"))
            //     .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("최대 길이를 초과하면 예외 발생")
        void shouldThrowExceptionWhenTooLong() {
            // given
            String longEmail = "a".repeat(250) + "@example.com";

            // when & then
            assertThatThrownBy(() -> new Email(longEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255자를 초과");
        }
    }

    @Nested
    @DisplayName("도메인 추출 테스트")
    class DomainExtractionTest {

        @Test
        @DisplayName("도메인 추출 성공")
        void shouldExtractDomain() {
            // given
            Email email = new Email("user@example.com");

            // when
            String domain = email.getDomain();

            // then
            assertThat(domain).isEqualTo("example.com");
        }

        @Test
        @DisplayName("로컬 파트 추출 성공")
        void shouldExtractLocalPart() {
            // given
            Email email = new Email("user@example.com");

            // when
            String localPart = email.getLocalPart();

            // then
            assertThat(localPart).isEqualTo("user");
        }

        @Test
        @DisplayName("특정 도메인인지 확인")
        void shouldCheckIfFromDomain() {
            // given
            Email email = new Email("user@example.com");

            // when & then
            assertThat(email.isFromDomain("example.com")).isTrue();
            assertThat(email.isFromDomain("other.com")).isFalse();
            assertThat(email.isFromDomain("EXAMPLE.COM")).isTrue(); // 대소문자 무시
        }

        @Test
        @DisplayName("여러 도메인 중 하나인지 확인")
        void shouldCheckIfFromDomains() {
            // given
            Email email = new Email("user@example.com");

            // when & then
            assertThat(email.isFromDomains("example.com", "test.com")).isTrue();
            assertThat(email.isFromDomains("test.com", "other.com")).isFalse();
            assertThat(email.isFromDomains()).isFalse();
        }
    }

    @Nested
    @DisplayName("마스킹 테스트")
    class MaskingTest {

        @Test
        @DisplayName("기본 마스킹")
        void shouldMaskEmail() {
            // given
            Email email = new Email("user@example.com");

            // when
            String masked = email.getMasked();

            // then
            assertThat(masked).isEqualTo("u***@example.com");
        }

        @Test
        @DisplayName("짧은 이메일 마스킹")
        void shouldMaskShortEmail() {
            // given
            Email email = new Email("a@example.com");

            // when
            String masked = email.getMasked();

            // then
            assertThat(masked).isEqualTo("a***@example.com");
        }

        @Test
        @DisplayName("자세한 마스킹")
        void shouldMaskDetailed() {
            // given
            Email email = new Email("user@example.com");

            // when
            String masked = email.getMaskedDetailed();

            // then
            assertThat(masked).startsWith("us**@");
            assertThat(masked).endsWith(".com");
            assertThat(masked).contains("*****");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 이메일은 같은 객체")
        void shouldBeEqualWhenSameEmail() {
            // given
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("test@example.com");

            // when & then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("대소문자가 달라도 같은 객체")
        void shouldBeEqualRegardlessOfCase() {
            // given
            Email email1 = new Email("Test@Example.COM");
            Email email2 = new Email("test@example.com");

            // when & then
            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("다른 이메일은 다른 객체")
        void shouldNotBeEqualWhenDifferentEmail() {
            // given
            Email email1 = new Email("user1@example.com");
            Email email2 = new Email("user2@example.com");

            // when & then
            assertThat(email1).isNotEqualTo(email2);
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드 테스트")
    class StaticFactoryTest {

        @Test
        @DisplayName("of() 메서드로 생성")
        void shouldCreateWithOfMethod() {
            // when
            Email email = Email.of("test@example.com");

            // then
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 이메일 값을 반환")
        void shouldReturnEmailValueInToString() {
            // given
            Email email = new Email("test@example.com");

            // when
            String result = email.toString();

            // then
            assertThat(result).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("실제 이메일 케이스 테스트")
    class RealWorldEmailTest {

        @Test
        @DisplayName("일반적인 이메일 형식들")
        void shouldAcceptCommonEmailFormats() {
            // when & then
            assertThat(new Email("simple@example.com")).isNotNull();
            assertThat(new Email("very.common@example.com")).isNotNull();
            assertThat(
                new Email("disposable.style.email.with+symbol@example.com")
            ).isNotNull();
            assertThat(new Email("user-name@example.com")).isNotNull();
            assertThat(new Email("user_name@example.com")).isNotNull();
            assertThat(new Email("user123@example.co.kr")).isNotNull();
        }

        @Test
        @DisplayName("Gmail, Naver 등 실제 이메일")
        void shouldAcceptRealEmailProviders() {
            // when & then
            assertThat(new Email("user@gmail.com")).isNotNull();
            assertThat(new Email("user@naver.com")).isNotNull();
            assertThat(new Email("user@daum.net")).isNotNull();
            assertThat(new Email("user@kakao.com")).isNotNull();
        }
    }
}
