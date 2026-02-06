package om.dxline.dxtalent.communication.domain.model;

import java.util.UUID;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 채팅방 식별자 값 객체 (ChatRoom Identifier Value Object)
 *
 * 채팅방을 고유하게 식별하는 ID를 표현하는 값 객체입니다.
 *
 * 설계 특징:
 * - UUID 기반 식별자 사용 (분산 환경에 적합)
 * - 타입 안전성 보장 (ChatRoomId vs UserId 혼동 방지)
 * - 불변성 보장
 *
 * 사용 예시:
 * <pre>
 * ChatRoomId roomId = ChatRoomId.newId();
 * ChatRoomId existingId = ChatRoomId.of(uuid);
 * </pre>
 */
public class ChatRoomId extends BaseValueObject {

    private final UUID value;

    /**
     * 기존 UUID로 ChatRoomId 생성
     *
     * @param value UUID 값
     * @throws IllegalArgumentException UUID가 null인 경우
     */
    public ChatRoomId(UUID value) {
        this.value = requireNonNull(value, "ChatRoom ID");
    }

    /**
     * 새로운 ChatRoomId 생성
     *
     * @return 새 ChatRoomId
     */
    public static ChatRoomId newId() {
        return new ChatRoomId(UUID.randomUUID());
    }

    /**
     * 기존 UUID로 ChatRoomId 생성
     *
     * @param uuid UUID 값
     * @return ChatRoomId 인스턴스
     */
    public static ChatRoomId of(UUID uuid) {
        return new ChatRoomId(uuid);
    }

    /**
     * 문자열로부터 ChatRoomId 생성
     *
     * @param uuidString UUID 문자열
     * @return ChatRoomId 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 UUID 형식인 경우
     */
    public static ChatRoomId fromString(String uuidString) {
        try {
            return new ChatRoomId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 ChatRoom ID 형식입니다: " + uuidString, e);
        }
    }

    /**
     * UUID 값 반환
     *
     * @return UUID
     */
    public UUID getValue() {
        return value;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
