package om.dxline.dxtalent.talent.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resume JPA Entity (인프라 레이어)
 *
 * Resume 도메인 모델을 데이터베이스에 영속화하기 위한 JPA 엔티티입니다.
 * 기존의 ResumeFile과 ResumeProfile을 하나의 테이블로 통합했습니다.
 *
 * 테이블 구조:
 * - 파일 정보: file_name, file_size, file_type, s3_key
 * - 프로필 정보: candidate_name, skills, experiences, educations, contact_info
 * - 상태 정보: status, parsed_at, parse_error_message, parse_retry_count
 * - 타임스탬프: uploaded_at, updated_at, deleted_at
 *
 * 주의사항:
 * - 이 클래스는 인프라 레이어에만 존재하며 도메인 레이어에 노출되지 않습니다.
 * - 복잡한 값 객체(Skill, Experience, Education)는 JSON으로 직렬화하여 저장합니다.
 * - 도메인 모델과의 변환은 ResumeMapper가 담당합니다.
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 기본 정보 ==========
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ResumeStatusJpa status;

    // ========== 파일 정보 ==========
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    // ========== 프로필 정보 (파싱 결과) ==========
    @Column(name = "candidate_name", length = 100)
    private String candidateName;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // JSON 직렬화: List<Skill>

    @Column(name = "experiences", columnDefinition = "TEXT")
    private String experiences; // JSON 직렬화: List<Experience>

    @Column(name = "educations", columnDefinition = "TEXT")
    private String educations; // JSON 직렬화: List<Education>

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_address", length = 500)
    private String contactAddress;

    @Column(name = "contact_linkedin", length = 200)
    private String contactLinkedin;

    @Column(name = "contact_github", length = 200)
    private String contactGithub;

    // ========== 파싱 정보 ==========
    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @Column(name = "parse_error_message", columnDefinition = "TEXT")
    private String parseErrorMessage;

    @Column(name = "parse_retry_count")
    private Integer parseRetryCount;

    // ========== 타임스탬프 ==========
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * JPA Status Enum
     */
    public enum ResumeStatusJpa {
        UPLOADED,       // 업로드 완료
        PARSING,        // 파싱 중
        PARSED,         // 파싱 완료
        PARSE_FAILED,   // 파싱 실패
        ARCHIVED,       // 보관됨
        DELETED         // 삭제됨
    }

    /**
     * 생성 시 자동으로 타임스탬프 설정
     */
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (parseRetryCount == null) {
            parseRetryCount = 0;
        }
    }

    /**
     * 업데이트 시 자동으로 updated_at 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 전체 생성자 (Builder 패턴 대신)
     */
    public ResumeJpaEntity(
        Long id,
        Long userId,
        ResumeStatusJpa status,
        String fileName,
        Long fileSize,
        String fileType,
        String s3Key,
        String candidateName,
        String skills,
        String experiences,
        String educations,
        String contactEmail,
        String contactPhone,
        String contactAddress,
        String contactLinkedin,
        String contactGithub,
        LocalDateTime parsedAt,
        String parseErrorMessage,
        Integer parseRetryCount,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.s3Key = s3Key;
        this.candidateName = candidateName;
        this.skills = skills;
        this.experiences = experiences;
        this.educations = educations;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.contactAddress = contactAddress;
        this.contactLinkedin = contactLinkedin;
        this.contactGithub = contactGithub;
        this.parsedAt = parsedAt;
        this.parseErrorMessage = parseErrorMessage;
        this.parseRetryCount = parseRetryCount;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * 소프트 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 파싱 완료 여부 확인
     */
    public boolean isParsed() {
        return status == ResumeStatusJpa.PARSED;
    }
}
