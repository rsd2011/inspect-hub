package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.LoginMethod;
import com.inspecthub.auth.domain.LoginPolicy;
import com.inspecthub.auth.repository.LoginPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
}
