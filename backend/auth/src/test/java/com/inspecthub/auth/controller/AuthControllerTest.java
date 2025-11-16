package com.inspecthub.auth.controller;

import com.inspecthub.auth.TestApplication;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.service.AdAuthenticationService;
import com.inspecthub.auth.service.AuthService;
import com.inspecthub.common.exception.BusinessException;
import com.inspecthub.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController Integration Tests
 *
 * Controller 레이어 통합 테스트
 * BDD 스타일(Given-When-Then)로 작성
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@DisplayName("AuthController - 인증 API")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AdAuthenticationService adAuthenticationService;

    @Nested
    @DisplayName("AD 로그인 API")
    class AdLoginEndpoint {

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 유효한 사원ID와 비밀번호로 AD 로그인 성공")
        void shouldLoginWithValidAdCredentials() throws Exception {
            // Given (준비)
            TokenResponse expectedResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.access...")
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.refresh...")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

            given(adAuthenticationService.authenticate(any()))
                .willReturn(expectedResponse);

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(expectedResponse.getAccessToken()))
                .andExpect(jsonPath("$.data.refreshToken").value(expectedResponse.getRefreshToken()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 잘못된 비밀번호로 AD 로그인 실패")
        void shouldFailWithInvalidAdPassword() throws Exception {
            // Given (준비)
            given(adAuthenticationService.authenticate(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_002));

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "WrongPassword"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_002"))
                .andExpect(jsonPath("$.error.message").value("비밀번호가 일치하지 않습니다"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 비활성화된 계정으로 로그인 실패")
        void shouldFailWithInactiveAccount() throws Exception {
            // Given (준비)
            given(adAuthenticationService.authenticate(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_003));

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_003"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 잠긴 계정으로 로그인 실패")
        void shouldFailWithLockedAccount() throws Exception {
            // Given (준비)
            given(adAuthenticationService.authenticate(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_004));

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_004"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 빈 사원ID로 로그인 실패")
        void shouldFailWithBlankEmployeeId() throws Exception {
            // Given (준비)
            String requestBody = """
                {
                    "employeeId": "",
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 빈 비밀번호로 로그인 실패")
        void shouldFailWithBlankPassword() throws Exception {
            // Given (준비)
            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": ""
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - null 사원ID로 로그인 실패")
        void shouldFailWithNullEmployeeId() throws Exception {
            // Given (준비)
            String requestBody = """
                {
                    "employeeId": null,
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - null 비밀번호로 로그인 실패")
        void shouldFailWithNullPassword() throws Exception {
            // Given (준비)
            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": null
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 만료된 계정으로 로그인 실패")
        void shouldFailWithExpiredAccount() throws Exception {
            // Given (준비)
            given(adAuthenticationService.authenticate(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_007));

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "ValidPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_007"))
                .andExpect(jsonPath("$.error.message").value("계정이 만료되었습니다"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login/ad - 비밀번호 만료로 로그인 실패")
        void shouldFailWithExpiredCredentials() throws Exception {
            // Given (준비)
            given(adAuthenticationService.authenticate(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_008, "비밀번호가 만료되었습니다. 비밀번호를 변경해주세요"));

            String requestBody = """
                {
                    "employeeId": "202401001",
                    "password": "ExpiredPass123!"
                }
                """;

            // When & Then (실행 & 검증)
            mockMvc.perform(post("/api/v1/auth/login/ad")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_008"))
                .andExpect(jsonPath("$.error.message").value("비밀번호가 만료되었습니다. 비밀번호를 변경해주세요"));
        }
    }
}
