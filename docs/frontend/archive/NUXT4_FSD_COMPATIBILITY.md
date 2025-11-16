# Nuxt 4 + FSD 호환성 가이드

> **Nuxt 4 공식 디렉토리 구조와 Feature-Sliced Design (FSD) 통합 방법**
>
> **Last Updated**: 2025-01-16
> **Status**: Production Ready

---

## 📋 개요

### 문제점

**Nuxt 4의 기본 디렉토리 구조:**
```
app/
├── components/    # 🔴 컴포넌트 자동 임포트
├── composables/   # 🔴 Composables 자동 임포트
├── pages/
├── layouts/
└── utils/         # 🔴 Utils 자동 임포트
```

**Feature-Sliced Design (FSD) 구조:**
```
app/
pages/
widgets/
features/
  └── auth/
      ├── ui/         # 🟡 컴포넌트가 여기에!
      └── model/      # 🟡 Composables가 여기에!
entities/
  └── user/
      ├── ui/         # 🟡 컴포넌트가 여기에!
      └── model/      # 🟡 Composables가 여기에!
shared/
  ├── ui/            # 🟡 공통 컴포넌트
  └── lib/           # 🟡 공통 Utils
```

**충돌 지점:**
- ❌ Nuxt 4는 `app/components/`를 기대하지만, FSD는 `features/*/ui/` 등으로 분산
- ❌ Auto-import가 작동하지 않음
- ❌ TypeScript 타입 인식 실패

---

## ✅ 해결 방법

**Nuxt 4는 완전히 커스터마이징 가능합니다!**

`nuxt.config.ts`에서 `components`와 `imports.dirs`를 설정하여 FSD 구조를 Nuxt 4의 auto-import 시스템과 통합할 수 있습니다.

---

## 🔧 nuxt.config.ts 완전한 설정

### 1. 컴포넌트 Auto-Import (FSD 호환)

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  ssr: false, // ⚠️ 프로젝트 요구사항: SSR 금지

  // 🎨 컴포넌트 Auto-Import 설정
  components: [
    // ✅ FSD: features/**/ui/
    {
      path: '~/features',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false, // <AuthLoginForm> (AuthLoginForm만 사용)
    },

    // ✅ FSD: widgets/**/ui/
    {
      path: '~/widgets',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false,
    },

    // ✅ FSD: entities/**/ui/
    {
      path: '~/entities',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false,
    },

    // ✅ FSD: shared/ui/ (Atomic Design)
    {
      path: '~/shared/ui/atoms',
      pathPrefix: false,
    },
    {
      path: '~/shared/ui/molecules',
      pathPrefix: false,
    },
    {
      path: '~/shared/ui/organisms',
      pathPrefix: false,
    },

    // ✅ Nuxt 기본 (app/components/ - 필요 시)
    {
      path: '~/app/components',
      pathPrefix: false,
    },
  ],

  // ... 다음 섹션 계속
})
```

**설명:**
- `pattern: '**/ui/**/*.vue'` - FSD의 `ui/` 폴더만 스캔
- `pathPrefix: false` - 컴포넌트 이름에 경로 프리픽스 제거
  - ❌ `<FeaturesAuthUiLoginForm>` (나쁨)
  - ✅ `<LoginForm>` (좋음)

---

### 2. Composables & Utils Auto-Import (FSD 호환)

```typescript
// nuxt.config.ts (계속)
export default defineNuxtConfig({
  // ... 위에서 정의한 components

  // 🧩 Composables & Utils Auto-Import 설정
  imports: {
    dirs: [
      // ✅ FSD: features/**/model/
      'features/*/model',
      'features/*/model/composables',

      // ✅ FSD: entities/**/model/
      'entities/*/model',
      'entities/*/model/composables',

      // ✅ FSD: shared/lib/
      'shared/lib/composables',
      'shared/lib/utils',
      'shared/lib/helpers',

      // ✅ Nuxt 기본 (app/composables/ - 필요 시)
      'app/composables',
      'app/utils',
    ],
  },
})
```

**설명:**
- `features/*/model` - 각 feature의 composables (예: `useAuth`, `useLoginPolicy`)
- `entities/*/model` - 각 entity의 composables (예: `useUser`, `usePolicy`)
- `shared/lib/composables` - 공통 composables (예: `useApi`, `useFetch`)
- `shared/lib/utils` - 공통 utils (예: `formatDate`, `validateEmail`)

---

### 3. 전체 nuxt.config.ts 예시

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  // ⚠️ SSR 금지 (프로젝트 요구사항)
  ssr: false,

  // 📁 Source Directory
  srcDir: './',

  // 🎨 컴포넌트 Auto-Import (FSD 호환)
  components: [
    // FSD features/
    {
      path: '~/features',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false,
    },
    // FSD widgets/
    {
      path: '~/widgets',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false,
    },
    // FSD entities/
    {
      path: '~/entities',
      pattern: '**/ui/**/*.vue',
      pathPrefix: false,
    },
    // FSD shared/ui/ (Atomic Design)
    {
      path: '~/shared/ui/atoms',
      pathPrefix: false,
    },
    {
      path: '~/shared/ui/molecules',
      pathPrefix: false,
    },
    {
      path: '~/shared/ui/organisms',
      pathPrefix: false,
    },
  ],

  // 🧩 Composables/Utils Auto-Import (FSD 호환)
  imports: {
    dirs: [
      // FSD features/
      'features/*/model',
      'features/*/model/composables',

      // FSD entities/
      'entities/*/model',
      'entities/*/model/composables',

      // FSD shared/
      'shared/lib/composables',
      'shared/lib/utils',
      'shared/lib/helpers',
    ],
  },

  // 🎯 Modules
  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
    '@nuxtjs/i18n',
    // ... 기타 모듈
  ],

  // 🔧 TypeScript
  typescript: {
    strict: true,
    typeCheck: true,
  },

  // 🌐 Vite 설정
  vite: {
    resolve: {
      alias: {
        '@': '/',
        '~': '/',
      },
    },
  },
})
```

---

## 📖 사용 예시

### 컴포넌트 Auto-Import

**파일 구조:**
```
features/
  └── auth/
      └── ui/
          ├── LoginForm.vue
          └── SsoLoginButton.vue

entities/
  └── user/
      └── ui/
          ├── UserAvatar.vue
          └── UserCard.vue

shared/
  └── ui/
      ├── atoms/
      │   └── Button.vue
      └── molecules/
          └── FormInput.vue
```

**사용 (자동 임포트):**
```vue
<template>
  <div>
    <!-- ✅ features/auth/ui/LoginForm.vue -->
    <LoginForm />

    <!-- ✅ entities/user/ui/UserAvatar.vue -->
    <UserAvatar :user="currentUser" />

    <!-- ✅ shared/ui/atoms/Button.vue -->
    <Button>로그인</Button>

    <!-- ✅ shared/ui/molecules/FormInput.vue -->
    <FormInput v-model="username" />
  </div>
</template>

<script setup lang="ts">
// ❌ import 문 불필요! (자동 임포트)
const currentUser = ref(null)
</script>
```

---

### Composables Auto-Import

**파일 구조:**
```
features/
  └── auth/
      └── model/
          ├── useAuth.ts
          └── useLoginPolicy.ts

entities/
  └── user/
      └── model/
          └── useUser.ts

shared/
  └── lib/
      ├── composables/
      │   ├── useApi.ts
      │   └── useFetch.ts
      └── utils/
          ├── formatDate.ts
          └── validateEmail.ts
```

**사용 (자동 임포트):**
```vue
<script setup lang="ts">
// ✅ features/auth/model/useAuth.ts
const { login, logout, isAuthenticated } = useAuth()

// ✅ features/auth/model/useLoginPolicy.ts
const { policy, enabledMethods } = useLoginPolicy()

// ✅ entities/user/model/useUser.ts
const { currentUser, updateUser } = useUser()

// ✅ shared/lib/composables/useApi.ts
const api = useApi()

// ✅ shared/lib/utils/formatDate.ts
const formattedDate = formatDate(new Date())

// ✅ shared/lib/utils/validateEmail.ts
const isValid = validateEmail('test@example.com')
</script>
```
---

## 🔍 TypeScript 타입 인식 확인

### 자동 생성된 타입 파일 확인

Nuxt 4는 auto-import된 컴포넌트와 composables의 타입을 자동 생성합니다.

```bash
# 컴포넌트 타입 확인
cat .nuxt/types/components.d.ts

# Composables 타입 확인
cat .nuxt/types/imports.d.ts
```

**예시 출력 (.nuxt/types/components.d.ts):**
```typescript
declare module 'vue' {
  export interface GlobalComponents {
    LoginForm: typeof import('../../features/auth/ui/LoginForm.vue')['default']
    UserAvatar: typeof import('../../entities/user/ui/UserAvatar.vue')['default']
    Button: typeof import('../../shared/ui/atoms/Button.vue')['default']
    // ... 기타
  }
}
```

---

## ⚠️ 주의사항

### 1. 컴포넌트 이름 충돌 방지

**문제:**
```
features/auth/ui/LoginForm.vue
features/sso/ui/LoginForm.vue  # ❌ 이름 충돌!
```

**해결:**
```
features/auth/ui/AuthLoginForm.vue       # ✅ 프리픽스 추가
features/sso/ui/SsoLoginForm.vue         # ✅ 프리픽스 추가
```

또는 `pathPrefix: true` 사용:
```typescript
{
  path: '~/features',
  pattern: '**/ui/**/*.vue',
  pathPrefix: true,  // <FeaturesAuthLoginForm>
}
```

---

### 2. Composables 이름 충돌 방지

**문제:**
```
features/auth/model/useUser.ts
entities/user/model/useUser.ts  # ❌ 이름 충돌!
```

**해결:**
```typescript
// features/auth/model/useAuthUser.ts
export const useAuthUser = () => { ... }

// entities/user/model/useUser.ts
export const useUser = () => { ... }
```

---

### 3. 성능 최적화

**너무 많은 디렉토리 스캔 시 빌드 성능 저하:**
```typescript
// ❌ 나쁨 (모든 하위 디렉토리 스캔)
imports: {
  dirs: ['features/**']
}

// ✅ 좋음 (필요한 디렉토리만 명시)
imports: {
  dirs: [
    'features/*/model',
    'features/*/model/composables',
  ]
}
```

---

## 🧪 검증 방법

### 1. 빌드 시 경고 확인

```bash
npm run build
```

**예상 출력:**
```
✓ Components scanned: 42 components
✓ Imports scanned: 28 composables
✓ TypeScript types generated
```

---

### 2. 개발 서버에서 테스트

```bash
npm run dev
```

**브라우저에서 확인:**
- 컴포넌트가 정상적으로 렌더링되는지
- TypeScript 에러가 없는지 (VSCode)
- Hot Module Replacement (HMR) 작동하는지

---

### 3. TypeScript 타입 검사

```bash
npm run typecheck
```

**예상 출력:**
```
✓ No TypeScript errors found
```

---

## 📊 FSD vs Nuxt 4 구조 비교

| 항목 | Nuxt 4 기본 | FSD 구조 | 통합 방법 |
|------|------------|----------|-----------|
| **컴포넌트** | `app/components/` | `features/*/ui/`, `entities/*/ui/`, `shared/ui/` | `components` 배열 설정 |
| **Composables** | `app/composables/` | `features/*/model/`, `entities/*/model/`, `shared/lib/composables/` | `imports.dirs` 설정 |
| **Utils** | `app/utils/` | `shared/lib/utils/` | `imports.dirs` 설정 |
| **Pages** | `app/pages/` | `pages/` | ✅ 동일 (변경 불필요) |
| **Layouts** | `app/layouts/` | `app/layouts/` | ✅ 동일 (변경 불필요) |
| **Middleware** | `app/middleware/` | `app/middleware/` | ✅ 동일 (변경 불필요) |

---

## 🎯 권장 사항

### ✅ 추천: FSD + Nuxt 4 통합

**이유:**
1. **확장성**: Feature별 디렉토리로 대규모 프로젝트 관리 용이
2. **캡슐화**: Feature 간 의존성 명확히 관리
3. **재사용성**: shared/ 레이어로 공통 코드 관리
4. **Nuxt 4 호환**: `nuxt.config.ts` 설정으로 완벽 통합

**단점:**
1. **초기 설정**: `nuxt.config.ts` 설정 필요
2. **러닝 커브**: FSD 개념 이해 필요
3. **빌드 시간**: 약간 증가 (디렉토리 스캔 범위 증가)

---

### ❌ 비추천: Nuxt 4 기본 구조만 사용

**이유:**
1. **확장성 부족**: `app/components/`에 모든 컴포넌트 → 관리 어려움
2. **캡슐화 부족**: Feature 간 의존성 관리 어려움
3. **재사용성 부족**: 공통 코드 분리 어려움

**이 프로젝트에는 부적합:**
- 대규모 엔터프라이즈 시스템 (수백 개 컴포넌트)
- 장기 유지보수 필요
- 팀 협업 (Feature별 개발)

---

## 📚 참고 자료

### Nuxt 4 공식 문서

- [Components Directory](https://nuxt.com/docs/4.x/guide/directory-structure/app/components)
- [Composables Directory](https://nuxt.com/docs/4.x/guide/directory-structure/app/composables)
- [Auto-Imports](https://nuxt.com/docs/4.x/guide/concepts/auto-imports)
- [Nuxt Config - components](https://nuxt.com/docs/4.x/api/configuration/nuxt-config#components)
- [Nuxt Config - imports](https://nuxt.com/docs/4.x/api/configuration/nuxt-config#imports)

### Feature-Sliced Design 공식 문서

- [FSD Official](https://feature-sliced.design/)
- [FSD Examples](https://github.com/feature-sliced/examples)

### 관련 프로젝트 문서

- [Frontend ARCHITECTURE.md](./ARCHITECTURE.md) - FSD 아키텍처 상세 가이드
- [Frontend README.md](./README.md) - 프론트엔드 전체 가이드

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-16 | 초안 생성 - Nuxt 4 + FSD 호환성 가이드 | PM |

---

## 📞 문의

**Nuxt 4 + FSD 통합 관련 문의:**
- GitHub Issues: #nuxt4-fsd-compatibility
- Slack: #inspect-hub-frontend
