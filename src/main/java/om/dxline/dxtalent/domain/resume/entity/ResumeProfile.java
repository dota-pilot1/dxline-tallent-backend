package om.dxline.dxtalent.domain.resume.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long resumeFileId;

    private String name;

    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String skills;

    private Integer totalExperienceYears;

    @Column(columnDefinition = "TEXT")
    private String experienceSummary;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    @Column(columnDefinition = "TEXT")
    private String parsedJson;

    private boolean parsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ResumeProfile(Long userId, Long resumeFileId, String name, String email, String phone,
                         String skills, Integer totalExperienceYears, String experienceSummary,
                         String education, String rawText, String parsedJson, boolean parsed) {
        this.userId = userId;
        this.resumeFileId = resumeFileId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skills = skills;
        this.totalExperienceYears = totalExperienceYears;
        this.experienceSummary = experienceSummary;
        this.education = education;
        this.rawText = rawText;
        this.parsedJson = parsedJson;
        this.parsed = parsed;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateParsedData(String name, String email, String phone, String skills,
                                  Integer totalExperienceYears, String experienceSummary,
                                  String education, String parsedJson) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skills = skills;
        this.totalExperienceYears = totalExperienceYears;
        this.experienceSummary = experienceSummary;
        this.education = education;
        this.parsedJson = parsedJson;
        this.parsed = true;
    }
}
