package om.dxline.dxtalent.identity.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.Password;
import om.dxline.dxtalent.identity.domain.model.Role;
import om.dxline.dxtalent.identity.domain.model.User;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.identity.domain.model.UserName;
import om.dxline.dxtalent.identity.domain.model.UserStatus;
import org.springframework.stereotype.Repository;

/**
 * UserRepositoryAdapter - 기존 JPA Repository와 새 도메인 Repository 사이의 브릿지
 *
 * 이 어댑터는 두 세계를 연결합니다:
 * 1. 기존 JPA 기반 Repository (domain/user/repository/UserRepository)
 * 2. 새로운 도메인 Repository 인터페이스 (identity/domain/repository/UserRepository)
 *
 * 전략:
 * - Phase 2.5: 임시로 기존 JPA Entity 사용
 * - Phase 3: 점진적으로 매핑 로직 추가
 * - Phase 4: 완전한 분리 (선택)
 *
 * 현재는 간단한 변환만 수행합니다.
 */
@Repository("identityUserRepository")
public class UserRepositoryAdapter
    implements om.dxline.dxtalent.identity.domain.repository.UserRepository
{

    private final om.dxline.dxtalent.domain.user.repository.UserRepository jpaRepository;

    public UserRepositoryAdapter(
        om.dxline.dxtalent.domain.user.repository.UserRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        om.dxline.dxtalent.domain.user.entity.User jpaEntity;

        // 데이터베이스에 실제로 존재하는지 확인
        // ID가 null이거나 0이거나, DB에 존재하지 않으면 신규 사용자
        boolean existsInDb = false;
        if (user.getId() != null && !user.getId().isNew()) {
            existsInDb = jpaRepository.existsById(user.getId().getValue());
        }

        if (existsInDb) {
            // 기존 사용자 업데이트
            om.dxline.dxtalent.domain.user.entity.User existingEntity =
                jpaRepository
                    .findById(user.getId().getValue())
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "사용자를 찾을 수 없습니다: " +
                                user.getId().getValue()
                        )
                    );

            // 기존 엔티티의 ID를 사용하여 새 엔티티 생성
            jpaEntity = om.dxline.dxtalent.domain.user.entity.User.builder()
                .email(user.getEmail().getValue())
                .password(user.getPassword().getValue())
                .name(user.getName().getValue())
                .role(mapRoleToOld(user.getRole()))
                .build();

            // ID, 타임스탬프, status를 리플렉션으로 설정
            setIdUsingReflection(jpaEntity, existingEntity.getId());
            setTimestampsUsingReflection(
                jpaEntity,
                existingEntity.getCreatedAt(),
                user.getUpdatedAt() != null
                    ? user.getUpdatedAt()
                    : LocalDateTime.now()
            );
            setStatusUsingReflection(jpaEntity, user.getStatus().name());
        } else {
            // 신규 사용자 생성 - ID는 JPA가 자동 생성
            jpaEntity = om.dxline.dxtalent.domain.user.entity.User.builder()
                .email(user.getEmail().getValue())
                .password(user.getPassword().getValue())
                .name(user.getName().getValue())
                .role(mapRoleToOld(user.getRole()))
                .build();
            // createdAt, updatedAt는 @PrePersist에서 자동 설정됨
        }

        // JPA로 저장
        om.dxline.dxtalent.domain.user.entity.User savedEntity =
            jpaRepository.save(jpaEntity);

        // 저장된 Entity를 도메인 모델로 변환하여 반환
        return toDomainModel(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        // TODO: 임시 구현 - Phase 3에서 실제 매핑 구현
        return jpaRepository.findById(id.getValue()).map(this::toDomainModel);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        // TODO: 임시 구현 - Phase 3에서 실제 매핑 구현
        return jpaRepository
            .findByEmail(email.getValue())
            .map(this::toDomainModel);
    }

    @Override
    public void delete(User user) {
        if (user.getId() != null && !user.getId().isNew()) {
            jpaRepository.deleteById(user.getId().getValue());
        }
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public List<User> findAll() {
        return jpaRepository
            .findAll()
            .stream()
            .map(this::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        // 기존 JPA Entity에 status 필드가 없으므로
        // ACTIVE 상태만 모든 사용자로 간주
        if (status == UserStatus.ACTIVE) {
            return findAll();
        }
        return List.of();
    }

    @Override
    public List<User> findByRole(Role role) {
        // TODO: JPA Repository에 findByRole 메서드 추가 필요
        // 임시로 전체 조회 후 필터링
        return findAll()
            .stream()
            .filter(user -> user.getRole() == role)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findUsersNotLoggedInSince(LocalDateTime since) {
        throw new UnsupportedOperationException(
            "findUsersNotLoggedInSince - Phase 3에서 구현 예정"
        );
    }

    @Override
    public List<User> findRecentlyRegistered(int limit) {
        throw new UnsupportedOperationException(
            "findRecentlyRegistered - Phase 3에서 구현 예정"
        );
    }

    @Override
    public List<User> searchByEmail(String emailPattern) {
        throw new UnsupportedOperationException(
            "searchByEmail - Phase 3에서 구현 예정"
        );
    }

    @Override
    public List<User> searchByName(String namePattern) {
        throw new UnsupportedOperationException(
            "searchByName - Phase 3에서 구현 예정"
        );
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(UserStatus status) {
        if (status == UserStatus.ACTIVE) {
            return count();
        }
        return 0;
    }

    @Override
    public long countByRole(Role role) {
        return findByRole(role).size();
    }

    @Override
    public long countRegisteredBetween(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        throw new UnsupportedOperationException(
            "countRegisteredBetween - Phase 3에서 구현 예정"
        );
    }

    @Override
    public List<User> saveAll(List<User> users) {
        return users.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public void deleteAll(List<User> users) {
        users.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    // ============================================================
    // 매핑 메서드 (JPA Entity <-> Domain Model)
    // ============================================================

    /**
     * JPA Entity를 Domain Model로 변환
     *
     * TODO: Phase 3에서 완전한 매핑 구현
     * 현재는 간단한 변환만 수행
     */
    private User toDomainModel(
        om.dxline.dxtalent.domain.user.entity.User jpaEntity
    ) {
        // 임시 구현: 기존 JPA Entity의 필드를 새 도메인 모델로 변환
        UserStatus status = UserStatus.ACTIVE; // 기본값
        try {
            // 리플렉션으로 status 필드 읽기
            java.lang.reflect.Field statusField =
                om.dxline.dxtalent.domain.user.entity
                    .User.class.getDeclaredField("status");
            statusField.setAccessible(true);
            String statusValue = (String) statusField.get(jpaEntity);
            if (statusValue != null && !statusValue.isEmpty()) {
                status = UserStatus.valueOf(statusValue);
            }
        } catch (Exception e) {
            // status 필드가 없거나 읽기 실패하면 기본값 사용
        }

        return User.reconstitute(
            UserId.of(jpaEntity.getId()),
            new Email(jpaEntity.getEmail()),
            Password.fromEncrypted(jpaEntity.getPassword()),
            new UserName(jpaEntity.getName()),
            mapRole(jpaEntity.getRole()),
            status,
            jpaEntity.getCreatedAt(),
            jpaEntity.getUpdatedAt(),
            null // lastLoginAt - 기존 Entity에 없을 수 있음
        );
    }

    /**
     * Domain Model을 JPA Entity로 변환
     */
    private om.dxline.dxtalent.domain.user.entity.User toJpaEntity(
        User domainModel
    ) {
        return om.dxline.dxtalent.domain.user.entity.User.builder()
            .email(domainModel.getEmail().getValue())
            .password(domainModel.getPassword().getValue())
            .name(domainModel.getName().getValue())
            .role(mapRoleToOld(domainModel.getRole()))
            .build();
    }

    /**
     * 기존 Role enum을 새 Role enum으로 매핑
     */
    private Role mapRole(om.dxline.dxtalent.domain.user.entity.Role oldRole) {
        return switch (oldRole) {
            case USER -> Role.USER;
            case ADMIN -> Role.ADMIN;
            // 기존에 HR이 없으면 USER로 매핑
            default -> Role.USER;
        };
    }

    /**
     * 새 Role enum을 기존 Role enum으로 매핑
     */
    private om.dxline.dxtalent.domain.user.entity.Role mapRoleToOld(
        Role newRole
    ) {
        return switch (newRole) {
            case USER -> om.dxline.dxtalent.domain.user.entity.Role.USER;
            case ADMIN -> om.dxline.dxtalent.domain.user.entity.Role.ADMIN;
            case HR -> om.dxline.dxtalent.domain.user.entity.Role.USER; // HR을 USER로 매핑
        };
    }

    /**
     * 리플렉션을 사용하여 JPA Entity의 ID 설정
     * JPA Entity가 불변이므로 임시 방편으로 사용
     */
    private void setIdUsingReflection(
        om.dxline.dxtalent.domain.user.entity.User entity,
        Long id
    ) {
        try {
            java.lang.reflect.Field field =
                om.dxline.dxtalent.domain.user.entity
                    .User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    /**
     * 리플렉션을 사용하여 JPA Entity의 타임스탬프 설정
     */
    private void setTimestampsUsingReflection(
        om.dxline.dxtalent.domain.user.entity.User entity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        try {
            if (createdAt != null) {
                java.lang.reflect.Field createdAtField =
                    om.dxline.dxtalent.domain.user.entity
                        .User.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(entity, createdAt);
            }

            if (updatedAt != null) {
                java.lang.reflect.Field updatedAtField =
                    om.dxline.dxtalent.domain.user.entity
                        .User.class.getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(entity, updatedAt);
            }
        } catch (Exception e) {
            throw new RuntimeException("타임스탬프 설정 실패", e);
        }
    }

    /**
     * 리플렉션을 사용하여 JPA Entity의 status 설정
     */
    private void setStatusUsingReflection(
        om.dxline.dxtalent.domain.user.entity.User entity,
        String status
    ) {
        try {
            java.lang.reflect.Field statusField =
                om.dxline.dxtalent.domain.user.entity
                    .User.class.getDeclaredField("status");
            statusField.setAccessible(true);
            // null이나 빈 문자열이면 ACTIVE로 기본값 설정
            statusField.set(
                entity,
                status != null && !status.isEmpty() ? status : "ACTIVE"
            );
        } catch (Exception e) {
            throw new RuntimeException("status 설정 실패", e);
        }
    }
}
