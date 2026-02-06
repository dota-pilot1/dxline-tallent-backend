package om.dxline.dxtalent.shared.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 도메인 이벤트 발행자 (Domain Event Publisher)
 *
 * 도메인 계층에서 발생한 이벤트를 인프라 계층(Spring)의 이벤트 버스로 전달합니다.
 * Spring의 ApplicationEventPublisher를 어댑터로 사용하여 도메인이 인프라에 의존하지 않도록 합니다.
 *
 * 사용 방법:
 * <pre>
 * // Application Service에서
 * public class AuthService {
 *     private final DomainEventPublisher eventPublisher;
 *
 *     public void signup(SignupCommand command) {
 *         User user = User.register(...);
 *         userRepository.save(user);
 *
 *         // 도메인 이벤트 발행
 *         eventPublisher.publishAll(user.getDomainEvents());
 *         user.clearDomainEvents();
 *     }
 * }
 * </pre>
 *
 * 이벤트 핸들러 작성:
 * <pre>
 * {@literal @}Component
 * public class UserEventHandler {
 *
 *     {@literal @}EventListener
 *     {@literal @}Async  // 비동기 처리
 *     public void handleUserRegistered(UserRegisteredEvent event) {
 *         // 환영 이메일 발송
 *         emailService.sendWelcomeEmail(event.getEmail());
 *     }
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 단일 도메인 이벤트 발행
     *
     * @param event 발행할 도메인 이벤트
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }

        log.debug("Publishing domain event: {} (ID: {})",
                 event.eventType(),
                 event.eventId());

        try {
            applicationEventPublisher.publishEvent(event);

            log.debug("Successfully published domain event: {}", event.eventType());
        } catch (Exception e) {
            log.error("Failed to publish domain event: {}", event.eventType(), e);
            // 이벤트 발행 실패를 어떻게 처리할지는 프로젝트 정책에 따라 결정
            // 옵션 1: 예외를 다시 던짐 (트랜잭션 롤백)
            // 옵션 2: 로깅만 하고 계속 진행
            // 옵션 3: Dead Letter Queue에 저장
            throw new DomainEventPublishException("Failed to publish event: " + event.eventType(), e);
        }
    }

    /**
     * 여러 도메인 이벤트를 한 번에 발행
     *
     * @param events 발행할 도메인 이벤트 목록
     */
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("No domain events to publish");
            return;
        }

        log.debug("Publishing {} domain events", events.size());

        for (DomainEvent event : events) {
            publish(event);
        }

        log.debug("Successfully published all {} domain events", events.size());
    }

    /**
     * 도메인 이벤트 발행 실패 예외
     */
    public static class DomainEventPublishException extends RuntimeException {
        public DomainEventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
