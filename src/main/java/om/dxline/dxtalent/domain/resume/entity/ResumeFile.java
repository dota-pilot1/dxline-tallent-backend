package om.dxline.dxtalent.domain.resume.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String s3Url;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ResumeFile(Long userId, String originalName, String storedName, String s3Url,
                      String filePath, Long fileSize, String mimeType, FileType fileType) {
        this.userId = userId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.s3Url = s3Url;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileType = fileType;
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

    public void updateOriginalName(String newName) {
        this.originalName = newName;
    }

    public enum FileType {
        PDF, DOCX, XLSX, TXT, IMAGE, OTHER
    }
}
