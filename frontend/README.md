# Inspect-Hub Frontend

Inspect-Hub AML 통합 컴플라이언스 모니터링 시스템의 프론트엔드 애플리케이션입니다.

## 🚨 중요한 제약사항 (CRITICAL CONSTRAINTS)

### 1. SPA 모드 Only - SSR 절대 금지

**이 프로젝트는 반드시 SPA(Single Page Application) 모드로만 동작해야 합니다.**

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  ssr: false,  // ⚠️ 절대 변경 금지 - NEVER change this to true
})
```

**금지 사항:**
- ❌ SSR (Server-Side Rendering) 사용 금지
- ❌ `ssr: true` 설정 금지
- ❌ Nuxt Server API (`/server` 디렉토리) 사용 금지
- ❌ Node.js 서버 사이드 로직 사용 금지
- ❌ `useAsyncData`, `useFetch`의 서버 사이드 기능 의존 금지
- ❌ Server Routes (`/server/api/*`) 사용 금지
- ❌ Server Middleware 사용 금지
- ❌ Nitro 서버 기능 사용 금지

**허용 사항:**
- ✅ 클라이언트 사이드 렌더링만 사용
- ✅ 정적 리소스로 빌드 (HTML, CSS, JS)
- ✅ 백엔드 API는 별도의 Spring Boot 서버 사용
- ✅ `$fetch`, `axios`, `fetch` 등 클라이언트 HTTP 라이브러리 사용
- ✅ 브라우저 API 사용 (localStorage, sessionStorage, etc.)

### 2. 배포 방식

**정적 리소스로 빌드 후 웹 서버(Nginx, Apache 등)에 배포:**

```bash
# 프로덕션 빌드 (정적 파일 생성)
npm run build

# 빌드 결과물: .output/public/
# 이 디렉토리를 Nginx, Apache, S3 등에 배포
```

**배포 구조:**
```
Frontend (SPA)           Backend (Spring Boot)
    ↓                           ↓
Nginx/Apache              Java 21 + Tomcat
    ↓                           ↓
Static HTML/CSS/JS      RESTful API (/api/v1/*)
    ↓                           ↓
Browser                   PostgreSQL/Redis/Kafka
```

### 3. API 통신 방식

**모든 데이터 통신은 클라이언트에서 백엔드 RESTful API로 직접 요청:**

```typescript
// ✅ 올바른 방법 - 클라이언트에서 백엔드 API 직접 호출
const api = useApiClient()
const response = await api.get('/api/v1/cases/str')

// ❌ 잘못된 방법 - Nuxt Server API 사용
const { data } = await useFetch('/api/cases')  // Server API 사용 금지
```

**API 호출 규칙:**
- Backend API Base URL: `http://localhost:8090/api/v1` (개발)
- 프로덕션: `https://api.inspecthub.example.com/api/v1`
- 모든 API 호출은 `shared/api/client.ts`를 통해 수행
- CORS 설정은 백엔드에서 처리

### 4. 웹 브라우저 중심 UI 설계

**이 프로젝트는 데스크톱/노트북 웹 브라우저를 주요 타겟으로 합니다.**

**UI 설계 원칙:**
- 🖥️ **웹 브라우저 First**: 데스크톱/노트북 사용 환경 최적화
- ❌ **모바일 고려 불필요**: 모바일 전용 UI/UX 설계 불필요
- ✅ **반응형 디자인 필수**: 다양한 해상도 대응 필요

**타겟 해상도:**
```
주요 타겟:
- 1920x1080 (Full HD) - 가장 일반적
- 1680x1050 (WSXGA+)
- 1600x900 (HD+)
- 1366x768 (HD) - 최소 지원 해상도

선택적 지원:
- 2560x1440 (QHD)
- 3840x2160 (4K)
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

**주의사항:**
- ❌ 모바일 터치 제스처 불필요 (스와이프, 핀치 줌 등)
- ❌ 모바일 네비게이션 패턴 불필요 (햄버거 메뉴, 하단 탭 등)
- ✅ 마우스 + 키보드 상호작용 최적화
- ✅ 넓은 화면 활용 (멀티 컬럼, 사이드바 등)
- ✅ 테이블, 그리드 중심 UI (RealGrid 활용)

## 기술 스택

### Core Framework
- **Nuxt 3**: Vue 3 기반 프레임워크 (SPA 모드)
- **Vue 3**: Composition API
- **Vite**: 빌드 도구
- **TypeScript**: 타입 안정성

### UI/UX
- **PrimeVue**: UI 컴포넌트 라이브러리
- **Tailwind CSS**: 유틸리티 CSS (prefix: `tw-`)
- **RealGrid**: 상용 그리드 컴포넌트
- **PrimeIcons**: 아이콘 세트
- **Noto Sans KR**: 기본 폰트

### State Management & Validation
- **Pinia**: 상태 관리
- **VeeValidate**: 폼 검증
- **Zod**: 스키마 검증

### Visualization
- **Apache ECharts**: 차트 라이브러리
- **vue-echarts**: Vue 3 래퍼

### Internationalization
- **@nuxtjs/i18n**: 다국어 지원 (한국어/영어)

## 프로젝트 구조 (Feature-Sliced Design)

```
frontend/
├── app/                    # 애플리케이션 레이어
│   ├── layouts/           # 레이아웃 컴포넌트
│   ├── providers/         # 글로벌 프로바이더 (PrimeVue, Auth)
│   └── styles/            # 글로벌 스타일, 폰트
├── pages/                  # 페이지 라우트 (Nuxt auto-routing)
├── widgets/                # 위젯 (대형 페이지 블록)
│   ├── header/            # AppHeader
│   ├── sidebar/           # AppSidebar
│   └── menu-navigation/   # MenuNavigation
├── features/               # 사용자 기능 (비즈니스 기능)
│   ├── attachment/
│   ├── memo/
│   └── notification/
├── entities/               # 비즈니스 엔티티
│   ├── user/
│   ├── case/
│   └── report/
├── shared/                 # 공유 리소스
│   ├── ui/                # UI 컴포넌트
│   │   ├── atoms/         # 기본 요소 (Button, Input, Label)
│   │   ├── molecules/     # 조합 컴포넌트 (FormField, SearchBox)
│   │   └── organisms/     # 복잡한 컴포넌트 (Modal, DataTable)
│   ├── api/               # API 클라이언트
│   ├── lib/               # 유틸리티, 시스템 클래스
│   │   ├── session-manager/
│   │   ├── permission-manager/
│   │   └── loading-manager/
│   └── types/             # TypeScript 타입 정의
├── public/                 # 정적 파일
├── i18n/                   # 다국어 번역 파일
├── nuxt.config.ts         # Nuxt 설정 (ssr: false 필수)
├── tailwind.config.js     # Tailwind 설정
└── package.json           # 의존성
```

## 개발 환경 설정

### 필수 요구사항
- Node.js 20.x 이상
- npm 10.x 이상

### 설치 및 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (http://localhost:3000)
npm run dev

# 프로덕션 빌드 (정적 파일 생성)
npm run build

# 빌드 결과물 미리보기
npm run preview

# 타입 체크
npm run typecheck

# Lint
npm run lint

# Lint 자동 수정
npm run lint:fix
```

### 환경 변수

`.env` 파일 생성:

```bash
# Backend API Base URL
NUXT_PUBLIC_API_BASE=http://localhost:8090/api/v1
```

프로덕션 환경:
```bash
NUXT_PUBLIC_API_BASE=https://api.inspecthub.example.com/api/v1
```

## 코딩 규칙

### 1. 컴포넌트 명명 규칙

```
atoms/      → PascalCase (Button.vue, Input.vue)
molecules/  → PascalCase (FormField.vue, SearchBox.vue)
organisms/  → PascalCase (Modal.vue, DataTable.vue)
widgets/    → AppPrefix (AppHeader.vue, AppSidebar.vue)
```

### 2. TypeScript 사용 필수

```vue
<script setup lang="ts">
// ✅ 모든 props, emits에 타입 정의
const props = defineProps<{
  title: string
  count?: number
}>()

const emit = defineEmits<{
  submit: [data: FormData]
  cancel: []
}>()
</script>
```

### 3. Tailwind CSS Prefix 사용

```vue
<template>
  <!-- ✅ Tailwind 클래스는 tw- prefix 사용 -->
  <div class="tw-flex tw-items-center tw-gap-4">
    <!-- PrimeVue/RealGrid 클래스는 prefix 없음 -->
    <Button label="저장" severity="primary" />
  </div>
</template>
```

**이유:** PrimeVue, RealGrid와 클래스 충돌 방지

### 4. API 호출 규칙

```typescript
// ✅ 올바른 방법 - shared/api/client.ts 사용
const api = useApiClient()
const data = await api.get('/users')

// ❌ 잘못된 방법 - 직접 $fetch 사용 지양
const data = await $fetch('/api/users')
```

### 5. 상태 관리 규칙

```typescript
// ✅ Pinia store 사용
const authStore = useAuthStore()
const user = computed(() => authStore.user)

// ❌ 전역 변수 사용 금지
let globalUser = null  // Bad
```

### 6. 권한 체크

```typescript
// ✅ PermissionManager 사용
const perm = usePermissionManager()

if (perm.hasRole('ROLE_ADMIN')) {
  // 관리자 전용 로직
}

if (perm.can('write', 'case-management')) {
  // 쓰기 권한이 있는 경우
}
```

## 테스트

### Unit Tests (Vitest)

```bash
# 단위 테스트 실행
npm run test

# Watch 모드
npm run test:watch

# UI 모드
npm run test:ui

# 커버리지 리포트
npm run test:coverage
```

### E2E Tests (Playwright)

```bash
# E2E 테스트 실행 (헤드리스)
npm run test:e2e

# UI 모드로 실행 (권장)
npm run test:e2e:ui

# 브라우저를 보면서 실행
npm run test:e2e:headed

# 디버그 모드
npm run test:e2e:debug

# 테스트 리포트 보기
npm run test:e2e:report
```

**E2E 테스트 구성:**
- **Login Tests** (9개): 로그인 페이지 렌더링, 폼 검증, 인증 처리
- **Dashboard Tests** (17개): 대시보드 표시, 통계 카드, 테이블, 네비게이션, 권한

**테스트 브라우저 (웹 브라우저 중심):**
- ✅ **Chromium (Desktop)** - 주요 테스트 브라우저
- ✅ **Firefox (Desktop)** - 크로스 브라우저 검증
- ✅ **WebKit/Safari (Desktop)** - Safari 호환성 검증
- ⚠️ Mobile Chrome - 선택적 (낮은 우선순위)
- ⚠️ Mobile Safari - 선택적 (낮은 우선순위)

**참고:** 모바일 브라우저 테스트는 반응형 디자인 검증 목적으로만 사용되며, 모바일 전용 기능 테스트는 포함되지 않습니다.

자세한 내용은 [tests/e2e/README.md](./tests/e2e/README.md) 참고.

## SSR 금지 확인 체크리스트

개발 중 다음 사항을 반드시 준수하세요:

- [ ] `nuxt.config.ts`에서 `ssr: false` 유지
- [ ] `/server` 디렉토리 생성 금지
- [ ] `useAsyncData`/`useFetch` 사용 시 클라이언트 전용인지 확인
- [ ] 모든 API 호출은 `shared/api/client.ts` 사용
- [ ] `process.server` 체크 코드 사용 금지
- [ ] Nitro 서버 기능 사용 금지
- [ ] 빌드 결과물이 정적 파일(.html, .js, .css)인지 확인

## 빌드 및 배포

### 빌드

```bash
npm run build
```

**빌드 결과물 위치:**
```
.output/
└── public/          # 이 디렉토리를 배포
    ├── _nuxt/       # JS, CSS 번들
    ├── index.html   # 메인 HTML
    └── ...
```

### 배포 (Nginx 예시)

```nginx
server {
    listen 80;
    server_name inspecthub.example.com;

    root /var/www/inspect-hub/frontend;
    index index.html;

    # SPA 라우팅 처리
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 정적 리소스 캐싱
    location /_nuxt/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 프록시 (선택사항)
    location /api/ {
        proxy_pass http://backend:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 참고 문서

- [COMPONENTS_ROADMAP.md](./COMPONENTS_ROADMAP.md) - 컴포넌트 구현 로드맵
- [Nuxt 3 Documentation](https://nuxt.com/docs)
- [PrimeVue Documentation](https://primevue.org/)
- [Tailwind CSS Documentation](https://tailwindcss.com/)
- [Pinia Documentation](https://pinia.vuejs.org/)

## 라이선스

Copyright © 2025 Inspect-Hub. All rights reserved.
