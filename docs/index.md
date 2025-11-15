# Inspect-Hub 문서 센터

> **AML 통합 준법감시 시스템 - 중앙 문서 저장소**

프로젝트의 모든 기술 문서가 이 디렉토리에 체계적으로 정리되어 있습니다.

---

## 📚 문서 구조

### [📋 PRD (Product Requirements Document)](./prd/index.md)

제품 요구사항 정의 및 기능 명세

- [00. 프로젝트 개요](./prd/00-overview.md) - 사용자 롤, 핵심 KPI
- [01. 배경 및 문제 정의](./prd/01-background-and-problems.md)
- [02. 목표 및 비전](./prd/02-objectives-and-goals.md)
- [03-1. 기능 요구사항 - 정책·기준 관리](./prd/03-1-policy-management.md)
- [03-2. 기능 요구사항 - 리스크 분석](./prd/03-2-risk-simulation.md)
- [03-3. 기능 요구사항 - 탐지·보고·점검](./prd/03-3-detection-reporting.md)
- [03-4. 기능 요구사항 - 운영 및 관리](./prd/03-4-operations.md)
- [04. 비기능 요구사항](./prd/04-non-functional-requirements.md)
- [05. 시스템 연계](./prd/05-system-integration.md)
- [06. 데이터 구조/모델](./prd/06-data-structure.md)
- [07. 사용자 및 권한](./prd/07-users-and-roles.md)
- [08. 일정 및 우선순위](./prd/08-timeline-and-priority.md)
- [09. 품질보증 및 검증](./prd/09-qa-and-validation.md)
- [10. 이해관계자](./prd/10-stakeholders.md)
- [11. 기타](./prd/11-misc.md)

### [🎨 Frontend](./frontend/)

프론트엔드 개발 가이드 및 컴포넌트 로드맵

- **[README.md](./frontend/README.md)** - 전체 가이드, SSR 제약사항, 코딩 규칙
- **[COMPONENTS_ROADMAP.md](./frontend/COMPONENTS_ROADMAP.md)** - 공통 컴포넌트, 시스템 클래스, 구현 계획
- **[TOOLS.md](./frontend/TOOLS.md)** - Mock API Server (MSW), Component Generator, Build Validator
- **[TESTING.md](./frontend/TESTING.md)** - Vitest, Testing Library, Playwright E2E, MSW 모킹
- **[STATE_MANAGEMENT.md](./frontend/STATE_MANAGEMENT.md)** - Pinia Store 패턴, SSE 알림, 상태 영속화

**핵심 주제:**
- FSD (Feature-Sliced Design) + Atomic Design 아키텍처
- SPA 모드 전용 (SSR 금지)
- RealGrid2 통합 가이드
- 페이지 템플릿 구조 (BasePage, ListPage, FormPage, DetailPage)
- TabManager (VS Code 스타일)
- PageStateManager (상태 유지)

### [⚙️ Backend](./backend/)

백엔드 개발 가이드 및 설계 문서

- **[README.md](./backend/README.md)** - 백엔드 전체 가이드, 멀티모듈 구조, MyBatis 패턴
- **[TOOLS.md](./backend/TOOLS.md)** - API Generator, Module Validator
- **[ULID.md](./backend/ULID.md)** - ULID 식별자 가이드 (26자 time-sortable ID)
- **[TESTING.md](./backend/TESTING.md)** - JUnit 5, Mockito, Testcontainers, API 테스트, 커버리지

**핵심 기술:**
- Java 21 + Spring Boot 3.3.2
- MyBatis (SQL Mapper)
- Spring Security (JWT, OAuth2)
- Swagger (springdoc-openapi)

### [🔗 API](./api/)

API 계약 및 명세

- **[CONTRACT.md](./api/CONTRACT.md)** - Frontend ↔ Backend API 계약 및 동기화 계획
- **[DESIGN.md](./api/DESIGN.md)** - RESTful API 설계 원칙, URL 구조, 에러 처리
- **[AUTHENTICATION.md](./api/AUTHENTICATION.md)** - 인증 API 설계 (AD, SSO, 일반 로그인)
- **[ENDPOINTS.md](./api/ENDPOINTS.md)** - 전체 API 엔드포인트 명세 및 Request/Response 예제

### [🏗️ Architecture](./architecture/)

시스템 아키텍처 문서

- **[OVERVIEW.md](./architecture/OVERVIEW.md)** - 전체 시스템 아키텍처, 모듈 구조, 기술 스택, 성능 목표
- **[DDD_DESIGN.md](./architecture/DDD_DESIGN.md)** - DDD 레이어 구조, 도메인 모델, 테스트 전략
- **[DATABASE.md](./architecture/DATABASE.md)** - ERD, 테이블 스키마, 인덱스 전략, 파티셔닝, 백업/복구
- **[SECURITY.md](./architecture/SECURITY.md)** - JWT 인증, AES-256 암호화, RBAC, 감사 로깅

### [🛠️ Development](./development/)

개발 방법론 및 테스트 계획

- **[Development Guide](./development/index.md)** - 전체 개발 가이드 (TDD + BDD + DDD)
- **[Test Plan](./development/plan.md)** - TDD + BDD 테스트 계획서 (Progressive Disclosure 적용)
- **[TDD + DDD Workflow](./development/TDD_DDD_WORKFLOW.md)** - 상세 워크플로우 가이드
- **[Development Workflow](./development/WORKFLOW.md)** - 일반 개발 가이드, 코딩 스타일, 커밋 규칙

**하위 문서:**
- **DDD Layers** - [Layer 1 (Domain)](./development/layers/layer-1-domain.md) | [Layer 2 (Application)](./development/layers/layer-2-application.md) | [Layer 3 (Infrastructure)](./development/layers/layer-3-infrastructure.md) | [Layer 4 (Interface)](./development/layers/layer-4-interface.md)
- **Cross-Cutting** - [Login Policy](./development/cross-cutting/login-policy.md) | [Authentication](./development/cross-cutting/authentication.md) | [JWT](./development/cross-cutting/jwt.md) | [Audit Logging](./development/cross-cutting/audit-logging.md)
- **Implementation** - [Backend Guide](./development/implementation/backend-guide.md) | [Frontend Guide](./development/implementation/frontend-guide.md) | [Checklist](./development/implementation/checklist.md) | [Considerations](./development/implementation/considerations.md)

### [🚀 Deployment](./DEPLOYMENT.md)

배포 및 운영 가이드

- Docker 컨테이너화 (멀티스테이지 빌드)
- Kubernetes 배포 (Blue-Green 전략)
- Flyway 데이터베이스 마이그레이션
- CI/CD 파이프라인 (GitHub Actions)
- 모니터링 (Prometheus + Grafana)
- 로깅 (ELK Stack)

---

## 🎯 문서 탐색 가이드

### 신규 개발자 온보딩 순서

1. **[프로젝트 루트 CLAUDE.md](../CLAUDE.md)** - 프로젝트 전체 개요 및 기술 스택
2. **[PRD 개요](./prd/00-overview.md)** - 비즈니스 요구사항 이해
3. **[Frontend 가이드](./frontend/README.md)** - 프론트엔드 개발 규칙
4. **[컴포넌트 로드맵](./frontend/COMPONENTS_ROADMAP.md)** - 공통 컴포넌트 구현 현황
5. **[API 계약](./api/CONTRACT.md)** - Backend ↔ Frontend 통신 규약

### 역할별 주요 문서

**프로젝트 매니저 (PM):**
- [PRD 전체](./prd/index.md)
- [일정 및 우선순위](./prd/08-timeline-and-priority.md)
- [이해관계자](./prd/10-stakeholders.md)

**프론트엔드 개발자:**
- [Frontend README](./frontend/README.md) - **필독**
- [컴포넌트 로드맵](./frontend/COMPONENTS_ROADMAP.md) - **필독**
- [API 계약](./api/CONTRACT.md)
- [PRD - 기능 요구사항](./prd/03-1-policy-management.md)

**백엔드 개발자:**
- [Backend README](./backend/README.md) - **필독**
- [Backend TESTING](./backend/TESTING.md) - **필독**
- [ULID 가이드](./backend/ULID.md)
- [API 계약](./api/CONTRACT.md)
- [API 엔드포인트](./api/ENDPOINTS.md)
- [PRD - 데이터 구조](./prd/06-data-structure.md)

**QA/테스터:**
- [PRD - 품질보증](./prd/09-qa-and-validation.md)
- [비기능 요구사항](./prd/04-non-functional-requirements.md)
- [Backend TESTING](./backend/TESTING.md)
- [Frontend TESTING](./frontend/TESTING.md)

**아키텍트:**
- [아키텍처 개요](./architecture/OVERVIEW.md) - **필독**
- [DDD 설계](./architecture/DDD_DESIGN.md)
- [데이터베이스 설계](./architecture/DATABASE.md)
- [보안 아키텍처](./architecture/SECURITY.md)
- [시스템 연계](./prd/05-system-integration.md)
- [데이터 구조](./prd/06-data-structure.md)
- [비기능 요구사항](./prd/04-non-functional-requirements.md)

**DevOps:**
- [배포 가이드](./DEPLOYMENT.md) - **필독**
- [개발 워크플로우](./development/WORKFLOW.md)
- [보안 설정](./architecture/SECURITY.md)

---

## 📝 문서 작성 규칙

### Markdown 스타일 가이드

1. **헤딩 계층 구조**:
   - `#`: 문서 제목 (1개만)
   - `##`: 주요 섹션
   - `###`: 하위 섹션
   - `####`: 세부 항목

2. **코드 블록**:
   ```typescript
   // 언어 명시 필수
   const example = 'code'
   ```

3. **표 사용**:
   - 비교, 옵션, 체크리스트에 적극 활용

4. **이모지 사용**:
   - 섹션 구분: 📋 📚 🎯 🚀 ⚙️ 🔗
   - 상태 표시: ✅ ❌ 🔴 🟡 🟢
   - 주의/경고: ⚠️ 💡 📌

### 문서 업데이트 절차

1. 문서 수정 시 변경 이력 기록 (하단 Change Log)
2. 관련 문서 간 링크 동기화
3. Git commit 메시지에 문서 변경 내역 명시

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-15 | **Progressive Disclosure 전면 적용** - development/ 구조화 (13개 파일 → 체계적 계층 구조) | PM |
| 2025-01-15 | development/plan.md Progressive Disclosure 적용 (3259줄 → 228줄 메인 + 13개 하위 파일) | PM |
| 2025-01-15 | 파일명 정리: AGENTS.md → TOOLS.md (backend/frontend), WORKFLOW.md (development) | PM |
| 2025-01-15 | development/index.md, layers/index.md, implementation/index.md 생성 | PM |
| 2025-01-15 | docs/index.md 대폭 업데이트 (development 섹션 보강, 하위 문서 링크 추가) | PM |
| 2025-01-13 | 핵심 기술 문서 9개 추가 (OVERVIEW, DATABASE, SECURITY, DESIGN, ENDPOINTS, TESTING x2, DEPLOYMENT, STATE_MANAGEMENT) | PM |
| 2025-01-13 | docs/index.md 업데이트 (새 문서 링크, 역할별 가이드 보강) | PM |
| 2025-01-13 | PRD 분할 (15개 파일) | PM |
| 2025-01-13 | 문서 디렉토리 구조화 및 중앙 집중화 | PM |
| 2025-01-13 | 컴포넌트 로드맵 대폭 업데이트 (675줄) | PM |
| 2025-01-13 | 아키텍처/개발 가이드 추가 (DDD_DESIGN, AGENTS) | PM |
| 2025-01-13 | 루트 README 대폭 개선 (docs/ 중앙화 반영) | PM |

---

## 📞 문의

문서 관련 문의사항:
- **Issues**: GitHub Issues
- **Email**: inspect-hub-team@example.com
- **Slack**: #inspect-hub-docs
