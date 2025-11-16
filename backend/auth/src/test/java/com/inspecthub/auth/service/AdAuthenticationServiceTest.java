package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.repository.UserRepository;
import com.inspecthub.common.exception.BusinessException;
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

import java.util.Optional;

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
    @DisplayName("AD 정책 격리 검증")
    class AdPolicyIsolation {

        @Test
        @DisplayName("AD 로그인 실패 시 failedLoginAttempts 증가하지 않음")
        void shouldNotIncrementFailedAttemptsOnAdFailure() {
            // Given (준비)
            given(userRepository.findByEmployeeId(validRequest.getEmployeeId()))
                .willReturn(Optional.of(existingUser));

            // LDAP 인증 실패 시뮬레이션 (예외 발생)
            doThrow(new AuthenticationException(
                new javax.naming.AuthenticationException("Invalid credentials")))
                .when(ldapTemplate).authenticate(any(LdapQuery.class), eq(validRequest.getPassword()));

            // When (실행)
            try {
                adAuthenticationService.authenticate(validRequest);
            } catch (BusinessException e) {
                // 예외 무시 (실패 예상)
            }

            // Then (검증) - failedLoginAttempts 증가하지 않음
            verify(userRepository, org.mockito.Mockito.never())
                .incrementFailedAttempts(any());
        }
    }
}
