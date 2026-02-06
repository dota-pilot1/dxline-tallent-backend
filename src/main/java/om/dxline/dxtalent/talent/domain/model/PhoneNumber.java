package om.dxline.dxtalent.talent.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 전화번호 값 객체 (PhoneNumber Value Object)
 *
 * 이력서에 기재된 전화번호를 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 한국 전화번호 형식만 지원
 * - 휴대폰: 010-XXXX-XXXX
 * - 일반 전화: 02-XXX-XXXX, 031-XXX-XXXX 등
 * - 하이픈(-) 자동 추가
 * - 숫자만 입력 가능
 *
 * 사용 예시:
 * <pre>
 * PhoneNumber phone = new PhoneNumber("01012345678");
 * PhoneNumber phone2 = new PhoneNumber("010-1234-5678");
 * String formatted = phone.getFormatted(); // "010-1234-5678"
 * boolean isMobile = phone.isMobile(); // true
 * </pre>
 */
public class PhoneNumber extends BaseValueObject {

    private static final String MOBILE_PATTERN = "^010\\d{8}$"; // 010으로 시작하는 11자리
    private static final String SEOUL_PATTERN = "^02\\d{7,8}$"; // 02로 시작하는 9-10자리
    private static final String LOCAL_PATTERN = "^0(3[1-3]|4[1-4]|5[1-5]|6[1-4])\\d{7,8}$"; // 지역번호

    private final String value; // 숫자만 저장 (하이픈 제거)

    /**
     * 전화번호 생성
     *
     * @param value 전화번호 (하이픈 포함 또는 미포함)
     * @throws IllegalArgumentException 유효하지 않은 전화번호인 경우
     */
    public PhoneNumber(String value) {
        String cleaned = cleanPhoneNumber(requireNonNull(value, "전화번호"));
        this.value = cleaned;
        validatePhoneNumber(cleaned);
    }

    /**
     * 전화번호에서 숫자만 추출
     */
    private String cleanPhoneNumber(String phoneNumber) {
        // 하이픈, 공백, 괄호 등 제거
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    /**
     * 전화번호 검증
     */
    private void validatePhoneNumber(String cleaned) {
        // 빈 문자열 검증
        validate(
            !cleaned.isEmpty(),
            "전화번호는 비어있을 수 없습니다"
        );

        // 숫자만 있는지 검증
        validate(
            cleaned.matches("^[0-9]+$"),
            "전화번호는 숫자만 입력 가능합니다"
        );

        // 0으로 시작하는지 검증
        validate(
            cleaned.startsWith("0"),
            "전화번호는 0으로 시작해야 합니다"
        );

        // 길이 검증 (최소 9자리, 최대 11자리)
        validate(
            cleaned.length() >= 9 && cleaned.length() <= 11,
            "전화번호는 9-11자리여야 합니다"
        );

        // 유효한 형식인지 검증
        boolean isValid = cleaned.matches(MOBILE_PATTERN) ||
                         cleaned.matches(SEOUL_PATTERN) ||
                         cleaned.matches(LOCAL_PATTERN);

        validate(
            isValid,
            "유효하지 않은 전화번호 형식입니다"
        );
    }

    /**
     * 전화번호 생성 정적 팩토리 메서드
     *
     * @param value 전화번호
     * @return PhoneNumber 인스턴스
     */
    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    /**
     * 전화번호 값 반환 (숫자만)
     *
     * @return 전화번호 (숫자만)
     */
    public String getValue() {
        return value;
    }

    /**
     * 형식화된 전화번호 반환 (하이픈 포함)
     *
     * @return 형식화된 전화번호
     */
    public String getFormatted() {
        if (isMobile()) {
            // 010-XXXX-XXXX
            return String.format("%s-%s-%s",
                value.substring(0, 3),
                value.substring(3, 7),
                value.substring(7));
        } else if (isSeoul()) {
            // 02-XXX-XXXX 또는 02-XXXX-XXXX
            if (value.length() == 9) {
                return String.format("%s-%s-%s",
                    value.substring(0, 2),
                    value.substring(2, 5),
                    value.substring(5));
            } else {
                return String.format("%s-%s-%s",
                    value.substring(0, 2),
                    value.substring(2, 6),
                    value.substring(6));
            }
        } else {
            // 0XX-XXX-XXXX 또는 0XX-XXXX-XXXX
            if (value.length() == 10) {
                return String.format("%s-%s-%s",
                    value.substring(0, 3),
                    value.substring(3, 6),
                    value.substring(6));
            } else {
                return String.format("%s-%s-%s",
                    value.substring(0, 3),
                    value.substring(3, 7),
                    value.substring(7));
            }
        }
    }

    /**
     * 휴대폰 번호인지 확인
     *
     * @return 휴대폰 번호면 true
     */
    public boolean isMobile() {
        return value.matches(MOBILE_PATTERN);
    }

    /**
     * 서울 지역 번호인지 확인
     *
     * @return 서울 번호면 true
     */
    public boolean isSeoul() {
        return value.matches(SEOUL_PATTERN);
    }

    /**
     * 지역 번호인지 확인
     *
     * @return 지역 번호면 true
     */
    public boolean isLocal() {
        return value.matches(LOCAL_PATTERN);
    }

    /**
     * 일반 전화(유선)인지 확인
     *
     * @return 유선 전화면 true
     */
    public boolean isLandline() {
        return isSeoul() || isLocal();
    }

    /**
     * 지역번호 반환
     *
     * @return 지역번호 (예: "010", "02", "031")
     */
    public String getAreaCode() {
        if (isMobile()) {
            return value.substring(0, 3);
        } else if (isSeoul()) {
            return value.substring(0, 2);
        } else {
            return value.substring(0, 3);
        }
    }

    /**
     * 전화번호가 일치하는지 확인
     *
     * @param phoneNumber 비교할 전화번호 (형식 무관)
     * @return 일치하면 true
     */
    public boolean matches(String phoneNumber) {
        String cleaned = cleanPhoneNumber(phoneNumber);
        return this.value.equals(cleaned);
    }

    /**
     * 마스킹된 전화번호 반환 (개인정보 보호)
     *
     * @return 마스킹된 전화번호 (예: "010-****-5678")
     */
    public String getMasked() {
        String formatted = getFormatted();
        String[] parts = formatted.split("-");

        if (parts.length == 3) {
            return String.format("%s-****-%s", parts[0], parts[2]);
        }

        return formatted;
    }

    /**
     * 국제 전화번호 형식으로 변환
     *
     * @return +82 형식의 전화번호
     */
    public String toInternationalFormat() {
        // 0을 제거하고 +82 추가
        String withoutZero = value.substring(1);
        return "+82" + withoutZero;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value };
    }

    /**
     * 문자열 표현 (형식화된 전화번호)
     */
    @Override
    public String toString() {
        return getFormatted();
    }
}
