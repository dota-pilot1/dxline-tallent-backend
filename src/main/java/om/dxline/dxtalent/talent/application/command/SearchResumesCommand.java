package om.dxline.dxtalent.talent.application.command;

import java.util.List;
import om.dxline.dxtalent.talent.domain.model.SkillLevel;

/**
 * 이력서 검색 커맨드
 *
 * 이력서를 검색하기 위한 조건을 담는 Command 객체입니다.
 *
 * 검색 조건:
 * - 필수 스킬: 반드시 보유해야 하는 스킬
 * - 선호 스킬: 있으면 가산점을 주는 스킬
 * - 최소 경력 년수: 최소 총 경력 년수
 * - 최소 스킬 레벨: 스킬의 최소 숙련도
 * - 전공 키워드: 특정 전공 관련 검색
 * - 회사 키워드: 특정 회사 경험
 * - 직책 키워드: 특정 직책 경험
 * - 최소 매칭 점수: 검색 결과에 포함될 최소 점수
 *
 * 사용 예시:
 * <pre>
 * SearchResumesCommand command = SearchResumesCommand.builder()
 *     .requiredSkills(List.of("Java", "Spring Boot"))
 *     .preferredSkills(List.of("Docker", "Kubernetes"))
 *     .minimumYearsOfExperience(3)
 *     .minimumSkillLevel(SkillLevel.INTERMEDIATE)
 *     .minimumScore(50)
 *     .build();
 * </pre>
 */
public class SearchResumesCommand {

    private final List<String> requiredSkills;
    private final List<String> preferredSkills;
    private final Integer minimumYearsOfExperience;
    private final SkillLevel minimumSkillLevel;
    private final String majorKeyword;
    private final String companyKeyword;
    private final String positionKeyword;
    private final Integer minimumScore;
    private final Integer pageNumber;
    private final Integer pageSize;

    private SearchResumesCommand(Builder builder) {
        this.requiredSkills = builder.requiredSkills;
        this.preferredSkills = builder.preferredSkills;
        this.minimumYearsOfExperience = builder.minimumYearsOfExperience;
        this.minimumSkillLevel = builder.minimumSkillLevel;
        this.majorKeyword = builder.majorKeyword;
        this.companyKeyword = builder.companyKeyword;
        this.positionKeyword = builder.positionKeyword;
        this.minimumScore = builder.minimumScore != null ? builder.minimumScore : 0;
        this.pageNumber = builder.pageNumber != null ? builder.pageNumber : 0;
        this.pageSize = builder.pageSize != null ? builder.pageSize : 20;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public List<String> getPreferredSkills() {
        return preferredSkills;
    }

    public Integer getMinimumYearsOfExperience() {
        return minimumYearsOfExperience;
    }

    public SkillLevel getMinimumSkillLevel() {
        return minimumSkillLevel;
    }

    public String getMajorKeyword() {
        return majorKeyword;
    }

    public String getCompanyKeyword() {
        return companyKeyword;
    }

    public String getPositionKeyword() {
        return positionKeyword;
    }

    public Integer getMinimumScore() {
        return minimumScore;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 검색 조건이 비어있는지 확인
     *
     * @return 모든 조건이 null이면 true
     */
    public boolean isEmpty() {
        return (requiredSkills == null || requiredSkills.isEmpty()) &&
               (preferredSkills == null || preferredSkills.isEmpty()) &&
               minimumYearsOfExperience == null &&
               minimumSkillLevel == null &&
               (majorKeyword == null || majorKeyword.trim().isEmpty()) &&
               (companyKeyword == null || companyKeyword.trim().isEmpty()) &&
               (positionKeyword == null || positionKeyword.trim().isEmpty());
    }

    /**
     * Builder 클래스
     */
    public static class Builder {
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private Integer minimumYearsOfExperience;
        private SkillLevel minimumSkillLevel;
        private String majorKeyword;
        private String companyKeyword;
        private String positionKeyword;
        private Integer minimumScore;
        private Integer pageNumber;
        private Integer pageSize;

        private Builder() {}

        public Builder requiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public Builder preferredSkills(List<String> preferredSkills) {
            this.preferredSkills = preferredSkills;
            return this;
        }

        public Builder minimumYearsOfExperience(Integer minimumYearsOfExperience) {
            this.minimumYearsOfExperience = minimumYearsOfExperience;
            return this;
        }

        public Builder minimumSkillLevel(SkillLevel minimumSkillLevel) {
            this.minimumSkillLevel = minimumSkillLevel;
            return this;
        }

        public Builder majorKeyword(String majorKeyword) {
            this.majorKeyword = majorKeyword;
            return this;
        }

        public Builder companyKeyword(String companyKeyword) {
            this.companyKeyword = companyKeyword;
            return this;
        }

        public Builder positionKeyword(String positionKeyword) {
            this.positionKeyword = positionKeyword;
            return this;
        }

        public Builder minimumScore(Integer minimumScore) {
            this.minimumScore = minimumScore;
            return this;
        }

        public Builder pageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public SearchResumesCommand build() {
            return new SearchResumesCommand(this);
        }
    }

    @Override
    public String toString() {
        return "SearchResumesCommand{" +
                "requiredSkills=" + requiredSkills +
                ", preferredSkills=" + preferredSkills +
                ", minimumYearsOfExperience=" + minimumYearsOfExperience +
                ", minimumSkillLevel=" + minimumSkillLevel +
                ", majorKeyword='" + majorKeyword + '\'' +
                ", companyKeyword='" + companyKeyword + '\'' +
                ", positionKeyword='" + positionKeyword + '\'' +
                ", minimumScore=" + minimumScore +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                '}';
    }
}
