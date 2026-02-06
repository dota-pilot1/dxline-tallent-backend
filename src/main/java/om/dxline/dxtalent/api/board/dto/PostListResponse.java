package om.dxline.dxtalent.api.board.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import om.dxline.dxtalent.domain.board.entity.Post;

@Getter
@Builder
public class PostListResponse {
    private Long id;
    private String title;
    private AuthorInfo author;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isPinned;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String name;
    }

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .name(post.getAuthor().getName())
                        .build())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPinned(post.getIsPinned())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
