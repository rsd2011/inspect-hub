# Inspect-Hub

AML (Anti-Money Laundering) 통합 준법감시 시스템

## 프로젝트 개요

금융기관을 위한 ~~멀티 테넌트 기반~~ 자금세탁방지 시스템입니다.

**주요 기능:**
- STR (의심거래보고)
- CTR (고액현금거래보고)
- WLF (감시대상명단)
- 준법감시 워크플로

## 기술 스택

### Backend
- Java 21
- Spring Boot 3.3.2
- Spring Security
- MyBatis 3.0.3
- PostgreSQL / H2 (dev)
- Redis
- Kafka

### Frontend
- Nuxt 3 (SPA mode)
- Vue 3 Composition API
- PrimeVue + RealGrid
- Tailwind CSS (with prefix)
- Pinia (State Management)
- VeeValidate + Zod
- Apache ECharts
- i18n

## 프로젝트 구조

```
inspect-hub/
├── backend/                        # Spring Boot 멀티 모듈
│   ├── common/                     # 공통 모듈 (Entity, DTO, Utils, Exception)
│   ├── policy/                     # 정책·기준 관리 (KYC/STR/CTR/WLF, 스냅샷)
│   ├── detection/                  # 탐지 엔진 (STR/CTR/WLF)
│   ├── investigation/              # 조사·케이스 관리 (워크플로, 결재)
│   ├── reporting/                  # 보고서 생성 (STR/CTR, FIU 제출)
│   ├── batch/                      # Spring Batch (점검 수행, 스케줄러)
│   ├── simulation/                 # 리스크 분석 (What-if 시뮬레이션)
│   ├── admin/                      # 시스템 관리 (사용자/조직/권한/감사)
│   └── server/                     # API Gateway (Main Application)
├── frontend/                       # Nuxt 3 애플리케이션 (SPA)
│   ├── pages/                      # 페이지 컴포넌트
│   ├── components/                 # 재사용 컴포넌트
│   ├── composables/                # Composition API
│   ├── stores/                     # Pinia 스토어
│   ├── plugins/                    # Nuxt 플러그인
│   ├── locales/                    # i18n 번역 파일
│   └── nuxt.config.ts              # Nuxt 설정
├── gradle/
├── build.gradle                    # 루트 빌드 설정
├── settings.gradle                 # 멀티 모듈 설정
└── PRD.md                          # 상세 요구사항 (Korean)
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

## 시작하기

### 사전 요구사항

- Java 21 이상
- Node.js 20.18+ (frontend 빌드용, Gradle이 자동 다운로드)
- (선택) PostgreSQL 15+
- (선택) Redis 7+

### 빌드 및 실행

#### Backend + Frontend 통합 빌드

```bash
# 전체 빌드 (backend + frontend)
./gradlew buildAll

# 또는 개별 빌드
./gradlew :backend:server:build
./gradlew :frontend:build
```

#### Backend 실행

```bash
# 서버 실행 (H2 in-memory DB 사용)
./gradlew :backend:server:bootRun

# 특정 프로파일로 실행
./gradlew :backend:server:bootRun --args='--spring.profiles.active=dev'
```

#### Frontend 개발 서버 실행

```bash
# Frontend 개발 서버 (Gradle 사용)
./gradlew :frontend:npmDev

# 또는 직접 실행
cd frontend
npm install
npm run dev
```

### 접속 정보

- **API 서버**: http://localhost:8090/api
- **Frontend (개발)**: http://localhost:3000
- **Health Check**: http://localhost:8090/api/health
- **H2 Console**: http://localhost:8090/api/h2-console
  - JDBC URL: `jdbc:h2:mem:inspecthub`
  - Username: `sa`
  - Password: (비어있음)
- **Actuator**: http://localhost:8090/api/actuator

### 테스트

```bash
# Backend 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :backend:server:test

# Frontend 테스트
./gradlew :frontend:npmTest
# 또는
cd frontend && npm run test
```

## ~~멀티 테넌시~~

~~이 시스템은 멀티 테넌트 아키텍처를 사용합니다. 모든 API 요청에 테넌트 ID를 포함해야 합니다.~~

### ~~HTTP 헤더~~

~~```
X-Tenant-ID: YOUR_TENANT_ID
```~~

### ~~예시~~

~~```bash
curl -H "X-Tenant-ID: DEFAULT" http://localhost:8080/api/health
```~~

## 개발 가이드

자세한 개발 가이드는 [CLAUDE.md](CLAUDE.md)를 참조하세요.

### 주요 아키텍처 원칙

1. ~~**멀티 테넌시**: 모든 데이터는 테넌트별로 격리~~
2. **스냅샷 버저닝**: 정책/기준은 불변 스냅샷으로 관리
3. **감사 로깅**: 100% 감사 추적 가능
4. **Separation of Duties (SoD)**: Maker-Checker 원칙
5. **SPA 프론트엔드**: Nuxt 3 SPA 모드로 정적 리소스 배포

### 코드 규칙

- **패키지 구조**: `com.inspecthub.aml.{module}.{layer}`
- **명명 규칙**:
  - Java: camelCase
  - Database: snake_case
- **API 버전**: `/api/v1/...`

## 라이선스

Proprietary - 내부 사용만 가능

## 상태

현재 상태: **POC/MVP 개발 단계**

MVP Go-Live 목표: 2026년 1월 20일
