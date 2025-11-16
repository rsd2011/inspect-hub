# Architecture Documentation

> **Inspect-Hub 시스템 아키텍처 문서 센터**

이 디렉토리는 Inspect-Hub AML 통합 컴플라이언스 모니터링 시스템의 전체 아키텍처 문서를 포함합니다.

---

## 📚 문서 구조

### 1. [📊 시스템 개요 (OVERVIEW.md)](./OVERVIEW.md)

전체 시스템 아키텍처 개요

**주요 내용:**
- 시스템 구성도 (Frontend + Backend + Infrastructure)
- 기술 스택 (Java 21, Spring Boot, Nuxt 4, PostgreSQL, Redis, Kafka)
- 모듈 구조 (멀티모듈 설계)
- 성능 목표 (10M TPS, 99.9% SLA)
- 배포 전략 (Docker, Kubernetes, Blue-Green)

**읽어야 할 사람:**
- ⭐ 프로젝트 매니저
- ⭐ 시스템 아키텍트
- ⭐ 신규 개발자 (온보딩 필수)

---

### 2. [🏗️ DDD 설계 (DDD_DESIGN.md)](./DDD_DESIGN.md)

Domain-Driven Design 레이어 구조

**주요 내용:**
- DDD 4-Layer 아키텍처
  - Layer 1: Domain (도메인 엔티티, 비즈니스 규칙)
  - Layer 2: Application (유즈케이스, 서비스)
  - Layer 3: Infrastructure (DB, 외부 시스템)
  - Layer 4: Interface (REST API, UI)
- 레이어 간 의존성 규칙
- 테스트 전략 (TDD)

**읽어야 할 사람:**
- ⭐ 백엔드 개발자
- ⭐ 아키텍트

**관련 문서:**
- [Development Layers](../development/layers/index.md) - 상세 구현 가이드

---

### 3. [💾 데이터베이스 설계 (DATABASE.md)](./DATABASE.md)

데이터베이스 스키마 및 설계 전략

**주요 내용:**
- ERD (Entity Relationship Diagram)
- 핵심 테이블 스키마
  - 정책/기준 관리 (스냅샷 버전 관리)
  - 탐지/사건 관리
  - 사용자/권한 관리
  - 감사 로그
- 인덱스 전략
- 파티셔닝 전략 (날짜별, 해시 파티션)
- 백업/복구 전략

**읽어야 할 사람:**
- ⭐ 백엔드 개발자
- ⭐ DB 관리자 (DBA)

**관련 문서:**
- [PRD - 데이터 구조](../prd/06-data-structure.md)

---

### 4. [🔒 보안 아키텍처 (SECURITY.md)](./SECURITY.md)

보안 요구사항 및 구현 방안

**주요 내용:**
- **인증 (Authentication)** - JWT, AD, SSO, 일반 로그인
- **인가 (Authorization)** - RBAC (역할 기반 접근 제어)
- **데이터 암호화** - TLS (전송 중), AES-256 (저장 시)
- **감사 로깅** - 100% 커버리지, 무결성 보장
- **보안 취약점 대응** - SQL Injection, XSS, CSRF
- **컴플라이언스** - 금융권 보안 규정 준수

**읽어야 할 사람:**
- ⭐ 보안 담당자
- ⭐ 백엔드 개발자
- ⭐ 아키텍트

**관련 문서:**
- [API - Authentication](../api/AUTHENTICATION.md)
- [Development - Authentication](../development/cross-cutting/authentication.md)
- [Development - JWT](../development/cross-cutting/jwt.md)

---

## 🎯 빠른 탐색

### 역할별 필수 문서

**프로젝트 매니저:**
- [시스템 개요 (OVERVIEW.md)](./OVERVIEW.md)

**시스템 아키텍트:**
- [시스템 개요 (OVERVIEW.md)](./OVERVIEW.md)
- [DDD 설계 (DDD_DESIGN.md)](./DDD_DESIGN.md)
- [보안 아키텍처 (SECURITY.md)](./SECURITY.md)
- [데이터베이스 설계 (DATABASE.md)](./DATABASE.md)

**백엔드 개발자:**
- [DDD 설계 (DDD_DESIGN.md)](./DDD_DESIGN.md)
- [데이터베이스 설계 (DATABASE.md)](./DATABASE.md)
- [보안 아키텍처 (SECURITY.md)](./SECURITY.md)

**프론트엔드 개발자:**
- [시스템 개요 (OVERVIEW.md)](./OVERVIEW.md)
- [보안 아키텍처 (SECURITY.md)](./SECURITY.md) - 인증/인가 부분

**DBA (DB 관리자):**
- [데이터베이스 설계 (DATABASE.md)](./DATABASE.md)

**보안 담당자:**
- [보안 아키텍처 (SECURITY.md)](./SECURITY.md)

### 주제별 문서

**시스템 구조 이해:**
1. [시스템 개요 (OVERVIEW.md)](./OVERVIEW.md) ← 시작점
2. [DDD 설계 (DDD_DESIGN.md)](./DDD_DESIGN.md)

**데이터 설계:**
1. [데이터베이스 설계 (DATABASE.md)](./DATABASE.md)
2. [PRD - 데이터 구조](../prd/06-data-structure.md)

**보안 구현:**
1. [보안 아키텍처 (SECURITY.md)](./SECURITY.md)
2. [API - Authentication](../api/AUTHENTICATION.md)
3. [Development - Authentication](../development/cross-cutting/authentication.md)

---

## 🔗 관련 문서

### 상위 문서
- [Documentation Center](../index.md) - 중앙 문서 센터

### 관련 영역
- [API Documentation](../api/index.md) - API 설계 및 명세
- [Backend Documentation](../backend/README.md) - 백엔드 개발 가이드
- [Development Guide](../development/index.md) - 개발 방법론 및 워크플로우
- [PRD](../prd/index.md) - 제품 요구사항 정의

---

## 📝 문서 작성 규칙

### 아키텍처 문서 작성 시 포함할 내용

1. **개요** - 해당 영역의 목적 및 범위
2. **기술 스택** - 사용 기술 및 선택 이유
3. **설계 원칙** - 핵심 설계 원칙 및 제약사항
4. **구조도** - 다이어그램 (아키텍처, ERD 등)
5. **상세 설계** - 구체적인 구현 방안
6. **참고 자료** - 관련 문서 및 외부 링크

### 다이어그램 포맷

- **Mermaid** (Markdown 내장) 권장
- **PlantUML** 허용
- **이미지** (PNG, SVG) 허용 (public/images/architecture/ 저장)

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-15 | Architecture index.md 생성 (Progressive Disclosure 적용) | PM |

---

## 📞 문의

아키텍처 관련 문의:
- **GitHub Issues**: [프로젝트 이슈](https://github.com/inspect-hub/issues)
- **Slack**: #inspect-hub-architecture
