# Admin Module

시스템 관리 모듈 - 사용자/조직/권한/감사

## 주요 기능

### 1. 사용자 관리 (`user`)
- 사용자 CRUD
- 권한그룹 할당
- 로그인 이력

### 2. 조직 관리 (`org`)
- 조직 계층 구조
- 조직정책(Organization Policy)
- 기본 권한그룹 설정

### 3. 권한 관리 (`permission`)
- 권한그룹(Permission Group)
- 메뉴 권한
- 기능별 권한 (Feature-Action)

### 4. 메뉴 관리 (`menu`)
- 메뉴 트리 구조
- 동적 메뉴 관리
- 권한 요구조건 매핑

### 5. 결재선 관리 (`approval`)
- 결재선 템플릿
- 권한그룹 기반 단계 정의
- Fallback 정책

### 6. 데이터 정책 (`datapolicy`)
- Row-Level 접근 제어
- Field-Level 마스킹
- 권한그룹별 정책

### 7. 감사 로그 (`audit`)
- 100% 감사 추적
- Before/After 상태
- 접근 로그

### 8. 알람/SMTP (`notification`)
- SMTP 설정 관리
- 이메일 발송
- 알람 시스템

## 패키지 구조

```
admin/
├── user/             # 사용자 관리
├── org/              # 조직 관리
├── permission/       # 권한 관리
├── menu/             # 메뉴 관리
├── approval/         # 결재선 관리
├── datapolicy/       # 데이터 정책
├── audit/            # 감사 로그
├── notification/     # 알람/SMTP
└── config/           # 모듈 설정
```

## 의존성

- `backend:common` - 공통 기능
- `spring-boot-starter-mail` - SMTP
