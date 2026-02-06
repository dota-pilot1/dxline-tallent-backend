package om.dxline.dxtalent.shared.application;

import lombok.Getter;

/**
 * Application Service 예외 기본 클래스
 *
 * Application Service에서 발생하는 모든 비즈니스 예외의 기본 클래스입니다.
 *
 * 예외 계층 구조:
 * - ApplicationException (기본)
 *   - ValidationException (검증 실패)
 *   - ResourceNotFoundException (리소스 없음)
 *   - DuplicateResourceException (중복 리소스)
 *   - UnauthorizedException (인증 실패)
 *   - ForbiddenException (권한 없음)
 *   - BusinessRuleViolationException (비즈니스 규칙 위반)
 *
 * 사용 예시:
 * <pre>
 * public class DuplicateEmailException extends ApplicationException {
 *     public DuplicateEmailException(String email) {
 *         super(
 *             ErrorCode.DUPLICATE_EMAIL,
 *             "이메일이 이미 사용 중입니다: " + email
 *         );
 *     }
 * }
 * </pre>
 *
 * HTTP 상태 코드 매핑:
 * - ValidationException -> 400 Bad Request
 * - ResourceNotFoundException -> 404 Not Found
 * - DuplicateResourceException -> 409 Conflict
 * - UnauthorizedException -> 401 Unauthorized
 * - ForbiddenException -> 403 Forbidden
 * - BusinessRuleViolationException -> 422 Unprocessable Entity
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    /**
     * 에러 코드 (API 응답에 사용)
     */
    private final String errorCode;

    /**
     * 사용자에게 보여줄 메시지
     */
    private final String userMessage;

    /**
     * 추가 상세 정보 (디버깅용)
     */
    private final Object details;

    /**
     * 기본 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    protected ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.details = null;
    }

    /**
     * 상세 정보를 포함한 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param details 추가 상세 정보
     */
    protected ApplicationException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.details = details;
    }

    /**
     * 원인 예외를 포함한 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    protected ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.details = null;
    }

    /**
     * 모든 정보를 포함한 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param details 추가 상세 정보
     * @param cause 원인 예외
     */
    protected ApplicationException(
        String errorCode,
        String message,
        Object details,
        Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.details = details;
    }

    /**
     * 에러 응답을 위한 정보 반환
     *
     * @return ErrorResponse 객체
     */
    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(
            errorCode,
            userMessage,
            details
        );
    }

    /**
     * 에러 응답 DTO
     */
    @Getter
    public static class ErrorResponse {
        private final String errorCode;
        private final String message;
        private final Object details;
        private final long timestamp;

        public ErrorResponse(String errorCode, String message, Object details) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // ============================================================
    // 표준 에러 코드 정의
    // ============================================================

    public static class ErrorCode {
        // 일반 에러 (1xxx)
        public static final String INTERNAL_SERVER_ERROR = "1000";
        public static final String INVALID_REQUEST = "1001";
        public static final String VALIDATION_ERROR = "1002";

        // 인증/인가 에러 (2xxx)
        public static final String UNAUTHORIZED = "2000";
        public static final String FORBIDDEN = "2001";
        public static final String INVALID_CREDENTIALS = "2002";
        public static final String TOKEN_EXPIRED = "2003";
        public static final String TOKEN_INVALID = "2004";

        // 리소스 에러 (3xxx)
        public static final String RESOURCE_NOT_FOUND = "3000";
        public static final String RESOURCE_ALREADY_EXISTS = "3001";
        public static final String DUPLICATE_EMAIL = "3002";
        public static final String DUPLICATE_RESOURCE = "3003";

        // 비즈니스 규칙 에러 (4xxx)
        public static final String BUSINESS_RULE_VIOLATION = "4000";
        public static final String INVALID_STATE_TRANSITION = "4001";
        public static final String INSUFFICIENT_PERMISSION = "4002";
        public static final String OPERATION_NOT_ALLOWED = "4003";

        // User 관련 에러 (5xxx)
        public static final String USER_NOT_FOUND = "5000";
        public static final String USER_ALREADY_EXISTS = "5001";
        public static final String USER_INACTIVE = "5002";
        public static final String USER_SUSPENDED = "5003";
        public static final String PASSWORD_MISMATCH = "5004";

        // Resume 관련 에러 (6xxx)
        public static final String RESUME_NOT_FOUND = "6000";
        public static final String RESUME_PARSING_FAILED = "6001";
        public static final String INVALID_FILE_FORMAT = "6002";
        public static final String FILE_TOO_LARGE = "6003";

        // ChatRoom 관련 에러 (7xxx)
        public static final String CHATROOM_NOT_FOUND = "7000";
        public static final String NOT_PARTICIPANT = "7001";
        public static final String CANNOT_ADD_PARTICIPANT = "7002";
        public static final String MESSAGE_NOT_FOUND = "7003";

        // 외부 서비스 에러 (8xxx)
        public static final String EXTERNAL_SERVICE_ERROR = "8000";
        public static final String S3_UPLOAD_FAILED = "8001";
        public static final String EMAIL_SEND_FAILED = "8002";
        public static final String AI_SERVICE_ERROR = "8003";

        private ErrorCode() {
            // Utility class
        }
    }
}
