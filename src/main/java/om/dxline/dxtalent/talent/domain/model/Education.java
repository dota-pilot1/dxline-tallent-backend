package om.dxline.dxtalent.talent.domain.model;

import java.time.LocalDate;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 학력 값 객체 (Education Value Object)
 *
 * 이력서에 기재된 학력 정보를 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 학교명: 필수, 2-100자
 * - 학위: 필수, 2-50자
 * - 전공: 필수, 2-100자
 * - 졸업일: 필수, 현재보다 이전
 * - 학점: 선택, 0.0-4.5 (4.5 만점 기준)
 *
 * 사용 예시:
 * <pre>
 * Education edu = Education.of(
 *     "한국대학교",
 *     "학사",
 *     "컴퓨터공학",
 *     LocalDate.of(2020, 2, 28)
 * );
 * boolean graduated = edu.hasGraduated(); // true
 * </pre>
 */
public class Education extends BaseValueObject {

    private static final int MIN_SCHOOL_NAME_LENGTH = 2;
    private static final int MAX_SCHOOL_NAME_LENGTH = 100;
    private static final int MIN_DEGREE_LENGTH = 2;
    private static final int MAX_DEGREE_LENGTH = 50;
    private static final int MIN_MAJOR_LENGTH = 2;
    private static final int MAX_MAJOR_LENGTH = 100;
    private static final double MIN_GPA = 0.0;
    private static final double MAX_GPA = 4.5;

    private final String schoolName;
    private final String degree;
    private final String major;
    private final LocalDate graduationDate;
    private final Double gpa; // 선택적 (nullable)

    /**
     * 학력 생성
     *
     * @param schoolName 학교명
     * @param degree 학위 (예: 학사, 석사, 박사)
     * @param major 전공
     * @param graduationDate 졸업일
     * @param gpa 학점 (선택)
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public Education(
        String schoolName,
        String degree,
        String major,
        LocalDate graduationDate,
        Double gpa
    ) {
        this.schoolName = requireNonNull(schoolName, "학교명").trim();
        this.degree = requireNonNull(degree, "학위").trim();
        this.major = requireNonNull(major, "전공").trim();
        this.graduationDate = requireNonNull(graduationDate, "졸업일");
        this.gpa = gpa;

        validateEducation();
    }

    /**
     * 학력 검증
     */
    private void validateEducation() {
        // 학교명 검증
        validate(
            schoolName.length() >= MIN_SCHOOL_NAME_LENGTH && schoolName.length() <= MAX_SCHOOL_NAME_LENGTH,
            String.format("학교명은 %d자 이상 %d자 이하여야 합니다", MIN_SCHOOL_NAME_LENGTH, MAX_SCHOOL_NAME_LENGTH)
        );

        validate(
            !schoolName.trim().isEmpty(),
            "학교명은 공백만으로 구성될 수 없습니다"
        );

        // 학위 검증
        validate(
            degree.length() >= MIN_DEGREE_LENGTH && degree.length() <= MAX_DEGREE_LENGTH,
            String.format("학위는 %d자 이상 %d자 이하여야 합니다", MIN_DEGREE_LENGTH, MAX_DEGREE_LENGTH)
        );

        validate(
            !degree.trim().isEmpty(),
            "학위는 공백만으로 구성될 수 없습니다"
        );

        // 전공 검증
        validate(
            major.length() >= MIN_MAJOR_LENGTH && major.length() <= MAX_MAJOR_LENGTH,
            String.format("전공은 %d자 이상 %d자 이하여야 합니다", MIN_MAJOR_LENGTH, MAX_MAJOR_LENGTH)
        );

        validate(
            !major.trim().isEmpty(),
            "전공은 공백만으로 구성될 수 없습니다"
        );

        // 졸업일 검증
        validate(
            !graduationDate.isAfter(LocalDate.now()),
            "졸업일은 현재보다 이후일 수 없습니다"
        );

        // 학점 검증 (있는 경우)
        if (gpa != null) {
            validate(
                gpa >= MIN_GPA && gpa <= MAX_GPA,
                String.format("학점은 %.1f 이상 %.1f 이하여야 합니다", MIN_GPA, MAX_GPA)
            );
        }
    }

    /**
     * 학력 생성 정적 팩토리 메서드 (학점 포함)
     *
     * @param schoolName 학교명
     * @param degree 학위
     * @param major 전공
     * @param graduationDate 졸업일
     * @param gpa 학점
     * @return Education 인스턴스
     */
    public static Education of(
        String schoolName,
        String degree,
        String major,
        LocalDate graduationDate,
        Double gpa
    ) {
        return new Education(schoolName, degree, major, graduationDate, gpa);
    }

    /**
     * 학력 생성 정적 팩토리 메서드 (학점 없이)
     *
     * @param schoolName 학교명
     * @param degree 학위
     * @param major 전공
     * @param graduationDate 졸업일
     * @return Education 인스턴스
     */
    public static Education of(
        String schoolName,
        String degree,
        String major,
        LocalDate graduationDate
    ) {
        return new Education(schoolName, degree, major, graduationDate, null);
    }

    /**
     * 학교명 반환
     *
     * @return 학교명
     */
    public String getSchoolName() {
        return schoolName;
    }

    /**
     * 학위 반환
     *
     * @return 학위
     */
    public String getDegree() {
        return degree;
    }

    /**
     * 전공 반환
     *
     * @return 전공
     */
    public String getMajor() {
        return major;
    }

    /**
     * 졸업일 반환
     *
     * @return 졸업일
     */
    public LocalDate getGraduationDate() {
        return graduationDate;
    }

    /**
     * 학점 반환
     *
     * @return 학점 (없으면 null)
     */
    public Double getGpa() {
        return gpa;
    }

    /**
     * 학점이 있는지 확인
     *
     * @return 학점이 있으면 true
     */
    public boolean hasGpa() {
        return gpa != null;
    }

    /**
     * 졸업했는지 확인 (졸업일이 과거인지)
     *
     * @return 졸업했으면 true
     */
    public boolean hasGraduated() {
        return graduationDate.isBefore(LocalDate.now()) || graduationDate.isEqual(LocalDate.now());
    }

    /**
     * 학사 학위인지 확인
     *
     * @return 학사 학위면 true
     */
    public boolean isBachelorDegree() {
        return degree.contains("학사") || degree.equalsIgnoreCase("Bachelor");
    }

    /**
     * 석사 학위인지 확인
     *
     * @return 석사 학위면 true
     */
    public boolean isMasterDegree() {
        return degree.contains("석사") || degree.equalsIgnoreCase("Master");
    }

    /**
     * 박사 학위인지 확인
     *
     * @return 박사 학위면 true
     */
    public boolean isDoctorDegree() {
        return degree.contains("박사") || degree.equalsIgnoreCase("Doctor") || degree.equalsIgnoreCase("PhD");
    }

    /**
     * 특정 학점 이상인지 확인
     *
     * @param minimumGpa 최소 학점
     * @return 최소 학점 이상이면 true (학점이 없으면 false)
     */
    public boolean hasGpaAtLeast(double minimumGpa) {
        return gpa != null && gpa >= minimumGpa;
    }

    /**
     * 전공이 일치하는지 확인 (대소문자 구분 없음, 부분 일치)
     *
     * @param majorKeyword 전공 키워드
     * @return 일치하면 true
     */
    public boolean isMajorRelatedTo(String majorKeyword) {
        return major.toLowerCase().contains(majorKeyword.toLowerCase());
    }

    /**
     * 학교명이 일치하는지 확인 (대소문자 구분 없음)
     *
     * @param schoolName 비교할 학교명
     * @return 일치하면 true
     */
    public boolean isSchool(String schoolName) {
        return this.schoolName.equalsIgnoreCase(schoolName);
    }

    /**
     * 특정 날짜 이후 졸업했는지 확인
     *
     * @param date 기준 날짜
     * @return 이후에 졸업했으면 true
     */
    public boolean graduatedAfter(LocalDate date) {
        return graduationDate.isAfter(date);
    }

    /**
     * 특정 날짜 이전에 졸업했는지 확인
     *
     * @param date 기준 날짜
     * @return 이전에 졸업했으면 true
     */
    public boolean graduatedBefore(LocalDate date) {
        return graduationDate.isBefore(date);
    }

    /**
     * 학점 업데이트
     *
     * @param gpa 새 학점
     * @return 새로운 Education 인스턴스
     */
    public Education withGpa(Double gpa) {
        return new Education(this.schoolName, this.degree, this.major, this.graduationDate, gpa);
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] {
            schoolName.toLowerCase(),
            degree.toLowerCase(),
            major.toLowerCase(),
            graduationDate
        };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(schoolName).append(" - ").append(degree);
        sb.append(" (").append(major).append(")");
        sb.append(" [").append(graduationDate).append("]");
        if (gpa != null) {
            sb.append(" GPA: ").append(String.format("%.2f", gpa));
        }
        return sb.toString();
    }
}
