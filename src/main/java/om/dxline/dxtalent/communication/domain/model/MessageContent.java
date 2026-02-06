package om.dxline.dxtalent.communication.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 메시지 내용 값 객체 (MessageContent Value Object)
 *
 * 채팅 메시지의 내용을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 최소 길이: 1자
 * - 최대 길이: 5000자
 * - 공백만으로 구성 불가
 * - null 불가
 *
 * 사용 예시:
 * <pre>
 * MessageContent content = new MessageContent("안녕하세요!");
 * String text = content.getValue();
 * </pre>
 */
public class MessageContent extends BaseValueObject {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 5000;

    private final String value;

    /**
     * 메시지 내용 생성
     *
     * @param value 메시지 텍스트
     * @throws IllegalArgumentException 유효하지 않은 내용인 경우
     */
    public MessageContent(String value) {
        this.value = requireNonNull(value, "메시지 내용").trim();
        validateContent(this.value);
    }

    /**
     * 메시지 내용 검증
     */
    private void validateContent(String value) {
        // 공백만 있는지 검증
        validate(
            !value.isEmpty(),
            "메시지 내용은 공백만으로 구성될 수 없습니다"
        );

        // 길이 검증
        validate(
            value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH,
            String.format("메시지 내용은 %d자 이상 %d자 이하여야 합니다", MIN_LENGTH, MAX_LENGTH)
        );
    }

    /**
     * 메시지 내용 생성 정적 팩토리 메서드
     *
     * @param value 메시지 텍스트
     * @return MessageContent 인스턴스
     */
    public static MessageContent of(String value) {
        return new MessageContent(value);
    }

    /**
     * 메시지 내용 값 반환
     *
     * @return 메시지 텍스트
     */
    public String getValue() {
        return value;
    }

    /**
     * 메시지 길이 반환
     *
     * @return 글자 수
     */
    public int getLength() {
        return value.length();
    }

    /**
     * 특정 키워드를 포함하는지 확인
     *
     * @param keyword 검색 키워드
     * @return 포함하면 true
     */
    public boolean contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return value.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * 메시지가 짧은지 확인 (100자 미만)
     *
     * @return 짧으면 true
     */
    public boolean isShort() {
        return value.length() < 100;
    }

    /**
     * 메시지가 긴지 확인 (1000자 이상)
     *
     * @return 길면 true
     */
    public boolean isLong() {
        return value.length() >= 1000;
    }

    /**
     * 메시지 미리보기 생성 (처음 100자)
     *
     * @return 미리보기 텍스트
     */
    public String getPreview() {
        if (value.length() <= 100) {
            return value;
        }
        return value.substring(0, 100) + "...";
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
