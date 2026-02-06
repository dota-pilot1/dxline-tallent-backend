package om.dxline.dxtalent.shared.infrastructure.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 도메인 이벤트 저장소 Repository
 *
 * 영속화된 도메인 이벤트를 조회하고 관리합니다.
 *
 * 주요 기능:
 * - 이벤트 타입별 조회
 * - 상태별 조회 (실패, 재시도 대기 등)
 * - 애그리게이트별 이벤트 히스토리 조회
 * - 재처리가 필요한 이벤트 조회
 */
@Repository
public interface DomainEventStoreRepository extends JpaRepository<DomainEventStore, Long> {

    /**
     * 이벤트 ID로 조회
     *
     * @param eventId 이벤트 ID (UUID 문자열)
     * @return DomainEventStore
     */
    Optional<DomainEventStore> findByEventId(String eventId);

    /**
     * 이벤트 타입으로 조회
     *
     * @param eventType 이벤트 타입 (클래스명)
     * @return 해당 타입의 모든 이벤트
     */
    List<DomainEventStore> findByEventType(String eventType);

    /**
     * 상태별 조회
     *
     * @param status 이벤트 상태
     * @return 해당 상태의 모든 이벤트
     */
    List<DomainEventStore> findByStatus(EventStatus status);

    /**
     * 특정 애그리게이트의 이벤트 히스토리 조회
     *
     * @param aggregateType 애그리게이트 타입
     * @param aggregateId 애그리게이트 ID
     * @return 해당 애그리게이트의 모든 이벤트 (시간순 정렬)
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE e.aggregateType = :aggregateType " +
           "AND e.aggregateId = :aggregateId " +
           "ORDER BY e.occurredOn ASC")
    List<DomainEventStore> findAggregateHistory(
        @Param("aggregateType") String aggregateType,
        @Param("aggregateId") String aggregateId
    );

    /**
     * 재처리가 필요한 이벤트 조회
     *
     * FAILED 또는 RETRY_PENDING 상태이면서
     * 최대 재시도 횟수를 초과하지 않은 이벤트
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재처리 대상 이벤트 목록
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE (e.status = 'FAILED' OR e.status = 'RETRY_PENDING') " +
           "AND e.retryCount < :maxRetryCount " +
           "ORDER BY e.occurredOn ASC")
    List<DomainEventStore> findEventsToRetry(@Param("maxRetryCount") int maxRetryCount);

    /**
     * 특정 시간 이전의 처리된 이벤트 조회 (정리용)
     *
     * @param before 기준 시각
     * @return 해당 시각 이전의 처리된 이벤트
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE e.status = 'PROCESSED' " +
           "AND e.processedAt < :before")
    List<DomainEventStore> findProcessedEventsBefore(@Param("before") LocalDateTime before);

    /**
     * 특정 기간의 이벤트 조회
     *
     * @param startDate 시작 시각
     * @param endDate 종료 시각
     * @return 해당 기간의 이벤트
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE e.occurredOn BETWEEN :startDate AND :endDate " +
           "ORDER BY e.occurredOn DESC")
    List<DomainEventStore> findEventsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 이벤트 타입별 통계 조회
     *
     * @param eventType 이벤트 타입
     * @return 해당 타입의 이벤트 개수
     */
    @Query("SELECT COUNT(e) FROM DomainEventStore e WHERE e.eventType = :eventType")
    long countByEventType(@Param("eventType") String eventType);

    /**
     * 상태별 통계 조회
     *
     * @param status 이벤트 상태
     * @return 해당 상태의 이벤트 개수
     */
    long countByStatus(EventStatus status);

    /**
     * 오래된 처리 완료 이벤트 삭제
     *
     * @param before 기준 시각
     * @return 삭제된 레코드 수
     */
    @Query("DELETE FROM DomainEventStore e " +
           "WHERE e.status = 'PROCESSED' " +
           "AND e.processedAt < :before")
    int deleteProcessedEventsBefore(@Param("before") LocalDateTime before);

    /**
     * 최근 이벤트 조회 (페이징)
     *
     * @param limit 조회할 개수
     * @return 최근 이벤트 목록
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "ORDER BY e.occurredOn DESC " +
           "LIMIT :limit")
    List<DomainEventStore> findRecentEvents(@Param("limit") int limit);

    /**
     * 특정 애그리게이트의 최근 이벤트 조회
     *
     * @param aggregateType 애그리게이트 타입
     * @param aggregateId 애그리게이트 ID
     * @param limit 조회할 개수
     * @return 최근 이벤트 목록
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE e.aggregateType = :aggregateType " +
           "AND e.aggregateId = :aggregateId " +
           "ORDER BY e.occurredOn DESC " +
           "LIMIT :limit")
    List<DomainEventStore> findRecentAggregateEvents(
        @Param("aggregateType") String aggregateType,
        @Param("aggregateId") String aggregateId,
        @Param("limit") int limit
    );

    /**
     * 실패한 이벤트 중 재시도가 필요한 이벤트 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회할 개수
     * @return 재시도 대상 이벤트 목록
     */
    @Query("SELECT e FROM DomainEventStore e " +
           "WHERE e.status = 'FAILED' " +
           "AND e.retryCount < :maxRetryCount " +
           "ORDER BY e.occurredOn ASC " +
           "LIMIT :limit")
    List<DomainEventStore> findFailedEventsForRetry(
        @Param("maxRetryCount") int maxRetryCount,
        @Param("limit") int limit
    );

    /**
     * 처리 중인 이벤트 수 조회
     *
     * @return PENDING 또는 RETRY_PENDING 상태의 이벤트 수
     */
    @Query("SELECT COUNT(e) FROM DomainEventStore e " +
           "WHERE e.status IN ('PENDING', 'RETRY_PENDING')")
    long countPendingEvents();
}
