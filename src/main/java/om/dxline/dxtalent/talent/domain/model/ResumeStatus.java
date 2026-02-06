package om.dxline.dxtalent.talent.domain.model;

import java.util.Arrays;
import java.util.List;

/**
 * 이력서 상태 열거형 (ResumeStatus Enum)
 *
 * 이력서의 현재 상태를 정의하고 상태 전이 규칙을 관리합니다.
 *
 * 상태:
 * - UPLOADED: 업로드 완료 (파싱 대기 중)
 * - PARSING: 파싱 중
 * - PARSED: 파싱 완료 (정상 사용 가능)
 * - PARSE_FAILED: 파싱 실패
 * - ARCHIVED: 보관됨 (사용하지 않지만 보관)
 * - DELETED: 삭제됨 (소프트 삭제)
 *
 * 상태 전이 규칙:
 * <pre>
 * UPLOADED → PARSING, DELETED
 * PARSING → PARSED, PARSE_FAILED
 * PARSED → ARCHIVED, DELETED
 * PARSE_FAILED → PARSING (재파싱), DELETED
 * ARCHIVED → PARSED (재활성화), DELETED
 * DELETED → (전이 불가, 최종 상태)
 * </pre>
 */
public enum ResumeStatus {

    UPLOADED(
        "업로드 완료",
        "파일이 업로드되어 파싱 대기 중입니다",
        true,
        true
    ),

    PARSING(
        "파싱 중",
        "AI가 이력서를 분석하고 있습니다",
        false,
        false
    ),

    PARSED(
        "파싱 완료",
        "이력서 분석이 완료되어 사용 가능합니다",
        true,
        true
    ),

    PARSE_FAILED(
        "파싱 실패",
        "이력서 분석에 실패했습니다",
        true,
        false
    ),

    ARCHIVED(
        "보관됨",
        "이력서가 보관 상태입니다",
        true,
        false
    ),

    DELETED(
        "삭제됨",
        "이력서가 삭제되었습니다",
        false,
        false
    );

    private final String koreanName;
    private final String description;
    private final boolean canEdit;
    private final boolean canSearch;

    /**
     * ResumeStatus 생성자
     *
     * @param koreanName 한글 이름
     * @param description 설명
     * @param canEdit 편집 가능 여부
     * @param canSearch 검색 가능 여부
     */
    ResumeStatus(String koreanName, String description, boolean canEdit, boolean canSearch) {
        this.koreanName = koreanName;
        this.description = description;
        this.canEdit = canEdit;
        this.canSearch = canSearch;
    }

    /**
     * 한글 이름 반환
     *
     * @return 한글 상태명
     */
    public String getKoreanName() {
        return koreanName;
    }

    /**
     * 설명 반환
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 편집 가능 여부
     *
     * @return 편집 가능하면 true
     */
    public boolean canEdit() {
        return canEdit;
    }

    /**
     * 검색 가능 여부
     *
     * @return 검색 가능하면 true
     */
    public boolean canSearch() {
        return canSearch;
    }

    /**
     * 특정 상태로 전이 가능한지 확인
     *
     * @param newStatus 전이하려는 상태
     * @return 전이 가능하면 true
     */
    public boolean canTransitionTo(ResumeStatus newStatus) {
        if (this == newStatus) {
            return false; // 같은 상태로는 전이 불가
        }

        return getAllowedTransitions().contains(newStatus);
    }

    /**
     * 허용된 상태 전이 목록 반환
     *
     * @return 전이 가능한 상태 목록
     */
    public List<ResumeStatus> getAllowedTransitions() {
        switch (this) {
            case UPLOADED:
                return Arrays.asList(PARSING, DELETED);

            case PARSING:
                return Arrays.asList(PARSED, PARSE_FAILED);

            case PARSED:
                return Arrays.asList(ARCHIVED, DELETED);

            case PARSE_FAILED:
                return Arrays.asList(PARSING, DELETED); // 재파싱 가능

            case ARCHIVED:
                return Arrays.asList(PARSED, DELETED); // 재활성화 가능

            case DELETED:
                return Arrays.asList(); // 최종 상태, 전이 불가

            default:
                return Arrays.asList();
        }
    }

    /**
     * 상태 전이 검증 및 예외 발생
     *
     * @param newStatus 전이하려는 상태
     * @throws IllegalStateException 전이 불가능한 경우
     */
    public void validateTransitionTo(ResumeStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format(
                    "이력서 상태를 %s에서 %s(으)로 변경할 수 없습니다",
                    this.koreanName,
                    newStatus.koreanName
                )
            );
        }
    }

    /**
     * 업로드 상태인지 확인
     *
     * @return UPLOADED 상태면 true
     */
    public boolean isUploaded() {
        return this == UPLOADED;
    }

    /**
     * 파싱 중인지 확인
     *
     * @return PARSING 상태면 true
     */
    public boolean isParsing() {
        return this == PARSING;
    }

    /**
     * 파싱 완료 상태인지 확인
     *
     * @return PARSED 상태면 true
     */
    public boolean isParsed() {
        return this == PARSED;
    }

    /**
     * 파싱 실패 상태인지 확인
     *
     * @return PARSE_FAILED 상태면 true
     */
    public boolean isParseFailed() {
        return this == PARSE_FAILED;
    }

    /**
     * 보관 상태인지 확인
     *
     * @return ARCHIVED 상태면 true
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }

    /**
     * 삭제 상태인지 확인
     *
     * @return DELETED 상태면 true
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * 사용 가능한 상태인지 확인
     * (PARSED 또는 UPLOADED 상태)
     *
     * @return 사용 가능하면 true
     */
    public boolean isUsable() {
        return this == PARSED || this == UPLOADED;
    }

    /**
     * 활성 상태인지 확인
     * (DELETED가 아닌 모든 상태)
     *
     * @return 활성 상태면 true
     */
    public boolean isActive() {
        return this != DELETED;
    }

    /**
     * 파싱 가능한 상태인지 확인
     *
     * @return 파싱 가능하면 true
     */
    public boolean canParse() {
        return this == UPLOADED || this == PARSE_FAILED;
    }

    /**
     * 삭제 가능한 상태인지 확인
     *
     * @return 삭제 가능하면 true
     */
    public boolean canBeDeleted() {
        return this != PARSING && this != DELETED;
    }

    /**
     * 재파싱 가능한 상태인지 확인
     *
     * @return 재파싱 가능하면 true
     */
    public boolean canReparse() {
        return this == PARSE_FAILED;
    }

    /**
     * 보관 가능한 상태인지 확인
     *
     * @return 보관 가능하면 true
     */
    public boolean canBeArchived() {
        return this == PARSED;
    }

    /**
     * 한글 이름으로 ResumeStatus 찾기
     *
     * @param koreanName 한글 이름
     * @return ResumeStatus
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public static ResumeStatus fromKoreanName(String koreanName) {
        for (ResumeStatus status : values()) {
            if (status.koreanName.equals(koreanName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 이력서 상태 이름: " + koreanName);
    }

    /**
     * 기본 상태 반환
     *
     * @return UPLOADED
     */
    public static ResumeStatus getDefault() {
        return UPLOADED;
    }

    /**
     * 검색 가능한 모든 상태 반환
     *
     * @return 검색 가능한 상태 목록
     */
    public static List<ResumeStatus> getSearchableStatuses() {
        return Arrays.asList(UPLOADED, PARSED);
    }

    /**
     * 활성 상태 목록 반환
     *
     * @return 활성 상태 목록 (DELETED 제외)
     */
    public static List<ResumeStatus> getActiveStatuses() {
        return Arrays.asList(UPLOADED, PARSING, PARSED, PARSE_FAILED, ARCHIVED);
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", koreanName, name());
    }
}
