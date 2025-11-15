package com.inspecthub.admin.systemconfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.systemconfig.dto.LoginPolicyResponse;
import com.inspecthub.admin.systemconfig.dto.UpdateLoginPolicyRequest;
import com.inspecthub.admin.systemconfig.dto.UpdateMethodsRequest;
import com.inspecthub.admin.systemconfig.dto.UpdatePriorityRequest;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import com.inspecthub.admin.loginpolicy.service.LoginPolicyService;
import com.inspecthub.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SystemConfigController 통합 테스트
 */
@WebMvcTest(SystemConfigController.class)
@ContextConfiguration(classes = {SystemConfigController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SystemConfigController - 시스템 설정 API 테스트")
class SystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginPolicyService loginPolicyService;

    private LoginPolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        Set<LoginMethod> enabledMethods = new LinkedHashSet<>(
            Arrays.asList(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL)
        );
        List<LoginMethod> priority = Arrays.asList(
            LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL
        );

        defaultPolicy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF002")
            .name("시스템 로그인 정책")
            .enabledMethods(enabledMethods)
            .priority(priority)
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/system/login-policy - 전역 정책 조회")
    class GetLoginPolicy {

        @Test
        @DisplayName("전역 로그인 정책 조회 성공 - 200 OK")
        void shouldReturnGlobalPolicy() throws Exception {
            // Given
            given(loginPolicyService.getGlobalPolicy()).willReturn(defaultPolicy);

            // When & Then
            mockMvc.perform(get("/api/v1/system/login-policy"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("01JCXYZ1234567890ABCDEF002"))
                .andExpect(jsonPath("$.data.name").value("시스템 로그인 정책"))
                .andExpect(jsonPath("$.data.enabledMethods").isArray())
                .andExpect(jsonPath("$.data.priority").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/system/login-policy/available-methods - 사용 가능한 로그인 방식 조회")
    class GetAvailableMethods {

        @Test
        @DisplayName("활성화된 로그인 방식 조회 성공 - 200 OK")
        void shouldReturnAvailableMethods() throws Exception {
            // Given
            Set<LoginMethod> availableMethods = Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);
            given(loginPolicyService.getAvailableMethods()).willReturn(availableMethods);

            // When & Then
            mockMvc.perform(get("/api/v1/system/login-policy/available-methods"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/system/login-policy - 전역 정책 전체 업데이트")
    class UpdateLoginPolicy {

        @Test
        @DisplayName("유효한 요청으로 정책 업데이트 성공 - 200 OK")
        void shouldUpdatePolicy_WhenRequestIsValid() throws Exception {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .name("변경된 정책")
                .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.SSO, LoginMethod.LOCAL))
                .build();

            LoginPolicy updatedPolicy = LoginPolicy.builder()
                .id("01JCXYZ1234567890ABCDEF002")
                .name("변경된 정책")
                .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.SSO, LoginMethod.LOCAL))
                .build();

            given(loginPolicyService.updateGlobalPolicy(any(), any(), any()))
                .willReturn(updatedPolicy);

            // When & Then
            mockMvc.perform(put("/api/v1/system/login-policy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("변경된 정책"));
        }

        @Test
        @DisplayName("name이 누락된 경우 400 Bad Request")
        void shouldReturn400_WhenNameIsMissing() throws Exception {
            // Given
            UpdateLoginPolicyRequest request = UpdateLoginPolicyRequest.builder()
                .enabledMethods(Set.of(LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL))
                .build();

            // When & Then
            mockMvc.perform(put("/api/v1/system/login-policy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/system/login-policy/methods - 로그인 방식 업데이트")
    class UpdateMethods {

        @Test
        @DisplayName("로그인 방식 업데이트 성공 - 200 OK")
        void shouldUpdateMethods_WhenRequestIsValid() throws Exception {
            // Given
            UpdateMethodsRequest request = UpdateMethodsRequest.builder()
                .enabledMethods(Set.of(LoginMethod.SSO))
                .build();

            LoginPolicy updatedPolicy = LoginPolicy.builder()
                .id("01JCXYZ1234567890ABCDEF002")
                .name("시스템 로그인 정책")
                .enabledMethods(Set.of(LoginMethod.SSO))
                .priority(List.of(LoginMethod.SSO))
                .build();

            given(loginPolicyService.updateEnabledMethods(any())).willReturn(updatedPolicy);

            // When & Then
            mockMvc.perform(patch("/api/v1/system/login-policy/methods")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/system/login-policy/priority - 우선순위 업데이트")
    class UpdatePriority {

        @Test
        @DisplayName("우선순위 업데이트 성공 - 200 OK")
        void shouldUpdatePriority_WhenRequestIsValid() throws Exception {
            // Given
            UpdatePriorityRequest request = UpdatePriorityRequest.builder()
                .priority(List.of(LoginMethod.LOCAL, LoginMethod.SSO))
                .build();

            LoginPolicy updatedPolicy = LoginPolicy.builder()
                .id("01JCXYZ1234567890ABCDEF002")
                .name("시스템 로그인 정책")
                .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
                .priority(List.of(LoginMethod.LOCAL, LoginMethod.SSO))
                .build();

            given(loginPolicyService.updatePriority(any())).willReturn(updatedPolicy);

            // When & Then
            mockMvc.perform(patch("/api/v1/system/login-policy/priority")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }
}
