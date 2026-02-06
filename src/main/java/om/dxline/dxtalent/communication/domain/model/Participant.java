package om.dxline.dxtalent.communication.domain.model;

import java.time.LocalDateTime;
import om.dxline.dxtalent.identity.domain.model.UserId;

public class Participant {
    private final UserId userId;
    private final LocalDateTime joinedAt;

    public Participant(UserId userId, LocalDateTime joinedAt) {
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public UserId getUserId() {
        return userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
