package om.dxline.dxtalent.talent.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEvent;
import om.dxline.dxtalent.talent.domain.model.FileName;
import om.dxline.dxtalent.talent.domain.model.FileType;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * 이력서 업로드 완료 도메인 이벤트
 *
 * 이력서 파일이 성공적으로 업로드되었을 때 발행됩니다.
 *
 * 이벤트 용도:
 * - 자동 파싱 프로세스 트리거
 * - 업로드 알림 전송
 * - 업로드 이력 기록
 *
 * 사용 예시:
 * <pre>
 * ResumeUploadedEvent event = new ResumeUploadedEvent(
 *     resumeId,
 *     userId,
 *     fileName,
 *     fileType,
 *     LocalDateTime.now()
 * );
 * </pre>
 */
public class ResumeUploadedEvent implements DomainEvent {

    private final UUID eventId;
    private final ResumeId resumeId;
    private final UserId userId;
    private final FileName fileName;
    private final FileType fileType;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime occurredOn;

    /**
     * 이력서 업로드 완료 이벤트 생성
     *
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID
     * @param fileName 파일명
     * @param fileType 파일 타입
     * @param uploadedAt 업로드 시각
     */
    public ResumeUploadedEvent(
        ResumeId resumeId,
        UserId userId,
        FileName fileName,
        FileType fileType,
        LocalDateTime uploadedAt
    ) {
        this.eventId = UUID.randomUUID();
        this.resumeId = resumeId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
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
     * 사용자 ID 반환
     *
     * @return 사용자 ID
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * 파일명 반환
     *
     * @return 파일명
     */
    public FileName getFileName() {
        return fileName;
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
     * 업로드 시각 반환
     *
     * @return 업로드 시각
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
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
            "ResumeUploadedEvent[resumeId=%s, userId=%s, fileName=%s, fileType=%s, uploadedAt=%s]",
            resumeId,
            userId,
            fileName,
            fileType,
            uploadedAt
        );
    }
}
