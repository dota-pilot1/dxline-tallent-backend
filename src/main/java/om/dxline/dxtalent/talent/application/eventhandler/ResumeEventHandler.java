package om.dxline.dxtalent.talent.application.eventhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.talent.application.ResumeApplicationService;
import om.dxline.dxtalent.talent.domain.event.ResumeUploadedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeParsingCompletedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeParsingFailedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeDeletedEvent;
import om.dxline.dxtalent.talent.domain.port.ResumeParsingPort;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Resume 도메인 이벤트 핸들러
 *
 * Resume 애그리게이트에서 발생하는 도메인 이벤트를 처리합니다.
 *
 * 처리 이벤트:
 * - ResumeUploadedEvent: 이력서 업로드 완료 → 자동 파싱 시작
 * - ResumeParsingCompletedEvent: 파싱 완료 → 알림 전송 (추후)
 * - ResumeParsingFailedEvent: 파싱 실패 → 알림 전송 (추후)
 * - ResumeDeletedEvent: 이력서 삭제 → 파일 삭제 (추후)
 *
 * 설계 원칙:
 * - @TransactionalEventListener로 트랜잭션 커밋 후 실행
 * - @Async로 비동기 처리 (메인 플로우 블로킹 방지)
 * - 이벤트 처리 실패 시 로깅만 수행 (메인 플로우에 영향 없음)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeEventHandler {

    private final ResumeParsingPort resumeParsingPort;
    private final ResumeApplicationService resumeApplicationService;

    /**
     * 이력서 업로드 완료 이벤트 처리
     *
     * 업로드가 완료되면 자동으로 파싱을 시작합니다.
     *
     * @param event 이력서 업로드 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeUploaded(ResumeUploadedEvent event) {
        log.info("Handling ResumeUploadedEvent: resumeId={}, userId={}, fileName={}",
            event.getResumeId().getValue(),
            event.getUserId().getValue(),
            event.getFileName().getValue());

        try {
            // 1. S3에서 파일을 읽어 파싱 시작
            String s3Key = extractS3KeyFromEvent(event);

            log.info("Starting automatic parsing for resumeId: {}", event.getResumeId().getValue());

            // 2. 파싱 포트를 통해 파싱 수행
            ResumeParsingPort.ResumeParsingResult parsingResult =
                resumeParsingPort.parseResume(s3Key, event.getFileType());

            // 3. 파싱 결과를 Application Service로 전달
            resumeApplicationService.completeResumeParsing(
                event.getResumeId().getValue(),
                parsingResult
            );

            if (parsingResult.isSuccess()) {
                log.info("Resume parsing completed successfully: resumeId={}",
                    event.getResumeId().getValue());
            } else {
                log.warn("Resume parsing failed: resumeId={}, error={}",
                    event.getResumeId().getValue(),
                    parsingResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Failed to handle ResumeUploadedEvent: resumeId={}",
                event.getResumeId().getValue(), e);
            // 이벤트 처리 실패는 메인 플로우에 영향을 주지 않도록 예외를 먹음
        }
    }

    /**
     * 이력서 파싱 완료 이벤트 처리
     *
     * 파싱이 완료되면 알림을 전송합니다 (추후 구현).
     *
     * @param event 파싱 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeParsingCompleted(ResumeParsingCompletedEvent event) {
        log.info("Handling ResumeParsingCompletedEvent: resumeId={}, candidateName={}, skillCount={}, experienceCount={}",
            event.getResumeId().getValue(),
            event.getCandidateName() != null ? event.getCandidateName().getValue() : "N/A",
            event.getSkillCount(),
            event.getExperienceCount());

        try {
            // TODO: 알림 전송
            // - 사용자에게 파싱 완료 알림
            // - 관리자에게 새 이력서 등록 알림
            // - 검색 인덱스 업데이트 (Elasticsearch 등)

            log.debug("Resume parsing completed notification would be sent here");

        } catch (Exception e) {
            log.error("Failed to handle ResumeParsingCompletedEvent: resumeId={}",
                event.getResumeId().getValue(), e);
        }
    }

    /**
     * 이력서 파싱 실패 이벤트 처리
     *
     * 파싱이 실패하면 알림을 전송합니다 (추후 구현).
     *
     * @param event 파싱 실패 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeParsingFailed(ResumeParsingFailedEvent event) {
        log.warn("Handling ResumeParsingFailedEvent: resumeId={}, errorMessage={}, retryCount={}",
            event.getResumeId().getValue(),
            event.getErrorMessage(),
            event.getRetryCount());

        try {
            // TODO: 알림 전송
            // - 사용자에게 파싱 실패 알림
            // - 관리자에게 파싱 실패 알림 (재시도 필요)
            // - 재시도 큐에 추가 (재시도 횟수 제한)

            log.debug("Resume parsing failure notification would be sent here");

            // 자동 재시도 (최대 3회)
            if (event.getRetryCount() < 3) {
                log.info("Scheduling automatic retry for resumeId: {}, retryCount: {}",
                    event.getResumeId().getValue(), event.getRetryCount());
                // TODO: 재시도 스케줄링 (Spring Scheduler 또는 메시지 큐)
            }

        } catch (Exception e) {
            log.error("Failed to handle ResumeParsingFailedEvent: resumeId={}",
                event.getResumeId().getValue(), e);
        }
    }

    /**
     * 이력서 삭제 이벤트 처리
     *
     * 이력서가 삭제되면 관련 리소스를 정리합니다 (추후 구현).
     *
     * @param event 이력서 삭제 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeDeleted(ResumeDeletedEvent event) {
        log.info("Handling ResumeDeletedEvent: resumeId={}, deletedBy={}, s3Key={}",
            event.getResumeId().getValue(),
            event.getDeletedBy().getValue(),
            event.getS3Key());

        try {
            // TODO: 리소스 정리
            // - S3에서 파일 삭제 (선택적 - 백업 정책에 따라)
            // - 검색 인덱스에서 제거
            // - 캐시 무효화

            log.debug("Resume deletion cleanup would be performed here");

            // 참고: 소프트 삭제이므로 실제 파일은 보관할 수 있음
            // 하드 삭제 정책이 있다면 여기서 S3 파일도 삭제

        } catch (Exception e) {
            log.error("Failed to handle ResumeDeletedEvent: resumeId={}",
                event.getResumeId().getValue(), e);
        }
    }

    /**
     * 이벤트에서 S3 키 추출
     *
     * ResumeUploadedEvent는 S3 키를 직접 포함하지 않으므로,
     * 실제로는 Resume 애그리게이트를 다시 조회해야 합니다.
     *
     * 여기서는 간단하게 처리를 위해 resumeId로 조회하는 방식을 사용합니다.
     */
    private String extractS3KeyFromEvent(ResumeUploadedEvent event) {
        // 실제 구현에서는 Repository를 통해 Resume을 조회하여 S3 키를 얻어야 합니다.
        // 하지만 이벤트 핸들러가 Application Service를 호출하므로,
        // Application Service 내에서 처리하도록 위임합니다.

        // 임시로 resumeId를 기반으로 S3 키 형식 반환
        return String.format("resumes/user-%d/%s",
            event.getUserId().getValue(),
            event.getResumeId().getValue());
    }
}
