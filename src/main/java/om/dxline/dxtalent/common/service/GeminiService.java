package om.dxline.dxtalent.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    public String chat(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String fullPrompt =
                systemPrompt + "\n\n사용자 메시지: " + userMessage;

            Map<String, Object> requestBody = Map.of(
                "contents",
                List.of(Map.of("parts", List.of(Map.of("text", fullPrompt)))),
                "generationConfig",
                Map.of("temperature", 0.3, "maxOutputTokens", 2048)
            );

            String url = GEMINI_API_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                requestBody,
                headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (
                response.getStatusCode().is2xxSuccessful() &&
                response.getBody() != null
            ) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    return candidates
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
                }
            }

            log.error("Gemini API chat error: {}", response.getBody());
            return null;
        } catch (Exception e) {
            log.error("Failed to chat with Gemini", e);
            return null;
        }
    }

    public String parseResumeText(String resumeText) {
        String prompt =
            """
                다음 이력서 텍스트를 분석하여 JSON 형식으로 정보를 추출해주세요.
                반드시 아래 JSON 형식만 반환하고, 다른 텍스트는 포함하지 마세요.

                {
                  "name": "이름",
                  "email": "이메일 (없으면 null)",
                  "phone": "전화번호 (없으면 null)",
                  "skills": ["기술1", "기술2", "기술3"],
                  "totalExperienceYears": 총경력년수(숫자, 없으면 0),
                  "experienceSummary": "경력 요약 (최대 500자)",
                  "education": "학력 정보"
                }

                이력서 텍스트:
                """ +
            resumeText;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "contents",
                List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig",
                Map.of("temperature", 0.1, "maxOutputTokens", 2048)
            );

            String url = GEMINI_API_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                requestBody,
                headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (
                response.getStatusCode().is2xxSuccessful() &&
                response.getBody() != null
            ) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String text = candidates
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();

                    // JSON 부분만 추출
                    int startIdx = text.indexOf("{");
                    int endIdx = text.lastIndexOf("}");
                    if (startIdx != -1 && endIdx != -1) {
                        return text.substring(startIdx, endIdx + 1);
                    }
                    return text;
                }
            }

            log.error("Gemini API response error: {}", response.getBody());
            return null;
        } catch (Exception e) {
            log.error("Failed to parse resume with Gemini", e);
            return null;
        }
    }
}
