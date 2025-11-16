package com.inspecthub.auth.config;

import com.inspecthub.auth.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig Tests
 *
 * Security Configuration 테스트
 * BDD 스타일(Given-When-Then)로 작성
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@DisplayName("SecurityConfig - 보안 설정")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("인증 필수 정책")
    class AuthenticationRequired {

        @Test
        @DisplayName("모든 기능 로그인 필수 - 사내 업무시스템으로 공개 API 없음")
        void shouldRequireAuthenticationForAllFunctions() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            // /api/v1/** 엔드포인트는 모두 인증 필요
            mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/detection/str"))
                .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/policy"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("공개 API 예외 - 비밀번호 리셋 요청 API는 인증 불필요")
        void shouldAllowPasswordResetRequestWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            // 비밀번호 리셋 API는 인증 불필요 (401이 아님 = Security 통과)
            mockMvc.perform(post("/api/v1/auth/request-reset")
                    .contentType("application/json")
                    .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }

        @Test
        @DisplayName("공개 API 예외 - 비밀번호 리셋 토큰 검증 API는 인증 불필요")
        void shouldAllowResetTokenValidationWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            mockMvc.perform(get("/api/v1/auth/validate-reset-token")
                    .param("token", "dummy-token"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }

        @Test
        @DisplayName("공개 API 예외 - 비밀번호 리셋 실행 API는 인증 불필요")
        void shouldAllowPasswordResetExecutionWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/reset-password")
                    .contentType("application/json")
                    .content("{\"token\":\"dummy-token\",\"newPassword\":\"NewPass123!\"}"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }

        @Test
        @DisplayName("Health Check 엔드포인트 - 인증 불필요 (모니터링 목적)")
        void shouldAllowHealthCheckWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }
    }

    @Nested
    @DisplayName("로그인 엔드포인트 접근")
    class LoginEndpoints {

        @Test
        @DisplayName("LOCAL 로그인 엔드포인트는 인증 불필요")
        void shouldAllowLocalLoginWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content("{\"employeeId\":\"202401001\",\"password\":\"test\"}"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }

        @Test
        @DisplayName("AD 로그인 엔드포인트는 인증 불필요")
        void shouldAllowAdLoginWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType("application/json")
                    .content("{\"employeeId\":\"202401001\",\"password\":\"test\"}"))
                .andExpect(status().is(not(401)));  // 401이 아니면 Security 통과
        }

        @Test
        @DisplayName("토큰 갱신 엔드포인트는 인증 불필요")
        void shouldAllowTokenRefreshWithoutAuth() throws Exception {
            // Given (준비)
            // 비인증 사용자
            // 유효하지 않은 Refresh Token 전달

            // When & Then (실행 & 검증)
            // Security는 permitAll이므로 Controller까지 도달
            // AuthService에서 BusinessException 발생 → 401 반환 (AUTH_006)
            // 하지만 응답 본문에 ApiResponse 구조가 있으면 Security 통과로 판단
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType("application/json")
                    .content("{\"refreshToken\":\"dummy-refresh-token\"}"))
                .andExpect(status().isUnauthorized())                    // BusinessException으로 401
                .andExpect(jsonPath("$.success").value(false))           // ApiResponse 구조 확인
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));  // BusinessException 에러 코드
        }
    }
}
