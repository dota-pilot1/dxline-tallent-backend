package om.dxline.dxtalent.talent.domain.repository;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.talent.domain.model.Resume;
import om.dxline.dxtalent.talent.domain.model.ResumeId;

/**
 * Resume 애그리게이트 Repository 인터페이스 (도메인 레이어)
 *
 * 이력서 애그리게이트의 영속성을 담당하는 Repository 인터페이스입니다.
 * 도메인 레이어에 위치하며, 인프라 레이어에서 구현됩니다.
 *
 * 설계 원칙:
 * - 도메인 모델(Resume)만 다룸 (JPA Entity 노출 안 함)
 * - 애그리게이트 단위로 저장/조회 (트랜잭션 경계)
 * - 컬렉션 지향 Repository 패턴
 *
 * 구현 책임:
 * - 도메인 모델 ↔ JPA Entity 변환 (Adapter에서)
 * - 데이터베이스 영속성 처리
 * - 도메인 이벤트 발행 (저장 시)
 */
public interface ResumeRepository {

    /**
     * 이력서 저장
     *
     * 신규 생성 또는 기존 이력서 업데이트 모두 처리합니다.
     * 저장 후 도메인 이벤트를 발행합니다.
     *
     * @param resume 저장할 이력서 애그리게이트
     * @return 저장된 이력서 (ID가 할당됨)
     */
    Resume save(Resume resume);

    /**
     * ID로 이력서 조회
     *
     * @param id 이력서 ID
     * @return 이력서 Optional (없으면 empty)
     */
    Optional<Resume> findById(ResumeId id);

    /**
     * 사용자 ID로 이력서 목록 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 목록 (최신순 정렬)
     */
    List<Resume> findByUserId(UserId userId);

    /**
     * 사용자 ID와 이력서 ID로 조회
     *
     * @param id 이력서 ID
     * @param userId 사용자 ID
     * @return 이력서 Optional (없으면 empty)
     */
    Optional<Resume> findByIdAndUserId(ResumeId id, UserId userId);

    /**
     * 이력서 삭제 (물리적 삭제)
     *
     * 주의: 도메인에서는 소프트 삭제(resume.delete())를 권장합니다.
     * 이 메서드는 관리자용 또는 테스트용으로만 사용하세요.
     *
     * @param id 이력서 ID
     */
    void deleteById(ResumeId id);

    /**
     * 이력서 존재 여부 확인
     *
     * @param id 이력서 ID
     * @return 존재하면 true
     */
    boolean existsById(ResumeId id);

    /**
     * 사용자의 이력서 개수 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 개수
     */
    long countByUserId(UserId userId);

    /**
     * 모든 이력서 조회 (관리자용)
     *
     * @return 전체 이력서 목록
     */
    List<Resume> findAll();
}
