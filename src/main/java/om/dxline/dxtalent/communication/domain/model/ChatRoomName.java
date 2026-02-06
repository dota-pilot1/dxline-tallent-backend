package om.dxline.dxtalent.communication.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 채팅방 이름 값 객체 (ChatRoomName Value Object)
 *
 * 채팅방의 이름을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 길이: 1-100자
 * - 공백만으로 구성 불가
 * - 특수문자 허용 (이모지 포함)
 *
 * 사용 예시:
 * <pre>
 * ChatRoomName name = new ChatRoomName("프로젝트 논의방");
 * ChatRoomName name2 = ChatRoomName.of("1:1 채팅 - 홍길동");
 * </pre>
 */
public class ChatRoomName extends BaseValueObject {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;

    private final String value;

    /**
     * 채팅방 이름 생성
     *
     * @param value 채팅방 이름
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public ChatRoomName(String value) {
        this.value = requireNonNull(value, "채팅방 이름").trim();
        validateName(this.value);
    }

    /**
     * 이름 검증
     */
    private void validateName(String value) {
        // 길이 검증
        validate(
            value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH,
            String.format("채팅방 이름은 %d자 이상 %d자 이하여야 합니다", MIN_LENGTH, MAX_LENGTH)
        );

        // 공백만 있는지 검증
        validate(
            !value.trim().isEmpty(),
            "채팅방 이름은 공백만으로 구성될 수 없습니다"
        );
    }

    /**
     * 채팅방 이름 생성 정적 팩토리 메서드
     *
     * @param value 채팅방 이름
     * @return ChatRoomName 인스턴스
     */
    public static ChatRoomName of(String value) {
        return new ChatRoomName(value);
    }

    /**
     * 1:1 채팅방 이름 생성
     *
     * @param userName1 사용자1 이름
     * @param userName2 사용자2 이름
     * @return ChatRoomName 인스턴스
     */
    public static ChatRoomName forDirectChat(String userName1, String userName2) {
        return new ChatRoomName(String.format("%s, %s", userName1, userName2));
    }

    /**
     * 그룹 채팅방 이름 생성
     *
     * @param groupName 그룹명
     * @return ChatRoomName 인스턴스
     */
    public static ChatRoomName forGroup(String groupName) {
        return new ChatRoomName(groupName);
    }

    /**
     * 이름 값 반환
     *
     * @return 채팅방 이름 문자열
     */
    public String getValue() {
        return value;
    }

    /**
     * 이름 길이 반환
     *
     * @return 글자 수
     */
    public int getLength() {
        return value.length();
    }

    /**
     * 이름에 특정 키워드가 포함되어 있는지 확인
     *
     * @param keyword 검색 키워드
     * @return 포함되어 있으면 true
     */
    public boolean contains(String keyword) {
        return this.value.toLowerCase().contains(keyword.toLowerCase());
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
        return value;
    }
}
