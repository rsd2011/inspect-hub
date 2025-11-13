# AML 통합 준법감시 시스템 - PRD (Product Requirements Document)

> **프로젝트명**: Inspect-Hub
> **버전**: 1.0
> **최종 업데이트**: 2025-01-13
> **상태**: Planning/POC Phase

---

## 📚 문서 구조

본 PRD는 관리 편의성을 위해 논리적 섹션별로 분할되어 있습니다. 각 문서는 독립적으로 읽을 수 있도록 작성되었으며, 필요 시 관련 섹션 간 상호 참조를 포함합니다.

---

## 📑 목차

### [00. 프로젝트 개요](./00-overview.md)

- 사용자 유형(롤) 정의
- 핵심 아키텍처 원칙
- 프로젝트 개요 및 핵심 KPI

### [01. 배경 및 문제 정의](./01-background-and-problems.md)

- 현 문제/비효율
- 프로젝트 추진 배경
- 기존 시스템/프로세스
- 범위(Scope)

### [02. 목표 및 비전](./02-objectives-and-goals.md)

- 주요 목표(3)
- 상위 목표 연계
- 성공 판단 지표 (정량/정성)

### [03-1. 기능 요구사항 - 정책·기준 관리](./03-1-policy-management.md)

- KYC 위험평가 기준 관리
- 상품위험 – 신상품 설문 연계(SMTP)
- WLF(요주의인물) 임계치·알고리즘·카테고리 관리
  - 개인(Individual) 매칭 정책
  - 법인/단체(Organization) 매칭 정책
  - 정책 논리 및 DSL
- FIU/STR/RBA 기준 관리

### [03-2. 기능 요구사항 - 리스크 분석](./03-2-risk-simulation.md)

- 리스크 분석(시뮬레이션) 모듈
- 대상 범위 (KYC/STR/CTR/WLF)
- 기능 세부 및 활용 예시

### [03-3. 기능 요구사항 - 탐지·보고·점검](./03-3-detection-reporting.md)

- 탐지 모듈 (STR/CTR/WLF)
- 보고 모듈 (FIU 템플릿)
- 점검 수행 모듈 (배치 구조)
- 핵심 플로우

### [03-4. 기능 요구사항 - 운영 및 관리](./03-4-operations.md)

- 사용자·조직·조직정책·권한그룹·결재선 관리
- 메뉴 관리
- 룰/스냅샷 배포 관리
- 로그/감사 관리
- SMTP 발송 설정 관리
- 우선순위 (MVP vs 차순위)

### [04. 비기능 요구사항](./04-non-functional-requirements.md)

- 성능/용량 목표
- 보안 요구사항
- 가용성/확장성/유지보수성
- 규제/감사 준수
- 지원 환경 (브라우저, OS, 런타임)
- Backend 기술 스택 (JDK 21, Spring Boot 3.3.2, MyBatis, Swagger)
- Frontend 기술 스택 (Nuxt, Vue, RealGrid, PrimeVue 등)

### [05. 시스템 연계](./05-system-integration.md)

- 연계 대상
- 교환 방식 (실시간/배치/대외 보고)
- 인증/보안 토큰

### [06. 데이터 구조/모델](./06-data-structure.md)

- 스냅샷 기반 기준 마스터
- 탐지/조사/보고 테이블
- 점검 배치 구조 (Spring Batch 유사)
- 이력/감사 테이블
- 식별자/키 전략

### [07. 사용자 및 권한](./07-users-and-roles.md)

- 역할별 접근 기능
- SoD(분리의 원칙)

### [08. 일정 및 우선순위](./08-timeline-and-priority.md)

- 프로젝트 일정 (2025-10 ~ 2026-01)
- 우선순위 규칙
- MVP 범위

### [09. 품질보증 및 검증](./09-qa-and-validation.md)

- 테스트 전략
- 검증 주체
- 수용 기준

### [10. 이해관계자](./10-stakeholders.md)

- 역할별 담당자
- 커뮤니케이션 도구

### [11. 기타](./11-misc.md)

- 법·정책·규제 요건
- 리스크/제약
- 참고 산출물

---

## 🔗 관련 문서

- **API 계약**: [/API-CONTRACT.md](../../API-CONTRACT.md)
- **Frontend 가이드**: [/frontend/README.md](../../frontend/README.md)
- **Frontend 에이전트**: [/frontend/AGENTS.md](../../frontend/AGENTS.md)
- **Backend 에이전트**: [/backend/AGENTS.md](../../backend/AGENTS.md)
- **컴포넌트 로드맵**: [/frontend/COMPONENTS_ROADMAP.md](../../frontend/COMPONENTS_ROADMAP.md)
- **프로젝트 루트 CLAUDE.md**: [/CLAUDE.md](../../CLAUDE.md)

---

## 📝 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2025-01-13 | PRD 초안 작성 (486줄) | PM |
| 1.1 | 2025-01-13 | PRD 섹션별 분할 (15개 파일) | PM |

---

## 💡 읽기 가이드

1. **처음 읽는 경우**: [00-overview.md](./00-overview.md) → [01-background-and-problems.md](./01-background-and-problems.md) → [02-objectives-and-goals.md](./02-objectives-and-goals.md) 순서로 읽으시면 프로젝트 전체 맥락을 이해하실 수 있습니다.

2. **기능 상세 확인**: [03-1 ~ 03-4](./03-1-policy-management.md) 섹션에서 구체적인 기능 요구사항을 확인하세요.

3. **기술 스택 이해**: [04-non-functional-requirements.md](./04-non-functional-requirements.md)에서 기술 선택 사유와 환경 설정을 확인하세요.

4. **개발 착수 준비**: [08-timeline-and-priority.md](./08-timeline-and-priority.md)에서 MVP 범위와 우선순위를 확인하세요.

---

## ⚠️ 중요 원칙

본 시스템은 다음 핵심 원칙을 준수해야 합니다:

1. **스냅샷 기반 버전 관리**: 모든 기준정보는 스냅샷 구조로 관리하며 점검 수행은 스프링 배치 테이블 구조와 유사하게 구현
2. **분리의 원칙(SoD)**: Analyst와 Approver는 동일 케이스에서 겸직 금지
3. **감사추적 100%**: 모든 접근·변경 로그 완전성 보장
4. **규제 준수 최우선**: 금융정보분석원 및 특금법 관련 요구사항 완전 준수

---

**문서 작성**: John (Product Manager)
**문의**: inspect-hub-team@example.com
