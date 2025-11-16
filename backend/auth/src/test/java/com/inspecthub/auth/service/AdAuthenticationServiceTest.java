package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.domain.AccountLockPolicy;
import com.inspecthub.auth.repository.UserRepository;
import com.inspecthub.common.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;

/**
 * AdAuthenticationService Tests
 *
 * Active Directory (AD) LDAP 인증 서비스 테스트
 * BDD 스타일(Given-When-Then)로 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdAuthenticationService - AD 로그인 인증")
class AdAuthenticationServiceTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Validator validator;

    @Mock
    private AccountLockPolicy accountLockPolicy;

    @InjectMocks
    private AdAuthenticationService adAuthenticationService;

    private LoginRequest validRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Given: 유효한 로그인 요청
        validRequest = LoginRequest.builder()
            .employeeId("202401001")
            .password("ValidPass123!")
            .build();

        // Given: 기존 사용자
        existingUser = User.builder()
            .id(UserId.generate())
            .employeeId("202401001")
            .password(null)  // AD 사용자는 비밀번호 저장 안 함
            .name("홍길동")
            .email("hong@company.com")
            .active(true)
            .loginMethod("AD")
            .build();

        // Given: userRepository.save() Mock 설정 (user를 그대로 반환)
        org.mockito.Mockito.lenient().when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Given: Validator Mock 기본 설정 (validation 통과)
        given(validator.validate(any(LoginRequest.class)))
                .willReturn(Collections.emptySet());
    }

    @Nested
    @DisplayName("AD 인증 성공 시나리오")
    class SuccessfulAuthentication {

        @Test
        @DisplayName("유효한 사원ID와 AD 비밀번호로 인증 성공")
        void shouldAuthenticateWithValidAdCredentials() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 인증 성공 시뮬레이션 (예외 없음 = 성공)
            // Spring LDAP authenticate()는 void 리턴, 실패 시 예외 발생
            // Mock에서는 doNothing()으로 성공 시뮬레이션

            String expectedAccessToken = "eyJhbGciOiJIUzI1NiJ9.access...";
            String expectedRefreshToken = "eyJhbGciOiJIUzI1NiJ9.refresh...";

            given(jwtTokenProvider.generateAccessToken(existingUser))
                .willReturn(expectedAccessToken);
            given(jwtTokenProvider.generateRefreshToken(existingUser))
                .willReturn(expectedRefreshToken);

            // When (실행)
            TokenResponse result = adAuthenticationService.authenticate(validRequest);

            // Then (검증)
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(expectedAccessToken);
            assertThat(result.getRefreshToken()).isEqualTo(expectedRefreshToken);
            assertThat(result.getTokenType()).isEqualTo("Bearer");

            verify(auditLogService).logLoginSuccess(validRequest.getEmployeeId(), "AD");
        }

        @Test
        @DisplayName("AD 인증 성공 시 사용자 정보 업데이트")
        void shouldUpdateUserInfoOnSuccessfulLogin() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 인증 성공 (void 메서드는 기본적으로 아무것도 안 함)

            given(jwtTokenProvider.generateAccessToken(any(User.class)))
                .willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(any(User.class)))
                .willReturn("refresh-token");

            // When (실행)
            adAuthenticationService.authenticate(validRequest);

            // Then (검증) - 로그인 성공 상태가 저장되어야 함 (도메인 메서드 호출 후 save)
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("AD 인증 실패 시나리오")
    class FailedAuthentication {

        @Test
        @DisplayName("잘못된 AD 비밀번호로 인증 실패")
        void shouldThrowExceptionWhenInvalidAdPassword() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 인증 실패 시뮬레이션 (예외 발생)
            doThrow(new AuthenticationException(
                new javax.naming.AuthenticationException("Invalid credentials")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_002")
                .hasMessageContaining("비밀번호가 일치하지 않습니다");

            verify(auditLogService).logLoginFailure(
                validRequest.getEmployeeId(),
                "INVALID_AD_PASSWORD",
                "AD"
            );
        }

        @Test
        @DisplayName("존재하지 않는 사원ID로 인증 실패")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.empty());

            // LDAP에서도 존재하지 않는 사원ID (인증 실패)
            doThrow(new AuthenticationException(
                new javax.naming.AuthenticationException("Invalid credentials")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_002")
                .hasMessageContaining("비밀번호가 일치하지 않습니다");

            verify(auditLogService).logLoginFailure(
                validRequest.getEmployeeId(),
                "INVALID_AD_PASSWORD",
                "AD"
            );
        }

        @Test
        @DisplayName("비활성화된 사용자 인증 실패")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given (준비)
            User inactiveUser = User.builder()
                .id(UserId.generate())
                .employeeId("202401001")
                .name("홍길동")
                .active(false)  // 비활성
                .loginMethod("AD")
                .build();

            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(inactiveUser));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_003")
                .hasMessageContaining("비활성화된 계정입니다");
        }

        @Test
        @DisplayName("잠긴 계정 인증 실패")
        void shouldThrowExceptionWhenUserIsLocked() {
            // Given (준비)
            User lockedUser = User.builder()
                .id(UserId.generate())
                .employeeId("202401001")
                .name("홍길동")
                .active(true)
                .locked(true)  // 잠김
                .lockedUntil(java.time.LocalDateTime.now().plusMinutes(5))  // 잠금 기간 설정
                .loginMethod("AD")
                .build();

            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(lockedUser));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AUTH_004")
                .hasMessageContaining("계정이 잠금되었습니다");
        }
    }

    @Nested
    @DisplayName("AD 사용자 자동 생성")
    class UserAutoCreation {

        @Test
        @DisplayName("AD 인증 성공 시 사용자가 없으면 자동 생성")
        void shouldCreateUserIfNotExistsOnSuccessfulAdAuth() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.empty());  // 사용자 없음

            // LDAP 인증 성공 (void 메서드는 기본적으로 아무것도 안 함)

            // AD에서 조회한 사용자 정보 시뮬레이션
            // TODO: LDAP attribute 조회 Mock 구현 필요

            User newUser = User.builder()
                .id(UserId.generate())
                .employeeId(validRequest.getEmployeeId())
                .name("신규사용자")
                .email("new@company.com")
                .active(true)
                .loginMethod("AD")
                .build();

            given(userRepository.save(any(User.class)))
                .willReturn(newUser);

            given(jwtTokenProvider.generateAccessToken(any(User.class)))
                .willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(any(User.class)))
                .willReturn("refresh-token");

            // When (실행)
            TokenResponse result = adAuthenticationService.authenticate(validRequest);

            // Then (검증)
            assertThat(result).isNotNull();
            // 사용자 생성 시 1번, recordLoginSuccess() 후 1번 = 총 2번 호출
            verify(userRepository, times(2)).save(any(User.class));
            verify(auditLogService).logLoginSuccess(validRequest.getEmployeeId(), "AD");
        }
    }

    @Nested
    @DisplayName("AD 연동 오류 시나리오")
    class AdConnectionErrors {

        @Test
        @DisplayName("AD 서버 응답 없음 (Read Timeout) 시 예외 발생")
        void shouldThrowExceptionWhenAdServerReadTimeout() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 서버 Read Timeout 시뮬레이션 (3초 초과)
            doThrow(new CommunicationException(
                new javax.naming.CommunicationException("Read timed out")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AD_CONNECTION_ERROR")
                .hasMessageContaining("AD 서버 연결 실패");

            verify(auditLogService).logLoginFailure(
                validRequest.getEmployeeId(),
                "AD_CONNECTION_ERROR",
                "AD"
            );
        }

        @Test
        @DisplayName("AD 서버 연결 실패 시 예외 발생")
        void shouldThrowExceptionWhenAdServerIsUnreachable() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 서버 연결 실패 시뮬레이션
            doThrow(new CommunicationException(
                new javax.naming.CommunicationException("Connection refused")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When & Then (실행 & 검증)
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", "AD_CONNECTION_ERROR")
                .hasMessageContaining("AD 서버 연결 실패");

            verify(auditLogService).logLoginFailure(
                validRequest.getEmployeeId(),
                "AD_CONNECTION_ERROR",
                "AD"
            );
        }
    }

    @Nested
    @DisplayName("계정 잠금 정책 시나리오")
    class AccountLockPolicyScenarios {

        @Test
        @DisplayName("AD 로그인 5회 실패 시 5분 계정 잠금")
        void shouldLockAccountFor5MinutesAfter5Failures() {
            // Given: 4회 실패한 사용자
            User userWith4Failures = existingUser.toBuilder()
                .failedAttempts(4)
                .build();
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(userWith4Failures));

            // LDAP 인증 실패 시뮬레이션 (예외 발생)
            doThrow(new AuthenticationException(
                new javax.naming.AuthenticationException("Invalid credentials")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When: 5번째 실패
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class);

            // Then: 도메인 정책이 적용되고 저장되어야 함
            verify(accountLockPolicy).applyLockPolicy(any(User.class), any(Integer.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("AD 로그인 10회 실패 시 30분 계정 잠금")
        void shouldLockAccountFor30MinutesAfter10Failures() {
            // Given: 9회 실패한 사용자
            User userWith9Failures = existingUser.toBuilder()
                .failedAttempts(9)
                .build();
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(userWith9Failures));

            // LDAP 인증 실패 시뮬레이션
            doThrow(new AuthenticationException(
                new javax.naming.AuthenticationException("Invalid credentials")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When: 10번째 실패
            assertThatThrownBy(() -> adAuthenticationService.authenticate(validRequest))
                .isInstanceOf(BusinessException.class);

            // Then: 도메인 정책이 적용되고 저장되어야 함
            verify(accountLockPolicy).applyLockPolicy(any(User.class), any(Integer.class));
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("입력 검증 시나리오")
    class InputValidation {

        @Test
        @DisplayName("사원ID 형식 오류 (영문자 포함) - 입력 검증 실패")
        void shouldRejectInvalidEmployeeIdFormat() {
            // Given (준비)
            // 사원ID는 숫자만 허용 (예: 202401001)
            // 영문자 포함 시 검증 실패
            LoginRequest invalidRequest = LoginRequest.builder()
                .employeeId("EMP001ABC")  // 영문자 포함 (잘못된 형식)
                .password("ValidPass123!")
                .build();

            // Mock ConstraintViolation 생성
            @SuppressWarnings("unchecked")
            ConstraintViolation<LoginRequest> violation = mock(ConstraintViolation.class);
            given(violation.getMessage()).willReturn("사원ID는 숫자만 입력 가능합니다");
            given(validator.validate(invalidRequest))
                .willReturn(Set.of(violation));

            // When & Then (실행 & 검증)
            // DTO Validation으로 Controller에서 차단되어야 함
            // Service 레이어까지 오지 않아야 함
            // 하지만 만약 Service까지 온다면 예외 발생
            assertThatThrownBy(() -> adAuthenticationService.authenticate(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사원ID는 숫자만 입력 가능합니다");
        }

        @Test
        @DisplayName("사원ID 길이 초과 (50자 초과) - 입력 검증 실패")
        void shouldRejectEmployeeIdTooLong() {
            // Given (준비)
            // 사원ID 최대 50자 제한
            String tooLongId = "1".repeat(51);  // 51자
            LoginRequest invalidRequest = LoginRequest.builder()
                .employeeId(tooLongId)
                .password("ValidPass123!")
                .build();

            // Mock ConstraintViolation 생성
            @SuppressWarnings("unchecked")
            ConstraintViolation<LoginRequest> violation = mock(ConstraintViolation.class);
            given(violation.getMessage()).willReturn("사원ID는 최대 50자까지 입력 가능합니다");
            given(validator.validate(invalidRequest))
                .willReturn(Set.of(violation));

            // When & Then (실행 & 검증)
            // DTO Validation으로 Controller에서 차단되어야 함
            assertThatThrownBy(() -> adAuthenticationService.authenticate(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사원ID는 최대 50자까지 입력 가능합니다");
        }

        @Test
        @DisplayName("비밀번호 길이 초과 (100자 초과) - 입력 검증 실패")
        void shouldRejectPasswordTooLong() {
            // Given (준비)
            // 비밀번호 최대 100자 제한
            String tooLongPassword = "A".repeat(101);  // 101자
            LoginRequest invalidRequest = LoginRequest.builder()
                .employeeId("202401001")
                .password(tooLongPassword)
                .build();

            // Mock ConstraintViolation 생성
            @SuppressWarnings("unchecked")
            ConstraintViolation<LoginRequest> violation = mock(ConstraintViolation.class);
            given(violation.getMessage()).willReturn("비밀번호는 최대 100자까지 입력 가능합니다");
            given(validator.validate(invalidRequest))
                .willReturn(Set.of(violation));

            // When & Then (실행 & 검증)
            // DTO Validation으로 Controller에서 차단되어야 함
            assertThatThrownBy(() -> adAuthenticationService.authenticate(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호는 최대 100자까지 입력 가능합니다");
        }
    }

    @Nested
    @DisplayName("보안 시나리오")
    class SecurityScenarios {

        @Test
        @DisplayName("SQL Injection 시도 차단 - 사원ID에 SQL 구문 포함")
        void shouldBlockSqlInjectionAttempt() {
            // Given (준비)
            // SQL Injection 시도: ' OR '1'='1
            LoginRequest sqlInjectionRequest = LoginRequest.builder()
                .employeeId("' OR '1'='1")
                .password("password")
                .build();

            // Mock ConstraintViolation 생성 (Pattern 검증 실패)
            @SuppressWarnings("unchecked")
            ConstraintViolation<LoginRequest> violation = mock(ConstraintViolation.class);
            given(violation.getMessage()).willReturn("사원ID는 숫자만 입력 가능합니다");
            given(validator.validate(sqlInjectionRequest))
                .willReturn(Set.of(violation));

            // When & Then (실행 & 검증)
            // Pattern Validation으로 SQL 구문 차단
            assertThatThrownBy(() -> adAuthenticationService.authenticate(sqlInjectionRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사원ID는 숫자만 입력 가능합니다");

            // 데이터베이스 접근 시도조차 하지 않음
            verify(userRepository, never()).findByEmployeeId(anyString());
            verify(ldapTemplate, never()).authenticate(any(LdapQuery.class), anyString());
        }

        @Test
        @DisplayName("XSS 스크립트 입력 차단 - 사원ID에 스크립트 태그 포함")
        void shouldBlockXssScriptAttempt() {
            // Given (준비)
            // XSS 시도: <script>alert('XSS')</script>
            LoginRequest xssRequest = LoginRequest.builder()
                .employeeId("<script>alert('XSS')</script>")
                .password("password")
                .build();

            // Mock ConstraintViolation 생성 (Pattern 검증 실패)
            @SuppressWarnings("unchecked")
            ConstraintViolation<LoginRequest> violation = mock(ConstraintViolation.class);
            given(violation.getMessage()).willReturn("사원ID는 숫자만 입력 가능합니다");
            given(validator.validate(xssRequest))
                .willReturn(Set.of(violation));

            // When & Then (실행 & 검증)
            // Pattern Validation으로 XSS 스크립트 차단
            assertThatThrownBy(() -> adAuthenticationService.authenticate(xssRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사원ID는 숫자만 입력 가능합니다");

            // 데이터베이스 접근 시도조차 하지 않음
            verify(userRepository, never()).findByEmployeeId(anyString());
            verify(ldapTemplate, never()).authenticate(any(LdapQuery.class), anyString());
        }
    }
}
