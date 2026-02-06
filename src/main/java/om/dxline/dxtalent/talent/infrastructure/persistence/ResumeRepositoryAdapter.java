package om.dxline.dxtalent.talent.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.shared.domain.event.DomainEventPublisher;
import om.dxline.dxtalent.talent.domain.model.Resume;
import om.dxline.dxtalent.talent.domain.model.ResumeId;
import om.dxline.dxtalent.talent.domain.repository.ResumeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ResumeRepositoryAdapter (인프라 레이어)
 *
 * Resume 도메인 Repository 인터페이스의 구현체입니다.
 * JPA Repository와 도메인 모델 사이를 연결하는 어댑터 역할을 합니다.
 *
 * 책임:
 * - 도메인 모델 ↔ JPA Entity 변환 (ResumeMapper 사용)
 * - JPA를 통한 데이터베이스 영속성 처리
 * - 도메인 이벤트 발행 (저장 시)
 * - 트랜잭션 경계 관리
 *
 * 설계 패턴:
 * - Adapter Pattern: 도메인 인터페이스를 JPA 구현으로 어댑팅
 * - Repository Pattern: 컬렉션처럼 애그리게이트를 저장/조회
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ResumeRepositoryAdapter implements ResumeRepository {

    private final ResumeJpaRepository jpaRepository;
    private final ResumeMapper mapper;
    private final DomainEventPublisher eventPublisher;

    /**
     * 이력서 저장 (생성 또는 업데이트)
     *
     * 동작:
     * 1. 도메인 모델 → JPA Entity 변환
     * 2. JPA로 저장 (ID가 없으면 생성, 있으면 업데이트)
     * 3. 저장된 Entity → 도메인 모델 변환
     * 4. 도메인 이벤트 발행
     *
     * @param resume 저장할 이력서 애그리게이트
     * @return 저장된 이력서 (ID가 할당됨)
     */
    @Override
    @Transactional
    public Resume save(Resume resume) {
        log.debug("Saving resume: {}", resume.getId());

        // 1. 도메인 모델 → JPA Entity 변환
        ResumeJpaEntity entity = mapper.toEntity(resume);

        // 2. 기존 엔티티 확인 및 병합 처리
        ResumeJpaEntity entityToSave;
        if (
            entity.getId() != null && jpaRepository.existsById(entity.getId())
        ) {
            // 기존 엔티티가 있는 경우 - 병합
            log.debug("Merging existing resume entity: ID={}", entity.getId());
            entityToSave = entity;
        } else {
            // 신규 엔티티 - ID를 null로 설정하여 JPA가 새로 생성하도록 함
            if (entity.getId() != null) {
                log.debug(
                    "New resume with temporary ID, setting ID to null for JPA generation"
                );
                entity.setId(null);
            }
            entityToSave = entity;
        }

        // 3. JPA로 저장
        ResumeJpaEntity savedEntity = jpaRepository.save(entityToSave);

        // 4. 저장된 Entity → 도메인 모델 변환
        Resume savedResume = mapper.toDomain(savedEntity);

        // 5. 도메인 이벤트 발행
        if (
            resume.getDomainEvents() != null &&
            !resume.getDomainEvents().isEmpty()
        ) {
            resume.getDomainEvents().forEach(eventPublisher::publish);
            resume.clearDomainEvents();
        }

        log.info("Resume saved successfully: ID={}", savedEntity.getId());
        return savedResume;
    }

    /**
     * ID로 이력서 조회
     *
     * @param id 이력서 ID
     * @return 이력서 Optional (삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findById(ResumeId id) {
        log.debug("Finding resume by ID: {}", id.getValue());

        return jpaRepository
            .findByIdNotDeleted(id.getValue())
            .map(mapper::toDomain);
    }

    /**
     * 사용자 ID로 이력서 목록 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 목록 (최신순 정렬, 삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resume> findByUserId(UserId userId) {
        log.debug("Finding resumes by user ID: {}", userId.getValue());

        return jpaRepository
            .findByUserIdOrderByUploadedAtDesc(userId.getValue())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * 사용자 ID와 이력서 ID로 조회
     *
     * @param id 이력서 ID
     * @param userId 사용자 ID
     * @return 이력서 Optional (삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findByIdAndUserId(ResumeId id, UserId userId) {
        log.debug(
            "Finding resume by ID {} and user ID {}",
            id.getValue(),
            userId.getValue()
        );

        return jpaRepository
            .findByIdAndUserId(id.getValue(), userId.getValue())
            .map(mapper::toDomain);
    }

    /**
     * 이력서 삭제 (물리적 삭제)
     *
     * 주의: 도메인에서는 소프트 삭제(resume.delete())를 권장합니다.
     * 이 메서드는 관리자용 또는 테스트용으로만 사용하세요.
     *
     * @param id 이력서 ID
     */
    @Override
    @Transactional
    public void deleteById(ResumeId id) {
        log.warn("Physically deleting resume: {}", id.getValue());
        jpaRepository.deleteById(id.getValue());
    }

    /**
     * 이력서 존재 여부 확인
     *
     * @param id 이력서 ID
     * @return 존재하면 true (삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ResumeId id) {
        return jpaRepository.findByIdNotDeleted(id.getValue()).isPresent();
    }

    /**
     * 사용자의 이력서 개수 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 개수 (삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public long countByUserId(UserId userId) {
        return jpaRepository.countByUserId(userId.getValue());
    }

    /**
     * 모든 이력서 조회 (관리자용)
     *
     * @return 전체 이력서 목록 (삭제되지 않은 것만)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resume> findAll() {
        log.debug("Finding all resumes");

        return jpaRepository
            .findAllNotDeleted()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
