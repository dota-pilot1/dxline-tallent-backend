package om.dxline.dxtalent.domain.resume.repository;

import om.dxline.dxtalent.domain.resume.entity.ResumeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeFileRepository extends JpaRepository<ResumeFile, Long> {
    List<ResumeFile> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ResumeFile> findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
