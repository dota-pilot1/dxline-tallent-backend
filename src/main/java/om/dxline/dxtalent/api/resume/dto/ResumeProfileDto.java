package om.dxline.dxtalent.api.resume.dto;

import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.resume.entity.ResumeProfile;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResumeProfileDto {
    private Long id;
    private Long userId;
    private Long resumeFileId;
    private String name;
    private String email;
    private String phone;
    private String skills;
    private Integer totalExperienceYears;
    private String experienceSummary;
    private String education;
    private boolean parsed;
    private LocalDateTime createdAt;

    public static ResumeProfileDto from(ResumeProfile entity) {
        return ResumeProfileDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .resumeFileId(entity.getResumeFileId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .skills(entity.getSkills())
                .totalExperienceYears(entity.getTotalExperienceYears())
                .experienceSummary(entity.getExperienceSummary())
                .education(entity.getEducation())
                .parsed(entity.isParsed())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
