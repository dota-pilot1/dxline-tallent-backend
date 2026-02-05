package om.dxline.dxtalent.api.chat.dto;

import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.api.resume.dto.ResumeProfileDto;

import java.util.List;

@Getter
@Builder
public class ChatResponse {
    private String message;
    private List<ResumeProfileDto> candidates;
}
