package om.dxline.dxtalent.identity.domain.model;

import java.util.Objects;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 사용자 식별자 값 객체 (User Identifier Value Object)
 *
 * 사용자를 고유하게 식별하는 ID를 표현하는 값 객체입니다.
 *
 * 원시 타입(Long) 대신 값 객체를 사용하는 이유:
 * 1. 타입 안정성: UserId와 OrderId를 혼동할 수 없음
 * 2. 유효성 검증: 생성 시점에 검증 가능
 * 3. 도메인 개념 명확화: "Long 타입"이 아닌 "사용자 ID"
 *
 * 사용 예시:
 * <pre>
 * // ❌ 나쁜 예 - 원시 타입 사용
 * public void assignRole(Long userId, Long roleId) {
 *     // userId와 roleId를 혼동할 위험!
 * }
 *
 * // ✅ 좋은 예 - 값 객체 사용
 * public void assignRole(UserId userId, RoleId roleId) {
 *     // 타입이 명확하고 혼동 불가!
 * }
 * </pre>
 */
public class UserId extends BaseValueObject {

    private final Long value;

    /**
     * 기존 ID 값으로 UserId 생성
     *
     * @param value 사용자 ID 값
     * @throws IllegalArgumentException ID가 null이거나 유효하지 않은 경우
     */
    public UserId(Long value) {
        this.value = requireNonNull(value, "User ID");
        // 0은 임시 ID로 허용 (영속화 전 상태)
        validate(value >= 0, "User ID must be non-negative");
    }

    /**
     * 새로운 UserId 생성 (신규 사용자 등록 시)
     * 테스트나 도메인 로직에서는 임시 ID를 생성하고,
     * 실제 ID는 Repository 저장 시 JPA가 할당합니다.
     *
     * @return 새 UserId (임시 ID)
     */
    public static UserId newId() {
        // 도메인 로직 테스트를 위해 임시 양수 ID 반환
        // 실제 영속화 시 JPA가 실제 ID로 대체
        return new UserId((System.currentTimeMillis() % 1000000L) + 1L);
    }

    /**
     * 기존 ID로 UserId 생성 (재구성 시)
     *
     * @param id 사용자 ID
     * @return UserId 인스턴스
     */
    public static UserId of(Long id) {
        return new UserId(id);
    }

    /**
     * ID 값 반환
     *
     * @return Long 타입의 ID 값
     */
    public Long getValue() {
        return value;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value };
    }

    /**
     * 새로운 ID인지 확인 (아직 영속화되지 않음)
     *
     * @return ID가 0이면 true (임시 ID)
     */
    public boolean isNew() {
        return value == null || value == 0;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
