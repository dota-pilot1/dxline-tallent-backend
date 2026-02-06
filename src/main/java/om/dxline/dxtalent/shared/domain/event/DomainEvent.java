package om.dxline.dxtalent.shared.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트 기본 인터페이스
 *
 * 도메인에서 발생한 중요한 사건을 표현합니다.
 * 모든 도메인 이벤트는 이 인터페이스를 구현해야 합니다.
 *
 * 특징:
 * - 불변 (Immutable): 한번 발생한 이벤트는 변경할 수 없음
 * - 과거형 명명: UserRegisteredEvent, OrderPlacedEvent 등
 * - 충분한 정보 포함: 이벤트 처리에 필요한 모든 정보
 *
 * 사용 예시:
 * <pre>
 * public class UserRegisteredEvent implements DomainEvent {
 *     private final UUID eventId;
 *     private final LocalDateTime occurredOn;
 *     private final UserId userId;
 *     private final Email email;
 *
 *     public UserRegisteredEvent(UserId userId, Email email) {
 *         this.eventId = UUID.randomUUID();
 *         this.occurredOn = LocalDateTime.now();
 *         this.userId = userId;
 *         this.email = email;
 *     }
 *
 *     // getters...
 * }
 * </pre>
 */
public interface DomainEvent {

    /**
     * 이벤트 고유 식별자
     *
     * @return 이벤트 ID (UUID)
     */
    UUID eventId();

    /**
     * 이벤트 발생 시각
     *
     * @return 이벤트가 발생한 시각
     */
    LocalDateTime occurredOn();

    /**
     * 이벤트 타입 (이벤트 클래스명)
     *
     * @return 이벤트 타입 문자열
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
