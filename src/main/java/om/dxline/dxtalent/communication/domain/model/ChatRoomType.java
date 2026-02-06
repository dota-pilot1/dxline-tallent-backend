package om.dxline.dxtalent.communication.domain.model;

/**
 * 채팅방 타입 열거형 (ChatRoomType Enum)
 *
 * 채팅방의 유형을 정의합니다.
 *
 * 타입:
 * - DIRECT: 1:1 채팅 (2명의 참여자)
 * - GROUP: 그룹 채팅 (3명 이상의 참여자)
 *
 * 비즈니스 규칙:
 * - DIRECT 채팅방은 최대 2명까지만 참여 가능
 * - GROUP 채팅방은 2명 이상 무제한 참여 가능
 * - DIRECT 채팅방은 이름이 없을 수 있음 (참여자 이름으로 자동 생성)
 * - GROUP 채팅방은 반드시 이름이 있어야 함
 */
public enum ChatRoomType {

    /**
     * 1:1 채팅 (Direct Message)
     */
    DIRECT("1:1 채팅", 2, 2),

    /**
     * 그룹 채팅 (Group Chat)
     */
    GROUP("그룹 채팅", 2, Integer.MAX_VALUE);

    private final String koreanName;
    private final int minParticipants;
    private final int maxParticipants;

    /**
     * ChatRoomType 생성자
     *
     * @param koreanName 한글 이름
     * @param minParticipants 최소 참여자 수
     * @param maxParticipants 최대 참여자 수
     */
    ChatRoomType(String koreanName, int minParticipants, int maxParticipants) {
        this.koreanName = koreanName;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
    }

    /**
     * 한글 이름 반환
     *
     * @return 한글 타입명
     */
    public String getKoreanName() {
        return koreanName;
    }

    /**
     * 최소 참여자 수 반환
     *
     * @return 최소 참여자 수
     */
    public int getMinParticipants() {
        return minParticipants;
    }

    /**
     * 최대 참여자 수 반환
     *
     * @return 최대 참여자 수
     */
    public int getMaxParticipants() {
        return maxParticipants;
    }

    /**
     * 1:1 채팅인지 확인
     *
     * @return DIRECT 타입이면 true
     */
    public boolean isDirect() {
        return this == DIRECT;
    }

    /**
     * 그룹 채팅인지 확인
     *
     * @return GROUP 타입이면 true
     */
    public boolean isGroup() {
        return this == GROUP;
    }

    /**
     * 참여자 수가 유효한지 확인
     *
     * @param participantCount 참여자 수
     * @return 유효하면 true
     */
    public boolean isValidParticipantCount(int participantCount) {
        return participantCount >= minParticipants &&
               participantCount <= maxParticipants;
    }

    /**
     * 참여자 수가 최대치에 도달했는지 확인
     *
     * @param currentCount 현재 참여자 수
     * @return 최대치면 true
     */
    public boolean isMaxParticipantsReached(int currentCount) {
        return currentCount >= maxParticipants;
    }

    /**
     * 새 참여자를 추가할 수 있는지 확인
     *
     * @param currentCount 현재 참여자 수
     * @return 추가 가능하면 true
     */
    public boolean canAddParticipant(int currentCount) {
        return currentCount < maxParticipants;
    }

    /**
     * 참여자 수 검증 및 예외 발생
     *
     * @param participantCount 참여자 수
     * @throws IllegalStateException 유효하지 않은 참여자 수인 경우
     */
    public void validateParticipantCount(int participantCount) {
        if (!isValidParticipantCount(participantCount)) {
            throw new IllegalStateException(
                String.format(
                    "%s 채팅방은 %d명 이상 %d명 이하여야 합니다. 현재: %d명",
                    koreanName,
                    minParticipants,
                    maxParticipants == Integer.MAX_VALUE ? 999 : maxParticipants,
                    participantCount
                )
            );
        }
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", koreanName, name());
    }
}
