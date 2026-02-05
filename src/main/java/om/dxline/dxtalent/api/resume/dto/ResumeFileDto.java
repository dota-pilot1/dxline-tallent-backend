package om.dxline.dxtalent.api.resume.dto;

import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.resume.entity.ResumeFile;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResumeFileDto {
    private Long id;
    private Long userId;
    private String originalName;
    private String storedName;
    private String s3Url;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private String fileType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResumeFileDto from(ResumeFile entity) {
        return ResumeFileDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .originalName(entity.getOriginalName())
                .storedName(entity.getStoredName())
                .s3Url(entity.getS3Url())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .fileType(entity.getFileType().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
