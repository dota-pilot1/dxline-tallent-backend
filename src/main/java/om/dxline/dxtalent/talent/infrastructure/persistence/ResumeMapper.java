package om.dxline.dxtalent.talent.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.talent.domain.model.*;
import org.springframework.stereotype.Component;

/**
 * Resume 도메인 모델 ↔ ResumeJpaEntity 변환 매퍼
 *
 * 도메인 모델과 JPA 엔티티 간의 변환을 담당합니다.
 * 복잡한 값 객체(Skill, Experience, Education)는 JSON으로 직렬화하여 저장합니다.
 *
 * 설계 원칙:
 * - 도메인 레이어는 JPA Entity를 알지 못함
 * - 인프라 레이어만 매핑 로직을 알고 있음
 * - JSON 직렬화 실패 시 빈 리스트로 처리 (데이터 손실 방지)
 */
@Component
public class ResumeMapper {

    private final ObjectMapper objectMapper;

    public ResumeMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 도메인 모델 → JPA Entity 변환
     *
     * @param resume Resume 도메인 모델
     * @return ResumeJpaEntity
     */
    public ResumeJpaEntity toEntity(Resume resume) {
        if (resume == null) {
            return null;
        }

        Long id = resume.getId() != null ? resume.getId().getValue() : null;

        Long userId = resume.getUserId().getValue();
        ResumeJpaEntity.ResumeStatusJpa status = toJpaStatus(
            resume.getStatus()
        );

        // 파일 정보
        String fileName = resume.getFileName().getValue();
        Long fileSize = resume.getFileSize().getBytes();
        String fileType = resume.getFileType().name();
        String s3Key = resume.getS3Key();

        // 프로필 정보
        String candidateName =
            resume.getCandidateName() != null
                ? resume.getCandidateName().getValue()
                : null;

        // 복잡한 값 객체를 JSON으로 직렬화
        String skillsJson = serializeSkills(resume.getSkills());
        String experiencesJson = serializeExperiences(resume.getExperiences());
        String educationsJson = serializeEducations(resume.getEducations());

        // ContactInfo 분해
        String contactEmail = null;
        String contactPhone = null;
        String contactAddress = null;

        if (resume.getContactInfo() != null) {
            ContactInfo contactInfo = resume.getContactInfo();
            contactEmail = contactInfo.hasEmail()
                ? contactInfo.getEmail().getValue()
                : null;
            contactPhone = contactInfo.hasPhoneNumber()
                ? contactInfo.getPhoneNumber().getValue()
                : null;
            contactAddress = contactInfo.hasAddress()
                ? contactInfo.getAddress()
                : null;
        }

        return new ResumeJpaEntity(
            id,
            userId,
            status,
            fileName,
            fileSize,
            fileType,
            s3Key,
            candidateName,
            skillsJson,
            experiencesJson,
            educationsJson,
            contactEmail,
            contactPhone,
            contactAddress,
            null, // linkedin
            null, // github
            resume.getParsedAt(),
            resume.getParseErrorMessage(),
            resume.getParseRetryCount(),
            resume.getUploadedAt(),
            resume.getUpdatedAt(),
            resume.getDeletedAt()
        );
    }

    /**
     * JPA Entity → 도메인 모델 변환 (재구성)
     *
     * @param entity ResumeJpaEntity
     * @return Resume 도메인 모델
     */
    public Resume toDomain(ResumeJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        ResumeId id = ResumeId.of(entity.getId());
        UserId userId = UserId.of(entity.getUserId());
        ResumeStatus status = toDomainStatus(entity.getStatus());

        // 파일 정보
        FileName fileName = new FileName(entity.getFileName());
        FileSize fileSize = new FileSize(entity.getFileSize());
        FileType fileType = FileType.valueOf(entity.getFileType());
        String s3Key = entity.getS3Key();

        // 프로필 정보
        CandidateName candidateName =
            entity.getCandidateName() != null
                ? new CandidateName(entity.getCandidateName())
                : null;

        // JSON에서 복잡한 값 객체 역직렬화
        List<Skill> skills = deserializeSkills(entity.getSkills());
        List<Experience> experiences = deserializeExperiences(
            entity.getExperiences()
        );
        List<Education> educations = deserializeEducations(
            entity.getEducations()
        );

        // ContactInfo 재구성
        ContactInfo contactInfo = reconstructContactInfo(
            entity.getContactEmail(),
            entity.getContactPhone(),
            entity.getContactAddress()
        );

        return Resume.reconstitute(
            id,
            userId,
            status,
            fileName,
            fileSize,
            fileType,
            s3Key,
            candidateName,
            skills,
            experiences,
            educations,
            contactInfo,
            entity.getParsedAt(),
            entity.getParseErrorMessage(),
            entity.getParseRetryCount(),
            entity.getUploadedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()
        );
    }

    /**
     * 도메인 Status → JPA Status 변환
     */
    private ResumeJpaEntity.ResumeStatusJpa toJpaStatus(ResumeStatus status) {
        return ResumeJpaEntity.ResumeStatusJpa.valueOf(status.name());
    }

    /**
     * JPA Status → 도메인 Status 변환
     */
    private ResumeStatus toDomainStatus(
        ResumeJpaEntity.ResumeStatusJpa status
    ) {
        return ResumeStatus.valueOf(status.name());
    }

    // ========== JSON 직렬화 메서드 ==========

    /**
     * Skill 리스트를 JSON으로 직렬화
     */
    private String serializeSkills(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }

        try {
            List<SkillDto> dtos = new ArrayList<>();
            for (Skill skill : skills) {
                dtos.add(
                    new SkillDto(
                        skill.getName(),
                        skill.getLevel().name(),
                        skill.getYearsOfExperience()
                    )
                );
            }
            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize skills", e);
        }
    }

    /**
     * Experience 리스트를 JSON으로 직렬화
     */
    private String serializeExperiences(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return null;
        }

        try {
            List<ExperienceDto> dtos = new ArrayList<>();
            for (Experience exp : experiences) {
                dtos.add(
                    new ExperienceDto(
                        exp.getCompanyName(),
                        exp.getPosition(),
                        exp.getStartDate(),
                        exp.getEndDate(),
                        exp.getDescription()
                    )
                );
            }
            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize experiences", e);
        }
    }

    /**
     * Education 리스트를 JSON으로 직렬화
     */
    private String serializeEducations(List<Education> educations) {
        if (educations == null || educations.isEmpty()) {
            return null;
        }

        try {
            List<EducationDto> dtos = new ArrayList<>();
            for (Education edu : educations) {
                dtos.add(
                    new EducationDto(
                        edu.getSchoolName(),
                        edu.getDegree(),
                        edu.getMajor(),
                        edu.getGraduationDate(),
                        edu.getGpa()
                    )
                );
            }
            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize educations", e);
        }
    }

    // ========== JSON 역직렬화 메서드 ==========

    /**
     * JSON을 Skill 리스트로 역직렬화
     */
    private List<Skill> deserializeSkills(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<SkillDto> dtos = objectMapper.readValue(
                json,
                new TypeReference<List<SkillDto>>() {}
            );

            List<Skill> skills = new ArrayList<>();
            for (SkillDto dto : dtos) {
                skills.add(
                    new Skill(
                        dto.name,
                        SkillLevel.valueOf(dto.level),
                        dto.yearsOfExperience
                    )
                );
            }
            return skills;
        } catch (JsonProcessingException e) {
            // 역직렬화 실패 시 빈 리스트 반환 (데이터 손실 방지)
            return Collections.emptyList();
        }
    }

    /**
     * JSON을 Experience 리스트로 역직렬화
     */
    private List<Experience> deserializeExperiences(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<ExperienceDto> dtos = objectMapper.readValue(
                json,
                new TypeReference<List<ExperienceDto>>() {}
            );

            List<Experience> experiences = new ArrayList<>();
            for (ExperienceDto dto : dtos) {
                experiences.add(
                    new Experience(
                        dto.companyName,
                        dto.position,
                        dto.startDate,
                        dto.endDate,
                        dto.description
                    )
                );
            }
            return experiences;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * JSON을 Education 리스트로 역직렬화
     */
    private List<Education> deserializeEducations(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<EducationDto> dtos = objectMapper.readValue(
                json,
                new TypeReference<List<EducationDto>>() {}
            );

            List<Education> educations = new ArrayList<>();
            for (EducationDto dto : dtos) {
                educations.add(
                    new Education(
                        dto.schoolName,
                        dto.degree,
                        dto.major,
                        dto.graduationDate,
                        dto.gpa
                    )
                );
            }
            return educations;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * ContactInfo 재구성
     */
    private ContactInfo reconstructContactInfo(
        String email,
        String phone,
        String address
    ) {
        Email emailObj = null;
        PhoneNumber phoneObj = null;

        try {
            if (email != null && !email.trim().isEmpty()) {
                emailObj = new Email(email);
            }
        } catch (Exception e) {
            // 이메일 검증 실패 시 null
        }

        try {
            if (phone != null && !phone.trim().isEmpty()) {
                phoneObj = new PhoneNumber(phone);
            }
        } catch (Exception e) {
            // 전화번호 검증 실패 시 null
        }

        // 둘 다 null이면 null 반환
        if (emailObj == null && phoneObj == null) {
            return null;
        }

        return new ContactInfo(phoneObj, emailObj, address);
    }

    // ========== DTO 클래스 (JSON 직렬화용) ==========

    /**
     * Skill DTO (JSON 직렬화용)
     */
    private static class SkillDto {

        public String name;
        public String level;
        public Integer yearsOfExperience;

        public SkillDto() {}

        public SkillDto(String name, String level, Integer yearsOfExperience) {
            this.name = name;
            this.level = level;
            this.yearsOfExperience = yearsOfExperience;
        }
    }

    /**
     * Experience DTO (JSON 직렬화용)
     */
    private static class ExperienceDto {

        public String companyName;
        public String position;
        public LocalDate startDate;
        public LocalDate endDate;
        public String description;

        public ExperienceDto() {}

        public ExperienceDto(
            String companyName,
            String position,
            LocalDate startDate,
            LocalDate endDate,
            String description
        ) {
            this.companyName = companyName;
            this.position = position;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
        }
    }

    /**
     * Education DTO (JSON 직렬화용)
     */
    private static class EducationDto {

        public String schoolName;
        public String degree;
        public String major;
        public LocalDate graduationDate;
        public Double gpa;

        public EducationDto() {}

        public EducationDto(
            String schoolName,
            String degree,
            String major,
            LocalDate graduationDate,
            Double gpa
        ) {
            this.schoolName = schoolName;
            this.degree = degree;
            this.major = major;
            this.graduationDate = graduationDate;
            this.gpa = gpa;
        }
    }
}
