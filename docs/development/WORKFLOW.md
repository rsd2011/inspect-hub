# Development Guidelines - Inspect-Hub

> **TDD + DDD 기반 개발 가이드**
>
> **Version**: 2.0
> **Last Updated**: 2025-01-13

---

## 📋 목차

1. [TDD + DDD 워크플로우](#tdd--ddd-워크플로우)
2. [프로젝트 구조 & 모듈 구성](#프로젝트-구조--모듈-구성)
3. [빌드, 테스트, 개발 명령어](#빌드-테스트-개발-명령어)
4. [코딩 스타일 & 명명 규칙](#코딩-스타일--명명-규칙)
5. [테스트 가이드라인](#테스트-가이드라인)
6. [Commit & Pull Request 가이드라인](#commit--pull-request-가이드라인)
7. [보안 & 설정 팁](#보안--설정-팁)

---

## TDD + DDD 워크플로우

### 📖 plan.md 기반 개발

**이 프로젝트는 Kent Beck의 TDD와 Domain-Driven Design을 결합하여 개발합니다.**

#### "go" 명령어 워크플로우

사용자가 **"go"**를 입력하면:

1. **`plan.md`에서 다음 체크되지 않은 테스트 찾기**
2. **Red → Green → Refactor 사이클 실행:**
   - **Red**: 실패하는 테스트 먼저 작성
   - **Green**: 테스트를 통과시키기 위한 최소한의 코드 구현
   - **Refactor**: 테스트가 통과한 상태에서 코드 개선
3. **`plan.md`에서 해당 테스트 체크 (`[x]` 표시)**
4. **Commit** (모든 테스트 통과 확인 후)
5. **다음 "go" 대기**

#### 핵심 원칙

| 원칙 | 설명 | 예제 |
|------|------|------|
| **Test First** | 코드 작성 전 테스트 먼저 작성 | `shouldRejectDuplicatePolicyName()` 테스트 → `PolicyDomainService.validateUniqueness()` 구현 |
| **Minimal Implementation** | 테스트를 통과시키는 최소 코드만 작성 | 하드코딩도 OK, 나중에 리팩토링 |
| **Tidy First** | 구조적 변경(리팩토링)과 행동적 변경(기능 추가) 분리 | Commit 1: `refactor: Extract method`, Commit 2: `feat: Add validation` |
| **DDD Layers** | Domain → Application → Infrastructure → Interface 순서로 개발 | Domain 테스트 → Application 테스트 → Controller 테스트 |

#### 참고 문서

자세한 내용은 **[TDD_DDD_WORKFLOW.md](./TDD_DDD_WORKFLOW.md)** 참조

---

## 프로젝트 구조 & 모듈 구성
- `backend/common` holds shared DTOs, exception types, and utilities; wire new shared contracts here so they can be reused across modules.
- `backend/server` is the Spring Boot service. Place controllers, services, and mappers under `src/main/java/com/inspecthub/aml/server/**`; keep SQL mappers under `src/main/resources/mybatis`.
- Environment defaults live in `backend/server/src/main/resources/application*.yml`; use profile-specific files (`application-dev.yml`, `application-prod.yml`) for overrides instead of in-code switches.

## 빌드, 테스트, 개발 명령어

### Backend (Gradle)

```bash
# 전체 빌드 (컴파일 + 테스트)
./gradlew clean build

# 특정 모듈 빌드
./gradlew :backend:server:build

# 로컬 API 서버 실행 (H2 in-memory DB)
./gradlew :backend:server:bootRun

# 개발 프로필로 실행
./gradlew :backend:server:bootRun --args='--spring.profiles.active=dev'

# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests PolicyTest

# 특정 테스트 메서드 실행
./gradlew test --tests PolicyTest.shouldCreatePolicyWithValidVersion

# 장기 실행 테스트 제외
./gradlew test -x longRunningTests

# 테스트 커버리지 리포트 생성
./gradlew test jacocoTestReport
open backend/server/build/reports/jacoco/test/html/index.html
```

### Frontend (Nuxt 4)

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (http://localhost:3000)
npm run dev

# 프로덕션 빌드 (SPA 모드)
npm run build

# 빌드 결과 미리보기
npm run preview

# 타입 체크
npm run typecheck

# Lint
npm run lint

# Lint 자동 수정
npm run lint:fix

# 단위 테스트 (Vitest)
npm test

# E2E 테스트 (Playwright)
npm run test:e2e
```

## 코딩 스타일 & 명명 규칙

### Backend (Java)

- **Java 21** 사용, 4-space 들여쓰기
- **Package 구조**: `com.inspecthub.aml.server.<layer>`
  - `domain` - 도메인 레이어 (Aggregate, Value Object, Domain Service)
  - `application` - 애플리케이션 레이어 (Application Service, Command, Query)
  - `infrastructure` - 인프라 레이어 (Repository 구현)
  - `interfaces` - 인터페이스 레이어 (Controller, DTO)
- **DTO**: Immutable + Lombok `@Builder` 사용
- **Mapper**: MapStruct 사용, `mapper` 패키지에 배치
- **API 엔드포인트**: `/api/v1/**` 유지
- **Database**: `snake_case` 사용 (SQL, MyBatis XML)

### Frontend (TypeScript)

- **Naming Conventions**:
  - Components: `PascalCase.vue` (예: `LoginPage.vue`, `AppHeader.vue`)
  - Composables: `camelCase` with `use` prefix (예: `useAuth.ts`)
  - Stores: `kebab-case` (예: `auth.ts`, `user.ts`)
  - Utils: `kebab-case` (예: `format.ts`)
- **Tailwind CSS**: `tw-` prefix 필수 (PrimeVue 충돌 방지)
- **Props/Emits**: TypeScript 타입 정의 필수

## 테스트 가이드라인

### Backend Testing (TDD + DDD)

#### 레이어별 테스트 전략

| Layer | Test Type | Tools | Coverage Target |
|-------|-----------|-------|-----------------|
| **Domain** | Unit Tests | JUnit 5, AssertJ | 95% |
| **Application** | Unit + Integration | JUnit 5, Mockito, Testcontainers | 90% |
| **Infrastructure** | Integration Tests | JUnit 5, Testcontainers (PostgreSQL) | 80% |
| **Interface** | Integration Tests | MockMvc, @WebMvcTest | 85% |

#### 테스트 작성 규칙

1. **Given-When-Then 패턴** 사용
2. **테스트 이름은 행동 중심** (예: `shouldRejectDuplicatePolicyName`)
3. **`@DisplayName`** 사용하여 한글 설명 추가
4. **Mock 최소화** - Domain Layer에서는 실제 객체 사용
5. **Testcontainers** - 통합 테스트에서 실제 DB 사용
6. **모든 기능은 최소 1개의 성공 케이스 + 1개의 실패 케이스** 테스트 필요

#### 테스트 예제

```java
@Test
@DisplayName("중복된 이름의 Policy 생성 시 예외 발생")
void shouldRejectDuplicatePolicyName() {
    // Given
    String duplicateName = "KYC-Standard-v1";
    Policy existingPolicy = Policy.create(new PolicyId(), duplicateName, PolicyType.KYC);
    policyRepository.save(existingPolicy);

    // When & Then
    assertThatThrownBy(() -> {
        Policy newPolicy = Policy.create(new PolicyId(), duplicateName, PolicyType.KYC);
        policyDomainService.validateUniqueness(newPolicy);
    })
    .isInstanceOf(DuplicatePolicyException.class)
    .hasMessageContaining("이미 존재하는 정책 이름입니다");
}
```

### Frontend Testing

- **Unit Tests**: Vitest + Testing Library
- **E2E Tests**: Playwright (Desktop 브라우저 중심)
- **Component Tests**: Render 테스트, 이벤트 테스트
- **Store Tests**: Pinia store 액션/상태 테스트

자세한 내용은 **[docs/backend/TESTING.md](../backend/TESTING.md)** 및 **[docs/frontend/TESTING.md](../frontend/TESTING.md)** 참조

## Commit & Pull Request 가이드라인

### Commit 규칙 (Tidy First 원칙)

**모든 변경사항은 두 가지 유형으로 분류:**

1. **STRUCTURAL CHANGES (구조적 변경)** - 행동 변경 없이 코드 재배치
   - Commit Type: `refactor:`
   - 예: 메서드 추출, 이름 변경, 파일 이동
   - 테스트: 변경 전/후 동일하게 통과

2. **BEHAVIORAL CHANGES (행동적 변경)** - 기능 추가/수정
   - Commit Type: `feat:`, `fix:`
   - 예: 새 기능 추가, 버그 수정
   - 테스트: 새로운 테스트 추가 또는 기존 테스트 수정

**절대 규칙: 구조적 변경과 행동적 변경을 같은 Commit에 혼합하지 않는다**

### Commit 조건

**다음 조건을 모두 만족할 때만 Commit:**

- [ ] **모든 테스트가 통과** (`./gradlew test`)
- [ ] **컴파일러/린터 경고 해결** (`./gradlew build`, `npm run lint`)
- [ ] **단일 논리적 작업 단위**
- [ ] **구조적 또는 행동적 변경 중 하나만 포함**

### Conventional Commits 형식

```bash
<type>(<scope>): <subject>

<body>

<footer>
```

**Type:**
- `feat`: 새로운 기능 (행동적 변경)
- `fix`: 버그 수정 (행동적 변경)
- `refactor`: 리팩토링 (구조적 변경)
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅
- `chore`: 빌드/설정 변경

**예제:**

```bash
# 구조적 변경
git commit -m "refactor(policy): Extract validateUniqueness method

- PolicyDomainService에서 중복 검증 로직 추출
- 테스트는 변경 없음 (구조적 변경만)
"

# 행동적 변경
git commit -m "feat(policy): Add duplicate policy name validation

- Policy 생성 시 이름 중복 검사
- DuplicatePolicyException 추가
- 테스트: shouldRejectDuplicatePolicyName
"
```

### Pull Request 가이드라인

- **의도 설명**: 무엇을, 왜 변경했는지 명확히 기술
- **영향받는 모듈 명시**: `backend/common`, `backend/server`, `frontend` 등
- **테스트 결과 포함**: 테스트 통과 스크린샷 또는 로그
- **API 변경 시**: 샘플 요청/응답 예제 포함
- **UI 변경 시**: 스크린샷 또는 동영상 첨부

## 보안 & 설정 팁

### Secrets 관리

- ✅ **환경 변수** 사용 (`.env` 파일)
- ❌ **코드에 직접 하드코딩 금지** (JWT 키, DB 비밀번호, API 키 등)
- ✅ **프로필별 설정** (`application-dev.yml`, `application-prod.yml`)
- ❌ **`.env` 파일을 Git에 커밋하지 않음** (`.gitignore` 포함)

### 로컬 개발 환경

- **Redis, PostgreSQL, Kafka** - 프로필에서 명시적으로 활성화하지 않으면 Mock 사용
- **H2 In-memory DB** - 로컬 개발용 기본 데이터베이스
- **Testcontainers** - 통합 테스트 시 실제 DB/Redis 사용

---

## 📚 추가 참고 문서

- **[TDD_DDD_WORKFLOW.md](./TDD_DDD_WORKFLOW.md)** - TDD + DDD 상세 워크플로우
- **[plan.md](/plan.md)** - 테스트 계획서 (TDD 체크리스트)
- **[docs/architecture/DDD_DESIGN.md](../architecture/DDD_DESIGN.md)** - DDD 레이어 구조
- **[docs/backend/TESTING.md](../backend/TESTING.md)** - Backend 테스트 가이드
- **[docs/frontend/TESTING.md](../frontend/TESTING.md)** - Frontend 테스트 가이드
