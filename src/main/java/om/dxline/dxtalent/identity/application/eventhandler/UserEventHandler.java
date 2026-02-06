package om.dxline.dxtalent.identity.application.eventhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.identity.domain.model.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * User 도메인 이벤트 핸들러
 *
 * User 애그리게이트에서 발생하는 도메인 이벤트를 처리합니다.
 *
 * 주요 책임:
 * - 회원가입 완료 시 환영 이메일 발송
 * - 비밀번호 변경 시 알림 전송
 * - 로그인 시 로그 기록 및 통계 업데이트
 * - 계정 비활성화 시 관련 처리
 *
 * 설계 원칙:
 * - @TransactionalEventListener로 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
 * - @Async로 비동기 처리 (메인 플로우 블로킹 방지)
 * - 이벤트 처리 실패가 원본 트랜잭션에 영향 없음
 * - 실패 시 로깅 및 모니터링 (Dead Letter Queue 전송 등)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventHandler {

    // TODO: 이메일 서비스 주입
    // private final EmailService emailService;

    // TODO: 알림 서비스 주입
    // private final NotificationService notificationService;

    // TODO: 통계 서비스 주입
    // private final StatisticsService statisticsService;

    // TODO: 감사 로그 서비스 주입
    // private final AuditLogService auditLogService;

    /**
     * 사용자 등록 이벤트 처리
     *
     * 신규 사용자가 등록되면:
     * 1. 환영 이메일 발송
     * 2. 가입 통계 업데이트
     * 3. 관리자에게 알림 (필요 시)
     *
     * @param event 사용자 등록 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(User.UserRegisteredEvent event) {
        log.info(
            "사용자 등록 이벤트 처리 시작: userId={}, email={}",
            event.userId().getValue(),
            event.email().getValue()
        );

        try {
            // TODO: 환영 이메일 발송
            // String emailContent = buildWelcomeEmail(event);
            // emailService.sendEmail(
            //     event.getEmail().getValue(),
            //     "DX Talent에 오신 것을 환영합니다!",
            //     emailContent
            // );
            log.info(
                "환영 이메일 발송 예정: email={}",
                event.email().getValue()
            );

            // TODO: 가입 통계 업데이트
            // statisticsService.incrementUserRegistrationCount();
            log.info("가입 통계 업데이트 예정");

            // TODO: 관리자에게 알림
            // notificationService.notifyAdmins(
            //     "새로운 사용자 가입: " + event.email().getValue()
            // );

            log.info(
                "사용자 등록 이벤트 처리 완료: userId={}",
                event.userId().getValue()
            );
        } catch (Exception e) {
            log.error(
                "사용자 등록 이벤트 처리 중 오류 발생: userId={}",
                event.userId().getValue(),
                e
            );
            // 이벤트 처리 실패는 원본 트랜잭션에 영향 없음
            // TODO: Dead Letter Queue에 전송하여 재처리 가능하도록 함
        }
    }

    /**
     * 사용자 로그인 이벤트 처리
     *
     * 사용자가 로그인하면:
     * 1. 로그인 로그 기록 (감사 로그)
     * 2. 마지막 로그인 시간 업데이트
     * 3. 로그인 통계 업데이트
     * 4. 의심스러운 로그인 감지 (선택)
     *
     * @param event 사용자 로그인 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserLoggedIn(User.UserLoggedInEvent event) {
        log.info(
            "사용자 로그인 이벤트 처리 시작: userId={}",
            event.userId().getValue()
        );

        try {
            // TODO: 감사 로그 기록
            // auditLogService.logUserLogin(
            //     event.userId(),
            //     event.occurredOn(),
            //     request.getRemoteAddr() // IP 주소
            // );
            log.info(
                "로그인 로그 기록 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 로그인 통계 업데이트
            // statisticsService.incrementLoginCount(event.userId());
            log.info(
                "로그인 통계 업데이트 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 의심스러운 로그인 감지
            // if (securityService.isSuspiciousLogin(event)) {
            //     notificationService.sendSecurityAlert(
            //         event.userId(),
            //         "새로운 위치에서 로그인이 감지되었습니다."
            //     );
            // }

            log.info(
                "사용자 로그인 이벤트 처리 완료: userId={}",
                event.userId().getValue()
            );
        } catch (Exception e) {
            log.error(
                "사용자 로그인 이벤트 처리 중 오류 발생: userId={}",
                event.userId().getValue(),
                e
            );
        }
    }

    /**
     * 비밀번호 변경 이벤트 처리
     *
     * 비밀번호가 변경되면:
     * 1. 사용자에게 알림 이메일 발송
     * 2. 감사 로그 기록
     * 3. 모든 기존 세션 무효화 (선택)
     *
     * @param event 비밀번호 변경 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(User.PasswordChangedEvent event) {
        log.info(
            "비밀번호 변경 이벤트 처리 시작: userId={}",
            event.userId().getValue()
        );

        try {
            // TODO: 비밀번호 변경 알림 이메일 발송
            // emailService.sendEmail(
            //     event.userId() + "@email.com", // TODO: Email 정보 필요
            //     "비밀번호가 변경되었습니다",
            //     buildPasswordChangedEmail(event)
            // );
            log.info(
                "비밀번호 변경 알림 이메일 발송 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 감사 로그 기록
            // auditLogService.logPasswordChanged(
            //     event.userId(),
            //     event.occurredOn()
            // );
            log.info(
                "비밀번호 변경 감사 로그 기록 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 보안을 위해 모든 기존 세션 무효화
            // sessionService.invalidateAllUserSessions(event.userId());
            log.info(
                "기존 세션 무효화 예정: userId={}",
                event.userId().getValue()
            );

            log.info(
                "비밀번호 변경 이벤트 처리 완료: userId={}",
                event.userId().getValue()
            );
        } catch (Exception e) {
            log.error(
                "비밀번호 변경 이벤트 처리 중 오류 발생: userId={}",
                event.userId().getValue(),
                e
            );
        }
    }

    /**
     * 사용자 비활성화 이벤트 처리
     *
     * 사용자 계정이 비활성화되면:
     * 1. 모든 활성 세션 무효화
     * 2. 사용자에게 알림 이메일 발송
     * 3. 관련 데이터 정리 (선택)
     * 4. 감사 로그 기록
     *
     * @param event 사용자 비활성화 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeactivated(User.UserDeactivatedEvent event) {
        log.info(
            "사용자 비활성화 이벤트 처리 시작: userId={}, reason={}",
            event.userId().getValue(),
            event.reason()
        );

        try {
            // TODO: 모든 활성 세션 무효화
            // sessionService.invalidateAllUserSessions(event.userId());
            log.info(
                "활성 세션 무효화 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 계정 비활성화 알림 이메일 발송
            // emailService.sendEmail(
            //     event.userId() + "@email.com", // TODO: Email 정보 필요
            //     "계정이 비활성화되었습니다",
            //     buildAccountDeactivatedEmail(event)
            // );
            log.info(
                "계정 비활성화 알림 이메일 발송 예정: userId={}",
                event.userId().getValue()
            );

            // TODO: 감사 로그 기록
            // auditLogService.logUserDeactivated(
            //     event.userId(),
            //     event.reason(),
            //     event.occurredOn()
            // );
            log.info(
                "계정 비활성화 감사 로그 기록 예정: userId={}, reason={}",
                event.userId().getValue(),
                event.reason()
            );

            // TODO: 관련 데이터 정리 (선택)
            // - 임시 파일 삭제
            // - 캐시 무효화
            // - 알림 구독 취소 등

            log.info(
                "사용자 비활성화 이벤트 처리 완료: userId={}",
                event.userId().getValue()
            );
        } catch (Exception e) {
            log.error(
                "사용자 비활성화 이벤트 처리 중 오류 발생: userId={}",
                event.userId().getValue(),
                e
            );
        }
    }

    // ============================================================
    // Private Helper Methods (TODO: 이메일 템플릿 빌더)
    // ============================================================

    /**
     * 환영 이메일 내용 생성
     */
    // private String buildWelcomeEmail(UserRegisteredEvent event) {
    //     return String.format(
    //         "안녕하세요 %s님!\n\n" +
    //         "DX Talent에 가입해주셔서 감사합니다.\n" +
    //         "이제 다양한 인재 관리 기능을 사용하실 수 있습니다.\n\n" +
    //         "문의사항이 있으시면 언제든 연락주세요.\n\n" +
    //         "감사합니다.\n" +
    //         "DX Talent 팀",
    //         event.getName().getValue()
    //     );
    // }

    /**
     * 비밀번호 변경 알림 이메일 내용 생성
     */
    // private String buildPasswordChangedEmail(PasswordChangedEvent event) {
    //     return String.format(
    //         "안녕하세요,\n\n" +
    //         "회원님의 비밀번호가 %s에 변경되었습니다.\n\n" +
    //         "본인이 변경한 것이 아니라면 즉시 고객센터로 연락주시기 바랍니다.\n\n" +
    //         "감사합니다.\n" +
    //         "DX Talent 팀",
    //         event.occurredOn()
    //     );
    // }

    /**
     * 계정 비활성화 알림 이메일 내용 생성
     */
    // private String buildAccountDeactivatedEmail(UserDeactivatedEvent event) {
    //     return String.format(
    //         "안녕하세요,\n\n" +
    //         "회원님의 계정이 비활성화되었습니다.\n" +
    //         "사유: %s\n\n" +
    //         "문의사항이 있으시면 고객센터로 연락주시기 바랍니다.\n\n" +
    //         "감사합니다.\n" +
    //         "DX Talent 팀",
    //         event.getReason()
    //     );
    // }
}
