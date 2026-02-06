package om.dxline.dxtalent.shared.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 값 객체 기본 추상 클래스 (Base Value Object)
 *
 * DDD에서 값 객체는 다음과 같은 특징을 가집니다:
 * 1. 불변성 (Immutability): 한번 생성되면 변경할 수 없음
 * 2. 값 동등성 (Value Equality): 모든 속성 값이 같으면 같은 객체
 * 3. 자가 검증 (Self-Validation): 생성 시점에 유효성 검증
 * 4. 부수 효과 없음 (Side-Effect Free): 메서드 호출이 상태를 변경하지 않음
 *
 * 값 객체 vs 엔티티:
 * - 엔티티: 식별자로 구분 (같은 속성이어도 다른 객체일 수 있음)
 * - 값 객체: 값으로 구분 (모든 속성이 같으면 같은 객체)
 *
 * 사용 예시:
 * <pre>
 * public class Email extends BaseValueObject {
 *     private final String value;
 *
 *     public Email(String value) {
 *         validate(value);
 *         this.value = value.toLowerCase().trim();
 *     }
 *
 *     private void validate(String value) {
 *         if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
 *             throw new IllegalArgumentException("Invalid email format");
 *         }
 *     }
 *
 *     public String getValue() {
 *         return value;
 *     }
 *
 *     {@literal @}Override
 *     protected Object[] getEqualityComponents() {
 *         return new Object[]{value};
 *     }
 * }
 * </pre>
 *
 * 값 객체를 사용하는 이유:
 * 1. 원시 타입 집착(Primitive Obsession) 방지
 * 2. 유효성 검증 로직 중앙화
 * 3. 도메인 개념을 명확하게 표현
 * 4. 타입 안정성 향상
 *
 * 예시 - 원시 타입 집착 문제:
 * <pre>
 * // ❌ 나쁜 예 - 검증 로직 중복
 * public void sendEmail(String email) {
 *     if (!email.contains("@")) throw new IllegalArgumentException();
 *     // ...
 * }
 *
 * public void saveUser(String email) {
 *     if (!email.contains("@")) throw new IllegalArgumentException();
 *     // ...
 * }
 *
 * // ✅ 좋은 예 - 값 객체 사용
 * public void sendEmail(Email email) {
 *     // Email 생성 시 이미 검증됨!
 * }
 *
 * public void saveUser(Email email) {
 *     // 검증 로직 중복 없음!
 * }
 * </pre>
 */
public abstract class BaseValueObject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 값 객체의 동등성을 비교할 때 사용할 컴포넌트들을 반환합니다.
     * 서브클래스에서 반드시 구현해야 합니다.
     *
     * 예시:
     * <pre>
     * // Money 값 객체
     * {@literal @}Override
     * protected Object[] getEqualityComponents() {
     *     return new Object[]{amount, currency};
     * }
     *
     * // Email 값 객체
     * {@literal @}Override
     * protected Object[] getEqualityComponents() {
     *     return new Object[]{value};
     * }
     * </pre>
     *
     * @return 동등성 비교에 사용할 속성 배열
     */
    protected abstract Object[] getEqualityComponents();

    /**
     * 값 객체 동등성 비교
     * 모든 속성이 같으면 같은 객체로 취급합니다.
     *
     * @param o 비교할 객체
     * @return 모든 속성이 같으면 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseValueObject that = (BaseValueObject) o;

        Object[] components = getEqualityComponents();
        Object[] thatComponents = that.getEqualityComponents();

        if (components == null || thatComponents == null) {
            return components == thatComponents;
        }

        if (components.length != thatComponents.length) {
            return false;
        }

        for (int i = 0; i < components.length; i++) {
            if (!Objects.equals(components[i], thatComponents[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 해시코드 생성
     * 모든 속성을 기반으로 해시코드를 생성합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        Object[] components = getEqualityComponents();
        if (components == null) {
            return 0;
        }
        return Objects.hash(components);
    }

    /**
     * 문자열 표현
     * 디버깅과 로깅에 유용합니다.
     *
     * @return 값 객체의 문자열 표현
     */
    @Override
    public String toString() {
        Object[] components = getEqualityComponents();
        if (components == null || components.length == 0) {
            return getClass().getSimpleName() + "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");

        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(components[i]);
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 유효성 검증을 위한 헬퍼 메서드
     * 값 객체 생성자에서 사용할 수 있습니다.
     *
     * @param condition 검증 조건
     * @param message 실패 시 예외 메시지
     * @throws IllegalArgumentException 조건이 거짓이면 예외 발생
     */
    protected void validate(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * null 체크를 위한 헬퍼 메서드
     *
     * @param value 체크할 값
     * @param fieldName 필드 이름
     * @param <T> 값의 타입
     * @return 값 (null이 아닌 경우)
     * @throws IllegalArgumentException 값이 null이면 예외 발생
     */
    protected <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    /**
     * 문자열이 비어있지 않은지 체크하는 헬퍼 메서드
     *
     * @param value 체크할 문자열
     * @param fieldName 필드 이름
     * @return 값 (비어있지 않은 경우)
     * @throws IllegalArgumentException 값이 null이거나 비어있으면 예외 발생
     */
    protected String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        return value;
    }

    /**
     * 숫자가 양수인지 체크하는 헬퍼 메서드
     *
     * @param value 체크할 숫자
     * @param fieldName 필드 이름
     * @return 값 (양수인 경우)
     * @throws IllegalArgumentException 값이 0 이하면 예외 발생
     */
    protected long requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    /**
     * 숫자가 음수가 아닌지 체크하는 헬퍼 메서드
     *
     * @param value 체크할 숫자
     * @param fieldName 필드 이름
     * @return 값 (0 이상인 경우)
     * @throws IllegalArgumentException 값이 음수면 예외 발생
     */
    protected long requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    /**
     * 값이 특정 범위 내에 있는지 체크하는 헬퍼 메서드
     *
     * @param value 체크할 값
     * @param min 최소값 (포함)
     * @param max 최대값 (포함)
     * @param fieldName 필드 이름
     * @return 값 (범위 내인 경우)
     * @throws IllegalArgumentException 값이 범위를 벗어나면 예외 발생
     */
    protected long requireInRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("%s must be between %d and %d", fieldName, min, max)
            );
        }
        return value;
    }
}
