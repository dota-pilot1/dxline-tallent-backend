package om.dxline.dxtalent.talent.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 기술(스킬) 값 객체 (Skill Value Object)
 *
 * 이력서에 기재된 기술/역량을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 스킬명: 2-50자
 * - 숙련도: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
 * - 경력 년수: 0-50년 (선택적)
 *
 * 사용 예시:
 * <pre>
 * Skill skill = new Skill("Java", SkillLevel.ADVANCED, 5);
 * Skill skill2 = Skill.of("Python", SkillLevel.INTERMEDIATE);
 * boolean isExpert = skill.isExpertLevel(); // false
 * </pre>
 */
public class Skill extends BaseValueObject {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_YEARS = 0;
    private static final int MAX_YEARS = 50;

    private final String name;
    private final SkillLevel level;
    private final Integer yearsOfExperience;

    /**
     * 스킬 생성 (경력 년수 포함)
     *
     * @param name 스킬명
     * @param level 숙련도
     * @param yearsOfExperience 경력 년수
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public Skill(String name, SkillLevel level, Integer yearsOfExperience) {
        this.name = requireNonNull(name, "스킬명").trim();
        this.level = requireNonNull(level, "숙련도");
        this.yearsOfExperience = yearsOfExperience;

        validateSkill();
    }

    /**
     * 스킬 생성 (경력 년수 없이)
     *
     * @param name 스킬명
     * @param level 숙련도
     */
    public Skill(String name, SkillLevel level) {
        this(name, level, null);
    }

    /**
     * 스킬 검증
     */
    private void validateSkill() {
        // 스킬명 길이 검증
        validate(
            name.length() >= MIN_NAME_LENGTH && name.length() <= MAX_NAME_LENGTH,
            String.format("스킬명은 %d자 이상 %d자 이하여야 합니다", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        );

        // 공백만 있는지 검증
        validate(
            !name.trim().isEmpty(),
            "스킬명은 공백만으로 구성될 수 없습니다"
        );

        // 경력 년수 검증 (있는 경우)
        if (yearsOfExperience != null) {
            validate(
                yearsOfExperience >= MIN_YEARS && yearsOfExperience <= MAX_YEARS,
                String.format("경력 년수는 %d년 이상 %d년 이하여야 합니다", MIN_YEARS, MAX_YEARS)
            );
        }
    }

    /**
     * 스킬 생성 정적 팩토리 메서드 (경력 년수 포함)
     *
     * @param name 스킬명
     * @param level 숙련도
     * @param yearsOfExperience 경력 년수
     * @return Skill 인스턴스
     */
    public static Skill of(String name, SkillLevel level, Integer yearsOfExperience) {
        return new Skill(name, level, yearsOfExperience);
    }

    /**
     * 스킬 생성 정적 팩토리 메서드 (경력 년수 없이)
     *
     * @param name 스킬명
     * @param level 숙련도
     * @return Skill 인스턴스
     */
    public static Skill of(String name, SkillLevel level) {
        return new Skill(name, level, null);
    }

    /**
     * 스킬 생성 정적 팩토리 메서드 (기본 레벨: INTERMEDIATE)
     *
     * @param name 스킬명
     * @return Skill 인스턴스
     */
    public static Skill of(String name) {
        return new Skill(name, SkillLevel.INTERMEDIATE, null);
    }

    /**
     * 스킬명 반환
     *
     * @return 스킬명
     */
    public String getName() {
        return name;
    }

    /**
     * 숙련도 반환
     *
     * @return 숙련도
     */
    public SkillLevel getLevel() {
        return level;
    }

    /**
     * 경력 년수 반환
     *
     * @return 경력 년수 (없으면 null)
     */
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    /**
     * 경력 년수가 있는지 확인
     *
     * @return 있으면 true
     */
    public boolean hasYearsOfExperience() {
        return yearsOfExperience != null;
    }

    /**
     * 전문가 레벨인지 확인
     *
     * @return EXPERT 레벨이면 true
     */
    public boolean isExpertLevel() {
        return level == SkillLevel.EXPERT;
    }

    /**
     * 초보자 레벨인지 확인
     *
     * @return BEGINNER 레벨이면 true
     */
    public boolean isBeginnerLevel() {
        return level == SkillLevel.BEGINNER;
    }

    /**
     * 특정 레벨 이상인지 확인
     *
     * @param minimumLevel 최소 레벨
     * @return 최소 레벨 이상이면 true
     */
    public boolean isLevelAtLeast(SkillLevel minimumLevel) {
        return level.isHigherThanOrEqual(minimumLevel);
    }

    /**
     * 특정 경력 년수 이상인지 확인
     *
     * @param minYears 최소 년수
     * @return 최소 년수 이상이면 true (경력 년수가 없으면 false)
     */
    public boolean hasExperienceAtLeast(int minYears) {
        return yearsOfExperience != null && yearsOfExperience >= minYears;
    }

    /**
     * 숙련도 업그레이드
     *
     * @param newLevel 새 숙련도
     * @return 새로운 Skill 인스턴스
     */
    public Skill upgradeLevel(SkillLevel newLevel) {
        return new Skill(this.name, newLevel, this.yearsOfExperience);
    }

    /**
     * 경력 년수 업데이트
     *
     * @param years 새 경력 년수
     * @return 새로운 Skill 인스턴스
     */
    public Skill withYearsOfExperience(Integer years) {
        return new Skill(this.name, this.level, years);
    }

    /**
     * 스킬명이 일치하는지 확인 (대소문자 구분 없음)
     *
     * @param skillName 비교할 스킬명
     * @return 일치하면 true
     */
    public boolean matchesName(String skillName) {
        return this.name.equalsIgnoreCase(skillName);
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     * 스킬명만으로 동등성 판단 (대소문자 구분 없음)
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { name.toLowerCase() };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(level.getKoreanName()).append(")");
        if (yearsOfExperience != null) {
            sb.append(" - ").append(yearsOfExperience).append("년");
        }
        return sb.toString();
    }
}
