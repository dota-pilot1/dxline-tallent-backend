package om.dxline.dxtalent.shared.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 응답 래퍼 클래스
 *
 * 모든 API 응답을 일관된 형식으로 반환하기 위한 래퍼 클래스입니다.
 *
 * 성공 응답 형식:
 * <pre>
 * {
 *   "success": true,
 *   "data": {
 *     "userId": 1,
 *     "email": "user@example.com",
 *     "name": "홍길동"
 *   },
 *   "message": "회원가입이 완료되었습니다",
 *   "timestamp": "2024-02-06T10:00:00"
 * }
 * </pre>
 *
 * 실패 응답 형식:
 * <pre>
 * {
 *   "success": false,
 *   "errorCode": "3002",
 *   "message": "이메일이 이미 사용 중입니다",
 *   "details": {
 *     "email": "user@example.com"
 *   },
 *   "timestamp": "2024-02-06T10:00:00"
 * }
 * </pre>
 *
 * 사용 예시:
 * <pre>
 * // 성공 응답
 * return ApiResponse.success(userResult);
 * return ApiResponse.success(userResult, "회원가입이 완료되었습니다");
 *
 * // 실패 응답
 * return ApiResponse.error("3002", "이메일이 이미 사용 중입니다");
 * return ApiResponse.error("3002", "이메일이 이미 사용 중입니다", details);
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 성공 여부
     */
    private final boolean success;

    /**
     * 응답 데이터 (성공 시)
     */
    private final T data;

    /**
     * 메시지
     */
    private final String message;

    /**
     * 에러 코드 (실패 시)
     */
    private final String errorCode;

    /**
     * 에러 상세 정보 (실패 시)
     */
    private final Object details;

    /**
     * 응답 시각
     */
    private final LocalDateTime timestamp;

    /**
     * Private 생성자 (빌더 패턴)
     */
    private ApiResponse(
        boolean success,
        T data,
        String message,
        String errorCode,
        Object details
    ) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // ============================================================
    // 성공 응답 생성 메서드
    // ============================================================

    /**
     * 데이터만 포함한 성공 응답
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, null);
    }

    /**
     * 데이터와 메시지를 포함한 성공 응답
     *
     * @param data 응답 데이터
     * @param message 성공 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null);
    }

    /**
     * 메시지만 포함한 성공 응답 (데이터 없음)
     *
     * @param message 성공 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message, null, null);
    }

    /**
     * 데이터 없이 성공만 표시
     *
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, "성공", null, null);
    }

    // ============================================================
    // 실패 응답 생성 메서드
    // ============================================================

    /**
     * 에러 코드와 메시지를 포함한 실패 응답
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode, null);
    }

    /**
     * 에러 코드, 메시지, 상세 정보를 포함한 실패 응답
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param details 에러 상세 정보
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, Object details) {
        return new ApiResponse<>(false, null, message, errorCode, details);
    }

    /**
     * ApplicationException으로부터 실패 응답 생성
     *
     * @param exception ApplicationException
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ApplicationException exception) {
        return new ApiResponse<>(
            false,
            null,
            exception.getUserMessage(),
            exception.getErrorCode(),
            exception.getDetails()
        );
    }

    // ============================================================
    // 페이지네이션 응답 생성 메서드
    // ============================================================

    /**
     * 페이지네이션 데이터를 포함한 성공 응답
     *
     * @param data 페이지 데이터
     * @param page 현재 페이지 (0-based)
     * @param size 페이지 크기
     * @param totalElements 전체 요소 개수
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<PageResponse<T>> page(
        T data,
        int page,
        int size,
        long totalElements
    ) {
        PageResponse<T> pageResponse = new PageResponse<>(
            data,
            page,
            size,
            totalElements
        );
        return new ApiResponse<>(true, pageResponse, null, null, null);
    }

    /**
     * 페이지네이션 응답 DTO
     *
     * @param <T> 데이터 타입
     */
    @Getter
    public static class PageResponse<T> {
        private final T content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;

        public PageResponse(T content, int page, int size, long totalElements) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
            this.first = page == 0;
            this.last = page >= totalPages - 1;
        }
    }

    // ============================================================
    // 리스트 응답 생성 메서드
    // ============================================================

    /**
     * 리스트 데이터를 포함한 성공 응답
     *
     * @param data 리스트 데이터
     * @param totalCount 전체 개수
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<ListResponse<T>> list(T data, long totalCount) {
        ListResponse<T> listResponse = new ListResponse<>(data, totalCount);
        return new ApiResponse<>(true, listResponse, null, null, null);
    }

    /**
     * 리스트 응답 DTO
     *
     * @param <T> 데이터 타입
     */
    @Getter
    public static class ListResponse<T> {
        private final T items;
        private final long totalCount;

        public ListResponse(T items, long totalCount) {
            this.items = items;
            this.totalCount = totalCount;
        }
    }

    // ============================================================
    // Utility 메서드
    // ============================================================

    /**
     * 응답이 성공인지 확인
     *
     * @return 성공이면 true
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 응답이 실패인지 확인
     *
     * @return 실패면 true
     */
    public boolean isError() {
        return !success;
    }

    @Override
    public String toString() {
        if (success) {
            return "ApiResponse{" +
                "success=true" +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
        } else {
            return "ApiResponse{" +
                "success=false" +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", details=" + details +
                ", timestamp=" + timestamp +
                '}';
        }
    }
}
