package om.dxline.dxtalent.api.auth.service;

import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.auth.dto.LoginRequest;
import om.dxline.dxtalent.api.auth.dto.LoginResponse;
import om.dxline.dxtalent.api.auth.dto.SignupRequest;
import om.dxline.dxtalent.domain.user.entity.Role;
import om.dxline.dxtalent.domain.user.entity.User;
import om.dxline.dxtalent.domain.user.repository.UserRepository;
import om.dxline.dxtalent.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long expiration;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .role(Role.USER)
            .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다"
                )
            );

        if (
            !passwordEncoder.matches(request.getPassword(), user.getPassword())
        ) {
            throw new IllegalArgumentException(
                "이메일 또는 비밀번호가 올바르지 않습니다"
            );
        }

        String token = jwtTokenProvider.createToken(
            user.getEmail(),
            user.getRole().name()
        );
        return LoginResponse.of(
            token,
            expiration / 1000,
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().name()
        );
    }
}
