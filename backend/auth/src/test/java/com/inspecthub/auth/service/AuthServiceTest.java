package com.inspecthub.auth.service;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.TokenRefreshRequest;
import com.inspecthub.auth.dto.TokenRefreshResponse;
import com.inspecthub.auth.exception.InvalidCredentialsException;
import com.inspecthub.auth.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("유효한 사용자 정보로 로그인에 성공한다")
        void login_WithValidCredentials_ReturnsLoginResponse() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("test123");

            String expectedAccessToken = "access.token.jwt";
            String expectedRefreshToken = "refresh.token.jwt";
            List<String> expectedRoles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(expectedAccessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(expectedRefreshToken);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(expectedAccessToken);
            assertThat(response.getRefreshToken()).isEqualTo(expectedRefreshToken);
            assertThat(response.getUsername()).isEqualTo("admin");
            assertThat(response.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoles);

            // Password check is currently disabled for demo, so it should not be called
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtTokenProvider).createAccessToken(anyString(), eq("admin"), anyList());
            verify(jwtTokenProvider).createRefreshToken(anyString());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시도 시 예외가 발생한다")
        void login_WithNonExistentUser_ThrowsInvalidCredentialsException() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword("password");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtTokenProvider, never()).createAccessToken(anyString(), anyString(), anyList());
            verify(jwtTokenProvider, never()).createRefreshToken(anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로도 로그인에 성공한다 (현재는 비밀번호 검증이 비활성화됨)")
        void login_WithInvalidPassword_SucceedsForDemo() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("wrongpassword");

            String expectedAccessToken = "access.token.jwt";
            String expectedRefreshToken = "refresh.token.jwt";

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(expectedAccessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(expectedRefreshToken);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("admin");

            // Password validation is currently disabled for demo
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtTokenProvider).createAccessToken(anyString(), eq("admin"), anyList());
            verify(jwtTokenProvider).createRefreshToken(anyString());
        }

        @Test
        @DisplayName("null username으로 로그인 시도 시 예외가 발생한다")
        void login_WithNullUsername_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername(null);
            request.setPassword("password");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("null password로도 로그인에 성공한다 (현재는 비밀번호 검증이 비활성화됨)")
        void login_WithNullPassword_SucceedsForDemo() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword(null);

            String expectedAccessToken = "access.token.jwt";
            String expectedRefreshToken = "refresh.token.jwt";

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(expectedAccessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(expectedRefreshToken);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("빈 username으로 로그인 시도 시 예외가 발생한다")
        void login_WithEmptyUsername_ThrowsException() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("");
            request.setPassword("password");

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("빈 password로도 로그인에 성공한다 (현재는 비밀번호 검증이 비활성화됨)")
        void login_WithEmptyPassword_SucceedsForDemo() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("");

            String expectedAccessToken = "access.token.jwt";
            String expectedRefreshToken = "refresh.token.jwt";

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(expectedAccessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(expectedRefreshToken);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("유효한 Refresh Token으로 Access Token을 갱신할 수 있다")
        void refreshToken_WithValidRefreshToken_ReturnsNewAccessToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("valid.refresh.token");

            String userId = "user123";
            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";

            given(jwtTokenProvider.validateToken(eq("valid.refresh.token"))).willReturn(true);
            given(jwtTokenProvider.getUserId(eq("valid.refresh.token"))).willReturn(userId);
            given(jwtTokenProvider.createAccessToken(anyString(), anyString(), anyList()))
                    .willReturn(newAccessToken);
            given(jwtTokenProvider.createRefreshToken(eq(userId)))
                    .willReturn(newRefreshToken);

            // when
            TokenRefreshResponse response = authService.refreshToken(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);

            verify(jwtTokenProvider).validateToken(eq("valid.refresh.token"));
            verify(jwtTokenProvider).getUserId(eq("valid.refresh.token"));
            // AuthService uses hardcoded values for username and roles, not from token
            verify(jwtTokenProvider).createAccessToken(eq(userId), eq("admin"), anyList());
            verify(jwtTokenProvider).createRefreshToken(eq(userId));
        }

        @Test
        @DisplayName("만료된 Refresh Token으로 갱신 시도 시 예외가 발생한다")
        void refreshToken_WithExpiredToken_ThrowsTokenExpiredException() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("expired.refresh.token");

            given(jwtTokenProvider.validateToken(eq("expired.refresh.token"))).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("invalid or expired");

            verify(jwtTokenProvider).validateToken(eq("expired.refresh.token"));
            verify(jwtTokenProvider, never()).getUserId(anyString());
            verify(jwtTokenProvider, never()).createAccessToken(anyString(), anyString(), anyList());
        }

        @Test
        @DisplayName("잘못된 형식의 Refresh Token으로 갱신 시도 시 예외가 발생한다")
        void refreshToken_WithInvalidToken_ThrowsTokenExpiredException() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("invalid.token");

            given(jwtTokenProvider.validateToken(eq("invalid.token"))).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(TokenExpiredException.class)
                    .hasMessageContaining("invalid or expired");

            verify(jwtTokenProvider).validateToken(eq("invalid.token"));
        }

        @Test
        @DisplayName("null Refresh Token으로 갱신 시도 시 예외가 발생한다")
        void refreshToken_WithNullToken_ThrowsException() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken(null);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("빈 Refresh Token으로 갱신 시도 시 예외가 발생한다")
        void refreshToken_WithEmptyToken_ThrowsException() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("");

            given(jwtTokenProvider.validateToken(eq(""))).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTests {

        @Test
        @DisplayName("유효한 userId로 로그아웃을 할 수 있다")
        void logout_WithValidUserId_Success() {
            // given
            String userId = "user123";

            // when & then
            assertThatNoException().isThrownBy(() -> authService.logout(userId));
        }

        @Test
        @DisplayName("null userId로 로그아웃 시도 시 정상 처리된다")
        void logout_WithNullUserId_Success() {
            // when & then
            assertThatNoException().isThrownBy(() -> authService.logout(null));
        }

        @Test
        @DisplayName("빈 userId로 로그아웃 시도 시 정상 처리된다")
        void logout_WithEmptyUserId_Success() {
            // when & then
            assertThatNoException().isThrownBy(() -> authService.logout(""));
        }

        @Test
        @DisplayName("여러 번 로그아웃을 시도해도 정상 처리된다")
        void logout_MultipleTimes_Success() {
            // given
            String userId = "user123";

            // when & then
            assertThatNoException().isThrownBy(() -> {
                authService.logout(userId);
                authService.logout(userId);
                authService.logout(userId);
            });
        }
    }

    @Nested
    @DisplayName("인증 통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("로그인 후 토큰 갱신 시나리오가 정상 작동한다")
        void loginAndRefresh_Scenario_Success() {
            // given - 로그인
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("test123");

            String accessToken = "access.token.jwt";
            String refreshToken = "refresh.token.jwt";

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(accessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(refreshToken);

            // when - 로그인
            LoginResponse loginResponse = authService.login(loginRequest);

            // then - 로그인 검증
            assertThat(loginResponse.getAccessToken()).isEqualTo(accessToken);
            assertThat(loginResponse.getRefreshToken()).isEqualTo(refreshToken);

            // given - 토큰 갱신
            TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
            refreshRequest.setRefreshToken(refreshToken);

            String userId = "user123";
            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";

            given(jwtTokenProvider.validateToken(eq(refreshToken))).willReturn(true);
            given(jwtTokenProvider.getUserId(eq(refreshToken))).willReturn(userId);
            given(jwtTokenProvider.createAccessToken(eq(userId), eq("admin"), anyList()))
                    .willReturn(newAccessToken);
            given(jwtTokenProvider.createRefreshToken(eq(userId)))
                    .willReturn(newRefreshToken);

            // when - 토큰 갱신
            TokenRefreshResponse refreshResponse = authService.refreshToken(refreshRequest);

            // then - 갱신 검증
            assertThat(refreshResponse.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(refreshResponse.getRefreshToken()).isEqualTo(newRefreshToken);
        }

        @Test
        @DisplayName("로그인 후 로그아웃 시나리오가 정상 작동한다")
        void loginAndLogout_Scenario_Success() {
            // given - 로그인
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("test123");

            String accessToken = "access.token.jwt";
            String refreshToken = "refresh.token.jwt";

            given(jwtTokenProvider.createAccessToken(anyString(), eq("admin"), anyList()))
                    .willReturn(accessToken);
            given(jwtTokenProvider.createRefreshToken(anyString()))
                    .willReturn(refreshToken);

            // when - 로그인
            LoginResponse loginResponse = authService.login(loginRequest);

            // then - 로그인 검증
            assertThat(loginResponse).isNotNull();

            // when & then - 로그아웃
            assertThatNoException().isThrownBy(() -> authService.logout("admin_id"));
        }
    }
}
