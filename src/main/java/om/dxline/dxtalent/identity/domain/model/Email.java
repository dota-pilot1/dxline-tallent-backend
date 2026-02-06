package om.dxline.dxtalent.identity.domain.model;

import java.util.regex.Pattern;
import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * Email 값 객체 (Email Value Object)
 *
 * 이메일 주소를 표현하는 값 객체입니다.
 *
 * DDD에서 원시 타입(String) 대신 값 객체를 사용하는 이유:
 * 1. 유효성 검증 중앙화: 생성 시점에 한 번만 검증
 * 2. 도메인 개념 명확화: "문자열"이 아닌 "이메일"
 * 3. 비즈니스 로직 캡슐화: 도메인 추출, 마스킹 등
 * 4. 타입 안정성: Email과 다른 String을 혼동할 수 없음
 *
 * 원시 타입 집착(Primitive Obsession) 안티패턴 해결:
 * <pre>
 * // ❌ 나쁜 예 - 검증 로직이 여러 곳에 중복
 * public void sendEmail(String email) {
 *     if (email == null || !email.contains("@")) {
 *         throw new IllegalArgumentException("Invalid email");
 *     }
 *     // ...
 * }
 *
 * public void saveUser(String email) {
 *     if (email == null || !email.contains("@")) {  // 중복!
 *         throw new IllegalArgumentException("Invalid email");
 *     }
 *     // ...
 * }
 *
 * // ✅ 좋은 예 - 값 객체 사용
 * public void sendEmail(Email email) {
 *     // Email 생성 시 이미 검증됨!
 * }
 *
 * public void saveUser(Email email) {
 *     // 검증 로직 중복 없음!
 * }
 * </pre>
 */
public class Email extends BaseValueObject {

    /**
     * 이메일 정규식 패턴
     * RFC 5322 표준의 단순화된 버전
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 이메일 최대 길이 (RFC 5321 표준)
     */
    private static final int MAX_LENGTH = 255;

    /**
     * 로컬 파트 최대 길이 (@ 앞부분)
     */
    private static final int MAX_LOCAL_LENGTH = 64;

    /**
     * 도메인 최대 길이 (@ 뒷부분)
     */
    private static final int MAX_DOMAIN_LENGTH = 255;

    /**
     * 정규화된 이메일 값 (소문자, 공백 제거)
     */
    private final String value;

    /**
     * Email 생성자
     *
     * @param value 이메일 주소 문자열
     * @throws IllegalArgumentException 유효하지 않은 이메일 형식인 경우
     */
    public Email(String value) {
        this.value = normalize(validate(value));
    }

    /**
     * 이메일 유효성 검증
     *
     * @param value 검증할 이메일 값
     * @return 검증된 이메일 값
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private String validate(String value) {
        // null 및 빈 값 체크
        requireNonEmpty(value, "Email");

        String trimmed = value.trim();

        // 길이 체크
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("이메일은 %d자를 초과할 수 없습니다", MAX_LENGTH)
            );
        }

        // 정규식 패턴 검증
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다"
            );
        }

        // @ 기호 존재 여부 (정규식으로 이미 검증되지만 명확한 메시지를 위해)
        if (!trimmed.contains("@")) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다: @ 기호가 필요합니다"
            );
        }

        // 로컬 파트와 도메인 분리
        String[] parts = trimmed.split("@", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다"
            );
        }

        String localPart = parts[0];
        String domain = parts[1];

        // 로컬 파트 검증
        if (localPart.isEmpty()) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다: @ 앞부분이 비어있습니다"
            );
        }

        if (localPart.length() > MAX_LOCAL_LENGTH) {
            throw new IllegalArgumentException(
                String.format(
                    "이메일 로컬 파트는 %d자를 초과할 수 없습니다",
                    MAX_LOCAL_LENGTH
                )
            );
        }

        // 도메인 검증
        if (domain.isEmpty()) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다: @ 뒷부분이 비어있습니다"
            );
        }

        if (domain.length() > MAX_DOMAIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format(
                    "이메일 도메인은 %d자를 초과할 수 없습니다",
                    MAX_DOMAIN_LENGTH
                )
            );
        }

        // 도메인에 최소 하나의 점(.)이 있는지 확인
        if (!domain.contains(".")) {
            throw new IllegalArgumentException(
                "유효하지 않은 이메일 형식입니다: 도메인에 점(.)이 필요합니다"
            );
        }

        return trimmed;
    }

    /**
     * 이메일 정규화 (소문자 변환, 공백 제거)
     *
     * @param value 정규화할 이메일
     * @return 정규화된 이메일
     */
    private String normalize(String value) {
        return value.toLowerCase().trim();
    }

    /**
     * 이메일 값 반환
     *
     * @return 정규화된 이메일 문자열
     */
    public String getValue() {
        return value;
    }

    /**
     * 도메인 추출 (예: "user@example.com" -> "example.com")
     *
     * @return 이메일의 도메인 부분
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }

    /**
     * 로컬 파트 추출 (예: "user@example.com" -> "user")
     *
     * @return 이메일의 로컬 파트 (@ 앞부분)
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }

    /**
     * 특정 도메인인지 확인
     *
     * @param domain 확인할 도메인
     * @return 해당 도메인이면 true
     */
    public boolean isFromDomain(String domain) {
        if (domain == null) {
            return false;
        }
        return getDomain().equalsIgnoreCase(domain);
    }

    /**
     * 특정 도메인 목록 중 하나인지 확인
     *
     * @param domains 확인할 도메인 목록
     * @return 목록 중 하나면 true
     */
    public boolean isFromDomains(String... domains) {
        if (domains == null || domains.length == 0) {
            return false;
        }
        String emailDomain = getDomain();
        for (String domain : domains) {
            if (emailDomain.equalsIgnoreCase(domain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 마스킹된 이메일 반환 (예: "user@example.com" -> "u***@example.com")
     * 개인정보 보호를 위해 로그나 화면 표시 시 사용
     *
     * @return 마스킹된 이메일 문자열
     */
    public String getMasked() {
        String localPart = getLocalPart();
        String domain = getDomain();

        if (localPart.length() <= 1) {
            return localPart + "***@" + domain;
        }

        // 첫 글자만 표시하고 나머지는 ***
        return localPart.charAt(0) + "***@" + domain;
    }

    /**
     * 더 자세한 마스킹 (예: "user@example.com" -> "us**@ex*****.com")
     *
     * @return 자세히 마스킹된 이메일
     */
    public String getMaskedDetailed() {
        String localPart = getLocalPart();
        String domain = getDomain();

        // 로컬 파트 마스킹
        String maskedLocal;
        if (localPart.length() <= 2) {
            maskedLocal = localPart.charAt(0) + "**";
        } else {
            maskedLocal = localPart.substring(0, 2) + "**";
        }

        // 도메인 마스킹
        String maskedDomain;
        int dotIndex = domain.indexOf('.');
        if (dotIndex > 0) {
            String domainName = domain.substring(0, dotIndex);
            String extension = domain.substring(dotIndex);

            if (domainName.length() <= 2) {
                maskedDomain = domainName.charAt(0) + "*****" + extension;
            } else {
                maskedDomain = domainName.substring(0, 2) + "*****" + extension;
            }
        } else {
            maskedDomain = domain.charAt(0) + "*****";
        }

        return maskedLocal + "@" + maskedDomain;
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value };
    }

    /**
     * 문자열 표현 (이메일 값 그대로 반환)
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * 편의 메서드: 문자열로부터 Email 생성
     *
     * @param value 이메일 문자열
     * @return Email 인스턴스
     */
    public static Email of(String value) {
        return new Email(value);
    }
}
