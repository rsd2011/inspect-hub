package com.inspecthub.auth.controller;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.TokenRefreshRequest;
import com.inspecthub.auth.dto.TokenRefreshResponse;
import com.inspecthub.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("로그인 API 테스트")
    class LoginTests {

        @Test
        @DisplayName("유효한 로그인 요청 처리")
        void login_WithValidRequest_ReturnsOkResponse() {
            // given
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("test123");

            LoginResponse expectedResponse = LoginResponse.builder()
                    .userId("USER-001")
                    .username("admin")
                    .accessToken("access.token.jwt")
                    .refreshToken("refresh.token.jwt")
                    .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
                    .expiresIn(3600000L)
                    .build();

            given(authService.login(any(LoginRequest.class))).willReturn(expectedResponse);

            // when
            ResponseEntity<LoginResponse> response = authController.login(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUserId()).isEqualTo("USER-001");
            assertThat(response.getBody().getUsername()).isEqualTo("admin");
            assertThat(response.getBody().getAccessToken()).isEqualTo("access.token.jwt");
            assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh.token.jwt");

            verify(authService).login(any(LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("유효한 토큰 갱신 요청 처리")
        void refreshToken_WithValidRequest_ReturnsOkResponse() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("valid.refresh.token");

            TokenRefreshResponse expectedResponse = TokenRefreshResponse.builder()
                    .accessToken("new.access.token")
                    .refreshToken("new.refresh.token")
                    .expiresIn(3600000L)
                    .build();

            given(authService.refreshToken(any(TokenRefreshRequest.class))).willReturn(expectedResponse);

            // when
            ResponseEntity<TokenRefreshResponse> response = authController.refreshToken(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAccessToken()).isEqualTo("new.access.token");
            assertThat(response.getBody().getRefreshToken()).isEqualTo("new.refresh.token");

            verify(authService).refreshToken(any(TokenRefreshRequest.class));
        }
    }

    @Nested
    @DisplayName("로그아웃 API 테스트")
    class LogoutTests {

        @Test
        @DisplayName("로그아웃 요청 처리")
        void logout_ReturnsOkResponse() {
            // given
            String userId = "USER-001";

            // when
            ResponseEntity<Void> response = authController.logout(userId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();

            verify(authService).logout(userId);
        }
    }

    @Nested
    @DisplayName("현재 사용자 조회 API 테스트")
    class GetCurrentUserTests {

        @Test
        @DisplayName("현재 사용자 정보 조회")
        void getCurrentUser_ReturnsOkResponse() {
            // given
            String userId = "USER-001";

            // when
            ResponseEntity<String> response = authController.getCurrentUser(userId);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("USER-001");
            assertThat(response.getBody()).contains("Authenticated user ID:");
        }
    }
}
