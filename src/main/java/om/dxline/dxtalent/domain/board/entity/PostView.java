package om.dxline.dxtalent.domain.board.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import om.dxline.dxtalent.domain.user.entity.User;

@Entity
@Table(name = "post_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String ipAddress;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @Builder
    public PostView(Post post, User user, String ipAddress) {
        this.post = post;
        this.user = user;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void onCreate() {
        this.viewedAt = LocalDateTime.now();
    }
}
