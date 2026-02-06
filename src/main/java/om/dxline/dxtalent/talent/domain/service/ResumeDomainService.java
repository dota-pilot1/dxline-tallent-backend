package om.dxline.dxtalent.talent.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import om.dxline.dxtalent.talent.domain.model.Experience;
import om.dxline.dxtalent.talent.domain.model.Resume;
import om.dxline.dxtalent.talent.domain.model.Skill;
import om.dxline.dxtalent.talent.domain.model.SkillLevel;
import org.springframework.stereotype.Service;

/**
 * Resume 도메인 서비스 (Domain Service)
 *
 * 여러 애그리게이트에 걸친 비즈니스 로직이나
 * 단일 애그리게이트에 속하지 않는 도메인 로직을 처리합니다.
 *
 * 주요 책임:
 * - 이력서 중복 확인 (이메일, 전화번호 기반)
 * - 스킬/경력 기반 매칭 점수 계산
 * - 이력서 검색 조건 검증
 * - 도메인 규칙 검증 (여러 애그리게이트 관련)
 *
 * 설계 원칙:
 * - Stateless: 상태를 갖지 않음
 * - 도메인 로직만 포함 (인프라 의존성 없음)
 * - 순수한 비즈니스 로직
 */
@Service
public class ResumeDomainService {

    /**
     * 이력서 중복 여부 확인
     *
     * 동일한 이메일 또는 전화번호를 가진 이력서가 있는지 확인합니다.
     * 중복 기준:
     * - 동일한 이메일 주소
     * - 동일한 전화번호
     * - 동일한 후보자 이름 + 유사한 연락처
     *
     * @param target 확인할 이력서
     * @param existingResumes 기존 이력서 목록
     * @return 중복이면 true
     */
    public boolean isDuplicateResume(
        Resume target,
        List<Resume> existingResumes
    ) {
        if (target.getContactInfo() == null) {
            return false; // 연락처 정보 없으면 중복 판단 불가
        }

        for (Resume existing : existingResumes) {
            // 자기 자신과는 비교하지 않음 (객체 참조 또는 ID 비교)
            if (target == existing) {
                continue;
            }
            if (
                target.getId() != null &&
                existing.getId() != null &&
                target.getId().equals(existing.getId())
            ) {
                continue;
            }

            if (existing.getContactInfo() == null) {
                continue;
            }

            // 이메일이 같으면 중복
            if (
                target.getContactInfo().hasEmail() &&
                existing.getContactInfo().hasEmail()
            ) {
                if (
                    target
                        .getContactInfo()
                        .getEmail()
                        .equals(existing.getContactInfo().getEmail())
                ) {
                    return true;
                }
            }

            // 전화번호가 같으면 중복
            if (
                target.getContactInfo().hasPhoneNumber() &&
                existing.getContactInfo().hasPhoneNumber()
            ) {
                if (
                    target
                        .getContactInfo()
                        .getPhoneNumber()
                        .equals(existing.getContactInfo().getPhoneNumber())
                ) {
                    return true;
                }
            }

            // 이름이 같고 연락처가 유사하면 중복 가능성
            if (
                target.getCandidateName() != null &&
                existing.getCandidateName() != null
            ) {
                if (
                    target
                        .getCandidateName()
                        .equals(existing.getCandidateName())
                ) {
                    // 이름이 같으면서 이메일이나 전화번호 중 하나라도 같으면 중복
                    boolean contactMatch = false;
                    if (
                        target.getContactInfo().hasEmail() &&
                        existing.getContactInfo().hasEmail()
                    ) {
                        contactMatch = target
                            .getContactInfo()
                            .getEmail()
                            .equals(existing.getContactInfo().getEmail());
                    }
                    if (
                        !contactMatch &&
                        target.getContactInfo().hasPhoneNumber() &&
                        existing.getContactInfo().hasPhoneNumber()
                    ) {
                        contactMatch = target
                            .getContactInfo()
                            .getPhoneNumber()
                            .equals(existing.getContactInfo().getPhoneNumber());
                    }
                    if (contactMatch) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 매칭 점수 계산
     *
     * 요구 스킬과 후보자의 스킬을 비교하여 매칭 점수를 계산합니다.
     * 점수 계산 방식:
     * - 필수 스킬 매칭: 각 20점 (최대 100점 초과 가능)
     * - 선호 스킬 매칭: 각 10점
     * - 스킬 레벨 가산점: EXPERT +5, ADVANCED +3, INTERMEDIATE +1
     * - 경력 년수 가산점: 요구 년수 이상이면 +5
     *
     * @param resume 후보자 이력서
     * @param requiredSkills 필수 스킬 목록 (스킬명)
     * @param preferredSkills 선호 스킬 목록 (스킬명)
     * @param minimumYearsOfExperience 최소 경력 년수 요구사항
     * @return 매칭 점수 (0-100+)
     */
    public int calculateMatchingScore(
        Resume resume,
        List<String> requiredSkills,
        List<String> preferredSkills,
        Integer minimumYearsOfExperience
    ) {
        int score = 0;

        // 1. 필수 스킬 매칭
        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            for (String requiredSkill : requiredSkills) {
                for (Skill candidateSkill : resume.getSkills()) {
                    if (candidateSkill.matchesName(requiredSkill)) {
                        score += 20; // 필수 스킬 매칭: 20점

                        // 레벨 가산점
                        score += getSkillLevelBonus(candidateSkill.getLevel());

                        // 경력 년수 가산점
                        if (
                            candidateSkill.hasYearsOfExperience() &&
                            minimumYearsOfExperience != null
                        ) {
                            if (
                                candidateSkill.getYearsOfExperience() >=
                                minimumYearsOfExperience
                            ) {
                                score += 5;
                            }
                        }
                        break;
                    }
                }
            }
        }

        // 2. 선호 스킬 매칭
        if (preferredSkills != null && !preferredSkills.isEmpty()) {
            for (String preferredSkill : preferredSkills) {
                for (Skill candidateSkill : resume.getSkills()) {
                    if (candidateSkill.matchesName(preferredSkill)) {
                        score += 10; // 선호 스킬 매칭: 10점

                        // 레벨 가산점 (필수 스킬보다 작게)
                        score +=
                            getSkillLevelBonus(candidateSkill.getLevel()) / 2;
                        break;
                    }
                }
            }
        }

        // 3. 전체 경력 년수 평가
        if (minimumYearsOfExperience != null) {
            int totalYears = resume.getTotalExperienceYears();
            if (totalYears >= minimumYearsOfExperience) {
                score += 10; // 경력 요구사항 충족: 10점

                // 초과 경력 가산점 (1년당 1점, 최대 10점)
                int extraYears = totalYears - minimumYearsOfExperience;
                score += Math.min(extraYears, 10);
            }
        }

        return Math.max(0, score); // 음수 방지
    }

    /**
     * 스킬 레벨 가산점 계산
     *
     * @param level 스킬 레벨
     * @return 가산점
     */
    private int getSkillLevelBonus(SkillLevel level) {
        switch (level) {
            case EXPERT:
                return 5;
            case ADVANCED:
                return 3;
            case INTERMEDIATE:
                return 1;
            case BEGINNER:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 스킬 기반 검색
     *
     * 지정된 스킬을 가진 이력서를 필터링합니다.
     *
     * @param resumes 이력서 목록
     * @param skillKeywords 검색할 스킬 키워드 목록
     * @param minimumLevel 최소 스킬 레벨 (optional)
     * @return 필터링된 이력서 목록
     */
    public List<Resume> searchBySkills(
        List<Resume> resumes,
        List<String> skillKeywords,
        SkillLevel minimumLevel
    ) {
        if (skillKeywords == null || skillKeywords.isEmpty()) {
            return resumes;
        }

        return resumes
            .stream()
            .filter(resume ->
                hasMatchingSkills(resume, skillKeywords, minimumLevel)
            )
            .collect(Collectors.toList());
    }

    /**
     * 이력서가 지정된 스킬을 가지고 있는지 확인
     *
     * @param resume 이력서
     * @param skillKeywords 스킬 키워드
     * @param minimumLevel 최소 레벨
     * @return 매칭되면 true
     */
    private boolean hasMatchingSkills(
        Resume resume,
        List<String> skillKeywords,
        SkillLevel minimumLevel
    ) {
        if (resume.getSkills() == null || resume.getSkills().isEmpty()) {
            return false;
        }

        for (String keyword : skillKeywords) {
            boolean found = false;
            for (Skill skill : resume.getSkills()) {
                if (skill.matchesName(keyword)) {
                    if (minimumLevel != null) {
                        if (skill.isLevelAtLeast(minimumLevel)) {
                            found = true;
                            break;
                        }
                    } else {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return false; // 하나라도 없으면 false
            }
        }

        return true; // 모든 스킬이 있음
    }

    /**
     * 경력 기반 검색
     *
     * 특정 회사나 직책 경험이 있는 이력서를 필터링합니다.
     *
     * @param resumes 이력서 목록
     * @param companyKeyword 회사명 키워드 (optional)
     * @param positionKeyword 직책 키워드 (optional)
     * @param minimumMonths 최소 근무 개월 수 (optional)
     * @return 필터링된 이력서 목록
     */
    public List<Resume> searchByExperience(
        List<Resume> resumes,
        String companyKeyword,
        String positionKeyword,
        Integer minimumMonths
    ) {
        return resumes
            .stream()
            .filter(resume ->
                hasMatchingExperience(
                    resume,
                    companyKeyword,
                    positionKeyword,
                    minimumMonths
                )
            )
            .collect(Collectors.toList());
    }

    /**
     * 이력서가 지정된 경력 조건을 만족하는지 확인
     */
    private boolean hasMatchingExperience(
        Resume resume,
        String companyKeyword,
        String positionKeyword,
        Integer minimumMonths
    ) {
        if (
            resume.getExperiences() == null || resume.getExperiences().isEmpty()
        ) {
            return false;
        }

        for (Experience exp : resume.getExperiences()) {
            boolean companyMatch =
                companyKeyword == null ||
                exp.isCompany(companyKeyword) ||
                exp
                    .getCompanyName()
                    .toLowerCase()
                    .contains(companyKeyword.toLowerCase());

            boolean positionMatch =
                positionKeyword == null ||
                exp
                    .getPosition()
                    .toLowerCase()
                    .contains(positionKeyword.toLowerCase());

            boolean durationMatch =
                minimumMonths == null ||
                exp.getDurationInMonths() >= minimumMonths;

            if (companyMatch && positionMatch && durationMatch) {
                return true;
            }
        }

        return false;
    }

    /**
     * 복합 검색
     *
     * 스킬, 경력, 학력 등 여러 조건을 조합하여 검색합니다.
     *
     * @param resumes 이력서 목록
     * @param criteria 검색 조건
     * @return 필터링 및 정렬된 이력서 목록 (매칭 점수순)
     */
    public List<ResumeSearchResult> complexSearch(
        List<Resume> resumes,
        SearchCriteria criteria
    ) {
        List<ResumeSearchResult> results = new ArrayList<>();

        for (Resume resume : resumes) {
            // 1. 기본 필터링
            if (!matchesBasicCriteria(resume, criteria)) {
                continue;
            }

            // 2. 매칭 점수 계산
            int score = calculateMatchingScore(
                resume,
                criteria.getRequiredSkills(),
                criteria.getPreferredSkills(),
                criteria.getMinimumYearsOfExperience()
            );

            // 3. 최소 점수 이상만 포함
            if (score >= criteria.getMinimumScore()) {
                results.add(new ResumeSearchResult(resume, score));
            }
        }

        // 4. 점수순 정렬 (높은 순)
        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        return results;
    }

    /**
     * 기본 검색 조건 매칭 확인
     */
    private boolean matchesBasicCriteria(
        Resume resume,
        SearchCriteria criteria
    ) {
        // 파싱 완료된 이력서만
        if (!resume.isParsed()) {
            return false;
        }

        // 최소 경력 년수
        if (criteria.getMinimumYearsOfExperience() != null) {
            if (
                resume.getTotalExperienceYears() <
                criteria.getMinimumYearsOfExperience()
            ) {
                return false;
            }
        }

        // 특정 전공
        if (
            criteria.getMajorKeyword() != null &&
            !criteria.getMajorKeyword().isEmpty()
        ) {
            boolean hasMajor = resume
                .getEducations()
                .stream()
                .anyMatch(edu ->
                    edu.isMajorRelatedTo(criteria.getMajorKeyword())
                );
            if (!hasMajor) {
                return false;
            }
        }

        return true;
    }

    /**
     * 검색 조건 VO
     */
    public static class SearchCriteria {

        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private Integer minimumYearsOfExperience;
        private String majorKeyword;
        private Integer minimumScore;

        public SearchCriteria() {
            this.minimumScore = 0;
        }

        // Getters and Setters
        public List<String> getRequiredSkills() {
            return requiredSkills;
        }

        public void setRequiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
        }

        public List<String> getPreferredSkills() {
            return preferredSkills;
        }

        public void setPreferredSkills(List<String> preferredSkills) {
            this.preferredSkills = preferredSkills;
        }

        public Integer getMinimumYearsOfExperience() {
            return minimumYearsOfExperience;
        }

        public void setMinimumYearsOfExperience(
            Integer minimumYearsOfExperience
        ) {
            this.minimumYearsOfExperience = minimumYearsOfExperience;
        }

        public String getMajorKeyword() {
            return majorKeyword;
        }

        public void setMajorKeyword(String majorKeyword) {
            this.majorKeyword = majorKeyword;
        }

        public Integer getMinimumScore() {
            return minimumScore;
        }

        public void setMinimumScore(Integer minimumScore) {
            this.minimumScore = minimumScore;
        }
    }

    /**
     * 검색 결과 VO
     */
    public static class ResumeSearchResult {

        private final Resume resume;
        private final int score;

        public ResumeSearchResult(Resume resume, int score) {
            this.resume = resume;
            this.score = score;
        }

        public Resume getResume() {
            return resume;
        }

        public int getScore() {
            return score;
        }
    }
}
