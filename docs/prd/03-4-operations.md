# 🧱 기능 요구사항 - 운영 및 관리

## 7. 운영 및 관리

### 사용자·조직·조직정책·권한그룹·결재선 관리

#### 사용자 및 조직 구조

- **사용자(User)** 는 반드시 **하나의 조직**과 **하나의 권한그룹(Permission Group)** 에 속함.
- **조직** 은 **조직정책(Organization Policy)** 을 참조하고, 조직정책은
    1. **신규/이동 시 부여될 기본 권한그룹 세트**(초기값)와
    2. **업무별 결재선 템플릿**을 정의함.

#### 권한그룹

- **권한그룹** 은 **메뉴별 권한, 기능별 권한** 및 **역할(Role) 매핑을 포함**하며 **사용자에 직접 연결**됨.

**메뉴 권한**
- 메뉴에 대한 접근 가능 여부 관리

**기능별 권한**

```sql
FEATURE(feature_code pk, name, desc, owner_team, is_active)
FEATURE_ACTION(feature_code fk, action_code)  -- 허용 액션 카탈로그

PERM_GROUP_FEATURE(feature_code fk, perm_group_code, action_code)
-- 권한그룹이 어떤 기능-액션을 가질 수 있는지

MENU(id pk, parent_id, title, route_name, route_params_json, external_url, visible, sort_order, icon)
```

#### 데이터 정책 관리

```sql
-- 데이터 정책 헤더(행/기본 마스킹)
DATA_POLICY(
  policy_id pk,
  feature_code,               -- 어떤 기능에서
  action_code null,           -- (선택) 특정 액션일 때만
  perm_group_code null,       -- (선택) 특정 권한그룹일 때만
  org_policy_id null,         -- (선택) 특정 '조직정책'일 때만
  business_type varchar null, -- (선택) 업무유형별(결재선과 동일한 키)
  row_scope varchar not null, -- OWN | ORG | ALL | CUSTOM
  default_mask_rule varchar not null, -- NONE | PARTIAL | FULL | HASH
  priority int not null,      -- 우선순위(낮을수록 먼저)
  active boolean not null
);
```

#### 결재선(Approval Line)

- **결재선(Approval Line)** 은 **업무별 템플릿**을 사용하며, **각 결재 단계는 '권한그룹'으로 지정**됨.
- 예: `STEP1=PG.REVIEWER`, `STEP2=PG.APPROVER`, `STEP3=PG.HEAD_COMPLIANCE`

**결재 대상 사용자 선택 규칙(권장)**
- **기본**: 요청자의 **동일 조직** 내 해당 권한그룹 소속자 중 1인(라운드로빈/가중치 선택 정책 지원)
- **병렬 단계**: same-step 안에 다수 권한그룹 지정 가능(ANY/ALL 모드)
- **부재/결원**: 위임자(or 상위 조직 경로) → 정책별 **fallback**
- **충돌 방지**: 요청자와 동일 권한그룹일 경우 **self-approval 금지** 옵션

### 메뉴 관리 (Menu Management)

- 페이지가 없는 카테고리 관리 필요
- **메뉴 트리/순서/노출 여부/아이콘/외부링크**와 **권한 요구조건(필요 권한그룹, 허용 액션)** 을 **DB로 관리**.
- **메뉴는 라우트 이름(routeName)과 매핑**되어, **메뉴 가시성**은 DB가, **실제 접근 통제**는 백엔드 권한 검사로 보장.
- 변경 이력/감사(메뉴 구조, 권한 요구조건 수정) 기록.

### 룰 / 스냅샷 배포 관리

- 탐지 룰 및 시뮬레이션 스냅샷의 버전·배포·승인·롤백 관리

### 로그 / 감사 관리

- 사용자 행위, 정책 변경, 룰 배포 등 시스템 내 주요 이벤트 감사 로그 관리

### SMTP 발송 설정 관리

- 발신 도메인, SPF/DKIM, 발송 한도, 템플릿 설정 및 발송 이력 관리

### 알람 시스템

- (내용 추가 필요)

## 우선순위

### 필수(MVP)

데이터 수집/정합성, STR/CTR 기본 룰, 케이스/결재, 보고 템플릿, 사용자·역할, 감사로그, 대시보드(핵심)

### 선택(차순위)

고급 유사도 튜닝, 자동 재학습 지원(비ML), 시뮬레이션/백테스트 UI, 룰 A/B 배포, 규제 변경 알림
