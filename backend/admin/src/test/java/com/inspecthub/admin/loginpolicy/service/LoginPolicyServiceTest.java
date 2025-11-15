package com.inspecthub.admin.loginpolicy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import com.inspecthub.admin.loginpolicy.repository.LoginPolicyRepository;
import com.inspecthub.common.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * LoginPolicyService Tests
 *
 * Jenkins 스타일 전역 정책 (조직별 정책 없음)
 * BDD 스타일(Given-When-Then)로 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginPolicyService - 로그인 정책 조회 (전역 정책)")
class LoginPolicyServiceTest {

    @Mock
    private LoginPolicyRepository loginPolicyRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LoginPolicyService loginPolicyService;

    private LoginPolicy systemPolicy;

    @BeforeEach
    void setUp() {
        // Given: 시스템 전역 정책 준비
        systemPolicy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF002")
            .name("시스템 로그인 정책")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .build();
    }

    @Test
    @DisplayName("시스템 전역 정책을 조회할 수 있다")
    void shouldGetGlobalPolicy() {
        // Given (준비)
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));

        // When (실행)
        LoginPolicy result = loginPolicyService.getGlobalPolicy();

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("시스템 로그인 정책");
        assertThat(result.getEnabledMethods())
            .containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);

        verify(loginPolicyRepository).findGlobalPolicy();
    }

    @Test
    @DisplayName("사용 가능한 로그인 방식 리스트를 반환한다")
    void shouldGetAvailableMethods() {
        // Given (준비)
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));

        // When (실행)
        Set<LoginMethod> result = loginPolicyService.getAvailableMethods();

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result).containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL);

        verify(loginPolicyRepository).findGlobalPolicy();
    }

    @Test
    @DisplayName("최우선 로그인 방식을 반환한다")
    void shouldGetPrimaryMethod() {
        // Given (준비)
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));

        // When (실행)
        LoginMethod result = loginPolicyService.getPrimaryMethod();

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LoginMethod.SSO);  // 우선순위: SSO > AD > LOCAL

        verify(loginPolicyRepository).findGlobalPolicy();
    }

    @Test
    @DisplayName("전역 정책을 전체 업데이트할 수 있다")
    void shouldUpdateGlobalPolicy() throws Exception {
        // Given (준비)
        String newName = "변경된 로그인 정책";
        Set<LoginMethod> newMethods = new LinkedHashSet<>(
            Arrays.asList(LoginMethod.SSO, LoginMethod.LOCAL)
        );
        List<LoginMethod> newPriority = Arrays.asList(
            LoginMethod.SSO, LoginMethod.LOCAL
        );

        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));
        given(objectMapper.writeValueAsString(any(LoginPolicy.class)))
            .willReturn("{\"id\":\"01JCXYZ1234567890ABCDEF002\"}");

        // When (실행)
        LoginPolicy result = loginPolicyService.updateGlobalPolicy(
            newName, newMethods, newPriority
        );

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getEnabledMethods())
            .containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.LOCAL);
        assertThat(result.getPriority())
            .containsExactly(LoginMethod.SSO, LoginMethod.LOCAL);

        verify(loginPolicyRepository).findGlobalPolicy();
        verify(loginPolicyRepository).save(any(LoginPolicy.class));
    }

    @Test
    @DisplayName("활성화된 로그인 방식만 업데이트할 수 있다")
    void shouldUpdateEnabledMethods() throws Exception {
        // Given (준비)
        Set<LoginMethod> newMethods = Set.of(LoginMethod.SSO);

        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));
        given(objectMapper.writeValueAsString(any(LoginPolicy.class)))
            .willReturn("{\"id\":\"01JCXYZ1234567890ABCDEF002\"}");

        // When (실행)
        LoginPolicy result = loginPolicyService.updateEnabledMethods(newMethods);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getEnabledMethods()).containsExactly(LoginMethod.SSO);
        assertThat(result.getPriority()).containsExactly(LoginMethod.SSO);

        verify(loginPolicyRepository).findGlobalPolicy();
        verify(loginPolicyRepository).save(any(LoginPolicy.class));
    }

    @Test
    @DisplayName("우선순위만 업데이트할 수 있다")
    void shouldUpdatePriority() throws Exception {
        // Given (준비)
        List<LoginMethod> newPriority = Arrays.asList(
            LoginMethod.LOCAL, LoginMethod.AD, LoginMethod.SSO
        );

        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));
        given(objectMapper.writeValueAsString(any(LoginPolicy.class)))
            .willReturn("{\"id\":\"01JCXYZ1234567890ABCDEF002\"}");

        // When (실행)
        LoginPolicy result = loginPolicyService.updatePriority(newPriority);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getPriority())
            .containsExactly(LoginMethod.LOCAL, LoginMethod.AD, LoginMethod.SSO);

        verify(loginPolicyRepository).findGlobalPolicy();
        verify(loginPolicyRepository).save(any(LoginPolicy.class));
    }

    @Test
    @DisplayName("정책 업데이트 시 감사 로그를 호출한다")
    void shouldCallAuditLogOnPolicyUpdate() throws Exception {
        // Given (준비)
        String newName = "변경된 로그인 정책";
        Set<LoginMethod> newMethods = Set.of(LoginMethod.SSO, LoginMethod.LOCAL);
        List<LoginMethod> newPriority = Arrays.asList(LoginMethod.SSO, LoginMethod.LOCAL);

        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));
        given(objectMapper.writeValueAsString(any(LoginPolicy.class)))
            .willReturn("{\"id\":\"01JCXYZ1234567890ABCDEF002\"}");

        // When (실행)
        loginPolicyService.updateGlobalPolicy(newName, newMethods, newPriority);

        // Then (검증) - logPolicyChange 호출 확인
        verify(auditLogService).logPolicyChange(
            eq("SYSTEM_LOGIN_POLICY_UPDATED"),
            anyString(),  // userId
            anyString(),  // beforeJson
            anyString()   // afterJson
        );
    }

    @Test
    @DisplayName("감사 로그에 before/after JSON이 포함된다")
    void shouldIncludeBeforeAfterJsonInAuditLog() throws Exception {
        // Given (준비)
        Set<LoginMethod> newMethods = Set.of(LoginMethod.SSO);

        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(systemPolicy));
        given(objectMapper.writeValueAsString(any(LoginPolicy.class)))
            .willReturn("{\"id\":\"01JCXYZ1234567890ABCDEF002\"}");

        // When (실행)
        loginPolicyService.updateEnabledMethods(newMethods);

        // Then (검증) - JSON 파라미터 포함 확인
        verify(auditLogService).logPolicyChange(
            eq("LOGIN_METHOD_ENABLED"),
            anyString(),
            anyString(),  // beforeJson - null이 아니어야 함
            anyString()   // afterJson - null이 아니어야 함
        );
    }

    // NOTE: 캐시 동작 테스트는 Integration Test에서 수행
    // (Unit Test에서는 Mock 환경이므로 Spring Cache가 실제로 동작하지 않음)
}
