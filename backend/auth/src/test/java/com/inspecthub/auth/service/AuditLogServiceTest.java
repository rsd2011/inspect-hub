package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    @DisplayName("로그인 성공 감사 로그 - IP 주소 기록")
    class ClientIpRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있는 경우 해당 IP 사용")
        void shouldUseXForwardedForWhenPresent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String expectedIp = "203.0.113.195";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(expectedIp);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
        }

        @Test
        @DisplayName("X-Forwarded-For가 없으면 RemoteAddr 사용")
        void shouldUseRemoteAddrWhenNoXForwardedFor() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String expectedIp = "192.168.1.100";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(expectedIp);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
        }

        @Test
        @DisplayName("여러 IP가 있는 X-Forwarded-For 헤더는 첫 번째 IP 사용")
        void shouldUseFirstIpWhenMultipleXForwardedFor() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String multipleIps = "203.0.113.195, 70.41.3.18, 150.172.238.178";
            String expectedIp = "203.0.113.195";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(multipleIps);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
        }
    }

    @Nested
    @DisplayName("로그인 성공 감사 로그 - User-Agent 기록")
    class UserAgentRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("User-Agent 헤더가 있는 경우 기록")
        void shouldRecordUserAgentWhenPresent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn(userAgent);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getUserAgent()).isEqualTo(userAgent);
        }

        @Test
        @DisplayName("User-Agent 헤더가 없는 경우 null로 기록")
        void shouldRecordNullWhenUserAgentNotPresent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getUserAgent()).isNull();
        }

        @Test
        @DisplayName("실제 Chrome User-Agent 문자열 기록")
        void shouldRecordRealChromeUserAgent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String chromeUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn(chromeUserAgent);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getUserAgent()).isEqualTo(chromeUserAgent);
            assertThat(savedLog.getUserAgent()).contains("Chrome/120.0.0.0");
        }
    }

    @Nested
    @DisplayName("로그인 성공 감사 로그 - 세션 ID 기록")
    class SessionIdRecording {

        @Mock
        private HttpServletRequest request;

        @Mock
        private HttpSession session;

        @Test
        @DisplayName("세션이 존재하는 경우 세션 ID 기록")
        void shouldRecordSessionIdWhenSessionExists() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String sessionId = "E6B9A3F2-1D4C-4E8F-9A2B-C3D4E5F6A7B8";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(session);
            given(session.getId()).willReturn(sessionId);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getSessionId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("세션이 없는 경우 null로 기록")
        void shouldRecordNullWhenNoSession() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getSessionId()).isNull();
        }
    }

    @Nested
    @DisplayName("로그인 성공 감사 로그 - 타임스탬프 기록")
    class TimestampRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("로그인 성공 시 타임스탬프가 자동으로 기록됨")
        void shouldRecordTimestampWhenLoginSuccess() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            LocalDateTime beforeLog = LocalDateTime.now();

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            LocalDateTime afterLog = LocalDateTime.now();

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getTimestamp()).isNotNull();
            assertThat(savedLog.getTimestamp()).isAfterOrEqualTo(beforeLog);
            assertThat(savedLog.getTimestamp()).isBeforeOrEqualTo(afterLog);
        }

        @Test
        @DisplayName("타임스탬프가 현재 시간과 가까움 (1초 이내)")
        void shouldRecordTimestampCloseToCurrentTime() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            LocalDateTime now = LocalDateTime.now();

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getTimestamp()).isNotNull();
            
            // 타임스탬프가 테스트 시작 시간 기준 1초 이내여야 함
            long secondsDiff = java.time.Duration.between(now, savedLog.getTimestamp()).abs().getSeconds();
            assertThat(secondsDiff).isLessThanOrEqualTo(1L);
        }

        @Test
        @DisplayName("createdAt도 함께 기록됨")
        void shouldAlsoRecordCreatedAt() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getTimestamp()).isNotNull();
            assertThat(savedLog.getCreatedAt()).isNotNull();
            
            // timestamp와 createdAt은 거의 같은 시간이어야 함 (비동기 저장 시에만 차이 발생 가능)
            assertThat(savedLog.getTimestamp()).isEqualTo(savedLog.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("로그인 성공 감사 로그 - Referer 헤더 기록")
    class RefererRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("Referer 헤더가 있는 경우 기록")
        void shouldRecordRefererWhenPresent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";
            String referer = "https://intranet.company.com/portal";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);
            given(request.getHeader("Referer")).willReturn(referer);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReferer()).isEqualTo(referer);
        }

        @Test
        @DisplayName("Referer 헤더가 없는 경우 null로 기록")
        void shouldRecordNullWhenRefererNotPresent() {
            // Given (준비)
            UserId userId = UserId.of("01HN3Z8Q6PXYZ9ABCD1234EFGH");
            String employeeId = "202401001";
            String username = "홍길동";
            String loginMethod = "AD";

            User user = User.builder()
                .id(userId)
                .employeeId(employeeId)
                .name(username)
                .email("hong@example.com")
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn("Mozilla/5.0");
            given(request.getSession(false)).willReturn(null);
            given(request.getHeader("Referer")).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginSuccess(user, request, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReferer()).isNull();
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

    @Nested
    @DisplayName("로그인 실패 감사 로그 - IP 주소 기록")
    class LoginFailureClientIpRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있는 경우 해당 IP 사용")
        void shouldUseXForwardedForWhenPresent() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";
            String expectedIp = "203.0.113.195";

            given(request.getHeader("X-Forwarded-For")).willReturn(expectedIp);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod, request);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
            assertThat(savedLog.getReason()).isEqualTo(reason);
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("X-Forwarded-For가 없으면 RemoteAddr 사용")
        void shouldUseRemoteAddrWhenNoXForwardedFor() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";
            String expectedIp = "192.168.1.100";

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(expectedIp);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod, request);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
        }

        @Test
        @DisplayName("여러 IP가 있는 X-Forwarded-For 헤더는 첫 번째 IP 사용")
        void shouldUseFirstIpWhenMultipleXForwardedFor() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";
            String multipleIps = "203.0.113.195, 70.41.3.18, 150.172.238.178";
            String expectedIp = "203.0.113.195";

            given(request.getHeader("X-Forwarded-For")).willReturn(multipleIps);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod, request);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getClientIp()).isEqualTo(expectedIp);
        }
    }

    @Nested
    @DisplayName("로그인 실패 감사 로그 - User-Agent 기록")
    class LoginFailureUserAgentRecording {

        @Mock
        private HttpServletRequest request;

        @Test
        @DisplayName("User-Agent 헤더가 있는 경우 기록")
        void shouldRecordUserAgentWhenPresent() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn(userAgent);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod, request);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getUserAgent()).isEqualTo(userAgent);
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("User-Agent 헤더가 없는 경우 null로 기록")
        void shouldRecordNullWhenUserAgentNotPresent() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "INVALID_CREDENTIALS";
            String loginMethod = "AD";

            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn("192.168.1.100");
            given(request.getHeader("User-Agent")).willReturn(null);

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod, request);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getUserAgent()).isNull();
        }
    }

    @Nested
    @DisplayName("로그인 실패 감사 로그 - 다양한 실패 사유")
    class LoginFailureReasons {

        @Test
        @DisplayName("ACCOUNT_DISABLED - 비활성화된 계정")
        void shouldRecordAccountDisabledReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "ACCOUNT_DISABLED";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("ACCOUNT_DISABLED");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("ACCOUNT_LOCKED - 잠긴 계정")
        void shouldRecordAccountLockedReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "ACCOUNT_LOCKED";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("ACCOUNT_LOCKED");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("ACCOUNT_EXPIRED - 만료된 계정")
        void shouldRecordAccountExpiredReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "ACCOUNT_EXPIRED";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("ACCOUNT_EXPIRED");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("CREDENTIALS_EXPIRED - 비밀번호 만료")
        void shouldRecordCredentialsExpiredReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "CREDENTIALS_EXPIRED";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("CREDENTIALS_EXPIRED");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("AD_SERVER_UNAVAILABLE - AD 서버 장애")
        void shouldRecordAdServerUnavailableReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "AD_SERVER_UNAVAILABLE";
            String loginMethod = "AD";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("AD_SERVER_UNAVAILABLE");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("SSO_SERVER_UNAVAILABLE - SSO 서버 장애")
        void shouldRecordSsoServerUnavailableReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "SSO_SERVER_UNAVAILABLE";
            String loginMethod = "SSO";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("SSO_SERVER_UNAVAILABLE");
            assertThat(savedLog.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("BRUTE_FORCE_DETECTED - 무차별 대입 공격 감지")
        void shouldRecordBruteForceDetectedReason() {
            // Given (준비)
            String employeeId = "202401001";
            String reason = "BRUTE_FORCE_DETECTED";
            String loginMethod = "LOCAL";

            ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
            doNothing().when(auditLogMapper).insert(any(AuditLog.class));

            // When (실행)
            auditLogService.logLoginFailure(employeeId, reason, loginMethod);

            // Then (검증)
            verify(auditLogMapper).insert(auditLogCaptor.capture());

            AuditLog savedLog = auditLogCaptor.getValue();
            assertThat(savedLog.getReason()).isEqualTo("BRUTE_FORCE_DETECTED");
            assertThat(savedLog.getSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("감사 로그 데이터 구조 검증")
    class AuditLogDataStructure {

        @Test
        @DisplayName("로그인 성공 - ID는 ULID 형식 (26자, Base32)")
        void shouldGenerateUlidFormatIdOnLoginSuccess() {
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
            String id = savedLog.getId();

            // ULID 형식 검증
            assertThat(id).isNotNull();
            assertThat(id).hasSize(26);  // ULID는 26자
            assertThat(id).matches("[0-9A-HJKMNP-TV-Z]{26}");  // Crockford's Base32
            assertThat(id).isUpperCase();  // 모두 대문자
        }

        @Test
        @DisplayName("로그인 실패 - ID는 ULID 형식 (26자, Base32)")
        void shouldGenerateUlidFormatIdOnLoginFailure() {
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
            String id = savedLog.getId();

            // ULID 형식 검증
            assertThat(id).isNotNull();
            assertThat(id).hasSize(26);
            assertThat(id).matches("[0-9A-HJKMNP-TV-Z]{26}");
            assertThat(id).isUpperCase();
        }

        @Test
        @DisplayName("필수 필드는 항상 non-null (action, employeeId, timestamp, success)")
        void shouldAlwaysHaveRequiredFields() {
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

            // 필수 필드 검증
            assertThat(savedLog.getId()).isNotNull();
            assertThat(savedLog.getAction()).isNotNull();
            assertThat(savedLog.getEmployeeId()).isNotNull();
            assertThat(savedLog.getTimestamp()).isNotNull();
            assertThat(savedLog.getSuccess()).isNotNull();
            assertThat(savedLog.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("로그인 실패 시 userId와 username은 null 가능")
        void shouldAllowNullUserIdAndUsernameOnLoginFailure() {
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

            // nullable 필드 검증 (로그인 실패 시 userId, username은 null)
            assertThat(savedLog.getUserId()).isNull();
            assertThat(savedLog.getUsername()).isNull();

            // 필수 필드는 존재
            assertThat(savedLog.getId()).isNotNull();
            assertThat(savedLog.getEmployeeId()).isNotNull();
            assertThat(savedLog.getReason()).isNotNull();
        }
    }
}
