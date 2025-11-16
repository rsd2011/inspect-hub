# 🔐 인증 (Authentication)

## JWT (JSON Web Token) 인증

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

## JWT 구조

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

## 로그인 API

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

## Token Refresh

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

## Token 유효성 검증

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

## 비밀번호 정책

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

## Multi-Factor Authentication (MFA) - 향후 구현

**2FA 흐름:**
1. Username/Password 인증
2. OTP 발송 (Email/SMS)
3. OTP 검증
4. JWT 발급

---
