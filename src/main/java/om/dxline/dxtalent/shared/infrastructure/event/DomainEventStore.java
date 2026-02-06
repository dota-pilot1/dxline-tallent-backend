package om.dxline.dxtalent.shared.infrastructure.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 도메인 이벤트 저장소 엔티티
 *
 * 발생한 모든 도메인 이벤트를 데이터베이스에 영속화합니다.
 *
 * 용도:
 * - 이벤트 소싱 (Event Sourcing)
 * - 감사 로그 (Audit Log)
 * - 이벤트 재처리 (Event Replay)
 * - 디버깅 및 모니터링
 * - 이벤트 기반 통계 및 분석
 *
 * 테이블 구조:
 * - event_id: 이벤트 고유 식별자 (UUID)
 * - event_type: 이벤트 타입 (클래스명)
 * - aggregate_type: 애그리게이트 타입
 * - aggregate_id: 애그리게이트 ID
 * - payload: 이벤트 데이터 (JSON)
 * - occurred_on: 이벤트 발생 시각
 * - processed_at: 이벤트 처리 시각
 * - status: 처리 상태
 *
 * 사용 예시:
 * <pre>
 * // 이벤트 저장
 * DomainEventStore eventStore = DomainEventStore.from(userRegisteredEvent);
 * eventStoreRepository.save(eventStore);
 *
 * // 재처리가 필요한 이벤트 조회
 * List<DomainEventStore> failedEvents = eventStoreRepository
 *     .findByStatus(EventStatus.FAILED);
 * </pre>
 */
@Entity
@Table(
    name = "domain_event_store",
    indexes = {
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_aggregate_type", columnList = "aggregate_type"),
        @Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_occurred_on", columnList = "occurred_on"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_event_type_occurred_on", columnList = "event_type, occurred_on")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DomainEventStore {

    /**
     * Primary Key (Auto Increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트 고유 식별자 (UUID)
     * 도메인 이벤트의 eventId()와 매핑
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    /**
     * 이벤트 타입 (클래스명)
     * 예: UserRegisteredEvent, ResumeUploadedEvent
     */
    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    /**
     * 애그리게이트 타입
     * 예: User, Resume, ChatRoom
     */
    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    /**
     * 애그리게이트 ID
     * 어떤 애그리게이트에서 발생한 이벤트인지 추적
     */
    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    /**
     * 이벤트 페이로드 (JSON)
     * 이벤트의 모든 데이터를 JSON으로 직렬화하여 저장
     */
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    /**
     * 이벤트 발생 시각
     */
    @Column(name = "occurred_on", nullable = false)
    private LocalDateTime occurredOn;

    /**
     * 이벤트 처리 시각
     * 이벤트 핸들러가 처리를 완료한 시각
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 처리 상태
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * 에러 메시지
     * 처리 실패 시 에러 정보 저장
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 업데이트 시각
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 생성 시 자동으로 타임스탬프 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.status == null) {
            this.status = EventStatus.PENDING;
        }
    }

    /**
     * 업데이트 시 자동으로 타임스탬프 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================================
    // 정적 팩토리 메서드
    // ============================================================

    /**
     * 도메인 이벤트로부터 EventStore 생성
     *
     * @param eventId 이벤트 ID
     * @param eventType 이벤트 타입
     * @param aggregateType 애그리게이트 타입
     * @param aggregateId 애그리게이트 ID
     * @param payload 이벤트 페이로드 (JSON)
     * @param occurredOn 발생 시각
     * @return DomainEventStore 인스턴스
     */
    public static DomainEventStore create(
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload,
        LocalDateTime occurredOn
    ) {
        DomainEventStore eventStore = new DomainEventStore();
        eventStore.eventId = eventId.toString();
        eventStore.eventType = eventType;
        eventStore.aggregateType = aggregateType;
        eventStore.aggregateId = aggregateId;
        eventStore.payload = payload;
        eventStore.occurredOn = occurredOn;
        eventStore.status = EventStatus.PENDING;
        eventStore.retryCount = 0;
        return eventStore;
    }

    // ============================================================
    // 비즈니스 메서드
    // ============================================================

    /**
     * 이벤트 처리 성공 표시
     */
    public void markAsProcessed() {
        this.status = EventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 이벤트 처리 실패 표시
     *
     * @param errorMessage 에러 메시지
     */
    public void markAsFailed(String errorMessage) {
        this.status = EventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * 재처리 대기 상태로 변경
     */
    public void markAsRetryPending() {
        this.status = EventStatus.RETRY_PENDING;
    }

    /**
     * 재처리 가능 여부 확인
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재처리 가능하면 true
     */
    public boolean canRetry(int maxRetryCount) {
        return this.retryCount < maxRetryCount;
    }

    /**
     * 이벤트 스킵 (더 이상 처리하지 않음)
     *
     * @param reason 스킵 사유
     */
    public void skip(String reason) {
        this.status = EventStatus.SKIPPED;
        this.errorMessage = reason;
    }
}

/**
 * 이벤트 처리 상태
 */
enum EventStatus {
    /**
     * 처리 대기 중
     */
    PENDING,

    /**
     * 처리 완료
     */
    PROCESSED,

    /**
     * 처리 실패
     */
    FAILED,

    /**
     * 재처리 대기 중
     */
    RETRY_PENDING,

    /**
     * 스킵됨 (더 이상 처리하지 않음)
     */
    SKIPPED
}
