package om.dxline.dxtalent.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import om.dxline.dxtalent.identity.application.dto.LoginCommand;
import om.dxline.dxtalent.identity.application.dto.LoginResult;
import om.dxline.dxtalent.identity.application.dto.SignupCommand;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthApplicationService 통합 테스트
 *
 * Spring Boot 컨텍스트를 로드하여 실제 의존성들과 함께 테스트합니다.
 * - 실제 Repository (메모리 DB 사용)
 * - 실제 PasswordEncoder
 * - 실제 JwtTokenProvider
 * - 실제 DomainEventPublisher
 *
 * 이것은 통합 테스트(Integration Test)입니다:
 * - 여러 레이어가 함께 동작하는지 검증
 * - 실제 의존성 사용
 * - 도메인 로직 + Application 로직 + Infrastructure 통합
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthApplicationService 통합 테스트")
class AuthApplicationServiceIntegrationTest {

    @Autowired
    private AuthApplicationService authService;

    @Autowired
    @Qualifier("identityUserRepository")
    private UserRepository userRepository;

    private SignupCommand testSignupCommand;
    private LoginCommand testLoginCommand;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        testSignupCommand = new SignupCommand(
            "test@example.com",
            "password123",
            "홍길동"
        );

        testLoginCommand = new LoginCommand(
            "test@example.com",
            "password123"
        );
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 정리 (필요시)
        // @Transactional이 자동으로 롤백하므로 일반적으로 불필요
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void shouldSignupSuccessfully() {
            // when
            authService.signup(testSignupCommand);

            // then
            boolean exists = userRepository.existsByEmail(
                new Email(testSignupCommand.email())
            );
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 실패")
        void shouldFailWhenEmailIsDuplicate() {
            // given
            authService.signup(testSignupCommand);

            // when & then
            assertThatThrownBy(() -> authService.signup(testSignupCommand))
                .isInstanceOf(AuthApplicationService.DuplicateEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
        }

        @Test
        @DisplayName("유효하지 않은 이메일로 회원가입 실패")
        void shouldFailWhenEmailIsInvalid() {
            // given
            SignupCommand invalidCommand = new SignupCommand(
                "invalid-email",
                "password123",
                "홍길동"
            );

            // when & then
            assertThatThrownBy(() -> authService.signup(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일");
        }

        @Test
        @DisplayName("비밀번호 규칙 위반 시 회원가입 실패")
        void shouldFailWhenPasswordIsInvalid() {
            // given
            SignupCommand invalidCommand = new SignupCommand(
                "test@example.com",
                "short", // 8자 미만
                "홍길동"
            );

            // when & then
            assertThatThrownBy(() -> authService.signup(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호");
        }

        @Test
        @DisplayName("이름이 비어있으면 회원가입 실패")
        void shouldFailWhenNameIsEmpty() {
            // given
            SignupCommand invalidCommand = new SignupCommand(
                "test@example.com",
                "password123",
                ""
            );

            // when & then
            assertThatThrownBy(() -> authService.signup(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @BeforeEach
        void setupUser() {
            // 로그인 테스트를 위해 사용자 먼저 생성
            authService.signup(testSignupCommand);
        }

        @Test
        @DisplayName("로그인 성공")
        void shouldLoginSuccessfully() {
            // when
            LoginResult result = authService.login(testLoginCommand);

            // then
            assertThat(result).isNotNull();
            assertThat(result.token()).isNotBlank();
            assertThat(result.email()).isEqualTo(testSignupCommand.email());
            assertThat(result.name()).isEqualTo(testSignupCommand.name());
            assertThat(result.role()).isEqualTo("USER");
            assertThat(result.expiresIn()).isGreaterThan(0);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void shouldFailWhenEmailNotFound() {
            // given
            LoginCommand invalidCommand = new LoginCommand(
                "nonexistent@example.com",
                "password123"
            );

            // when & then
            assertThatThrownBy(() -> authService.login(invalidCommand))
                .isInstanceOf(AuthApplicationService.InvalidCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void shouldFailWhenPasswordIsWrong() {
            // given
            LoginCommand invalidCommand = new LoginCommand(
                testSignupCommand.email(),
                "wrongpassword"
            );

            // when & then
            assertThatThrownBy(() -> authService.login(invalidCommand))
                .isInstanceOf(AuthApplicationService.InvalidCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("유효하지 않은 이메일 형식으로 로그인 실패")
        void shouldFailWhenEmailFormatIsInvalid() {
            // given
            LoginCommand invalidCommand = new LoginCommand(
                "invalid-email",
                "password123"
            );

            // when & then
            assertThatThrownBy(() -> authService.login(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("JWT 토큰이 정상적으로 생성됨")
        void shouldGenerateValidJwtToken() {
            // when
            LoginResult result = authService.login(testLoginCommand);

            // then
            assertThat(result.token())
                .isNotNull()
                .isNotEmpty()
                .contains("."); // JWT는 점으로 구분된 형식
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePasswordTest {

        private Long userId;

        @BeforeEach
        void setupUser() {
            // 사용자 생성
            authService.signup(testSignupCommand);

            // userId 조회
            userId = userRepository
                .findByEmail(new Email(testSignupCommand.email()))
                .get()
                .getId()
                .getValue();
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void shouldChangePasswordSuccessfully() {
            // given
            String currentPassword = "password123";
            String newPassword = "newPassword456";

            // when
            authService.changePassword(userId, currentPassword, newPassword);

            // then - 새 비밀번호로 로그인 성공
            LoginCommand newLoginCommand = new LoginCommand(
                testSignupCommand.email(),
                newPassword
            );
            LoginResult result = authService.login(newLoginCommand);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("현재 비밀번호가 틀리면 변경 실패")
        void shouldFailWhenCurrentPasswordIsWrong() {
            // given
            String wrongCurrentPassword = "wrongpassword";
            String newPassword = "newPassword456";

            // when & then
            assertThatThrownBy(() ->
                authService.changePassword(userId, wrongCurrentPassword, newPassword)
            )
                .isInstanceOf(om.dxline.dxtalent.identity.domain.model.User.InvalidPasswordException.class);
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 비밀번호 변경 실패")
        void shouldFailWhenUserNotFound() {
            // given
            Long nonExistentUserId = 99999L;

            // when & then
            assertThatThrownBy(() ->
                authService.changePassword(nonExistentUserId, "password123", "newPassword456")
            )
                .isInstanceOf(AuthApplicationService.UserNotFoundException.class);
        }

        @Test
        @DisplayName("비밀번호 변경 후 이전 비밀번호로 로그인 불가")
        void shouldNotLoginWithOldPasswordAfterChange() {
            // given
            String currentPassword = "password123";
            String newPassword = "newPassword456";
            authService.changePassword(userId, currentPassword, newPassword);

            // when & then - 이전 비밀번호로 로그인 시도
            LoginCommand oldLoginCommand = new LoginCommand(
                testSignupCommand.email(),
                currentPassword
            );
            assertThatThrownBy(() -> authService.login(oldLoginCommand))
                .isInstanceOf(AuthApplicationService.InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("사용자 비활성화 테스트")
    class DeactivateUserTest {

        private Long userId;

        @BeforeEach
        void setupUser() {
            authService.signup(testSignupCommand);
            userId = userRepository
                .findByEmail(new Email(testSignupCommand.email()))
                .get()
                .getId()
                .getValue();
        }

        @Test
        @DisplayName("사용자 비활성화 성공")
        void shouldDeactivateUserSuccessfully() {
            // when
            authService.deactivateUser(userId, "테스트 비활성화", 1L);

            // then - 비활성화된 사용자는 로그인 불가
            assertThatThrownBy(() -> authService.login(testLoginCommand))
                .isInstanceOf(om.dxline.dxtalent.identity.domain.model.User.UserNotActiveException.class);
        }

        @Test
        @DisplayName("비활성화된 사용자 재활성화 성공")
        void shouldReactivateUserSuccessfully() {
            // given
            authService.deactivateUser(userId, "테스트", 1L);

            // when
            authService.activateUser(userId);

            // then - 재활성화 후 로그인 가능
            LoginResult result = authService.login(testLoginCommand);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("엔드투엔드 시나리오 테스트")
    class EndToEndScenarioTest {

        @Test
        @DisplayName("전체 사용자 라이프사이클 테스트")
        void shouldCompleteUserLifecycle() {
            // 1. 회원가입
            authService.signup(testSignupCommand);
            assertThat(
                userRepository.existsByEmail(new Email(testSignupCommand.email()))
            ).isTrue();

            // 2. 로그인
            LoginResult loginResult = authService.login(testLoginCommand);
            assertThat(loginResult.token()).isNotBlank();

            // 3. 비밀번호 변경
            Long userId = loginResult.userId();
            authService.changePassword(userId, "password123", "newPassword789");

            // 4. 새 비밀번호로 로그인
            LoginCommand newLoginCommand = new LoginCommand(
                testSignupCommand.email(),
                "newPassword789"
            );
            LoginResult newLoginResult = authService.login(newLoginCommand);
            assertThat(newLoginResult.token()).isNotBlank();

            // 5. 비활성화
            authService.deactivateUser(userId, "테스트 종료", userId);

            // 6. 로그인 불가 확인
            assertThatThrownBy(() -> authService.login(newLoginCommand))
                .isInstanceOf(om.dxline.dxtalent.identity.domain.model.User.UserNotActiveException.class);

            // 7. 재활성화
            authService.activateUser(userId);

            // 8. 다시 로그인 가능
            LoginResult finalLoginResult = authService.login(newLoginCommand);
            assertThat(finalLoginResult).isNotNull();
        }
    }
}
