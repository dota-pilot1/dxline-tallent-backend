package om.dxline.dxtalent.identity.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.identity.application.dto.LoginCommand;
import om.dxline.dxtalent.identity.application.dto.LoginResult;
import om.dxline.dxtalent.identity.application.dto.SignupCommand;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.model.Password;
import om.dxline.dxtalent.identity.domain.model.User;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.identity.domain.model.UserName;
import om.dxline.dxtalent.identity.domain.repository.UserRepository;
import om.dxline.dxtalent.security.jwt.JwtTokenProvider;
import om.dxline.dxtalent.shared.domain.event.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthApplicationService - DDD 방식의 Application Service
 *
 * Application Service의 역할 (DDD):
 * 1. 트랜잭션 관리
 * 2. 도메인 객체 조율 (Orchestration)
 * 3. DTO <-> 도메인 모델 변환
 * 4. 도메인 이벤트 발행
 * 5. 인프라 서비스 호출
 *
 * 비즈니스 로직은 도메인 모델(User)에 있습니다!
 *
 * Before (Transaction Script):
 * <pre>
 * public void signup(SignupRequest request) {
 *     // 검증 로직
 *     if (request.getEmail() == null) { ... }
 *
 *     // 비즈니스 로직
 *     User user = User.builder()
 *         .email(request.getEmail())
 *         .password(encoder.encode(request.getPassword()))
 *         .role(Role.USER)  // 비즈니스 규칙!
 *         .build();
 *
 *     repository.save(user);
 * }
 * </pre>
 *
 * After (DDD):
 * <pre>
 * public void signup(SignupCommand command) {
 *     // 1. DTO -> 값 객체 변환 (검증 자동)
 *     Email email = new Email(command.email());
 *     Password password = Password.fromRaw(command.password(), encoder);
 *     UserName name = new UserName(command.name());
 *
 *     // 2. 중복 체크
 *     if (repository.existsByEmail(email)) { ... }
 *
 *     // 3. 도메인 로직 호출 (비즈니스 규칙은 여기에!)
 *     User user = User.register(email, password, name);
 *
 *     // 4. 저장
 *     repository.save(user);
 *
 *     // 5. 이벤트 발행
 *     eventPublisher.publishAll(user.getDomainEvents());
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final Password.PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainEventPublisher eventPublisher;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * 회원가입
     *
     * Application Service의 역할:
     * - Command 검증 및 변환
     * - 중복 체크
     * - 도메인 객체 생성 호출
     * - 영속화
     * - 이벤트 발행
     *
     * @param command 회원가입 커맨드
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    public void signup(SignupCommand command) {
        log.info("회원가입 시작: email={}", command.email());

        // 1. Command 검증
        command.validate();

        // 2. DTO -> 값 객체 변환 (유효성 검증 자동)
        Email email = new Email(command.email());
        Password password = Password.fromRaw(command.password(), passwordEncoder);
        UserName name = new UserName(command.name());

        // 3. 중복 체크 (비즈니스 규칙)
        if (userRepository.existsByEmail(email)) {
            log.warn("회원가입 실패: 이메일 중복 - {}", email.getValue());
            throw new DuplicateEmailException(email);
        }

        // 4. 도메인 객체 생성 (비즈니스 로직은 도메인에!)
        User user = User.register(email, password, name);
        log.debug("사용자 도메인 모델 생성: {}", user);

        // 5. 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}",
                 savedUser.getId().getValue(),
                 savedUser.getEmail().getValue());

        // 6. 도메인 이벤트 발행
        publishDomainEvents(savedUser);
    }

    /**
     * 로그인
     *
     * Application Service의 역할:
     * - Command 검증 및 변환
     * - 사용자 조회
     * - 도메인 인증 로직 호출
     * - JWT 토큰 생성
     * - Result 객체 반환
     *
     * @param command 로그인 커맨드
     * @return 로그인 결과 (JWT 토큰 포함)
     * @throws InvalidCredentialsException 인증 실패 시
     */
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        log.info("로그인 시도: email={}", command.email());

        // 1. Command 검증
        command.validate();

        // 2. DTO -> 값 객체 변환
        Email email = new Email(command.email());

        // 3. 사용자 조회
        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> {
                log.warn("로그인 실패: 사용자 없음 - {}", email.getValue());
                return new InvalidCredentialsException();
            });

        // 4. 도메인 인증 로직 호출 (비즈니스 로직은 도메인에!)
        boolean authenticated = user.authenticate(command.password(), passwordEncoder);

        if (!authenticated) {
            log.warn("로그인 실패: 비밀번호 불일치 - {}", email.getValue());
            throw new InvalidCredentialsException();
        }

        // 5. 저장 (마지막 로그인 시각 업데이트)
        userRepository.save(user);
        log.info("로그인 성공: userId={}, email={}",
                 user.getId().getValue(),
                 user.getEmail().getValue());

        // 6. 도메인 이벤트 발행
        publishDomainEvents(user);

        // 7. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(
            user.getEmail().getValue(),
            user.getRole().name()
        );

        // 8. Result 객체 반환
        return LoginResult.from(user, token, jwtExpiration / 1000);
    }

    /**
     * 비밀번호 변경
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않는 경우
     */
    public void changePassword(
        Long userId,
        String currentPassword,
        String newPassword
    ) {
        log.info("비밀번호 변경 시도: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository
            .findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 새 비밀번호 값 객체 생성
        Password newPasswordObj = Password.fromRaw(newPassword, passwordEncoder);

        // 3. 도메인 로직 호출
        user.changePassword(currentPassword, newPasswordObj, passwordEncoder);

        // 4. 저장
        userRepository.save(user);
        log.info("비밀번호 변경 완료: userId={}", userId);

        // 5. 이벤트 발행
        publishDomainEvents(user);
    }

    /**
     * 사용자 비활성화
     *
     * @param userId 비활성화할 사용자 ID
     * @param reason 비활성화 사유
     * @param deactivatedBy 비활성화를 수행한 사용자 ID
     */
    public void deactivateUser(Long userId, String reason, Long deactivatedBy) {
        log.info("사용자 비활성화: userId={}, by={}", userId, deactivatedBy);

        // 1. 사용자 조회
        User user = userRepository
            .findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 도메인 로직 호출
        user.deactivate(reason, UserId.of(deactivatedBy));

        // 3. 저장
        userRepository.save(user);
        log.info("사용자 비활성화 완료: userId={}", userId);

        // 4. 이벤트 발행
        publishDomainEvents(user);
    }

    /**
     * 사용자 활성화 (재활성화)
     *
     * @param userId 활성화할 사용자 ID
     */
    public void activateUser(Long userId) {
        log.info("사용자 재활성화: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository
            .findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 도메인 로직 호출
        user.activate();

        // 3. 저장
        userRepository.save(user);
        log.info("사용자 재활성화 완료: userId={}", userId);

        // 4. 이벤트 발행
        publishDomainEvents(user);
    }

    // ============================================================
    // Private 헬퍼 메서드
    // ============================================================

    /**
     * 도메인 이벤트 발행 및 초기화
     *
     * @param user 사용자 애그리게이트
     */
    private void publishDomainEvents(User user) {
        if (user.hasDomainEvents()) {
            log.debug("도메인 이벤트 발행: {} 개", user.getDomainEvents().size());
            eventPublisher.publishAll(user.getDomainEvents());
            user.clearDomainEvents();
        }
    }

    // ============================================================
    // 예외 클래스
    // ============================================================

    /**
     * 이메일 중복 예외
     */
    public static class DuplicateEmailException extends RuntimeException {
        private final Email email;

        public DuplicateEmailException(Email email) {
            super("이미 사용 중인 이메일입니다: " + email.getValue());
            this.email = email;
        }

        public Email getEmail() {
            return email;
        }
    }

    /**
     * 인증 실패 예외
     */
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }

    /**
     * 사용자 없음 예외
     */
    public static class UserNotFoundException extends RuntimeException {
        private final Long userId;

        public UserNotFoundException(Long userId) {
            super("사용자를 찾을 수 없습니다: " + userId);
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
