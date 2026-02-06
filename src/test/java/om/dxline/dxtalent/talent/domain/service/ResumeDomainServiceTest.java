package om.dxline.dxtalent.talent.domain.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.talent.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ResumeDomainService 단위 테스트
 *
 * 테스트 범위:
 * - 이력서 중복 확인
 * - 매칭 점수 계산
 * - 스킬 기반 검색
 * - 경력 기반 검색
 * - 복합 검색
 */
@DisplayName("ResumeDomainService 단위 테스트")
class ResumeDomainServiceTest {

    private ResumeDomainService resumeDomainService;

    @BeforeEach
    void setUp() {
        resumeDomainService = new ResumeDomainService();
    }

    @Nested
    @DisplayName("이력서 중복 확인 테스트")
    class DuplicateCheckTests {

        @Test
        @DisplayName("이메일이 같으면 중복으로 판단")
        void isDuplicateResume_WhenSameEmail_ShouldReturnTrue() {
            // Given: 같은 이메일을 가진 이력서
            ContactInfo contact1 = ContactInfo.withEmailOnly(
                new Email("test@example.com")
            );
            ContactInfo contact2 = ContactInfo.withEmailOnly(
                new Email("test@example.com")
            );

            Resume resume1 = createResumeWithContact(UserId.of(1L), contact1);
            Resume resume2 = createResumeWithContact(UserId.of(2L), contact2);

            // When: 중복 확인
            boolean isDuplicate = resumeDomainService.isDuplicateResume(
                resume2,
                List.of(resume1)
            );

            // Then: 중복으로 판단
            assertThat(isDuplicate).isTrue();
        }

        @Test
        @DisplayName("전화번호가 같으면 중복으로 판단")
        void isDuplicateResume_WhenSamePhoneNumber_ShouldReturnTrue() {
            // Given: 같은 전화번호를 가진 이력서
            ContactInfo contact1 = ContactInfo.withPhoneOnly(
                new PhoneNumber("010-1234-5678")
            );
            ContactInfo contact2 = ContactInfo.withPhoneOnly(
                new PhoneNumber("010-1234-5678")
            );

            Resume resume1 = createResumeWithContact(UserId.of(1L), contact1);
            Resume resume2 = createResumeWithContact(UserId.of(2L), contact2);

            // When: 중복 확인
            boolean isDuplicate = resumeDomainService.isDuplicateResume(
                resume2,
                List.of(resume1)
            );

            // Then: 중복으로 판단
            assertThat(isDuplicate).isTrue();
        }

        @Test
        @DisplayName("이메일과 전화번호가 모두 다르면 중복 아님")
        void isDuplicateResume_WhenDifferentContact_ShouldReturnFalse() {
            // Given: 다른 연락처를 가진 이력서
            ContactInfo contact1 = ContactInfo.of(
                new PhoneNumber("010-1111-1111"),
                new Email("user1@example.com")
            );
            ContactInfo contact2 = ContactInfo.of(
                new PhoneNumber("010-2222-2222"),
                new Email("user2@example.com")
            );

            Resume resume1 = createResumeWithContact(UserId.of(1L), contact1);
            Resume resume2 = createResumeWithContact(UserId.of(2L), contact2);

            // When: 중복 확인
            boolean isDuplicate = resumeDomainService.isDuplicateResume(
                resume2,
                List.of(resume1)
            );

            // Then: 중복 아님
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("연락처 정보가 없으면 중복 판단 불가")
        void isDuplicateResume_WhenNoContactInfo_ShouldReturnFalse() {
            // Given: 연락처 정보가 없는 이력서
            Resume resume1 = Resume.upload(
                UserId.of(1L),
                new FileName("resume1.pdf"),
                new FileSize(1_048_576L),
                FileType.PDF,
                "s3://test/1.pdf"
            );
            Resume resume2 = Resume.upload(
                UserId.of(2L),
                new FileName("resume2.pdf"),
                new FileSize(1_048_576L),
                FileType.PDF,
                "s3://test/2.pdf"
            );

            // When: 중복 확인
            boolean isDuplicate = resumeDomainService.isDuplicateResume(
                resume2,
                List.of(resume1)
            );

            // Then: 중복 판단 불가
            assertThat(isDuplicate).isFalse();
        }
    }

    @Nested
    @DisplayName("매칭 점수 계산 테스트")
    class MatchingScoreTests {

        @Test
        @DisplayName("필수 스킬이 모두 매칭되면 높은 점수")
        void calculateMatchingScore_WhenAllRequiredSkillsMatch_ShouldReturnHighScore() {
            // Given: Java, Spring을 가진 이력서
            Resume resume = createResumeWithSkills(
                List.of(
                    new Skill("Java", SkillLevel.ADVANCED, 5),
                    new Skill("Spring Boot", SkillLevel.INTERMEDIATE, 3)
                )
            );

            List<String> requiredSkills = List.of("Java", "Spring Boot");

            // When: 매칭 점수 계산
            int score = resumeDomainService.calculateMatchingScore(
                resume,
                requiredSkills,
                null,
                null
            );

            // Then: 필수 스킬 2개 * 20점 + 레벨 가산점 = 44점 이상 (ADVANCED 3점 + INTERMEDIATE 1점)
            assertThat(score).isGreaterThanOrEqualTo(44);
        }

        @Test
        @DisplayName("필수 스킬이 없으면 낮은 점수")
        void calculateMatchingScore_WhenNoRequiredSkills_ShouldReturnLowScore() {
            // Given: Python만 가진 이력서
            Resume resume = createResumeWithSkills(
                List.of(new Skill("Python", SkillLevel.INTERMEDIATE, 2))
            );

            List<String> requiredSkills = List.of("Java", "Spring Boot");

            // When: 매칭 점수 계산
            int score = resumeDomainService.calculateMatchingScore(
                resume,
                requiredSkills,
                null,
                null
            );

            // Then: 0점
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("선호 스킬 매칭 시 추가 점수")
        void calculateMatchingScore_WhenPreferredSkillsMatch_ShouldAddBonus() {
            // Given: Java, Docker를 가진 이력서
            Resume resume = createResumeWithSkills(
                List.of(
                    new Skill("Java", SkillLevel.ADVANCED, 5),
                    new Skill("Docker", SkillLevel.INTERMEDIATE, 2)
                )
            );

            List<String> requiredSkills = List.of("Java");
            List<String> preferredSkills = List.of("Docker");

            // When: 매칭 점수 계산
            int score = resumeDomainService.calculateMatchingScore(
                resume,
                requiredSkills,
                preferredSkills,
                null
            );

            // Then: 필수 스킬 20점 + 선호 스킬 10점 + 레벨 가산점
            assertThat(score).isGreaterThanOrEqualTo(33);
        }

        @Test
        @DisplayName("경력 년수가 요구사항 이상이면 가산점")
        void calculateMatchingScore_WhenExperienceMeetsRequirement_ShouldAddBonus() {
            // Given: 5년 경력의 Java 개발자
            Resume resume = createResumeWithSkillsAndExperience(
                List.of(new Skill("Java", SkillLevel.ADVANCED, 5)),
                5
            );

            List<String> requiredSkills = List.of("Java");

            // When: 최소 3년 경력 요구
            int score = resumeDomainService.calculateMatchingScore(
                resume,
                requiredSkills,
                null,
                3
            );

            // Then: 필수 스킬 + 경력 요구사항 충족 + 초과 경력 가산점
            assertThat(score).isGreaterThanOrEqualTo(35);
        }

        @Test
        @DisplayName("EXPERT 레벨 스킬은 더 높은 가산점")
        void calculateMatchingScore_WhenExpertLevel_ShouldGetHigherBonus() {
            // Given: EXPERT 레벨 Java 스킬
            Resume expertResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.EXPERT, 10))
            );
            Resume beginnerResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.BEGINNER, 1))
            );

            List<String> requiredSkills = List.of("Java");

            // When: 매칭 점수 계산
            int expertScore = resumeDomainService.calculateMatchingScore(
                expertResume,
                requiredSkills,
                null,
                null
            );
            int beginnerScore = resumeDomainService.calculateMatchingScore(
                beginnerResume,
                requiredSkills,
                null,
                null
            );

            // Then: EXPERT가 더 높은 점수
            assertThat(expertScore).isGreaterThan(beginnerScore);
        }
    }

    @Nested
    @DisplayName("스킬 기반 검색 테스트")
    class SkillSearchTests {

        @Test
        @DisplayName("지정된 스킬을 가진 이력서만 필터링")
        void searchBySkills_ShouldFilterResumesBySkills() {
            // Given: 다양한 스킬을 가진 이력서들
            Resume javaResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.ADVANCED, 5))
            );
            Resume pythonResume = createResumeWithSkills(
                List.of(new Skill("Python", SkillLevel.INTERMEDIATE, 3))
            );
            Resume bothResume = createResumeWithSkills(
                List.of(
                    new Skill("Java", SkillLevel.ADVANCED, 5),
                    new Skill("Python", SkillLevel.INTERMEDIATE, 3)
                )
            );

            List<Resume> allResumes = List.of(
                javaResume,
                pythonResume,
                bothResume
            );

            // When: Java 스킬로 검색
            List<Resume> results = resumeDomainService.searchBySkills(
                allResumes,
                List.of("Java"),
                null
            );

            // Then: Java를 가진 이력서만 조회
            assertThat(results).hasSize(2);
            assertThat(results).contains(javaResume, bothResume);
        }

        @Test
        @DisplayName("최소 레벨 조건을 만족하는 이력서만 필터링")
        void searchBySkills_WhenMinimumLevelSpecified_ShouldFilterByLevel() {
            // Given: 다양한 레벨의 이력서들
            Resume expertResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.EXPERT, 10))
            );
            Resume advancedResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.ADVANCED, 5))
            );
            Resume beginnerResume = createResumeWithSkills(
                List.of(new Skill("Java", SkillLevel.BEGINNER, 1))
            );

            List<Resume> allResumes = List.of(
                expertResume,
                advancedResume,
                beginnerResume
            );

            // When: ADVANCED 레벨 이상으로 검색
            List<Resume> results = resumeDomainService.searchBySkills(
                allResumes,
                List.of("Java"),
                SkillLevel.ADVANCED
            );

            // Then: ADVANCED 이상만 조회
            assertThat(results).hasSize(2);
            assertThat(results).contains(expertResume, advancedResume);
        }
    }

    @Nested
    @DisplayName("경력 기반 검색 테스트")
    class ExperienceSearchTests {

        @Test
        @DisplayName("회사명 키워드로 검색")
        void searchByExperience_WhenCompanyKeyword_ShouldFilterByCompany() {
            // Given: 다양한 회사 경력을 가진 이력서들
            Resume samsungResume = createResumeWithExperience(
                "삼성전자",
                "개발자",
                24
            );
            Resume lgResume = createResumeWithExperience(
                "LG전자",
                "개발자",
                18
            );

            List<Resume> allResumes = List.of(samsungResume, lgResume);

            // When: "삼성" 키워드로 검색
            List<Resume> results = resumeDomainService.searchByExperience(
                allResumes,
                "삼성",
                null,
                null
            );

            // Then: 삼성 이력서만 조회
            assertThat(results).hasSize(1);
            assertThat(results).contains(samsungResume);
        }

        @Test
        @DisplayName("최소 근무 기간 조건으로 필터링")
        void searchByExperience_WhenMinimumMonths_ShouldFilterByDuration() {
            // Given: 다양한 근무 기간을 가진 이력서들
            Resume longTermResume = createResumeWithExperience(
                "테크회사",
                "개발자",
                36
            );
            Resume shortTermResume = createResumeWithExperience(
                "스타트업",
                "개발자",
                12
            );

            List<Resume> allResumes = List.of(longTermResume, shortTermResume);

            // When: 최소 24개월 이상으로 검색
            List<Resume> results = resumeDomainService.searchByExperience(
                allResumes,
                null,
                null,
                24
            );

            // Then: 24개월 이상만 조회
            assertThat(results).hasSize(1);
            assertThat(results).contains(longTermResume);
        }
    }

    @Nested
    @DisplayName("복합 검색 테스트")
    class ComplexSearchTests {

        @Test
        @DisplayName("복합 조건으로 검색하고 점수순 정렬")
        void complexSearch_ShouldFilterAndSortByScore() {
            // Given: 다양한 조건의 이력서들
            Resume highMatch = createParsedResume(
                List.of(
                    new Skill("Java", SkillLevel.EXPERT, 10),
                    new Skill("Spring Boot", SkillLevel.ADVANCED, 7)
                ),
                10
            );

            Resume mediumMatch = createParsedResume(
                List.of(new Skill("Java", SkillLevel.INTERMEDIATE, 5)),
                5
            );

            Resume lowMatch = createParsedResume(
                List.of(new Skill("Python", SkillLevel.BEGINNER, 1)),
                1
            );

            List<Resume> allResumes = List.of(highMatch, mediumMatch, lowMatch);

            // When: 복합 검색
            ResumeDomainService.SearchCriteria criteria =
                new ResumeDomainService.SearchCriteria();
            criteria.setRequiredSkills(List.of("Java"));
            criteria.setPreferredSkills(List.of("Spring Boot"));
            criteria.setMinimumYearsOfExperience(3);
            criteria.setMinimumScore(20);

            List<ResumeDomainService.ResumeSearchResult> results =
                resumeDomainService.complexSearch(allResumes, criteria);

            // Then: 점수순으로 정렬되어 반환
            assertThat(results).hasSize(2); // lowMatch는 최소 점수 미달
            assertThat(results.get(0).getResume()).isEqualTo(highMatch);
            assertThat(results.get(1).getResume()).isEqualTo(mediumMatch);
            assertThat(results.get(0).getScore()).isGreaterThan(
                results.get(1).getScore()
            );
        }

        @Test
        @DisplayName("파싱되지 않은 이력서는 제외")
        void complexSearch_ShouldExcludeUnparsedResumes() {
            // Given: 파싱된 이력서와 파싱 안 된 이력서
            Resume parsedResume = createParsedResume(
                List.of(new Skill("Java", SkillLevel.ADVANCED, 5)),
                5
            );

            Resume unparsedResume = Resume.upload(
                UserId.of(2L),
                new FileName("resume2.pdf"),
                new FileSize(1_048_576L),
                FileType.PDF,
                "s3://test/2.pdf"
            );

            List<Resume> allResumes = List.of(parsedResume, unparsedResume);

            // When: 복합 검색
            ResumeDomainService.SearchCriteria criteria =
                new ResumeDomainService.SearchCriteria();
            criteria.setRequiredSkills(List.of("Java"));

            List<ResumeDomainService.ResumeSearchResult> results =
                resumeDomainService.complexSearch(allResumes, criteria);

            // Then: 파싱된 이력서만 반환
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getResume()).isEqualTo(parsedResume);
        }
    }

    // ========== Helper Methods ==========

    private Resume createResumeWithContact(UserId userId, ContactInfo contact) {
        Resume resume = Resume.upload(
            userId,
            new FileName("test_" + userId.getValue() + ".pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "s3://test/file_" + userId.getValue() + ".pdf"
        );

        // 파싱 완료 처리
        resume.startParsing();
        resume.completeParsing(
            new CandidateName("테스트"),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            contact
        );

        return resume;
    }

    private Resume createResumeWithSkills(List<Skill> skills) {
        Resume resume = Resume.upload(
            UserId.of(1L),
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "s3://test/file.pdf"
        );

        resume.startParsing();
        resume.completeParsing(
            new CandidateName("개발자"),
            new ArrayList<>(skills),
            new ArrayList<>(),
            new ArrayList<>(),
            null
        );

        return resume;
    }

    private Resume createResumeWithSkillsAndExperience(
        List<Skill> skills,
        int totalYears
    ) {
        Resume resume = Resume.upload(
            UserId.of(1L),
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "s3://test/file.pdf"
        );

        // 경력 생성 (totalYears를 12개월씩 나눔)
        List<Experience> experiences = List.of(
            Experience.of(
                "테크회사",
                "개발자",
                LocalDate.now().minusYears(totalYears),
                LocalDate.now()
            )
        );

        resume.startParsing();
        resume.completeParsing(
            new CandidateName("개발자"),
            new ArrayList<>(skills),
            new ArrayList<>(experiences),
            new ArrayList<>(),
            null
        );

        return resume;
    }

    private Resume createResumeWithExperience(
        String companyName,
        String position,
        int durationMonths
    ) {
        Resume resume = Resume.upload(
            UserId.of(1L),
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "s3://test/file.pdf"
        );

        Experience experience = Experience.of(
            companyName,
            position,
            LocalDate.now().minusMonths(durationMonths),
            LocalDate.now()
        );

        resume.startParsing();
        resume.completeParsing(
            new CandidateName("개발자"),
            new ArrayList<>(),
            new ArrayList<>(List.of(experience)),
            new ArrayList<>(),
            null
        );

        return resume;
    }

    private Resume createParsedResume(
        List<Skill> skills,
        int totalExperienceYears
    ) {
        Resume resume = Resume.upload(
            UserId.of(1L),
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "s3://test/file.pdf"
        );

        List<Experience> experiences = List.of(
            Experience.of(
                "테크회사",
                "개발자",
                LocalDate.now().minusYears(totalExperienceYears),
                LocalDate.now()
            )
        );

        resume.startParsing();
        resume.completeParsing(
            new CandidateName("개발자"),
            new ArrayList<>(skills),
            new ArrayList<>(experiences),
            new ArrayList<>(),
            null
        );

        return resume;
    }
}
