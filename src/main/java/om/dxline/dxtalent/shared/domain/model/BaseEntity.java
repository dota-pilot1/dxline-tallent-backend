package om.dxline.dxtalent.shared.domain.model;

import om.dxline.dxtalent.shared.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 엔티티 기본 추상 클래스
 *
 * DDD에서 엔티티는 식별자를 통해 구분되는 객체입니다.
 * 모든 도메인 엔티티는 이 클래스를 상속받아 구현할 수 있습니다.
 *
 * 특징:
 * - 식별자 기반 동등성 (Identity-based Equality)
 * - 도메인 이벤트 관리 기능 제공
 * - 불변성을 위한 보호된 setter
 *
 * @param <ID> 엔티티의 식별자 타입
 *
 * 사용 예시:
 * <pre>
 * public class User extends BaseEntity<UserId> {
 *     private Email email;
 *     private Password password;
 *
 *     public static User register(Email email, Password password) {
 *         User user = new User();
 *         user.setId(UserId.newId());
 *         user.email = email;
 *         user.password = password;
 *
 *         user.addDomainEvent(new UserRegisteredEvent(user.getId(), email));
 *         return user;
 *     }
 * }
 * </pre>
 */
public abstract class BaseEntity<ID> {

    /**
     * 엔티티 식별자
     */
    private ID id;

    /**
     * 도메인 이벤트 목록
     * 애그리게이트 루트에서 발생한 이벤트를 저장합니다.
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 기본 생성자 (JPA를 위해 protected)
     */
    protected BaseEntity() {
    }

    /**
     * 식별자를 받는 생성자
     *
     * @param id 엔티티 식별자
     */
    protected BaseEntity(ID id) {
        this.id = id;
    }

    /**
     * 엔티티 식별자 반환
     *
     * @return 식별자
     */
    public ID getId() {
        return id;
    }

    /**
     * 엔티티 식별자 설정
     * 주의: 이 메서드는 신중하게 사용해야 합니다.
     * 일반적으로 생성 시점에만 호출되어야 합니다.
     *
     * @param id 설정할 식별자
     */
    protected void setId(ID id) {
        if (this.id != null) {
            throw new IllegalStateException("Entity ID cannot be changed once set");
        }
        this.id = id;
    }

    /**
     * 도메인 이벤트 추가
     * 애그리게이트 루트에서 비즈니스 로직 수행 후 이벤트를 추가합니다.
     *
     * @param event 추가할 도메인 이벤트
     */
    protected void addDomainEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }

    /**
     * 여러 도메인 이벤트 추가
     *
     * @param events 추가할 도메인 이벤트 목록
     */
    protected void addDomainEvents(List<DomainEvent> events) {
        if (events != null) {
            this.domainEvents.addAll(events);
        }
    }

    /**
     * 도메인 이벤트 목록 반환 (불변 리스트)
     * Application Service에서 이벤트를 발행한 후 호출합니다.
     *
     * @return 도메인 이벤트 목록 (읽기 전용)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트 초기화
     * 이벤트 발행 후 호출하여 메모리 누수를 방지합니다.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * 도메인 이벤트가 있는지 확인
     *
     * @return 이벤트가 있으면 true
     */
    public boolean hasDomainEvents() {
        return !this.domainEvents.isEmpty();
    }

    /**
     * 엔티티 동등성 비교
     * 엔티티는 식별자(ID)로만 구분됩니다.
     *
     * @param o 비교할 객체
     * @return 같은 엔티티면 true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;

        // ID가 null이면 같은 객체가 아님
        if (id == null || that.id == null) {
            return false;
        }

        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드 생성
     * 엔티티는 식별자 기반으로 해시코드를 생성합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        // ID가 null인 경우를 고려한 안전한 해시코드
        return id != null ? Objects.hash(id) : 0;
    }

    /**
     * 문자열 표현
     *
     * @return 엔티티 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("%s{id=%s}", getClass().getSimpleName(), id);
    }

    /**
     * 엔티티가 새로운 엔티티인지 확인 (아직 영속화되지 않음)
     *
     * @return ID가 없으면 true
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * 엔티티가 영속화되었는지 확인
     *
     * @return ID가 있으면 true
     */
    public boolean isPersisted() {
        return this.id != null;
    }
}
