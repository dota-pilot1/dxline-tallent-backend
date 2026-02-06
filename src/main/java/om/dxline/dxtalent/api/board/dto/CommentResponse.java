package om.dxline.dxtalent.api.board.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.board.entity.PostComment;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorInfo author;
    private Long parentId;
    private Integer depth;
    private Boolean isDeleted;
    private Boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String name;
    }

    public static CommentResponse from(PostComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(AuthorInfo.builder()
                        .id(comment.getAuthor().getId())
                        .name(comment.getAuthor().getName())
                        .build())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .isDeleted(comment.getIsDeleted())
                .isEdited(comment.getIsEdited())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
