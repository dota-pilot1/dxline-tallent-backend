package om.dxline.dxtalent.talent.application.result;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import om.dxline.dxtalent.talent.domain.model.*;

/**
 * 이력서 결과 DTO
 *
 * Application 레이어에서 이력서 정보를 반환할 때 사용하는 DTO입니다.
 * 도메인 모델을 외부에 직접 노출하지 않고, 필요한 정보만 선택적으로 제공합니다.
 *
 * 책임:
 * - 도메인 모델 → DTO 변환
 * - API 응답 형식 정의
 * - 민감한 정보 필터링
 *
 * 사용 예시:
 * <pre>
 * ResumeResult result = ResumeResult.from(resume);
 * </pre>
 */
public class ResumeResult {

    private final Long id;
    private final Long userId;
    private final String status;
    private final String fileName;
    private final Long fileSize;
    private final String fileType;
    private final String s3Key;

    // 프로필 정보
    private final String candidateName;
    private final List<SkillDto> skills;
    private final List<ExperienceDto> experiences;
    private final List<EducationDto> educations;
    private final ContactInfoDto contactInfo;

    // 메타 정보
    private final LocalDateTime parsedAt;
    private final String parseErrorMessage;
    private final Integer parseRetryCount;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime updatedAt;
    private final int totalExperienceYears;

    private ResumeResult(
        Long id,
        Long userId,
        String status,
        String fileName,
        Long fileSize,
        String fileType,
        String s3Key,
        String candidateName,
        List<SkillDto> skills,
        List<ExperienceDto> experiences,
        List<EducationDto> educations,
        ContactInfoDto contactInfo,
        LocalDateTime parsedAt,
        String parseErrorMessage,
        Integer parseRetryCount,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        int totalExperienceYears
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
        this.contactInfo = contactInfo;
        this.parsedAt = parsedAt;
        this.parseErrorMessage = parseErrorMessage;
        this.parseRetryCount = parseRetryCount;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.totalExperienceYears = totalExperienceYears;
    }

    /**
     * 도메인 모델로부터 ResumeResult 생성
     *
     * @param resume Resume 도메인 모델
     * @return ResumeResult
     */
    public static ResumeResult from(Resume resume) {
        return new ResumeResult(
            resume.getId() != null ? resume.getId().getValue() : null,
            resume.getUserId().getValue(),
            resume.getStatus().name(),
            resume.getFileName().getValue(),
            resume.getFileSize().getBytes(),
            resume.getFileType().name(),
            resume.getS3Key(),
            resume.getCandidateName() != null ? resume.getCandidateName().getValue() : null,
            mapSkills(resume.getSkills()),
            mapExperiences(resume.getExperiences()),
            mapEducations(resume.getEducations()),
            mapContactInfo(resume.getContactInfo()),
            resume.getParsedAt(),
            resume.getParseErrorMessage(),
            resume.getParseRetryCount(),
            resume.getUploadedAt(),
            resume.getUpdatedAt(),
            resume.getTotalExperienceYears()
        );
    }

    /**
     * 매칭 점수를 포함한 ResumeResult 생성
     *
     * @param resume Resume 도메인 모델
     * @param matchingScore 매칭 점수
     * @return ResumeResultWithScore
     */
    public static ResumeResultWithScore withScore(Resume resume, int matchingScore) {
        return new ResumeResultWithScore(from(resume), matchingScore);
    }

    // Mapping methods
    private static List<SkillDto> mapSkills(List<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
            .map(skill -> new SkillDto(
                skill.getName(),
                skill.getLevel().name(),
                skill.getYearsOfExperience()
            ))
            .collect(Collectors.toList());
    }

    private static List<ExperienceDto> mapExperiences(List<Experience> experiences) {
        if (experiences == null) {
            return List.of();
        }
        return experiences.stream()
            .map(exp -> new ExperienceDto(
                exp.getCompanyName(),
                exp.getPosition(),
                exp.getStartDate(),
                exp.getEndDate(),
                exp.getDescription(),
                exp.getDurationInMonths()
            ))
            .collect(Collectors.toList());
    }

    private static List<EducationDto> mapEducations(List<Education> educations) {
        if (educations == null) {
            return List.of();
        }
        return educations.stream()
            .map(edu -> new EducationDto(
                edu.getSchoolName(),
                edu.getDegree(),
                edu.getMajor(),
                edu.getGraduationDate(),
                edu.getGpa()
            ))
            .collect(Collectors.toList());
    }

    private static ContactInfoDto mapContactInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }
        return new ContactInfoDto(
            contactInfo.hasEmail() ? contactInfo.getEmail().getValue() : null,
            contactInfo.hasPhoneNumber() ? contactInfo.getPhoneNumber().getValue() : null,
            contactInfo.getAddress()
        );
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public List<SkillDto> getSkills() {
        return skills;
    }

    public List<ExperienceDto> getExperiences() {
        return experiences;
    }

    public List<EducationDto> getEducations() {
        return educations;
    }

    public ContactInfoDto getContactInfo() {
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

    public int getTotalExperienceYears() {
        return totalExperienceYears;
    }

    // Nested DTOs
    public static class SkillDto {
        private final String name;
        private final String level;
        private final Integer yearsOfExperience;

        public SkillDto(String name, String level, Integer yearsOfExperience) {
            this.name = name;
            this.level = level;
            this.yearsOfExperience = yearsOfExperience;
        }

        public String getName() {
            return name;
        }

        public String getLevel() {
            return level;
        }

        public Integer getYearsOfExperience() {
            return yearsOfExperience;
        }
    }

    public static class ExperienceDto {
        private final String companyName;
        private final String position;
        private final java.time.LocalDate startDate;
        private final java.time.LocalDate endDate;
        private final String description;
        private final int durationInMonths;

        public ExperienceDto(
            String companyName,
            String position,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String description,
            int durationInMonths
        ) {
            this.companyName = companyName;
            this.position = position;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
            this.durationInMonths = durationInMonths;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getPosition() {
            return position;
        }

        public java.time.LocalDate getStartDate() {
            return startDate;
        }

        public java.time.LocalDate getEndDate() {
            return endDate;
        }

        public String getDescription() {
            return description;
        }

        public int getDurationInMonths() {
            return durationInMonths;
        }
    }

    public static class EducationDto {
        private final String schoolName;
        private final String degree;
        private final String major;
        private final java.time.LocalDate graduationDate;
        private final Double gpa;

        public EducationDto(
            String schoolName,
            String degree,
            String major,
            java.time.LocalDate graduationDate,
            Double gpa
        ) {
            this.schoolName = schoolName;
            this.degree = degree;
            this.major = major;
            this.graduationDate = graduationDate;
            this.gpa = gpa;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public String getDegree() {
            return degree;
        }

        public String getMajor() {
            return major;
        }

        public java.time.LocalDate getGraduationDate() {
            return graduationDate;
        }

        public Double getGpa() {
            return gpa;
        }
    }

    public static class ContactInfoDto {
        private final String email;
        private final String phoneNumber;
        private final String address;

        public ContactInfoDto(String email, String phoneNumber, String address) {
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getAddress() {
            return address;
        }
    }

    /**
     * 매칭 점수를 포함한 결과
     */
    public static class ResumeResultWithScore {
        private final ResumeResult resume;
        private final int matchingScore;

        public ResumeResultWithScore(ResumeResult resume, int matchingScore) {
            this.resume = resume;
            this.matchingScore = matchingScore;
        }

        public ResumeResult getResume() {
            return resume;
        }

        public int getMatchingScore() {
            return matchingScore;
        }
    }
}
