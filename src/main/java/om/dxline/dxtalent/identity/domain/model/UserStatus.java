package om.dxline.dxtalent.identity.domain.model;

/**
 * UserStatus 열거형 (사용자 계정 상태)
 *
 * 사용자 계정의 상태를 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 신규 가입 시 ACTIVE 상태로 시작
 * 2. ACTIVE 상태만 로그인 가능
 * 3. DEACTIVATED 상태는 재활성화 가능
 * 4. SUSPENDED 상태는 관리자 승인 필요
 * 5. DELETED 상태는 복구 불가능 (soft delete)
 *
 * 상태 전이:
 * ACTIVE -> DEACTIVATED (본인 또는 관리자)
 * ACTIVE -> SUSPENDED (관리자)
 * DEACTIVATED -> ACTIVE (재활성화)
 * SUSPENDED -> ACTIVE (관리자 승인)
 * * -> DELETED (완전 삭제, 복구 불가)
 */
public enum UserStatus {
    /**
     * 활성 상태
     * - 정상적으로 모든 기능 사용 가능
     * - 로그인 가능
     */
    ACTIVE("활성", "정상적으로 사용 가능한 계정"),

    /**
     * 비활성 상태
     * - 사용자가 스스로 비활성화했거나 관리자가 비활성화
     * - 로그인 불가
     * - 재활성화 가능
     */
    DEACTIVATED("비활성", "비활성화된 계정 (재활성화 가능)"),

    /**
     * 정지 상태
     * - 관리자에 의해 일시적으로 정지됨
     * - 로그인 불가
     * - 관리자 승인으로 복구 가능
     * - 이용 약관 위반, 의심스러운 활동 등
     */
    SUSPENDED("정지", "일시 정지된 계정 (관리자 승인 필요)"),

    /**
     * 삭제 상태 (Soft Delete)
     * - 완전히 삭제 예정
     * - 로그인 불가
     * - 데이터는 일정 기간 보관 후 완전 삭제
     * - 복구 불가능
     */
    DELETED("삭제", "삭제된 계정 (복구 불가)");

    private final String koreanName;
    private final String description;

    /**
     * UserStatus 생성자
     *
     * @param koreanName 한글 이름
     * @param description 상태 설명
     */
    UserStatus(String koreanName, String description) {
        this.koreanName = koreanName;
        this.description = description;
    }

    /**
     * 한글 이름 반환
     *
     * @return 한글 이름
     */
    public String getKoreanName() {
        return koreanName;
    }

    /**
     * 상태 설명 반환
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 로그인 가능한 상태인지 확인
     *
     * @return ACTIVE 상태만 true
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * 활성 상태인지 확인
     *
     * @return ACTIVE면 true
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 비활성 상태인지 확인
     *
     * @return DEACTIVATED면 true
     */
    public boolean isDeactivated() {
        return this == DEACTIVATED;
    }

    /**
     * 정지 상태인지 확인
     *
     * @return SUSPENDED면 true
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    /**
     * 삭제 상태인지 확인
     *
     * @return DELETED면 true
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * 재활성화 가능한 상태인지 확인
     *
     * @return DEACTIVATED 또는 SUSPENDED면 true
     */
    public boolean canBeReactivated() {
        return this == DEACTIVATED || this == SUSPENDED;
    }

    /**
     * 삭제 가능한 상태인지 확인
     * 이미 삭제된 상태가 아니면 삭제 가능
     *
     * @return DELETED가 아니면 true
     */
    public boolean canBeDeleted() {
        return this != DELETED;
    }

    /**
     * 정지 가능한 상태인지 확인
     * ACTIVE 또는 DEACTIVATED 상태만 정지 가능
     *
     * @return 정지 가능하면 true
     */
    public boolean canBeSuspended() {
        return this == ACTIVE || this == DEACTIVATED;
    }

    /**
     * 특정 상태로 전이 가능한지 확인
     *
     * @param targetStatus 전이하려는 상태
     * @return 전이 가능하면 true
     */
    public boolean canTransitionTo(UserStatus targetStatus) {
        // 같은 상태로는 전이 불가
        if (this == targetStatus) {
            return false;
        }

        // DELETED 상태에서는 어디로도 전이 불가
        if (this == DELETED) {
            return false;
        }

        // DELETED로는 항상 전이 가능 (삭제는 모든 상태에서 가능)
        if (targetStatus == DELETED) {
            return true;
        }

        // ACTIVE로 전이 가능한 경우
        if (targetStatus == ACTIVE) {
            return this == DEACTIVATED || this == SUSPENDED;
        }

        // DEACTIVATED로 전이 가능한 경우
        if (targetStatus == DEACTIVATED) {
            return this == ACTIVE;
        }

        // SUSPENDED로 전이 가능한 경우
        if (targetStatus == SUSPENDED) {
            return this == ACTIVE || this == DEACTIVATED;
        }

        return false;
    }

    /**
     * 상태 전이 가능 여부 검증 (예외 발생)
     *
     * @param targetStatus 전이하려는 상태
     * @throws IllegalStateException 전이 불가능한 경우
     */
    public void validateTransitionTo(UserStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                String.format(
                    "계정 상태를 %s에서 %s로 변경할 수 없습니다",
                    this.getKoreanName(),
                    targetStatus.getKoreanName()
                )
            );
        }
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return koreanName + " (" + name() + ")";
    }

    /**
     * 문자열로부터 UserStatus 생성 (대소문자 무시)
     *
     * @param value 상태 문자열
     * @return UserStatus 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 상태인 경우
     */
    public static UserStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("상태 값은 필수입니다");
        }

        try {
            return UserStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "유효하지 않은 상태입니다: " + value +
                " (허용값: ACTIVE, DEACTIVATED, SUSPENDED, DELETED)"
            );
        }
    }

    /**
     * 기본 상태 반환 (신규 사용자용)
     *
     * @return ACTIVE 상태
     */
    public static UserStatus getDefault() {
        return ACTIVE;
    }
}
