package om.dxline.dxtalent.api.resume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.resume.dto.CandidateSearchRequest;
import om.dxline.dxtalent.api.resume.dto.ResumeFileDto;
import om.dxline.dxtalent.api.resume.dto.ResumeProfileDto;
import om.dxline.dxtalent.api.resume.service.ResumeFileService;
import om.dxline.dxtalent.domain.resume.entity.ResumeFile;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Resume Files", description = "이력서 파일 관리 API")
@RestController
@RequestMapping("/api/resume/files")
@RequiredArgsConstructor
public class ResumeFileController {

    private final ResumeFileService resumeFileService;

    @Operation(summary = "내 이력서 파일 목록 조회")
    @GetMapping
    public ResponseEntity<List<ResumeFileDto>> getMyFiles(
        @AuthenticationPrincipal User user
    ) {
        List<ResumeFileDto> files = resumeFileService.getFilesByUserId(
            user.getId()
        );
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "이력서 파일 업로드")
    @PostMapping
    public ResponseEntity<ResumeFileDto> uploadFile(
        @AuthenticationPrincipal User user,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        ResumeFileDto result = resumeFileService.uploadFile(user.getId(), file);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "이력서 파일 삭제")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
        @AuthenticationPrincipal User user,
        @PathVariable Long fileId
    ) {
        resumeFileService.deleteFile(user.getId(), fileId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이력서 파일 이름 변경")
    @PatchMapping("/{fileId}/rename")
    public ResponseEntity<ResumeFileDto> renameFile(
        @AuthenticationPrincipal User user,
        @PathVariable Long fileId,
        @RequestBody Map<String, String> request
    ) {
        String newName = request.get("newName");
        ResumeFileDto result = resumeFileService.renameFile(
            user.getId(),
            fileId,
            newName
        );
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "이력서 파일 다운로드")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
        @AuthenticationPrincipal User user,
        @PathVariable Long fileId
    ) {
        ResumeFile file = resumeFileService.getFileEntity(user.getId(), fileId);
        InputStream inputStream = resumeFileService.downloadFile(
            user.getId(),
            fileId
        );

        String encodedFileName = URLEncoder.encode(
            file.getOriginalName(),
            StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName
            )
            .contentType(MediaType.parseMediaType(file.getMimeType()))
            .contentLength(file.getFileSize())
            .body(new InputStreamResource(inputStream));
    }

    @Operation(summary = "지원자 검색 (보유 기술, 주요 업무)")
    @GetMapping("/search")
    public ResponseEntity<List<ResumeProfileDto>> searchCandidates(
        @RequestParam(required = false) String skill,
        @RequestParam(required = false) String task
    ) {
        List<ResumeProfileDto> candidates = resumeFileService.searchCandidates(
            skill,
            task
        );
        return ResponseEntity.ok(candidates);
    }

    @Operation(summary = "모든 이력서 다시 파싱")
    @PostMapping("/reparse-all")
    public ResponseEntity<String> reparseAll(
        @AuthenticationPrincipal User user
    ) {
        int count = resumeFileService.reparseAllFiles();
        return ResponseEntity.ok(count + "개 파일 파싱 시작됨");
    }
}
