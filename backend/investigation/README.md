# Investigation Module

조사 및 케이스 관리 모듈 - 워크플로 및 결재

## 주요 기능

### 1. 케이스 관리 (`case`)
- 탐지 이벤트 → 케이스 생성
- 케이스 상태 관리
- 케이스 활동(Case Activity) 로깅

### 2. 워크플로 (`workflow`)
- 결재선 기반 워크플로
- 단계별 상태 전이
- 병렬/순차 결재 지원

### 3. 증빙 관리 (`evidence`)
- 파일 첨부
- 증빙 문서 버전 관리

### 4. 결재 (`approval`)
- Maker-Checker 원칙
- 권한그룹 기반 결재자 선정
- Self-approval 방지

## 패키지 구조

```
investigation/
├── case/             # 케이스 관리
├── workflow/         # 워크플로
├── evidence/         # 증빙 관리
├── approval/         # 결재
└── config/           # 모듈 설정
```

## 의존성

- `backend:common` - 공통 기능
- `backend:detection` - 탐지 이벤트 참조
