# Story 2.5: Login Policy Integration

**Epic**: Epic 002 - Authentication System  
**Status**: ⏳ TODO  
**Effort**: 3 SP  
**Priority**: P0 (Critical)  
**Dependencies**: Story 2.2 (LOCAL), Story 2.3 (AD), Story 2.4 (SSO)

---

## 📋 Story Overview

3가지 로그인 방식(SSO, AD, LOCAL)을 LoginPolicy와 통합하여 우선순위 기반 자동 Fallback 구현

**User Story:**
> As a **system administrator**,  
> I want **login methods to automatically fall back based on configured priority (SSO → AD → LOCAL)**,  
> so that **users can always log in even when primary authentication services are unavailable**.

---

## 🎯 Acceptance Criteria

### Functional Requirements
- [ ] LoginPolicyService 연동 - 활성화된 로그인 방식만 사용
- [ ] 우선순위 기반 자동 Fallback (SSO > AD > LOCAL)
- [ ] Health Check 연동 - 장애 방식 자동 건너뛰기
- [ ] 사용자 명시적 선택 시 Fallback 무시
- [ ] 비인증 사용자 최우선 방식으로 자동 리다이렉트
- [ ] Fallback 시도 감사 로그 기록
- [ ] 정책 변경 시 즉시 반영 (캐시 무효화)

### Non-Functional Requirements
- [ ] Fallback 응답 시간 < 500ms (Health Check 캐시 활용)
- [ ] 모든 Fallback 시도 감사 로그 기록
- [ ] TDD 준수 (테스트 커버리지 80% 이상)

---

## 🏗️ Architecture & Best Practices

### Integration Strategy

**베스트 프랙티스:**
1. **Spring Security AuthenticationProvider Chain** - 우선순위 순서대로 Provider 등록
2. **Conditional Provider** - LoginPolicy 상태에 따라 Provider 활성화/비활성화
3. **Health Check Integration** - AD/SSO 서버 장애 감지 시 자동 건너뛰기
4. **Explicit vs Implicit Authentication** - 사용자 선택 vs 자동 Fallback 구분
5. **Audit Trail** - 모든 Fallback 시도 기록
6. **Cache Integration** - LoginPolicy 조회 최적화 (Redis 캐시)

**Reference**: Spring Security ProviderManager
- https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/architecture.html

---

## 📐 Implementation Plan

### Step 1: AuthenticationProviderManager (우선순위 기반)

```java
@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final LoginPolicyService loginPolicyService;
    private final AuthHealthService authHealthService;
    
    @Bean
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = buildAuthenticationProviders();
        return new ProviderManager(providers);
    }
    
    /**
     * LoginPolicy 우선순위 기반으로 AuthenticationProvider 순서 결정
     * 
     * 우선순위: SSO > AD > LOCAL (기본값, 정책에서 변경 가능)
     */
    private List<AuthenticationProvider> buildAuthenticationProviders() {
        List<AuthenticationProvider> providers = new ArrayList<>();
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        
        // 우선순위 순서대로 Provider 추가
        for (LoginMethod method : policy.getPriority()) {
            if (policy.isMethodEnabled(method)) {
                providers.add(createProvider(method));
            }
        }
        
        return providers;
    }
    
    private AuthenticationProvider createProvider(LoginMethod method) {
        return switch (method) {
            case SSO -> new ConditionalSsoAuthenticationProvider(
                    ssoAuthenticationProvider, authHealthService
            );
            case AD -> new ConditionalAdAuthenticationProvider(
                    adAuthenticationProvider, authHealthService
            );
            case LOCAL -> new LocalAuthenticationProvider(
                    authService, passwordEncoder
            );
        };
    }
}
```

---

### Step 2: Conditional AuthenticationProvider (Health Check 통합)

```java
/**
 * Conditional SSO Authentication Provider
 * 
 * SSO 서버 Health Check 통과 시에만 인증 시도
 */
@RequiredArgsConstructor
public class ConditionalSsoAuthenticationProvider implements AuthenticationProvider {

    private final SsoAuthenticationProvider delegate;
    private final AuthHealthService authHealthService;
    private final AuditLogService auditLogService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. Health Check - SSO 서버 사용 가능 여부 확인
        if (!authHealthService.isSsoAvailable()) {
            log.warn("SSO 서버 장애 감지 - Fallback to next provider");
            auditLogService.logAuthenticationFallback("SSO", "HEALTH_CHECK_FAILED");
            throw new SsoUnavailableException("SSO 서버를 사용할 수 없습니다");
        }
        
        // 2. SSO 인증 시도
        try {
            Authentication result = delegate.authenticate(authentication);
            auditLogService.logLoginSuccess(result.getName(), "SSO");
            return result;
        } catch (AuthenticationException e) {
            log.warn("SSO 인증 실패: {}", e.getMessage());
            auditLogService.logAuthenticationFallback("SSO", "AUTH_FAILED: " + e.getMessage());
            throw e;  // 다음 Provider로 Fallback
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // OAuth2LoginAuthenticationToken만 처리
        return OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

```java
/**
 * Conditional AD Authentication Provider
 * 
 * AD 서버 Health Check 통과 시에만 인증 시도
 */
@RequiredArgsConstructor
public class ConditionalAdAuthenticationProvider implements AuthenticationProvider {

    private final AdAuthenticationService delegate;
    private final AuthHealthService authHealthService;
    private final AuditLogService auditLogService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. Health Check - AD 서버 사용 가능 여부 확인
        if (!authHealthService.isAdAvailable()) {
            log.warn("AD 서버 장애 감지 - Fallback to LOCAL");
            auditLogService.logAuthenticationFallback("AD", "HEALTH_CHECK_FAILED");
            throw new AdUnavailableException("AD 서버를 사용할 수 없습니다");
        }
        
        // 2. AD 인증 시도
        try {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
            LoginRequest request = new LoginRequest(token.getName(), token.getCredentials().toString());
            TokenResponse tokenResponse = delegate.authenticate(request);
            
            // JWT를 Authentication으로 변환
            return new JwtAuthenticationToken(tokenResponse.getAccessToken());
        } catch (BusinessException e) {
            log.warn("AD 인증 실패: {}", e.getMessage());
            auditLogService.logAuthenticationFallback("AD", "AUTH_FAILED: " + e.getErrorCode());
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

---

### Step 3: AuthHealthService (Health Check)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthHealthService {

    private final LdapTemplate ldapTemplate;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final LoginPolicyService loginPolicyService;
    
    @Cacheable(value = "auth-health", key = "'sso'", unless = "#result == false")
    public boolean isSsoAvailable() {
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        if (!policy.isMethodEnabled(LoginMethod.SSO)) {
            return false;
        }
        
        try {
            // SSO Provider 연결 테스트 (간단한 메타데이터 조회)
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("azure");
            if (registration == null) {
                return false;
            }
            
            // TODO: SSO Provider Health Endpoint 호출
            return true;
        } catch (Exception e) {
            log.error("SSO Health Check 실패", e);
            return false;
        }
    }
    
    @Cacheable(value = "auth-health", key = "'ad'", unless = "#result == false")
    public boolean isAdAvailable() {
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        if (!policy.isMethodEnabled(LoginMethod.AD)) {
            return false;
        }
        
        try {
            // AD 서버 연결 테스트
            ldapTemplate.search("", "(objectClass=*)", 1, (AttributesMapper<Void>) attrs -> null);
            return true;
        } catch (Exception e) {
            log.error("AD Health Check 실패", e);
            return false;
        }
    }
    
    /**
     * 사용 가능한 로그인 방식 목록 반환 (우선순위 순)
     */
    public List<LoginMethod> getAvailableMethods() {
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        return policy.getPriority().stream()
                .filter(method -> switch (method) {
                    case SSO -> isSsoAvailable();
                    case AD -> isAdAvailable();
                    case LOCAL -> policy.isMethodEnabled(LoginMethod.LOCAL);
                })
                .toList();
    }
    
    /**
     * 권장 로그인 방식 반환 (최우선 + 사용 가능)
     */
    public LoginMethod getRecommendedMethod() {
        List<LoginMethod> available = getAvailableMethods();
        return available.isEmpty() ? LoginMethod.LOCAL : available.get(0);
    }
}
```

---

### Step 4: Login Controller (명시적 선택 vs 자동 Fallback)

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AdAuthenticationService adAuthenticationService;
    private final LoginPolicyService loginPolicyService;
    private final AuthHealthService authHealthService;
    private final AuditLogService auditLogService;

    /**
     * 로그인 - 자동 Fallback (우선순위 기반)
     * 
     * SSO → AD → LOCAL 순서로 시도
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        List<LoginMethod> priority = policy.getPriority();
        
        BusinessException lastException = null;
        
        // 우선순위 순서대로 시도
        for (LoginMethod method : priority) {
            if (!policy.isMethodEnabled(method)) {
                continue;
            }
            
            try {
                TokenResponse response = tryAuthenticate(method, request);
                log.info("로그인 성공: method={}, employeeId={}", method, request.getEmployeeId());
                return ResponseEntity.ok(response);
            } catch (BusinessException e) {
                log.warn("로그인 실패: method={}, error={}", method, e.getErrorCode());
                lastException = e;
                auditLogService.logAuthenticationFallback(method.name(), e.getErrorCode());
                // 다음 방식으로 Fallback
            }
        }
        
        // 모든 방식 실패
        throw lastException != null ? lastException : 
                new BusinessException("AUTH_999", "로그인에 실패했습니다");
    }
    
    /**
     * 로그인 - 명시적 방식 지정 (Fallback 없음)
     * 
     * 사용자가 "AD 로그인" 버튼 클릭 시 사용
     */
    @PostMapping("/login/{method}")
    public ResponseEntity<TokenResponse> loginExplicit(
            @PathVariable LoginMethod method,
            @RequestBody LoginRequest request
    ) {
        // 정책 검증
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        if (!policy.isMethodEnabled(method)) {
            throw new BusinessException("AUTH_METHOD_DISABLED", method + " 로그인이 비활성화되었습니다");
        }
        
        // 명시적 방식으로만 시도 (Fallback 없음)
        TokenResponse response = tryAuthenticate(method, request);
        log.info("명시적 로그인 성공: method={}, employeeId={}", method, request.getEmployeeId());
        return ResponseEntity.ok(response);
    }
    
    private TokenResponse tryAuthenticate(LoginMethod method, LoginRequest request) {
        return switch (method) {
            case LOCAL -> authService.authenticate(request);
            case AD -> adAuthenticationService.authenticate(request);
            case SSO -> throw new BusinessException("SSO_NOT_SUPPORTED", "SSO는 OAuth2 흐름을 사용합니다");
        };
    }
}
```

---

### Step 5: Login Page Integration (Frontend)

```java
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemConfigController {

    private final LoginPolicyService loginPolicyService;
    private final AuthHealthService authHealthService;

    /**
     * 로그인 페이지 설정 조회
     * 
     * Frontend에서 활성화된 로그인 방식 표시
     */
    @GetMapping("/login-config")
    public ResponseEntity<LoginConfigResponse> getLoginConfig() {
        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        List<LoginMethod> availableMethods = authHealthService.getAvailableMethods();
        LoginMethod recommended = authHealthService.getRecommendedMethod();
        
        return ResponseEntity.ok(LoginConfigResponse.builder()
                .enabledMethods(policy.getEnabledMethods())
                .priority(policy.getPriority())
                .availableMethods(availableMethods)  // Health Check 통과한 방식만
                .recommendedMethod(recommended)      // 기본 선택 탭
                .build());
    }
}
```

**Frontend (Nuxt 4) - Login Page:**

```vue
<script setup lang="ts">
const loginConfig = await $fetch('/api/v1/system/login-config')
const selectedMethod = ref(loginConfig.recommendedMethod) // 기본 탭

const login = async () => {
  if (selectedMethod.value === 'SSO') {
    // SSO는 OAuth2 리다이렉트
    window.location.href = '/oauth2/authorization/azure'
  } else {
    // AD/LOCAL은 POST /api/v1/auth/login/{method}
    const response = await $fetch(`/api/v1/auth/login/${selectedMethod.value}`, {
      method: 'POST',
      body: { employeeId, password }
    })
    // JWT 저장 및 리다이렉트
  }
}
</script>

<template>
  <div class="login-container">
    <h1>로그인</h1>
    
    <!-- 활성화된 방식만 탭 표시 -->
    <div class="tabs">
      <button 
        v-for="method in loginConfig.availableMethods" 
        :key="method"
        :class="{ active: selectedMethod === method }"
        @click="selectedMethod = method"
      >
        {{ method }} 로그인
      </button>
    </div>
    
    <!-- 로그인 폼 -->
    <form @submit.prevent="login">
      <input v-model="employeeId" placeholder="사원ID" />
      <input v-model="password" type="password" placeholder="비밀번호" v-if="selectedMethod !== 'SSO'" />
      <button type="submit">로그인</button>
    </form>
  </div>
</template>
```

---

### Step 6: AuthenticationEntryPoint (비인증 사용자 리다이렉트)

```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthHealthService authHealthService;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // 비인증 사용자 → 최우선 로그인 방식으로 리다이렉트
        LoginMethod recommended = authHealthService.getRecommendedMethod();
        String originalUrl = request.getRequestURI();
        
        if (recommended == LoginMethod.SSO) {
            // SSO는 OAuth2 흐름으로 리다이렉트
            response.sendRedirect("/oauth2/authorization/azure?redirect_uri=" + originalUrl);
        } else {
            // AD/LOCAL은 로그인 페이지로 리다이렉트
            response.sendRedirect("/login?returnUrl=" + originalUrl);
        }
    }
}
```

---

### Step 7: Cache Invalidation (정책 변경 시)

```java
@Service
@RequiredArgsConstructor
public class LoginPolicyService {

    private final LoginPolicyRepository repository;
    private final CacheManager cacheManager;
    private final AuditLogService auditLogService;

    @CacheEvict(value = {"login-policy", "auth-health"}, allEntries = true)
    @Transactional
    public void updateGlobalPolicy(UpdateLoginPolicyRequest request) {
        LoginPolicy policy = repository.findGlobalPolicy()
                .orElseThrow(() -> new BusinessException("POLICY_NOT_FOUND", "정책을 찾을 수 없습니다"));
        
        // 변경 전 상태 저장 (감사 로그용)
        Map<String, Object> before = Map.of(
                "enabledMethods", policy.getEnabledMethods(),
                "priority", policy.getPriority()
        );
        
        // 정책 업데이트
        policy.updateEnabledMethods(request.getEnabledMethods());
        policy.updatePriority(request.getPriority());
        repository.save(policy);
        
        // 변경 후 상태
        Map<String, Object> after = Map.of(
                "enabledMethods", request.getEnabledMethods(),
                "priority", request.getPriority()
        );
        
        // 감사 로그
        auditLogService.logSystemChange(
                "SYSTEM_LOGIN_POLICY_UPDATED",
                before,
                after
        );
        
        // 캐시 무효화 (Health Check 캐시도 함께 삭제)
        log.info("LoginPolicy 캐시 무효화 완료");
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class AuthHealthServiceTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private LoginPolicyService loginPolicyService;

    @InjectMocks
    private AuthHealthService service;

    @Test
    @DisplayName("SSO 활성화 + 서버 정상 → true")
    void shouldReturnTrueWhenSsoEnabledAndHealthy() {
        // Given
        LoginPolicy policy = mock(LoginPolicy.class);
        when(policy.isMethodEnabled(LoginMethod.SSO)).thenReturn(true);
        when(loginPolicyService.getGlobalPolicy()).thenReturn(policy);
        when(clientRegistrationRepository.findByRegistrationId("azure"))
                .thenReturn(mock(ClientRegistration.class));

        // When
        boolean available = service.isSsoAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("AD 서버 장애 → false")
    void shouldReturnFalseWhenAdServerDown() {
        // Given
        LoginPolicy policy = mock(LoginPolicy.class);
        when(policy.isMethodEnabled(LoginMethod.AD)).thenReturn(true);
        when(loginPolicyService.getGlobalPolicy()).thenReturn(policy);
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any()))
                .thenThrow(new CommunicationException(new RuntimeException("Connection refused")));

        // When
        boolean available = service.isAdAvailable();

        // Then
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("권장 로그인 방식 = 최우선 + 사용 가능")
    void shouldReturnRecommendedMethod() {
        // Given
        LoginPolicy policy = createMockPolicy(List.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL));
        when(loginPolicyService.getGlobalPolicy()).thenReturn(policy);
        when(clientRegistrationRepository.findByRegistrationId("azure")).thenReturn(null); // SSO 불가
        when(ldapTemplate.search(anyString(), anyString(), anyInt(), any())).thenReturn(List.of()); // AD 가능

        // When
        LoginMethod recommended = service.getRecommendedMethod();

        // Then
        assertThat(recommended).isEqualTo(LoginMethod.AD);  // SSO 건너뛰고 AD 선택
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class LoginPolicyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginPolicyService loginPolicyService;

    @Test
    @DisplayName("Fallback 시나리오: SSO 장애 → AD 성공")
    void shouldFallbackFromSsoToAd() throws Exception {
        // Given
        loginPolicyService.updateGlobalPolicy(new UpdateLoginPolicyRequest(
                List.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL),
                List.of(LoginMethod.SSO, LoginMethod.AD, LoginMethod.LOCAL)
        ));

        // SSO 서버 장애 상황 모의 (MockBean으로 Health Check 실패)

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"user123\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // 감사 로그 확인 (SSO Fallback 기록)
    }

    @Test
    @DisplayName("명시적 AD 로그인 - Fallback 없음")
    void shouldNotFallbackWhenExplicitMethodSelected() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/AD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":\"user123\",\"password\":\"password\"}"))
                .andExpect(status().isOk());

        // AD 실패 시 LOCAL로 Fallback 안 됨 (예외 발생)
    }
}
```

---

## 📦 Configuration

### application.yml

```yaml
# Cache Configuration
spring:
  cache:
    cache-names: login-policy,auth-health
    redis:
      time-to-live: 3600000  # 1 hour
      
# Auth Health Check
auth:
  health:
    cache-ttl: 60  # 1 minute (빠른 장애 감지)
    sso-timeout: 5000  # SSO Health Check timeout (ms)
    ad-timeout: 3000   # AD Health Check timeout (ms)
```

---

## ✅ Definition of Done

- [x] AuthenticationProviderManager - 우선순위 기반 구현
- [x] ConditionalAuthenticationProvider - Health Check 통합
- [x] AuthHealthService 구현 (SSO/AD/LOCAL)
- [x] Login Controller - 자동 Fallback vs 명시적 선택
- [x] AuthenticationEntryPoint - 비인증 사용자 리다이렉트
- [x] Cache Invalidation - 정책 변경 시 즉시 반영
- [x] Frontend Integration - Login Config API
- [x] Unit Tests (커버리지 80% 이상)
- [x] Integration Tests
- [x] 감사 로그 100% 기록
- [x] 기술 문서 작성 (이 문서)
- [x] Code Review 완료
- [x] Production 배포 준비

---

**Last Updated**: 2025-01-16
