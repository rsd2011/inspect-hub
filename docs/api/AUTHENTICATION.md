# Authentication API Design

> **3가지 로그인 방식 지원: AD, SSO, 일반 로그인**

## Overview

Inspect-Hub 시스템은 내부 시스템 특성상 다음 3가지 인증 방식을 지원합니다:

1. **AD 로그인** - Active Directory 인증
2. **SSO 로그인** - Single Sign-On (Redirect 방식)
3. **일반 로그인** - 자체 DB 인증 (Fallback)

## Authentication Endpoints

### 1. AD 로그인

**Endpoint:** `POST /api/v1/auth/login/ad`

**Request:**
```json
{
  "employeeId": "EMP001",
  "password": "P@ssw0rd123"
}
```

**Response (성공):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "01HGW2N7XKQJBZ9VFQR8X7Y3ZT",
    "employeeId": "EMP001",
    "fullName": "홍길동",
    "orgId": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "orgName": "준법감시팀",
    "roles": ["ROLE_COMPLIANCE_OFFICER"]
  }
}
```

**Response (실패 - AD 서버 장애):**
```json
{
  "errorCode": "AD_SERVER_UNAVAILABLE",
  "message": "AD 서버에 연결할 수 없습니다. 일반 로그인을 시도해주세요.",
  "timestamp": "2025-01-13T10:30:00Z",
  "fallbackAvailable": true
}
```

**Response (실패 - 인증 실패):**
```json
{
  "errorCode": "INVALID_CREDENTIALS",
  "message": "사원ID 또는 비밀번호가 올바르지 않습니다.",
  "timestamp": "2025-01-13T10:30:00Z"
}
```

---

### 2. SSO 로그인

**Endpoint:** `GET /api/v1/auth/login/sso`

**Flow:**
1. Frontend → Backend API 호출
2. Backend → SSO 서버로 Redirect
3. SSO 서버 인증 완료 → Backend callback
4. Backend → JWT 발급 후 Frontend로 Redirect

**Query Parameters:**
- `returnUrl` (optional): 인증 성공 후 돌아갈 URL (default: `/`)

**Example Request:**
```
GET /api/v1/auth/login/sso?returnUrl=/dashboard
```

**SSO Callback Endpoint:** `GET /api/v1/auth/callback/sso`

**Callback Query Parameters (from SSO Server):**
```
GET /api/v1/auth/callback/sso?token=<sso_token>&state=<state>
```

**Final Redirect (성공):**
```
HTTP 302 Redirect
Location: http://frontend.inspecthub.example.com/dashboard?accessToken=<jwt>&refreshToken=<jwt>
```

**Response (실패 - SSO 서버 장애):**
```json
{
  "errorCode": "SSO_SERVER_UNAVAILABLE",
  "message": "SSO 서버에 연결할 수 없습니다. AD 또는 일반 로그인을 시도해주세요.",
  "timestamp": "2025-01-13T10:30:00Z",
  "fallbackAvailable": true
}
```

---

### 3. 일반 로그인 (Fallback)

**Endpoint:** `POST /api/v1/auth/login/local`

**Request:**
```json
{
  "employeeId": "EMP001",
  "password": "P@ssw0rd123"
}
```

**Response (성공):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "01HGW2N7XKQJBZ9VFQR8X7Y3ZT",
    "employeeId": "EMP001",
    "fullName": "홍길동",
    "orgId": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "orgName": "준법감시팀",
    "roles": ["ROLE_COMPLIANCE_OFFICER"]
  }
}
```

**Response (실패):**
```json
{
  "errorCode": "INVALID_CREDENTIALS",
  "message": "사원ID 또는 비밀번호가 올바르지 않습니다.",
  "timestamp": "2025-01-13T10:30:00Z"
}
```

---

### 4. 서버 상태 체크 (Health Check)

**Endpoint:** `GET /api/v1/auth/health`

**Response:**
```json
{
  "ad": {
    "available": true,
    "responseTime": 120
  },
  "sso": {
    "available": false,
    "responseTime": null,
    "error": "Connection timeout"
  },
  "local": {
    "available": true,
    "responseTime": 5
  },
  "recommendedMethod": "ad"
}
```

---

### 5. 토큰 갱신

**Endpoint:** `POST /api/v1/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 6. 로그아웃

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

## Authentication Flow Diagram

```
┌─────────────────┐
│  Frontend (SPA) │
└────────┬────────┘
         │
         │ 1. User clicks login
         ▼
   ┌──────────────┐
   │ Login Method │
   │   Selection  │
   └──────┬───────┘
          │
    ┌─────┴─────────────────────────┐
    │                               │
    ▼                               ▼
┌────────┐                    ┌──────────┐
│   AD   │                    │   SSO    │
│ Login  │                    │  Login   │
└───┬────┘                    └────┬─────┘
    │                              │
    │ POST /auth/login/ad          │ GET /auth/login/sso
    │                              │
    ▼                              ▼
┌─────────────────────────────────────────┐
│         Backend Spring Boot             │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐  │
│  │ 1. AD Server Health Check        │  │
│  │    ↓ Available?                  │  │
│  │    YES → AD Authentication       │  │
│  │    NO → Fallback to Local        │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │ 2. SSO Server Redirect           │  │
│  │    ↓ Available?                  │  │
│  │    YES → SSO Redirect            │  │
│  │    NO → Return error             │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │ 3. Local DB Authentication       │  │
│  │    ↓ BCrypt password check       │  │
│  │    ↓ Generate JWT token          │  │
│  └──────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
             │
             │ JWT Token
             ▼
      ┌──────────────┐
      │   Frontend   │
      │ Store Token  │
      │  in Pinia    │
      └──────────────┘
```

---

## Implementation Details

### Backend (Spring Security)

**AuthenticationService Interface:**
```java
public interface AuthenticationService {
    AuthenticationResponse authenticateWithAD(String employeeId, String password);
    AuthenticationResponse authenticateWithSSO(String ssoToken);
    AuthenticationResponse authenticateWithLocal(String employeeId, String password);
    AuthServerHealthStatus checkAuthServersHealth();
}
```

**ADAuthenticationProvider:**
```java
@Component
public class ADAuthenticationProvider implements AuthenticationProvider {

    @Value("${auth.ad.server-url}")
    private String adServerUrl;

    @Value("${auth.ad.timeout}")
    private Duration timeout; // 3초

    public AuthenticationResponse authenticate(String employeeId, String password) {
        try {
            // LDAP/Kerberos 인증 로직
            LdapContext ctx = createLdapContext(employeeId, password);
            User user = loadUserFromAD(ctx, employeeId);
            return generateJwtToken(user);
        } catch (NamingException e) {
            throw new ADAuthenticationException("AD 인증 실패", e);
        } catch (TimeoutException e) {
            throw new ADServerUnavailableException("AD 서버 연결 실패", e);
        }
    }
}
```

**SSOAuthenticationProvider:**
```java
@Component
public class SSOAuthenticationProvider {

    @Value("${auth.sso.server-url}")
    private String ssoServerUrl;

    @Value("${auth.sso.timeout}")
    private Duration timeout; // 5초

    public String initiateSSO(String returnUrl) {
        try {
            // SSO 서버로 Redirect URL 생성
            String redirectUrl = ssoServerUrl + "/login?returnUrl=" +
                                 URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            return redirectUrl;
        } catch (Exception e) {
            throw new SSOServerUnavailableException("SSO 서버 연결 실패", e);
        }
    }

    public AuthenticationResponse handleCallback(String ssoToken) {
        // SSO 토큰 검증 및 사용자 정보 조회
        User user = loadUserFromSSO(ssoToken);
        return generateJwtToken(user);
    }
}
```

**LocalAuthenticationProvider:**
```java
@Component
public class LocalAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthenticationResponse authenticate(String employeeId, String password) {
        User user = userRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new InvalidCredentialsException("사원ID 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("사원ID 또는 비밀번호가 올바르지 않습니다.");
        }

        return generateJwtToken(user);
    }
}
```

---

### Frontend (Nuxt 4)

**Login Page Component:**
```vue
<script setup lang="ts">
const authStore = useAuthStore()
const loginMethod = ref<'ad' | 'sso' | 'local'>('ad')
const employeeId = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    if (loginMethod.value === 'sso') {
      // SSO 로그인 - Backend로 Redirect
      window.location.href = `/api/v1/auth/login/sso?returnUrl=${encodeURIComponent('/dashboard')}`
    } else {
      // AD 또는 일반 로그인
      const endpoint = loginMethod.value === 'ad' ? '/auth/login/ad' : '/auth/login/local'
      const response = await authStore.login(endpoint, {
        employeeId: employeeId.value,
        password: password.value
      })

      // JWT 토큰 저장 및 대시보드로 이동
      navigateTo('/dashboard')
    }
  } catch (error) {
    if (error.code === 'AD_SERVER_UNAVAILABLE' || error.code === 'SSO_SERVER_UNAVAILABLE') {
      // Fallback 옵션 제공
      showFallbackDialog()
    } else {
      showError(error.message)
    }
  } finally {
    loading.value = false
  }
}

const checkAuthHealth = async () => {
  const health = await $fetch('/api/v1/auth/health')

  if (!health.sso.available && !health.ad.available) {
    // SSO, AD 모두 장애 → 일반 로그인만 제공
    loginMethod.value = 'local'
    showWarning('SSO 및 AD 서버 장애로 일반 로그인만 가능합니다.')
  } else if (health.recommendedMethod) {
    loginMethod.value = health.recommendedMethod
  }
}

onMounted(() => {
  checkAuthHealth()
})
</script>

<template>
  <div class="tw-flex tw-items-center tw-justify-center tw-min-h-screen">
    <div class="tw-w-full tw-max-w-md tw-p-8 tw-bg-white tw-rounded-lg tw-shadow-lg">
      <h1 class="tw-text-2xl tw-font-bold tw-mb-6">Inspect-Hub 로그인</h1>

      <!-- 로그인 방식 선택 -->
      <div class="tw-mb-6">
        <label class="tw-block tw-mb-2 tw-font-medium">로그인 방식</label>
        <div class="tw-flex tw-gap-4">
          <Button
            :label="AD 로그인"
            :severity="loginMethod === 'ad' ? 'primary' : 'secondary'"
            @click="loginMethod = 'ad'"
          />
          <Button
            label="SSO 로그인"
            :severity="loginMethod === 'sso' ? 'primary' : 'secondary'"
            @click="loginMethod = 'sso'"
          />
          <Button
            label="일반 로그인"
            :severity="loginMethod === 'local' ? 'primary' : 'secondary'"
            @click="loginMethod = 'local'"
          />
        </div>
      </div>

      <!-- 사원ID/비밀번호 입력 (SSO 제외) -->
      <div v-if="loginMethod !== 'sso'">
        <FormField label="사원ID" required>
          <Input v-model="employeeId" placeholder="EMP001" />
        </FormField>

        <FormField label="비밀번호" required>
          <Input v-model="password" type="password" />
        </FormField>
      </div>

      <!-- SSO 안내 -->
      <div v-else class="tw-p-4 tw-bg-blue-50 tw-rounded tw-mb-4">
        <p class="tw-text-sm">
          SSO 서버로 이동하여 인증을 진행합니다.
        </p>
      </div>

      <!-- 로그인 버튼 -->
      <Button
        label="로그인"
        :loading="loading"
        @click="handleLogin"
        class="tw-w-full"
      />
    </div>
  </div>
</template>
```

---

## Configuration

### application.yml (Backend)

```yaml
auth:
  ad:
    enabled: true
    server-url: ldap://ad.company.com:389
    base-dn: DC=company,DC=com
    timeout: 3s

  sso:
    enabled: true
    server-url: https://sso.company.com
    client-id: inspect-hub
    client-secret: ${SSO_CLIENT_SECRET}
    callback-url: ${BASE_URL}/api/v1/auth/callback/sso
    timeout: 5s

  local:
    enabled: true

  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: 3600  # 1시간
    refresh-token-expiration: 86400  # 24시간
```

---

## Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `INVALID_CREDENTIALS` | 401 | 사원ID 또는 비밀번호 오류 |
| `AD_SERVER_UNAVAILABLE` | 503 | AD 서버 장애 |
| `SSO_SERVER_UNAVAILABLE` | 503 | SSO 서버 장애 |
| `INVALID_SSO_TOKEN` | 401 | 잘못된 SSO 토큰 |
| `TOKEN_EXPIRED` | 401 | JWT 토큰 만료 |
| `INVALID_TOKEN` | 401 | 잘못된 JWT 토큰 |

---

## Testing Checklist

- [ ] AD 로그인 성공 테스트
- [ ] AD 로그인 실패 테스트 (잘못된 사원ID/비밀번호)
- [ ] AD 서버 장애 시 Fallback 테스트
- [ ] SSO 로그인 Redirect 테스트
- [ ] SSO Callback 처리 테스트
- [ ] SSO 서버 장애 시 에러 핸들링 테스트
- [ ] 일반 로그인 성공 테스트
- [ ] 일반 로그인 실패 테스트
- [ ] 서버 Health Check API 테스트
- [ ] JWT 토큰 갱신 테스트
- [ ] 로그아웃 테스트
- [ ] 로그인 이력 Audit Log 기록 테스트
