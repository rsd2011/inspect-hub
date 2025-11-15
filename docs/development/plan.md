# Test Plan - Inspect-Hub (TDD + BDD)

> **Kent Beck's TDD + Behavior-Driven Development 테스트 계획서**
>
> **Last Updated**: 2025-01-15
> **Status**: Active Development

---

## 📖 사용 방법

1. **"go" 명령어 입력** → 다음 체크되지 않은 테스트를 찾아 구현
2. **Red → Green → Refactor** 사이클 준수 (TDD)
3. **Given-When-Then** 구조로 테스트 작성 (BDD)
4. **테스트 완료 시**: `[x]` 표시
5. **각 테스트는 독립적으로 실행 가능해야 함**

---

## 📚 테스트 구조 (Progressive Disclosure)

이 테스트 계획은 **점진적 공개(Progressive Disclosure)** 원칙에 따라 계층적으로 구성되어 있습니다.
필요한 섹션만 열어서 집중할 수 있도록 파일이 분리되어 있습니다.

### 🏗️ Architecture Layers (아키텍처 레이어)

> **Note**: 테스트는 **BDD 스타일**(Given-When-Then)로 작성하되,
> 코드 구조는 **DDD 아키텍처** 레이어를 따릅니다.

각 레이어별로 독립적인 파일로 분리되어 있습니다:

#### Layer 1: Domain Layer (도메인 레이어)
**📄 [layers/layer-1-domain.md](./layers/layer-1-domain.md)**

- **Aggregate: User** (사용자)
  - Value Objects: UserId, EmployeeId, Password
  - Aggregate Root: User 엔티티
  - Domain Service: UserDomainService

- **Aggregate: Policy** (정책 - 스냅샷 기반)
  - Value Objects: PolicyId, PolicyVersion, PolicyType
  - Aggregate Root: PolicySnapshot
  - Domain Service: PolicyDomainService

#### Layer 2: Application Layer (애플리케이션 레이어)
**📄 [layers/layer-2-application.md](./layers/layer-2-application.md)**

- **User Application Service**
  - Commands: CreateUserCommand, UpdateUserCommand, AssignRoleCommand
  - Application Service: UserApplicationService
  - Query Service: UserQueryService

- **Policy Application Service**
  - Commands: CreatePolicySnapshotCommand, ApprovePolicyCommand, DeployPolicyCommand
  - Application Service: PolicyApplicationService
  - Query Service: PolicyQueryService

#### Layer 3: Infrastructure Layer (인프라 레이어)
**📄 [layers/layer-3-infrastructure.md](./layers/layer-3-infrastructure.md)**

- **User Infrastructure**
  - Repository Implementation: UserRepositoryImpl

- **Policy Infrastructure**
  - Repository Implementation: PolicyRepositoryImpl

#### Layer 4: Interface Layer (인터페이스 레이어)
**📄 [layers/layer-4-interface.md](./layers/layer-4-interface.md)**

- **User API**
  - Controller Tests (MockMvc): POST, GET, PUT, DELETE

- **Policy API**
  - Controller Tests (MockMvc): POST, GET

---

### 🔐 Cross-Cutting Concerns (횡단 관심사)

시스템 전반에 걸친 공통 관심사들입니다:

#### 로그인 정책 관리
**📄 [cross-cutting/login-policy.md](./cross-cutting/login-policy.md)**

> **아키텍처**: Jenkins 스타일 시스템 설정 - **전역 정책만 존재** (조직별 정책 없음)

- Login Policy Domain Model (전역 정책)
- Login Policy Application Service (전역 정책 조회/수정)
- Login Policy Data Structure
- Repository & Queries (글로벌 정책 전용)
- Integration with Authentication
- API Endpoints (시스템 설정 API), Validation, Caching, Audit Logging
- System Configuration UI (Jenkins 스타일)

#### 인증 (Authentication)
**📄 [cross-cutting/authentication.md](./cross-cutting/authentication.md)**

- **Security Framework**
- **AD Login** (Active Directory)
- **SSO Login** (Single Sign-On)
- **Local Login** (Fallback)
- **System Configuration Architecture**
- **Account Security Policies**
- **Password Management Policies**
- **Session Management Policies**
- **External Authentication Configuration**
- **Password Reset Policies**
- **Health Check & Fallback**
- **Concurrent & Performance**
- **Integration**

#### JWT 토큰 관리
**📄 [cross-cutting/jwt.md](./cross-cutting/jwt.md)**

- Token Generation (생성)
- Token Validation (검증)
- Token Refresh (갱신)
- Token Revocation (무효화)
- Token Storage & Transport (저장 및 전송)
- Claims & Payload (클레임 및 페이로드)
- Performance & Caching (성능 및 캐싱)
- Error Handling (오류 처리)
- Testing Utilities (테스트 유틸리티)

#### 감사 로그 (Audit Logging)
**📄 [cross-cutting/audit-logging.md](./cross-cutting/audit-logging.md)**

- Login Success (로그인 성공)
- Login Failure (로그인 실패)
- Token Operations (토큰 작업)
- Security Events (보안 이벤트)
- Data Structure (데이터 구조)
- Repository & Queries (저장소 및 쿼리)
- Retention & Compliance (보관 및 규정 준수)

#### 기타 (Password Management, Exception Handling)
**📄 [cross-cutting/misc.md](./cross-cutting/misc.md)**

- Password Management
- Exception Handling

---

### 🛠️ Implementation Guides (구현 가이드)

구현에 필요한 상세 가이드입니다:

#### Backend Implementation Guide
**📄 [implementation/backend-guide.md](./implementation/backend-guide.md)**

- 환경 변수 설정 (Environment Variables)
- ConfigurationService 상세 설계
- Entity 설계
- Repository 설계
- REST API Controller
- Flyway 마이그레이션 스크립트

#### Frontend UI Design
**📄 [implementation/frontend-guide.md](./implementation/frontend-guide.md)**

- System Configuration UI
- 화면 구조
- 컴포넌트 구조 (FSD + Atomic Design)
- Pinia Store
- UI/UX 요구사항

#### Implementation Checklist
**📄 [implementation/checklist.md](./implementation/checklist.md)**

- Backend 체크리스트
- Frontend 체크리스트
- Documentation 체크리스트

#### Additional Considerations
**📄 [implementation/considerations.md](./implementation/considerations.md)**

- 설정 값 검증 및 타입 안전성
- 에러 처리 전략
- 성능 모니터링
- 운영 가이드라인
- 보안 강화 방안
- 테스트 전략

---

## 📊 테스트 커버리지 목표

| Layer | Minimum Coverage | Test Type |
|-------|-----------------|-----------|
| **Domain Logic** | 90% | Unit Tests |
| **Service Layer** | 80% | Unit Tests |
| **Repository** | 70% | Integration Tests |
| **Controller** | 80% | Integration Tests |

---

## 🚀 실행 방법

### 단일 테스트 실행
```bash
./gradlew test --tests "AuthServiceTest.shouldReturnJwtTokenWhenValidCredentials"
```

### 특정 클래스 테스트 실행
```bash
./gradlew test --tests "AuthServiceTest"
```

### 전체 테스트 실행 (장기 실행 제외)
```bash
./gradlew test
```

### 커버리지 리포트 생성
```bash
./gradlew test jacocoTestReport
open backend/auth/build/reports/jacoco/test/html/index.html
```

---

## 📝 Notes

### 테스트 방법론
- **TDD (Test-Driven Development)**: Red → Green → Refactor 사이클
- **BDD (Behavior-Driven Development)**: Given-When-Then 구조로 테스트 작성
- **@DisplayName**: 한글로 비즈니스 시나리오를 명확히 표현

### 도메인 설계
- **ULID 사용**: 모든 Entity ID는 ULID (Universally Unique Lexicographically Sortable Identifier) 사용
- **스냅샷 패턴**: Policy 등 버전 관리가 필요한 도메인은 스냅샷 패턴 적용
- **도메인 이벤트**: 중요한 상태 변경 시 도메인 이벤트 발행 (Spring Application Events 사용)
- **Maker-Checker**: 모든 승인 프로세스는 Maker-Checker 원칙 준수
- **감사 로그**: 모든 중요 작업은 감사 로그 기록

---

## 🔄 마지막 업데이트

- **2025-01-15**:
  - 🏗️ **아키텍처 변경**: 로그인 정책을 조직별(orgId) → 전역(Global) 정책으로 변경
    - Jenkins 스타일 시스템 설정 방식 채택
    - 단일 글로벌 정책만 존재 (조직별 분리 없음)
    - LoginPolicyService 구조 변경 예정 (기존 orgId 파라미터 제거)
    - login-policy.md 문서 전면 개정 완료
  - Progressive Disclosure 적용 - 파일 구조 재구성
  - 제목 정정: TDD + BDD (Behavior-Driven Development)
  - BDD 스타일 테스트 작성 가이드 추가
- **2025-01-13**: 초기 테스트 계획 작성
