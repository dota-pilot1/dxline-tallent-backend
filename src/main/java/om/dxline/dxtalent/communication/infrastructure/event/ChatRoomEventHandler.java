package om.dxline.dxtalent.communication.infrastructure.event;

import om.dxline.dxtalent.communication.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ChatRoom 도메인 이벤트 핸들러
 *
 * 채팅방 관련 도메인 이벤트를 처리합니다.
 *
 * 주요 책임:
 * - 채팅방 생성 시 알림 전송
 * - 메시지 전송 시 WebSocket 브로드캐스트
 * - 참여자 추가/제거 시 알림
 * - 메시지 읽음 처리 시 상태 업데이트
 *
 * 설계 원칙:
 * - 비동기 처리 (@Async)
 * - 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
 * - 이벤트 처리 실패가 원본 트랜잭션에 영향 없음
 */
@Component
public class ChatRoomEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomEventHandler.class);

    // TODO: WebSocket 메시징 템플릿 주입
    // private final SimpMessagingTemplate messagingTemplate;

    // TODO: 알림 서비스 주입
    // private final NotificationService notificationService;

    public ChatRoomEventHandler() {
        // Dependencies will be injected here
    }

    /**
     * 채팅방 생성 이벤트 처리
     *
     * 채팅방이 생성되면:
     * - 모든 참여자에게 알림 전송
     * - 채팅방 목록 갱신 이벤트 발행
     *
     * @param event 채팅방 생성 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomCreated(ChatRoomCreatedEvent event) {
        log.info("채팅방 생성 이벤트 처리 시작: chatRoomId={}, type={}, createdBy={}",
            event.getChatRoomId().getValue(),
            event.getChatRoomType(),
            event.getCreatedBy().getValue());

        try {
            // TODO: WebSocket으로 참여자들에게 새 채팅방 알림
            // messagingTemplate.convertAndSend(
            //     "/topic/chatrooms/new",
            //     new ChatRoomCreatedNotification(event)
            // );

            // TODO: 푸시 알림 전송 (그룹 채팅방인 경우)
            // if (event.getChatRoomType() == ChatRoomType.GROUP) {
            //     notificationService.sendPushNotification(
            //         participantIds,
            //         "새 그룹 채팅방에 초대되었습니다: " + event.getChatRoomName()
            //     );
            // }

            log.info("채팅방 생성 이벤트 처리 완료: chatRoomId={}",
                event.getChatRoomId().getValue());

        } catch (Exception e) {
            log.error("채팅방 생성 이벤트 처리 중 오류 발생: chatRoomId={}",
                event.getChatRoomId().getValue(), e);
            // 이벤트 처리 실패는 원본 트랜잭션에 영향 없음
        }
    }

    /**
     * 메시지 전송 이벤트 처리
     *
     * 메시지가 전송되면:
     * - WebSocket으로 채팅방 참여자들에게 실시간 전송
     * - 오프라인 사용자에게 푸시 알림
     * - 메시지 전송 통계 업데이트
     *
     * @param event 메시지 전송 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(MessageSentEvent event) {
        log.info("메시지 전송 이벤트 처리 시작: chatRoomId={}, messageId={}, senderId={}",
            event.getChatRoomId().getValue(),
            event.getMessageId().getValue(),
            event.getSenderId().getValue());

        try {
            // TODO: WebSocket으로 채팅방 참여자들에게 메시지 브로드캐스트
            // String destination = "/topic/chatrooms/" + event.getChatRoomId().getValue() + "/messages";
            // messagingTemplate.convertAndSend(
            //     destination,
            //     new MessageNotification(event)
            // );

            // TODO: 오프라인 사용자에게 푸시 알림
            // List<UserId> offlineParticipants = getOfflineParticipants(event.getChatRoomId());
            // if (!offlineParticipants.isEmpty()) {
            //     notificationService.sendPushNotification(
            //         offlineParticipants,
            //         "새 메시지: " + event.getContent().getValue()
            //     );
            // }

            // TODO: 메시지 전송 통계 업데이트
            // statisticsService.incrementMessageCount(event.getChatRoomId());

            log.info("메시지 전송 이벤트 처리 완료: messageId={}",
                event.getMessageId().getValue());

        } catch (Exception e) {
            log.error("메시지 전송 이벤트 처리 중 오류 발생: messageId={}",
                event.getMessageId().getValue(), e);
        }
    }

    /**
     * 참여자 추가 이벤트 처리
     *
     * 새 참여자가 추가되면:
     * - 기존 참여자들에게 알림
     * - 새 참여자에게 환영 메시지
     * - 채팅방 정보 동기화
     *
     * @param event 참여자 추가 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantJoined(ParticipantJoinedEvent event) {
        log.info("참여자 추가 이벤트 처리 시작: chatRoomId={}, participantId={}, invitedBy={}",
            event.getChatRoomId().getValue(),
            event.getParticipantId().getValue(),
            event.getInvitedBy().getValue());

        try {
            // TODO: WebSocket으로 채팅방에 참여자 추가 알림
            // String destination = "/topic/chatrooms/" + event.getChatRoomId().getValue() + "/participants";
            // messagingTemplate.convertAndSend(
            //     destination,
            //     new ParticipantJoinedNotification(event)
            // );

            // TODO: 새 참여자에게 채팅방 정보 전송
            // messagingTemplate.convertAndSendToUser(
            //     event.getParticipantId().getValue().toString(),
            //     "/queue/chatroom-info",
            //     getChatRoomInfo(event.getChatRoomId())
            // );

            // TODO: 새 참여자에게 푸시 알림
            // notificationService.sendPushNotification(
            //     event.getParticipantId(),
            //     "채팅방에 초대되었습니다"
            // );

            log.info("참여자 추가 이벤트 처리 완료: participantId={}",
                event.getParticipantId().getValue());

        } catch (Exception e) {
            log.error("참여자 추가 이벤트 처리 중 오류 발생: participantId={}",
                event.getParticipantId().getValue(), e);
        }
    }

    /**
     * 참여자 퇴장 이벤트 처리
     *
     * 참여자가 나가면:
     * - 남은 참여자들에게 알림
     * - 채팅방이 비었는지 확인
     * - 필요시 채팅방 정리
     *
     * @param event 참여자 퇴장 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantLeft(ParticipantLeftEvent event) {
        log.info("참여자 퇴장 이벤트 처리 시작: chatRoomId={}, userId={}",
            event.getChatRoomId().getValue(),
            event.getUserId().getValue());

        try {
            // TODO: WebSocket으로 채팅방에 참여자 퇴장 알림
            // String destination = "/topic/chatrooms/" + event.getChatRoomId().getValue() + "/participants";
            // messagingTemplate.convertAndSend(
            //     destination,
            //     new ParticipantLeftNotification(event)
            // );

            // TODO: 채팅방이 비었는지 확인하고 정리
            // if (chatRoomDomainService.isChatRoomEmpty(event.getChatRoomId())) {
            //     log.info("채팅방이 비었습니다. 정리 작업 시작: chatRoomId={}",
            //         event.getChatRoomId().getValue());
            //     // 채팅방 자동 삭제 또는 보관 처리
            // }

            log.info("참여자 퇴장 이벤트 처리 완료: userId={}",
                event.getUserId().getValue());

        } catch (Exception e) {
            log.error("참여자 퇴장 이벤트 처리 중 오류 발생: userId={}",
                event.getUserId().getValue(), e);
        }
    }

    /**
     * 메시지 읽음 이벤트 처리
     *
     * 메시지를 읽으면:
     * - 읽음 상태를 WebSocket으로 브로드캐스트
     * - 안 읽은 메시지 카운트 업데이트
     *
     * @param event 메시지 읽음 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageRead(MessageReadEvent event) {
        log.debug("메시지 읽음 이벤트 처리 시작: chatRoomId={}, messageId={}, userId={}",
            event.getChatRoomId().getValue(),
            event.getMessageId().getValue(),
            event.getUserId().getValue());

        try {
            // TODO: WebSocket으로 읽음 상태 브로드캐스트
            // String destination = "/topic/chatrooms/" + event.getChatRoomId().getValue() + "/read-status";
            // messagingTemplate.convertAndSend(
            //     destination,
            //     new MessageReadNotification(event)
            // );

            // TODO: 안 읽은 메시지 개수 캐시 업데이트
            // cacheService.updateUnreadCount(
            //     event.getChatRoomId(),
            //     event.getUserId()
            // );

            log.debug("메시지 읽음 이벤트 처리 완료: messageId={}",
                event.getMessageId().getValue());

        } catch (Exception e) {
            log.error("메시지 읽음 이벤트 처리 중 오류 발생: messageId={}",
                event.getMessageId().getValue(), e);
        }
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    /**
     * 오프라인 참여자 조회 (TODO: 구현 필요)
     */
    // private List<UserId> getOfflineParticipants(ChatRoomId chatRoomId) {
    //     // WebSocket 세션 관리자를 통해 온라인 상태 확인
    //     // 오프라인 사용자 목록 반환
    //     return Collections.emptyList();
    // }

    /**
     * 채팅방 정보 조회 (TODO: 구현 필요)
     */
    // private ChatRoomInfo getChatRoomInfo(ChatRoomId chatRoomId) {
    //     // 채팅방 상세 정보 조회 및 반환
    //     return null;
    // }
}
