package om.dxline.dxtalent.talent.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import om.dxline.dxtalent.talent.domain.model.CandidateName;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * 이력서 파싱 완료 도메인 이벤트
 *
 * 이력서 파일의 파싱 작업이 성공적으로 완료되었을 때 발행됩니다.
 *
 * 이벤트 용도:
 * - 사용자에게 파싱 완료 알림 전송
 * - 검색 인덱스 업데이트
 * - 파싱 완료 이력 기록
 * - 통계 데이터 수집
 *
 * 사용 예시:
 * <pre>
 * ResumeParsingCompletedEvent event = new ResumeParsingCompletedEvent(
 *     resumeId,
 *     candidateName,
 *     skillCount,
 *     experienceCount,
 *     LocalDateTime.now()
 * );
 * </pre>
 */
public class ResumeParsingCompletedEvent implements DomainEvent {

    private final UUID eventId;
    private final ResumeId resumeId;
    private final CandidateName candidateName;
    private final int skillCount;
    private final int experienceCount;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredOn;

    /**
     * 이력서 파싱 완료 이벤트 생성
     *
     * @param resumeId 이력서 ID
     * @param candidateName 지원자 이름
     * @param skillCount 파싱된 스킬 개수
     * @param experienceCount 파싱된 경력 개수
     * @param completedAt 파싱 완료 시각
     */
    public ResumeParsingCompletedEvent(
        ResumeId resumeId,
        CandidateName candidateName,
        int skillCount,
        int experienceCount,
        LocalDateTime completedAt
    ) {
        this.eventId = UUID.randomUUID();
        this.resumeId = resumeId;
        this.candidateName = candidateName;
        this.skillCount = skillCount;
        this.experienceCount = experienceCount;
        this.completedAt = completedAt;
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
     * 지원자 이름 반환
     *
     * @return 지원자 이름
     */
    public CandidateName getCandidateName() {
        return candidateName;
    }

    /**
     * 스킬 개수 반환
     *
     * @return 파싱된 스킬 개수
     */
    public int getSkillCount() {
        return skillCount;
    }

    /**
     * 경력 개수 반환
     *
     * @return 파싱된 경력 개수
     */
    public int getExperienceCount() {
        return experienceCount;
    }

    /**
     * 파싱 완료 시각 반환
     *
     * @return 완료 시각
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
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
            "ResumeParsingCompletedEvent[resumeId=%s, candidateName=%s, skillCount=%d, experienceCount=%d, completedAt=%s]",
            resumeId,
            candidateName,
            skillCount,
            experienceCount,
            completedAt
        );
    }
}
