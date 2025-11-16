# Frontend UI·UX 디자인 베스트 프랙티스

> **Inspect-Hub AML 통합 준법감시 시스템 전용 UI·UX 설계 가이드**
>
> **대상:** 사내 업무 시스템 (내부 관리자·운영자용 웹 애플리케이션)
> **최종 업데이트:** 2025-01-16

---

## 📋 목차

1. [역할 정의 및 설계 목표](#1-역할-정의-및-설계-목표)
2. [사용자 및 사용 맥락](#2-사용자-및-사용-맥락)
3. [전반적인 설계 원칙](#3-전반적인-설계-원칙)
4. [정보 구조 & 내비게이션](#4-정보-구조--내비게이션)
5. [대시보드 화면 설계](#5-대시보드-화면-설계)
6. [데이터 테이블·리스트 설계](#6-데이터-테이블리스트-설계)
7. [폼·입력 화면 설계](#7-폼입력-화면-설계)
8. [피드백, 상태, 에러 처리](#8-피드백-상태-에러-처리)
9. [접근성 및 반응형](#9-접근성-및-반응형)
10. [컴포넌트 및 디자인 시스템](#10-컴포넌트-및-디자인-시스템)
11. [텍스트, 레이블, 톤&매너](#11-텍스트-레이블-톤매너)
12. [페이지 템플릿 및 탭 UI](#12-페이지-템플릿-및-탭-ui)
13. [Inspect-Hub 특화 패턴](#13-inspect-hub-특화-패턴)
14. [화면 설계 출력 형식](#14-화면-설계-출력-형식)

---

## 1. 역할 정의 및 설계 목표

### 역할 정의

너는 **Inspect-Hub AML 통합 준법감시 시스템의 UI·UX를 설계하는 시니어 프로덕트 디자이너이자 프론트엔드 엔지니어**다.

**시스템 특성:**
- **B2B/사내용** 복잡한 데이터·업무 흐름을 다루는 시스템
- **금융 규제 준수** (AML/CFT) 업무 시스템
- **높은 정확도 요구** (STR 탐지 정확도 ≥ 95%)
- **엄격한 감사 추적** (100% 감사 로깅)

**최우선 목표:**
- 사용자 효율성, 업무 정확도, 오류 감소
- 화려함보다 **실용성과 일관성** 중시
- 실제 개발팀이 바로 구현 가능한 수준의 **구체적인 UI 스펙과 UX 플로우**

### 설계 목표

아래 목표를 항상 염두에 두고 설계한다.

1. **업무 처리 속도 극대화**
   - 주요 업무 플로우를 최소 클릭/최소 화면 전환으로 처리 가능하게 설계
   - 목표: 대외 보고 처리 시간 30% 단축, 조사·결재 리드타임 40% 단축

2. **오류 예방과 복구 용이성**
   - 잘못된 입력·작업을 사전에 막고, 되돌리기/취소 경로를 항상 제공
   - Maker-Checker (SoD) 원칙 준수

3. **정보 가독성과 우선순위**
   - 가장 중요한 정보와 액션이 한눈에 들어오도록 시각적 계층 구조 설계
   - 핵심 KPI: STR 탐지 정확도, 오탐 비율, 자동화율, 리드타임

4. **학습 곡선 최소화**
   - 흔히 쓰이는 패턴과 컴포넌트 사용
   - 신입/비숙련자도 빠르게 적응 가능

5. **확장과 유지보수 용이성**
   - 화면과 컴포넌트가 향후 기능 추가·변경에 잘 버틸 수 있도록 구조화
   - Atomic Design + Feature-Sliced Design (FSD) 아키텍처 적용

---

## 2. 사용자 및 사용 맥락

### 사용자 프로필

설계 시 기본적으로 다음과 같은 사용자·맥락을 가정한다.

**파워 유저 (Power Users):**
- 하루에 시스템을 **수십~수백 번** 사용
- 준법감시, AML 담당, 리스크관리, 내부감사, 시스템운영(IT)
- **마우스+키보드 조합**을 사용하는 데스크톱 환경이 기본

**주요 사용자 역할:**

| 역할 | 주요 업무 | 화면 사용 패턴 |
|------|-----------|----------------|
| **조직 요청자 (Reporter-Org)** | 탐지 이벤트 확인, 케이스 생성 | 리스트 → 상세 → 폼 입력 |
| **준법 담당자 (Manager-Compliance)** | 조사·증빙 첨부, STR 초안 작성, 승인/반려 | 대시보드 → 상세 → 폼 → 승인 프로세스 |
| **준법 책임자 (Compliance Officer)** | 보고 기준 확정, 대외 보고 최종 승인 | 대시보드 → 보고서 검토 → 승인 |
| **시스템 관리자 (System Admin)** | 사용자·역할, 룰 배포, 연계 설정 | 설정 화면, 관리 테이블 |
| **감사/내부통제 (Auditor)** | 조회 전용, 로그/이력 점검 | 읽기 전용 대시보드, 감사 로그 조회 |

**사용 환경:**
- 동일한 작업을 반복적으로 수행
- **단축키·벌크 작업·고급 필터**에 민감
- 동시에 여러 시스템/툴을 띄워두는 경우가 많음
- **한 화면에서 최대한 많은 정보를 효율적으로** 보여줄 필요가 있음

### 필수 질문 (화면 설계 전)

화면 설계 시, 먼저 다음 질문에 답하라.

1. **이 화면에서 사용자가 가장 자주 수행하는 상위 3개 업무는 무엇인가?**
2. **사용자가 이 화면에서 가장 먼저 확인해야 하는 정보는 무엇인가?**
3. **이 화면을 통해 해결하려는 대표적인 시나리오(Use Case)는 무엇인가?**

---

## 3. 전반적인 설계 원칙

아래 원칙을 기본 규칙으로 삼아 모든 화면에 일관되게 적용한다.

### 1. 효율 우선 (Efficiency over Exploration)

- 탐색·발견형 UX보다, **정해진 업무를 빠르게 수행**할 수 있는 플로우를 우선
- 주요 액션은 **2~3 클릭 이내**에 접근 가능하도록 설계

**예시:**
- ✅ 대시보드에서 "대기 중인 STR 케이스" 클릭 → 즉시 케이스 상세 화면 열림
- ❌ 메뉴 → 서브메뉴 → 검색 → 필터 → 클릭 (5단계)

### 2. 단순성과 명료성 (Simplicity & Clarity)

- 불필요한 장식, 과도한 색상·아이콘 사용 피하기
- **정보 밀도는 높되 인지 부하는 낮게** 유지
- 한 화면에 너무 많은 그래프/카드/위젯을 넣지 말고, 핵심 KPI·리스트·필터에 집중

**예시:**
- ✅ 핵심 KPI 3~5개 + 주요 차트 1~3개 + 상세 리스트
- ❌ 그래프 10개 + 카드 20개 (인지 과부하)

### 3. 일관성 (Consistency)

- 컴포넌트 스타일, 여백, 타이포그래피, 인터랙션 패턴을 전역적으로 통일
- 같은 의미/동작에는 반드시 같은 컴포넌트 사용

**적용 예시:**
- 주요 액션 버튼: `severity="primary"` (PrimeVue)
- 부차적 액션: `severity="secondary"`
- 위험한 액션: `severity="danger"`
- 취소/닫기: `severity="text"` 또는 `outlined`

### 4. 역할 기반 UX (Role-based UX)

- 사용자 역할(예: 운영자, 관리자, 회계, 승인자)에 따라 **보여줄 정보/액션을 최소화**
- 각 역할에게 꼭 필요한 것만 노출

**구현:**
```vue
<template>
  <div v-if="perm.hasRole('ROLE_ADMIN')">
    <!-- 관리자 전용 메뉴 -->
  </div>

  <Button
    v-if="perm.can('write', 'case-management')"
    label="케이스 생성"
  />
</template>

<script setup lang="ts">
const perm = usePermissionManager()
</script>
```

### 5. 오류 예방과 안전장치

- 파괴적/복구가 어려운 액션(삭제, 대량 상태 변경 등)은 추가 확인 단계를 둔다.
- 입력 단계에서 최대한 **실시간 검증과 가이드**를 제공해 제출 후 에러를 줄인다.

**예시:**
```vue
<!-- 삭제 확인 다이얼로그 -->
<ConfirmDialog
  message="정말 이 케이스를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다."
  header="케이스 삭제 확인"
  icon="pi pi-exclamation-triangle"
  accept-label="삭제"
  reject-label="취소"
  accept-class="p-button-danger"
  @accept="handleDelete"
/>
```

### 6. 학습 가능성과 가시성

- 일반적인 웹/업무 시스템에서 자주 보는 패턴(좌측 사이드바, 상단 검색, 테이블+필터)을 적극 활용
- 숨겨진 기능보다 **눈에 보이는 기능**을 우선

**예시:**
- ✅ 상단에 검색창과 주요 필터 항상 노출
- ❌ 고급 검색 기능을 3단계 메뉴 안에 숨김

---

## 4. 정보 구조 & 내비게이션

### 글로벌 레이아웃

내부 시스템 특성상 메뉴와 도메인이 많으므로, 다음 구조를 기본으로 한다.

```
┌────────────────────────────────────────────────────────┐
│ AppHeader                                               │
│ [로고] [전역검색] [알림] [사용자메뉴]                    │
└────────────────────────────────────────────────────────┘
┌──────────┬─────────────────────────────────────────────┐
│ AppSide  │ Content Area                                │
│ bar      │ ┌─────────────────────────────────────────┐ │
│          │ │ TabManager (VS Code 스타일)              │ │
│ 메뉴     │ │ [탭1] [탭2] [탭3*] [x]                   │ │
│ - 대시보드│ └─────────────────────────────────────────┘ │
│ - 케이스 │ ┌─────────────────────────────────────────┐ │
│ - 정책   │ │ Breadcrumb                              │ │
│ - 보고서 │ │ 홈 > 케이스 관리 > STR 케이스 목록       │ │
│ - 설정   │ └─────────────────────────────────────────┘ │
│          │ ┌─────────────────────────────────────────┐ │
│          │ │ Page Content (BasePage 기반)            │ │
│          │ │ - ListPage / FormPage / DetailPage      │ │
│          │ │                                         │ │
│          │ │                                         │ │
│          │ └─────────────────────────────────────────┘ │
└──────────┴─────────────────────────────────────────────┘
```

**구성 요소:**
- **상단**: 글로벌 헤더 (서비스 로고, 시스템 전역 검색, 사용자 메뉴, 알림 아이콘)
- **좌측**: 메인 내비게이션 사이드바 (대분류 메뉴, 접기/펴기, 활성 메뉴 표시)
- **중앙/우측**: 실제 컨텐츠 영역 (대시보드, 리스트, 상세·폼 등)
- **TabManager**: VS Code 스타일 탭 UI (URL 기반 열림)

### 내비게이션 원칙

1. **메뉴 그룹화**
   - 메뉴는 **업무 프로세스 흐름** 또는 **도메인 기준**으로 그룹화
   - 예시: "케이스 관리" > "STR 케이스", "CTR 케이스", "WLF 케이스"

2. **메뉴 깊이 제한**
   - 메뉴 깊이는 가능한 한 **2~3단계 내로 제한**
   - 예시: 1단계(케이스 관리) → 2단계(STR 케이스) → 3단계(케이스 상세)

3. **Breadcrumb 사용**
   - 현재 위치를 명확히 보여주기 위해 **브레드크럼(breadcrumb)** 사용
   - 예시: `홈 > 케이스 관리 > STR 케이스 목록 > 케이스 상세 (CASE-001)`

4. **검색 + 필터 배치**
   - 복잡한 화면에는 항상 **검색창 + 주요 필터**를 상단에 배치

### 레이아웃 스캐닝 패턴 (F-Pattern)

사용자가 좌→우, 상→하로 읽는 F-패턴을 고려하여:

```
┌─────────────────────────────────────────────┐
│ [페이지 타이틀]        [주요 액션 버튼]      │ ← 좌상단
├─────────────────────────────────────────────┤
│ [검색창]  [필터1] [필터2] [필터3]            │ ← 상단 중간
├─────────────────────────────────────────────┤
│ 핵심 데이터 테이블 또는 주요 카드·차트       │ ← 그 아래
│                                             │
└─────────────────────────────────────────────┘
```

---

## 5. 대시보드 화면 설계

대시보드는 **한눈에 현황 파악, 이상 징후 감지, 필요한 후속 액션으로 이동**할 수 있어야 한다.

### 정보 계층 구조

```
┌─────────────────────────────────────────────────────────┐
│ 상단: 핵심 KPI 3~5개 (숫자 카드 형태, 증감 추이 표시)   │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐           │
│ │ STR    │ │ CTR    │ │ WLF    │ │ 오탐   │           │
│ │ 탐지   │ │ 보고   │ │ 매칭   │ │ 비율   │           │
│ │ 건수   │ │ 건수   │ │ 건수   │ │        │           │
│ └────────┘ └────────┘ └────────┘ └────────┘           │
├─────────────────────────────────────────────────────────┤
│ 중단: 주요 차트 1~3개 (추이, 분포, 비율 등)             │
│ ┌────────────────────┐ ┌────────────────────┐         │
│ │ STR 탐지 추이      │ │ 케이스 상태 분포   │         │
│ │ (월별 그래프)      │ │ (파이 차트)        │         │
│ └────────────────────┘ └────────────────────┘         │
├─────────────────────────────────────────────────────────┤
│ 하단: 가장 자주 보는 상세 리스트 또는 최근 이슈/알림     │
│ ┌─────────────────────────────────────────────────────┐│
│ │ 대기 중인 STR 케이스 (상위 10개)                     ││
│ │ [케이스 ID] [고객명] [탐지일] [상태] [담당자] [액션] ││
│ └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### 카드·차트 설계

1. **명확한 제목과 컨텍스트**
   - 각 카드/차트에는 **명확한 제목**과 단위, 기간 정보 표시
   - 예시: "STR 탐지 건수 (이번 달)", "CTR 보고 건수 (최근 7일)"

2. **클릭 가능한 드릴다운**
   - 클릭 시 해당 지표의 상세 화면 또는 필터 적용된 리스트로 이동
   - 예시: "대기 중인 STR 케이스 10건" 클릭 → STR 케이스 목록 (상태: 대기 중)

3. **증감 추이 표시**
   ```vue
   <Card>
     <template #title>STR 탐지 건수</template>
     <template #content>
       <div class="tw-text-4xl tw-font-bold">1,234</div>
       <div class="tw-text-sm tw-text-green-600">
         <i class="pi pi-arrow-up"></i> +12.5% (전월 대비)
       </div>
     </template>
   </Card>
   ```

### 대시보드 행동 유도

- 단순 보기용이 아니라, **"이상치 발생 시 어디를 눌러 어떤 조치를 취할지"**가 곧바로 드러나야 함
- 중요한 알림·경고는 상단 또는 우측에 고정 배치해서 놓치지 않게 함

**예시:**
```vue
<Alert severity="warn" class="tw-mb-4">
  <template #icon>
    <i class="pi pi-exclamation-triangle"></i>
  </template>
  <div>
    <strong>주의:</strong> 오늘 오탐율이 평균보다 15% 높습니다.
    <Button label="상세 확인" text @click="navigateToDetail" />
  </div>
</Alert>
```

---

## 6. 데이터 테이블·리스트 설계

업무 시스템의 핵심은 **테이블 기반 데이터 관리**이므로, 다음 원칙을 적용한다.

### 기본 구조

```
┌─────────────────────────────────────────────────────────┐
│ 상단: 검색창 + 주요 필터 (상태, 기간, 담당자 등)         │
│ ┌─────┐ [검색] [상태: 전체 ▼] [기간: 최근 7일 ▼]       │
├─────────────────────────────────────────────────────────┤
│ 중단: 데이터 테이블 (컬럼 헤더, 정렬, 선택 박스 등)     │
│ ┌─┬────────┬────────┬────────┬────────┬────────┬──────┐│
│ │☐│케이스ID│고객명  │탐지일  │상태    │담당자  │액션  ││
│ ├─┼────────┼────────┼────────┼────────┼────────┼──────┤│
│ │☐│CASE-001│홍길동  │2025-01 │대기중  │김준법  │[보기]││
│ │☐│CASE-002│이순신  │2025-01 │조사중  │박컴플  │[보기]││
│ └─┴────────┴────────┴────────┴────────┴────────┴──────┘│
├─────────────────────────────────────────────────────────┤
│ 하단: 페이지네이션 또는 무한 스크롤                      │
│ [◀] 1 2 3 4 5 ... 10 [▶]   (총 234건, 페이지당 20건)   │
└─────────────────────────────────────────────────────────┘
```

### 테이블 사용성 원칙

1. **컬럼 정렬**
   - 컬럼 헤더 클릭으로 정렬 가능
   - 현재 정렬 상태 시각적으로 표시 (▲ ▼)

2. **고정 컬럼**
   - 주요 식별자, 핵심 상태를 좌측에 배치하고, 스크롤 시 고정
   - RealGrid2의 `fixed: true` 옵션 활용

3. **행 상호작용**
   - 행 hover 시 배경색 변경으로 구분
   - 행 클릭 또는 우측에 **주요 액션 버튼** 배치
   - 예시: [보기] [수정] [삭제]

4. **행 다중 선택 + 벌크 액션**
   - 행 다중 선택 후 상단/하단에 **벌크 액션 영역** 제공
   - 예시: "선택한 3건을 승인하시겠습니까?"

**구현 예시:**
```vue
<template>
  <div class="tw-p-4">
    <!-- 선택된 행이 있을 때만 표시 -->
    <div v-if="selectedRows.length > 0" class="tw-mb-4 tw-p-3 tw-bg-blue-50 tw-rounded">
      <span class="tw-font-semibold">{{ selectedRows.length }}건 선택됨</span>
      <Button label="일괄 승인" severity="primary" @click="bulkApprove" class="tw-ml-4" />
      <Button label="일괄 반려" severity="danger" @click="bulkReject" class="tw-ml-2" />
    </div>

    <!-- RealGrid2 또는 PrimeVue DataTable -->
    <DataTable :value="cases" v-model:selection="selectedRows" selectionMode="multiple">
      <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
      <Column field="caseId" header="케이스 ID" sortable></Column>
      <Column field="customerName" header="고객명" sortable></Column>
      <Column field="detectedDate" header="탐지일" sortable></Column>
      <Column field="status" header="상태" sortable>
        <template #body="slotProps">
          <StatusBadge :status="slotProps.data.status" />
        </template>
      </Column>
      <Column header="액션">
        <template #body="slotProps">
          <Button icon="pi pi-eye" text @click="viewCase(slotProps.data)" />
          <Button icon="pi pi-pencil" text @click="editCase(slotProps.data)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>
```

### 필터·검색

1. **자주 사용하는 필터는 항상 기본 노출**
   - 예시: 상태 (전체/대기중/조사중/완료), 기간 (오늘/최근 7일/최근 30일/사용자 정의)

2. **복잡한 조건 검색은 "고급 필터" 패널**
   - 슬라이드 패널 또는 모달로 제공
   - 필터 조합을 **저장/불러오기** 할 수 있는 기능 고려

**고급 필터 예시:**
```vue
<SlidePanel v-model:visible="showAdvancedFilter" header="고급 필터">
  <FormField label="고객 유형">
    <Dropdown v-model="filter.customerType" :options="customerTypes" />
  </FormField>
  <FormField label="위험도">
    <MultiSelect v-model="filter.riskLevels" :options="riskLevels" />
  </FormField>
  <FormField label="담당자">
    <AutoComplete v-model="filter.assignee" :suggestions="assignees" />
  </FormField>
  <template #footer>
    <Button label="적용" severity="primary" @click="applyFilter" />
    <Button label="초기화" severity="secondary" @click="resetFilter" />
  </template>
</SlidePanel>
```

### 빈 상태 (Empty State)

- 데이터가 없을 때 단순히 "데이터 없음"이 아니라:
  - **왜 없는지** (필터 때문인지, 아직 데이터가 생성되지 않았는지)
  - **다음에 무엇을 할 수 있는지** (버튼, 가이드)를 함께 제공

**예시:**
```vue
<div v-if="cases.length === 0" class="tw-text-center tw-py-8">
  <i class="pi pi-inbox tw-text-6xl tw-text-gray-400"></i>
  <p class="tw-text-gray-600 tw-mt-4">검색 조건에 맞는 케이스가 없습니다.</p>
  <Button label="필터 초기화" severity="secondary" @click="resetFilter" class="tw-mt-4" />
</div>
```

---

## 7. 폼·입력 화면 설계

### 레이블과 정렬

1. **폼 라벨 위치**
   - 입력 필드 좌측 또는 상단에 배치하되, 한 화면에서 일관되게 유지
   - **권장**: 좌측 라벨 (공간 효율성 + 스캔 용이성)

2. **정렬과 간격**
   - 여러 필드를 열로 나열할 때는 **정렬과 간격**을 맞춰 스캔이 쉽도록

**예시:**
```vue
<div class="tw-grid tw-grid-cols-2 tw-gap-4">
  <FormField label="고객명" required>
    <Input v-model="form.customerName" />
  </FormField>
  <FormField label="고객 유형">
    <Dropdown v-model="form.customerType" :options="customerTypes" />
  </FormField>
  <FormField label="연락처">
    <Input v-model="form.phone" />
  </FormField>
  <FormField label="이메일">
    <Input v-model="form.email" type="email" />
  </FormField>
</div>
```

### 필수/선택 구분

- 필수 필드는 시각적 표시(예: `*` 아이콘)와 함께, 섹션 상단에 "* 필수 입력" 가이드를 둔다.

```vue
<FormField label="고객명" required>
  <Input v-model="form.customerName" />
</FormField>
```

### 실시간 검증 & 도움말

1. **실시간 검증**
   - 제출 시 한 번에 에러를 보여주는 대신, 필드 단위 실시간 검증 제공
   - VeeValidate + Zod 사용

2. **예시와 서브텍스트**
   - 포맷이 까다로운 필드는 예시(placeholder)와 서브텍스트로 가이드 제공

**예시:**
```vue
<FormField label="계좌번호" helper="하이픈(-) 없이 숫자만 입력하세요. 예: 1234567890">
  <Input
    v-model="form.accountNumber"
    placeholder="1234567890"
    :invalid="!!errors.accountNumber"
  />
  <small v-if="errors.accountNumber" class="p-error">
    {{ errors.accountNumber }}
  </small>
</FormField>
```

### 그룹화와 단계 분리

1. **필드 그룹화**
   - 필드가 많을 경우, 논리적으로 관련된 필드를 그룹화하고 섹션 헤더를 둔다.

```vue
<Panel header="기본 정보">
  <FormField label="고객명">...</FormField>
  <FormField label="고객 유형">...</FormField>
</Panel>

<Panel header="연락처 정보">
  <FormField label="전화번호">...</FormField>
  <FormField label="이메일">...</FormField>
</Panel>
```

2. **단계별 폼 (Stepper)**
   - 정말 많은 경우에는 단계별 폼(스텝퍼)을 고려하되, 현재 단계/전체 단계를 명확히 보여준다.

```vue
<Steps :model="steps" :activeStep="activeStep" />
```

### 파괴적 액션의 확인 절차

- 삭제, 대량 상태 변경 등은 별도의 확인 모달과 함께, 작업 결과를 요약해서 보여준다.

```vue
<ConfirmDialog
  message="정말 이 케이스를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다."
  header="케이스 삭제 확인"
  icon="pi pi-exclamation-triangle"
  accept-label="삭제"
  reject-label="취소"
  accept-class="p-button-danger"
  @accept="handleDelete"
/>
```

---

## 8. 피드백, 상태, 에러 처리

### 로딩 상태

1. **스켈레톤 UI**
   - 주요 리스트/카드에는 스켈레톤 UI 또는 로딩 인디케이터 제공

```vue
<Skeleton v-if="loading" height="2rem" class="tw-mb-2" />
<div v-else>{{ data }}</div>
```

2. **진행 상태 표시**
   - 긴 작업(수 초 이상)이 예상될 경우, 진행 상태(예: 40%) 또는 예상 소요 시간 표시

```vue
<ProgressBar :value="progress" />
<p class="tw-text-sm tw-text-gray-600">처리 중... {{ progress }}% 완료</p>
```

### 성공 피드백

1. **토스트 알림**
   - 저장/수정/삭제 성공 시, 사용 흐름을 끊지 않는 **토스트 알림** 사용

```typescript
const toast = useToast()
toast.add({
  severity: 'success',
  summary: '저장 완료',
  detail: '케이스가 성공적으로 저장되었습니다.',
  life: 3000
})
```

2. **요약 페이지**
   - 중요한 결과(예: 대량 처리 결과)는 요약 정보와 함께 별도 페이지 또는 패널로 제공

```vue
<Panel header="일괄 승인 결과">
  <p>총 10건 중 8건 승인 성공, 2건 실패</p>
  <DataTable :value="results">
    <Column field="caseId" header="케이스 ID"></Column>
    <Column field="status" header="상태"></Column>
    <Column field="message" header="메시지"></Column>
  </DataTable>
</Panel>
```

### 에러 피드백

1. **폼 에러**
   - 필드 근처에 명확한 메시지로 표시
   - 상단에 전체 요약 추가 제공

```vue
<Message v-if="errors.length > 0" severity="error">
  <strong>{{ errors.length }}개의 필드에 오류가 있습니다.</strong>
  <ul>
    <li v-for="error in errors" :key="error.field">{{ error.message }}</li>
  </ul>
</Message>
```

2. **시스템 에러**
   - 사용자 관점에서 이해 가능한 문장으로 설명
   - 다음에 무엇을 해야 하는지 안내

```vue
<Message severity="error">
  <strong>서버 연결 오류</strong>
  <p>서버와 연결할 수 없습니다. 네트워크 연결을 확인하거나 잠시 후 다시 시도해주세요.</p>
  <Button label="다시 시도" severity="secondary" @click="retry" />
</Message>
```

---

## 9. 접근성 및 반응형

### 접근성

1. **색상만으로 상태 구분 금지**
   - 아이콘/텍스트를 함께 사용

```vue
<!-- ✅ Good -->
<Badge :value="status" :severity="getSeverity(status)">
  <i :class="getIcon(status)"></i> {{ status }}
</Badge>

<!-- ❌ Bad -->
<Badge :value="status" :severity="getSeverity(status)" />
```

2. **명도 대비 기준**
   - WCAG 2.1 Level AA 기준 준수 (4.5:1)
   - 작은 글씨 남용 피하기

3. **키보드 포커스 순서**
   - 키보드만으로도 핵심 플로우를 수행할 수 있도록 포커스 순서 설계

### 반응형 설계 전략

**타겟 해상도:**

```
주요 타겟 (Primary):
- 1920x1080 (Full HD) - 가장 일반적
- 1680x1050 (WSXGA+)
- 1600x900 (HD+)
- 1366x768 (HD) - 최소 지원 해상도

선택적 지원 (Optional):
- 2560x1440 (QHD)
- 3840x2160 (4K)
```

**Tailwind 반응형 브레이크포인트:**

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    screens: {
      'sm': '640px',   // 사용 지양
      'md': '768px',   // 사용 지양
      'lg': '1024px',  // 1366px 대응
      'xl': '1280px',  // 1680px 대응
      '2xl': '1536px', // 1920px+ 대응
    }
  }
}
```

**반응형 디자인 구현:**

```vue
<template>
  <!-- ✅ 다양한 해상도에서 작동하는 레이아웃 -->
  <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-2 xl:tw-grid-cols-4 tw-gap-4">
    <!-- 해상도별로 컬럼 수 조정 -->
  </div>

  <!-- ✅ 최소 너비 지정 (1366px 기준) -->
  <div class="tw-min-w-[1280px] tw-mx-auto">
    <!-- 콘텐츠 -->
  </div>
</template>
```

**주의사항:**
- ❌ 모바일 터치 제스처 불필요 (스와이프, 핀치 줌 등)
- ❌ 모바일 네비게이션 패턴 불필요 (햄버거 메뉴, 하단 탭 등)
- ✅ 마우스 + 키보드 상호작용 최적화
- ✅ 넓은 화면 활용 (멀티 컬럼, 사이드바 등)
- ✅ 테이블, 그리드 중심 UI (RealGrid 활용)

---

## 10. 컴포넌트 및 디자인 시스템

### Atomic Design + Feature-Sliced Design (FSD)

**컴포넌트 계층:**

```
shared/ui/
├── atoms/          # 기본 요소 (Button, Input, Label, Badge, Icon)
├── molecules/      # 조합 컴포넌트 (FormField, SearchBox, DateRangePicker)
└── organisms/      # 복잡한 컴포넌트 (Modal, DataTable, SearchPanel)

widgets/            # 대형 페이지 블록
├── header/         # AppHeader
├── sidebar/        # AppSidebar
├── tab-manager/    # TabManager (VS Code 스타일)
└── menu-navigation/ # MenuNavigation

features/           # 사용자 기능
├── attachment/     # 첨부파일 업로드/다운로드
├── memo/           # 개인 업무 메모
└── notification/   # SSE 기반 실시간 알림
```

### 재사용 가능한 컴포넌트 정의

**각 컴포넌트별로 상태/변형을 정의:**

| 컴포넌트 | 상태/변형 |
|----------|-----------|
| Button | primary, secondary, success, info, warn, danger, text, outlined |
| Input | default, invalid, disabled, readonly |
| Badge | success, info, warn, danger, secondary |
| Modal | small, medium, large, fullscreen |

**예시:**

```vue
<!-- Button 컴포넌트 -->
<Button label="저장" severity="primary" />
<Button label="취소" severity="secondary" />
<Button label="삭제" severity="danger" />
<Button label="닫기" text />

<!-- Badge 컴포넌트 -->
<Badge value="승인" severity="success" />
<Badge value="대기" severity="warn" />
<Badge value="반려" severity="danger" />
```

### 패턴 컴포넌트

**조합 패턴을 재사용 가능한 컴포넌트로 정의:**

**ListPage 패턴:**
- 검색 + 필터 + 테이블 + 페이지네이션

**FormPage 패턴:**
- 섹션 헤더 + 폼 필드 그룹 + 액션 버튼

**DetailPage 패턴:**
- 정보 카드 + 탭 + 첨부파일 + 메모

---

## 11. 텍스트, 레이블, 톤&매너

### 레이블/버튼 텍스트

1. **간결한 동사 중심**
   - "등록", "저장", "삭제", "취소", "다시 시도"

2. **행동을 구체적으로 명시**
   - ✅ "변경 내용 저장"
   - ❌ "확인"

**예시:**

```vue
<Button label="케이스 생성" severity="primary" />
<Button label="변경 내용 저장" severity="primary" />
<Button label="케이스 삭제" severity="danger" />
<Button label="취소" severity="secondary" />
```

### 시스템 메시지 톤

1. **존댓말, 명료하고 차분한 톤**
   - ✅ "케이스가 성공적으로 저장되었습니다."
   - ❌ "케이스 저장 완료!"

2. **사용자 실수 시 비난 금지, 해결 방법 제공**
   - ✅ "고객명은 필수 입력 항목입니다. 고객명을 입력해주세요."
   - ❌ "고객명을 입력하지 않았습니다."

**예시:**

```vue
<Message severity="error">
  <strong>입력 오류</strong>
  <p>고객명은 필수 입력 항목입니다. 고객명을 입력해주세요.</p>
</Message>
```

---

## 12. 페이지 템플릿 및 탭 UI

### 페이지 템플릿 구조

**BasePage** - 모든 페이지의 기본 레이아웃

```vue
<template>
  <div class="base-page">
    <PageHeader :title="title" :breadcrumb="breadcrumb">
      <template #actions>
        <slot name="actions" />
      </template>
    </PageHeader>

    <div class="page-content">
      <slot />
    </div>

    <PageFooter>
      <slot name="footer" />
    </PageFooter>
  </div>
</template>
```

**ListPage** - 목록 페이지

```vue
<BasePage title="STR 케이스 목록">
  <template #actions>
    <Button label="케이스 생성" severity="primary" @click="createCase" />
  </template>

  <SearchPanel v-model:filters="filters" @search="handleSearch" />

  <DataTable :value="cases" selectionMode="multiple">
    <!-- 컬럼 정의 -->
  </DataTable>

  <Paginator :rows="20" :totalRecords="totalRecords" />
</BasePage>
```

**FormPage** - 작성/수정 페이지

```vue
<BasePage title="케이스 생성">
  <Form @submit="handleSubmit">
    <Panel header="기본 정보">
      <FormField label="고객명" required>
        <Input v-model="form.customerName" />
      </FormField>
    </Panel>

    <Panel header="상세 정보">
      <!-- 추가 필드 -->
    </Panel>

    <template #footer>
      <Button label="저장" severity="primary" type="submit" />
      <Button label="취소" severity="secondary" @click="cancel" />
    </template>
  </Form>
</BasePage>
```

**DetailPage** - 조회 페이지 (읽기 전용)

```vue
<BasePage :title="`케이스 상세 (${caseId})`">
  <template #actions>
    <Button label="수정" severity="primary" @click="edit" />
    <Button label="삭제" severity="danger" @click="confirmDelete" />
  </template>

  <TabView>
    <TabPanel header="기본 정보">
      <InfoCard :data="caseData" />
    </TabPanel>
    <TabPanel header="거래 내역">
      <DataTable :value="transactions" />
    </TabPanel>
    <TabPanel header="첨부파일">
      <AttachmentList :caseId="caseId" />
    </TabPanel>
    <TabPanel header="메모">
      <MemoList :caseId="caseId" />
    </TabPanel>
  </TabView>
</BasePage>
```

### TabManager (VS Code 스타일)

**URL 기반 탭 열림:**

```vue
<TabManager>
  <Tab title="대시보드" url="/" closable="false" />
  <Tab title="케이스 목록" url="/cases" />
  <Tab title="케이스 상세 (CASE-001)" url="/cases/CASE-001" />
</TabManager>
```

**페이지 상태 유지:**
- 탭 전환 시 작성 중인 데이터 보존
- `PageStateManager` 사용

---

## 13. Inspect-Hub 특화 패턴

### Snapshot 기반 정책 관리

**모든 정책/기준 데이터는 스냅샷 구조로 관리:**

```vue
<template>
  <Panel header="정책 버전 관리">
    <div class="tw-flex tw-items-center tw-gap-4">
      <Dropdown
        v-model="selectedSnapshot"
        :options="snapshots"
        optionLabel="versionName"
        placeholder="버전 선택"
      />
      <Badge :value="selectedSnapshot.status" :severity="getStatusSeverity(selectedSnapshot.status)" />
      <span class="tw-text-sm tw-text-gray-600">
        배포일: {{ formatDate(selectedSnapshot.deployedAt) }}
      </span>
    </div>

    <div class="tw-mt-4">
      <Button label="이전 버전으로 롤백" severity="warn" @click="confirmRollback" />
    </div>
  </Panel>
</template>
```

### Maker-Checker (SoD) 프로세스

**케이스 생성/조사/승인 프로세스:**

```vue
<template>
  <Panel header="승인 프로세스">
    <Steps :model="approvalSteps" :activeStep="currentStep" />

    <div class="tw-mt-4">
      <div v-if="canApprove">
        <Button label="승인" severity="success" @click="approve" />
        <Button label="반려" severity="danger" @click="reject" />
      </div>
      <div v-else-if="canRequest">
        <Button label="승인 요청" severity="primary" @click="requestApproval" />
      </div>
      <Message v-else severity="info">
        현재 단계에서는 승인/반려를 할 수 없습니다.
      </Message>
    </div>
  </Panel>
</template>

<script setup lang="ts">
const perm = usePermissionManager()
const canApprove = computed(() =>
  perm.hasRole('ROLE_APPROVER') && currentStep.value === 'pending_approval'
)
const canRequest = computed(() =>
  perm.hasRole('ROLE_ANALYST') && currentStep.value === 'draft'
)
</script>
```

### 감사 로깅 (100% Audit Trail)

**모든 중요 액션에 감사 로그 자동 기록:**

```typescript
// shared/lib/audit-logger/index.ts
export class AuditLogger {
  async log(action: string, target: string, beforeData?: any, afterData?: any) {
    await api.post('/api/v1/audit/logs', {
      action,          // 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT'
      target,          // 'CASE', 'POLICY', 'USER'
      targetId,        // 'CASE-001'
      beforeJson: beforeData ? JSON.stringify(beforeData) : null,
      afterJson: afterData ? JSON.stringify(afterData) : null,
      userId: sessionManager.getUserId(),
      timestamp: new Date().toISOString()
    })
  }
}
```

### RealGrid2 대용량 그리드

**대용량 데이터 테이블 (10만 건 이상):**

```vue
<template>
  <RealGrid
    :columns="columns"
    :dataSource="dataSource"
    :options="{
      checkBar: { visible: true },
      stateBar: { visible: true },
      sortMode: 'explicit',
      filterMode: 'immediate',
      export: { showProgress: true }
    }"
    @cellClicked="handleCellClick"
  />
</template>
```

---

## 14. 화면 설계 출력 형식

화면 설계 결과를 반환할 때, 아래 형식을 따른다.

### 1. 화면 개요

- **화면 이름**: (예: STR 케이스 목록)
- **주요 사용자 역할**: (예: 준법 담당자, 조직 요청자)
- **이 화면의 목표**: (3줄 이내로 요약)

### 2. 주요 사용자 시나리오

3~5개의 대표적인 시나리오를 "As-is → To-be" 형태로 간단히 정리:

1. **시나리오 1**: 준법 담당자가 대기 중인 STR 케이스를 빠르게 찾아 조사 시작
   - As-is: 메뉴 → 검색 → 필터 적용 → 케이스 클릭 (5단계)
   - To-be: 대시보드 "대기 중인 STR 10건" 클릭 → 케이스 목록 (2단계)

2. **시나리오 2**: 여러 케이스를 일괄 승인
   - As-is: 케이스 하나씩 열어서 승인 (10번 반복)
   - To-be: 체크박스로 10건 선택 → "일괄 승인" 버튼 (2단계)

### 3. 정보 구조 (IA)

상단/중단/하단, 좌/우 영역별로 어떤 블록이 배치되는지 서술:

```
┌─────────────────────────────────────────────────────────┐
│ 상단: 페이지 타이틀 + 주요 액션 버튼                     │
│ - "STR 케이스 목록" | [케이스 생성] 버튼                │
├─────────────────────────────────────────────────────────┤
│ 중단: 검색 + 필터 영역                                   │
│ - [검색창] [상태: 전체 ▼] [기간: 최근 7일 ▼] [고급 필터]│
├─────────────────────────────────────────────────────────┤
│ 하단: 데이터 테이블 + 페이지네이션                       │
│ - RealGrid2 테이블 (케이스 ID, 고객명, 탐지일, 상태 등) │
│ - 페이지네이션 (총 234건, 페이지당 20건)                │
└─────────────────────────────────────────────────────────┘
```

### 4. 와이어프레임 텍스트 설명

"[섹션 이름]: 구성 요소와 역할" 형식으로 상세하게 작성:

**[상단 헤더 영역]:**
- 좌측: 페이지 타이틀 "STR 케이스 목록"
- 우측: [케이스 생성] 버튼 (primary)

**[검색 및 필터 영역]:**
- 좌측: 검색창 (placeholder: "케이스 ID, 고객명으로 검색")
- 중앙: 드롭다운 필터 - 상태 (전체/대기중/조사중/완료)
- 중앙: 드롭다운 필터 - 기간 (오늘/최근 7일/최근 30일/사용자 정의)
- 우측: [고급 필터] 버튼 (슬라이드 패널 열림)

**[데이터 테이블 영역]:**
- 컬럼: [☐] [케이스 ID] [고객명] [탐지일] [상태] [담당자] [액션]
- 행 클릭 시: 케이스 상세 페이지로 이동
- [액션] 컬럼: [보기] [수정] 버튼
- 행 다중 선택 시: 상단에 "선택한 N건" + [일괄 승인] [일괄 반려] 버튼 표시

**[페이지네이션 영역]:**
- 하단 중앙: [◀] 1 2 3 4 5 ... 10 [▶]
- 우측: "총 234건, 페이지당 20건"

### 5. 컴포넌트 목록 및 상태

화면에 사용되는 주요 컴포넌트와 각 컴포넌트의 상태/변형을 목록으로 정리:

| 컴포넌트 | 위치 | 상태/변형 | 비고 |
|----------|------|-----------|------|
| Button | 상단 우측 | severity="primary" | "케이스 생성" |
| SearchBox | 검색 영역 | default | placeholder: "케이스 ID, 고객명으로 검색" |
| Dropdown | 필터 영역 | default | 상태, 기간 필터 |
| DataTable (RealGrid2) | 중앙 | selectionMode="multiple" | 행 다중 선택 가능 |
| StatusBadge | 테이블 내 | success/warn/danger | 케이스 상태 표시 |
| Paginator | 하단 | default | 페이지당 20건 |

### 6. 상태/에러/엣지 케이스

로딩/빈 상태/에러/권한 없음 상태 등 특수 상태에서의 UI를 각각 설명:

**[로딩 상태]:**
- 테이블 영역에 Skeleton UI 표시 (5줄)

**[빈 상태]:**
- 검색 결과가 없을 때:
  - 중앙에 아이콘 + "검색 조건에 맞는 케이스가 없습니다." 메시지
  - [필터 초기화] 버튼 표시

**[에러 상태]:**
- 서버 연결 오류 시:
  - Message 컴포넌트 (severity="error")
  - "서버와 연결할 수 없습니다. 네트워크 연결을 확인하거나 잠시 후 다시 시도해주세요."
  - [다시 시도] 버튼

**[권한 없음 상태]:**
- "케이스 생성" 권한이 없는 경우:
  - [케이스 생성] 버튼 숨김
  - 테이블은 읽기 전용으로 표시

### 7. 추가 고려사항

성능, 접근성, 반응형, 향후 확장성에서 특별히 유의해야 할 점:

**[성능]:**
- RealGrid2를 사용하여 10만 건 이상의 대용량 데이터도 렌더링 가능
- 가상 스크롤링 적용
- 서버 사이드 페이지네이션 사용

**[접근성]:**
- 키보드 단축키 지원 (Ctrl+N: 새 케이스, Ctrl+F: 검색 포커스)
- 색상 외에도 아이콘으로 상태 구분

**[반응형]:**
- 최소 지원 해상도: 1366x768
- 테이블은 가로 스크롤 가능

**[확장성]:**
- 추가 필터 항목 쉽게 추가 가능 (드롭다운 컴포넌트 추가)
- 테이블 컬럼 동적 추가/제거 가능 (설정에서 컬럼 표시/숨김 기능 추가 예정)

---

## 📖 참고 문서

- **[Frontend README](./README.md)** - Nuxt 4, SPA 제약사항, 코딩 규칙
- **[Components Roadmap](./COMPONENTS_ROADMAP.md)** - 공통 컴포넌트 구현 계획
- **[API Design](../api/DESIGN.md)** - RESTful API 설계 원칙
- **[Architecture Overview](../architecture/OVERVIEW.md)** - 전체 시스템 아키텍처
- **[PRD - Users & Roles](../prd/07-users-and-roles.md)** - 사용자 역할 정의

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-16 | 초안 작성 - LLM 지시문 템플릿 + Inspect-Hub 프로젝트 컨텍스트 결합 | PM |

---

## 📞 문의

문서 관련 문의사항:
- **Issues**: GitHub Issues
- **Email**: inspect-hub-team@example.com
- **Slack**: #inspect-hub-frontend
