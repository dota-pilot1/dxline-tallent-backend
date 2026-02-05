package om.dxline.dxtalent.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.domain.resume.entity.ResumeProfile;
import om.dxline.dxtalent.domain.resume.repository.ResumeProfileRepository;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParserService {

    private final S3UploadService s3UploadService;
    private final GeminiService geminiService;
    private final ResumeProfileRepository resumeProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Transactional
    public void parseAndSaveResumeProfile(Long userId, Long resumeFileId, String s3Url) {
        try {
            log.info("Starting resume parsing for fileId: {}", resumeFileId);

            // 1. S3에서 파일 다운로드
            InputStream inputStream = s3UploadService.downloadFile(s3Url);

            // 2. Tika로 텍스트 추출
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
            String rawText = reader.get().stream()
                    .map(doc -> doc.getText())
                    .collect(Collectors.joining("\n"));

            log.info("Extracted text length: {}", rawText.length());

            // 3. 기존 프로필이 있으면 업데이트, 없으면 생성
            ResumeProfile profile = resumeProfileRepository.findByResumeFileId(resumeFileId)
                    .orElse(ResumeProfile.builder()
                            .userId(userId)
                            .resumeFileId(resumeFileId)
                            .rawText(rawText)
                            .parsed(false)
                            .build());

            if (profile.getId() == null) {
                profile = resumeProfileRepository.save(profile);
            }

            // 4. Gemini로 파싱
            String parsedJson = geminiService.parseResumeText(rawText);

            if (parsedJson != null) {
                JsonNode json = objectMapper.readTree(parsedJson);

                String name = json.path("name").asText(null);
                String email = json.path("email").asText(null);
                String phone = json.path("phone").asText(null);

                // skills 배열을 쉼표로 구분된 문자열로 변환
                String skills = null;
                JsonNode skillsNode = json.path("skills");
                if (skillsNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode skill : skillsNode) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(skill.asText());
                    }
                    skills = sb.toString();
                }

                Integer totalExperienceYears = json.path("totalExperienceYears").asInt(0);
                String experienceSummary = json.path("experienceSummary").asText(null);
                String education = json.path("education").asText(null);

                profile.updateParsedData(name, email, phone, skills, totalExperienceYears,
                        experienceSummary, education, parsedJson);

                resumeProfileRepository.save(profile);
                log.info("Successfully parsed resume for fileId: {}", resumeFileId);
            } else {
                log.warn("Failed to parse resume with Gemini for fileId: {}", resumeFileId);
            }

        } catch (Exception e) {
            log.error("Error parsing resume for fileId: {}", resumeFileId, e);
        }
    }
}
