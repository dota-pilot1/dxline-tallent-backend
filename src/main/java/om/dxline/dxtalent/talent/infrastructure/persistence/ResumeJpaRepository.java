package om.dxline.dxtalent.talent.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Resume JPA Repository (인프라 레이어)
 *
 * Spring Data JPA를 사용한 Resume 영속성 처리를 위한 Repository입니다.
 * ResumeJpaEntity를 다루며, 도메인 레이어에는 노출되지 않습니다.
 *
 * 주의사항:
 * - 이 인터페이스는 인프라 레이어에만 존재합니다.
 * - 도메인 레이어는 ResumeRepository 인터페이스를 사용합니다.
 * - ResumeRepositoryAdapter가 이 둘을 연결합니다.
 */
@Repository
public interface ResumeJpaRepository extends JpaRepository<ResumeJpaEntity, Long> {

    /**
     * 사용자 ID로 이력서 목록 조회 (최신순 정렬, 삭제되지 않은 것만)
     *
     * @param userId 사용자 ID
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.userId = :userId AND r.deletedAt IS NULL ORDER BY r.uploadedAt DESC")
    List<ResumeJpaEntity> findByUserIdOrderByUploadedAtDesc(@Param("userId") Long userId);

    /**
     * 사용자 ID와 이력서 ID로 조회 (삭제되지 않은 것만)
     *
     * @param id 이력서 ID
     * @param userId 사용자 ID
     * @return 이력서 Optional
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.id = :id AND r.userId = :userId AND r.deletedAt IS NULL")
    Optional<ResumeJpaEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 사용자의 이력서 개수 조회 (삭제되지 않은 것만)
     *
     * @param userId 사용자 ID
     * @return 이력서 개수
     */
    @Query("SELECT COUNT(r) FROM ResumeJpaEntity r WHERE r.userId = :userId AND r.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 삭제되지 않은 이력서만 조회
     *
     * @param id 이력서 ID
     * @return 이력서 Optional
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<ResumeJpaEntity> findByIdNotDeleted(@Param("id") Long id);

    /**
     * 삭제되지 않은 모든 이력서 조회
     *
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.deletedAt IS NULL ORDER BY r.uploadedAt DESC")
    List<ResumeJpaEntity> findAllNotDeleted();

    /**
     * 특정 상태의 이력서 조회
     *
     * @param status 이력서 상태
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.status = :status AND r.deletedAt IS NULL ORDER BY r.uploadedAt DESC")
    List<ResumeJpaEntity> findByStatus(@Param("status") ResumeJpaEntity.ResumeStatusJpa status);

    /**
     * 파싱 실패한 이력서 조회 (재시도 대상)
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.status = 'PARSE_FAILED' AND r.parseRetryCount < :maxRetryCount AND r.deletedAt IS NULL ORDER BY r.uploadedAt ASC")
    List<ResumeJpaEntity> findFailedResumesForRetry(@Param("maxRetryCount") int maxRetryCount);

    /**
     * 스킬로 검색 (JSON 필드 내 검색)
     *
     * @param skillKeyword 스킬 키워드
     * @return 이력서 목록
     */
    @Query(value = "SELECT * FROM resumes r WHERE r.status = 'PARSED' AND r.deleted_at IS NULL " +
            "AND LOWER(r.skills) LIKE LOWER(CONCAT('%', :skillKeyword, '%')) " +
            "ORDER BY r.parsed_at DESC", nativeQuery = true)
    List<ResumeJpaEntity> searchBySkill(@Param("skillKeyword") String skillKeyword);

    /**
     * 스킬과 경력으로 검색
     *
     * @param skillKeyword 스킬 키워드
     * @param experienceKeyword 경력 키워드
     * @return 이력서 목록
     */
    @Query(value = "SELECT * FROM resumes r WHERE r.status = 'PARSED' AND r.deleted_at IS NULL " +
            "AND (:skillKeyword IS NULL OR LOWER(r.skills) LIKE LOWER(CONCAT('%', :skillKeyword, '%'))) " +
            "AND (:experienceKeyword IS NULL OR LOWER(r.experiences) LIKE LOWER(CONCAT('%', :experienceKeyword, '%'))) " +
            "ORDER BY r.parsed_at DESC", nativeQuery = true)
    List<ResumeJpaEntity> searchBySkillAndExperience(
            @Param("skillKeyword") String skillKeyword,
            @Param("experienceKeyword") String experienceKeyword
    );

    /**
     * 이름으로 검색
     *
     * @param nameKeyword 이름 키워드
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.status = 'PARSED' AND r.deletedAt IS NULL " +
            "AND LOWER(r.candidateName) LIKE LOWER(CONCAT('%', :nameKeyword, '%')) " +
            "ORDER BY r.parsedAt DESC")
    List<ResumeJpaEntity> searchByName(@Param("nameKeyword") String nameKeyword);

    /**
     * 이메일로 검색 (중복 확인용)
     *
     * @param email 이메일
     * @return 이력서 목록
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.contactEmail = :email AND r.deletedAt IS NULL")
    List<ResumeJpaEntity> findByContactEmail(@Param("email") String email);

    /**
     * S3 키로 조회
     *
     * @param s3Key S3 키
     * @return 이력서 Optional
     */
    @Query("SELECT r FROM ResumeJpaEntity r WHERE r.s3Key = :s3Key AND r.deletedAt IS NULL")
    Optional<ResumeJpaEntity> findByS3Key(@Param("s3Key") String s3Key);
}
