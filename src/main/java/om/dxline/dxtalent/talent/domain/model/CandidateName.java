package om.dxline.dxtalent.talent.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 지원자 이름 값 객체 (CandidateName Value Object)
 *
 * 이력서에 기재된 지원자의 이름을 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 길이: 2-100자
 * - 한글, 영문, 공백만 허용
 * - 앞뒤 공백 자동 제거
 * - 연속된 공백 불가
 *
 * 사용 예시:
 * <pre>
 * CandidateName name = new CandidateName("홍길동");
 * CandidateName name2 = new CandidateName("John Doe");
 * String fullName = name.getValue();
 * </pre>
 */
public class CandidateName extends BaseValueObject {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    // 한글, 영문, 공백만 허용 (숫자, 특수문자 제외)
    private static final String VALID_NAME_PATTERN = "^[가-힣a-zA-Z\\s]+$";

    private final String value;

    /**
     * 지원자 이름 생성
     *
     * @param value 이름
     * @throws IllegalArgumentException 유효하지 않은 이름인 경우
     */
    public CandidateName(String value) {
        this.value = requireNonNull(value, "지원자 이름").trim();
        validateName(this.value);
    }

    /**
     * 이름 검증
     */
    private void validateName(String value) {
        // 길이 검증
        validate(
            value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH,
            String.format("이름은 %d자 이상 %d자 이하여야 합니다", MIN_LENGTH, MAX_LENGTH)
        );

        // 공백만 있는지 검증
        validate(
            !value.trim().isEmpty(),
            "이름은 공백만으로 구성될 수 없습니다"
        );

        // 연속된 공백 검증
        validate(
            !value.contains("  "),
            "이름에 연속된 공백을 사용할 수 없습니다"
        );

        // 문자 종류 검증 (한글, 영문, 공백만)
        validate(
            value.matches(VALID_NAME_PATTERN),
            "이름은 한글, 영문, 공백만 사용할 수 있습니다"
        );

        // 숫자 포함 여부 확인
        validate(
            !value.matches(".*\\d.*"),
            "이름에 숫자를 사용할 수 없습니다"
        );
    }

    /**
     * 지원자 이름 생성 정적 팩토리 메서드
     *
     * @param value 이름
     * @return CandidateName 인스턴스
     */
    public static CandidateName of(String value) {
        return new CandidateName(value);
    }

    /**
     * 이름 값 반환
     *
     * @return 이름 문자열
     */
    public String getValue() {
        return value;
    }

    /**
     * 성과 이름을 분리 (한글 이름 기준)
     * 첫 글자를 성으로, 나머지를 이름으로 간주
     *
     * @return [성, 이름] 배열
     */
    public String[] splitKoreanName() {
        if (value.length() < 2) {
            return new String[] { value, "" };
        }

        // 한글 이름인 경우
        if (value.matches("^[가-힣]+$")) {
            return new String[] {
                value.substring(0, 1),
                value.substring(1)
            };
        }

        // 영문 이름인 경우 (띄어쓰기 기준)
        if (value.contains(" ")) {
            String[] parts = value.split(" ", 2);
            return parts.length == 2 ? parts : new String[] { value, "" };
        }

        return new String[] { value, "" };
    }

    /**
     * 이름의 이니셜 반환
     *
     * @return 이니셜 (예: "홍길동" -> "홍", "John Doe" -> "JD")
     */
    public String getInitials() {
        if (value.matches("^[가-힣]+$")) {
            // 한글 이름: 첫 글자만
            return value.substring(0, 1);
        } else if (value.contains(" ")) {
            // 영문 이름: 각 단어의 첫 글자
            String[] parts = value.split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    initials.append(part.charAt(0));
                }
            }
            return initials.toString().toUpperCase();
        } else {
            // 단일 단어: 첫 글자
            return value.substring(0, 1).toUpperCase();
        }
    }

    /**
     * 한글 이름인지 확인
     *
     * @return 한글 이름이면 true
     */
    public boolean isKorean() {
        return value.matches("^[가-힣\\s]+$");
    }

    /**
     * 영문 이름인지 확인
     *
     * @return 영문 이름이면 true
     */
    public boolean isEnglish() {
        return value.matches("^[a-zA-Z\\s]+$");
    }

    /**
     * 이름이 일치하는지 확인 (대소문자 구분 없음)
     *
     * @param name 비교할 이름
     * @return 일치하면 true
     */
    public boolean matches(String name) {
        return this.value.equalsIgnoreCase(name.trim());
    }

    /**
     * 이름에 특정 키워드가 포함되어 있는지 확인 (대소문자 구분 없음)
     *
     * @param keyword 검색 키워드
     * @return 포함되어 있으면 true
     */
    public boolean contains(String keyword) {
        return this.value.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * 이름 길이 반환
     *
     * @return 글자 수
     */
    public int getLength() {
        return value.length();
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     * 대소문자 구분 없이 비교
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value.toLowerCase() };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return value;
    }
}
