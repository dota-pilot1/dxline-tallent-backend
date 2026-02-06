package om.dxline.dxtalent.talent.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.model.BaseEntity;
import om.dxline.dxtalent.talent.domain.event.ResumeDeletedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeParsingCompletedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeParsingFailedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeParsingStartedEvent;
import om.dxline.dxtalent.talent.domain.event.ResumeUploadedEvent;

/**
 * Resume 애그리게이트 루트 (Resume Aggregate Root)
 *
 * 이력서와 관련된 모든 비즈니스 로직을 담당하는 애그리게이트 루트입니다.
 *
 * 애그리게이트 구성:
 * - Resume (Root): 이력서 기본 정보
 * - ResumeFile: 파일 정보 (파일명, 크기, 타입, S3 키)
 * - ResumeProfile: 파싱된 프로필 정보 (이름, 스킬, 경력, 학력, 연락처)
 *
 * 비즈니스 규칙:
 * - 파일 업로드 시 UPLOADED 상태로 시작
 * - 파싱은 UPLOADED 또는 PARSE_FAILED 상태에서만 가능
 * - PARSING 중에는 편집/삭제 불가
 * - PARSED 상태에서만 프로필 편집 가능
 * - 삭제는 소프트 삭제만 허용
 * - ResumeFile과 ResumeProfile은 함께 저장됨 (트랜잭션 경계)
 *
 * 도메인 이벤트:
 * - ResumeUploadedEvent: 업로드 완료
 * - ResumeParsingStartedEvent: 파싱 시작
 * - ResumeParsingCompletedEvent: 파싱 완료
 * - ResumeParsingFailedEvent: 파싱 실패
 * - ResumeDeletedEvent: 삭제됨
 */
public class Resume extends BaseEntity<ResumeId> {

    // ========== 기본 정보 ==========
    private UserId userId; // 이력서 소유자
    private ResumeStatus status; // 현재 상태

    // ========== 파일 정보 (애그리게이트 내부 엔티티) ==========
    private FileName fileName;
    private FileSize fileSize;
    private FileType fileType;
    private String s3Key; // S3 저장 경로

    // ========== 프로필 정보 (애그리게이트 내부 엔티티) ==========
    private CandidateName candidateName;
    private List<Skill> skills;
    private List<Experience> experiences;
    private List<Education> educations;
    private ContactInfo contactInfo;

    // ========== 파싱 정보 ==========
    private LocalDateTime parsedAt;
    private String parseErrorMessage; // 파싱 실패 시 에러 메시지
    private Integer parseRetryCount; // 재파싱 시도 횟수

    // ========== 타임스탬프 ==========
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * JPA를 위한 기본 생성자 (protected)
     */
    protected Resume() {
        super();
        this.skills = new ArrayList<>();
        this.experiences = new ArrayList<>();
        this.educations = new ArrayList<>();
        this.parseRetryCount = 0;
    }

    /**
     * 완전한 생성자 (재구성용 - Repository에서 사용)
     */
    private Resume(
        ResumeId id,
        UserId userId,
        ResumeStatus status,
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        String s3Key,
        CandidateName candidateName,
        List<Skill> skills,
        List<Experience> experiences,
        List<Education> educations,
        ContactInfo contactInfo,
        LocalDateTime parsedAt,
        String parseErrorMessage,
        Integer parseRetryCount,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        super(id);
        this.userId = userId;
        this.status = status;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.s3Key = s3Key;
        this.candidateName = candidateName;
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.experiences = experiences != null ? new ArrayList<>(experiences) : new ArrayList<>();
        this.educations = educations != null ? new ArrayList<>(educations) : new ArrayList<>();
        this.contactInfo = contactInfo;
        this.parsedAt = parsedAt;
        this.parseErrorMessage = parseErrorMessage;
        this.parseRetryCount = parseRetryCount != null ? parseRetryCount : 0;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // ============================================================
    // 정적 팩토리 메서드 (Static Factory Methods)
    // ============================================================

    /**
     * 이력서 업로드
     *
     * @param userId 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param fileType 파일 타입
     * @param s3Key S3 저장 키
     * @return 새로운 Resume 인스턴스
     */
    public static Resume upload(
        UserId userId,
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        String s3Key
    ) {
        Resume resume = new Resume();
        resume.setId(ResumeId.newId());
        resume.userId = userId;
        resume.status = ResumeStatus.getDefault(); // UPLOADED
        resume.fileName = fileName;
        resume.fileSize = fileSize;
        resume.fileType = fileType;
        resume.s3Key = s3Key;
        resume.uploadedAt = LocalDateTime.now();
        resume.updatedAt = LocalDateTime.now();
        resume.parseRetryCount = 0;

        // 도메인 이벤트 발행
        resume.addDomainEvent(
            new ResumeUploadedEvent(
                resume.getId(),
                userId,
                fileName,
                fileType,
                resume.uploadedAt
            )
        );

        return resume;
    }

    /**
     * Repository에서 재구성
     */
    public static Resume reconstitute(
        ResumeId id,
        UserId userId,
        ResumeStatus status,
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        String s3Key,
        CandidateName candidateName,
        List<Skill> skills,
        List<Experience> experiences,
        List<Education> educations,
        ContactInfo contactInfo,
        LocalDateTime parsedAt,
        String parseErrorMessage,
        Integer parseRetryCount,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return new Resume(
            id, userId, status, fileName, fileSize, fileType, s3Key,
            candidateName, skills, experiences, educations, contactInfo,
            parsedAt, parseErrorMessage, parseRetryCount,
            uploadedAt, updatedAt, deletedAt
        );
    }

    // ============================================================
    // 파싱 관련 비즈니스 메서드
    // ============================================================

    /**
     * 파싱 시작
     *
     * @throws IllegalStateException 파싱 불가능한 상태인 경우
     */
    public void startParsing() {
        // 비즈니스 규칙 검증
        if (!status.canParse()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 파싱을 시작할 수 없습니다", status.getKoreanName())
            );
        }

        // 상태 전이
        status.validateTransitionTo(ResumeStatus.PARSING);
        this.status = ResumeStatus.PARSING;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(
            new ResumeParsingStartedEvent(
                this.getId(),
                this.s3Key,
                this.fileType,
                this.updatedAt
            )
        );
    }

    /**
     * 파싱 완료
     *
     * @param candidateName 지원자 이름
     * @param skills 스킬 목록
     * @param experiences 경력 목록
     * @param educations 학력 목록
     * @param contactInfo 연락처 정보
     * @throws IllegalStateException 파싱 중이 아닌 경우
     */
    public void completeParsing(
        CandidateName candidateName,
        List<Skill> skills,
        List<Experience> experiences,
        List<Education> educations,
        ContactInfo contactInfo
    ) {
        // 비즈니스 규칙 검증
        if (!status.isParsing()) {
            throw new IllegalStateException("파싱 중인 이력서만 파싱 완료 처리할 수 있습니다");
        }

        // 프로필 정보 설정
        this.candidateName = candidateName;
        this.skills = new ArrayList<>(skills != null ? skills : Collections.emptyList());
        this.experiences = new ArrayList<>(experiences != null ? experiences : Collections.emptyList());
        this.educations = new ArrayList<>(educations != null ? educations : Collections.emptyList());
        this.contactInfo = contactInfo;

        // 상태 전이
        status.validateTransitionTo(ResumeStatus.PARSED);
        this.status = ResumeStatus.PARSED;
        this.parsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.parseErrorMessage = null; // 에러 메시지 초기화

        // 도메인 이벤트 발행
        addDomainEvent(
            new ResumeParsingCompletedEvent(
                this.getId(),
                candidateName,
                skills != null ? skills.size() : 0,
                experiences != null ? experiences.size() : 0,
                this.parsedAt
            )
        );
    }

    /**
     * 파싱 실패
     *
     * @param errorMessage 실패 사유
     * @throws IllegalStateException 파싱 중이 아닌 경우
     */
    public void failParsing(String errorMessage) {
        // 비즈니스 규칙 검증
        if (!status.isParsing()) {
            throw new IllegalStateException("파싱 중인 이력서만 파싱 실패 처리할 수 있습니다");
        }

        // 상태 전이
        status.validateTransitionTo(ResumeStatus.PARSE_FAILED);
        this.status = ResumeStatus.PARSE_FAILED;
        this.parseErrorMessage = errorMessage;
        this.parseRetryCount++;
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(
            new ResumeParsingFailedEvent(
                this.getId(),
                errorMessage,
                this.parseRetryCount,
                this.updatedAt
            )
        );
    }

    /**
     * 재파싱 요청
     *
     * @throws IllegalStateException 재파싱 불가능한 상태인 경우
     */
    public void requestReparse() {
        // 비즈니스 규칙 검증
        if (!status.canReparse()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 재파싱을 요청할 수 없습니다", status.getKoreanName())
            );
        }

        // 파싱 시작
        startParsing();
    }

    // ============================================================
    // 프로필 관리 비즈니스 메서드
    // ============================================================

    /**
     * 스킬 추가
     *
     * @param skill 추가할 스킬
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void addSkill(Skill skill) {
        validateCanEdit();

        // 중복 체크 (스킬명 기준)
        boolean exists = this.skills.stream()
            .anyMatch(s -> s.matchesName(skill.getName()));

        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 스킬입니다: " + skill.getName());
        }

        this.skills.add(skill);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 스킬 제거
     *
     * @param skillName 제거할 스킬명
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void removeSkill(String skillName) {
        validateCanEdit();

        boolean removed = this.skills.removeIf(skill -> skill.matchesName(skillName));

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 스킬입니다: " + skillName);
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 경력 추가
     *
     * @param experience 추가할 경력
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void addExperience(Experience experience) {
        validateCanEdit();

        this.experiences.add(experience);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 경력 제거
     *
     * @param experience 제거할 경력
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void removeExperience(Experience experience) {
        validateCanEdit();

        boolean removed = this.experiences.remove(experience);

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 경력입니다");
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 학력 추가
     *
     * @param education 추가할 학력
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void addEducation(Education education) {
        validateCanEdit();

        this.educations.add(education);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 학력 제거
     *
     * @param education 제거할 학력
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void removeEducation(Education education) {
        validateCanEdit();

        boolean removed = this.educations.remove(education);

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 학력입니다");
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 연락처 정보 업데이트
     *
     * @param contactInfo 새 연락처 정보
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void updateContactInfo(ContactInfo contactInfo) {
        validateCanEdit();

        this.contactInfo = contactInfo;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 지원자 이름 업데이트
     *
     * @param candidateName 새 이름
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void updateCandidateName(CandidateName candidateName) {
        validateCanEdit();

        this.candidateName = candidateName;
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================================
    // 파일 관리 비즈니스 메서드
    // ============================================================

    /**
     * 파일명 변경
     *
     * @param newFileName 새 파일명
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void renameFile(FileName newFileName) {
        validateCanEdit();

        // 확장자 검증 (기존 확장자와 동일해야 함)
        if (!newFileName.getExtension().equalsIgnoreCase(this.fileName.getExtension())) {
            throw new IllegalArgumentException("파일 확장자는 변경할 수 없습니다");
        }

        this.fileName = newFileName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 파일 교체
     *
     * @param fileName 새 파일명
     * @param fileSize 새 파일 크기
     * @param fileType 새 파일 타입
     * @param newS3Key 새 S3 키
     * @throws IllegalStateException 편집 불가능한 상태인 경우
     */
    public void replaceFile(
        FileName fileName,
        FileSize fileSize,
        FileType fileType,
        String newS3Key
    ) {
        validateCanEdit();

        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.s3Key = newS3Key;
        this.updatedAt = LocalDateTime.now();

        // 파일 교체 시 프로필 초기화
        clearProfile();

        // 상태를 UPLOADED로 변경 (재파싱 필요)
        if (this.status.isParsed()) {
            this.status = ResumeStatus.UPLOADED;
        }
    }

    // ============================================================
    // 상태 관리 비즈니스 메서드
    // ============================================================

    /**
     * 보관
     *
     * @throws IllegalStateException 보관 불가능한 상태인 경우
     */
    public void archive() {
        if (!status.canBeArchived()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 보관할 수 없습니다", status.getKoreanName())
            );
        }

        status.validateTransitionTo(ResumeStatus.ARCHIVED);
        this.status = ResumeStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 보관 해제 (재활성화)
     *
     * @throws IllegalStateException 보관 상태가 아닌 경우
     */
    public void unarchive() {
        if (!status.isArchived()) {
            throw new IllegalStateException("보관 상태인 이력서만 재활성화할 수 있습니다");
        }

        status.validateTransitionTo(ResumeStatus.PARSED);
        this.status = ResumeStatus.PARSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 삭제 (소프트 삭제)
     *
     * @param deletedBy 삭제한 사용자 ID
     * @throws IllegalStateException 삭제 불가능한 상태인 경우
     */
    public void delete(UserId deletedBy) {
        if (!status.canBeDeleted()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 삭제할 수 없습니다", status.getKoreanName())
            );
        }

        // 본인만 삭제 가능
        if (!this.userId.equals(deletedBy)) {
            throw new IllegalStateException("본인의 이력서만 삭제할 수 있습니다");
        }

        status.validateTransitionTo(ResumeStatus.DELETED);
        this.status = ResumeStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 도메인 이벤트 발행
        addDomainEvent(
            new ResumeDeletedEvent(
                this.getId(),
                deletedBy,
                this.s3Key,
                this.deletedAt
            )
        );
    }

    // ============================================================
    // 검증 메서드
    // ============================================================

    /**
     * 편집 가능 여부 검증
     *
     * @throws IllegalStateException 편집 불가능한 경우
     */
    private void validateCanEdit() {
        if (!status.canEdit()) {
            throw new IllegalStateException(
                String.format("현재 상태(%s)에서는 편집할 수 없습니다", status.getKoreanName())
            );
        }
    }

    /**
     * 삭제 가능 여부
     *
     * @return 삭제 가능하면 true
     */
    public boolean canBeDeleted() {
        return status.canBeDeleted();
    }

    /**
     * 편집 가능 여부
     *
     * @return 편집 가능하면 true
     */
    public boolean canBeEdited() {
        return status.canEdit();
    }

    /**
     * 파싱 가능 여부
     *
     * @return 파싱 가능하면 true
     */
    public boolean canBeParsed() {
        return status.canParse();
    }

    // ============================================================
    // 조회 메서드
    // ============================================================

    /**
     * 파싱 완료 여부
     *
     * @return 파싱 완료면 true
     */
    public boolean isParsed() {
        return status.isParsed();
    }

    /**
     * 완전한 프로필 정보를 가지고 있는지 확인
     *
     * @return 이름, 연락처가 있고 스킬이나 경력이 하나 이상 있으면 true
     */
    public boolean hasCompleteProfile() {
        return candidateName != null &&
               contactInfo != null &&
               (!skills.isEmpty() || !experiences.isEmpty());
    }

    /**
     * 총 경력 기간 (년 단위)
     *
     * @return 모든 경력의 합계 (년)
     */
    public int getTotalExperienceYears() {
        return experiences.stream()
            .mapToInt(Experience::getDurationInYears)
            .sum();
    }

    /**
     * 총 경력 기간 (월 단위)
     *
     * @return 모든 경력의 합계 (월)
     */
    public int getTotalExperienceMonths() {
        return experiences.stream()
            .mapToInt(Experience::getDurationInMonths)
            .sum();
    }

    /**
     * 스킬명 목록 반환
     *
     * @return 스킬명 리스트
     */
    public List<String> getSkillNames() {
        return skills.stream()
            .map(Skill::getName)
            .collect(Collectors.toList());
    }

    /**
     * 특정 스킬 보유 여부
     *
     * @param skillName 스킬명
     * @return 보유하면 true
     */
    public boolean hasSkill(String skillName) {
        return skills.stream()
            .anyMatch(skill -> skill.matchesName(skillName));
    }

    /**
     * 특정 레벨 이상의 스킬 보유 여부
     *
     * @param skillName 스킬명
     * @param minimumLevel 최소 레벨
     * @return 보유하고 최소 레벨 이상이면 true
     */
    public boolean hasSkillWithLevel(String skillName, SkillLevel minimumLevel) {
        return skills.stream()
            .anyMatch(skill -> skill.matchesName(skillName) && skill.isLevelAtLeast(minimumLevel));
    }

    /**
     * 프로필 초기화 (내부 메서드)
     */
    private void clearProfile() {
        this.candidateName = null;
        this.skills.clear();
        this.experiences.clear();
        this.educations.clear();
        this.contactInfo = null;
        this.parsedAt = null;
        this.parseErrorMessage = null;
    }

    // ============================================================
    // Getter 메서드
    // ============================================================

    public UserId getUserId() {
        return userId;
    }

    public ResumeStatus getStatus() {
        return status;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getS3Key() {
        return s3Key;
    }

    public CandidateName getCandidateName() {
        return candidateName;
    }

    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    public List<Experience> getExperiences() {
        return Collections.unmodifiableList(experiences);
    }

    public List<Education> getEducations() {
        return Collections.unmodifiableList(educations);
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public LocalDateTime getParsedAt() {
        return parsedAt;
    }

    public String getParseErrorMessage() {
        return parseErrorMessage;
    }

    public Integer getParseRetryCount() {
        return parseRetryCount;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
