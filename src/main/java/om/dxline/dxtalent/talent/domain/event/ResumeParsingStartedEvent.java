package om.dxline.dxtalent.talent.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import om.dxline.dxtalent.talent.domain.model.FileType;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * 이력서 파싱 시작 도메인 이벤트
 *
 * 이력서 파일의 파싱 작업이 시작되었을 때 발행됩니다.
 *
 * 이벤트 용도:
 * - 실제 파싱 작업 큐에 추가
 * - 파싱 시작 알림 (사용자에게)
 * - 파싱 이력 기록
 * - 모니터링 및 로깅
 *
 * 사용 예시:
 * <pre>
 * ResumeParsingStartedEvent event = new ResumeParsingStartedEvent(
 *     resumeId,
 *     s3Key,
 *     fileType,
 *     LocalDateTime.now()
 * );
 * </pre>
 */
public class ResumeParsingStartedEvent implements DomainEvent {

    private final UUID eventId;
    private final ResumeId resumeId;
    private final String s3Key;
    private final FileType fileType;
    private final LocalDateTime startedAt;
    private final LocalDateTime occurredOn;

    /**
     * 이력서 파싱 시작 이벤트 생성
     *
     * @param resumeId 이력서 ID
     * @param s3Key S3 저장 키
     * @param fileType 파일 타입
     * @param startedAt 파싱 시작 시각
     */
    public ResumeParsingStartedEvent(
        ResumeId resumeId,
        String s3Key,
        FileType fileType,
        LocalDateTime startedAt
    ) {
        this.eventId = UUID.randomUUID();
        this.resumeId = resumeId;
        this.s3Key = s3Key;
        this.fileType = fileType;
        this.startedAt = startedAt;
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
     * S3 키 반환
     *
     * @return S3 저장 키
     */
    public String getS3Key() {
        return s3Key;
    }

    /**
     * 파일 타입 반환
     *
     * @return 파일 타입
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * 파싱 시작 시각 반환
     *
     * @return 시작 시각
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
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
            "ResumeParsingStartedEvent[resumeId=%s, s3Key=%s, fileType=%s, startedAt=%s]",
            resumeId,
            s3Key,
            fileType,
            startedAt
        );
    }
}
