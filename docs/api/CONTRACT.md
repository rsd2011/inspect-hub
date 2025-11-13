# Frontend ↔ Backend API Contract

프론트엔드와 백엔드 간 API 계약 문서입니다. 양측이 이 문서를 기준으로 개발하고 동기화합니다.

**최종 업데이트:** 2025-01-07

## 📋 API 응답 표준 형식

모든 API는 `ApiResponse<T>` 래퍼를 사용합니다.

### 성공 응답

```json
{
  "success": true,
  "message": "Optional success message",
  "data": { /* Response data */ },
  "timestamp": "2025-01-07T10:30:00"
}
```

### 에러 응답

```json
{
  "success": false,
  "message": "Error message",
  "errorCode": "ERR_CODE",
  "timestamp": "2025-01-07T10:30:00"
}
```

## 🔐 Authentication APIs

### 1. POST `/v1/auth/login` - 전통 방식 로그인

**Request:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response:** `ApiResponse<LoginResponse>`
```json
{
  "success": true,
  "data": {
    "userId": "usr_01234567",
    "username": "admin",
    "email": "admin@inspecthub.com",
    "displayName": "관리자",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "roles": ["ROLE_ADMIN", "ROLE_USER"],
    "permissions": ["READ_USER", "WRITE_USER", "READ_CASE"],
    "organizationId": "org_12345",
    "organizationName": "본사",
    "expiresIn": 3600000,
    "tokenType": "Bearer",
    "loginMethod": "traditional"
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

**Error Response (401):**
```json
{
  "success": false,
  "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
  "errorCode": "AUTH_001",
  "timestamp": "2025-01-07T10:30:00"
}
```

### 2. POST `/v1/auth/sso/login` - SSO 로그인

**Request:**
```json
{
  "ssoToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "provider": "okta"
}
```

**Response:** `ApiResponse<LoginResponse>` (위와 동일, `loginMethod: "sso"`)

### 3. POST `/v1/auth/refresh` - 토큰 갱신

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** `ApiResponse<TokenRefreshResponse>`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600000
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

### 4. POST `/v1/auth/logout` - 로그아웃

**Request:** No body (uses Authorization header)

**Response:** `ApiResponse<Void>`
```json
{
  "success": true,
  "message": "로그아웃되었습니다.",
  "timestamp": "2025-01-07T10:30:00"
}
```

### 5. GET `/v1/auth/me` - 현재 사용자 조회

**Request:** No body (uses Authorization header)

**Response:** 현재는 `String`, 향후 `ApiResponse<UserInfo>`로 변경 필요
```json
{
  "success": true,
  "data": {
    "userId": "usr_01234567",
    "username": "admin",
    "email": "admin@inspecthub.com",
    "displayName": "관리자",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["*"],
    "organizationId": "org_12345",
    "organizationName": "본사"
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

## 🏥 Health Check API

### GET `/health` - 시스템 헬스 체크

**Request:** No body

**Response:** `Map<String, Object>` (표준 ApiResponse 래퍼 없음)
```json
{
  "status": "UP",
  "application": "Inspect-Hub System",
  "timestamp": "2025-01-07T10:30:00",
  "version": "0.0.1-SNAPSHOT"
}
```

## 🔄 프론트엔드 작업 필요 항목

### 1. API Mock 업데이트 (`frontend/tests/mocks/handlers.ts`)

**현재 문제:**
- ✅ API Base URL: `http://localhost:8090/api/v1` (올바름)
- ❌ 응답 구조: `user` 객체 사용 중 → flat 구조로 변경 필요
- ❌ 필드명: `user.id`, `user.name` → `userId`, `displayName`으로 변경
- ❌ 누락 필드: `expiresIn`, `tokenType`, `loginMethod`, `organizationId`, `organizationName` 추가

**수정 전:**
```typescript
{
  success: true,
  data: {
    accessToken: '...',
    refreshToken: '...',
    user: {
      id: 'user-001',
      username: 'admin',
      name: '관리자',
      email: 'admin@inspecthub.com',
      roles: ['ROLE_ADMIN'],
      permissions: ['*'],
    }
  }
}
```

**수정 후:**
```typescript
{
  success: true,
  data: {
    userId: 'usr_01234567',
    username: 'admin',
    email: 'admin@inspecthub.com',
    displayName: '관리자',
    accessToken: '...',
    refreshToken: '...',
    roles: ['ROLE_ADMIN', 'ROLE_USER'],
    permissions: ['READ_USER', 'WRITE_USER'],
    organizationId: 'org_12345',
    organizationName: '본사',
    expiresIn: 3600000,
    tokenType: 'Bearer',
    loginMethod: 'traditional'
  },
  timestamp: '2025-01-07T10:30:00'
}
```

### 2. TypeScript 타입 정의 추가 (`frontend/shared/types/`)

**새로 생성 필요:**

```typescript
// frontend/shared/types/api.ts
export interface ApiResponse<T> {
  success: boolean
  message?: string
  data?: T
  errorCode?: string
  timestamp: string
}

export interface ApiError {
  success: false
  message: string
  errorCode: string
  timestamp: string
}

// frontend/shared/types/auth.ts
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  userId: string
  username: string
  email: string
  displayName: string
  accessToken: string
  refreshToken: string
  roles: string[]
  permissions: string[]
  organizationId: string
  organizationName: string
  expiresIn: number
  tokenType: string
  loginMethod: 'traditional' | 'sso'
}

export interface SSOLoginRequest {
  ssoToken: string
  provider?: 'okta' | 'azure-ad' | 'custom-sso'
}

export interface TokenRefreshRequest {
  refreshToken: string
}

export interface TokenRefreshResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface UserInfo {
  userId: string
  username: string
  email: string
  displayName: string
  roles: string[]
  permissions: string[]
  organizationId: string
  organizationName: string
}
```

### 3. Auth Store 업데이트 (`frontend/features/auth/model/auth.store.ts`)

**업데이트 필요:**
- User 상태 필드명 변경: `id` → `userId`, `name` → `displayName`
- 새 필드 추가: `organizationId`, `organizationName`, `tokenType`, `expiresIn`

### 4. API Client 인터셉터 (`frontend/shared/api/`)

**구현 필요:**
- Response interceptor에서 `ApiResponse<T>` 래퍼 자동 처리
- Error interceptor에서 `errorCode` 기반 에러 핸들링
- Token refresh 로직 (401 에러 시 자동 refresh)

## 🔧 백엔드 작업 필요 항목

### 1. ✅ Swagger 어노테이션 추가 (완료)

모든 Controller와 DTO에 Swagger 어노테이션 적용 완료:
- AuthController: `@Tag`, `@Operation` 추가
- HealthController: `@Tag`, `@Operation` 추가
- 모든 Auth DTOs: `@Schema` 추가

### 2. GET `/v1/auth/me` 응답 구조 변경 (권장)

**현재:**
```java
public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok("Authenticated user ID: " + userId);
}
```

**권장 변경:**
```java
public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(@AuthenticationPrincipal String userId) {
    UserInfo userInfo = authService.getUserInfo(userId);
    return ResponseEntity.ok(ApiResponse.success(userInfo));
}
```

### 3. Global Exception Handler 구현

**구현 필요:**
- `@RestControllerAdvice` 클래스 생성
- 모든 예외를 `ApiResponse` 형식으로 변환
- `errorCode` 매핑 정의

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    // 기타 예외 핸들러...
}
```

### 4. CORS 설정 확인

**확인 필요:**
- 개발 환경: `http://localhost:3000` 허용
- 프로덕션: 실제 프론트엔드 도메인 허용

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

## 📊 Dashboard APIs (향후 구현)

### GET `/v1/dashboard/stats` - 대시보드 통계

**Status:** 🔴 미구현 (백엔드)

**Response:** `ApiResponse<DashboardStats>`
```json
{
  "success": true,
  "data": {
    "str": {
      "total": 1234,
      "pending": 45,
      "approved": 1150,
      "rejected": 39,
      "trend": 12.5
    },
    "ctr": {
      "total": 5678,
      "pending": 89,
      "approved": 5520,
      "rejected": 69,
      "trend": -3.2
    },
    "wlf": {
      "total": 234,
      "pending": 12,
      "approved": 210,
      "rejected": 12,
      "trend": 5.8
    },
    "cases": {
      "total": 890,
      "pending": 67,
      "investigating": 123,
      "completed": 700
    }
  },
  "timestamp": "2025-01-07T10:30:00"
}
```

### GET `/v1/cases/recent` - 최근 케이스 목록

**Status:** 🔴 미구현 (백엔드)

### GET `/v1/cases` - 케이스 목록 (페이지네이션)

**Status:** 🔴 미구현 (백엔드)

**Query Parameters:**
- `page`: 페이지 번호 (1부터 시작)
- `size`: 페이지 크기 (기본 20)
- `type`: 필터 (STR/CTR/WLF)
- `status`: 필터 (PENDING/INVESTIGATING/COMPLETED)

## 🎯 우선순위 작업 계획

### 프론트엔드 우선순위

1. **[HIGH]** API 타입 정의 생성 (`shared/types/api.ts`, `shared/types/auth.ts`)
2. **[HIGH]** Mock API 응답 구조 동기화 (`tests/mocks/handlers.ts`)
3. **[HIGH]** Auth Store 필드명 업데이트
4. **[MEDIUM]** API Client 인터셉터 구현
5. **[LOW]** Dashboard API 모킹 유지 (백엔드 구현 대기)

### 백엔드 우선순위

1. **[HIGH]** Global Exception Handler 구현
2. **[HIGH]** `/v1/auth/me` API 응답 구조 개선
3. **[MEDIUM]** CORS 설정 확인 및 업데이트
4. **[LOW]** Dashboard APIs 구현 (`/v1/dashboard/stats`, `/v1/cases/recent`, `/v1/cases`)

## 🔄 동기화 체크리스트

### 프론트엔드

- [ ] API 타입 정의 생성
- [ ] Mock API 응답 구조 수정
- [ ] Auth Store 필드명 변경
- [ ] API Client 인터셉터 구현
- [ ] E2E 테스트 업데이트

### 백엔드

- [x] Swagger 어노테이션 추가
- [ ] Global Exception Handler 구현
- [ ] `/v1/auth/me` 개선
- [ ] CORS 설정 확인
- [ ] Dashboard APIs 구현

## 📚 참고 문서

- Frontend: `/frontend/README.md`, `/frontend/AGENTS.md`
- Backend: `/backend/AGENTS.md`
- Project: `/CLAUDE.md`
- API Generator: `/backend/scripts/generate-api.py`
