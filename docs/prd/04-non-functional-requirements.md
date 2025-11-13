# 🧭 비기능 요구사항 (Non-Functional Requirements)

## 성능/용량(제안 초기치)

- 배치 처리: **≥ 1천만 건/일**, 야간 윈도우 내 완료
- 실시간 채널: **피크 300~500 TPS**(수평확장 용이)
- UI 응답: 일반 조회 **< 1초**, 복합 검색 **< 3초**

## 보안

- 전송구간 TLS, 저장구간 암호화(PII/민감필드), **RBAC + SoD**
- 필드 마스킹, 접근·변경 **감사로그 100%**
- 비밀번호/시크릿 외부화, KMS/하드닝, IP/망 분리 옵션

## 가용성/확장성/유지보수성

- 가용성 목표 **99.9%**(핵심 서비스), 무중단 배포(Blue/Green or Rolling)
- 모듈러 아키텍처, 룰/템플릿 외부화, CI/CD, 옵저버빌리티(OpenTelemetry 등)

## 규제/감사

- 데이터 보존: 규정 준수(예: STR/CTR 관련 원본/로그 N년)
- 운영행위 표준화: 변경관리·릴리즈 노트·승인 기록

## 지원 환경(Compatibility)

### 브라우저

- Chrome/Edge/Firefox 최근 **2개 메이저** 버전
- Safari 최신

### OS

- Windows 10/11, (운영툴은 사내 표준 OS 목록 준수)

### Frontend 배포 방식

- Frontend 소스는 정적리소스 배포
- Nuxt SSR문법 사용 금지

### 프로젝트 구조

- Gradle Module 기능으로 하나의 프로젝트에 `Backend`와 `Frontend`를 모듈로 분리

## 서버 런타임

### 빌드/공통

#### Gradle

멀티모듈·의존성 버전 일원화(Version Catalog 권장). 재현성 빌드를 위해 `gradle-wrapper.jar` 고정, 내부망은 prefetch 캐시/미러 저장소 설정 추천.

### Backend

#### JDK 21

Virtual Threads(선택), Record/Pattern Matching 최신 문법 활용. 컨테이너선 `-XX:+UseContainerSupport` 기본.

#### Spring Boot 3.3.2

Jakarta 전환(서블릿 패키지 변경) 반영. Actuator+Micrometer로 헬스/메트릭 수집, Caffeine/Redis 캐시 연동 용이.

#### Spring Security

(내용 생략)

#### Swagger (springdoc-openapi)

OpenAPI 3 스펙 자동화. 관리자/내부망만 노출, Bearer/JWT 스키마와 401/403 응답 모델 정의 권장.

#### MyBatis

복잡 SQL/프로시저 연계에 적합. TypeHandler(예: `LocalDateTimeSafeTypeHandler`)와 매핑 XML로 DB 방언 차이 관리. 페이징/동적 SQL은 `<trim>/<foreach>`로 템플릿화.

### Frontend (Nuxt/Vue)

#### Nuxt

파일 기반 라우팅·자동 코드 분할. SSR 필요 없으면 `ssr: false`로 SPA 빌드, `nitro` 설정 최소화. Runtime Config로 API 엔드포인트/키 분리.

**주요 플러그인:**

- **@pinia/nuxt**: 스토어 모듈화. `persisted state` 선택 적용으로 새로고침/탭 간 동기화.
- **@vueuse/nuxt**: 브라우저/네트워크/시간 유틸. Composition API 패턴 정리.
- **@nuxtjs/i18n**: 라우트 레벨 다국어. 메시지 키와 백엔드 사전(코드 딕셔너리) 매핑 규칙 합의 필요.
- **@nuxt/eslint**: CI에서 Lint fail을 빌드 실패로 연결(품질 게이트). Prettier 충돌 규칙 정리.
- **@nuxtjs/sentry**: 프론트 에러/성능 추적. 내부망은 프록시/릴레이 구성 또는 비활성 프로파일.
- **@nuxt/image**: 자동 리사이즈/웹포맷. 정적 자산 최적화와 LQIP(플레이스홀더)로 LCP 개선.
- **@nuxt/test-utils**: Vitest/Jest 래퍼. 컴포넌트·라우트 단위 테스트 베이스.
- **nuxt-security**: 보안 헤더(CSP, X-Frame-Options 등) 기본값. 내부망이어도 XSS/Clickjacking 최소화.
- **unplugin-auto-import / unplugin-vue-components**: API/컴포넌트 자동 import로 DX 향상, 트리셰이킹 유지.

#### pdf.js

브라우저에서 PDF 렌더링. 워커 분리 설정(성능), 대용량은 가상 스크롤/페이지 청크 로드.

#### Vue

Composition API + SFC. 재활용 가능한 UI/훅으로 도메인 로직 분리.

#### RealGrid

대용량 그리드(상용). 서버 페이징/가상 스크롤, 셀 에디트/검증/피벗. 라이선스/테마 커스터마이징 고려.

#### axios

공통 인스턴스/인터셉터로 토큰·에러 처리 일원화. 재시도(backoff), 취소 토큰으로 중복 요청 차단.

#### tailwind (prefix 적용)

PrimeVue/RealGrid 등 외부 CSS와 충돌 방지. 디자인 시스템 토큰(색/간격) 정의 용이.

#### PrimeVue

CRUD 폼/다이얼로그·테이블·Toast 등 풍부. Tailwind 프리셋과 톤 맞추고, Form은 VeeValidate와 결합 추천.

#### Zod

타입 안전한 스키마/런타임 검증. API 응답 파싱·폼 스키마 단일 소스화.

#### VeeValidate

폼 검증 프레임워크. Zod 어댑터로 스키마 공유(중복 제거), 에러 메시지 i18n 연계.

#### lodash-es

ES 모듈 트리셰이킹. 필요한 함수만 임포트(`import pick from 'lodash-es/pick'`).

#### Apache ECharts

대시보드 시각화. 데이터셋·툴박스·리사이즈 핸들러 구성, 캔버스 성능 고려.

#### FilePond

대용량 업로드/다중 파일·분할 전송·재시도

#### DOMPurify

리치 텍스트/외부 HTML sanitize. CSP와 함께 XSS 리스크 차단.
