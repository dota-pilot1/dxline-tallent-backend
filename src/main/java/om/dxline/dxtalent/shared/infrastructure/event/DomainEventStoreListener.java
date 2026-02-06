package om.dxline.dxtalent.shared.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트 저장소 리스너
 *
 * 발생하는 모든 도메인 이벤트를 자동으로 데이터베이스에 영속화합니다.
 *
 * 특징:
 * - 모든 DomainEvent 타입을 자동으로 캐치
 * - 트랜잭션 커밋 전에 저장 (BEFORE_COMMIT)
 * - @Order(0)로 다른 이벤트 리스너보다 먼저 실행
 * - JSON 직렬화를 통한 이벤트 페이로드 저장
 *
 * 사용 목적:
 * - 이벤트 소싱 (Event Sourcing)
 * - 감사 로그 (Audit Trail)
 * - 이벤트 재처리 (Event Replay)
 * - 디버깅 및 모니터링
 *
 * 실행 순서:
 * 1. 도메인 이벤트 발행 (DomainEventPublisher.publish())
 * 2. DomainEventStoreListener 실행 (저장)
 * 3. 트랜잭션 커밋
 * 4. 다른 이벤트 핸들러 실행 (비동기)
 */
@Slf4j
@Component
@Order(0) // 가장 먼저 실행
public class DomainEventStoreListener {

    private final DomainEventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    /**
     * 생성자
     */
    public DomainEventStoreListener(
        DomainEventStoreRepository eventStoreRepository
    ) {
        this.eventStoreRepository = eventStoreRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 모든 도메인 이벤트를 캐치하여 저장
     *
     * @param event 도메인 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDomainEvent(DomainEvent event) {
        log.debug(
            "도메인 이벤트 저장 시작: eventType={}, eventId={}",
            event.eventType(),
            event.eventId()
        );

        try {
            // 이벤트를 JSON으로 직렬화
            String payload = serializeEvent(event);

            // 애그리게이트 정보 추출
            EventMetadata metadata = extractMetadata(event);

            // EventStore 엔티티 생성
            DomainEventStore eventStore = DomainEventStore.create(
                event.eventId(),
                event.eventType(),
                metadata.aggregateType,
                metadata.aggregateId,
                payload,
                event.occurredOn()
            );

            // DB에 저장
            eventStoreRepository.save(eventStore);

            log.debug(
                "도메인 이벤트 저장 완료: eventType={}, eventId={}",
                event.eventType(),
                event.eventId()
            );
        } catch (Exception e) {
            log.error(
                "도메인 이벤트 저장 실패: eventType={}, eventId={}",
                event.eventType(),
                event.eventId(),
                e
            );

            // 이벤트 저장 실패가 비즈니스 로직에 영향을 주지 않도록
            // 예외를 다시 던지지 않음 (선택적)
            // 프로젝트 정책에 따라 예외를 던져서 트랜잭션 롤백을 유발할 수도 있음
        }
    }

    /**
     * 이벤트를 JSON으로 직렬화
     *
     * @param event 도메인 이벤트
     * @return JSON 문자열
     * @throws JsonProcessingException 직렬화 실패
     */
    private String serializeEvent(DomainEvent event)
        throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: eventType={}", event.eventType(), e);
            throw e;
        }
    }

    /**
     * 이벤트에서 메타데이터 추출
     *
     * @param event 도메인 이벤트
     * @return 이벤트 메타데이터
     */
    private EventMetadata extractMetadata(DomainEvent event) {
        String eventTypeName = event.getClass().getSimpleName();

        // 이벤트 타입명에서 애그리게이트 타입 추출
        // 예: UserRegisteredEvent -> User
        //     ResumeUploadedEvent -> Resume
        //     MessageSentEvent -> Message (또는 ChatRoom)
        String aggregateType = extractAggregateType(eventTypeName);

        // 애그리게이트 ID 추출 (리플렉션 사용)
        String aggregateId = extractAggregateId(event);

        return new EventMetadata(aggregateType, aggregateId);
    }

    /**
     * 이벤트 타입명에서 애그리게이트 타입 추출
     *
     * @param eventTypeName 이벤트 타입명
     * @return 애그리게이트 타입
     */
    private String extractAggregateType(String eventTypeName) {
        // "Event" 제거
        String withoutEvent = eventTypeName.replace("Event", "");

        // CamelCase에서 첫 번째 단어 추출
        // 예: UserRegistered -> User
        //     ResumeUploaded -> Resume
        //     MessageSent -> Message
        for (int i = 1; i < withoutEvent.length(); i++) {
            if (Character.isUpperCase(withoutEvent.charAt(i))) {
                return withoutEvent.substring(0, i);
            }
        }

        return withoutEvent;
    }

    /**
     * 이벤트에서 애그리게이트 ID 추출
     *
     * 이벤트 클래스에서 getId(), getUserId(), getResumeId() 등의
     * 메서드를 찾아서 호출
     *
     * @param event 도메인 이벤트
     * @return 애그리게이트 ID
     */
    private String extractAggregateId(DomainEvent event) {
        try {
            // 일반적인 ID 메서드 이름들
            String[] idMethodNames = {
                "getId",
                "getUserId",
                "getResumeId",
                "getChatRoomId",
                "getMessageId",
            };

            for (String methodName : idMethodNames) {
                try {
                    var method = event.getClass().getMethod(methodName);
                    Object id = method.invoke(event);
                    if (id != null) {
                        // ID 객체의 getValue() 메서드 호출
                        try {
                            var getValueMethod = id
                                .getClass()
                                .getMethod("getValue");
                            Object value = getValueMethod.invoke(id);
                            return value != null ? value.toString() : null;
                        } catch (NoSuchMethodException e) {
                            // getValue() 메서드가 없으면 toString() 사용
                            return id.toString();
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // 해당 메서드가 없으면 다음 시도
                    continue;
                }
            }

            // ID를 찾지 못한 경우
            log.warn(
                "애그리게이트 ID를 추출할 수 없습니다: eventType={}",
                event.eventType()
            );
            return null;
        } catch (Exception e) {
            log.error(
                "애그리게이트 ID 추출 중 오류 발생: eventType={}",
                event.eventType(),
                e
            );
            return null;
        }
    }

    /**
     * 이벤트 메타데이터 내부 클래스
     */
    private static class EventMetadata {

        final String aggregateType;
        final String aggregateId;

        EventMetadata(String aggregateType, String aggregateId) {
            this.aggregateType = aggregateType;
            this.aggregateId = aggregateId;
        }
    }
}
