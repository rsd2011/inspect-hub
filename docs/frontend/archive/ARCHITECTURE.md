# Frontend Architecture Guide

> **Nuxt 4 + Feature-Sliced Design (FSD) + Atomic Design 아키텍처**

## 📚 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [디렉토리 구조](#디렉토리-구조)
3. [FSD 레이어 상세](#fsd-레이어-상세)
4. [Nuxt 4 표준 디렉토리](#nuxt-4-표준-디렉토리)
5. [파일 명명 규칙](#파일-명명-규칙)
6. [Import 규칙](#import-규칙)
7. [마이그레이션 가이드](#마이그레이션-가이드)

---

## 아키텍처 개요

### 핵심 원칙

이 프로젝트는 **Feature-Sliced Design (FSD)** 아키텍처와 **Atomic Design** 패턴을 결합하여 사용합니다.

**FSD의 핵심:**
- **수직 분할 (Vertical Slices)**: 비즈니스 기능별로 코드 구성
- **수평 계층 (Horizontal Layers)**: 추상화 수준별로 레이어 분리
- **단방향 의존성**: 상위 레이어만 하위 레이어에 의존

**레이어 구조 (상위 → 하위):**
```
app/        → 애플리케이션 초기화 (최상위)
pages/      → 페이지 라우트
widgets/    → 독립적인 UI 블록
features/   → 사용자 시나리오 (비즈니스 기능)
entities/   → 비즈니스 엔티티
shared/     → 재사용 가능한 공통 코드 (최하위)
```

### Atomic Design 통합

`shared/ui/` 내부에서만 Atomic Design 적용:
- **atoms/**: 기본 UI 요소 (Button, Input, Label)
- **molecules/**: atoms 조합 (FormField, SearchBox)
- **organisms/**: 복잡한 UI 블록 (Modal, DataTable)

---

## 디렉토리 구조

### 완전한 구조

```
frontend/
├── app/                        # 🔵 Nuxt 4 표준 - 애플리케이션 계층
│   ├── config/                 # 앱 설정 (feature flags, constants)
│   ├── layouts/                # 레이아웃 컴포넌트
│   │   └── default.vue
│   ├── plugins/                # Nuxt 플러그인
│   ├── providers/              # 전역 프로바이더 (PrimeVue, Auth)
│   │   ├── auth.ts
│   │   └── primevue.ts
│   ├── styles/                 # 전역 스타일
│   │   ├── main.css
│   │   ├── tailwind.css
│   │   └── fonts.css
│   └── app.config.ts           # 앱 런타임 설정
│
├── pages/                      # 🟢 FSD - 페이지 라우트 (Nuxt auto-routing)
│   ├── index.vue               # 홈 페이지
│   ├── login.vue               # 로그인 페이지
│   ├── dashboard.vue           # 대시보드
│   └── cases/                  # 케이스 관리
│       ├── index.vue           # 목록 페이지
│       ├── [id].vue            # 상세 페이지
│       └── new.vue             # 생성 페이지
│
├── widgets/                    # 🟡 FSD - 위젯 (대형 독립 UI 블록)
│   ├── header/
│   │   ├── AppHeader.vue       # 헤더 컴포넌트
│   │   ├── model/              # 상태 관리
│   │   │   └── useHeaderState.ts
│   │   └── ui/                 # 내부 UI 컴포넌트
│   ├── sidebar/
│   │   ├── AppSidebar.vue
│   │   ├── model/
│   │   └── ui/
│   └── menu-navigation/
│       └── MenuNavigation.vue
│
├── features/                   # 🟠 FSD - 사용자 기능 (비즈니스 시나리오)
│   ├── auth/                   # 인증 기능
│   │   ├── api/                # API 호출
│   │   │   └── authApi.ts
│   │   ├── model/              # 상태 + 비즈니스 로직
│   │   │   ├── useAuthStore.ts (Pinia)
│   │   │   └── useAuthForm.ts (Composable)
│   │   ├── ui/                 # 기능별 UI
│   │   │   ├── LoginForm.vue
│   │   │   └── LogoutButton.vue
│   │   └── index.ts            # Public API
│   ├── notification/           # 알림 기능
│   │   ├── api/
│   │   ├── model/
│   │   └── ui/
│   └── attachment/             # 첨부파일 기능
│       ├── api/
│       ├── model/
│       └── ui/
│
├── entities/                   # 🔴 FSD - 비즈니스 엔티티
│   ├── user/                   # 사용자 엔티티
│   │   ├── api/
│   │   │   └── userApi.ts
│   │   ├── model/
│   │   │   ├── types.ts        # User interface
│   │   │   └── useUserStore.ts
│   │   ├── ui/
│   │   │   ├── UserCard.vue
│   │   │   └── UserAvatar.vue
│   │   └── index.ts
│   ├── case/                   # 케이스 엔티티
│   │   ├── api/
│   │   ├── model/
│   │   │   └── types.ts        # Case interface
│   │   ├── ui/
│   │   └── index.ts
│   └── report/                 # 리포트 엔티티
│       ├── api/
│       ├── model/
│       └── ui/
│
├── shared/                     # ⚪ FSD - 공유 리소스 (최하위 레이어)
│   ├── api/                    # API 클라이언트
│   │   ├── client.ts           # BaseApiClient 구현
│   │   ├── types.ts            # API 공통 타입
│   │   └── index.ts
│   ├── config/                 # 공유 설정
│   │   └── constants.ts
│   ├── lib/                    # 유틸리티 & 시스템 클래스
│   │   ├── composables/        # 공유 컴포저블
│   │   │   ├── useDebounce.ts
│   │   │   └── useBreakpoints.ts
│   │   ├── utils/              # 순수 함수
│   │   │   ├── formatDate.ts
│   │   │   └── validators.ts
│   │   ├── session-manager/
│   │   │   └── SessionManager.ts
│   │   ├── permission-manager/
│   │   │   └── PermissionManager.ts
│   │   └── loading-manager/
│   │       └── LoadingManager.ts
│   ├── types/                  # TypeScript 타입 정의
│   │   ├── api.ts              # API 관련 타입
│   │   ├── common.ts           # 공통 타입
│   │   └── index.ts
│   └── ui/                     # ⚛️ Atomic Design UI 컴포넌트
│       ├── atoms/              # 기본 요소
│       │   ├── Button/
│       │   │   ├── Button.vue
│       │   │   ├── Button.spec.ts
│       │   │   └── index.ts
│       │   ├── Input/
│       │   ├── Label/
│       │   └── index.ts
│       ├── molecules/          # 조합 컴포넌트
│       │   ├── FormField/
│       │   │   ├── FormField.vue
│       │   │   ├── FormField.spec.ts
│       │   │   └── index.ts
│       │   ├── SearchBox/
│       │   └── index.ts
│       └── organisms/          # 복잡한 컴포넌트
│           ├── Modal/
│           │   ├── Modal.vue
│           │   ├── Modal.spec.ts
│           │   └── index.ts
│           ├── DataTable/
│           └── index.ts
│
├── middleware/                 # 🔵 Nuxt 표준 - 라우트 미들웨어
│   ├── auth.ts                 # 인증 미들웨어
│   └── auth.global.ts          # 전역 미들웨어
│
├── i18n/                       # 🌐 다국어 지원
│   └── locales/
│       ├── ko.json
│       └── en.json
│
├── public/                     # 정적 파일
│   ├── favicon.ico
│   └── mockServiceWorker.js
│
├── tests/                      # 테스트
│   ├── e2e/                    # E2E 테스트 (Playwright)
│   │   ├── login.spec.ts
│   │   └── dashboard.spec.ts
│   ├── mocks/                  # MSW 모킹
│   │   ├── browser.ts
│   │   └── handlers.ts
│   └── setup.ts                # 테스트 설정
│
├── scripts/                    # 빌드 스크립트
│   ├── generate-component.mjs
│   └── validate-build.mjs
│
├── nuxt.config.ts              # Nuxt 설정
├── tailwind.config.js          # Tailwind 설정
├── tsconfig.json               # TypeScript 설정
├── vitest.config.ts            # Vitest 설정
├── playwright.config.ts        # Playwright 설정
└── package.json
```

---

## FSD 레이어 상세

### 1️⃣ shared/ (최하위 레이어)

**목적:** 프로젝트 전체에서 재사용 가능한 공통 코드

**내용:**
- **api/**: API 클라이언트 베이스
- **config/**: 공유 상수, 환경 변수
- **lib/**: 유틸리티, 컴포저블, 시스템 클래스
- **types/**: 공통 TypeScript 타입
- **ui/**: Atomic Design UI 컴포넌트

**규칙:**
- ❌ 비즈니스 로직 포함 금지
- ❌ 다른 레이어 import 금지
- ✅ 순수 함수, 재사용 가능한 컴포넌트만

**예시:**
```typescript
// shared/lib/utils/formatDate.ts
export function formatDate(date: Date): string {
  return date.toLocaleDateString('ko-KR')
}

// shared/ui/atoms/Button/Button.vue
<template>
  <button :class="buttonClass" @click="$emit('click')">
    <slot />
  </button>
</template>
```

---

### 2️⃣ entities/ (비즈니스 엔티티)

**목적:** 비즈니스 도메인의 핵심 엔티티 (User, Case, Report 등)

**구조:**
```
entities/
└── user/
    ├── api/                # 엔티티 CRUD API
    │   └── userApi.ts
    ├── model/              # 타입, 스토어, 비즈니스 로직
    │   ├── types.ts
    │   └── useUserStore.ts
    ├── ui/                 # 엔티티 표시 UI
    │   ├── UserCard.vue
    │   └── UserAvatar.vue
    └── index.ts            # Public API
```

**규칙:**
- ✅ CRUD API 정의
- ✅ 엔티티 타입 정의 (interface User)
- ✅ 엔티티 표시용 UI 컴포넌트
- ❌ 사용자 시나리오 (로그인, 회원가입) 로직 포함 금지 → `features/`로

**예시:**
```typescript
// entities/user/model/types.ts
export interface User {
  id: string
  name: string
  email: string
  role: UserRole
}

// entities/user/api/userApi.ts
export const userApi = {
  async getUser(id: string): Promise<User> {
    const api = useApiClient()
    return await api.get(`/users/${id}`)
  }
}

// entities/user/ui/UserCard.vue
<template>
  <div class="tw-border tw-p-4 tw-rounded">
    <h3>{{ user.name }}</h3>
    <p>{{ user.email }}</p>
  </div>
</template>
```

---

### 3️⃣ features/ (사용자 기능)

**목적:** 사용자 시나리오, 비즈니스 기능 (로그인, 알림, 첨부파일 등)

**구조:**
```
features/
└── auth/
    ├── api/                # 인증 관련 API
    │   └── authApi.ts
    ├── model/              # 상태 관리 + 비즈니스 로직
    │   ├── useAuthStore.ts (Pinia)
    │   └── useAuthForm.ts  (Composable)
    ├── ui/                 # 기능별 UI
    │   ├── LoginForm.vue
    │   └── LogoutButton.vue
    └── index.ts            # Public API
```

**규칙:**
- ✅ `entities/`의 엔티티 사용 가능
- ✅ `shared/`의 공통 코드 사용 가능
- ❌ 다른 `features/` import 금지
- ❌ `pages/`, `widgets/` import 금지

**예시:**
```typescript
// features/auth/model/useAuthStore.ts
import { defineStore } from 'pinia'
import type { User } from '~/entities/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const isAuthenticated = computed(() => !!user.value)

  async function login(email: string, password: string) {
    // 로그인 로직
  }

  return { user, isAuthenticated, login }
})

// features/auth/ui/LoginForm.vue
<script setup lang="ts">
import { useAuthStore } from '../model/useAuthStore'
import { Button } from '~/shared/ui/atoms'

const authStore = useAuthStore()
const email = ref('')
const password = ref('')

async function handleSubmit() {
  await authStore.login(email.value, password.value)
}
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <input v-model="email" type="email" />
    <input v-model="password" type="password" />
    <Button type="submit">로그인</Button>
  </form>
</template>
```

---

### 4️⃣ widgets/ (위젯)

**목적:** 독립적인 대형 UI 블록 (Header, Sidebar, Footer 등)

**특징:**
- 여러 `features/` 조합 가능
- 페이지에서 재사용 가능한 복합 컴포넌트

**구조:**
```
widgets/
└── header/
    ├── AppHeader.vue       # 메인 컴포넌트
    ├── model/              # 위젯 상태
    │   └── useHeaderState.ts
    └── ui/                 # 내부 UI
        ├── UserMenu.vue
        └── Notifications.vue
```

**예시:**
```vue
<!-- widgets/header/AppHeader.vue -->
<script setup lang="ts">
import { useAuthStore } from '~/features/auth'
import { UserMenu } from './ui/UserMenu.vue'
import { Notifications } from '~/features/notification'

const authStore = useAuthStore()
</script>

<template>
  <header class="tw-flex tw-justify-between tw-p-4">
    <div class="tw-logo">Logo</div>
    <div class="tw-flex tw-gap-4">
      <Notifications />
      <UserMenu :user="authStore.user" />
    </div>
  </header>
</template>
```

---

### 5️⃣ pages/ (페이지)

**목적:** 라우트별 페이지 컴포넌트 (Nuxt auto-routing)

**특징:**
- Nuxt가 자동으로 라우트 생성
- 모든 하위 레이어 사용 가능

**예시:**
```vue
<!-- pages/dashboard.vue -->
<script setup lang="ts">
import { AppHeader } from '~/widgets/header'
import { AppSidebar } from '~/widgets/sidebar'
import { CaseList } from '~/features/case-management'

definePageMeta({
  middleware: 'auth'
})
</script>

<template>
  <div class="tw-min-h-screen">
    <AppHeader />
    <div class="tw-flex">
      <AppSidebar />
      <main class="tw-flex-1">
        <CaseList />
      </main>
    </div>
  </div>
</template>
```

---

### 6️⃣ app/ (애플리케이션 계층)

**목적:** 전역 설정, 프로바이더, 레이아웃

**내용:**
- **config/**: 앱 설정
- **layouts/**: 레이아웃
- **plugins/**: Nuxt 플러그인
- **providers/**: 전역 프로바이더 (PrimeVue, Auth)
- **styles/**: 전역 스타일

**예시:**
```typescript
// app/providers/primevue.ts
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.use(PrimeVue, {
    theme: {
      preset: Aura
    }
  })
})

// app/layouts/default.vue
<template>
  <div>
    <AppHeader />
    <slot />
    <AppFooter />
  </div>
</template>
```

---

## Nuxt 4 표준 디렉토리

### 주요 변경사항

**Nuxt 4.x 새로운 기본 구조:**
- `app/` 디렉토리가 새로운 `srcDir` (기본값)
- TypeScript 프로젝트 레퍼런스 지원
- 향상된 모듈 로딩 순서

**tsconfig.json 구조:**
```json
{
  "files": [],
  "references": [
    { "path": "./.nuxt/tsconfig.app.json" },
    { "path": "./.nuxt/tsconfig.server.json" },
    { "path": "./.nuxt/tsconfig.shared.json" },
    { "path": "./.nuxt/tsconfig.node.json" }
  ]
}
```

**`.gitignore` 필수 항목:**
```gitignore
# Nuxt dev/build outputs
.output
.data
.nuxt
.nitro
.cache
dist

# Node dependencies
node_modules

# Logs
*.log

# Misc
.DS_Store

# Local env files
.env
.env.*
!.env.example
```

---

## 파일 명명 규칙

### 컴포넌트

| 위치 | 명명 규칙 | 예시 |
|------|-----------|------|
| **shared/ui/atoms/** | PascalCase | `Button.vue`, `Input.vue` |
| **shared/ui/molecules/** | PascalCase | `FormField.vue`, `SearchBox.vue` |
| **shared/ui/organisms/** | PascalCase | `Modal.vue`, `DataTable.vue` |
| **widgets/** | App 접두사 | `AppHeader.vue`, `AppSidebar.vue` |
| **features/*/ui/** | PascalCase | `LoginForm.vue`, `LogoutButton.vue` |
| **entities/*/ui/** | 엔티티명 접두사 | `UserCard.vue`, `CaseListItem.vue` |

### TypeScript 파일

| 타입 | 명명 규칙 | 예시 |
|------|-----------|------|
| **API** | camelCase + Api 접미사 | `userApi.ts`, `authApi.ts` |
| **Store** | use + PascalCase + Store | `useAuthStore.ts`, `useUserStore.ts` |
| **Composable** | use + PascalCase | `useDebounce.ts`, `usePermission.ts` |
| **Utils** | camelCase | `formatDate.ts`, `validators.ts` |
| **Types** | PascalCase (interface) | `User`, `Case`, `ApiResponse` |

### 디렉토리

| 레이어 | 명명 규칙 | 예시 |
|--------|-----------|------|
| **features/** | kebab-case | `auth/`, `case-management/`, `notification/` |
| **entities/** | kebab-case | `user/`, `case/`, `report/` |
| **widgets/** | kebab-case | `header/`, `sidebar/`, `menu-navigation/` |

---

## Import 규칙

### FSD 레이어 의존성 규칙

**허용되는 import:**
```
app/     → pages/, widgets/, features/, entities/, shared/
pages/   → widgets/, features/, entities/, shared/
widgets/ → features/, entities/, shared/
features/ → entities/, shared/
entities/ → shared/
shared/   → (없음)
```

**금지된 import:**
```
❌ shared/   → 다른 모든 레이어
❌ entities/ → features/, widgets/, pages/
❌ features/ → features/ (서로 다른 feature 간)
```

### Import 경로 별칭

```typescript
// Nuxt auto-import (권장)
import { useAuthStore } from '~/features/auth'
import { Button } from '~/shared/ui/atoms'

// 상대 경로 (같은 feature 내부에서만)
import { useAuthForm } from '../model/useAuthForm'

// features/ 간 import (❌ 금지)
// import { useNotificationStore } from '~/features/notification'
```

### Public API 패턴

각 feature/entity는 `index.ts`를 통해 Public API 노출:

```typescript
// features/auth/index.ts
export { useAuthStore } from './model/useAuthStore'
export { LoginForm, LogoutButton } from './ui'
export type { AuthCredentials } from './model/types'

// 사용처
import { useAuthStore, LoginForm } from '~/features/auth'
```

---

## 마이그레이션 가이드

### 현재 → 신규 구조

#### 1단계: entities/ 생성

```bash
# 새 디렉토리 생성
mkdir -p entities/user/{api,model,ui}
mkdir -p entities/case/{api,model,ui}
mkdir -p entities/report/{api,model,ui}
```

**이동할 코드:**
- `features/auth/model/types.ts` → `entities/user/model/types.ts` (User interface)
- API CRUD 메서드 → `entities/*/api/`

#### 2단계: composables/ 이동

```bash
# composables/ 내용을 shared/lib/composables/로 이동
mv composables/* shared/lib/composables/
rm -rf composables/
```

#### 3단계: types/ 통합

```bash
# types/ 내용을 shared/types/로 통합
cp -r types/* shared/types/
rm -rf types/
```

#### 4단계: Import 경로 수정

```typescript
// 변경 전
import { useDebounce } from '~/composables/useDebounce'
import type { User } from '~/types/user'

// 변경 후
import { useDebounce } from '~/shared/lib/composables/useDebounce'
import type { User } from '~/entities/user'
```

### 마이그레이션 체크리스트

- [ ] `entities/` 디렉토리 생성 및 엔티티 이동
- [ ] `composables/` → `shared/lib/composables/` 이동
- [ ] `types/` → `shared/types/` 통합
- [ ] Import 경로 전체 수정
- [ ] 테스트 실행 및 검증
- [ ] TypeScript 에러 수정
- [ ] ESLint/Prettier 실행
- [ ] 문서 업데이트

---

## 참고 문서

- **[Feature-Sliced Design 공식 문서](https://feature-sliced.design/)**
- **[Nuxt 4.x 공식 문서](https://nuxt.com/docs/4.x)**
- **[Atomic Design 원칙](https://bradfrost.com/blog/post/atomic-web-design/)**
- **[Frontend README](./README.md)** - SPA 제약사항, 배포 방식
- **[Components Roadmap](./COMPONENTS_ROADMAP.md)** - 컴포넌트 구현 현황

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-15 | 초안 작성 - Nuxt 4 + FSD 아키텍처 가이드 | PM |
