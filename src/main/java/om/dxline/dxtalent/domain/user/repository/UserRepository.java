package om.dxline.dxtalent.domain.user.repository;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(
        "SELECT u FROM User u WHERE " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND u.id != :excludeUserId " +
            "ORDER BY u.name"
    )
    List<User> searchByKeyword(
        @Param("keyword") String keyword,
        @Param("excludeUserId") Long excludeUserId
    );
}
