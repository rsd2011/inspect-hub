# API Documentation

> **Inspect-Hub API 설계 및 명세 문서 센터**

이 디렉토리는 Frontend ↔ Backend 간 API 계약 및 RESTful API 설계 문서를 포함합니다.

---

## 📚 문서 구조

### 1. [📋 API 계약 (CONTRACT.md)](./CONTRACT.md)

Frontend와 Backend 간 API 계약 및 동기화 계획

**주요 내용:**
- API 계약 원칙
- Frontend-Backend 협업 워크플로우
- OpenAPI/Swagger 스키마 관리
- 버전 관리 전략
- 변경 관리 프로세스
- Mock Server 활용 (MSW)

**읽어야 할 사람:**
- ⭐ 프론트엔드 개발자
- ⭐ 백엔드 개발자
- ⭐ 프로젝트 매니저

**핵심 포인트:**
- Frontend와 Backend가 독립적으로 개발 가능
- OpenAPI 스펙을 단일 진실 공급원(Single Source of Truth)으로 사용
- 변경 시 상호 통보 및 협의

---

### 2. [🎨 API 설계 원칙 (DESIGN.md)](./DESIGN.md)

RESTful API 설계 가이드라인

**주요 내용:**
- **REST 원칙** - 리소스 중심 설계
- **URL 구조 규칙**
  - `/api/v1/` 버전 접두사
  - 명사 복수형 (예: `/users`, `/cases`)
  - 계층 구조 (예: `/cases/{id}/attachments`)
- **HTTP 메서드 사용법**
  - GET (조회), POST (생성), PUT (전체 수정), PATCH (부분 수정), DELETE (삭제)
- **응답 코드 규칙**
  - 2xx (성공), 4xx (클라이언트 오류), 5xx (서버 오류)
- **에러 처리**
  - 일관된 에러 응답 포맷
  - 에러 코드 체계
- **페이지네이션, 정렬, 필터링**
- **HATEOAS (선택적)**

**읽어야 할 사람:**
- ⭐ API 설계자
- ⭐ 백엔드 개발자

**관련 문서:**
- [Backend README](../backend/README.md) - 백엔드 구현 가이드

---

### 3. [🔐 인증 API (AUTHENTICATION.md)](./AUTHENTICATION.md)

인증 관련 API 설계 (AD, SSO, 일반 로그인)

**주요 내용:**
- **3가지 로그인 방식**
  1. **AD 로그인** - LDAP/Kerberos
  2. **SSO 로그인** - Backend API 직접 호출
  3. **일반 로그인** - 자체 DB 인증 (BCrypt)
- **인증 플로우**
  - 로그인 시퀀스 다이어그램
  - JWT 토큰 발급 및 갱신
  - 로그아웃
- **API 엔드포인트**
  - `POST /api/v1/auth/login` - 일반 로그인
  - `POST /api/v1/auth/ad-login` - AD 로그인
  - `POST /api/v1/auth/sso-login` - SSO 로그인
  - `POST /api/v1/auth/refresh` - 토큰 갱신
  - `POST /api/v1/auth/logout` - 로그아웃
- **보안 고려사항**

**읽어야 할 사람:**
- ⭐ 백엔드 개발자
- ⭐ 프론트엔드 개발자
- ⭐ 보안 담당자

**관련 문서:**
- [Architecture - Security](../architecture/SECURITY.md)
- [Development - Authentication](../development/cross-cutting/authentication.md)
- [Development - JWT](../development/cross-cutting/jwt.md)

---

### 4. [📡 API 엔드포인트 명세 (ENDPOINTS.md)](./ENDPOINTS.md)

전체 API 엔드포인트 상세 명세

**주요 내용:**
- **인증/인가 API**
- **정책/기준 관리 API**
  - 정책 CRUD
  - 버전 관리
  - 배포/롤백
- **탐지/사건 관리 API**
  - STR/CTR/WLF 탐지
  - 사건 조회/수정
  - 승인 워크플로우
- **리포팅 API**
  - 리포트 생성
  - FIU 제출
- **운영 관리 API**
  - 사용자 관리
  - 시스템 설정
  - 감사 로그

**각 엔드포인트 포함 정보:**
- Method + Path
- Request Parameters/Body
- Response Body
- Status Codes
- 예시 (Example)

**읽어야 할 사람:**
- ⭐ 프론트엔드 개발자 (API 사용)
- ⭐ 백엔드 개발자 (API 구현)
- ⭐ QA 엔지니어 (테스트)

**관련 문서:**
- [Frontend README](../frontend/README.md) - API 호출 방법
- [Backend TOOLS](../backend/TOOLS.md) - API Generator 도구

---

## 🎯 빠른 탐색

### 역할별 필수 문서

**프론트엔드 개발자:**
1. [API 계약 (CONTRACT.md)](./CONTRACT.md) - 협업 프로세스 이해
2. [API 엔드포인트 (ENDPOINTS.md)](./ENDPOINTS.md) - API 사용법

**백엔드 개발자:**
1. [API 설계 원칙 (DESIGN.md)](./DESIGN.md) - 설계 가이드
2. [인증 API (AUTHENTICATION.md)](./AUTHENTICATION.md) - 인증 구현
3. [API 엔드포인트 (ENDPOINTS.md)](./ENDPOINTS.md) - 엔드포인트 명세

**QA 엔지니어:**
1. [API 엔드포인트 (ENDPOINTS.md)](./ENDPOINTS.md) - 테스트 명세

**프로젝트 매니저:**
1. [API 계약 (CONTRACT.md)](./CONTRACT.md) - 협업 프로세스

### 주제별 문서

**API 사용 (Frontend):**
1. [API 계약 (CONTRACT.md)](./CONTRACT.md)
2. [API 엔드포인트 (ENDPOINTS.md)](./ENDPOINTS.md)
3. [Frontend README](../frontend/README.md) - API 클라이언트 사용법

**API 설계 및 구현 (Backend):**
1. [API 설계 원칙 (DESIGN.md)](./DESIGN.md)
2. [인증 API (AUTHENTICATION.md)](./AUTHENTICATION.md)
3. [API 엔드포인트 (ENDPOINTS.md)](./ENDPOINTS.md)
4. [Backend README](../backend/README.md) - 백엔드 구현

**보안:**
1. [인증 API (AUTHENTICATION.md)](./AUTHENTICATION.md)
2. [Architecture - Security](../architecture/SECURITY.md)
3. [Development - Authentication](../development/cross-cutting/authentication.md)

---

## 🔗 관련 문서

### 상위 문서
- [Documentation Center](../index.md) - 중앙 문서 센터

### 관련 영역
- [Frontend Documentation](../frontend/README.md) - API 클라이언트 사용
- [Backend Documentation](../backend/README.md) - API 서버 구현
- [Architecture](../architecture/index.md) - 시스템 아키텍처
- [Development Guide](../development/index.md) - 개발 워크플로우

---

## 🛠️ API 개발 워크플로우

### 1. API 설계 단계
1. **요구사항 확인** (PRD 참조)
2. **API 설계** (DESIGN.md 원칙 준수)
3. **OpenAPI 스펙 작성** (Swagger Editor 사용)
4. **Frontend-Backend 리뷰**

### 2. 개발 단계
1. **Backend**: API 구현 (Spring Boot)
2. **Frontend**: Mock Server 사용 (MSW) + API 클라이언트 작성
3. **독립 개발** (계약 기반)

### 3. 통합 단계
1. **Backend 배포** (개발 환경)
2. **Frontend Mock → Real API 전환**
3. **통합 테스트**

### 4. 문서화
1. **Swagger UI 자동 생성** (springdoc-openapi)
2. **ENDPOINTS.md 수동 업데이트** (상세 예시 추가)

---

## 📝 API 문서 작성 규칙

### 엔드포인트 문서 포맷

```markdown
### `POST /api/v1/auth/login`

**설명:** 일반 로그인 (이메일 + 비밀번호)

**Request Body:**
```json
{
  "employeeId": "E12345",
  "password": "secretPassword123"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "홍길동",
    "email": "hong@example.com",
    "role": "OFFICER"
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "AUTHENTICATION_FAILED",
  "message": "Invalid credentials",
  "timestamp": "2025-01-15T10:30:00Z"
}
```
```

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-15 | API index.md 생성 (Progressive Disclosure 적용) | PM |

---

## 📞 문의

API 관련 문의:
- **GitHub Issues**: [프로젝트 이슈](https://github.com/inspect-hub/issues)
- **Slack**: #inspect-hub-api
