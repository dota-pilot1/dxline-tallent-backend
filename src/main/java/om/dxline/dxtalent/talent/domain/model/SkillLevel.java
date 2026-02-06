package om.dxline.dxtalent.talent.domain.model;

/**
 * 스킬 숙련도 열거형 (SkillLevel Enum)
 *
 * 기술/역량의 숙련도 수준을 정의합니다.
 *
 * 레벨:
 * - BEGINNER: 초급 (1-2년 미만)
 * - INTERMEDIATE: 중급 (2-5년)
 * - ADVANCED: 고급 (5-10년)
 * - EXPERT: 전문가 (10년 이상)
 *
 * 사용 예시:
 * <pre>
 * SkillLevel level = SkillLevel.ADVANCED;
 * boolean isHigher = level.isHigherThan(SkillLevel.INTERMEDIATE); // true
 * int numericValue = level.getNumericValue(); // 3
 * </pre>
 */
public enum SkillLevel {

    BEGINNER(1, "초급", "1-2년 미만의 경험"),
    INTERMEDIATE(2, "중급", "2-5년의 경험"),
    ADVANCED(3, "고급", "5-10년의 경험"),
    EXPERT(4, "전문가", "10년 이상의 경험");

    private final int numericValue;
    private final String koreanName;
    private final String description;

    /**
     * SkillLevel 생성자
     *
     * @param numericValue 숫자 값 (비교용)
     * @param koreanName 한글 이름
     * @param description 설명
     */
    SkillLevel(int numericValue, String koreanName, String description) {
        this.numericValue = numericValue;
        this.koreanName = koreanName;
        this.description = description;
    }

    /**
     * 숫자 값 반환
     *
     * @return 1-4 사이의 숫자
     */
    public int getNumericValue() {
        return numericValue;
    }

    /**
     * 한글 이름 반환
     *
     * @return 한글 레벨명
     */
    public String getKoreanName() {
        return koreanName;
    }

    /**
     * 설명 반환
     *
     * @return 레벨 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 특정 레벨보다 높은지 확인
     *
     * @param other 비교할 레벨
     * @return 더 높으면 true
     */
    public boolean isHigherThan(SkillLevel other) {
        return this.numericValue > other.numericValue;
    }

    /**
     * 특정 레벨보다 낮은지 확인
     *
     * @param other 비교할 레벨
     * @return 더 낮으면 true
     */
    public boolean isLowerThan(SkillLevel other) {
        return this.numericValue < other.numericValue;
    }

    /**
     * 특정 레벨 이상인지 확인
     *
     * @param other 비교할 레벨
     * @return 같거나 높으면 true
     */
    public boolean isHigherThanOrEqual(SkillLevel other) {
        return this.numericValue >= other.numericValue;
    }

    /**
     * 특정 레벨 이하인지 확인
     *
     * @param other 비교할 레벨
     * @return 같거나 낮으면 true
     */
    public boolean isLowerThanOrEqual(SkillLevel other) {
        return this.numericValue <= other.numericValue;
    }

    /**
     * 다음 레벨 반환
     *
     * @return 다음 레벨 (최상위면 현재 레벨)
     */
    public SkillLevel nextLevel() {
        switch (this) {
            case BEGINNER:
                return INTERMEDIATE;
            case INTERMEDIATE:
                return ADVANCED;
            case ADVANCED:
                return EXPERT;
            case EXPERT:
                return EXPERT; // 이미 최고 레벨
            default:
                return this;
        }
    }

    /**
     * 이전 레벨 반환
     *
     * @return 이전 레벨 (최하위면 현재 레벨)
     */
    public SkillLevel previousLevel() {
        switch (this) {
            case EXPERT:
                return ADVANCED;
            case ADVANCED:
                return INTERMEDIATE;
            case INTERMEDIATE:
                return BEGINNER;
            case BEGINNER:
                return BEGINNER; // 이미 최하 레벨
            default:
                return this;
        }
    }

    /**
     * 레벨 업그레이드 가능 여부
     *
     * @return 업그레이드 가능하면 true
     */
    public boolean canUpgrade() {
        return this != EXPERT;
    }

    /**
     * 레벨 다운그레이드 가능 여부
     *
     * @return 다운그레이드 가능하면 true
     */
    public boolean canDowngrade() {
        return this != BEGINNER;
    }

    /**
     * 숫자 값으로 SkillLevel 찾기
     *
     * @param numericValue 숫자 값 (1-4)
     * @return SkillLevel
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static SkillLevel fromNumericValue(int numericValue) {
        for (SkillLevel level : values()) {
            if (level.numericValue == numericValue) {
                return level;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 스킬 레벨 값: " + numericValue);
    }

    /**
     * 한글 이름으로 SkillLevel 찾기
     *
     * @param koreanName 한글 이름
     * @return SkillLevel
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public static SkillLevel fromKoreanName(String koreanName) {
        for (SkillLevel level : values()) {
            if (level.koreanName.equals(koreanName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 스킬 레벨 이름: " + koreanName);
    }

    /**
     * 기본 레벨 반환
     *
     * @return INTERMEDIATE
     */
    public static SkillLevel getDefault() {
        return INTERMEDIATE;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", koreanName, name());
    }
}
