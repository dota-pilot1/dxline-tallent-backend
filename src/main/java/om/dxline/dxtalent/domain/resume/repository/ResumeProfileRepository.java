package om.dxline.dxtalent.domain.resume.repository;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.domain.resume.entity.ResumeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeProfileRepository
    extends JpaRepository<ResumeProfile, Long>
{
    Optional<ResumeProfile> findByResumeFileId(Long resumeFileId);

    List<ResumeProfile> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(
        value = "SELECT * FROM resume_profiles rp WHERE rp.parsed = true " +
            "AND (:skill IS NULL OR LOWER(CAST(rp.skills AS TEXT)) LIKE LOWER(CONCAT('%', :skill, '%'))) " +
            "AND (:task IS NULL OR LOWER(CAST(rp.experience_summary AS TEXT)) LIKE LOWER(CONCAT('%', :task, '%')))",
        nativeQuery = true
    )
    List<ResumeProfile> searchBySkillAndTask(
        @Param("skill") String skill,
        @Param("task") String task
    );
}
