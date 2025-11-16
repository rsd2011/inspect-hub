package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.mapper.AuditLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

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
    private AuditLogMapper auditLogMapper;

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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(employeeId, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_SUCCESS");
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getMethod()).isEqualTo("LOCAL");
            assertThat(savedLog.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("로그인 성공 감사 로그 - 필수 필드 포함 (userId, employeeId, username)")
        void shouldIncludeAllRequiredFieldsWhenLoginSuccess() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String email = "hong@example.com";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email(email)
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog).isNotNull();
            assertThat(savedLog.getId()).isNotNull();
            assertThat(savedLog.getAction()).isEqualTo("LOGIN_SUCCESS");

            // 필수 필드 검증
            assertThat(savedLog.getUserId()).isEqualTo(userId.getValue());
            assertThat(savedLog.getEmployeeId()).isEqualTo(employeeId);
            assertThat(savedLog.getUsername()).isEqualTo(username);

            assertThat(savedLog.getMethod()).isEqualTo(loginMethod);
            assertThat(savedLog.getSuccess()).isTrue();
            assertThat(savedLog.getTimestamp()).isNotNull();
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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

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
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

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
