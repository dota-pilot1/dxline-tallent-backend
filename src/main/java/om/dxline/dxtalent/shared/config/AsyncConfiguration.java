package om.dxline.dxtalent.shared.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 처리 설정 (Async Configuration)
 *
 * 도메인 이벤트 핸들러의 비동기 처리를 위한 Thread Pool 설정입니다.
 *
 * 주요 설정:
 * - Core Pool Size: 5 (기본 스레드 수)
 * - Max Pool Size: 10 (최대 스레드 수)
 * - Queue Capacity: 100 (대기 큐 크기)
 * - Thread Name Prefix: "async-event-" (스레드 이름 접두사)
 *
 * 비동기 처리 플로우:
 * 1. 요청이 들어오면 Core Pool 스레드로 처리
 * 2. Core Pool이 모두 사용 중이면 Queue에 대기
 * 3. Queue가 가득 차면 Max Pool까지 스레드 증가
 * 4. 모두 가득 차면 RejectedExecutionHandler 동작
 *
 * 사용 예시:
 * <pre>
 * {@literal @}Async
 * {@literal @}TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
 * public void handleUserRegistered(UserRegisteredEvent event) {
 *     // 이 메서드는 별도 스레드에서 비동기로 실행됨
 *     emailService.sendWelcomeEmail(event.getEmail());
 * }
 * </pre>
 */
@Slf4j
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * 비동기 이벤트 처리를 위한 Executor 설정
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "asyncEventExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core Pool Size: 항상 유지되는 기본 스레드 수
        executor.setCorePoolSize(5);

        // Max Pool Size: 최대 스레드 수
        executor.setMaxPoolSize(10);

        // Queue Capacity: 대기 큐 크기
        // Core Pool이 모두 사용 중일 때 여기에 작업이 쌓임
        executor.setQueueCapacity(100);

        // Thread Name Prefix: 디버깅 시 스레드 식별에 유용
        executor.setThreadNamePrefix("async-event-");

        // Keep Alive Time: Max Pool 스레드가 유휴 상태일 때 유지 시간 (초)
        executor.setKeepAliveSeconds(60);

        // Allow Core Thread Timeout: Core 스레드도 타임아웃 허용
        executor.setAllowCoreThreadTimeOut(false);

        // Wait for Tasks to Complete on Shutdown
        // 애플리케이션 종료 시 대기 중인 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Await Termination (초): 종료 대기 시간
        executor.setAwaitTerminationSeconds(60);

        // Rejected Execution Handler
        // Queue가 가득 찼을 때의 처리 방식
        // CallerRunsPolicy: 호출한 스레드에서 직접 실행 (백프레셔)
        executor.setRejectedExecutionHandler(
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );

        // Task Decorator: 작업 실행 전후 처리 (선택)
        // executor.setTaskDecorator(new ContextCopyingDecorator());

        executor.initialize();

        log.info(
            "Async Event Executor initialized: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            executor.getCorePoolSize(),
            executor.getMaxPoolSize(),
            executor.getQueueCapacity()
        );

        return executor;
    }

    /**
     * 비동기 메서드에서 발생한 예외 처리
     *
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error(
                "비동기 메서드 실행 중 예외 발생: method={}, params={}, error={}",
                method.getName(),
                params,
                throwable.getMessage(),
                throwable
            );

            // TODO: 추가 예외 처리
            // 1. 에러 모니터링 시스템에 전송 (Sentry, DataDog 등)
            // 2. Dead Letter Queue에 저장하여 재처리 가능하도록
            // 3. 관리자에게 알림 전송

            // 예시: Dead Letter Queue 저장
            // deadLetterQueueService.save(
            //     method.getName(),
            //     Arrays.toString(params),
            //     throwable
            // );
        };
    }
}
