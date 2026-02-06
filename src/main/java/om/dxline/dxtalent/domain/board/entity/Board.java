package om.dxline.dxtalent.domain.board.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(length = 20, nullable = false)
    private String readPermission = "ALL";

    @Column(length = 20, nullable = false)
    private String writePermission = "USER";

    @Column(length = 100)
    private String icon;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Board(String code, String name, String description, BoardType boardType,
                 String readPermission, String writePermission, String icon, Integer displayOrder) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.boardType = boardType;
        this.readPermission = readPermission != null ? readPermission : "ALL";
        this.writePermission = writePermission != null ? writePermission : "USER";
        this.icon = icon;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String name, String description, String readPermission,
                       String writePermission, String icon, Integer displayOrder) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (readPermission != null) this.readPermission = readPermission;
        if (writePermission != null) this.writePermission = writePermission;
        if (icon != null) this.icon = icon;
        if (displayOrder != null) this.displayOrder = displayOrder;
    }
}
