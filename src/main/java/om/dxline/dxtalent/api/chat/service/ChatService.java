package om.dxline.dxtalent.api.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.api.chat.dto.ChatResponse;
import om.dxline.dxtalent.api.resume.dto.ResumeProfileDto;
import om.dxline.dxtalent.common.service.GeminiService;
import om.dxline.dxtalent.domain.resume.entity.ResumeProfile;
import om.dxline.dxtalent.domain.resume.repository.ResumeProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiService geminiService;
    private final ResumeProfileRepository resumeProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
        당신은 이력서 검색 도우미입니다. 사용자가 자연어로 지원자를 검색하면, 검색 조건을 JSON으로 추출해주세요.

        반드시 아래 JSON 형식만 반환하세요:
        {
          "skill": "검색할 기술 (없으면 null)",
          "task": "검색할 업무/경력 키워드 (없으면 null)",
          "minExperience": 최소 경력 년수 (없으면 null),
          "reply": "사용자에게 보여줄 친절한 응답 메시지"
        }

        예시:
        - "React 개발자 찾아줘" -> {"skill": "React", "task": null, "minExperience": null, "reply": "React 기술을 보유한 지원자를 검색합니다."}
        - "5년 이상 백엔드 개발자" -> {"skill": "Java,Spring", "task": "백엔드", "minExperience": 5, "reply": "5년 이상 경력의 백엔드 개발자를 검색합니다."}
        - "디자인 경력자" -> {"skill": "Figma,Photoshop", "task": "디자인", "minExperience": null, "reply": "디자인 관련 경력이 있는 지원자를 검색합니다."}

        검색과 관련 없는 질문이면:
        {"skill": null, "task": null, "minExperience": null, "reply": "지원자 검색에 대해 질문해주세요. 예: 'React 개발자 찾아줘', 'Java 5년차 이상 백엔드 개발자'"}
        """;

    public ChatResponse chat(String userMessage) {
        try {
            String response = geminiService.chat(SYSTEM_PROMPT, userMessage);

            if (response == null) {
                return ChatResponse.builder()
                    .message("죄송합니다. 요청을 처리하지 못했습니다.")
                    .candidates(List.of())
                    .build();
            }

            // JSON 추출
            int startIdx = response.indexOf("{");
            int endIdx = response.lastIndexOf("}");
            if (startIdx == -1 || endIdx == -1) {
                return ChatResponse.builder()
                    .message(response)
                    .candidates(List.of())
                    .build();
            }

            String jsonStr = response.substring(startIdx, endIdx + 1);
            JsonNode json = objectMapper.readTree(jsonStr);

            String skill = json.path("skill").isNull() ? null : json.path("skill").asText();
            String task = json.path("task").isNull() ? null : json.path("task").asText();
            Integer minExperience = json.path("minExperience").isNull() ? null : json.path("minExperience").asInt();
            String reply = json.path("reply").asText("검색 결과입니다.");

            // 검색 조건이 없으면 빈 결과 반환
            if (skill == null && task == null && minExperience == null) {
                return ChatResponse.builder()
                    .message(reply)
                    .candidates(List.of())
                    .build();
            }

            // DB 검색
            List<ResumeProfile> profiles = resumeProfileRepository.searchBySkillAndTask(skill, task);

            // 경력 필터링
            if (minExperience != null) {
                final int minExp = minExperience;
                profiles = profiles.stream()
                    .filter(p -> p.getTotalExperienceYears() != null && p.getTotalExperienceYears() >= minExp)
                    .collect(Collectors.toList());
            }

            List<ResumeProfileDto> candidates = profiles.stream()
                .map(ResumeProfileDto::from)
                .collect(Collectors.toList());

            String finalMessage = reply;
            if (!candidates.isEmpty()) {
                finalMessage += " " + candidates.size() + "명의 지원자를 찾았습니다.";
            } else {
                finalMessage += " 조건에 맞는 지원자가 없습니다.";
            }

            return ChatResponse.builder()
                .message(finalMessage)
                .candidates(candidates)
                .build();

        } catch (Exception e) {
            log.error("Chat error", e);
            return ChatResponse.builder()
                .message("오류가 발생했습니다: " + e.getMessage())
                .candidates(List.of())
                .build();
        }
    }
}
