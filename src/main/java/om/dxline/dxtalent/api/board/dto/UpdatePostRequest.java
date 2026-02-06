package om.dxline.dxtalent.api.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostRequest {
    private String title;
    private String content;
}
