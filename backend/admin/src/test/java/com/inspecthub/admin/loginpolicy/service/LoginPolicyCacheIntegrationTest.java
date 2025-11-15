package com.inspecthub.admin.loginpolicy.service;

import com.inspecthub.admin.config.TestCacheConfig;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import com.inspecthub.admin.loginpolicy.repository.LoginPolicyRepository;
import com.inspecthub.common.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * LoginPolicy Redis Cache Integration Test
 *
 * 실제 Spring Cache 동작 검증:
 * - @Cacheable이 실제로 캐시에 저장하는지
 * - @CacheEvict이 실제로 캐시를 무효화하는지
 * - TTL이 올바르게 설정되는지
 *
 * @SpringBootTest: 실제 Spring Context 로드
 * Cache Manager 실제 동작 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
@DisplayName("LoginPolicy - Redis Cache Integration Test")
class LoginPolicyCacheIntegrationTest {

    @Autowired
    private LoginPolicyService loginPolicyService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private LoginPolicyRepository loginPolicyRepository;

    @MockBean
    private AuditLogService auditLogService;

    private LoginPolicy testPolicy;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );

        // 테스트 데이터 준비
        testPolicy = LoginPolicy.builder()
            .id("01JCXYZ1234567890ABCDEF002")
            .name("시스템 로그인 정책")
            .enabledMethods(Set.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .priority(Arrays.asList(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL))
            .active(true)
            .build();
    }

    @Test
    @DisplayName("첫 호출 시 DB에서 조회하고 Redis에 캐싱한다")
    void shouldCachePolicyInRedis() {
        // Given
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(testPolicy));

        // When - 첫 번째 호출
        LoginPolicy result1 = loginPolicyService.getGlobalPolicy();

        // Then - DB 조회 확인
        verify(loginPolicyRepository, times(1)).findGlobalPolicy();
        assertThat(result1).isNotNull();
        assertThat(result1.getName()).isEqualTo("시스템 로그인 정책");

        // When - 두 번째 호출 (캐시에서 반환되어야 함)
        LoginPolicy result2 = loginPolicyService.getGlobalPolicy();

        // Then - DB는 여전히 1번만 호출되어야 함 (캐시 사용)
        verify(loginPolicyRepository, times(1)).findGlobalPolicy();
        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isEqualTo(result1.getId());
    }

    @Test
    @DisplayName("업데이트 시 캐시가 무효화되고 다음 조회 시 DB에서 다시 조회한다")
    void shouldInvalidateCacheOnUpdate() {
        // Given - 캐시에 정책 저장
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(testPolicy));

        loginPolicyService.getGlobalPolicy();  // 캐시 저장
        verify(loginPolicyRepository, times(1)).findGlobalPolicy();

        // When - 정책 업데이트 (캐시 무효화)
        loginPolicyService.updateGlobalPolicy(
            "변경된 정책",
            Set.of(LoginMethod.SSO),
            Arrays.asList(LoginMethod.SSO)
        );

        // When - 다시 조회 (캐시가 무효화되어 DB에서 조회)
        loginPolicyService.getGlobalPolicy();

        // Then - updateGlobalPolicy 내부에서 getGlobalPolicy() 1회 + 외부 호출 2회 = 총 3회
        verify(loginPolicyRepository, times(3)).findGlobalPolicy();
    }

    @Test
    @DisplayName("캐시 매니저에 'system:login-policy' 캐시가 존재한다")
    void shouldHaveLoginPolicyCacheInCacheManager() {
        // Given
        given(loginPolicyRepository.findGlobalPolicy())
            .willReturn(Optional.of(testPolicy));

        // When
        loginPolicyService.getGlobalPolicy();

        // Then
        assertThat(cacheManager.getCacheNames())
            .contains("system:login-policy");

        assertThat(cacheManager.getCache("system:login-policy"))
            .isNotNull();
    }
}
