package om.dxline.dxtalent.talent.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.talent.domain.model.*;
import om.dxline.dxtalent.talent.domain.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * ResumeRepositoryAdapter 통합 테스트
 *
 * 테스트 범위:
 * - Resume 저장 (생성)
 * - Resume 조회 (ID, UserId)
 * - Resume 업데이트
 * - Resume 삭제
 * - 도메인 모델 ↔ JPA Entity 변환
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("ResumeRepositoryAdapter 통합 테스트")
class ResumeRepositoryAdapterTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeJpaRepository jpaRepository;

    private UserId testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of(1L);
        jpaRepository.deleteAll();
    }

    @Test
    @DisplayName("이력서 저장 - 신규 생성")
    void save_WhenNewResume_ShouldPersistAndReturnWithId() {
        // Given: 새 이력서 생성
        Resume resume = Resume.upload(
            testUserId,
            new FileName("홍길동_이력서.pdf"),
            new FileSize(2_097_152L), // 2MB
            FileType.PDF,
            "resumes/user1/test.pdf"
        );

        // When: 저장
        Resume savedResume = resumeRepository.save(resume);

        // Then: ID가 할당되고 저장됨
        assertThat(savedResume.getId()).isNotNull();
        assertThat(savedResume.getId().getValue()).isGreaterThan(0L);
        assertThat(savedResume.getUserId()).isEqualTo(testUserId);
        assertThat(savedResume.getFileName().getValue()).isEqualTo(
            "홍길동_이력서.pdf"
        );
        assertThat(savedResume.getStatus()).isEqualTo(ResumeStatus.UPLOADED);
    }

    @Test
    @DisplayName("이력서 저장 - 업데이트")
    void save_WhenExistingResume_ShouldUpdate() {
        // Given: 이력서 생성 및 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("이력서.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: 파일명 변경 후 저장
        savedResume.renameFile(new FileName("새이력서.pdf"));
        Resume updatedResume = resumeRepository.save(savedResume);

        // Then: 변경사항이 반영됨
        assertThat(updatedResume.getId()).isEqualTo(savedResume.getId());
        assertThat(updatedResume.getFileName().getValue()).isEqualTo(
            "새이력서.pdf"
        );
    }

    @Test
    @DisplayName("ID로 이력서 조회 - 존재하는 경우")
    void findById_WhenResumeExists_ShouldReturnResume() {
        // Given: 이력서 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: ID로 조회
        Optional<Resume> found = resumeRepository.findById(savedResume.getId());

        // Then: 조회됨
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedResume.getId());
        assertThat(found.get().getFileName().getValue()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("ID로 이력서 조회 - 존재하지 않는 경우")
    void findById_WhenResumeNotExists_ShouldReturnEmpty() {
        // Given: 존재하지 않는 ID
        ResumeId nonExistentId = ResumeId.of(999999L);

        // When: ID로 조회
        Optional<Resume> found = resumeRepository.findById(nonExistentId);

        // Then: 빈 Optional 반환
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 이력서 목록 조회")
    void findByUserId_ShouldReturnUserResumes() throws InterruptedException {
        // Given: 같은 사용자의 이력서 2개 저장
        Resume resume1 = Resume.upload(
            testUserId,
            new FileName("이력서1.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/1.pdf"
        );
        Resume resume2 = Resume.upload(
            testUserId,
            new FileName("이력서2.pdf"),
            new FileSize(2_097_152L),
            FileType.PDF,
            "resumes/2.pdf"
        );

        resumeRepository.save(resume1);
        Thread.sleep(10); // 시간 차이를 두기 위해
        resumeRepository.save(resume2);

        // When: 사용자 ID로 조회
        List<Resume> resumes = resumeRepository.findByUserId(testUserId);

        // Then: 2개 조회됨 (최신순)
        assertThat(resumes).hasSize(2);
        assertThat(resumes.get(0).getFileName().getValue()).isEqualTo(
            "이력서2.pdf"
        ); // 최신
        assertThat(resumes.get(1).getFileName().getValue()).isEqualTo(
            "이력서1.pdf"
        );
    }

    @Test
    @DisplayName("사용자 ID와 이력서 ID로 조회")
    void findByIdAndUserId_WhenMatch_ShouldReturnResume() {
        // Given: 이력서 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: ID와 UserId로 조회
        Optional<Resume> found = resumeRepository.findByIdAndUserId(
            savedResume.getId(),
            testUserId
        );

        // Then: 조회됨
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("사용자 ID와 이력서 ID로 조회 - 다른 사용자")
    void findByIdAndUserId_WhenDifferentUser_ShouldReturnEmpty() {
        // Given: 이력서 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: 다른 사용자 ID로 조회
        UserId otherUserId = UserId.of(999L);
        Optional<Resume> found = resumeRepository.findByIdAndUserId(
            savedResume.getId(),
            otherUserId
        );

        // Then: 조회 안 됨
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이력서 개수 조회")
    void countByUserId_ShouldReturnCount() {
        // Given: 이력서 2개 저장
        Resume resume1 = Resume.upload(
            testUserId,
            new FileName("이력서1.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/1.pdf"
        );
        Resume resume2 = Resume.upload(
            testUserId,
            new FileName("이력서2.pdf"),
            new FileSize(2_097_152L),
            FileType.PDF,
            "resumes/2.pdf"
        );

        resumeRepository.save(resume1);
        resumeRepository.save(resume2);

        // When: 개수 조회
        long count = resumeRepository.countByUserId(testUserId);

        // Then: 2개
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("이력서 존재 여부 확인")
    void existsById_WhenResumeExists_ShouldReturnTrue() {
        // Given: 이력서 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: 존재 여부 확인
        boolean exists = resumeRepository.existsById(savedResume.getId());

        // Then: true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("파싱 완료된 이력서 저장 - 복잡한 프로필 정보")
    void save_WhenParsedResumeWithCompleteProfile_ShouldPersistAll() {
        // Given: 업로드된 이력서
        Resume resume = Resume.upload(
            testUserId,
            new FileName("홍길동_이력서.pdf"),
            new FileSize(2_097_152L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: 파싱 시작 및 완료
        savedResume.startParsing();

        CandidateName candidateName = new CandidateName("홍길동");
        List<Skill> skills = List.of(
            new Skill("Java", SkillLevel.ADVANCED, 5),
            new Skill("Spring Boot", SkillLevel.INTERMEDIATE, 3)
        );
        List<Experience> experiences = List.of(
            Experience.of(
                "테크회사",
                "시니어 개발자",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2023, 12, 31),
                "백엔드 개발"
            )
        );
        List<Education> educations = List.of(
            Education.of(
                "한국대학교",
                "학사",
                "컴퓨터공학",
                LocalDate.of(2019, 2, 28),
                3.8
            )
        );
        ContactInfo contactInfo = ContactInfo.of(
            new PhoneNumber("010-1234-5678"),
            new Email("hong@example.com"),
            "서울특별시"
        );

        savedResume.completeParsing(
            candidateName,
            skills,
            experiences,
            educations,
            contactInfo
        );

        // Then: 저장 및 조회 시 모든 정보가 유지됨
        Resume updated = resumeRepository.save(savedResume);
        Resume found = resumeRepository.findById(updated.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(ResumeStatus.PARSED);
        assertThat(found.getCandidateName().getValue()).isEqualTo("홍길동");
        assertThat(found.getSkills()).hasSize(2);
        assertThat(found.getSkills().get(0).getName()).isEqualTo("Java");
        assertThat(found.getExperiences()).hasSize(1);
        assertThat(found.getExperiences().get(0).getCompanyName()).isEqualTo(
            "테크회사"
        );
        assertThat(found.getEducations()).hasSize(1);
        assertThat(found.getEducations().get(0).getSchoolName()).isEqualTo(
            "한국대학교"
        );
        assertThat(found.getContactInfo().getEmail().getValue()).isEqualTo(
            "hong@example.com"
        );
    }

    @Test
    @DisplayName("소프트 삭제된 이력서는 조회되지 않음")
    void findById_WhenResumeDeleted_ShouldReturnEmpty() {
        // Given: 이력서 저장
        Resume resume = Resume.upload(
            testUserId,
            new FileName("test.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/test.pdf"
        );
        Resume savedResume = resumeRepository.save(resume);

        // When: 소프트 삭제
        savedResume.delete(testUserId);
        resumeRepository.save(savedResume);

        // Then: 조회 안 됨
        Optional<Resume> found = resumeRepository.findById(savedResume.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("전체 이력서 조회")
    void findAll_ShouldReturnAllResumes() {
        // Given: 여러 사용자의 이력서 저장
        Resume resume1 = Resume.upload(
            UserId.of(1L),
            new FileName("이력서1.pdf"),
            new FileSize(1_048_576L),
            FileType.PDF,
            "resumes/1.pdf"
        );
        Resume resume2 = Resume.upload(
            UserId.of(2L),
            new FileName("이력서2.pdf"),
            new FileSize(2_097_152L),
            FileType.PDF,
            "resumes/2.pdf"
        );

        resumeRepository.save(resume1);
        resumeRepository.save(resume2);

        // When: 전체 조회
        List<Resume> allResumes = resumeRepository.findAll();

        // Then: 모든 이력서 조회됨
        assertThat(allResumes).hasSizeGreaterThanOrEqualTo(2);
    }
}
