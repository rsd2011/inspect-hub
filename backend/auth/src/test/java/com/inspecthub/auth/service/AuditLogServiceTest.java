package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuditLogService Tests
 *
 * 감사 로그 서비스 테스트
 * BDD 스타일(Given-When-Then)로 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService - 감사 로그 서비스")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Nested
    @DisplayName("로그인 성공 감사 로그")
    class LoginSuccessAuditLog {

        @Test
        @DisplayName("AD 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: AD)")
        void shouldCreateAuditLogWhenAdLoginSuccess() {
            // Given (준비)
            String employeeId = "202401001";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getId()).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_SUCCESS");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("AD");
            assertThat(savedLog.getSuccess()).isTrue();
            assertThat(savedLog.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("SSO 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: SSO)")
        void shouldCreateAuditLogWhenSsoLoginSuccess() {
            // Given (준비)
            String employeeId = "202401002";
            String loginMethod = "SSO";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_SUCCESS");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("SSO");
            assertThat(savedLog.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("일반 로그인 성공 - 감사 로그 생성 (action: LOGIN_SUCCESS, method: LOCAL)")
        void shouldCreateAuditLogWhenLocalLoginSuccess() {
            // Given (준비)
            String employeeId = "202401003";
            String loginMethod = "LOCAL";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_SUCCESS");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("LOCAL");
            assertThat(savedLog.getSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("로그인 실패 감사 로그")
    class LoginFailureAuditLog {

        @Test
        @DisplayName("AD 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: AD)")
        void shouldCreateAuditLogWhenAdLoginFailure() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getId()).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_FAILURE");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("AD");
            assertThat(savedLog.getReason()).isEqualTo(reason);
            assertThat(savedLog.getSuccess()).isFalse();
            assertThat(savedLog.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("SSO 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: SSO)")
        void shouldCreateAuditLogWhenSsoLoginFailure() {
            // Given (준비)
            String employeeId = "202401002";
            String reason = "INVALID_SSO_TOKEN";
            String loginMethod = "SSO";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_FAILURE");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("SSO");
            assertThat(savedLog.getReason()).isEqualTo(reason);
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("일반 로그인 실패 - 감사 로그 생성 (action: LOGIN_FAILURE, method: LOCAL)")
        void shouldCreateAuditLogWhenLocalLoginFailure() {
            // Given (준비)
            String employeeId = "202401003";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "LOCAL";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            given(auditLogRepository.save(any(AuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogRepository).save(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_FAILURE");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("LOCAL");
            assertThat(savedLog.getReason()).isEqualTo(reason);
            assertThat(savedLog.getSuccess()).isFalse();
        }
    }
}
