package om.dxline.dxtalent.talent.domain.model;

import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 연락처 정보 값 객체 (ContactInfo Value Object)
 *
 * 이력서에 기재된 지원자의 연락처 정보를 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 전화번호: 필수
 * - 이메일: 필수
 * - 주소: 선택
 * - 최소한 전화번호와 이메일 중 하나는 반드시 있어야 함
 *
 * 사용 예시:
 * <pre>
 * ContactInfo contact = ContactInfo.of(
 *     new PhoneNumber("010-1234-5678"),
 *     new Email("hong@example.com"),
 *     "서울특별시 강남구"
 * );
 * boolean complete = contact.hasCompleteInfo(); // true
 * </pre>
 */
public class ContactInfo extends BaseValueObject {

    private static final int MAX_ADDRESS_LENGTH = 200;

    private final PhoneNumber phoneNumber;
    private final Email email;
    private final String address; // 선택적 (nullable)

    /**
     * 연락처 정보 생성
     *
     * @param phoneNumber 전화번호
     * @param email 이메일
     * @param address 주소 (선택)
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public ContactInfo(PhoneNumber phoneNumber, Email email, String address) {
        this.phoneNumber = phoneNumber; // nullable
        this.email = email; // nullable
        this.address = address != null ? address.trim() : null;

        validateContactInfo();
    }

    /**
     * 연락처 정보 검증
     */
    private void validateContactInfo() {
        // 최소한 전화번호나 이메일 중 하나는 필수
        validate(
            phoneNumber != null || email != null,
            "전화번호 또는 이메일 중 최소 하나는 필수입니다"
        );

        // 주소 길이 검증 (있는 경우)
        if (address != null && !address.isEmpty()) {
            validate(
                address.length() <= MAX_ADDRESS_LENGTH,
                String.format("주소는 최대 %d자까지 입력 가능합니다", MAX_ADDRESS_LENGTH)
            );
        }
    }

    /**
     * 연락처 정보 생성 정적 팩토리 메서드 (주소 포함)
     *
     * @param phoneNumber 전화번호
     * @param email 이메일
     * @param address 주소
     * @return ContactInfo 인스턴스
     */
    public static ContactInfo of(PhoneNumber phoneNumber, Email email, String address) {
        return new ContactInfo(phoneNumber, email, address);
    }

    /**
     * 연락처 정보 생성 정적 팩토리 메서드 (주소 없이)
     *
     * @param phoneNumber 전화번호
     * @param email 이메일
     * @return ContactInfo 인스턴스
     */
    public static ContactInfo of(PhoneNumber phoneNumber, Email email) {
        return new ContactInfo(phoneNumber, email, null);
    }

    /**
     * 전화번호만으로 연락처 정보 생성
     *
     * @param phoneNumber 전화번호
     * @return ContactInfo 인스턴스
     */
    public static ContactInfo withPhoneOnly(PhoneNumber phoneNumber) {
        return new ContactInfo(phoneNumber, null, null);
    }

    /**
     * 이메일만으로 연락처 정보 생성
     *
     * @param email 이메일
     * @return ContactInfo 인스턴스
     */
    public static ContactInfo withEmailOnly(Email email) {
        return new ContactInfo(null, email, null);
    }

    /**
     * 전화번호 반환
     *
     * @return 전화번호 (없으면 null)
     */
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 이메일 반환
     *
     * @return 이메일 (없으면 null)
     */
    public Email getEmail() {
        return email;
    }

    /**
     * 주소 반환
     *
     * @return 주소 (없으면 null)
     */
    public String getAddress() {
        return address;
    }

    /**
     * 전화번호가 있는지 확인
     *
     * @return 있으면 true
     */
    public boolean hasPhoneNumber() {
        return phoneNumber != null;
    }

    /**
     * 이메일이 있는지 확인
     *
     * @return 있으면 true
     */
    public boolean hasEmail() {
        return email != null;
    }

    /**
     * 주소가 있는지 확인
     *
     * @return 있으면 true
     */
    public boolean hasAddress() {
        return address != null && !address.isEmpty();
    }

    /**
     * 완전한 정보를 가지고 있는지 확인
     * (전화번호, 이메일, 주소 모두 있음)
     *
     * @return 모두 있으면 true
     */
    public boolean hasCompleteInfo() {
        return hasPhoneNumber() && hasEmail() && hasAddress();
    }

    /**
     * 필수 정보만 있는지 확인
     * (전화번호와 이메일)
     *
     * @return 전화번호와 이메일이 모두 있으면 true
     */
    public boolean hasRequiredInfo() {
        return hasPhoneNumber() && hasEmail();
    }

    /**
     * 전화번호 업데이트
     *
     * @param phoneNumber 새 전화번호
     * @return 새로운 ContactInfo 인스턴스
     */
    public ContactInfo withPhoneNumber(PhoneNumber phoneNumber) {
        return new ContactInfo(phoneNumber, this.email, this.address);
    }

    /**
     * 이메일 업데이트
     *
     * @param email 새 이메일
     * @return 새로운 ContactInfo 인스턴스
     */
    public ContactInfo withEmail(Email email) {
        return new ContactInfo(this.phoneNumber, email, this.address);
    }

    /**
     * 주소 업데이트
     *
     * @param address 새 주소
     * @return 새로운 ContactInfo 인스턴스
     */
    public ContactInfo withAddress(String address) {
        return new ContactInfo(this.phoneNumber, this.email, address);
    }

    /**
     * 주소 제거
     *
     * @return 새로운 ContactInfo 인스턴스 (주소 없음)
     */
    public ContactInfo withoutAddress() {
        return new ContactInfo(this.phoneNumber, this.email, null);
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] {
            phoneNumber,
            email,
            address != null ? address.toLowerCase() : null
        };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (hasPhoneNumber()) {
            sb.append("전화: ").append(phoneNumber);
        }

        if (hasEmail()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("이메일: ").append(email.getValue());
        }

        if (hasAddress()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("주소: ").append(address);
        }

        return sb.length() > 0 ? sb.toString() : "연락처 정보 없음";
    }
}
