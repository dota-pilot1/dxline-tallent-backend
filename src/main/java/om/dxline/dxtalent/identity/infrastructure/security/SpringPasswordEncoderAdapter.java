package om.dxline.dxtalent.identity.infrastructure.security;

import om.dxline.dxtalent.identity.domain.model.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring PasswordEncoder 어댑터
 *
 * 도메인의 Password.PasswordEncoder 인터페이스를
 * Spring Security의 PasswordEncoder로 어댑팅합니다.
 *
 * 이것이 어댑터 패턴(Adapter Pattern)입니다:
 * - 도메인 인터페이스 (Password.PasswordEncoder)
 * - 인프라 구현 (Spring Security PasswordEncoder)
 * - 어댑터 (이 클래스)
 *
 * 장점:
 * 1. 도메인이 Spring에 의존하지 않음
 * 2. 테스트 시 Mock으로 교체 가능
 * 3. 암호화 구현 기술 변경 용이
 */
@Component
public class SpringPasswordEncoderAdapter implements Password.PasswordEncoder {

    private final PasswordEncoder delegate;

    /**
     * 생성자
     *
     * @param delegate Spring Security의 PasswordEncoder
     */
    public SpringPasswordEncoderAdapter(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    /**
     * 원본 비밀번호를 암호화
     *
     * @param rawPassword 원본 비밀번호
     * @return 암호화된 비밀번호
     */
    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    /**
     * 원본 비밀번호와 암호화된 비밀번호가 일치하는지 확인
     *
     * @param rawPassword 원본 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치하면 true
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
