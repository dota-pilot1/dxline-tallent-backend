package om.dxline.dxtalent.talent.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * 이력서 삭제 도메인 이벤트
 *
 * 이력서가 삭제(소프트 삭제)되었을 때 발행됩니다.
 *
 * 이벤트 용도:
 * - S3 파일 삭제
 * - 검색 인덱스에서 제거
 * - 삭제 이력 기록
 * - 관련 데이터 정리
 *
 * 사용 예시:
 * <pre>
 * ResumeDeletedEvent event = new ResumeDeletedEvent(
 *     resumeId,
 *     deletedBy,
 *     s3Key,
 *     LocalDateTime.now()
 * );
 * </pre>
 */
public class ResumeDeletedEvent implements DomainEvent {

    private final UUID eventId;
    private final ResumeId resumeId;
    private final UserId deletedBy;
    private final String s3Key;
    private final LocalDateTime deletedAt;
    private final LocalDateTime occurredOn;

    /**
     * 이력서 삭제 이벤트 생성
     *
     * @param resumeId 이력서 ID
     * @param deletedBy 삭제한 사용자 ID
     * @param s3Key S3 저장 키
     * @param deletedAt 삭제 시각
     */
    public ResumeDeletedEvent(
        ResumeId resumeId,
        UserId deletedBy,
        String s3Key,
        LocalDateTime deletedAt
    ) {
        this.eventId = UUID.randomUUID();
        this.resumeId = resumeId;
        this.deletedBy = deletedBy;
        this.s3Key = s3Key;
        this.deletedAt = deletedAt;
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
     * 삭제한 사용자 ID 반환
     *
     * @return 사용자 ID
     */
    public UserId getDeletedBy() {
        return deletedBy;
    }

    /**
     * S3 키 반환
     *
     * @return S3 저장 키
     */
    public String getS3Key() {
        return s3Key;
    }

    /**
     * 삭제 시각 반환
     *
     * @return 삭제 시각
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
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
            "ResumeDeletedEvent[resumeId=%s, deletedBy=%s, s3Key=%s, deletedAt=%s]",
            resumeId,
            deletedBy,
            s3Key,
            deletedAt
        );
    }
}
