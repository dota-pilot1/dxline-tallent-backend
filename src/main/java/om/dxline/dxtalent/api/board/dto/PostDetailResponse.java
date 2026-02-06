package om.dxline.dxtalent.api.board.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.board.entity.Post;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorInfo author;
    private String boardCode;
    private String boardName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isPinned;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String email;
    }

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .name(post.getAuthor().getName())
                        .email(post.getAuthor().getEmail())
                        .build())
                .boardCode(post.getBoard().getCode())
                .boardName(post.getBoard().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPinned(post.getIsPinned())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
