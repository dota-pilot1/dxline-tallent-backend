package om.dxline.dxtalent.api.board.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.board.entity.Board;

@Getter
@Builder
public class BoardResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String boardType;
    private String readPermission;
    private String writePermission;
    private String icon;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .code(board.getCode())
                .name(board.getName())
                .description(board.getDescription())
                .boardType(board.getBoardType().name())
                .readPermission(board.getReadPermission())
                .writePermission(board.getWritePermission())
                .icon(board.getIcon())
                .displayOrder(board.getDisplayOrder())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
