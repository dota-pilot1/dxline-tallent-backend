package om.dxline.dxtalent.shared.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BaseValueObject 테스트
 *
 * 값 객체의 핵심 특징을 검증합니다:
 * 1. 불변성 (Immutability)
 * 2. 값 동등성 (Value Equality)
 * 3. 자가 검증 (Self-Validation)
 */
@DisplayName("BaseValueObject 테스트")
class BaseValueObjectTest {

    @Nested
    @DisplayName("값 동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("모든 속성이 같으면 같은 객체로 취급")
        void shouldBeEqualWhenAllComponentsAreEqual() {
            // given
            TestValueObject vo1 = new TestValueObject("test", 123);
            TestValueObject vo2 = new TestValueObject("test", 123);

            // when & then
            assertThat(vo1).isEqualTo(vo2);
            assertThat(vo1.hashCode()).isEqualTo(vo2.hashCode());
        }

        @Test
        @DisplayName("속성이 다르면 다른 객체로 취급")
        void shouldNotBeEqualWhenComponentsAreDifferent() {
            // given
            TestValueObject vo1 = new TestValueObject("test", 123);
            TestValueObject vo2 = new TestValueObject("test", 456);
            TestValueObject vo3 = new TestValueObject("other", 123);

            // when & then
            assertThat(vo1).isNotEqualTo(vo2);
            assertThat(vo1).isNotEqualTo(vo3);
        }

        @Test
        @DisplayName("같은 인스턴스는 항상 같음")
        void shouldBeEqualToItself() {
            // given
            TestValueObject vo = new TestValueObject("test", 123);

            // when & then
            assertThat(vo).isEqualTo(vo);
        }

        @Test
        @DisplayName("null과는 같지 않음")
        void shouldNotBeEqualToNull() {
            // given
            TestValueObject vo = new TestValueObject("test", 123);

            // when & then
            assertThat(vo).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과는 같지 않음")
        void shouldNotBeEqualToDifferentType() {
            // given
            TestValueObject vo = new TestValueObject("test", 123);
            String string = "test";

            // when & then
            assertThat(vo).isNotEqualTo(string);
        }
    }

    @Nested
    @DisplayName("유효성 검증 헬퍼 메서드 테스트")
    class ValidationHelperTest {

        @Test
        @DisplayName("validate - 조건이 true면 통과")
        void validateShouldPassWhenConditionIsTrue() {
            // given & when & then
            new TestValueObject("valid", 1); // 예외 없이 통과
        }

        @Test
        @DisplayName("validate - 조건이 false면 예외 발생")
        void validateShouldThrowWhenConditionIsFalse() {
            // when & then
            assertThatThrownBy(() -> new TestValueObject("", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
        }

        @Test
        @DisplayName("requireNonNull - null이 아니면 통과")
        void requireNonNullShouldPassWhenNotNull() {
            // given & when & then
            new TestValueObject("valid", 1);
        }

        @Test
        @DisplayName("requireNonNull - null이면 예외 발생")
        void requireNonNullShouldThrowWhenNull() {
            // when & then
            assertThatThrownBy(() -> new TestValueObject(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("requireNonEmpty - 비어있지 않으면 통과")
        void requireNonEmptyShouldPassWhenNotEmpty() {
            // given & when & then
            new TestValueObjectWithEmpty("valid");
        }

        @Test
        @DisplayName("requireNonEmpty - 비어있으면 예외 발생")
        void requireNonEmptyShouldThrowWhenEmpty() {
            // when & then
            assertThatThrownBy(() -> new TestValueObjectWithEmpty(""))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new TestValueObjectWithEmpty("   "))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("requirePositive - 양수면 통과")
        void requirePositiveShouldPassWhenPositive() {
            // given & when & then
            new TestValueObjectWithPositive(1L);
            new TestValueObjectWithPositive(100L);
        }

        @Test
        @DisplayName("requirePositive - 0 이하면 예외 발생")
        void requirePositiveShouldThrowWhenNotPositive() {
            // when & then
            assertThatThrownBy(() -> new TestValueObjectWithPositive(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");

            assertThatThrownBy(() -> new TestValueObjectWithPositive(-1L))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("requireNonNegative - 0 이상이면 통과")
        void requireNonNegativeShouldPassWhenNonNegative() {
            // given & when & then
            new TestValueObjectWithNonNegative(0L);
            new TestValueObjectWithNonNegative(1L);
        }

        @Test
        @DisplayName("requireNonNegative - 음수면 예외 발생")
        void requireNonNegativeShouldThrowWhenNegative() {
            // when & then
            assertThatThrownBy(() -> new TestValueObjectWithNonNegative(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
        }

        @Test
        @DisplayName("requireInRange - 범위 내면 통과")
        void requireInRangeShouldPassWhenInRange() {
            // given & when & then
            new TestValueObjectWithRange(0L);
            new TestValueObjectWithRange(50L);
            new TestValueObjectWithRange(100L);
        }

        @Test
        @DisplayName("requireInRange - 범위 밖이면 예외 발생")
        void requireInRangeShouldThrowWhenOutOfRange() {
            // when & then
            assertThatThrownBy(() -> new TestValueObjectWithRange(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be between");

            assertThatThrownBy(() -> new TestValueObjectWithRange(101L))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 클래스명과 속성값을 포함")
        void toStringShouldIncludeClassNameAndValues() {
            // given
            TestValueObject vo = new TestValueObject("test", 123);

            // when
            String result = vo.toString();

            // then
            assertThat(result)
                .contains("TestValueObject")
                .contains("test")
                .contains("123");
        }
    }

    // ============================================================
    // 테스트용 값 객체들
    // ============================================================

    /**
     * 기본 테스트용 값 객체
     */
    static class TestValueObject extends BaseValueObject {
        private final String name;
        private final Integer number;

        public TestValueObject(String name, Integer number) {
            this.name = requireNonNull(name, "name");
            validate(!name.isEmpty(), "Name cannot be empty");
            this.number = number;
        }

        @Override
        protected Object[] getEqualityComponents() {
            return new Object[]{name, number};
        }

        public String getName() {
            return name;
        }

        public Integer getNumber() {
            return number;
        }
    }

    /**
     * requireNonEmpty 테스트용 값 객체
     */
    static class TestValueObjectWithEmpty extends BaseValueObject {
        private final String value;

        public TestValueObjectWithEmpty(String value) {
            this.value = requireNonEmpty(value, "value");
        }

        @Override
        protected Object[] getEqualityComponents() {
            return new Object[]{value};
        }
    }

    /**
     * requirePositive 테스트용 값 객체
     */
    static class TestValueObjectWithPositive extends BaseValueObject {
        private final Long value;

        public TestValueObjectWithPositive(Long value) {
            this.value = requirePositive(value, "value");
        }

        @Override
        protected Object[] getEqualityComponents() {
            return new Object[]{value};
        }
    }

    /**
     * requireNonNegative 테스트용 값 객체
     */
    static class TestValueObjectWithNonNegative extends BaseValueObject {
        private final Long value;

        public TestValueObjectWithNonNegative(Long value) {
            this.value = requireNonNegative(value, "value");
        }

        @Override
        protected Object[] getEqualityComponents() {
            return new Object[]{value};
        }
    }

    /**
     * requireInRange 테스트용 값 객체
     */
    static class TestValueObjectWithRange extends BaseValueObject {
        private final Long value;

        public TestValueObjectWithRange(Long value) {
            this.value = requireInRange(value, 0, 100, "value");
        }

        @Override
        protected Object[] getEqualityComponents() {
            return new Object[]{value};
        }
    }
}
