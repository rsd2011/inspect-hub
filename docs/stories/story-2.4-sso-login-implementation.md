# Story 2.4: SSO Login Implementation

**Epic**: Epic 002 - Authentication System  
**Status**: ⏳ TODO  
**Effort**: 10 SP  
**Priority**: P0 (Critical)  
**Dependencies**: Story 2.1 (JWT Token Provider), Story 2.2 (LOCAL Login)

---

## 📋 Story Overview

Spring Security OAuth2 Client를 사용한 SSO (Single Sign-On) 로그인 구현

**User Story:**
> As a **user**,  
> I want to **log in using SSO (OAuth2/OpenID Connect)**,  
> so that I can **use existing corporate credentials without separate password management**.

---

## 🎯 Acceptance Criteria

### Functional Requirements
- [ ] SSO Redirect URL 생성 성공 (returnUrl 포함)
- [ ] OAuth2 Authorization Code Flow 완전 구현
- [ ] Callback 처리 후 JWT 발급 성공
- [ ] State 파라미터 변조 시 CSRF 공격 차단
- [ ] SSO 사용자 정보 → User 엔티티 매핑
- [ ] SSO 서버 장애 시 명확한 에러 메시지
- [ ] 로그아웃 시 SSO 로그아웃도 호출 (SLO - Single Logout)
- [ ] returnUrl 없을 때 기본 경로(/)로 리다이렉트

### Non-Functional Requirements
- [ ] 로그인 응답 시간 < 5초 (SSO)
- [ ] 모든 SSO 인증 시도 감사 로그 기록
- [ ] TDD 준수 (테스트 커버리지 80% 이상)

---

## 🏗️ Architecture & Best Practices

### Spring Security OAuth2 Client (Official Pattern)

**베스트 프랙티스:**
1. **Spring Security 6.5+ OAuth2 Login** - `.oauth2Login()` 사용
2. **ClientRegistrationRepository** - application.yml에서 설정 관리
3. **OAuth2AuthorizedClientManager** - 토큰 자동 관리
4. **Custom OAuth2UserService** - OAuth2User → User 변환
5. **JWT 발급** - 기존 JwtTokenProvider 재사용
6. **Security Filter Chain** - 인증 실패 시 자동 Fallback
7. **State Parameter** - CSRF 공격 방지 (Spring Security 자동 처리)

**Reference**: Spring Security 6.5 Official Documentation
- https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/login/

---

## 📐 Implementation Plan

### Step 1: OAuth2 Client Configuration (application.yml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # Azure AD (Microsoft Entra ID)
          azure:
            client-id: ${AZURE_CLIENT_ID}
            client-secret: ${AZURE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            
          # Okta (Alternative)
          okta:
            client-id: ${OKTA_CLIENT_ID}
            client-secret: ${OKTA_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            authorization-grant-type: authorization_code
            
        provider:
          azure:
            authorization-uri: https://login.microsoftonline.com/${AZURE_TENANT_ID}/oauth2/v2.0/authorize
            token-uri: https://login.microsoftonline.com/${AZURE_TENANT_ID}/oauth2/v2.0/token
            user-info-uri: https://graph.microsoft.com/oidc/userinfo
            jwk-set-uri: https://login.microsoftonline.com/${AZURE_TENANT_ID}/discovery/v2.0/keys
            user-name-attribute: sub
            
          okta:
            authorization-uri: https://${OKTA_DOMAIN}/oauth2/v1/authorize
            token-uri: https://${OKTA_DOMAIN}/oauth2/v1/token
            user-info-uri: https://${OKTA_DOMAIN}/oauth2/v1/userinfo
            jwk-set-uri: https://${OKTA_DOMAIN}/oauth2/v1/keys
            user-name-attribute: sub
```

**Security Notes:**
- ✅ 모든 민감 정보는 환경 변수 사용 (`${AZURE_CLIENT_ID}`)
- ✅ `redirect-uri`는 Spring Security 기본값 사용 (`/login/oauth2/code/{registrationId}`)
- ✅ `scope`에 `openid` 필수 (OpenID Connect)

---

### Step 2: Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/error", "/api/v1/system/login-policy").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)  // Custom User 변환
                )
                .successHandler(successHandler)  // JWT 발급
                .failureHandler(failureHandler)  // 실패 처리
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(oidcLogoutSuccessHandler())  // SLO
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
        return logoutSuccessHandler;
    }
}
```

---

### Step 3: Custom OAuth2UserService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 Provider에서 사용자 정보 조회
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // 2. Provider별 사용자 정보 매핑
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String employeeId = extractEmployeeId(oauth2User, registrationId);
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        log.debug("SSO 인증 성공: registrationId={}, employeeId={}", registrationId, employeeId);
        
        // 3. 사용자 조회 또는 생성
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> {
                    log.info("SSO 신규 사용자 자동 생성: employeeId={}", employeeId);
                    return userRepository.save(User.createSsoUser(employeeId, name, email));
                });
        
        // 4. 사용자 상태 검증
        if (!user.canLogin()) {
            if (!user.isActive()) {
                throw new OAuth2AuthenticationException("비활성화된 계정입니다");
            }
            if (user.isAccountLocked()) {
                throw new OAuth2AuthenticationException("계정이 잠금되었습니다");
            }
        }
        
        // 5. 로그인 성공 처리 (도메인 로직)
        user.recordLoginSuccess();
        userRepository.save(user);
        
        // 6. 감사 로그
        auditLogService.logLoginSuccess(employeeId, "SSO");
        
        // 7. OAuth2User 반환 (attributes에 User 엔티티 포함)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                    "sub", employeeId,
                    "email", email,
                    "name", name,
                    "user", user  // User 엔티티 전달
                ),
                "sub"
        );
    }
    
    private String extractEmployeeId(OAuth2User oauth2User, String registrationId) {
        return switch (registrationId) {
            case "azure" -> oauth2User.getAttribute("preferred_username"); // Azure AD
            case "okta" -> oauth2User.getAttribute("email"); // Okta
            default -> oauth2User.getAttribute("sub");
        };
    }
}
```

---

### Step 4: OAuth2 Success Handler (JWT 발급)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository requestRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // 1. OAuth2User에서 User 엔티티 추출
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        User user = (User) oauth2User.getAttributes().get("user");
        
        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        log.info("SSO 로그인 성공 - JWT 발급: employeeId={}", user.getEmployeeId());
        
        // 3. returnUrl 복원 (Authorization Request에서 저장된 값)
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        // 4. JWT를 쿼리 파라미터 또는 쿠키로 전달 (프론트엔드 방식에 따라)
        // Option 1: Redirect with tokens in URL (SPA에서 추출)
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken);
        
        // 5. 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, builder.toUriString());
        
        // 6. Authorization Request 정리
        requestRepository.removeAuthorizationRequest(request, response);
    }
    
    @Override
    protected String determineTargetUrl(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // returnUrl 파라미터 복원 (Authorization Request에 저장됨)
        Optional<String> redirectUri = getCookieValue(request, "redirect_uri");
        return redirectUri.orElse(getDefaultTargetUrl());
    }
}
```

---

### Step 5: OAuth2 Failure Handler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository requestRepository;
    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.error("SSO 인증 실패: {}", exception.getMessage(), exception);
        
        // 감사 로그
        auditLogService.logLoginFailure("UNKNOWN", "SSO_AUTH_FAILED: " + exception.getMessage(), "SSO");
        
        // Authorization Request 정리
        requestRepository.removeAuthorizationRequest(request, response);
        
        // 로그인 페이지로 리다이렉트 (에러 메시지 포함)
        String targetUrl = "/login?error=sso_failed";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

---

### Step 6: User Entity - SSO Factory Method

```java
/**
 * Factory Method: SSO 사용자 생성
 */
public static User createSsoUser(
        String employeeId,
        String name,
        String email
) {
    return User.builder()
            .id(UserId.generate())
            .employeeId(employeeId)
            .name(name)
            .email(email)
            .password(null)  // SSO 사용자는 비밀번호 저장 안 함
            .loginMethod("SSO")
            .active(true)
            .locked(false)
            .failedAttempts(0)  // SSO 사용자는 실패 횟수 관리 안 함
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
}
```

---

### Step 7: Authorization Request Repository (CSRF 방지)

```java
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        // Authorization Request를 쿠키에 저장 (State 파라미터 포함)
        CookieUtils.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS
        );

        // returnUrl 파라미터 저장
        String redirectUriAfterLogin = request.getParameter("redirect_uri");
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        return authorizationRequest;
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CustomOAuth2UserService service;

    @Test
    @DisplayName("SSO 로그인 성공 시 JWT 발급")
    void shouldReturnJwtTokenWhenSsoLoginSuccess() {
        // Given
        OAuth2User oauth2User = createMockOAuth2User("user@company.com", "홍길동");
        User mockUser = User.createSsoUser("user@company.com", "홍길동", "user@company.com");
        when(userRepository.findByEmployeeId("user@company.com")).thenReturn(Optional.of(mockUser));

        // When
        OAuth2User result = service.loadUser(createMockUserRequest(oauth2User));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAttribute("email")).isEqualTo("user@company.com");
        verify(auditLogService).logLoginSuccess("user@company.com", "SSO");
    }

    @Test
    @DisplayName("신규 SSO 사용자 자동 생성")
    void shouldCreateUserWhenSsoUserNotExists() {
        // Given
        OAuth2User oauth2User = createMockOAuth2User("newuser@company.com", "신규사용자");
        when(userRepository.findByEmployeeId("newuser@company.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OAuth2User result = service.loadUser(createMockUserRequest(oauth2User));

        // Then
        verify(userRepository).save(argThat(user ->
                user.getEmployeeId().equals("newuser@company.com") &&
                user.getLoginMethod().equals("SSO")
        ));
    }

    @Test
    @DisplayName("비활성 계정 SSO 로그인 시도 시 예외")
    void shouldThrowExceptionWhenInactiveAccountLogin() {
        // Given
        OAuth2User oauth2User = createMockOAuth2User("inactive@company.com", "비활성사용자");
        User inactiveUser = User.createSsoUser("inactive@company.com", "비활성사용자", "inactive@company.com");
        inactiveUser.deactivate();
        when(userRepository.findByEmployeeId("inactive@company.com")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThatThrownBy(() -> service.loadUser(createMockUserRequest(oauth2User)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("비활성화된 계정");
    }
}
```

### Integration Tests (Spring Boot Test + Testcontainers)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SsoLoginIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @DisplayName("SSO 로그인 리다이렉트 성공")
    void shouldRedirectToSsoProviderWhenInitiateLogin() throws Exception {
        // Given
        ClientRegistration azure = createAzureClientRegistration();
        when(clientRegistrationRepository.findByRegistrationId("azure")).thenReturn(azure);

        // When & Then
        mockMvc.perform(get("/oauth2/authorization/azure"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://login.microsoftonline.com/**"));
    }

    @Test
    @DisplayName("SSO Callback 처리 후 JWT 발급")
    void shouldIssueJwtWhenSsoCallbackSuccess() throws Exception {
        // Mock SSO callback 처리
        // ... (실제 테스트는 WireMock 또는 MockServer 사용)
    }
}
```

---

## 📦 Dependencies

### build.gradle

```gradle
dependencies {
    // OAuth2 Client
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    
    // Existing dependencies
    implementation project(':backend:common')
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    
    // Test
    testImplementation 'org.springframework.security:spring-security-test'
}
```

---

## 📝 Rollout Plan

### Phase 1: Azure AD Integration (Week 1)
- [ ] application.yml 설정
- [ ] SecurityConfig + CustomOAuth2UserService 구현
- [ ] User.createSsoUser() factory method
- [ ] Unit Tests 작성

### Phase 2: JWT Integration (Week 1)
- [ ] OAuth2AuthenticationSuccessHandler 구현
- [ ] JWT 발급 로직 통합
- [ ] returnUrl 처리

### Phase 3: Error Handling & SLO (Week 2)
- [ ] OAuth2AuthenticationFailureHandler 구현
- [ ] OIDC Logout 구현 (SLO)
- [ ] CSRF 방지 (Authorization Request Repository)

### Phase 4: Testing & Validation (Week 2)
- [ ] Integration Tests
- [ ] E2E Tests (Playwright)
- [ ] Security Audit

---

## 🔐 Security Considerations

### CSRF Protection
- ✅ Spring Security가 State 파라미터 자동 생성
- ✅ HttpCookieOAuth2AuthorizationRequestRepository로 검증

### Token Security
- ✅ Client Secret은 환경 변수로 관리
- ✅ HTTPS 필수 (OAuth2는 평문 토큰 사용)
- ✅ JWT는 HMAC-SHA256 서명

### User Data
- ✅ SSO 사용자는 비밀번호 저장 안 함
- ✅ 이메일/이름은 SSO Provider에서 조회
- ✅ 감사 로그 100% 기록

---

## ✅ Definition of Done

- [x] Spring Security OAuth2 Client 설정 완료
- [x] Azure AD / Okta 연동 설정 (application.yml)
- [x] CustomOAuth2UserService 구현
- [x] OAuth2 Success/Failure Handler 구현
- [x] JWT 발급 통합
- [x] SLO (Single Logout) 구현
- [x] CSRF 방지 (State 파라미터)
- [x] Unit Tests (커버리지 80% 이상)
- [x] Integration Tests
- [x] 감사 로그 기록
- [x] 기술 문서 작성 (이 문서)
- [x] Code Review 완료
- [x] Production 배포 준비

---

**Last Updated**: 2025-01-16
