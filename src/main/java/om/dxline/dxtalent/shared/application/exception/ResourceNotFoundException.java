package om.dxline.dxtalent.shared.application.exception;

import om.dxline.dxtalent.shared.application.ApplicationException;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 *
 * HTTP 상태 코드: 404 Not Found
 *
 * 사용 예시:
 * <pre>
 * User user = userRepository.findById(userId)
 *     .orElseThrow(() -> new ResourceNotFoundException(
 *         "User",
 *         "userId",
 *         userId
 *     ));
 * </pre>
 */
public class ResourceNotFoundException extends ApplicationException {

    /**
     * 리소스 타입과 ID로 예외 생성
     *
     * @param resourceType 리소스 타입 (예: "User", "Resume", "ChatRoom")
     * @param idField ID 필드명 (예: "userId", "resumeId")
     * @param idValue ID 값
     */
    public ResourceNotFoundException(
        String resourceType,
        String idField,
        Object idValue
    ) {
        super(
            ErrorCode.RESOURCE_NOT_FOUND,
            String.format("%s를 찾을 수 없습니다: %s=%s", resourceType, idField, idValue),
            new ResourceDetails(resourceType, idField, idValue)
        );
    }

    /**
     * 리소스 타입으로만 예외 생성
     *
     * @param resourceType 리소스 타입
     */
    public ResourceNotFoundException(String resourceType) {
        super(
            ErrorCode.RESOURCE_NOT_FOUND,
            String.format("%s를 찾을 수 없습니다", resourceType)
        );
    }

    /**
     * 커스텀 메시지로 예외 생성
     *
     * @param message 에러 메시지
     */
    public ResourceNotFoundException(String message, String errorCode) {
        super(errorCode, message);
    }

    /**
     * 리소스 상세 정보
     */
    public record ResourceDetails(
        String resourceType,
        String idField,
        Object idValue
    ) {}
}
