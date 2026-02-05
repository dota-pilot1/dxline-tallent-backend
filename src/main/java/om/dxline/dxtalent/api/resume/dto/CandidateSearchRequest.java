package om.dxline.dxtalent.api.resume.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CandidateSearchRequest {

    private String skill; // 보유 기술
    private String task; // 주요 업무
}
