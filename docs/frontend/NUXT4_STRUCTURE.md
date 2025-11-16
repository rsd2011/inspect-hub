# Nuxt 4 공식 디렉토리 구조 가이드

> **Nuxt 4 Official Best Practices Structure**
>
> **Last Updated**: 2025-01-16
> **Status**: Active - 프로젝트 공식 아키텍처
> **Reference**: [Nuxt 4 Official Docs](https://nuxt.com/docs/4.x)

---

## 📋 개요

Inspect-Hub Frontend는 **Nuxt 4 공식 디렉토리 구조**를 따릅니다.

**핵심 원칙:**
- ✅ Nuxt 4 공식 베스트 프랙티스 준수
- ✅ Auto-Import 기능 최대 활용
- ✅ 간결하고 직관적인 구조
- ✅ TypeScript 타입 안전성
- ✅ SPA 모드 전용 (SSR 금지)

---

## 🗂️ 디렉토리 구조

### 전체 구조

```
frontend/
└── app/
    ├── components/        # 📦 모든 Vue 컴포넌트 (Auto-Import)
    │   ├── auth/         # Feature별 폴더 정리
    │   ├── user/
    │   ├── policy/
    │   ├── common/       # 공통 컴포넌트
    │   └── layout/       # 레이아웃 관련 컴포넌트
    │
    ├── composables/       # 🧩 모든 Composables (Auto-Import)
    │   ├── auth/
    │   ├── user/
    │   └── api/
    │
    ├── pages/             # 📄 페이지 라우트 (File-based Routing)
    │   ├── index.vue     # /
    │   ├── login.vue     # /login
    │   └── policy/
    │       └── index.vue # /policy
    │
    ├── layouts/           # 🎨 레이아웃 컴포넌트
    │   ├── default.vue   # 기본 레이아웃
    │   └── auth.vue      # 인증 레이아웃
    │
    ├── middleware/        # 🛡️ 라우트 미들웨어
    │   ├── auth.ts       # 인증 체크
    │   └── permission.ts # 권한 체크
    │
    ├── plugins/           # 🔌 Nuxt 플러그인
    │   ├── primevue.ts   # PrimeVue 설정
    │   ├── pinia.ts      # Pinia 설정
    │   └── i18n.ts       # i18n 설정
    │
    ├── utils/             # 🛠️ 유틸리티 함수 (Auto-Import)
    │   ├── format.ts     # 포맷팅
    │   ├── validation.ts # 검증
    │   └── constants.ts  # 상수
    │
    ├── assets/            # 🎭 정적 리소스
    │   ├── styles/       # CSS/SCSS
    │   ├── images/       # 이미지
    │   └── fonts/        # 폰트
    │
    ├── stores/            # 📊 Pinia Stores
    │   ├── auth.ts
    │   ├── user.ts
    │   └── policy.ts
    │
    ├── types/             # 📝 TypeScript 타입 정의
    │   ├── api.ts
    │   ├── models.ts
    │   └── components.ts
    │
    └── app.vue            # 🎯 루트 컴포넌트
```

---

## 📦 디렉토리별 상세 가이드

### 1. app/components/ - 컴포넌트

**목적:** 모든 Vue 컴포넌트를 저장하고 자동 임포트

**규칙:**
- ✅ 모든 `.vue` 파일은 자동으로 글로벌 컴포넌트로 등록
- ✅ Feature별 폴더로 정리 (auth/, user/, policy/)
- ✅ 공통 컴포넌트는 common/ 폴더
- ✅ PascalCase 네이밍 (예: LoginForm.vue, UserAvatar.vue)

**예시 구조:**
```
app/components/
├── auth/
│   ├── LoginForm.vue         # <AuthLoginForm />
│   ├── SsoLoginButton.vue    # <AuthSsoLoginButton />
│   └── PasswordInput.vue     # <AuthPasswordInput />
├── user/
│   ├── UserAvatar.vue        # <UserAvatar />
│   └── UserCard.vue          # <UserCard />
├── policy/
│   ├── PolicyList.vue        # <PolicyPolicyList />
│   └── PolicyEditor.vue      # <PolicyPolicyEditor />
└── common/
    ├── Button.vue            # <CommonButton />
    ├── Input.vue             # <CommonInput />
    └── Table.vue             # <CommonTable />
```

**사용 예시:**
```vue
<template>
  <div>
    <!-- ✅ Auto-Import - import 문 불필요 -->
    <AuthLoginForm @submit="handleLogin" />
    <UserAvatar :user="currentUser" />
    <CommonButton>로그인</CommonButton>
  </div>
</template>

<script setup lang="ts">
// ❌ import 문 불필요!
const currentUser = ref(null)
const handleLogin = () => { ... }
</script>
```

---

### 2. app/composables/ - Composables

**목적:** 재사용 가능한 Vue Composition API 로직

**규칙:**
- ✅ `use` 접두사 사용 (예: useAuth, useUser)
- ✅ Feature별 폴더로 정리
- ✅ 자동 임포트 (import 문 불필요)
- ✅ TypeScript 타입 정의 포함

**예시 구조:**
```
app/composables/
├── auth/
│   ├── useAuth.ts            # 인증 상태 관리
│   ├── useLogin.ts           # 로그인 로직
│   └── useLoginPolicy.ts     # 로그인 정책
├── user/
│   ├── useUser.ts            # 사용자 정보
│   └── usePermissions.ts     # 권한 관리
└── api/
    ├── useApi.ts             # API 클라이언트
    └── useFetch.ts           # 커스텀 Fetch
```

**useAuth.ts 예시:**
```typescript
// app/composables/auth/useAuth.ts
export const useAuth = () => {
  const authStore = useAuthStore()
  const router = useRouter()

  const login = async (credentials: LoginCredentials) => {
    const response = await $fetch('/api/v1/auth/login', {
      method: 'POST',
      body: credentials,
    })
    authStore.setTokens(response.accessToken, response.refreshToken)
    router.push('/')
  }

  const logout = () => {
    authStore.clearTokens()
    router.push('/login')
  }

  return {
    isAuthenticated: computed(() => authStore.isAuthenticated),
    user: computed(() => authStore.user),
    login,
    logout,
  }
}
```

**사용 예시:**
```vue
<script setup lang="ts">
// ✅ Auto-Import
const { isAuthenticated, user, login, logout } = useAuth()

const handleLogin = async () => {
  await login({ username: 'test', password: 'pass' })
}
</script>
```

---

### 3. app/pages/ - 페이지 라우트

**목적:** File-based Routing으로 페이지 정의

**규칙:**
- ✅ 파일명 = 라우트 경로
- ✅ index.vue = 기본 경로
- ✅ [id].vue = 동적 라우트
- ✅ kebab-case 또는 PascalCase

**라우팅 매핑:**
| 파일 경로 | URL |
|-----------|-----|
| `pages/index.vue` | `/` |
| `pages/login.vue` | `/login` |
| `pages/policy/index.vue` | `/policy` |
| `pages/policy/[id].vue` | `/policy/:id` |
| `pages/policy/create.vue` | `/policy/create` |
| `pages/user/[id]/settings.vue` | `/user/:id/settings` |

**페이지 예시:**
```vue
<!-- app/pages/policy/[id].vue -->
<template>
  <div>
    <h1>정책 상세: {{ policy?.name }}</h1>
    <PolicyEditor v-if="policy" :policy="policy" />
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const policyId = route.params.id

// ✅ Auto-Import composable
const { policy, loading } = usePolicy(policyId)
</script>
```

---

### 4. app/layouts/ - 레이아웃

**목적:** 페이지 공통 레이아웃 정의

**기본 레이아웃:**
```vue
<!-- app/layouts/default.vue -->
<template>
  <div class="app-layout">
    <AppHeader />
    <AppSidebar />
    <main class="app-content">
      <slot />  <!-- 페이지 내용 -->
    </main>
    <AppFooter />
  </div>
</template>
```

**페이지에서 사용:**
```vue
<!-- app/pages/policy/index.vue -->
<template>
  <div>
    <h1>정책 관리</h1>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default'  // layouts/default.vue 사용
})
</script>
```

---

### 5. app/middleware/ - 미들웨어

**목적:** 라우트 네비게이션 전 로직 실행

**인증 미들웨어 예시:**
```typescript
// app/middleware/auth.ts
export default defineNuxtRouteMiddleware((to, from) => {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated.value && to.path !== '/login') {
    return navigateTo(`/login?returnUrl=${to.fullPath}`)
  }

  if (isAuthenticated.value && to.path === '/login') {
    return navigateTo('/')
  }
})
```

**페이지에서 사용:**
```vue
<script setup lang="ts">
definePageMeta({
  middleware: ['auth']  // middleware/auth.ts 실행
})
</script>
```

---

### 6. app/plugins/ - 플러그인

**목적:** Nuxt 앱 초기화 시 실행되는 로직

**PrimeVue 플러그인 예시:**
```typescript
// app/plugins/primevue.ts
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(PrimeVue, { ripple: true })
  nuxtApp.vueApp.component('Button', Button)
  nuxtApp.vueApp.component('InputText', InputText)
})
```

---

### 7. app/utils/ - 유틸리티

**목적:** 순수 함수 유틸리티 (Auto-Import)

**예시:**
```typescript
// app/utils/format.ts
export const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR').format(date)
}

export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
  }).format(amount)
}
```

**사용 (Auto-Import):**
```vue
<script setup lang="ts">
// ✅ import 불필요
const formatted = formatDate(new Date())
const price = formatCurrency(10000)
</script>
```

---

### 8. app/stores/ - Pinia Stores

**목적:** 전역 상태 관리

**Auth Store 예시:**
```typescript
// app/stores/auth.ts
export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null as string | null,
    refreshToken: null as string | null,
    user: null as User | null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },

  actions: {
    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
    },

    clearTokens() {
      this.accessToken = null
      this.refreshToken = null
      this.user = null
    },
  },
})
```

---

## 🔧 nuxt.config.ts 설정

### 기본 설정

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  // ⚠️ SSR 금지 (프로젝트 요구사항)
  ssr: false,

  // 📁 Source Directory
  srcDir: 'app/',

  // 🎨 CSS
  css: [
    'primevue/resources/themes/lara-light-blue/theme.css',
    'primevue/resources/primevue.min.css',
    'primeicons/primeicons.css',
    '~/assets/styles/main.scss',
  ],

  // 🎯 Modules
  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
    '@nuxtjs/i18n',
  ],

  // 🌐 Tailwind CSS (tw- prefix)
  tailwindcss: {
    config: {
      prefix: 'tw-',
    },
  },

  // 🔧 TypeScript
  typescript: {
    strict: true,
    typeCheck: true,
  },

  // 🌍 i18n
  i18n: {
    locales: ['ko', 'en'],
    defaultLocale: 'ko',
  },

  // 🎨 Auto-Import 설정 (기본값 사용, 커스터마이징 불필요)
  components: true,  // app/components/ 자동 임포트
  imports: {
    autoImport: true,  // composables, utils 자동 임포트
  },
})
```

**주의:**
- ❌ FSD 구조와 달리 **커스터마이징 불필요**
- ✅ Nuxt 4 기본 설정으로 모든 Auto-Import 작동
- ✅ 간단하고 명확한 설정

---

## 📖 코딩 규칙

### 1. 컴포넌트 네이밍

**파일명: PascalCase**
```
✅ LoginForm.vue
✅ UserAvatar.vue
❌ loginForm.vue
❌ user-avatar.vue
```

**사용 시: PascalCase 또는 kebab-case**
```vue
<!-- 둘 다 가능 -->
<LoginForm />
<login-form />
```

---

### 2. Composable 네이밍

**파일명: use 접두사**
```
✅ useAuth.ts
✅ useUser.ts
❌ auth.ts
❌ Auth.ts
```

---

### 3. Import 규칙

**Auto-Import 최대 활용:**
```vue
<script setup lang="ts">
// ✅ Auto-Import (추천)
const { data } = await useFetch('/api/users')
const user = useUser()
const formatted = formatDate(new Date())

// ❌ 명시적 Import (불필요)
import { useFetch } from '#app'
import { useUser } from '~/composables/user/useUser'
import { formatDate } from '~/utils/format'
</script>
```

---

### 4. TypeScript 타입

**types/ 디렉토리에 정의:**
```typescript
// app/types/models.ts
export interface User {
  id: string
  username: string
  email: string
}

export interface LoginCredentials {
  username: string
  password: string
}
```

**사용 (Auto-Import):**
```typescript
// ✅ types/models.ts가 자동으로 글로벌 타입으로 등록됨
const user: User = { ... }
const credentials: LoginCredentials = { ... }
```

---

## 📊 Feature별 폴더 정리 가이드

### components/ 폴더 정리

```
app/components/
├── auth/                # 인증 관련
│   ├── LoginForm.vue
│   ├── SsoLogin.vue
│   └── PasswordReset.vue
├── user/                # 사용자 관련
│   ├── UserAvatar.vue
│   ├── UserCard.vue
│   └── UserSettings.vue
├── policy/              # 정책 관련
│   ├── PolicyList.vue
│   ├── PolicyEditor.vue
│   └── PolicyViewer.vue
├── detection/           # 탐지 관련
│   ├── DetectionList.vue
│   └── DetectionDetail.vue
└── common/              # 공통 컴포넌트
    ├── Button.vue
    ├── Input.vue
    ├── Table.vue
    ├── Modal.vue
    └── Toast.vue
```

**폴더명 = Feature 도메인**
- ✅ auth, user, policy, detection (도메인명)
- ✅ common (공통 컴포넌트)
- ❌ components, views (의미 없음)

---

### composables/ 폴더 정리

```
app/composables/
├── auth/
│   ├── useAuth.ts
│   ├── useLogin.ts
│   └── useLoginPolicy.ts
├── user/
│   ├── useUser.ts
│   └── usePermissions.ts
├── policy/
│   ├── usePolicy.ts
│   └── usePolicyList.ts
└── api/
    ├── useApi.ts
    └── useFetch.ts
```

---

## 🧪 테스트 구조

```
frontend/
├── app/
└── tests/
    ├── unit/
    │   ├── components/
    │   ├── composables/
    │   └── utils/
    └── e2e/
        ├── auth.spec.ts
        └── policy.spec.ts
```

---

## 🚀 개발 워크플로우

### 1. 새 Feature 추가

```bash
# 1. 컴포넌트 생성
touch app/components/myfeature/MyComponent.vue

# 2. Composable 생성
touch app/composables/myfeature/useMyFeature.ts

# 3. 페이지 생성
touch app/pages/myfeature/index.vue

# 4. 자동으로 사용 가능! (import 불필요)
```

### 2. 개발 서버 실행

```bash
cd frontend
npm run dev
```

### 3. 빌드 (SPA)

```bash
npm run build
# 결과: .output/public/ (정적 파일)
```

---

## 📚 참고 자료

### Nuxt 4 공식 문서

- [Directory Structure](https://nuxt.com/docs/4.x/guide/directory-structure)
- [Auto-imports](https://nuxt.com/docs/4.x/guide/concepts/auto-imports)
- [Components](https://nuxt.com/docs/4.x/guide/directory-structure/app/components)
- [Composables](https://nuxt.com/docs/4.x/guide/directory-structure/app/composables)
- [Pages](https://nuxt.com/docs/4.x/guide/directory-structure/app/pages)

### 프로젝트 문서

- [Frontend README.md](./README.md) - 전체 가이드
- [REFACTORING_PLAN.md](./REFACTORING_PLAN.md) - 리팩터링 계획

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-16 | 초안 생성 - Nuxt 4 공식 구조 가이드 | PM |

---

## 📞 문의

**Nuxt 4 구조 관련 문의:**
- GitHub Issues: #nuxt4-structure
- Slack: #inspect-hub-frontend
