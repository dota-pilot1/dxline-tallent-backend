package om.dxline.dxtalent.api.resume.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.api.resume.dto.ResumeFileDto;
import om.dxline.dxtalent.api.resume.dto.ResumeProfileDto;
import om.dxline.dxtalent.common.service.ResumeParserService;
import om.dxline.dxtalent.common.service.S3UploadService;
import om.dxline.dxtalent.domain.resume.entity.ResumeFile;
import om.dxline.dxtalent.domain.resume.repository.ResumeFileRepository;
import om.dxline.dxtalent.domain.resume.repository.ResumeProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeFileService {

    private final ResumeFileRepository resumeFileRepository;
    private final ResumeProfileRepository resumeProfileRepository;
    private final S3UploadService s3UploadService;
    private final ResumeParserService resumeParserService;

    @Transactional(readOnly = true)
    public List<ResumeFileDto> getFilesByUserId(Long userId) {
        return resumeFileRepository
            .findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(ResumeFileDto::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public ResumeFileDto uploadFile(Long userId, MultipartFile file)
        throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String storedName = UUID.randomUUID() + extension;
        String folder = String.format("resume-files/%d", userId);

        String s3Url = s3UploadService.uploadFile(file, folder, storedName);
        ResumeFile.FileType fileType = determineFileType(
            extension,
            file.getContentType()
        );

        ResumeFile entity = ResumeFile.builder()
            .userId(userId)
            .originalName(originalName)
            .storedName(storedName)
            .s3Url(s3Url)
            .filePath(folder + "/" + storedName)
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .fileType(fileType)
            .build();

        ResumeFile savedFile = resumeFileRepository.save(entity);

        // 비동기로 이력서 파싱 시작
        resumeParserService.parseAndSaveResumeProfile(
            userId,
            savedFile.getId(),
            s3Url
        );

        return ResumeFileDto.from(savedFile);
    }

    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        ResumeFile file = resumeFileRepository
            .findByIdAndUserId(fileId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("파일을 찾을 수 없습니다")
            );

        s3UploadService.deleteFile(file.getS3Url());
        resumeProfileRepository.deleteByResumeFileId(fileId);
        resumeFileRepository.deleteByIdAndUserId(fileId, userId);
    }

    @Transactional
    public ResumeFileDto renameFile(Long userId, Long fileId, String newName) {
        ResumeFile file = resumeFileRepository
            .findByIdAndUserId(fileId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("파일을 찾을 수 없습니다")
            );

        file.updateOriginalName(newName);
        return ResumeFileDto.from(file);
    }

    public InputStream downloadFile(Long userId, Long fileId) {
        ResumeFile file = resumeFileRepository
            .findByIdAndUserId(fileId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("파일을 찾을 수 없습니다")
            );

        return s3UploadService.downloadFile(file.getS3Url());
    }

    public ResumeFile getFileEntity(Long userId, Long fileId) {
        return resumeFileRepository
            .findByIdAndUserId(fileId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("파일을 찾을 수 없습니다")
            );
    }

    @Transactional(readOnly = true)
    public List<ResumeProfileDto> searchCandidates(String skill, String task) {
        return resumeProfileRepository
            .searchBySkillAndTask(skill, task)
            .stream()
            .map(ResumeProfileDto::from)
            .collect(Collectors.toList());
    }

    public int reparseAllFiles() {
        List<ResumeFile> allFiles = resumeFileRepository.findAll();
        for (ResumeFile file : allFiles) {
            resumeParserService.parseAndSaveResumeProfile(
                file.getUserId(),
                file.getId(),
                file.getS3Url()
            );
        }
        return allFiles.size();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private ResumeFile.FileType determineFileType(
        String extension,
        String mimeType
    ) {
        if (extension == null) return ResumeFile.FileType.OTHER;

        String ext = extension.toLowerCase();
        if (ext.equals(".pdf")) return ResumeFile.FileType.PDF;
        if (ext.matches("\\.(docx?|rtf)")) return ResumeFile.FileType.DOCX;
        if (ext.matches("\\.(xlsx?|csv)")) return ResumeFile.FileType.XLSX;
        if (ext.equals(".txt")) return ResumeFile.FileType.TXT;
        if (
            mimeType != null && mimeType.startsWith("image/")
        ) return ResumeFile.FileType.IMAGE;

        return ResumeFile.FileType.OTHER;
    }
}
