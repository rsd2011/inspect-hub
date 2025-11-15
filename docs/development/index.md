# Development Guide - Inspect-Hub

> **TDD + BDD 기반 개발 가이드 및 테스트 계획**
>
> **Last Updated**: 2025-01-15
> **Status**: Active Development

---

## 📖 개요

Inspect-Hub 프로젝트의 **개발 방법론**, **테스트 계획**, **구현 가이드**가 포함된 문서 모음입니다.

**핵심 방법론:**
- **TDD (Test-Driven Development)**: Red → Green → Refactor 사이클
- **BDD (Behavior-Driven Development)**: Given-When-Then 테스트 구조
- **DDD (Domain-Driven Design)**: 도메인 중심 아키텍처

---

## 📚 문서 구조

### 🎯 핵심 문서

#### [Test Plan (TDD + BDD)](./plan.md)
**테스트 계획서 - Progressive Disclosure 적용**

**개요:**
- "go" 명령어 워크플로우
- TDD + BDD 테스트 작성 방법
- 테스트 커버리지 목표
- 실행 방법 및 커버리지 리포트

**하위 문서:**
- [Layer 1: Domain Layer](./layers/layer-1-domain.md) - Value Objects, Aggregate Roots, Domain Services
- [Layer 2: Application Layer](./layers/layer-2-application.md) - Commands, Application Services, Query Services
- [Layer 3: Infrastructure Layer](./layers/layer-3-infrastructure.md) - Repository Implementations
- [Layer 4: Interface Layer](./layers/layer-4-interface.md) - Controller Tests (MockMvc)
- [Cross-Cutting: Login Policy](./cross-cutting/login-policy.md)
- [Cross-Cutting: Authentication](./cross-cutting/authentication.md) - AD, SSO, Local Login
- [Cross-Cutting: JWT](./cross-cutting/jwt.md) - Token Management
- [Cross-Cutting: Audit Logging](./cross-cutting/audit-logging.md)
- [Cross-Cutting: Misc](./cross-cutting/misc.md) - Password, Exception Handling

#### [TDD + DDD Workflow](./TDD_DDD_WORKFLOW.md)
**상세 워크플로우 가이드 (1298줄)**

**주요 내용:**
- TDD 사이클 상세 설명
- Tidy First 원칙
- DDD 레이어별 TDD 적용 전략
- Commit 규칙
- 리팩토링 가이드라인
- 실전 워크플로우 예제

#### [Development Workflow](./WORKFLOW.md)
**일반 개발 가이드**

**주요 내용:**
- 프로젝트 구조 & 모듈 구성
- 빌드, 테스트, 개발 명령어
- 코딩 스타일 & 명명 규칙
- 테스트 가이드라인
- Commit & Pull Request 가이드라인
- 보안 & 설정 팁

---

### 🏗️ DDD 레이어별 테스트

**📁 [layers/](./layers/)** - 아키텍처 레이어별 상세 테스트 케이스

| 파일 | 설명 | 주요 내용 |
|------|------|----------|
| [layer-1-domain.md](./layers/layer-1-domain.md) | 도메인 레이어 | Value Objects, Aggregate Roots (User, Policy) |
| [layer-2-application.md](./layers/layer-2-application.md) | 애플리케이션 레이어 | Commands, Application Services, Query Services |
| [layer-3-infrastructure.md](./layers/layer-3-infrastructure.md) | 인프라 레이어 | Repository Implementations |
| [layer-4-interface.md](./layers/layer-4-interface.md) | 인터페이스 레이어 | REST API Controllers (MockMvc) |

---

### 🔐 횡단 관심사 (Cross-Cutting Concerns)

**📁 [cross-cutting/](./cross-cutting/)** - 시스템 전반에 걸친 공통 테스트

| 파일 | 설명 | 주요 내용 |
|------|------|----------|
| [login-policy.md](./cross-cutting/login-policy.md) | 로그인 정책 관리 | Domain Model, Application Service, Repository |
| [authentication.md](./cross-cutting/authentication.md) | 인증 (929줄) | AD/SSO/Local Login, Security Policies, Session Management |
| [jwt.md](./cross-cutting/jwt.md) | JWT 토큰 관리 | Generation, Validation, Refresh, Revocation |
| [audit-logging.md](./cross-cutting/audit-logging.md) | 감사 로그 | Login Success/Failure, Token Operations, Security Events |
| [misc.md](./cross-cutting/misc.md) | 기타 | Password Management, Exception Handling |

---

### 🛠️ 구현 가이드 (Implementation Guides)

**📁 [implementation/](./implementation/)** - 구현에 필요한 상세 가이드

| 파일 | 설명 | 주요 내용 |
|------|------|----------|
| [backend-guide.md](./implementation/backend-guide.md) | Backend 구현 가이드 | 환경 변수, ConfigurationService, Entity, Repository, REST API, Flyway |
| [frontend-guide.md](./implementation/frontend-guide.md) | Frontend UI 설계 | System Configuration UI, 컴포넌트 구조, Pinia Store |
| [checklist.md](./implementation/checklist.md) | 구현 체크리스트 | Backend, Frontend, Documentation 체크리스트 |
| [considerations.md](./implementation/considerations.md) | 추가 고려사항 | 검증, 에러 처리, 성능, 운영, 보안, 테스트 전략 |

---

## 🚀 빠른 시작 가이드

### "go" 명령어 워크플로우

1. **[plan.md](./plan.md)** 열기
2. 다음 체크되지 않은 테스트 찾기
3. **Red → Green → Refactor** 사이클 실행:
   - **Red**: 실패하는 테스트 작성 (BDD: Given-When-Then)
   - **Green**: 테스트를 통과하는 최소 코드 구현
   - **Refactor**: 코드 개선 (테스트는 계속 통과)
4. 테스트 완료 시 `[x]` 표시
5. Commit
6. 다음 "go"까지 대기

### 테스트 실행

```bash
# 단일 테스트 실행
./gradlew test --tests "AuthServiceTest.shouldReturnJwtTokenWhenValidCredentials"

# 특정 클래스 테스트
./gradlew test --tests "AuthServiceTest"

# 전체 테스트
./gradlew test

# 커버리지 리포트
./gradlew test jacocoTestReport
open backend/auth/build/reports/jacoco/test/html/index.html
```

---

## 📊 테스트 커버리지 목표

| Layer | Minimum Coverage | Test Type |
|-------|-----------------|-----------|
| **Domain Logic** | 90% | Unit Tests |
| **Service Layer** | 80% | Unit Tests |
| **Repository** | 70% | Integration Tests |
| **Controller** | 80% | Integration Tests |

---

## 📝 개발 규칙

### TDD + BDD 원칙

- **TDD**: Red → Green → Refactor 사이클 엄격히 준수
- **BDD**: Given-When-Then 구조로 테스트 작성
- **@DisplayName**: 한글로 비즈니스 시나리오 명확히 표현

**예시:**
```java
@Test
@DisplayName("유효한 사원ID와 비밀번호로 로그인 시 JWT 토큰을 반환한다")
void shouldReturnJwtTokenWhenValidCredentials() {
    // Given (준비)
    LoginRequest request = new LoginRequest("EMP001", "P@ssw0rd");

    // When (실행)
    TokenResponse response = authService.login(request);

    // Then (검증)
    assertThat(response.getAccessToken()).isNotNull();
}
```

### Lombok 사용 정책 (MUST)

**❌ 금지:**
- `@Data` (너무 광범위)

**✅ 허용:**
- `@Getter`, `@Setter`, `@Builder`
- `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`
- `@Slf4j`, `@ToString`, `@EqualsAndHashCode`

### 도메인 설계 원칙

- **ULID 사용**: 모든 Entity ID는 ULID 사용
- **스냅샷 패턴**: Policy 등 버전 관리 필요 도메인
- **도메인 이벤트**: 중요 상태 변경 시 이벤트 발행
- **Maker-Checker**: 모든 승인 프로세스
- **감사 로그**: 모든 중요 작업 기록

---

## 🔗 관련 문서

### Backend
- [Backend README](../backend/README.md)
- [Backend TOOLS](../backend/TOOLS.md) - API Generator, Module Validator
- [Backend TESTING](../backend/TESTING.md)
- [ULID Guide](../backend/ULID.md)

### Frontend
- [Frontend README](../frontend/README.md)
- [Frontend TOOLS](../frontend/TOOLS.md) - MSW Mock, Component Generator
- [Frontend TESTING](../frontend/TESTING.md)

### Architecture
- [DDD Design](../architecture/DDD_DESIGN.md)
- [Security Architecture](../architecture/SECURITY.md)

### API
- [API Contract](../api/CONTRACT.md)
- [API Design](../api/DESIGN.md)
- [Authentication API](../api/AUTHENTICATION.md)

---

## 🔄 변경 이력

| 날짜 | 변경 내용 |
|------|-----------|
| 2025-01-15 | Progressive Disclosure 적용 - development/ 구조화 |
| 2025-01-15 | plan.md Progressive Disclosure 적용 (3259줄 → 228줄) |
| 2025-01-15 | TDD + BDD로 제목 정정 |
| 2025-01-13 | 초기 테스트 계획 작성 |
