package om.dxline.dxtalent.shared.config;

import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.shared.application.ApplicationException;
import om.dxline.dxtalent.shared.application.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 글로벌 예외 핸들러
 *
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리합니다.
 *
 * 응답 형식:
 * <pre>
 * {
 *   "timestamp": "2024-02-06T10:00:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "errorCode": "3000",
 *   "message": "User를 찾을 수 없습니다: userId=123",
 *   "details": {
 *     "resourceType": "User",
 *     "idField": "userId",
 *     "idValue": 123
 *   },
 *   "path": "/api/users/123"
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Application 예외 처리
     *
     * @param ex ApplicationException
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
        ApplicationException ex,
        WebRequest request
    ) {
        log.error("Application 예외 발생: errorCode={}, message={}",
            ex.getErrorCode(), ex.getMessage());

        HttpStatus status = determineHttpStatus(ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .errorCode(ex.getErrorCode())
            .message(ex.getUserMessage())
            .details(ex.getDetails())
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * ResourceNotFoundException 처리
     *
     * @param ex ResourceNotFoundException
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex,
        WebRequest request
    ) {
        log.warn("리소스를 찾을 수 없음: message={}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .errorCode(ex.getErrorCode())
            .message(ex.getUserMessage())
            .details(ex.getDetails())
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Validation 예외 처리 (Spring Validation)
     *
     * @param ex MethodArgumentNotValidException
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        log.warn("Validation 실패: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode(ApplicationException.ErrorCode.VALIDATION_ERROR)
            .message("입력값 검증에 실패했습니다")
            .details(validationErrors)
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     *
     * @param ex IllegalArgumentException
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        WebRequest request
    ) {
        log.warn("잘못된 인자: message={}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode(ApplicationException.ErrorCode.INVALID_REQUEST)
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * IllegalStateException 처리
     *
     * @param ex IllegalStateException
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
        IllegalStateException ex,
        WebRequest request
    ) {
        log.warn("잘못된 상태: message={}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
            .errorCode(ApplicationException.ErrorCode.BUSINESS_RULE_VIOLATION)
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * 기타 모든 예외 처리
     *
     * @param ex Exception
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request
    ) {
        log.error("예상치 못한 예외 발생: message={}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .errorCode(ApplicationException.ErrorCode.INTERNAL_SERVER_ERROR)
            .message("서버 내부 오류가 발생했습니다")
            .path(extractPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * ApplicationException의 타입에 따라 HTTP 상태 코드 결정
     *
     * @param ex ApplicationException
     * @return HttpStatus
     */
    private HttpStatus determineHttpStatus(ApplicationException ex) {
        String errorCode = ex.getErrorCode();

        // 인증/인가 에러 (2xxx)
        if (errorCode.startsWith("2")) {
            if (errorCode.equals(ApplicationException.ErrorCode.UNAUTHORIZED) ||
                errorCode.equals(ApplicationException.ErrorCode.INVALID_CREDENTIALS) ||
                errorCode.equals(ApplicationException.ErrorCode.TOKEN_EXPIRED) ||
                errorCode.equals(ApplicationException.ErrorCode.TOKEN_INVALID)) {
                return HttpStatus.UNAUTHORIZED;
            }
            if (errorCode.equals(ApplicationException.ErrorCode.FORBIDDEN)) {
                return HttpStatus.FORBIDDEN;
            }
        }

        // 리소스 에러 (3xxx)
        if (errorCode.startsWith("3")) {
            if (errorCode.equals(ApplicationException.ErrorCode.RESOURCE_NOT_FOUND)) {
                return HttpStatus.NOT_FOUND;
            }
            if (errorCode.equals(ApplicationException.ErrorCode.RESOURCE_ALREADY_EXISTS) ||
                errorCode.equals(ApplicationException.ErrorCode.DUPLICATE_EMAIL) ||
                errorCode.equals(ApplicationException.ErrorCode.DUPLICATE_RESOURCE)) {
                return HttpStatus.CONFLICT;
            }
        }

        // 비즈니스 규칙 에러 (4xxx)
        if (errorCode.startsWith("4")) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }

        // User/Resume/ChatRoom 에러 (5xxx, 6xxx, 7xxx)
        if (errorCode.startsWith("5") || errorCode.startsWith("6") || errorCode.startsWith("7")) {
            if (errorCode.endsWith("00")) { // NOT_FOUND 에러
                return HttpStatus.NOT_FOUND;
            }
            return HttpStatus.BAD_REQUEST;
        }

        // 외부 서비스 에러 (8xxx)
        if (errorCode.startsWith("8")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        // Validation 에러 (1002)
        if (errorCode.equals(ApplicationException.ErrorCode.VALIDATION_ERROR)) {
            return HttpStatus.BAD_REQUEST;
        }

        // 기본값
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * WebRequest에서 요청 경로 추출
     *
     * @param request WebRequest
     * @return 요청 경로
     */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /**
     * 에러 응답 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private Object details;
        private String path;
    }
}
