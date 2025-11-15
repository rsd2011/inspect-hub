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
 * BDD 스타일(Given-When-Then)로 작성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginPolicyService - 로그인 정책 조회")
class LoginPolicyServiceTest {

    @Mock
    private LoginPolicyRepository loginPolicyRepository;

    @InjectMocks
    private LoginPolicyService loginPolicyService;

    private LoginPolicy orgPolicy;
    private LoginPolicy globalPolicy;

    @BeforeEach
    void setUp() {
        // Given: 조직별 정책 준비
        orgPolicy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF001")
            .name("조직별 정책")
            .orgId("01JCORG1234567890ABCDEF123")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.LOCAL))
            .build();

        // Given: 글로벌 정책 준비
        globalPolicy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF002")
            .name("글로벌 정책")
            .orgId(null)  // 글로벌
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .build();
    }

    @Test
    @DisplayName("조직ID로 조직별 정책을 조회할 수 있다")
    void shouldGetPolicyByOrgId() {
        // Given (준비)
        String orgId = "01JCORG1234567890ABCDEF123";
        given(loginPolicyRepository.findByOrgId(orgId))
            .willReturn(Optional.of(orgPolicy));

        // When (실행)
        LoginPolicy result = loginPolicyService.getPolicyByOrg(orgId);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getOrgId()).isEqualTo(orgId);
        assertThat(result.getName()).isEqualTo("조직별 정책");
        assertThat(result.isGlobalPolicy()).isFalse();

        verify(loginPolicyRepository).findByOrgId(orgId);
    }

    @Test
    @DisplayName("orgId가 null이면 글로벌 정책을 조회한다")
    void shouldGetGlobalPolicyWhenOrgIdIsNull() {
        // Given (준비)
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(globalPolicy));

        // When (실행)
        LoginPolicy result = loginPolicyService.getPolicyByOrg(null);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getOrgId()).isNull();
        assertThat(result.getName()).isEqualTo("글로벌 정책");
        assertThat(result.isGlobalPolicy()).isTrue();

        verify(loginPolicyRepository).findGlobalPolicy();
    }

    @Test
    @DisplayName("조직 정책이 없으면 글로벌 정책을 반환한다 (Fallback)")
    void shouldFallbackToGlobalPolicyWhenOrgPolicyNotFound() {
        // Given (준비)
        String orgId = "01JCORG1234567890ABCDEF999";  // 정책 없는 조직
        given(loginPolicyRepository.findByOrgId(orgId))
            .willReturn(Optional.empty());  // 조직 정책 없음
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(globalPolicy));  // 글로벌 정책으로 Fallback

        // When (실행)
        LoginPolicy result = loginPolicyService.getPolicyByOrg(orgId);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.isGlobalPolicy()).isTrue();
        assertThat(result.getName()).isEqualTo("글로벌 정책");

        verify(loginPolicyRepository).findByOrgId(orgId);
        verify(loginPolicyRepository).findGlobalPolicy();
    }

    @Test
    @DisplayName("조직의 사용 가능한 로그인 방식 리스트를 반환한다")
    void shouldGetAvailableMethodsByOrgId() {
        // Given (준비)
        String orgId = "01JCORG1234567890ABCDEF123";
        given(loginPolicyRepository.findByOrgId(orgId))
            .willReturn(Optional.of(orgPolicy));

        // When (실행)
        Set<LoginMethod> result = loginPolicyService.getAvailableMethods(orgId);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result).containsExactlyInAnyOrder(LoginMethod.SSO, LoginMethod.LOCAL);
        assertThat(result).doesNotContain(LoginMethod.AD);

        verify(loginPolicyRepository).findByOrgId(orgId);
    }

    @Test
    @DisplayName("조직의 최우선 로그인 방식을 반환한다")
    void shouldGetPrimaryMethodByOrgId() {
        // Given (준비)
        String orgId = "01JCORG1234567890ABCDEF123";
        given(loginPolicyRepository.findByOrgId(orgId))
            .willReturn(Optional.of(orgPolicy));

        // When (실행)
        LoginMethod result = loginPolicyService.getPrimaryMethod(orgId);

        // Then (검증)
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LoginMethod.SSO);  // 우선순위: SSO > AD > LOCAL

        verify(loginPolicyRepository).findByOrgId(orgId);
    }
}
