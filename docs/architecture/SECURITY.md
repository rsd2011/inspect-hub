# Security Architecture & Implementation

> **Inspect-Hub 보안 아키텍처 및 구현 가이드**

## 📚 목차

1. [보안 개요](#-보안-개요)
2. [인증 (Authentication)](#-인증-authentication)
3. [인가 (Authorization)](#-인가-authorization)
4. [데이터 보호](#-데이터-보호)
5. [API 보안](#-api-보안)
6. [세션 관리](#-세션-관리)
7. [감사 로깅](#-감사-로깅)
8. [보안 헤더](#-보안-헤더)
9. [취약점 방어](#-취약점-방어)
10. [모니터링 및 알림](#-모니터링-및-알림)
11. [보안 테스트](#-보안-테스트)
12. [규정 준수](#-규정-준수)

---

## 🔒 보안 개요

### 보안 목표

| 목표 | 설명 | 구현 방법 |
|------|------|-----------|
| **기밀성 (Confidentiality)** | 인가된 사용자만 접근 | 암호화, 접근 제어 |
| **무결성 (Integrity)** | 데이터 변조 방지 | 감사 로깅, 체크섬 |
| **가용성 (Availability)** | 서비스 지속성 보장 | Rate Limiting, 모니터링 |
| **책임추적성 (Accountability)** | 모든 행위 추적 | 100% 감사 로깅 |
| **부인방지 (Non-repudiation)** | 행위 부인 불가 | 디지털 서명, 감사 로그 |

### 보안 계층

```
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  - Input Validation                                      │
│  - Business Logic Security                               │
│  - Authorization (RBAC + SoD)                           │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   API Gateway Layer                      │
│  - Authentication (JWT)                                  │
│  - Rate Limiting                                         │
│  - Request Validation                                    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Network Layer                          │
│  - TLS/SSL (HTTPS)                                      │
│  - Firewall Rules                                        │
│  - VPC/Subnet Isolation                                 │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                             │
│  - Encryption at Rest                                    │
│  - Field-level Encryption (PII)                         │
│  - Database Access Control                               │
└─────────────────────────────────────────────────────────┘
```

### 위협 모델 (STRIDE)

| 위협 | 대응책 |
|------|--------|
| **S**poofing (위장) | JWT 인증, 강력한 비밀번호 정책 |
| **T**ampering (변조) | 감사 로깅, 체크섬, 불변 스냅샷 |
| **R**epudiation (부인) | 100% 감사 로깅, 타임스탬프 |
| **I**nformation Disclosure (정보 유출) | 암호화, 접근 제어, 데이터 마스킹 |
| **D**enial of Service (서비스 거부) | Rate Limiting, WAF, DDoS 방어 |
| **E**levation of Privilege (권한 상승) | RBAC, SoD, 최소 권한 원칙 |

---

## 🔐 인증 (Authentication)

### JWT (JSON Web Token) 인증

**Architecture:**
```
┌────────────┐     ┌────────────────┐     ┌─────────────┐
│   Client   │────▶│ Authentication │────▶│   Backend   │
│            │     │    Server      │     │     API     │
└────────────┘     └────────────────┘     └─────────────┘
      │                    │                      │
      │  1. Login Request  │                      │
      │───────────────────▶│                      │
      │                    │  2. Validate         │
      │                    │     Credentials      │
      │                    │                      │
      │  3. JWT Token      │                      │
      │◀───────────────────│                      │
      │                    │                      │
      │  4. API Request    │                      │
      │    + JWT Token     │                      │
      │───────────────────────────────────────────▶│
      │                    │  5. Validate Token   │
      │                    │                      │
      │  6. Response       │                      │
      │◀───────────────────────────────────────────│
```

### JWT 구조

```typescript
// JWT Header
{
  "alg": "HS256",     // 서명 알고리즘
  "typ": "JWT"
}

// JWT Payload
{
  "sub": "01ARZ3NDEKTSV4RRFFQ69G5FAV",  // User ID (ULID)
  "username": "admin",
  "email": "admin@example.com",
  "roles": [
    "ROLE_ADMIN",
    "ROLE_COMPLIANCE_OFFICER"
  ],
  "permissions": [
    "user:read",
    "user:write",
    "policy:approve",
    "case:investigate"
  ],
  "orgId": "01ORG123...",                // Organization ID
  "iat": 1673600000,                     // Issued At
  "exp": 1673603600,                     // Expiration (1 hour)
  "jti": "01JWT456..."                   // JWT ID (unique)
}

// JWT Signature
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

### 로그인 API

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "SecurePassword123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH123...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "01ARZ3...",
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

### Token Refresh

**Request:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "01REFRESH123..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "01REFRESH456...",
    "expiresIn": 3600
  }
}
```

### Token 유효성 검증

**SecurityFilter 구현:**

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extract JWT from Authorization header
        String token = extractToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 2. Get user details from token
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 3. Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            
            // 4. Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 비밀번호 정책

**강력한 비밀번호 요구사항:**
- 최소 8자 이상
- 영문 대문자 포함
- 영문 소문자 포함
- 숫자 포함
- 특수문자 포함 (선택)

**비밀번호 저장:**
```java
@Service
@RequiredArgsConstructor
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder; // BCrypt
    
    // 비밀번호 해싱
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    // 비밀번호 검증
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
```

**Spring Security Configuration:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Cost factor: 12
}
```

### Multi-Factor Authentication (MFA) - 향후 구현

**2FA 흐름:**
1. Username/Password 인증
2. OTP 발송 (Email/SMS)
3. OTP 검증
4. JWT 발급

---

## 🛡️ 인가 (Authorization)

### RBAC (Role-Based Access Control)

**Role 계층 구조:**

```
ROLE_SUPER_ADMIN (최고 관리자)
    ↓
ROLE_ADMIN (관리자)
    ↓
ROLE_COMPLIANCE_OFFICER (준법감시 담당자)
    ↓
ROLE_INVESTIGATOR (조사자)
    ↓
ROLE_REVIEWER (검토자)
    ↓
ROLE_USER (일반 사용자)
```

**Permission 구조:**

```
{resource}:{action}

Examples:
- user:read      # 사용자 조회
- user:write     # 사용자 생성/수정
- user:delete    # 사용자 삭제
- policy:read
- policy:write
- policy:approve # 정책 승인
- case:read
- case:write
- case:approve   # 사례 승인
- report:read
- report:submit  # FIU 보고서 제출
```

### 권한 체크 방법

#### 1. Method Security (권장)

```java
@Service
public class PolicyService {
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('policy:write')")
    public Policy createPolicy(CreatePolicyRequest request) {
        // ...
    }
    
    @PreAuthorize("hasAuthority('policy:approve')")
    public void approvePolicy(String policyId) {
        // ...
    }
    
    @PreAuthorize("@permissionEvaluator.canAccessPolicy(#policyId)")
    public Policy getPolicy(String policyId) {
        // Custom permission evaluator
    }
}
```

#### 2. Controller Security

```java
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    
    @PostMapping
    @PreAuthorize("hasAuthority('policy:write')")
    public ResponseEntity<?> createPolicy(@RequestBody CreatePolicyRequest request) {
        // ...
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('policy:approve')")
    public ResponseEntity<?> approvePolicy(@PathVariable String id) {
        // ...
    }
}
```

#### 3. Programmatic Check

```java
@Service
@RequiredArgsConstructor
public class CaseService {
    
    private final PermissionManager permissionManager;
    
    public void assignCase(String caseId, String userId) {
        // Check permission programmatically
        if (!permissionManager.hasPermission("case:assign")) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        
        // Business logic
    }
}
```

### Separation of Duties (SoD)

**Maker-Checker 원칙:**

```java
@Service
@RequiredArgsConstructor
public class ApprovalService {
    
    public void approveCase(String caseId, String approverId) {
        Case case = caseRepository.findById(caseId)
            .orElseThrow(() -> new CaseNotFoundException(caseId));
        
        // SoD: 생성자와 승인자가 같으면 안됨
        if (case.getCreatedBy().equals(approverId)) {
            throw new SeparationOfDutiesViolationException(
                "Maker cannot be the same as Checker"
            );
        }
        
        // Approval logic
        case.setStatus("APPROVED");
        case.setApprovedBy(approverId);
        case.setApprovedAt(LocalDateTime.now());
        
        caseRepository.update(case);
        auditLogger.log("CASE_APPROVED", caseId, approverId);
    }
}
```

### Data-Level Permission (행/필드 수준 권한)

**Row-Level Security:**

```java
@Service
public class CaseQueryService {
    
    public List<Case> listCases(String userId) {
        User user = userRepository.findById(userId);
        DataPolicy policy = dataPolicyRepository.findByUserRole(user.getRole());
        
        // Scope 기반 필터링
        switch (policy.getRowScope()) {
            case "OWN":
                // 본인이 생성한 사례만
                return caseRepository.findByCreatedBy(userId);
            
            case "ORG":
                // 동일 조직의 사례만
                return caseRepository.findByOrganization(user.getOrgId());
            
            case "ALL":
                // 전체 사례
                return caseRepository.findAll();
            
            default:
                return Collections.emptyList();
        }
    }
}
```

**Field-Level Masking:**

```java
@Service
public class DataMaskingService {
    
    public User maskSensitiveFields(User user, String viewerId) {
        DataPolicy policy = dataPolicyRepository.findByUser(viewerId);
        
        // PII 마스킹
        if (policy.shouldMask("ssn")) {
            user.setSsn(mask(user.getSsn(), MaskType.PARTIAL)); // "123-45-6789" → "***-**-6789"
        }
        
        if (policy.shouldMask("email")) {
            user.setEmail(mask(user.getEmail(), MaskType.EMAIL)); // "admin@example.com" → "a***n@example.com"
        }
        
        return user;
    }
    
    private String mask(String value, MaskType type) {
        // Masking logic
    }
}
```

---

## 🔐 데이터 보호

### Encryption at Rest

**데이터베이스 암호화:**

```sql
-- PostgreSQL TDE (Transparent Data Encryption)
-- 또는 파일 시스템 레벨 암호화 (LUKS, BitLocker)
```

**필드 레벨 암호화 (PII):**

```java
@Entity
@Table(name = "user")
public class User {
    
    @Id
    private String id;
    
    private String username;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String ssn;  // 주민등록번호 암호화
    
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;  // 전화번호 암호화
}
```

**Converter 구현:**

```java
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionService.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptionService.decrypt(dbData);
    }
}
```

**AES-256 암호화:**

```java
@Service
public class EncryptionService {
    
    @Value("${encryption.secret-key}")
    private String secretKey;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    
    public String encrypt(String plainText) {
        try {
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            byte[] iv = new byte[12];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // IV + Encrypted Data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[12];
            byte[] encryptedData = new byte[combined.length - 12];
            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, encryptedData, 0, encryptedData.length);
            
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptionException("Decryption failed", e);
        }
    }
    
    private SecretKey generateKey() throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 32); // AES-256
        return new SecretKeySpec(keyBytes, "AES");
    }
}
```

### Encryption in Transit

**TLS/SSL (HTTPS):**

```yaml
# application.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: inspect-hub
    protocol: TLS
    enabled-protocols: TLSv1.3,TLSv1.2
```

**Certificate 생성:**

```bash
# Self-signed certificate (개발)
keytool -genkeypair -alias inspect-hub \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365

# Production: Let's Encrypt 또는 CA 발급 인증서 사용
```

---

## 🔒 API 보안

### Rate Limiting

**Bucket4j 구현:**

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userId = extractUserId(httpRequest);
        
        Bucket bucket = resolveBucket(userId);
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("X-RateLimit-Retry-After", "60");
            httpResponse.getWriter().write(
                "{\"success\":false,\"message\":\"Rate limit exceeded\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\"}"
            );
        }
    }
    
    private Bucket resolveBucket(String userId) {
        return cache.computeIfAbsent(userId, k -> createBucket());
    }
    
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

**Rate Limit 정책:**

| 사용자 타입 | 제한 | 기간 |
|-------------|------|------|
| Anonymous | 10 req | 1분 |
| Authenticated | 100 req | 1분 |
| Admin | 500 req | 1분 |
| System | Unlimited | - |

### CORS 설정

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",           // 개발 환경
            "https://app.inspecthub.com"       // 프로덕션
        ));
        
        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));
        
        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "Location",
            "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

### CSRF Protection

**SPA 모드에서는 CSRF 보호 비활성화 (JWT 사용):**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // JWT 사용 시 비활성화
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // ... other configurations
            ;
        
        return http.build();
    }
}
```

---

## 🕐 세션 관리

### Stateless Session (JWT)

**세션 저장소 없음:**
- JWT에 모든 사용자 정보 포함
- 서버는 상태를 저장하지 않음 (Stateless)
- 확장성 우수

### Token Expiration

**Access Token:**
- 유효기간: 1시간
- 짧은 수명으로 보안 강화

**Refresh Token:**
- 유효기간: 7일
- Access Token 갱신용
- Redis에 저장하여 무효화 가능

```java
@Service
@RequiredArgsConstructor
public class TokenService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final long ACCESS_TOKEN_VALIDITY = 60 * 60; // 1 hour
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days
    
    public String createAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId())
            .claim("username", user.getUsername())
            .claim("roles", user.getRoles())
            .claim("permissions", user.getPermissions())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
    }
    
    public String createRefreshToken(String userId) {
        String refreshToken = UlidGenerator.generate();
        
        // Store in Redis with expiration
        redisTemplate.opsForValue().set(
            "refresh_token:" + refreshToken,
            userId,
            REFRESH_TOKEN_VALIDITY,
            TimeUnit.SECONDS
        );
        
        return refreshToken;
    }
    
    public void invalidateRefreshToken(String refreshToken) {
        redisTemplate.delete("refresh_token:" + refreshToken);
    }
}
```

### Session Timeout Warning

**Frontend에서 구현:**

```typescript
// 5분 전 경고
if (tokenExpiresIn < 5 * 60) {
  showSessionWarning("Your session will expire in 5 minutes");
}

// 1분 전 경고
if (tokenExpiresIn < 1 * 60) {
  showSessionWarning("Your session will expire in 1 minute. Please save your work.");
}

// 만료 시 자동 로그아웃
if (tokenExpiresIn <= 0) {
  logout();
}
```

### Multi-Tab Synchronization

**BroadcastChannel API 사용:**

```typescript
const channel = new BroadcastChannel('auth_channel');

// 로그아웃 시 모든 탭에 알림
channel.postMessage({ type: 'LOGOUT' });

// 다른 탭에서 로그아웃 감지
channel.onmessage = (event) => {
  if (event.data.type === 'LOGOUT') {
    logout(); // 현재 탭도 로그아웃
  }
};
```

---

## 📝 감사 로깅

### 100% 감사 로깅

**로깅 대상:**
- 모든 API 호출
- 데이터 생성/수정/삭제
- 로그인/로그아웃
- 권한 변경
- 정책 승인/배포
- 사례 조사 활동

### Audit Log 구조

```java
@Data
@Builder
public class AuditLog {
    private String id;                    // ULID
    private String userId;                // 사용자 ID
    private String username;              // 사용자명
    private String action;                // 행위 (CREATE, READ, UPDATE, DELETE, APPROVE, etc.)
    private String resource;              // 리소스 타입 (USER, POLICY, CASE, etc.)
    private String resourceId;            // 리소스 ID
    private String beforeValue;           // 변경 전 값 (JSON)
    private String afterValue;            // 변경 후 값 (JSON)
    private String ipAddress;             // IP 주소
    private String userAgent;             // User Agent
    private LocalDateTime timestamp;      // 타임스탬프
    private String result;                // 결과 (SUCCESS, FAILURE)
    private String errorMessage;          // 에러 메시지 (실패 시)
}
```

### AOP 기반 자동 로깅

```java
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLoggingAspect {
    
    private final AuditLogRepository auditLogRepository;
    
    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Before execution
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String action = auditable.action();
        String resource = auditable.resource();
        
        Object result = null;
        String status = "SUCCESS";
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            status = "FAILURE";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // After execution (success or failure)
            AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .timestamp(LocalDateTime.now())
                .result(status)
                .errorMessage(errorMessage)
                .build();
            
            auditLogRepository.insert(auditLog);
        }
        
        return result;
    }
}
```

**사용 예시:**

```java
@Service
public class PolicyService {
    
    @Auditable(action = "CREATE", resource = "POLICY")
    public Policy createPolicy(CreatePolicyRequest request) {
        // Business logic
    }
    
    @Auditable(action = "APPROVE", resource = "POLICY")
    public void approvePolicy(String policyId) {
        // Business logic
    }
}
```

### 감사 로그 보존

**보존 기간:**
- 최소 7년 (금융 규정)
- 별도 데이터베이스 저장
- 읽기 전용 복제본 생성
- 정기적 백업

**아카이빙:**
```sql
-- 1년 이상 된 로그는 별도 테이블로 이동
INSERT INTO audit_log_archive
SELECT * FROM audit_log
WHERE timestamp < NOW() - INTERVAL '1 year';

DELETE FROM audit_log
WHERE timestamp < NOW() - INTERVAL '1 year';
```

---

## 🛡️ 보안 헤더

### Security Headers

```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = 
            new FilterRegistrationBean<>();
        
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/api/*");
        
        return registration;
    }
}

public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Strict-Transport-Security (HSTS)
        httpResponse.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        // X-Content-Type-Options
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // X-XSS-Protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Content-Security-Policy
        httpResponse.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data:; " +
            "font-src 'self'; " +
            "connect-src 'self'"
        );
        
        // Referrer-Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy
        httpResponse.setHeader("Permissions-Policy",
            "geolocation=(), microphone=(), camera=()"
        );
        
        chain.doFilter(request, response);
    }
}
```

---

## 🚫 취약점 방어

### SQL Injection 방어

**MyBatis Parameterized Queries 사용:**

```xml
<!-- ✅ Good - Parameterized -->
<select id="findByEmail" resultType="User">
    SELECT * FROM "user"
    WHERE email = #{email}
      AND deleted = FALSE
</select>

<!-- ❌ Bad - String concatenation (VULNERABLE!) -->
<select id="findByEmail" resultType="User">
    SELECT * FROM "user"
    WHERE email = '${email}'
</select>
```

### XSS (Cross-Site Scripting) 방어

**입력 검증 및 출력 인코딩:**

```java
@Service
public class XssProtectionService {
    
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return policy.sanitize(input);
    }
}
```

**Frontend에서도 방어:**
```typescript
// Vue 3는 기본적으로 XSS 방어
<template>
  <div>{{ userInput }}</div>  <!-- 자동 이스케이프 -->
  <div v-html="sanitizedHtml"></div>  <!-- 검증된 HTML만 -->
</template>
```

### CSRF 방어

**JWT 사용 시 자동 방어:**
- Stateless 방식
- Cookie 미사용
- CSRF 토큰 불필요

### Path Traversal 방어

```java
@Service
public class FileService {
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    public File getFile(String filename) {
        // Validate filename
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Invalid filename");
        }
        
        // Resolve canonical path
        File file = new File(uploadDir, filename);
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(uploadDir)) {
                throw new SecurityException("Path traversal attempt detected");
            }
        } catch (IOException e) {
            throw new SecurityException("Invalid file path");
        }
        
        return file;
    }
}
```

### Command Injection 방어

**ProcessBuilder 사용 (Runtime.exec() 지양):**

```java
// ✅ Good
ProcessBuilder pb = new ProcessBuilder("command", "arg1", "arg2");
Process process = pb.start();

// ❌ Bad - Command injection vulnerable
Runtime.getRuntime().exec("command " + userInput);
```

---

## 📊 모니터링 및 알림

### 보안 이벤트 모니터링

**감지 대상:**
- 반복된 로그인 실패 (Brute Force)
- 권한 상승 시도
- 비정상적인 API 호출 패턴
- Rate Limit 초과
- SQL Injection 시도
- XSS 시도

### 자동 알림

**Slack/Email 알림:**

```java
@Service
@RequiredArgsConstructor
public class SecurityAlertService {
    
    private final SlackNotifier slackNotifier;
    
    public void alertBruteForceAttempt(String username, String ipAddress, int attemptCount) {
        if (attemptCount >= 5) {
            slackNotifier.send(
                "#security-alerts",
                String.format(
                    "🚨 Brute force attack detected!\n" +
                    "Username: %s\n" +
                    "IP: %s\n" +
                    "Attempts: %d",
                    username, ipAddress, attemptCount
                )
            );
        }
    }
    
    public void alertUnauthorizedAccess(String userId, String resource) {
        slackNotifier.send(
            "#security-alerts",
            String.format(
                "⚠️ Unauthorized access attempt!\n" +
                "User: %s\n" +
                "Resource: %s",
                userId, resource
            )
        );
    }
}
```

---

## 🧪 보안 테스트

### OWASP Top 10 체크리스트

| 취약점 | 대응 방법 | 테스트 방법 |
|--------|-----------|-------------|
| **A01: Broken Access Control** | RBAC, SoD | 권한 우회 시도 |
| **A02: Cryptographic Failures** | TLS, AES-256 | 암호화 강도 검증 |
| **A03: Injection** | Parameterized Queries | SQL Injection 테스트 |
| **A04: Insecure Design** | Threat Modeling | 설계 리뷰 |
| **A05: Security Misconfiguration** | 보안 헤더 | 설정 스캔 |
| **A06: Vulnerable Components** | 의존성 스캔 | OWASP Dependency Check |
| **A07: Authentication Failures** | 강력한 비밀번호, MFA | Brute Force 테스트 |
| **A08: Software/Data Integrity** | 감사 로깅 | 무결성 검증 |
| **A09: Logging Failures** | 100% 로깅 | 로그 누락 확인 |
| **A10: SSRF** | URL 검증 | SSRF 시도 |

### 자동화된 보안 스캔

```bash
# OWASP Dependency Check
./gradlew dependencyCheckAnalyze

# SonarQube 분석
./gradlew sonarqube

# SAST (Static Application Security Testing)
checkmarx scan --project=inspect-hub

# DAST (Dynamic Application Security Testing)
zap-cli quick-scan http://localhost:8090/api
```

---

## 📜 규정 준수

### 금융 규정

- **전자금융거래법** 준수
- **개인정보보호법** 준수
- **신용정보법** 준수
- **FATF 권고사항** 이행

### 데이터 보호

- **암호화**: 전송/저장 모두 암호화
- **접근 제어**: RBAC + SoD
- **감사 로깅**: 100% 로깅, 7년 보존
- **데이터 마스킹**: PII 필드 마스킹

### 정기 보안 점검

**분기별:**
- 보안 취약점 스캔
- 의존성 업데이트
- 접근 권한 검토

**연간:**
- 외부 보안 감사
- 침투 테스트
- 재해 복구 훈련

---

## 📞 참고 문서

- [API Security](../api/DESIGN.md#-보안)
- [Backend Security Config](../backend/README.md)
- [Audit Logging](../architecture/DATABASE.md#감사-로그)
- [Deployment Security](./DEPLOYMENT.md#보안-설정)

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | Security Architecture 초안 작성 | PM |

---

**Security Checklist:**

- [x] JWT 인증 구현
- [x] RBAC + SoD 구현
- [x] 데이터 암호화 (at rest + in transit)
- [x] Rate Limiting 구현
- [x] 100% 감사 로깅
- [x] 보안 헤더 설정
- [x] SQL Injection 방어
- [x] XSS 방어
- [x] 보안 모니터링 및 알림
- [ ] MFA 구현 (향후)
- [ ] WAF 구성 (프로덕션)
- [ ] DDoS 방어 (프로덕션)
