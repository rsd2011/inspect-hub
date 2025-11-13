# Inspect-Hub

> **AML (Anti-Money Laundering) 통합 준법감시 시스템**

금융기관을 위한 자금세탁방지 종합 컴플라이언스 모니터링 플랫폼입니다.

## 📚 프로젝트 문서

**상세 문서는 [`/docs`](./docs/index.md) 디렉토리를 참조하세요.**

- 📋 **[PRD (제품 요구사항)](./docs/prd/index.md)** - 기능 명세, 아키텍처 요구사항
- 🎨 **[Frontend 가이드](./docs/frontend/README.md)** - Nuxt 3, 컴포넌트, 아키텍처
- ⚙️ **[Backend 가이드](./docs/backend/AGENTS.md)** - Spring Boot, API 개발
- 🔗 **[API 계약](./docs/api/CONTRACT.md)** - Frontend ↔ Backend 통신 규약
- 🏗️ **[아키텍처](./docs/architecture/)** - 시스템 설계, DDD
- 🛠️ **[개발 가이드](./docs/development/AGENTS.md)** - 코딩 규칙, 테스트, 빌드

## 🚀 주요 기능

- **STR (의심거래보고)** - 자동 탐지 및 조사 워크플로
- **CTR (고액현금거래보고)** - 고액 현금 거래 모니터링
- **WLF (감시대상명단)** - Watch-list 필터링 및 매칭
- **정책 관리** - 스냅샷 기반 버전 관리 (KYC, STR, CTR, WLF)
- **리스크 분석** - What-if 시뮬레이션
- **준법 워크플로** - Maker-Checker, 결재선, 감사 로깅

## 🏗️ 기술 스택

### Backend
- **Java 21** + **Spring Boot 3.3.2**
- **Spring Security** (JWT, OAuth2)
- **MyBatis** 3.0.3 (SQL Mapper)
- **PostgreSQL** (운영) / **H2** (개발)
- **Redis** (캐싱) + **Kafka** (메시징)
- **Spring Batch** (배치 처리)

### Frontend
- **Nuxt 3** (SPA 모드 전용 - SSR 금지)
- **Vue 3** Composition API
- **PrimeVue** + **RealGrid2** (상용 그리드)
- **Tailwind CSS** (prefix: `tw-`)
- **Pinia** (상태 관리)
- **VeeValidate** + **Zod** (폼 검증)
- **Apache ECharts** (차트)
- **@nuxtjs/i18n** (다국어)

### Architecture
- **FSD (Feature-Sliced Design)** + **Atomic Design** (프론트엔드)
- **Snapshot-based Versioning** (정책 관리)
- **Audit Logging** (100% 추적)
- **RBAC + SoD** (역할 기반 권한 + 직무 분리)

## 📂 프로젝트 구조

```
inspect-hub/
├── docs/                           # 📚 중앙 문서 디렉토리
│   ├── index.md                    # 문서 센터 (시작점)
│   ├── prd/                        # 제품 요구사항 (15개 파일)
│   ├── frontend/                   # 프론트엔드 가이드
│   ├── backend/                    # 백엔드 가이드
│   ├── api/                        # API 계약
│   ├── architecture/               # 아키텍처 설계
│   └── development/                # 개발 규칙
│
├── backend/                        # Spring Boot 멀티 모듈
│   ├── common/                     # 공통 (Entity, DTO, Utils)
│   ├── policy/                     # 정책 관리 (스냅샷)
│   ├── detection/                  # 탐지 엔진 (STR/CTR/WLF)
│   ├── investigation/              # 조사/케이스 관리
│   ├── reporting/                  # 보고서 생성 (FIU 제출)
│   ├── batch/                      # Spring Batch
│   ├── simulation/                 # 리스크 시뮬레이션
│   ├── admin/                      # 시스템 관리
│   └── server/                     # Main Application
│
├── frontend/                       # Nuxt 3 애플리케이션
│   ├── app/                        # App layer (layouts, providers)
│   ├── pages/                      # Pages (Nuxt routing)
│   ├── widgets/                    # Widgets (large blocks)
│   ├── features/                   # Features (user scenarios)
│   ├── entities/                   # Entities (business models)
│   └── shared/                     # Shared (UI, API, utils)
│       └── ui/
│           ├── atoms/              # 기본 요소 (Button, Input)
│           ├── molecules/          # 조합 (FormField, SearchBox)
│           └── organisms/          # 복합 (DataTable, Modal)
│
├── CLAUDE.md                       # 🤖 AI Assistant 가이드
└── README.md                       # 이 파일
```

### Backend 모듈 의존성

```
server
  ├─→ common
  ├─→ policy    → common
  ├─→ detection → common, policy
  ├─→ investigation → common, detection
  ├─→ reporting → common, investigation
  ├─→ batch     → common, detection, investigation
  ├─→ simulation → common, policy, detection
  └─→ admin     → common
```

## 🏁 빠른 시작

### 사전 요구사항

- **Java 21+**
- **Node.js 20.18+** (Gradle이 자동 다운로드)
- **PostgreSQL 15+** (선택)
- **Redis 7+** (선택)

### 전체 빌드

```bash
# Backend + Frontend 통합 빌드
./gradlew buildAll

# 개별 빌드
./gradlew :backend:server:build
./gradlew :frontend:build
```

### Backend 실행

```bash
# H2 in-memory DB로 실행
./gradlew :backend:server:bootRun

# 특정 프로파일로 실행
./gradlew :backend:server:bootRun --args='--spring.profiles.active=dev'
```

### Frontend 개발 서버

```bash
# Gradle 사용
./gradlew :frontend:npmDev

# 또는 직접 실행
cd frontend
npm install
npm run dev
```

### 접속 정보

| 서비스 | URL | 비고 |
|--------|-----|------|
| **API 서버** | http://localhost:8090/api | RESTful API |
| **Frontend** | http://localhost:3000 | 개발 서버 |
| **Health Check** | http://localhost:8090/api/health | 상태 확인 |
| **H2 Console** | http://localhost:8090/api/h2-console | JDBC URL: `jdbc:h2:mem:inspecthub`<br>Username: `sa`, Password: (비어있음) |
| **Actuator** | http://localhost:8090/api/actuator | 모니터링 |

### 테스트

```bash
# Backend 테스트
./gradlew test
./gradlew :backend:server:test

# Frontend 테스트
./gradlew :frontend:npmTest
# 또는
cd frontend && npm run test
```

## 🧭 문서 탐색 가이드

### 역할별 추천 문서

**프로젝트 매니저:**
- [프로젝트 개요](./docs/prd/00-overview.md) - 사용자 롤, 핵심 KPI
- [일정 및 우선순위](./docs/prd/08-timeline-and-priority.md)
- [이해관계자](./docs/prd/10-stakeholders.md)

**프론트엔드 개발자:**
- [Frontend README](./docs/frontend/README.md) - **필독** (SSR 금지, 아키텍처)
- [컴포넌트 로드맵](./docs/frontend/COMPONENTS_ROADMAP.md) - **필독** (675줄)
- [API 계약](./docs/api/CONTRACT.md)

**백엔드 개발자:**
- [Backend AGENTS](./docs/backend/AGENTS.md) - API Generator, 개발 규칙
- [ULID 가이드](./docs/backend/ULID.md)
- [API 계약](./docs/api/CONTRACT.md)
- [DDD 설계](./docs/architecture/DDD_DESIGN.md)

**QA/테스터:**
- [품질보증](./docs/prd/09-qa-and-validation.md)
- [비기능 요구사항](./docs/prd/04-non-functional-requirements.md)

**아키텍트:**
- [아키텍처 문서](./docs/architecture/)
- [시스템 연계](./docs/prd/05-system-integration.md)
- [데이터 구조](./docs/prd/06-data-structure.md)

### 신규 개발자 온보딩 순서

1. **[이 README](./README.md)** - 프로젝트 개요 및 빠른 시작
2. **[CLAUDE.md](./CLAUDE.md)** - AI 가이드 (기술 스택, 제약사항)
3. **[문서 센터](./docs/index.md)** - 전체 문서 구조
4. **[PRD 개요](./docs/prd/00-overview.md)** - 비즈니스 요구사항
5. **역할별 가이드** (위 참조)

## 🏛️ 핵심 아키텍처 원칙

1. **스냅샷 버저닝**: 정책/기준은 불변 스냅샷으로 관리 (Spring Batch 패턴)
2. **감사 로깅**: 100% 감사 추적 가능 (`AUDIT_LOG` 테이블)
3. **Separation of Duties (SoD)**: Maker-Checker 원칙 (동일 사용자 생성+승인 불가)
4. **SPA 프론트엔드**: Nuxt 3 SPA 모드 전용 (SSR 절대 금지, 정적 리소스 배포)
5. **RBAC**: 역할 기반 권한 관리 + 메뉴/기능 권한

## 🔒 보안 요구사항

- **암호화**: TLS (전송 중), 필드 레벨 암호화 (저장 시)
- **인증**: Spring Security + JWT/OAuth2
- **권한**: RBAC + SoD 강제
- **데이터 마스킹**: 역할별 설정 가능 (`DATA_POLICY` 테이블)

## 📊 성능 목표

- **배치 처리**: ≥1000만 건/일 (야간 윈도우)
- **실시간 API**: 300-500 TPS (피크)
- **UI 응답**: <1초 (조회), <3초 (복잡 검색)
- **가용성**: 99.9% SLA

## 📝 개발 가이드

자세한 개발 가이드는 다음 문서를 참조하세요:

- **[CLAUDE.md](./CLAUDE.md)** - AI Assistant 전용 가이드 (기술 스택, 제약사항, 아키텍처)
- **[개발 규칙](./docs/development/AGENTS.md)** - 코딩 스타일, 테스트, 커밋 규칙
- **[Frontend 가이드](./docs/frontend/README.md)** - SPA 제약사항, FSD, Atomic Design
- **[Backend 가이드](./docs/backend/AGENTS.md)** - Spring Boot, MyBatis, 모듈 구조

### 코드 규칙

- **패키지 구조**: `com.inspecthub.aml.{module}.{layer}`
- **명명 규칙**:
  - Java/TypeScript: `camelCase`
  - Database: `snake_case`
  - React/Vue 컴포넌트: `PascalCase`
- **API 버전**: `/api/v1/**`
- **Tailwind CSS**: `tw-` prefix (PrimeVue/RealGrid 충돌 방지)

## 📅 프로젝트 상태

- **현재 단계**: POC/MVP 개발 단계
- **MVP Go-Live 목표**: 2026년 1월 20일
- **현재 Sprint**: Sprint 3-4 (Core UI Components)

## 📄 라이선스

Proprietary - 내부 사용만 가능

---

## 📞 문의

문서 관련 문의사항:
- **Issues**: GitHub Issues
- **Email**: inspect-hub-team@example.com

**상세 문서는 [`/docs`](./docs/index.md) 디렉토리를 참조하세요.**
