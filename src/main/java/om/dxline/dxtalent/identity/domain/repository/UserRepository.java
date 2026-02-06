package om.dxline.dxtalent.identity.domain.repository;

import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.Role;
import om.dxline.dxtalent.identity.domain.model.User;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.identity.domain.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository 인터페이스 (도메인 레이어)
 *
 * DDD에서 Repository는 도메인 레이어에 인터페이스로 정의됩니다.
 * 실제 구현체는 인프라 레이어에 위치합니다.
 *
 * 이것이 의존성 역전 원칙(Dependency Inversion Principle)입니다:
 * - 도메인 레이어는 인프라에 의존하지 않음
 * - 인프라 레이어가 도메인 인터페이스를 구현
 *
 * Repository의 책임:
 * 1. 애그리게이트의 영속화 및 재구성
 * 2. 도메인 객체로 작업 (JPA 엔티티 노출 금지)
 * 3. 컬렉션처럼 동작 (애그리게이트 추가/제거/조회)
 *
 * 기존 방식과의 차이:
 * <pre>
 * // ❌ 기존 - JpaRepository 직접 상속
 * public interface UserRepository extends JpaRepository<User, Long> {
 *     Optional<User> findByEmail(String email);
 * }
 * // 문제점: JPA에 강하게 결합, 도메인 개념 부족
 *
 * // ✅ DDD - 순수 도메인 인터페이스
 * public interface UserRepository {
 *     Optional<User> findByEmail(Email email);  // 값 객체 사용
 *     void save(User user);                      // 도메인 객체
 * }
 * // 장점: 인프라 독립, 도메인 개념 명확, 테스트 용이
 * </pre>
 */
public interface UserRepository {

    // ============================================================
    // 기본 CRUD 메서드
    // ============================================================

    /**
     * 사용자 저장 (생성 또는 업데이트)
     *
     * @param user 저장할 사용자
     * @return 저장된 사용자 (ID가 할당됨)
     */
    User save(User user);

    /**
     * 사용자 ID로 조회
     *
     * @param id 사용자 ID
     * @return 사용자 (존재하지 않으면 Empty)
     */
    Optional<User> findById(UserId id);

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 (존재하지 않으면 Empty)
     */
    Optional<User> findByEmail(Email email);

    /**
     * 사용자 삭제 (실제로는 Soft Delete 권장)
     *
     * @param user 삭제할 사용자
     */
    void delete(User user);

    /**
     * 사용자 ID로 삭제
     *
     * @param id 삭제할 사용자 ID
     */
    void deleteById(UserId id);

    /**
     * 사용자 존재 여부 확인
     *
     * @param id 사용자 ID
     * @return 존재하면 true
     */
    boolean existsById(UserId id);

    /**
     * 이메일 존재 여부 확인 (중복 체크)
     *
     * @param email 이메일
     * @return 존재하면 true
     */
    boolean existsByEmail(Email email);

    // ============================================================
    // 조회 메서드 (Query Methods)
    // ============================================================

    /**
     * 모든 사용자 조회
     *
     * @return 모든 사용자 목록
     */
    List<User> findAll();

    /**
     * 특정 상태의 사용자 목록 조회
     *
     * @param status 사용자 상태
     * @return 해당 상태의 사용자 목록
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 활성 사용자 목록 조회
     *
     * @return 활성 상태 사용자 목록
     */
    default List<User> findActiveUsers() {
        return findByStatus(UserStatus.ACTIVE);
    }

    /**
     * 특정 역할의 사용자 목록 조회
     *
     * @param role 역할
     * @return 해당 역할의 사용자 목록
     */
    List<User> findByRole(Role role);

    /**
     * 관리자 목록 조회
     *
     * @return 관리자 목록
     */
    default List<User> findAdmins() {
        return findByRole(Role.ADMIN);
    }

    /**
     * 특정 기간 동안 로그인하지 않은 사용자 조회
     *
     * @param since 기준 시점
     * @return 해당 기간 동안 로그인하지 않은 사용자 목록
     */
    List<User> findUsersNotLoggedInSince(LocalDateTime since);

    /**
     * 최근 가입한 사용자 조회
     *
     * @param limit 조회할 개수
     * @return 최근 가입한 사용자 목록
     */
    List<User> findRecentlyRegistered(int limit);

    /**
     * 이메일로 사용자 검색 (부분 일치)
     *
     * @param emailPattern 이메일 패턴
     * @return 검색된 사용자 목록
     */
    List<User> searchByEmail(String emailPattern);

    /**
     * 이름으로 사용자 검색 (부분 일치)
     *
     * @param namePattern 이름 패턴
     * @return 검색된 사용자 목록
     */
    List<User> searchByName(String namePattern);

    // ============================================================
    // 집계 메서드 (Aggregation Methods)
    // ============================================================

    /**
     * 총 사용자 수 조회
     *
     * @return 총 사용자 수
     */
    long count();

    /**
     * 특정 상태의 사용자 수 조회
     *
     * @param status 사용자 상태
     * @return 해당 상태의 사용자 수
     */
    long countByStatus(UserStatus status);

    /**
     * 활성 사용자 수 조회
     *
     * @return 활성 사용자 수
     */
    default long countActiveUsers() {
        return countByStatus(UserStatus.ACTIVE);
    }

    /**
     * 특정 역할의 사용자 수 조회
     *
     * @param role 역할
     * @return 해당 역할의 사용자 수
     */
    long countByRole(Role role);

    /**
     * 특정 기간 동안 가입한 사용자 수 조회
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 기간 동안 가입한 사용자 수
     */
    long countRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ============================================================
    // 배치 작업 메서드
    // ============================================================

    /**
     * 여러 사용자 일괄 저장
     *
     * @param users 저장할 사용자 목록
     * @return 저장된 사용자 목록
     */
    List<User> saveAll(List<User> users);

    /**
     * 여러 사용자 일괄 삭제
     *
     * @param users 삭제할 사용자 목록
     */
    void deleteAll(List<User> users);

    /**
     * 모든 사용자 삭제 (주의: 테스트 목적으로만 사용)
     */
    void deleteAll();

    // ============================================================
    // 유틸리티 메서드
    // ============================================================

    /**
     * Repository 구현체 초기화 (캐시 초기화 등)
     */
    default void flush() {
        // 기본 구현은 아무것도 하지 않음
        // JPA 구현체에서 필요시 오버라이드
    }

    /**
     * 영속성 컨텍스트 새로고침
     *
     * @param user 새로고침할 사용자
     */
    default void refresh(User user) {
        // 기본 구현은 아무것도 하지 않음
        // JPA 구현체에서 필요시 오버라이드
    }

    /**
     * 특정 사용자가 유일한 관리자인지 확인
     * (마지막 관리자 삭제 방지용)
     *
     * @param userId 확인할 사용자 ID
     * @return 유일한 관리자면 true
     */
    default boolean isOnlyAdmin(UserId userId) {
        long adminCount = countByRole(Role.ADMIN);
        if (adminCount != 1) {
            return false;
        }
        return findById(userId)
            .map(User::isAdmin)
            .orElse(false);
    }
}
