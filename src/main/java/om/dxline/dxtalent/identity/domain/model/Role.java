package om.dxline.dxtalent.identity.domain.model;

/**
 * Role 열거형 (사용자 역할)
 *
 * 사용자의 시스템 내 역할을 정의합니다.
 *
 * DDD에서 Enum은 값 객체의 한 형태로 볼 수 있습니다.
 * - 불변성 보장
 * - 제한된 값만 허용
 * - 타입 안정성
 *
 * 비즈니스 규칙:
 * 1. 신규 사용자는 USER 역할로 시작
 * 2. ADMIN 역할은 특별한 권한 필요
 * 3. 역할은 변경 가능하지만 이력이 남아야 함
 */
public enum Role {
    /**
     * 일반 사용자
     * - 기본 역할
     * - 이력서 업로드 가능
     * - 자신의 데이터만 조회/수정 가능
     */
    USER("일반 사용자", "ROLE_USER"),

    /**
     * 관리자
     * - 모든 데이터 접근 가능
     * - 사용자 관리 권한
     * - 시스템 설정 권한
     */
    ADMIN("관리자", "ROLE_ADMIN"),

    /**
     * HR 담당자 (선택적)
     * - 이력서 조회 및 관리
     * - 지원자와의 소통 권한
     */
    HR("HR 담당자", "ROLE_HR");

    private final String description;
    private final String authority;

    /**
     * Role 생성자
     *
     * @param description 역할 설명 (한글)
     * @param authority Spring Security 권한 문자열
     */
    Role(String description, String authority) {
        this.description = description;
        this.authority = authority;
    }

    /**
     * 역할 설명 반환
     *
     * @return 역할 설명 (한글)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Spring Security 권한 문자열 반환
     * Spring Security의 @PreAuthorize, @Secured 등에서 사용
     *
     * @return 권한 문자열 (예: "ROLE_USER")
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * 관리자 권한인지 확인
     *
     * @return ADMIN이면 true
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 일반 사용자 권한인지 확인
     *
     * @return USER면 true
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * HR 권한인지 확인
     *
     * @return HR이면 true
     */
    public boolean isHR() {
        return this == HR;
    }

    /**
     * 특정 역할보다 높은 권한인지 확인
     *
     * @param other 비교할 역할
     * @return 이 역할이 더 높으면 true
     */
    public boolean hasHigherAuthorityThan(Role other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * 다른 사용자의 데이터에 접근 가능한지 확인
     *
     * @param targetUserId 접근하려는 사용자 ID
     * @param currentUserId 현재 사용자 ID
     * @return 접근 가능하면 true
     */
    public boolean canAccessUser(UserId targetUserId, UserId currentUserId) {
        // 관리자는 모든 사용자 접근 가능
        if (this.isAdmin()) {
            return true;
        }

        // HR은 모든 사용자 조회 가능 (수정은 불가)
        if (this.isHR()) {
            return true;
        }

        // 일반 사용자는 자신만 접근 가능
        return targetUserId.equals(currentUserId);
    }

    /**
     * 사용자 정보 수정 권한 확인
     *
     * @param targetUserId 수정하려는 사용자 ID
     * @param currentUserId 현재 사용자 ID
     * @return 수정 가능하면 true
     */
    public boolean canModifyUser(UserId targetUserId, UserId currentUserId) {
        // 관리자는 모든 사용자 수정 가능
        if (this.isAdmin()) {
            return true;
        }

        // 일반 사용자와 HR은 자신만 수정 가능
        return targetUserId.equals(currentUserId);
    }

    /**
     * 역할 변경 권한 확인
     *
     * @return 역할 변경 가능하면 true (관리자만 가능)
     */
    public boolean canChangeRoles() {
        return this.isAdmin();
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return description + " (" + name() + ")";
    }

    /**
     * 문자열로부터 Role 생성 (대소문자 무시)
     *
     * @param value 역할 문자열 ("USER", "ADMIN", "HR")
     * @return Role 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 역할인 경우
     */
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("역할 값은 필수입니다");
        }

        try {
            return Role.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "유효하지 않은 역할입니다: " + value +
                " (허용값: USER, ADMIN, HR)"
            );
        }
    }

    /**
     * 기본 역할 반환 (신규 사용자용)
     *
     * @return USER 역할
     */
    public static Role getDefault() {
        return USER;
    }
}
