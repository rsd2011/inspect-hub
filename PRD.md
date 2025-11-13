# 자금세탁방지 시스템

### 1) 사용자 유형(롤) 정의

| 역할 | 주요 목적 | 대표 작업 |
| --- | --- | --- |
| **조직 요청자(Reporter-Org)** | 탐지건 접수·조사 시작 | 이벤트 확인, 기초 사실관계 입력, 케이스 생성 |
| **준법 담당자(Manager-Compliance)** | 탐지건 심층 조사 / 조사 시작
품질·위험 검토 및 승인 | 고객/거래 분석, 증빙 첨부, STR 초안 작성, STR/CTR 승인/반려, 정책 예외 승인 |
| **준법 책임자(Compliance Officer)** | 규제 준수 총괄 | 보고 기준 확정, 대외 보고 최종 승인/송신 |
| **시스템 관리자(System Admin)** | 시스템·정책 운영 | 사용자·역할 관리, 룰 배포, 연계/배치 설정 |
| **감사/내부통제(Auditor)** | 사후 감사·추적 | 감사로그 조회, 변경 이력 점검(쓰기 금지) |
| **읽기전용(Reader)** | 상황 인지 | 대시보드/조회만 가능(다운로드 제한 가능) |

> ⚖️ 분리의 원칙(SoD): Analyst와 Approver는 동일 케이스에서 겸직 금지(제도상 Maker-Checker).
> 
- **핵심 기능**
    1. 사용자/역할(Role) 기반 접근권한 관리
    2. 결재선(Approval Line) 및 Role별 승인 프로세스 구성
    3. 로그인 이력 및 접근로그 감사

---

* 모든 기준정보 관리는 스냅샷구조로 관리하며 점검 수행은 스프링 배치 테이블 구조와 유사하게한다.

# 🧩 1. 프로젝트 개요 (Project Overview)

- **프로젝트명**: AML 통합 준법감시 시스템
- **한 줄 목적**: 전사 거래·고객 데이터를 기반으로 STR/CTR/WLF를 **정확·신속**히 탐지·조사·보고하여 규제준수와 리스크를 동시에 관리한다.
- **주요 대상 사용자**
    - 내부 준법감시/AML 담당, 리스크관리, 내부감사, 시스템운영(IT)
    - 대외 보고 담당(금융정보분석원 보고)
- **핵심 KPI**
    1. **의심거래(STR) 탐지 정확도 ≥ 95%** (오탐↓, 재탐지율↑)
    2. **대외 보고 처리 시간 30% 단축**(기존 대비)
    3. **STR 자동화율 ≥ 80%**(룰/임계치/워크플로 자동화)
    4. **조사·결재 리드타임 40% 단축**, **오탐 비율 20% 감소**
    5. **감사추적 100%**(접근·변경 로그 완전성)

---

# 🚀 2. 배경 및 문제 정의 (Background & Problem Statement)

- **현 문제/비효율**
    - 기준·정책이 산재(문서/엑셀 등) → 버전관리/적용범위 불명확
    - STR/CTR 탐지 로직 산발·수동 운영 → 오탐多, 대응 지연
    - WLF/제재리스트 임계치 운영 일관성 부족 → 민감도 튜닝 어려움
    - 보고서 작성 수작업 비중 큼 → 품질 편차·시간 소모 큼
    - 감사/변경이력 추적 난이도 높음(증빙 취약)
- **왜 지금**
    - 규제 고도화·제재 강화, 대형 과징금/평판위험 증가
    - 거래량·신상품 증가로 기존 수작업 한계
    - 내부 표준화/자동화 없이는 확장성·일관성 확보 불가
- **기존 시스템/프로세스**
    - 코어뱅킹·카드·대외계 등 각 채널별 데이터, 엑셀 기반 점검/보고
    - 일부 룰 엔진/배치 존재하나 단일 거버넌스 부재
- **범위(Scope)**
    - **신규 구축 + 통합/고도화**: 정책/기준 스냅샷 관리, 탐지·조사·보고 일관 워크플로, 점검 배치, 감사/이력, 대시보드

---

# 🎯 3. 목표 및 비전 (Objectives & Goals)

- **주요 목표(3)**
    1. 정책/기준의 **스냅샷 단일진실원(SSOT)** 구축 및 배포 자동화
    2. STR/CTR/WLF **탐지→조사→결재→보고** **E2E 자동화**
    3. **감사·이력·접근제어** 완비로 **규제 대응력 극대화**
- **상위 목표 연계**: 대내 리스크 관리·내부통제 강화, 대외 규제준수/평판 보호, 운영비용 절감
- **성공 판단 지표**
    - 정량: KPI 항목, SLA(탐지→최초조치 ≤ X시간, 결재 리드타임, 보고 TAT), 오탐/미탐 비율
    - 정성: 사용자 만족도(≥4.5/5), 감사 지적 “0”건

---

# 🧱 4. 기능 요구사항 (Functional Requirements)

## 4.1 주요 기능

1. **정책·기준 관리 모듈(스냅샷)**
    - **KYC 위험평가 기준 관리**
        - 고객/국가/상품/행동 **팩터(Factor)** 관리
        - 각 팩터별 **평가항목(Criterion)** 및 **항목별 세부 평가기준(점수표·구간·룰)** 관리
        - 각 위험요소 **가중치** 및 **종합 등급 구간** 관리
        - **국가위험**: 평가기관별(FATF, OFAC, EU, 국내 FIU 등) 지표·등급 **병행 관리**(스냅샷 비교·통합)
    - **상품위험 – 신상품 설문 연계(신규)**
        - **SMTP 이메일로 설문 링크 발송**(상품출시 담당자/오너 대상)
        - **서명된 1회용 토큰 링크**(유효기간/만료, 재발급) 및 **다국어(i18n) 설문** 지원
        - **설문 응답 수집 → 상품위험 팩터 자동 산출/가중합 반영**, 응답 변경 시 **스냅샷 버전 고정**으로 재현성 확보
        - **리마인더/재전송**, 반송(bounce)/수신확인 트래킹, 응답 마감 관리
        - **첨부 증빙 업로드**(예: 상품 설명서/내부 승인서) 및 **감사 이력(Maker-Checker)**
        - **신상품 출시 프로세스와 연계**(승인 전 설문 완료·리스크 승인 Gate)
    - **WLF(요주의인물) 임계치·알고리즘·카테고리 관리**
        - 알고리즘/프로파일/언어/국가별 **매칭 임계치 세트**
        - **복수 매칭 알고리즘 앙상블** 및 **알고리즘별 가중치 관리**
        - **카테고리 관리**: 다우존스 카테고리 동기화/버전, **PEP 직업 카테고리**, **제재 카테고리** 관리 및 룰·임계치 연계
        - **엔터티 유형별 정책 세트**
            - **개인(Individual)** 정책, **법인/단체(Organization)** 정책을 **분리 관리**
        - **개인(Individual) 매칭 정책 구성 항목**
            - **영문명 비교 옵션**: 성/중간이름/이름 **순서 섞기(permutation)**, 이니셜 허용, 하이픈/스페이스/구두점 무시, 일반적 축약·변형(예: Alex↔Alexander) 허용 여부
            - **로컬네임 비교**: 한글/중문/아랍어 등 **로컬 스크립트** 매칭 + **음차/음역(transliteration)** 사용 여부
            - **보조 속성**: **성별, 국적/거주국, 출생일(±오차 허용), 문서번호(여권/국민식별), 주소(토큰 정규화)** 등 사용 여부·가중치
            - **정규화 규칙**: 대소문자/악센트 제거, 특수문자·스페이스 규칙, Stopword 처리(“BIN”, “AL”, “DU” 등)
        - **법인/단체(Organization) 매칭 정책 구성 항목**
            - **법인명 표기 변형**(Inc., Ltd., GmbH 등 접미사 제거/표준화), **AKA/별칭**, **등록번호/LEI**, **국가/주소 토큰화** 사용 여부·가중치
        - **정책 논리(조건) 지정**
            - **모두 일치(AND) / 일부 일치(OR) / 가중 합산(Weighted Score) / 조건부 임계치(Conditional Threshold)** 지정
            - 예) “영문명(permutation) ≥ 0.92 **AND** 로컬네임 ≥ 0.85 **AND** 생년월일 정확일치 → **확정 매칭**”
            - 예) “영문명 ≥ 0.90 **AND** (국가 동일 **OR** 여권번호 유사) → **후보 매칭**”
            - **케이스 구분 정책**: PEP/제재 카테고리 포함 시 임계치 상향 또는 추가 속성 필수
        - **알고리즘·가중치·임계치 연동(기존 항목과 연계)**
            - **복수 매칭 알고리즘 앙상블**(Edit distance/Jaro-Winkler/Phonetic/Token/Embedding 등) **+ 알고리즘별 가중치**
            - **필드별 가중치**(영문명/로컬네임/성별/국가/생년월일/문서번호 등)와 **정책 논리**를 함께 적용
        - **정책 DSL/템플릿(관리자 UI)**
            - 드롭다운/토글 기반 정책 빌더 + **간단 DSL**(예: `name.en.permute>=0.92 && name.local>=0.85 && dob.eq(true)`)
            - **사전 검증/시뮬레이션**(샘플 데이터로 FP/FN 변화 확인), **버전 비교/롤백** 지원
        - **예외/화이트리스트/블랙리스트**
            - 특정 인물/법인에 대한 **정책 예외**(임시 임계치, 필드 무시), **기간/사유** 기록(Maker-Checker 승인)
        - **감사/거버넌스**
            - 정책 변경 이력, 승인/배포 트랙, 스냅샷별 적용 범위, 성능지표(탐지율/오탐율) 모니터링
    - **FIU 보고서 기준 관리**: 항목 단위 기준/검증 규칙
    - **STR 기준 관리**: 룰 시나리오/팩터, 가중치/임계치/종합 위험 기준
    - **RBA 기준 관리**: 점검 항목 단위(평가항목/기준 포함)
3. **리스크 분석(시뮬레이션) 모듈 (Risk Analysis / Simulation)**
    - **대상 범위**
        - **KYC RA (위험평가)** : 팩터/가중치/등급구간 변경 시 위험점수 변동 시뮬레이션
        - **STR 시뮬레이션** : 룰 시나리오/임계치/가중치 변경이 탐지량·정탐률에 미치는 영향분석
        - **CTR 시뮬레이션** : 금액·빈도 기준 변경 시 탐지건수·임계값 민감도 분석
        - **WLF 시뮬레이션** : 매칭 알고리즘/가중치/임계치 조정 시 탐지율·오탐율 비교
    - **기능 세부**
        - 기준(스냅샷) 버전 단위 시뮬레이션 수행 및 **버전별 결과 비교(Diff)**
        - **가중치/임계치/룰** 조정 → 가상 데이터셋(샘플 거래/고객) 적용 → 탐지결과 예측
        - **탐지 모듈과 동일 엔진** 을 사용하되 “검증용 격리환경”에서 실행
        - **성과 지표 자동 산출**: 탐지율, 정탐률, 오탐률, 탐지건수 증감, 분포 변화 등
        - **시각화 대시보드** : 변경 전후 탐지량, 리스크 등급 분포, Top Rule 변동, 기관별 영향도 그래프
        - **시뮬레이션 시나리오 저장·공유·재실행** 및 감사 이력 관리(Maker–Checker)
        - **결과를 탐지모듈로 승격(Promotion)** 가능 (승인 후 배포)
    - **활용 예시**
        - STR 룰 조정 전, 탐지 수 및 정탐률 변화 예측
        - CTR 금액 임계치 변경 전, 탐지 건수 민감도 확인
        - WLF 앙상블 가중치 조정 후, 탐지율·오탐률 비교
        - KYC 위험 가중치 변경 전, 등급 재분포 시각화
4. **탐지 모듈**
    - STR / CTR / WLF 실행 및 운영 탐지 (실데이터 적용, 스냅샷 고정)
5. **보고 모듈**
    - STR / CTR 자동 보고서, FIU 템플릿 기반 생성·검증
6. **점검 수행 모듈**
    - STR / CTR / RBA / 요주의인물 점검(배치 Instance/Execution 구조)
7. **운영 및 관리**
    - **사용자·조직·조직정책·권한그룹·결재선 관리**
        - **사용자(User)** 는 반드시 **하나의 조직**과 **하나의 권한그룹(Permission Group)** 에 속함.
        - **조직** 은 **조직정책(Organization Policy)** 을 참조하고, 조직정책은
            1. **신규/이동 시 부여될 기본 권한그룹 세트**(초기값)와
            2. **업무별 결재선 템플릿**을 정의함.
        - **권한그룹** 은 **메뉴별 권한, 기능별 권한** 및 **역할(Role) 매핑을 포함**하며 **사용자에 직접 연결**됨.
            - 메뉴 권한
                - 메뉴에 대한 접근 가능 여부 관리
            - 기능별 권한
                
                ```bash
                FEATURE(feature_code pk, name, desc, owner_team, is_active)
                FEATURE_ACTION(feature_code fk, action_code)                  -- 허용 액션 카탈로그
                
                PERM_GROUP_FEATURE(feature_code fk, perm_group_code, action_code)  
                -- 권한그룹이 어떤 기능-액션을 가질 수 있는지
                
                MENU(id pk, parent_id, title, route_name, route_params_json, external_url, visible, sort_order, icon)
                ```
                
        - **데이터 정책 관리**
            
            ```bash
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
            
        - **결재선(Approval Line)** 은 **업무별 템플릿**을 사용하며, **각 결재 단계는 ‘권한그룹’으로 지정**됨.
            - 예: `STEP1=PG.REVIEWER`, `STEP2=PG.APPROVER`, `STEP3=PG.HEAD_COMPLIANCE`
            - 결재 대상 사용자 선택 규칙(권장):
                - **기본**: 요청자의 **동일 조직** 내 해당 권한그룹 소속자 중 1인(라운드로빈/가중치 선택 정책 지원)
                - **병렬 단계**: same-step 안에 다수 권한그룹 지정 가능(ANY/ALL 모드)
                - **부재/결원**: 위임자(or 상위 조직 경로) → 정책별 **fallback**
                - **충돌 방지**: 요청자와 동일 권한그룹일 경우 **self-approval 금지** 옵션
    - **메뉴 관리 (Menu Management)**
        - 페이지가 없는 카테고리 관리 필요
        - **메뉴 트리/순서/노출 여부/아이콘/외부링크**와 **권한 요구조건(필요 권한그룹, 허용 액션)** 을 **DB로 관리**.
        - **메뉴는 라우트 이름(routeName)과 매핑**되어, **메뉴 가시성**은 DB가, **실제 접근 통제**는 백엔드 권한 검사로 보장.
        - 변경 이력/감사(메뉴 구조, 권한 요구조건 수정) 기록.
    - **룰 / 스냅샷 배포 관리**
        - 탐지 룰 및 시뮬레이션 스냅샷의 버전·배포·승인·롤백 관리
    - **로그 / 감사 관리**
        - 사용자 행위, 정책 변경, 룰 배포 등 시스템 내 주요 이벤트 감사 로그 관리
    - **SMTP 발송 설정 관리**
        - 발신 도메인, SPF/DKIM, 발송 한도, 템플릿 설정 및 발송 이력 관리
    - **알람 시스템**

4.2 핵심 플로우(요약)

- **데이터 유입 → 탐지 → 케이스 생성 → 조사·증빙 → 결재 → 보고(전송/수신확인) → 감사/리포팅**
- **정책/기준**: 초안→리뷰→승인→배포(스냅샷)→탐지엔진 반영

## 4.3 우선순위

- **필수(MVP)**: 데이터 수집/정합성, STR/CTR 기본 룰, 케이스/결재, 보고 템플릿, 사용자·역할, 감사로그, 대시보드(핵심)
- **선택(차순위)**: 고급 유사도 튜닝, 자동 재학습 지원(비ML), 시뮬레이션/백테스트 UI, 룰 A/B 배포, 규제 변경 알림

---

# 🧭 5. 비기능 요구사항 (Non-Functional Requirements)

- **성능/용량(제안 초기치)**
    - 배치 처리: **≥ 1천만 건/일**, 야간 윈도우 내 완료
    - 실시간 채널: **피크 300~500 TPS**(수평확장 용이)
    - UI 응답: 일반 조회 **< 1초**, 복합 검색 **< 3초**
- **보안**
    - 전송구간 TLS, 저장구간 암호화(PII/민감필드), **RBAC + SoD**
    - 필드 마스킹, 접근·변경 **감사로그 100%**
    - 비밀번호/시크릿 외부화, KMS/하드닝, IP/망 분리 옵션
- **가용성/확장성/유지보수성**
    - 가용성 목표 **99.9%**(핵심 서비스), 무중단 배포(Blue/Green or Rolling)
    - 모듈러 아키텍처, 룰/템플릿 외부화, CI/CD, 옵저버빌리티(OpenTelemetry 등)
- **규제/감사**
    - 데이터 보존: 규정 준수(예: STR/CTR 관련 원본/로그 N년)
    - 운영행위 표준화: 변경관리·릴리즈 노트·승인 기록
- **지원 환경(Compatibility)**
    - **브라우저:** Chrome/Edge/Firefox 최근 **2개 메이저** 버전, Safari 최신
    - **OS:** Windows 10/11, (운영툴은 사내 표준 OS 목록 준수)
    - Frontend 소스는 정적리소스 배포
        - Nuxt SSR문법 사용 금지
    - Gradle Module 기능으로 하나의 프로젝트에 `Backend`와 `Frontend`를 모듈로 분리
    - **서버 런타임:**
        
        ## 빌드/공통
        
        - **Gradle**
            
            멀티모듈·의존성 버전 일원화(Version Catalog 권장). 재현성 빌드를 위해 `gradle-wrapper.jar` 고정, 내부망은 prefetch 캐시/미러 저장소 설정 추천.
            
        
        ## Backend
        
        - **JDK 21**
            
            Virtual Threads(선택), Record/Pattern Matching 최신 문법 활용. 컨테이너선 `-XX:+UseContainerSupport` 기본.
            
        - **Spring Boot 3.3.2**
            
            Jakarta 전환(서블릿 패키지 변경) 반영. Actuator+Micrometer로 헬스/메트릭 수집, Caffeine/Redis 캐시 연동 용이.
            
        - **Spring Security**
        - **Swagger (springdoc-openapi)**
            
            OpenAPI 3 스펙 자동화. 관리자/내부망만 노출, Bearer/JWT 스키마와 401/403 응답 모델 정의 권장.
            
        - **MyBatis**
            
            복잡 SQL/프로시저 연계에 적합. TypeHandler(예: `LocalDateTimeSafeTypeHandler`)와 매핑 XML로 DB 방언 차이 관리. 페이징/동적 SQL은 `<trim>/<foreach>`로 템플릿화.
            
        - 
        
        ## Frontend (Nuxt/Vue)
        
        - **Nuxt**
            
            파일 기반 라우팅·자동 코드 분할. SSR 필요 없으면 `ssr: false`로 SPA 빌드, `nitro` 설정 최소화. Runtime Config로 API 엔드포인트/키 분리.
            
            - **@pinia/nuxt**:
                
                스토어 모듈화. `persisted state` 선택 적용으로 새로고침/탭 간 동기화.
                
            - **@vueuse/nuxt**:
                
                브라우저/네트워크/시간 유틸. Composition API 패턴 정리.
                
            - **@nuxtjs/i18n**:
                
                라우트 레벨 다국어. 메시지 키와 백엔드 사전(코드 딕셔너리) 매핑 규칙 합의 필요.
                
            - **@nuxt/eslint**:
                
                CI에서 Lint fail을 빌드 실패로 연결(품질 게이트). Prettier 충돌 규칙 정리.
                
            - **@nuxtjs/sentry**:
                
                프론트 에러/성능 추적. 내부망은 프록시/릴레이 구성 또는 비활성 프로파일.
                
            - **@nuxt/image**:
                
                자동 리사이즈/웹포맷. 정적 자산 최적화와 LQIP(플레이스홀더)로 LCP 개선.
                
            - **@nuxt/test-utils**:
                
                Vitest/Jest 래퍼. 컴포넌트·라우트 단위 테스트 베이스.
                
            - **nuxt-security**:
                
                보안 헤더(CSP, X-Frame-Options 등) 기본값. 내부망이어도 XSS/Clickjacking 최소화.
                
            - **unplugin-auto-import / unplugin-vue-components**:
                
                API/컴포넌트 자동 import로 DX 향상, 트리셰이킹 유지.
                
        - **pdf.js**
            
            브라우저에서 PDF 렌더링. 워커 분리 설정(성능), 대용량은 가상 스크롤/페이지 청크 로드.
            
        - **Vue**
            
            Composition API + SFC. 재활용 가능한 UI/훅으로 도메인 로직 분리.
            
        - **RealGrid**
            
            대용량 그리드(상용). 서버 페이징/가상 스크롤, 셀 에디트/검증/피벗. 라이선스/테마 커스터마이징 고려.
            
        - **axios**
            
            공통 인스턴스/인터셉터로 토큰·에러 처리 일원화. 재시도(backoff), 취소 토큰으로 중복 요청 차단.
            
        - **tailwind (prefix 적용)**
            
            PrimeVue/RealGrid 등 외부 CSS와 충돌 방지. 디자인 시스템 토큰(색/간격) 정의 용이.
            
        - **PrimeVue**
            
            CRUD 폼/다이얼로그·테이블·Toast 등 풍부. Tailwind 프리셋과 톤 맞추고, Form은 VeeValidate와 결합 추천.
            
        - **Zod**
            
            타입 안전한 스키마/런타임 검증. API 응답 파싱·폼 스키마 단일 소스화.
            
        - **VeeValidate**
            
            폼 검증 프레임워크. Zod 어댑터로 스키마 공유(중복 제거), 에러 메시지 i18n 연계.
            
        - **lodash-es**
            
            ES 모듈 트리셰이킹. 필요한 함수만 임포트(`import pick from 'lodash-es/pick'`).
            
        - **Apache ECharts**
            
            대시보드 시각화. 데이터셋·툴박스·리사이즈 핸들러 구성, 캔버스 성능 고려.
            
        - **FilePond**
            
            대용량 업로드/다중 파일·분할 전송·재시도
            
        - **DOMPurify**
            
            리치 텍스트/외부 HTML sanitize. CSP와 함께 XSS 리스크 차단.
            

---

# 🔗 6. 시스템 연계 (System Integration)

- **연계 대상**
    - 코어뱅킹/카드/채널 거래, 고객/계좌/상품 마스터, HR(직무/조직), 인증/SSO(IdP), 대외 제재/WLF 소스
- **교환 방식(예시)**
    - 실시간: **REST/GraphQL**, **Kafka(MQ)** 이벤트
    - 배치: SFTP/파일 교환(CSV/Parquet), DB 링크/CDC
    - 대외 보고: 포맷(XML/CSV) + SFTP/전용 포털 연계(기관 규격 준수)
- **인증/보안 토큰**: OIDC/OAuth2, mTLS, 사내망 접근통제

---

# 🧮 7. 데이터 구조 / 모델 (Data & Schema)

- **스냅샷 기반 기준 마스터(예시 주요 컬럼)**
    - `STANDARD_SNAPSHOT`(id, type[KYC/STR/CTR/RBA/WLF/FIU], version, effective_from/to, status, created_by, approved_by, prev_id, next_id)
    - `KYC_RISK_FACTOR`(snapshot_id, risk_type[고객/국가/상품/행동], factor_code, value_set, weight, score_min/max, grade_band)
    - `WLF_THRESHOLD`(snapshot_id, algo(ver/sim), threshold, country_bias, update_source)
    - `FIU_REPORT_FIELD`(snapshot_id, field_code, label, type, required, validation_rule, mapping)
    - `STR_RULE`(snapshot_id, rule_code, severity, description, enabled)
        
        `STR_RULE_FACTOR`(rule_code, factor_code, operator, value, weight)
        
    - `RBA_CHECK_ITEM`(snapshot_id, item_code, description, weight, periodicity)
- **탐지/조사/보고**
    - `TX_STAGING`(tx_id, cust_id, acct_id, amount, channel, ts, …)
    - `DETECTION_EVENT`(event_id, tx_id, rule_code, score, matched_fields, snapshot_ver)
    - `ALERT_CASE`(case_id, status, priority, owner, created_ts, snapshot_ver)
    - `CASE_ACTIVITY`(case_id, action, actor, ts, comment, attachment_ref)
    - `REPORT_STR/CTR`(report_id, case_id, fields(json), status, sent_ts, ack_ts)
- **점검 배치(Spring Batch 유사)**
    - `INSPECTION_INSTANCE`(instance_id, type[STR/CTR/WLF/RBA], snapshot_ver, created_ts)
    - `INSPECTION_EXECUTION`(exec_id, instance_id, status, started_ts, ended_ts, read/write/skip_cnt, exit_msg)
- **이력/감사**
    - `AUDIT_LOG`(who, when, what, before/after), 각 마스터 prev/next 체인
- **식별자/키**
    - 전역 **ULID/UUID** 권장, 외부 연계키 별도 매핑

---

# 🧑‍💻 8. 사용자 및 권한 (Users & Roles)

| 역할 | 접근 기능(요약) |
| --- | --- |
| 조직 요청자(Reporter-Org) | 탐지 이벤트 확인, 케이스 생성/기초 입력 |
| 조직 승인자(Approver-Org) | STR/CTR 품질/위험 검토, 승인/반려 |
| 조사자/애널리스트(Analyst) | 조사·증빙 첨부, STR 초안 작성 |
| 준법 담당자(Manager-Compliance) | 조사/승인/반려, 예외 승인, 기준 리뷰 |
| 준법 승인자(Approver-Compliance) | 최종 내부 승인 |
| 준법 책임자(Compliance Officer) | 보고 기준 확정, 대외 보고 최종 승인/송신 |
| 시스템 관리자(System Admin) | 사용자/역할, 룰 배포, 스케줄, 연계/배치 설정 |
| 감사/내부통제(Auditor) | 조회 전용, 로그/이력 점검(쓰기 금지) |
| 읽기전용(Reader) | 대시보드·조회(다운로드 제한 가능) |

> SoD 원칙: 동일 케이스에서 Analyst ⇄ Approver 겸직 금지. 결재선/위임 규칙 별도 설정.
> 

---

# 📆 9. 일정 및 우선순위 (Timeline & Priority) — *제안 일정(조정 가능)*

- **2025-10-13 ~ 10-24**: 요건 상세/데이터 맵핑/POC 플랜
- **2025-10-27 ~ 11-07**: 아키텍처·보안·데이터 모델 확정, POC(탐지·스냅샷 배포)
- **2025-11-10 ~ 12-19**: **MVP 개발**(필수 기능) & 1차 통합
- **2026-01-05 ~ 01-16**: 통합/성능/보안 테스트, 사용자 교육
- **2026-01-20**: **MVP Go-Live** (점진 확대)
- **우선순위 규칙**: 규제 필수 → 운영근간(유입/배포/감사) → 탐지정확도 → 보고생산성 → 분석 편의

**MVP 범위**: 데이터 유입·정합성, STR/CTR 기본 룰, 케이스/결재, FIU 템플릿, 사용자·역할, 감사로그, 기본 대시보드

---

# 🧪 10. 품질보증 및 검증 (QA & Validation)

- **테스트 전략**
    - 단위/통합/시나리오/회귀 테스트, 룰 백테스트(샘플 과거 데이터 리플레이)
    - 성능: 배치 윈도우 내 처리, 실시간 피크 TPS, 인덱스/파티셔닝 검증
    - 보안: 취약점 점검, 권한/SoD/감사/데이터 마스킹 테스트
- **검증 주체**
    - QA 팀 주도, 준법/감사 참여 UAT, IT 운영 시운전
- **수용 기준**
    - KPI 달성(또는 근접), 결함 심각도 기준 충족, 규제·감사 시나리오 통과

---

# 💬 11. 이해관계자 (Stakeholders)

- **역할별**
    - **PO/PM**: 범위·우선순위·일정·리스크 관리
    - **아키텍트/리드 개발**: 데이터/연계/보안 설계 총괄
    - **백엔드/배치/룰 엔진** 개발, **프론트엔드**(조사/대시보드)
    - **데이터 엔지니어**(ETL/CDC/카프카), **DBA**, **QA**, **운영**
    - **준법/AML 실무자**(룰/보고서/업무검증), **감사**
- **커뮤니케이션**: Jira(이슈/스프린트), Confluence/Notion(문서), Slack/Teams(알림), GitLab/Jenkins(CI/CD)

---

# 🧾 12. 기타

- **법·정책·규제 요건(예)**
    - 국내 AML 관련 법령/가이드(금융정보분석원, 특금법 등) 준수
    - 데이터 보존/파기, 개인정보 보호, 접근통제·감사 의무
- **리스크/제약**
    - 데이터 품질/식별키 불일치, 대외 연계 변동성, 규제 변경 주기
    - 오탐/미탐 밸런스 튜닝 난이도, 조직 변경(역할/결재선) 대응
    - 내부망·보안 정책(외부 의존 라이브러리/레지스트리 제한)
- **참고 산출물**
    - 룰 카탈로그, 보고서 필드 매핑표, 데이터 계보(lineage), 운영 Runbook, 배포 체크리스트