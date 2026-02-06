package om.dxline.dxtalent.api.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import om.dxline.dxtalent.domain.board.entity.BoardType;

@Getter
@Setter
public class CreateBoardRequest {

    @NotBlank(message = "게시판 코드를 입력해주세요")
    private String code;

    @NotBlank(message = "게시판 이름을 입력해주세요")
    private String name;

    private String description;

    @NotNull(message = "게시판 타입을 선택해주세요")
    private BoardType boardType;

    private String readPermission = "ALL";
    private String writePermission = "USER";
    private String icon;
    private Integer displayOrder = 0;
}
