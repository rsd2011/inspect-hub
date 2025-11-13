# API Design Guidelines

> **RESTful API 설계 원칙 및 가이드라인**

## 📚 목차

1. [설계 원칙](#-설계-원칙)
2. [URL 구조](#-url-구조)
3. [HTTP 메서드](#-http-메서드)
4. [요청/응답 포맷](#-요청응답-포맷)
5. [상태 코드](#-상태-코드)
6. [에러 처리](#-에러-처리)
7. [페이지네이션](#-페이지네이션)
8. [검색 및 필터링](#-검색-및-필터링)
9. [정렬](#-정렬)
10. [버전 관리](#-버전-관리)
11. [보안](#-보안)
12. [성능 최적화](#-성능-최적화)
13. [API 문서화](#-api-문서화)

---

## 🎯 설계 원칙

### 1. RESTful 원칙 준수

**Resource 중심 설계:**
- URL은 리소스를 나타냄 (동사가 아닌 명사 사용)
- HTTP 메서드로 동작 표현
- 계층 구조 표현

**Good Examples:**
```
GET    /api/v1/users           # 사용자 목록 조회
GET    /api/v1/users/{id}      # 특정 사용자 조회
POST   /api/v1/users           # 사용자 생성
PUT    /api/v1/users/{id}      # 사용자 전체 수정
PATCH  /api/v1/users/{id}      # 사용자 부분 수정
DELETE /api/v1/users/{id}      # 사용자 삭제
```

**Bad Examples:**
```
❌ GET  /api/v1/getUsers
❌ POST /api/v1/createUser
❌ GET  /api/v1/deleteUser?id=123
```

### 2. 일관성 (Consistency)

**모든 API에서 동일한 패턴 적용:**
- 명명 규칙
- 응답 구조
- 에러 포맷
- 페이지네이션 방식
- 날짜/시간 포맷

### 3. 명확성 (Clarity)

**API는 자명해야 함:**
- 직관적인 URL
- 명확한 응답 메시지
- 상세한 에러 설명

### 4. 유연성 (Flexibility)

**확장 가능한 설계:**
- 버전 관리
- Optional 파라미터
- 하위 호환성 유지

### 5. 보안 우선 (Security First)

**모든 API는 보안 고려:**
- 인증/인가 필수
- HTTPS 사용
- 입력 검증
- Rate Limiting

---

## 🔗 URL 구조

### 기본 패턴

```
https://{domain}/api/{version}/{resource}[/{id}][/{sub-resource}][/{action}]
```

**Components:**
- `{domain}`: `api.inspecthub.example.com` (프로덕션)
- `{version}`: API 버전 (예: `v1`, `v2`)
- `{resource}`: 리소스 이름 (복수형 명사)
- `{id}`: 리소스 식별자 (ULID 26자)
- `{sub-resource}`: 하위 리소스
- `{action}`: 특수 동작 (동사)

### Examples

```
# 리소스 CRUD
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/{id}
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}

# 하위 리소스
GET    /api/v1/users/{userId}/permissions
POST   /api/v1/users/{userId}/permissions
DELETE /api/v1/users/{userId}/permissions/{permissionId}

# 특수 동작 (Action)
POST   /api/v1/policies/{id}/activate      # 정책 활성화
POST   /api/v1/policies/{id}/rollback      # 정책 롤백
POST   /api/v1/cases/{id}/assign           # 사례 할당
PUT    /api/v1/cases/{id}/approve          # 사례 승인

# 검색/필터링 (Query Parameters)
GET    /api/v1/users?status=active&role=admin
GET    /api/v1/policies?type=STR&version=1

# 페이지네이션
GET    /api/v1/users?page=1&size=20&sort=createdAt,desc
```

### 명명 규칙

| 항목 | 규칙 | 예시 |
|------|------|------|
| **리소스** | 복수형 명사, kebab-case | `users`, `policy-snapshots` |
| **리소스 ID** | ULID (26자) | `01ARZ3NDEKTSV4RRFFQ69G5FAV` |
| **Action** | 동사, kebab-case | `activate`, `send-email` |
| **Query Parameter** | camelCase | `userId`, `createdAfter` |

**❌ 금지:**
- 동사로 시작하는 URL (`/getUser`, `/createPolicy`)
- 밑줄 사용 (`/user_profiles` → `/user-profiles`)
- 단수형 리소스 (`/user` → `/users`)
- URL에 파일 확장자 (`.json`, `.xml`)

---

## 🔄 HTTP 메서드

### 표준 CRUD

| 메서드 | 용도 | 멱등성 | Request Body | Response Body |
|--------|------|--------|--------------|---------------|
| **GET** | 조회 | O | ❌ | ✅ (리소스 데이터) |
| **POST** | 생성 | ❌ | ✅ (생성 데이터) | ✅ (생성된 리소스) |
| **PUT** | 전체 수정 | O | ✅ (전체 데이터) | ✅ (수정된 리소스) |
| **PATCH** | 부분 수정 | O | ✅ (수정할 필드만) | ✅ (수정된 리소스) |
| **DELETE** | 삭제 | O | ❌ | ✅ (성공 메시지) |

### GET - 조회

```http
# 목록 조회
GET /api/v1/users?status=active&page=1&size=20
```

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
        "username": "admin",
        "email": "admin@example.com",
        "status": "active"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3
    }
  }
}
```

```http
# 단건 조회
GET /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
```

```json
{
  "success": true,
  "data": {
    "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "username": "admin",
    "email": "admin@example.com",
    "status": "active",
    "createdAt": "2025-01-13T10:00:00Z"
  }
}
```

### POST - 생성

```http
POST /api/v1/users
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePassword123!"
}
```

```json
// 201 Created
// Location: /api/v1/users/01H2X3Y4Z5A6B7C8D9E0F1G2H3
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "01H2X3Y4Z5A6B7C8D9E0F1G2H3",
    "username": "newuser",
    "email": "newuser@example.com",
    "status": "active",
    "createdAt": "2025-01-13T10:05:00Z"
  }
}
```

### PUT - 전체 수정

**전체 필드를 제공해야 함 (누락 시 null/default로 처리)**

```http
PUT /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
Content-Type: application/json

{
  "username": "admin-updated",
  "email": "admin-new@example.com",
  "status": "active"
}
```

### PATCH - 부분 수정

**수정할 필드만 제공 (JSON Merge Patch)**

```http
PATCH /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
Content-Type: application/json

{
  "email": "admin-new@example.com"
}
```

```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "username": "admin",  // 변경되지 않음
    "email": "admin-new@example.com",  // 변경됨
    "status": "active"
  }
}
```

### DELETE - 삭제

```http
DELETE /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV
```

```json
// 200 OK (soft delete) 또는 204 No Content (hard delete)
{
  "success": true,
  "message": "User deleted successfully"
}
```

**Note:** Inspect-Hub는 기본적으로 **Soft Delete** 사용

---

## 📋 요청/응답 포맷

### Content-Type

**요청/응답 모두 JSON 사용:**
```
Content-Type: application/json; charset=utf-8
```

### 통일된 응답 구조 (ApiResponse Wrapper)

```typescript
interface ApiResponse<T> {
  success: boolean;        // 성공 여부
  message?: string;        // 메시지 (성공/실패)
  data?: T;                // 응답 데이터
  errorCode?: string;      // 에러 코드 (실패 시)
  timestamp?: string;      // 타임스탬프
}
```

#### 성공 응답

```json
{
  "success": true,
  "data": {
    "id": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
    "username": "admin",
    "email": "admin@example.com"
  },
  "timestamp": "2025-01-13T10:00:00Z"
}
```

#### 목록 응답 (페이지네이션 포함)

```json
{
  "success": true,
  "data": {
    "items": [
      { "id": "01ARZ3...", "username": "user1" },
      { "id": "01BRZ3...", "username": "user2" }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "timestamp": "2025-01-13T10:00:00Z"
}
```

#### 실패 응답

```json
{
  "success": false,
  "message": "User not found with id: 01ARZ3NDEKTSV4RRFFQ69G5FAV",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2025-01-13T10:00:00Z"
}
```

### 날짜/시간 포맷

**ISO 8601 format (UTC):**
```
2025-01-13T10:00:00Z
```

**Request/Response 예시:**
```json
{
  "createdAt": "2025-01-13T10:00:00Z",
  "updatedAt": "2025-01-13T15:30:00Z",
  "effectiveFrom": "2025-01-01T00:00:00Z",
  "effectiveTo": "2025-12-31T23:59:59Z"
}
```

### Boolean 값

**JSON boolean 사용:**
```json
{
  "isActive": true,
  "deleted": false,
  "useYn": "Y"  // ❌ Bad - String 사용하지 않음
}
```

### Null 처리

**필드가 없을 때:**
- 빈 배열: `[]`
- 빈 객체: `{}` (지양)
- Null: `null` (허용)

```json
{
  "user": {
    "id": "01ARZ3...",
    "permissions": [],      // ✅ 빈 배열
    "metadata": null        // ✅ Null
  }
}
```

---

## 📊 상태 코드

### 표준 HTTP 상태 코드

| 코드 | 의미 | 사용 시나리오 |
|------|------|---------------|
| **200 OK** | 성공 | GET, PUT, PATCH, DELETE 성공 |
| **201 Created** | 생성 성공 | POST로 리소스 생성 성공 |
| **204 No Content** | 성공 (응답 없음) | DELETE 성공 (응답 body 없음) |
| **400 Bad Request** | 잘못된 요청 | 입력 검증 실패 |
| **401 Unauthorized** | 인증 실패 | 로그인 필요, 토큰 만료 |
| **403 Forbidden** | 권한 없음 | 인증은 되었으나 권한 부족 |
| **404 Not Found** | 리소스 없음 | 존재하지 않는 리소스 |
| **409 Conflict** | 충돌 | 중복 생성, 상태 충돌 |
| **422 Unprocessable Entity** | 처리 불가 | 비즈니스 규칙 위반 |
| **429 Too Many Requests** | 요청 과다 | Rate Limit 초과 |
| **500 Internal Server Error** | 서버 오류 | 예상치 못한 서버 에러 |

### 상태 코드 선택 가이드

#### 2xx - 성공

```
200 OK
- GET 요청 성공
- PUT/PATCH 수정 성공
- DELETE 성공 (응답 body 있음)

201 Created
- POST 생성 성공
- Response에 Location 헤더 포함 권장
  Location: /api/v1/users/01ARZ3NDEKTSV4RRFFQ69G5FAV

204 No Content
- DELETE 성공 (응답 body 없음)
- PUT/PATCH 성공 (응답 body 없음)
```

#### 4xx - 클라이언트 오류

```
400 Bad Request
- 필수 파라미터 누락
- 잘못된 JSON 형식
- 유효성 검증 실패

Example:
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "email",
      "message": "Email format is invalid"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}

401 Unauthorized
- 로그인 필요
- JWT 토큰 만료/없음/invalid

Example:
{
  "success": false,
  "message": "Authentication required",
  "errorCode": "UNAUTHORIZED"
}

403 Forbidden
- 인증은 되었으나 권한 부족
- Role 확인 실패

Example:
{
  "success": false,
  "message": "Insufficient permissions",
  "errorCode": "FORBIDDEN"
}

404 Not Found
- 리소스 존재하지 않음

Example:
{
  "success": false,
  "message": "User not found with id: 01ARZ3...",
  "errorCode": "USER_NOT_FOUND"
}

409 Conflict
- 중복 생성 (이미 존재하는 리소스)
- 상태 충돌 (이미 승인된 정책을 다시 승인)

Example:
{
  "success": false,
  "message": "User already exists with email: admin@example.com",
  "errorCode": "USER_ALREADY_EXISTS"
}

422 Unprocessable Entity
- 비즈니스 규칙 위반

Example:
{
  "success": false,
  "message": "Cannot activate policy: effective date is in the past",
  "errorCode": "POLICY_ACTIVATION_FAILED"
}
```

#### 5xx - 서버 오류

```
500 Internal Server Error
- 예상치 못한 서버 에러
- 데이터베이스 연결 실패
- 외부 API 호출 실패

Example:
{
  "success": false,
  "message": "An unexpected error occurred. Please contact support.",
  "errorCode": "INTERNAL_SERVER_ERROR"
}
```

---

## ⚠️ 에러 처리

### 에러 응답 구조

```typescript
interface ErrorResponse {
  success: false;
  message: string;           // 사용자 친화적 에러 메시지
  errorCode: string;         // 에러 코드 (상수)
  errors?: FieldError[];     // 필드 검증 에러 (선택)
  timestamp: string;         // 발생 시각
  path?: string;             // 요청 경로
}

interface FieldError {
  field: string;             // 필드 이름
  message: string;           // 에러 메시지
  rejectedValue?: any;       // 거부된 값
}
```

### 에러 코드 체계

```
{DOMAIN}_{ERROR_TYPE}
```

**Examples:**
```
USER_NOT_FOUND
USER_ALREADY_EXISTS
USER_VALIDATION_FAILED

POLICY_NOT_FOUND
POLICY_ACTIVATION_FAILED
POLICY_ALREADY_ACTIVE

CASE_NOT_FOUND
CASE_APPROVAL_DENIED
CASE_STATUS_INVALID

AUTHENTICATION_FAILED
UNAUTHORIZED
FORBIDDEN
PERMISSION_DENIED

INTERNAL_SERVER_ERROR
DATABASE_ERROR
EXTERNAL_API_ERROR
```

### 검증 에러 (400 Bad Request)

```json
{
  "success": false,
  "message": "Validation failed for user registration",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "email",
      "message": "Email format is invalid",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters",
      "rejectedValue": "abc"
    },
    {
      "field": "username",
      "message": "Username is required",
      "rejectedValue": null
    }
  ],
  "timestamp": "2025-01-13T10:00:00Z",
  "path": "/api/v1/users"
}
```

### 비즈니스 규칙 위반 (422 Unprocessable Entity)

```json
{
  "success": false,
  "message": "Cannot activate policy: effective date is in the past",
  "errorCode": "POLICY_ACTIVATION_FAILED",
  "timestamp": "2025-01-13T10:00:00Z",
  "path": "/api/v1/policies/01ARZ3.../activate"
}
```

### 인증/인가 에러

```json
// 401 Unauthorized
{
  "success": false,
  "message": "JWT token is invalid or expired",
  "errorCode": "UNAUTHORIZED",
  "timestamp": "2025-01-13T10:00:00Z"
}

// 403 Forbidden
{
  "success": false,
  "message": "You do not have permission to access this resource",
  "errorCode": "FORBIDDEN",
  "timestamp": "2025-01-13T10:00:00Z"
}
```

### 서버 에러 (500)

```json
{
  "success": false,
  "message": "An unexpected error occurred. Please contact support.",
  "errorCode": "INTERNAL_SERVER_ERROR",
  "timestamp": "2025-01-13T10:00:00Z",
  "path": "/api/v1/users"
}
```

**Note:** 프로덕션 환경에서는 상세한 스택 트레이스를 응답에 포함하지 않음

---

## 📄 페이지네이션

### Offset-based Pagination (기본 방식)

**Query Parameters:**
```
?page=1&size=20
```

**Request:**
```http
GET /api/v1/users?page=1&size=20&sort=createdAt,desc
```

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      { "id": "01ARZ3...", "username": "user1" },
      { "id": "01BRZ3...", "username": "user2" }
    ],
    "pagination": {
      "page": 1,                 // 현재 페이지 (1부터 시작)
      "size": 20,                // 페이지 크기
      "totalElements": 45,       // 전체 아이템 수
      "totalPages": 3,           // 전체 페이지 수
      "hasNext": true,           // 다음 페이지 존재 여부
      "hasPrevious": false       // 이전 페이지 존재 여부
    }
  }
}
```

### Cursor-based Pagination (대용량 데이터)

**대용량 실시간 데이터에 적합:**
- 탐지 이벤트 목록
- 트랜잭션 로그
- 감사 로그

**Query Parameters:**
```
?cursor=01ARZ3NDEKTSV4RRFFQ69G5FAV&size=20
```

**Request:**
```http
GET /api/v1/detection/events?cursor=01ARZ3...&size=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      { "id": "01ARZ3...", "eventType": "STR", ... },
      { "id": "01BRZ3...", "eventType": "CTR", ... }
    ],
    "cursor": {
      "next": "01CRZ3NDEKTSV4RRFFQ69G5FAV",  // 다음 페이지 커서
      "hasMore": true                         // 더 있는지 여부
    }
  }
}
```

### 기본값

```
page: 1 (첫 페이지)
size: 20 (기본 크기)
maxSize: 100 (최대 크기)
```

---

## 🔍 검색 및 필터링

### Query Parameters 사용

```http
# 단일 필터
GET /api/v1/users?status=active

# 다중 필터
GET /api/v1/users?status=active&role=admin&department=compliance

# 날짜 범위
GET /api/v1/cases?createdAfter=2025-01-01T00:00:00Z&createdBefore=2025-01-31T23:59:59Z

# 검색 (Full-text search)
GET /api/v1/users?search=admin

# 조합
GET /api/v1/policies?type=STR&status=active&version=1&page=1&size=20
```

### 연산자 사용

**필터 연산자:**
```
?amount[gte]=10000         # Greater Than or Equal (>=)
?amount[lte]=50000         # Less Than or Equal (<=)
?amount[gt]=10000          # Greater Than (>)
?amount[lt]=50000          # Less Than (<)
?createdAt[between]=2025-01-01,2025-01-31
?username[like]=admin      # LIKE 검색
?username[in]=admin,user1,user2
```

**Examples:**
```http
# 금액 범위
GET /api/v1/transactions?amount[gte]=10000&amount[lte]=50000

# 날짜 범위
GET /api/v1/cases?createdAt[between]=2025-01-01,2025-01-31

# IN 조건
GET /api/v1/users?status[in]=active,pending
```

### 복잡한 검색 (POST 방식)

**매우 복잡한 조건일 경우 POST 사용:**

```http
POST /api/v1/users/_search
Content-Type: application/json

{
  "filters": [
    { "field": "status", "operator": "eq", "value": "active" },
    { "field": "department", "operator": "in", "value": ["compliance", "audit"] },
    { "field": "createdAt", "operator": "gte", "value": "2025-01-01T00:00:00Z" }
  ],
  "search": "admin",
  "page": 1,
  "size": 20,
  "sort": [
    { "field": "createdAt", "direction": "desc" }
  ]
}
```

---

## 🔀 정렬

### Query Parameter

```
?sort={field},{direction}
```

**Examples:**
```http
# 단일 정렬
GET /api/v1/users?sort=createdAt,desc

# 다중 정렬
GET /api/v1/users?sort=department,asc&sort=createdAt,desc

# 페이지네이션과 함께
GET /api/v1/users?page=1&size=20&sort=username,asc
```

### 정렬 방향

```
asc   - Ascending (오름차순)
desc  - Descending (내림차순)
```

### 기본 정렬

**명시하지 않을 경우:**
- 일반적으로 `createdAt desc` (최신순)
- 리소스별로 적절한 기본 정렬 설정

---

## 🔢 버전 관리

### URL 버전 관리 (권장)

```
/api/v1/users
/api/v2/users
```

**장점:**
- 명확함
- 캐싱 용이
- 라우팅 단순

### 버전 전략

**Breaking Changes:**
- 새로운 버전 생성 (v2, v3)
- 기존 버전 유지 (하위 호환성)

**Non-breaking Changes:**
- 기존 버전에 추가 (v1에 새 필드 추가)
- Optional 파라미터 추가

### 버전 지원 기간

```
v1: 2025-01-01 ~ 2026-12-31 (2년)
v2: 2025-07-01 ~ 2027-06-30 (2년)
```

**Deprecation 공지:**
- 최소 6개월 전 공지
- Response Header에 경고 추가:
  ```
  Warning: 299 - "API version v1 will be deprecated on 2026-12-31"
  ```

---

## 🔒 보안

### 인증 (Authentication)

**JWT Bearer Token 사용:**

```http
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**토큰 구조:**
```json
{
  "sub": "01ARZ3NDEKTSV4RRFFQ69G5FAV",  // User ID
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_COMPLIANCE_OFFICER"],
  "permissions": ["user:read", "user:write", "policy:approve"],
  "iat": 1673600000,
  "exp": 1673603600
}
```

### 인가 (Authorization)

**Role-Based Access Control (RBAC):**

```http
# Admin만 접근 가능
GET /api/v1/admin/users
Authorization: Bearer {token with ROLE_ADMIN}

# Permission 체크
POST /api/v1/policies/{id}/approve
Authorization: Bearer {token with policy:approve permission}
```

### CORS 설정

```yaml
# application.yml
cors:
  allowed-origins:
    - http://localhost:3000       # 개발 환경
    - https://app.inspecthub.com  # 프로덕션
  allowed-methods:
    - GET
    - POST
    - PUT
    - PATCH
    - DELETE
  allowed-headers:
    - Authorization
    - Content-Type
  exposed-headers:
    - Location
  max-age: 3600
```

### Rate Limiting

**요청 제한:**
```
100 requests per minute per user
1000 requests per hour per user
```

**초과 시 응답:**
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1673603600

{
  "success": false,
  "message": "Rate limit exceeded. Please try again later.",
  "errorCode": "RATE_LIMIT_EXCEEDED"
}
```

### Input Validation

**모든 입력 검증:**
- Request Body 검증 (Jakarta Validation)
- Query Parameter 검증
- Path Variable 검증 (ULID 형식)

```java
@PostMapping("/users")
public ResponseEntity<ApiResponse<UserResponse>> createUser(
    @Valid @RequestBody CreateUserRequest request  // ✅ @Valid
) {
    // ...
}
```

### SQL Injection 방지

**MyBatis Parameterized Queries:**
```xml
<!-- ✅ Good - Parameterized -->
<select id="findByEmail" resultType="User">
    SELECT * FROM "user" WHERE email = #{email}
</select>

<!-- ❌ Bad - String concatenation -->
<select id="findByEmail" resultType="User">
    SELECT * FROM "user" WHERE email = '${email}'
</select>
```

---

## ⚡ 성능 최적화

### 필드 선택 (Sparse Fieldsets)

**클라이언트가 필요한 필드만 요청:**

```http
GET /api/v1/users?fields=id,username,email
```

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "01ARZ3...",
        "username": "admin",
        "email": "admin@example.com"
        // createdAt, updatedAt 등 생략됨
      }
    ]
  }
}
```

### 관계 확장 (Expand/Include)

**관련 리소스 포함:**

```http
GET /api/v1/users/{id}?expand=organization,permissions
```

```json
{
  "success": true,
  "data": {
    "id": "01ARZ3...",
    "username": "admin",
    "organization": {               // ✅ 확장됨
      "id": "01ORG...",
      "name": "ABC Bank"
    },
    "permissions": [                // ✅ 확장됨
      { "id": "01PERM...", "code": "user:read" }
    ]
  }
}
```

### 캐싱

**Cache-Control 헤더:**

```http
# 정적 데이터 (코드, 상수)
Cache-Control: public, max-age=86400  # 1일

# 동적 데이터 (사용자 정보)
Cache-Control: private, max-age=300   # 5분

# 캐싱 금지 (민감한 데이터)
Cache-Control: no-store
```

**ETag 사용:**

```http
# 첫 요청
GET /api/v1/users/{id}
→ 200 OK
ETag: "686897696a7c876b7e"

# 이후 요청
GET /api/v1/users/{id}
If-None-Match: "686897696a7c876b7e"
→ 304 Not Modified (변경 없음)
```

### Batch 요청

**다중 리소스 조회:**

```http
POST /api/v1/users/_batch
Content-Type: application/json

{
  "ids": [
    "01ARZ3...",
    "01BRZ3...",
    "01CRZ3..."
  ]
}
```

```json
{
  "success": true,
  "data": [
    { "id": "01ARZ3...", "username": "user1" },
    { "id": "01BRZ3...", "username": "user2" },
    { "id": "01CRZ3...", "username": "user3" }
  ]
}
```

### 비동기 처리

**장시간 소요 작업:**

```http
# 비동기 작업 시작
POST /api/v1/detection/inspect
→ 202 Accepted
Location: /api/v1/jobs/01JOB123...

{
  "success": true,
  "message": "Inspection job started",
  "data": {
    "jobId": "01JOB123...",
    "status": "PROCESSING"
  }
}

# 진행 상태 확인
GET /api/v1/jobs/01JOB123...
→ 200 OK

{
  "success": true,
  "data": {
    "jobId": "01JOB123...",
    "status": "COMPLETED",
    "progress": 100,
    "result": {
      "totalProcessed": 10000,
      "eventsGenerated": 45
    }
  }
}
```

---

## 📖 API 문서화

### Swagger/OpenAPI 사용

**자동 문서화:**
- Springdoc OpenAPI 사용
- 모든 API 자동 문서화
- Swagger UI 제공

**접속:**
```
http://localhost:8090/swagger-ui.html
```

### Annotation 예시

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "User Management API")
public class UserController {
    
    @PostMapping
    @Operation(
        summary = "Create User",
        description = "Create a new user account",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User creation request",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateUserRequest.class))
        )
        CreateUserRequest request
    ) {
        // ...
    }
}
```

### DTO Schema 문서화

```java
@Schema(description = "Create User Request")
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Schema(
        description = "Username (3-50 characters)",
        example = "admin",
        required = true
    )
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email
    @Schema(
        description = "Email address",
        example = "admin@example.com",
        required = true
    )
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$")
    @Schema(
        description = "Password (min 8 chars, must contain letters and numbers)",
        example = "SecurePassword123!",
        required = true,
        format = "password"
    )
    private String password;
}
```

---

## 📞 참고 문서

- [API Contract](./CONTRACT.md) - Frontend ↔ Backend API 계약
- [API Endpoints](./ENDPOINTS.md) - 전체 API 엔드포인트 목록
- [Backend README](../backend/README.md) - 백엔드 개발 가이드
- [Security](../architecture/SECURITY.md) - 보안 구현 세부사항

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | API Design Guidelines 초안 작성 | PM |

---

**Best Practices Summary:**

1. ✅ **RESTful 원칙 준수** - 리소스 중심, HTTP 메서드 활용
2. ✅ **일관된 응답 구조** - ApiResponse wrapper 사용
3. ✅ **명확한 에러 처리** - 의미 있는 에러 코드와 메시지
4. ✅ **페이지네이션 필수** - 대용량 데이터 대비
5. ✅ **보안 우선** - 인증/인가, 입력 검증, Rate Limiting
6. ✅ **버전 관리** - Breaking Changes 대비
7. ✅ **자동 문서화** - Swagger/OpenAPI 활용
8. ✅ **성능 최적화** - 캐싱, 필드 선택, 비동기 처리
