package om.dxline.dxtalent.api.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardRequest {
    private String name;
    private String description;
    private String readPermission;
    private String writePermission;
    private String icon;
    private Integer displayOrder;
}
