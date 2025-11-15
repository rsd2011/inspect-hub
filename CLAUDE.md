# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Inspect-Hub** is a comprehensive AML (Anti-Money Laundering) integrated compliance monitoring system for financial institutions in Korea. The system detects suspicious transactions (STR), currency transaction reports (CTR), watch-list filtering (WLF), and manages compliance workflows with regulatory compliance for Korean financial law and FATF guidelines.

**Current Status:** Planning/POC Phase - comprehensive PRD split into 15 documents in `/docs/prd/`, minimal code implementation.

## 📚 Documentation Structure

**All project documentation is centralized in the `/docs` directory:**

- **[Documentation Center](./docs/index.md)** - Start here for complete navigation
- **[PRD (Product Requirements)](./docs/prd/index.md)** - 15 documents covering all requirements
- **[Frontend Guide](./docs/frontend/README.md)** - Nuxt 4, components, architecture
- **[Backend Guide](./docs/backend/AGENTS.md)** - Spring Boot, API development
- **[API Contract](./docs/api/CONTRACT.md)** - Frontend ↔ Backend communication
- **[Architecture](./docs/architecture/DDD_DESIGN.md)** - System design, DDD patterns
- **[Development Guide](./docs/development/AGENTS.md)** - Coding rules, testing, builds

## TDD Development Workflow

This project follows **Kent Beck's TDD + Domain-Driven Design** methodology.

### "go" Command Workflow

When you type **"go"**:

1. **Find next unchecked test** in [plan.md](./docs/development/plan.md)
2. **Execute Red → Green → Refactor cycle:**
   - **Red**: Write failing test first
   - **Green**: Implement minimum code to pass
   - **Refactor**: Improve code while keeping tests green
3. **Mark test as `[x]`** in [plan.md](./docs/development/plan.md)
4. **Commit** (only when all tests pass)
5. **Wait for next "go"**

**References**:
- **[Test Plan](./docs/development/plan.md)** ⭐ Current test checklist (always check here)
- **[TDD_DDD_WORKFLOW.md](./docs/development/TDD_DDD_WORKFLOW.md)** - Detailed workflow guide

## Tech Stack

**Backend:**
- Java 21 + Spring Boot 3.3.2
- Spring Security for authentication/authorization
- MyBatis for database access
- Gradle multi-module build system
- PostgreSQL (primary DB) + Redis (caching) + Kafka (messaging)
- Spring Batch for batch processing

**Frontend:**
- Nuxt 4 (Vue 3 Composition API) - **⚠️ SPA MODE ONLY - SSR STRICTLY FORBIDDEN** (`ssr: false`)
- Vite build system
- UI: PrimeVue + RealGrid (commercial grid) + Tailwind CSS (with prefix `tw-`)
- State: Pinia
- Validation: VeeValidate + Zod
- Charts: Apache ECharts
- i18n: @nuxtjs/i18n
- **Deployment:** Static resources only (Nginx/Apache hosting)

**Project Structure:**
```
inspect-hub/
├── docs/                           # 📚 Central documentation
├── backend/                        # Spring Boot multi-module
│   ├── common/                     # Shared entities, DTOs, utilities
│   ├── policy/, detection/, ...    # Domain modules
│   └── server/                     # Main application
└── frontend/                       # Nuxt 4 application (SPA only)
    ├── app/                        # Application layer (Nuxt 4 standard)
    └── shared/                     # Shared resources
```

## Critical Constraints

### ⚠️ Frontend: SSR STRICTLY FORBIDDEN

**This frontend MUST be deployed as static resources only. SSR is absolutely prohibited.**

```typescript
// ✅ REQUIRED - nuxt.config.ts
export default defineNuxtConfig({
  ssr: false,  // MUST always be false
})

// ❌ FORBIDDEN - No /server directory
// ❌ FORBIDDEN - No Nuxt Server API routes
// ❌ FORBIDDEN - No server-side only features
```

**See [Frontend Guide](./docs/frontend/README.md) for complete constraints.**

### 🖥️ Web Browser Focus - Desktop/Laptop Priority

**This project targets desktop/laptop web browsers, NOT mobile devices.**

- **Primary Target:** Desktop/Laptop (1366px+ resolutions)
- **NOT Mobile-First:** Mobile-specific UI/UX not required
- **Responsive Design:** Must support various desktop resolutions

**See [Frontend Guide](./docs/frontend/README.md) for design guidelines.**

## Backend Coding Standards

### Lombok Usage Policy (MUST)

**❌ PROHIBITED:**
- `@Data` - **절대 금지** (Too broad)

**✅ ALLOWED:**
- `@Getter`, `@Setter`, `@Builder`
- `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`
- `@Slf4j`, `@ToString`, `@EqualsAndHashCode`

**Recommended Patterns:**
```java
// DTO Pattern
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO { ... }

// Entity Pattern
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User { ... }

// Service Pattern (DI)
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService { ... }
```

### Test-Driven Development (TDD) - MANDATORY

**ALL features MUST be written with tests FIRST.**

**Red → Green → Refactor Cycle:**
1. **Red**: Write failing test
2. **Green**: Implement minimum code
3. **Refactor**: Improve code while keeping tests green

**BDD-style Test Structure (Given-When-Then):**
```java
@Test
@DisplayName("유효한 사원ID와 비밀번호로 로그인 시 JWT 토큰을 반환한다")
void shouldReturnJwtTokenWhenValidCredentials() {
    // Given (준비)
    // ... setup

    // When (실행)
    // ... execute

    // Then (검증)
    // ... assertions
}
```

**Test Coverage Requirements:**

| Layer | Minimum Coverage | Test Type |
|-------|-----------------|-----------|
| **Domain Logic** | 90% | Unit Tests |
| **Service Layer** | 80% | Unit Tests |
| **Repository** | 70% | Integration Tests |
| **Controller** | 80% | Integration Tests |

**See [Development Guide](./docs/development/AGENTS.md) for complete TDD workflow.**

## Development Commands

### Backend (Gradle)

```bash
# Build all modules
./gradlew buildAll

# Run backend server
./gradlew :backend:server:bootRun

# All tests
./gradlew test

# Specific test
./gradlew test --tests AuthServiceTest.shouldReturnJwtTokenWhenValidCredentials

# Coverage report
./gradlew test jacocoTestReport
open backend/auth/build/reports/jacoco/test/html/index.html
```

### Frontend (Nuxt 4)

```bash
cd frontend

# Development
npm run dev              # Dev server (http://localhost:3000)

# Build & Preview
npm run build            # Production build (SPA mode)
npm run preview          # Preview production build

# Code Quality
npm run lint             # Check code quality
npm run typecheck        # TypeScript type checking

# Testing
npm test                 # Unit tests (Vitest)
npm run test:e2e         # E2E tests (Playwright)
```

## Core Architecture Principles

### 1. Snapshot-Based Versioning

**All policy/standard data uses snapshot structure:**
- Similar to Spring Batch's `BATCH_JOB_INSTANCE` / `BATCH_JOB_EXECUTION` pattern
- Core table: `STANDARD_SNAPSHOT` with version control
- Immutable once approved/deployed
- Support rollback to previous versions

### 2. Audit Requirements (100% Coverage)

**Every action must be logged:**
- User access logs (login, page views, data access)
- Data modification logs (before/after state)
- Configuration changes (policy updates, rule deployments)
- Approval workflows

**Implementation:** `AUDIT_LOG` table with `who`, `when`, `what`, `before_json`, `after_json`

### 3. Separation of Duties (SoD)

**Maker-Checker principle:**
- Same user cannot both create and approve
- Enforce at application level via RBAC
- Approval lines configured by organization + permission group

### 4. Detection Engine Architecture

**Core data flow:**
```
Data Ingestion → Detection (STR/CTR/WLF) → Event Generation → Case Creation →
Investigation → Approval Workflow → Reporting → FIU Submission
```

**See [Architecture Guide](./docs/architecture/DDD_DESIGN.md) for complete design.**

## Key Technical Requirements

### Performance Targets

- **Batch processing:** ≥10 million transactions/day (overnight window)
- **Real-time API:** 300-500 TPS peak load
- **UI response:** <1 second (queries), <3 seconds (complex searches)
- **Availability:** 99.9% SLA

### Security Requirements

#### Authentication (3가지 로그인 방식)

1. **AD 로그인 (Active Directory)** - LDAP/Kerberos
2. **SSO 로그인 (Single Sign-On)** - Backend API 직접 호출 방식
3. **일반 로그인 (Fallback)** - 자체 DB 인증 (BCrypt)

**Authentication Flow:** SSO 사용 가능? → YES → SSO 인증 → JWT 발급
                        ↓ NO
                        AD 서버 사용 가능? → YES → AD 인증 → JWT 발급
                        ↓ NO
                        일반 로그인 (DB 인증) → JWT 발급

#### Authorization & Data Security

- **Encryption:** TLS in transit, field-level encryption for PII at rest
- **Authorization:** RBAC + SoD enforcement
- **Data masking:** Configurable by role
- **Session:** JWT-based stateless authentication

**See [Architecture Guide](./docs/architecture/DDD_DESIGN.md) for complete security design.**

## Important Implementation Notes

1. **⚠️ SSR STRICTLY FORBIDDEN:** Frontend MUST use `ssr: false` - Deploy as static resources only
2. **🖥️ WEB BROWSER FOCUS:** UI targets desktop/laptop browsers (1366px+)
3. **🧪 TDD MANDATORY:** ALL features MUST have tests written FIRST
4. **🚫 NO @Data:** Lombok `@Data` annotation is PROHIBITED
5. **Tailwind Prefix:** Use `tw-` prefix to avoid conflicts
6. **Field Naming:** snake_case (DB), camelCase (Java/TypeScript)
7. **ID Strategy:** Use ULID or UUID for global identifiers
8. **Cache Strategy:** Redis cache with Caffeine for local L1 cache
9. **API Versioning:** Use `/api/v1/` prefix
10. **Logging:** Structured JSON logs with trace IDs

## Reference

**📚 All documentation is centralized in `/docs` directory. See [docs/index.md](./docs/index.md) for complete navigation.**

### Core Documentation

- **[Documentation Center](./docs/index.md)** - Start here
- **[PRD Index](./docs/prd/index.md)** - Product requirements (15 documents)
- **[API Contract](./docs/api/CONTRACT.md)** - Frontend ↔ Backend API

### Frontend

- **[Frontend README](./docs/frontend/README.md)** - SPA constraints, coding rules, deployment
- **[Components Roadmap](./docs/frontend/COMPONENTS_ROADMAP.md)** - Implementation progress

### Backend

- **[Backend Agents](./docs/backend/AGENTS.md)** - API Generator, Module Validator
- **[ULID Guide](./docs/backend/ULID.md)** - Identifier strategy

### Architecture

- **[DDD Design](./docs/architecture/DDD_DESIGN.md)** - Domain-Driven Design layers

### Development

- **[Test Plan](./docs/development/plan.md)** ⭐ Current test checklist (check before "go")
- **[TDD Workflow](./docs/development/TDD_DDD_WORKFLOW.md)** - Complete TDD guide
- **[Development Guide](./docs/development/AGENTS.md)** - Coding style, testing, commit rules

### Timeline

- **MVP Go-Live Target:** January 20, 2026
