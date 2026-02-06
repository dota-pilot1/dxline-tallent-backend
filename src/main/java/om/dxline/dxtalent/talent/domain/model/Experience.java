package om.dxline.dxtalent.talent.domain.model;

import java.time.LocalDate;
import java.time.Period;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 경력 값 객체 (Experience Value Object)
 *
 * 이력서에 기재된 직장 경력을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 회사명: 필수, 2-100자
 * - 직책: 필수, 2-100자
 * - 시작일: 필수, 종료일보다 이전
 * - 종료일: 선택 (현재 재직 중이면 null)
 * - 설명: 선택, 최대 1000자
 *
 * 사용 예시:
 * <pre>
 * Experience exp = Experience.of(
 *     "테크컴퍼니",
 *     "시니어 개발자",
 *     LocalDate.of(2020, 1, 1),
 *     LocalDate.of(2023, 12, 31),
 *     "Java 백엔드 개발"
 * );
 * int months = exp.getDurationInMonths(); // 48
 * boolean current = exp.isCurrent(); // false
 * </pre>
 */
public class Experience extends BaseValueObject {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_POSITION_LENGTH = 2;
    private static final int MAX_POSITION_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final String companyName;
    private final String position;
    private final LocalDate startDate;
    private final LocalDate endDate; // null이면 현재 재직 중
    private final String description;

    /**
     * 경력 생성
     *
     * @param companyName 회사명
     * @param position 직책
     * @param startDate 시작일
     * @param endDate 종료일 (현재 재직 중이면 null)
     * @param description 업무 설명
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public Experience(
        String companyName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        String description
    ) {
        this.companyName = requireNonNull(companyName, "회사명").trim();
        this.position = requireNonNull(position, "직책").trim();
        this.startDate = requireNonNull(startDate, "시작일");
        this.endDate = endDate; // null 가능
        this.description = description != null ? description.trim() : null;

        validateExperience();
    }

    /**
     * 경력 검증
     */
    private void validateExperience() {
        // 회사명 검증
        validate(
            companyName.length() >= MIN_NAME_LENGTH && companyName.length() <= MAX_NAME_LENGTH,
            String.format("회사명은 %d자 이상 %d자 이하여야 합니다", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        );

        validate(
            !companyName.trim().isEmpty(),
            "회사명은 공백만으로 구성될 수 없습니다"
        );

        // 직책 검증
        validate(
            position.length() >= MIN_POSITION_LENGTH && position.length() <= MAX_POSITION_LENGTH,
            String.format("직책은 %d자 이상 %d자 이하여야 합니다", MIN_POSITION_LENGTH, MAX_POSITION_LENGTH)
        );

        validate(
            !position.trim().isEmpty(),
            "직책은 공백만으로 구성될 수 없습니다"
        );

        // 날짜 검증
        validate(
            !startDate.isAfter(LocalDate.now()),
            "시작일은 현재보다 이후일 수 없습니다"
        );

        if (endDate != null) {
            validate(
                !endDate.isBefore(startDate),
                "종료일은 시작일보다 이전일 수 없습니다"
            );

            validate(
                !endDate.isAfter(LocalDate.now()),
                "종료일은 현재보다 이후일 수 없습니다"
            );
        }

        // 설명 검증
        if (description != null && !description.isEmpty()) {
            validate(
                description.length() <= MAX_DESCRIPTION_LENGTH,
                String.format("설명은 최대 %d자까지 입력 가능합니다", MAX_DESCRIPTION_LENGTH)
            );
        }
    }

    /**
     * 경력 생성 정적 팩토리 메서드 (설명 포함)
     *
     * @param companyName 회사명
     * @param position 직책
     * @param startDate 시작일
     * @param endDate 종료일
     * @param description 업무 설명
     * @return Experience 인스턴스
     */
    public static Experience of(
        String companyName,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        String description
    ) {
        return new Experience(companyName, position, startDate, endDate, description);
    }

    /**
     * 경력 생성 정적 팩토리 메서드 (설명 없이)
     *
     * @param companyName 회사명
     * @param position 직책
     * @param startDate 시작일
     * @param endDate 종료일
     * @return Experience 인스턴스
     */
    public static Experience of(
        String companyName,
        String position,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new Experience(companyName, position, startDate, endDate, null);
    }

    /**
     * 현재 재직 중인 경력 생성
     *
     * @param companyName 회사명
     * @param position 직책
     * @param startDate 시작일
     * @param description 업무 설명
     * @return Experience 인스턴스
     */
    public static Experience current(
        String companyName,
        String position,
        LocalDate startDate,
        String description
    ) {
        return new Experience(companyName, position, startDate, null, description);
    }

    /**
     * 회사명 반환
     *
     * @return 회사명
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * 직책 반환
     *
     * @return 직책
     */
    public String getPosition() {
        return position;
    }

    /**
     * 시작일 반환
     *
     * @return 시작일
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * 종료일 반환
     *
     * @return 종료일 (현재 재직 중이면 null)
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * 설명 반환
     *
     * @return 업무 설명 (없으면 null)
     */
    public String getDescription() {
        return description;
    }

    /**
     * 현재 재직 중인지 확인
     *
     * @return 재직 중이면 true
     */
    public boolean isCurrent() {
        return endDate == null;
    }

    /**
     * 경력 기간 (월 단위)
     *
     * @return 경력 기간 (개월)
     */
    public int getDurationInMonths() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Period period = Period.between(startDate, end);
        return period.getYears() * 12 + period.getMonths();
    }

    /**
     * 경력 기간 (년 단위, 반올림)
     *
     * @return 경력 기간 (년)
     */
    public int getDurationInYears() {
        return (int) Math.round(getDurationInMonths() / 12.0);
    }

    /**
     * 특정 기간 이상 근무했는지 확인
     *
     * @param months 최소 개월 수
     * @return 최소 기간 이상이면 true
     */
    public boolean hasWorkedAtLeast(int months) {
        return getDurationInMonths() >= months;
    }

    /**
     * 특정 날짜에 재직 중이었는지 확인
     *
     * @param date 확인할 날짜
     * @return 재직 중이었으면 true
     */
    public boolean wasWorkingOn(LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }
        if (endDate == null) {
            return true; // 현재 재직 중
        }
        return !date.isAfter(endDate);
    }

    /**
     * 다른 경력과 기간이 겹치는지 확인
     *
     * @param other 다른 경력
     * @return 겹치면 true
     */
    public boolean overlaps(Experience other) {
        LocalDate thisEnd = this.endDate != null ? this.endDate : LocalDate.now();
        LocalDate otherEnd = other.endDate != null ? other.endDate : LocalDate.now();

        return !this.startDate.isAfter(otherEnd) && !thisEnd.isBefore(other.startDate);
    }

    /**
     * 설명이 있는지 확인
     *
     * @return 설명이 있으면 true
     */
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    /**
     * 회사명이 일치하는지 확인 (대소문자 구분 없음)
     *
     * @param companyName 비교할 회사명
     * @return 일치하면 true
     */
    public boolean isCompany(String companyName) {
        return this.companyName.equalsIgnoreCase(companyName);
    }

    /**
     * 종료일 설정 (퇴사 처리)
     *
     * @param endDate 종료일
     * @return 새로운 Experience 인스턴스
     */
    public Experience withEndDate(LocalDate endDate) {
        return new Experience(this.companyName, this.position, this.startDate, endDate, this.description);
    }

    /**
     * 설명 업데이트
     *
     * @param description 새 설명
     * @return 새로운 Experience 인스턴스
     */
    public Experience withDescription(String description) {
        return new Experience(this.companyName, this.position, this.startDate, this.endDate, description);
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] {
            companyName.toLowerCase(),
            position.toLowerCase(),
            startDate,
            endDate
        };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(companyName).append(" - ").append(position);
        sb.append(" (").append(startDate);
        if (endDate != null) {
            sb.append(" ~ ").append(endDate);
        } else {
            sb.append(" ~ 현재");
        }
        sb.append(", ").append(getDurationInMonths()).append("개월)");
        return sb.toString();
    }
}
