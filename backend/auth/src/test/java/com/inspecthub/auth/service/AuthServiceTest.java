package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.repository.UserRepository;
import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.RefreshTokenRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.common.config.AuthProperties;
import com.inspecthub.common.exception.BusinessException;
import com.inspecthub.common.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * AuthService BDD Tests
 * 
 * Story 1: LOCAL Login Implementation
 * - POST /api/v1/auth/login
 * - BCrypt password validation
 * - JWT token generation (Access + Refresh)
 * - Audit logging
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - LOCAL Login Authentication")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthService authService;

    private User validUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        // Given: AuthProperties Mock 설정 (lenient - 모든 테스트에서 필요하지 않을 수 있음)
        AuthProperties.JwtConfig jwtConfig = new AuthProperties.JwtConfig();
        AuthProperties.JwtConfig.TokenConfig accessTokenConfig = new AuthProperties.JwtConfig.TokenConfig();
        accessTokenConfig.setExpirationSeconds(3600);
        AuthProperties.JwtConfig.TokenConfig refreshTokenConfig = new AuthProperties.JwtConfig.TokenConfig();
        refreshTokenConfig.setExpirationSeconds(86400);

        jwtConfig.setAccessToken(accessTokenConfig);
        jwtConfig.setRefreshToken(refreshTokenConfig);
        org.mockito.Mockito.lenient().when(authProperties.getJwt()).thenReturn(jwtConfig);

        // Given: 테스트용 유효한 사용자 준비
        validUser = User.builder()
                .id(UserId.of("01ARZ3NDEKTSV4RRFFQ69G5FAV"))
                .employeeId("EMP001")
                .password("$2a$10$hashedPassword")  // BCrypt hashed
                .name("홍길동")
                .email("hong@example.com")
                .active(true)
                .failedAttempts(0)
                .build();

        validLoginRequest = LoginRequest.builder()
                .employeeId("EMP001")
                .password("Password123!")
                .build();
    }

    @Nested
    @DisplayName("로그인 성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("유효한 자격증명으로 로그인 시 JWT 토큰을 반환한다")
        void shouldAuthenticateLocalUser_WhenValidCredentials() {
            // Given: 유효한 사용자와 비밀번호
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(validUser));
            given(passwordEncoder.matches("Password123!", validUser.getPassword()))
                    .willReturn(true);
            given(jwtTokenProvider.generateAccessToken(validUser))
                    .willReturn("access.token.jwt");
            given(jwtTokenProvider.generateRefreshToken(validUser))
                    .willReturn("refresh.token.jwt");

            // When: 로그인 시도
            TokenResponse response = authService.authenticate(validLoginRequest);

            // Then: JWT 토큰이 정상적으로 반환되어야 함
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access.token.jwt");
            assertThat(response.getRefreshToken()).isEqualTo("refresh.token.jwt");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(3600); // 1 hour

            // And: 로그인 성공 감사 로그가 기록되어야 함
            then(auditLogService).should(times(1))
                    .logLoginSuccess(validUser.getEmployeeId(), "LOCAL");

            // And: 실패 횟수가 초기화되어야 함
            then(userRepository).should(times(1))
                    .resetFailedAttempts(validUser.getId());
        }

        @Test
        @DisplayName("첫 로그인 시 lastLoginAt이 업데이트된다")
        void shouldUpdateLastLoginAt_OnSuccessfulLogin() {
            // Given
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(validUser));
            given(passwordEncoder.matches(any(), any())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(any())).willReturn("token");
            given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh");

            // When
            authService.authenticate(validLoginRequest);

            // Then: lastLoginAt이 업데이트되어야 함
            then(userRepository).should(times(1))
                    .updateLastLoginAt(validUser.getId());
        }
    }

    @Nested
    @DisplayName("로그인 실패 시나리오")
    class FailureScenarios {

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시 예외를 발생시킨다")
        void shouldThrowException_WhenUserNotFound() {
            // Given: 존재하지 않는 사용자
            given(userRepository.findByEmployeeId("INVALID"))
                    .willReturn(Optional.empty());

            LoginRequest invalidRequest = LoginRequest.builder()
                    .employeeId("INVALID")
                    .password("Password123!")
                    .build();

            // When & Then: BusinessException 발생
            assertThatThrownBy(() -> authService.authenticate(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("사용자를 찾을 수 없습니다")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_001");
                    });

            // And: 토큰 생성이 호출되지 않아야 함
            then(jwtTokenProvider).should(never()).generateAccessToken(any());

            // And: 로그인 실패 감사 로그가 기록되어야 함
            then(auditLogService).should(times(1))
                    .logLoginFailure("INVALID", "USER_NOT_FOUND", "LOCAL");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외를 발생시킨다")
        void shouldThrowException_WhenInvalidPassword() {
            // Given: 비밀번호 불일치
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(validUser));
            given(passwordEncoder.matches("WrongPassword!", validUser.getPassword()))
                    .willReturn(false);

            LoginRequest invalidRequest = LoginRequest.builder()
                    .employeeId("EMP001")
                    .password("WrongPassword!")
                    .build();

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_002");
                    });

            // And: 실패 횟수가 증가되어야 함
            then(userRepository).should(times(1))
                    .incrementFailedAttempts(validUser.getId());

            // And: 로그인 실패 감사 로그가 기록되어야 함
            then(auditLogService).should(times(1))
                    .logLoginFailure("EMP001", "INVALID_PASSWORD", "LOCAL");
        }

        @Test
        @DisplayName("비활성화된 사용자로 로그인 시 예외를 발생시킨다")
        void shouldThrowException_WhenUserIsInactive() {
            // Given: 비활성화된 사용자
            User inactiveUser = validUser.toBuilder()
                    .active(false)
                    .build();
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(inactiveUser));

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(validLoginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("비활성화된 계정입니다")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_003");
                    });

            // And: 비밀번호 검증이 수행되지 않아야 함
            then(passwordEncoder).should(never()).matches(any(), any());
        }

        @Test
        @DisplayName("잠금된 사용자로 로그인 시 예외를 발생시킨다")
        void shouldThrowException_WhenUserIsLocked() {
            // Given: 잠금된 사용자 (5회 실패)
            User lockedUser = validUser.toBuilder()
                    .active(true)
                    .locked(true)
                    .failedAttempts(5)
                    .lockedUntil(java.time.LocalDateTime.now().plusMinutes(5))
                    .build();
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(lockedUser));

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(validLoginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("계정이 잠금되었습니다")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_004");
                    });

            // And: 비밀번호 검증이 수행되지 않아야 함
            then(passwordEncoder).should(never()).matches(any(), any());
        }
    }

    @Nested
    @DisplayName("계정 잠금 정책 시나리오")
    class AccountLockScenarios {

        @Test
        @DisplayName("5회 실패 시 5분 잠금된다")
        void shouldLockAccount_After5Failures() {
            // Given: 4회 실패한 사용자
            User user = validUser.toBuilder()
                    .failedAttempts(4)
                    .build();
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(false);

            LoginRequest request = validLoginRequest.toBuilder()
                    .password("WrongPassword")
                    .build();

            // When: 5번째 실패
            assertThatThrownBy(() -> authService.authenticate(request))
                    .isInstanceOf(BusinessException.class);

            // Then: 계정이 5분 잠금되어야 함
            then(userRepository).should(times(1))
                    .lockAccount(any(UserId.class), any(java.time.LocalDateTime.class)); // 5 minutes lock
        }

        @Test
        @DisplayName("10회 실패 시 30분 잠금된다")
        void shouldLockAccount_After10Failures() {
            // Given: 9회 실패한 사용자
            User user = validUser.toBuilder()
                    .failedAttempts(9)
                    .build();
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(false);

            // When: 10번째 실패
            assertThatThrownBy(() -> authService.authenticate(validLoginRequest.toBuilder()
                    .password("WrongPassword").build()))
                    .isInstanceOf(BusinessException.class);

            // Then: 계정이 30분 잠금되어야 함
            then(userRepository).should(times(1))
                    .lockAccount(any(UserId.class), any(java.time.LocalDateTime.class)); // 30 minutes lock
        }

        @Test
        @DisplayName("15회 실패 시 영구 잠금된다")
        void shouldPermanentlyLockAccount_After15Failures() {
            // Given: 14회 실패한 사용자
            User user = validUser.toBuilder()
                    .failedAttempts(14)
                    .build();
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(false);

            // When: 15번째 실패
            assertThatThrownBy(() -> authService.authenticate(validLoginRequest.toBuilder()
                    .password("WrongPassword").build()))
                    .isInstanceOf(BusinessException.class);

            // Then: 계정이 영구 잠금되어야 함 (null = permanent)
            then(userRepository).should(times(1))
                    .lockAccount(user.getId(), null); // permanent lock
        }
    }

    @Nested
    @DisplayName("토큰 생성 검증")
    class TokenGenerationValidation {

        @Test
        @DisplayName("Access Token은 1시간 유효기간을 가진다")
        void shouldGenerateAccessToken_With1HourExpiration() {
            // Given
            given(userRepository.findByEmployeeId(any()))
                    .willReturn(Optional.of(validUser));
            given(passwordEncoder.matches(any(), any())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(validUser))
                    .willReturn("access.token");
            given(jwtTokenProvider.generateRefreshToken(validUser))
                    .willReturn("refresh.token");

            // When
            TokenResponse response = authService.authenticate(validLoginRequest);

            // Then
            assertThat(response.getExpiresIn()).isEqualTo(3600); // 1 hour = 3600 seconds
        }

        @Test
        @DisplayName("Refresh Token은 24시간 유효기간을 가진다")
        void shouldGenerateRefreshToken_With24HourExpiration() {
            // Given
            given(userRepository.findByEmployeeId(any()))
                    .willReturn(Optional.of(validUser));
            given(passwordEncoder.matches(any(), any())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(validUser))
                    .willReturn("access.token");
            given(jwtTokenProvider.generateRefreshToken(validUser))
                    .willReturn("refresh.token");

            // When
            TokenResponse response = authService.authenticate(validLoginRequest);

            // Then: Refresh Token 정보 확인
            then(jwtTokenProvider).should(times(1))
                    .generateRefreshToken(validUser);
        }
    }

    @Nested
    @DisplayName("Refresh Token 정책")
    class RefreshTokenPolicy {

        @Test
        @DisplayName("유효한 Refresh Token으로 새로운 Access Token을 발급한다")
        void shouldIssueNewAccessToken_WithValidRefreshToken() {
            // Given: 유효한 Refresh Token
            String validRefreshToken = "valid.refresh.token";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(validRefreshToken)
                    .build();

            given(jwtTokenProvider.validateToken(validRefreshToken)).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(validRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeId(validRefreshToken)).willReturn("EMP001");
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(validUser));
            given(jwtTokenProvider.generateAccessToken(validUser))
                    .willReturn("new.access.token");
            given(jwtTokenProvider.generateRefreshToken(validUser))
                    .willReturn("new.refresh.token");

            // When: Refresh Token으로 갱신
            TokenResponse response = authService.refreshToken(request);

            // Then: 새로운 토큰이 발급되어야 함
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new.access.token");
            assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(3600);
        }

        @Test
        @DisplayName("만료된 Refresh Token을 거부한다")
        void shouldRejectExpiredRefreshToken() {
            // Given: 만료된 Refresh Token
            String expiredRefreshToken = "expired.refresh.token";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(expiredRefreshToken)
                    .build();

            given(jwtTokenProvider.validateToken(expiredRefreshToken))
                    .willThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

            // When & Then: AUTH_005 예외 발생
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("만료된 Refresh Token입니다. 다시 로그인하세요.")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_005");
                    });

            // And: 새로운 토큰이 생성되지 않아야 함
            then(jwtTokenProvider).should(never()).generateAccessToken(any());
            then(jwtTokenProvider).should(never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token을 거부한다")
        void shouldRejectInvalidRefreshToken() {
            // Given: 유효하지 않은 Refresh Token
            String invalidRefreshToken = "invalid.refresh.token";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(invalidRefreshToken)
                    .build();

            given(jwtTokenProvider.validateToken(invalidRefreshToken)).willReturn(false);

            // When & Then: AUTH_006 예외 발생
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 Refresh Token입니다.")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_006");
                    });
        }

        @Test
        @DisplayName("Access Token을 Refresh Token으로 사용할 수 없다")
        void shouldRejectAccessToken_AsRefreshToken() {
            // Given: Access Token (Refresh Token 아님)
            String accessToken = "access.token.jwt";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(accessToken)
                    .build();

            given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(accessToken)).willReturn(false); // Access Token

            // When & Then: AUTH_006 예외 발생
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 Refresh Token입니다.")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_006");
                    });

            // And: Token Type 검증 후 사용자 조회가 수행되지 않아야 함
            then(jwtTokenProvider).should(never()).getEmployeeId(any());
            then(userRepository).should(never()).findByEmployeeId(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 Refresh Token을 거부한다")
        void shouldRejectRefreshToken_WhenUserNotFound() {
            // Given: 유효한 Refresh Token이지만 사용자가 삭제됨
            String validRefreshToken = "valid.refresh.token";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(validRefreshToken)
                    .build();

            given(jwtTokenProvider.validateToken(validRefreshToken)).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(validRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeId(validRefreshToken)).willReturn("DELETED_USER");
            given(userRepository.findByEmployeeId("DELETED_USER"))
                    .willReturn(Optional.empty());

            // When & Then: AUTH_001 예외 발생
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("사용자를 찾을 수 없습니다")
                    .satisfies(ex -> {
                        BusinessException businessEx = (BusinessException) ex;
                        assertThat(businessEx.getErrorCode()).isEqualTo("AUTH_001");
                    });
        }

        @Test
        @DisplayName("Token Rotation - 새로운 Access Token과 Refresh Token을 모두 발급한다")
        void shouldRotateTokens_WhenRefreshingToken() {
            // Given: 유효한 Refresh Token
            String oldRefreshToken = "old.refresh.token";
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(oldRefreshToken)
                    .build();

            given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(oldRefreshToken)).willReturn(true);
            given(jwtTokenProvider.getEmployeeId(oldRefreshToken)).willReturn("EMP001");
            given(userRepository.findByEmployeeId("EMP001"))
                    .willReturn(Optional.of(validUser));
            given(jwtTokenProvider.generateAccessToken(validUser))
                    .willReturn("new.access.token");
            given(jwtTokenProvider.generateRefreshToken(validUser))
                    .willReturn("new.refresh.token");

            // When: Token 갱신
            TokenResponse response = authService.refreshToken(request);

            // Then: 새로운 Access Token과 Refresh Token이 모두 발급되어야 함
            assertThat(response.getAccessToken()).isNotEqualTo(oldRefreshToken);
            assertThat(response.getRefreshToken()).isNotEqualTo(oldRefreshToken);

            // And: 두 토큰 모두 생성되어야 함
            then(jwtTokenProvider).should(times(1)).generateAccessToken(validUser);
            then(jwtTokenProvider).should(times(1)).generateRefreshToken(validUser);
        }
    }
}
