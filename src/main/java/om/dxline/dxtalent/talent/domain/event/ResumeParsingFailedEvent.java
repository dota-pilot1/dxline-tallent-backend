package om.dxline.dxtalent.talent.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * 이력서 파싱 실패 도메인 이벤트
 *
 * 이력서 파일의 파싱 작업이 실패했을 때 발행됩니다.
 *
 * 이벤트 용도:
 * - 관리자에게 파싱 실패 알림
 * - 재시도 스케줄링 (자동 재파싱)
 * - 실패 이력 기록
 * - 모니터링 및 에러 추적
 *
 * 사용 예시:
 * <pre>
 * ResumeParsingFailedEvent event = new ResumeParsingFailedEvent(
 *     resumeId,
 *     errorMessage,
 *     retryCount,
 *     LocalDateTime.now()
 * );
 * </pre>
 */
public class ResumeParsingFailedEvent implements DomainEvent {

    private final UUID eventId;
    private final ResumeId resumeId;
    private final String errorMessage;
    private final int retryCount;
    private final LocalDateTime failedAt;
    private final LocalDateTime occurredOn;

    /**
     * 이력서 파싱 실패 이벤트 생성
     *
     * @param resumeId 이력서 ID
     * @param errorMessage 실패 사유
     * @param retryCount 재시도 횟수
     * @param failedAt 실패 시각
     */
    public ResumeParsingFailedEvent(
        ResumeId resumeId,
        String errorMessage,
        int retryCount,
        LocalDateTime failedAt
    ) {
        this.eventId = UUID.randomUUID();
        this.resumeId = resumeId;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.failedAt = failedAt;
        this.occurredOn = LocalDateTime.now();
    }

    /**
     * 이벤트 ID 반환
     *
     * @return 이벤트 ID
     */
    @Override
    public UUID eventId() {
        return eventId;
    }

    /**
     * 이력서 ID 반환
     *
     * @return 이력서 ID
     */
    public ResumeId getResumeId() {
        return resumeId;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 실패 사유
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 재시도 횟수 반환
     *
     * @return 재시도 횟수
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 실패 시각 반환
     *
     * @return 실패 시각
     */
    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    /**
     * 재시도 가능 여부 확인
     * (일반적으로 3회 미만인 경우 재시도 가능)
     *
     * @return 재시도 가능하면 true
     */
    public boolean canRetry() {
        return retryCount < 3;
    }

    /**
     * 이벤트 발생 시각
     *
     * @return 발생 시각
     */
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format(
            "ResumeParsingFailedEvent[resumeId=%s, errorMessage=%s, retryCount=%d, failedAt=%s]",
            resumeId,
            errorMessage,
            retryCount,
            failedAt
        );
    }
}
