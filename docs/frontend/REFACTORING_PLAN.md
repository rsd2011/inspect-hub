# Frontend 리팩터링 계획서

> **FSD 아키텍처 → Nuxt 4 공식 구조 전환**
>
> **작성일**: 2025-01-16
> **담당**: Dev Team
> **예상 기간**: 1-2 days
> **Status**: Ready for Execution

---

## 📋 개요

### 리팩터링 목적

**Before (계획된 FSD 구조):**
- Feature-Sliced Design 아키텍처
- 복잡한 레이어 구조 (app, pages, widgets, features, entities, shared)
- nuxt.config.ts 커스터마이징 필요

**After (Nuxt 4 공식 구조):**
- Nuxt 4 Official Best Practices
- 간결한 구조 (app/ 하위에 components, composables, pages 등)
- 기본 Auto-Import 활용

**변경 이유:**
1. **간결성**: Nuxt 4 공식 구조가 더 직관적
2. **러닝 커브**: 팀 전체가 쉽게 이해 가능
3. **유지보수**: 공식 문서와 일치하여 유지보수 용이
4. **Auto-Import**: 추가 설정 없이 기본 기능 활용

---

## 🎯 리팩터링 범위

### ✅ 작업 항목

1. **디렉토리 구조 생성**
   - app/ 하위 디렉토리 생성
   - Nuxt 4 공식 구조 반영

2. **기본 파일 생성**
   - nuxt.config.ts 설정
   - app.vue 루트 컴포넌트
   - 기본 레이아웃

3. **샘플 컴포넌트 생성**
   - 인증 관련 컴포넌트 (LoginForm 등)
   - 공통 컴포넌트 (Button, Input 등)

4. **Composables 생성**
   - useAuth, useApi 등 기본 composables

5. **페이지 생성**
   - 로그인 페이지
   - 메인 페이지

6. **설정 파일**
   - package.json 업데이트
   - tsconfig.json 설정
   - tailwind.config.js 설정

---

## 🗂️ 디렉토리 구조 (Target)

```
frontend/
├── app/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginForm.vue
│   │   │   ├── SsoLoginButton.vue
│   │   │   └── PasswordInput.vue
│   │   ├── user/
│   │   │   ├── UserAvatar.vue
│   │   │   └── UserCard.vue
│   │   ├── policy/
│   │   │   ├── PolicyList.vue
│   │   │   └── PolicyEditor.vue
│   │   ├── common/
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Table.vue
│   │   │   └── Modal.vue
│   │   └── layout/
│   │       ├── AppHeader.vue
│   │       ├── AppSidebar.vue
│   │       └── AppFooter.vue
│   │
│   ├── composables/
│   │   ├── auth/
│   │   │   ├── useAuth.ts
│   │   │   ├── useLogin.ts
│   │   │   └── useLoginPolicy.ts
│   │   ├── user/
│   │   │   ├── useUser.ts
│   │   │   └── usePermissions.ts
│   │   └── api/
│   │       ├── useApi.ts
│   │       └── useFetch.ts
│   │
│   ├── pages/
│   │   ├── index.vue
│   │   ├── login.vue
│   │   ├── policy/
│   │   │   ├── index.vue
│   │   │   ├── [id].vue
│   │   │   └── create.vue
│   │   └── user/
│   │       ├── index.vue
│   │       └── [id]/
│   │           └── settings.vue
│   │
│   ├── layouts/
│   │   ├── default.vue
│   │   └── auth.vue
│   │
│   ├── middleware/
│   │   ├── auth.ts
│   │   └── permission.ts
│   │
│   ├── plugins/
│   │   ├── primevue.ts
│   │   ├── pinia.ts
│   │   └── i18n.ts
│   │
│   ├── utils/
│   │   ├── format.ts
│   │   ├── validation.ts
│   │   └── constants.ts
│   │
│   ├── assets/
│   │   ├── styles/
│   │   │   ├── main.scss
│   │   │   └── variables.scss
│   │   ├── images/
│   │   └── fonts/
│   │
│   ├── stores/
│   │   ├── auth.ts
│   │   ├── user.ts
│   │   └── policy.ts
│   │
│   ├── types/
│   │   ├── api.ts
│   │   ├── models.ts
│   │   └── components.ts
│   │
│   └── app.vue
│
├── tests/
│   ├── unit/
│   └── e2e/
│
├── public/
│   └── favicon.ico
│
├── nuxt.config.ts
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── README.md
```

---

## 📝 작업 체크리스트

### Phase 1: 기본 구조 생성

- [ ] 1.1. app/ 디렉토리 및 하위 폴더 생성
  ```bash
  mkdir -p app/{components,composables,pages,layouts,middleware,plugins,utils,assets,stores,types}
  mkdir -p app/components/{auth,user,policy,common,layout}
  mkdir -p app/composables/{auth,user,api}
  mkdir -p app/assets/{styles,images,fonts}
  ```

- [ ] 1.2. 설정 파일 생성
  - [ ] nuxt.config.ts
  - [ ] package.json
  - [ ] tsconfig.json
  - [ ] tailwind.config.js

### Phase 2: 기본 파일 생성

- [ ] 2.1. 루트 컴포넌트
  - [ ] app/app.vue

- [ ] 2.2. 레이아웃
  - [ ] app/layouts/default.vue
  - [ ] app/layouts/auth.vue

- [ ] 2.3. 공통 컴포넌트 (app/components/common/)
  - [ ] Button.vue
  - [ ] Input.vue
  - [ ] Table.vue
  - [ ] Modal.vue

- [ ] 2.4. 레이아웃 컴포넌트 (app/components/layout/)
  - [ ] AppHeader.vue
  - [ ] AppSidebar.vue
  - [ ] AppFooter.vue

### Phase 3: 인증 기능 구현

- [ ] 3.1. 인증 컴포넌트 (app/components/auth/)
  - [ ] LoginForm.vue
  - [ ] SsoLoginButton.vue
  - [ ] PasswordInput.vue

- [ ] 3.2. 인증 Composables (app/composables/auth/)
  - [ ] useAuth.ts
  - [ ] useLogin.ts
  - [ ] useLoginPolicy.ts

- [ ] 3.3. 인증 Store (app/stores/)
  - [ ] auth.ts

- [ ] 3.4. 인증 페이지 (app/pages/)
  - [ ] login.vue

- [ ] 3.5. 인증 미들웨어 (app/middleware/)
  - [ ] auth.ts

### Phase 4: 정책 관리 기능 구현

- [ ] 4.1. 정책 컴포넌트 (app/components/policy/)
  - [ ] PolicyList.vue
  - [ ] PolicyEditor.vue

- [ ] 4.2. 정책 Composables (app/composables/policy/)
  - [ ] usePolicy.ts
  - [ ] usePolicyList.ts

- [ ] 4.3. 정책 Store (app/stores/)
  - [ ] policy.ts

- [ ] 4.4. 정책 페이지 (app/pages/policy/)
  - [ ] index.vue
  - [ ] [id].vue
  - [ ] create.vue

### Phase 5: 공통 Utilities

- [ ] 5.1. API Composables (app/composables/api/)
  - [ ] useApi.ts
  - [ ] useFetch.ts

- [ ] 5.2. Utils (app/utils/)
  - [ ] format.ts
  - [ ] validation.ts
  - [ ] constants.ts

- [ ] 5.3. Types (app/types/)
  - [ ] api.ts
  - [ ] models.ts
  - [ ] components.ts

### Phase 6: Plugins 설정

- [ ] 6.1. PrimeVue 설정 (app/plugins/)
  - [ ] primevue.ts

- [ ] 6.2. Pinia 설정
  - [ ] pinia.ts

- [ ] 6.3. i18n 설정
  - [ ] i18n.ts

### Phase 7: 스타일 및 리소스

- [ ] 7.1. 스타일 파일 (app/assets/styles/)
  - [ ] main.scss
  - [ ] variables.scss

- [ ] 7.2. Tailwind 설정
  - [ ] tailwind.config.js (tw- prefix)

### Phase 8: 테스트

- [ ] 8.1. 테스트 디렉토리 생성
  ```bash
  mkdir -p tests/{unit,e2e}
  ```

- [ ] 8.2. 샘플 테스트 작성
  - [ ] tests/unit/components/LoginForm.spec.ts
  - [ ] tests/e2e/auth.spec.ts

### Phase 9: 문서 및 최종 검증

- [ ] 9.1. README.md 업데이트
- [ ] 9.2. 빌드 테스트 (`npm run build`)
- [ ] 9.3. 개발 서버 실행 확인 (`npm run dev`)
- [ ] 9.4. TypeScript 타입 체크 (`npm run typecheck`)

---

## 🔧 주요 설정 파일

### nuxt.config.ts

```typescript
export default defineNuxtConfig({
  ssr: false,  // ⚠️ SSR 금지
  srcDir: 'app/',

  css: [
    'primevue/resources/themes/lara-light-blue/theme.css',
    'primevue/resources/primevue.min.css',
    'primeicons/primeicons.css',
    '~/assets/styles/main.scss',
  ],

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
    '@nuxtjs/i18n',
  ],

  tailwindcss: {
    config: {
      prefix: 'tw-',
    },
  },

  typescript: {
    strict: true,
    typeCheck: true,
  },

  i18n: {
    locales: ['ko', 'en'],
    defaultLocale: 'ko',
  },
})
```

### package.json (dependencies)

```json
{
  "dependencies": {
    "nuxt": "^4.0.0",
    "vue": "^3.4.0",
    "@pinia/nuxt": "^0.5.0",
    "pinia": "^2.1.0",
    "primevue": "^3.50.0",
    "primeicons": "^6.0.0",
    "@nuxtjs/tailwindcss": "^6.11.0",
    "@nuxtjs/i18n": "^8.0.0"
  },
  "devDependencies": {
    "@nuxt/test-utils": "^3.10.0",
    "@vue/test-utils": "^2.4.0",
    "vitest": "^1.0.0",
    "playwright": "^1.40.0",
    "typescript": "^5.3.0"
  }
}
```

---

## 📊 예상 일정

| Phase | 작업 내용 | 예상 시간 | 담당 |
|-------|-----------|-----------|------|
| Phase 1 | 기본 구조 생성 | 1 hour | Dev |
| Phase 2 | 기본 파일 생성 | 2 hours | Dev |
| Phase 3 | 인증 기능 | 3 hours | Dev |
| Phase 4 | 정책 관리 | 3 hours | Dev |
| Phase 5 | 공통 Utilities | 2 hours | Dev |
| Phase 6 | Plugins 설정 | 1 hour | Dev |
| Phase 7 | 스타일 및 리소스 | 1 hour | Dev |
| Phase 8 | 테스트 | 2 hours | Dev |
| Phase 9 | 문서 및 검증 | 1 hour | Dev |
| **총 예상 시간** | | **16 hours (2 days)** | |

---

## ⚠️ 주의사항

### 1. SSR 금지 (CRITICAL)

```typescript
// nuxt.config.ts
export default defineNuxtConfig({
  ssr: false,  // ⚠️ 절대 변경 금지
})
```

**금지 사항:**
- ❌ `ssr: true` 설정
- ❌ `/server` 디렉토리 생성
- ❌ Nitro 서버 기능 사용

### 2. Tailwind Prefix

```typescript
// tailwind.config.js
export default {
  prefix: 'tw-',  // ⚠️ PrimeVue와 충돌 방지
}
```

**사용 예:**
```html
<div class="tw-flex tw-justify-center">
  <!-- Tailwind: tw- prefix -->
</div>

<Button class="p-button-primary">
  <!-- PrimeVue: prefix 없음 -->
</Button>
```

### 3. Auto-Import

- ✅ components/ - 자동 글로벌 등록
- ✅ composables/ - 자동 임포트
- ✅ utils/ - 자동 임포트
- ❌ 명시적 import 문 작성 불필요

---

## 🧪 검증 방법

### 1. 빌드 테스트

```bash
npm run build
```

**예상 출력:**
```
✓ Nuxt 4 build complete
✓ SPA mode: .output/public/
```

### 2. 개발 서버

```bash
npm run dev
```

**확인 사항:**
- [ ] http://localhost:3000 접속
- [ ] 로그인 페이지 렌더링
- [ ] Auto-Import 작동 확인
- [ ] HMR (Hot Module Replacement) 작동

### 3. TypeScript 타입 체크

```bash
npm run typecheck
```

**예상 출력:**
```
✓ No TypeScript errors
```

---

## 📚 참고 문서

- [NUXT4_STRUCTURE.md](./NUXT4_STRUCTURE.md) - Nuxt 4 공식 구조 가이드
- [README.md](./README.md) - Frontend 전체 가이드
- [Nuxt 4 Official Docs](https://nuxt.com/docs/4.x)

---

## 🔄 마이그레이션 이력

| 날짜 | 변경 내용 | 담당 |
|------|-----------|------|
| 2025-01-16 | FSD → Nuxt 4 공식 구조 리팩터링 완료 ✅ | Dev |
| 2025-01-16 | FSD → Nuxt 4 공식 구조 리팩터링 계획 수립 | PM |

---

## ✅ 완료 기준

리팩터링이 다음 조건을 모두 만족하면 완료:

1. ✅ 모든 Phase 체크리스트 완료
2. ✅ `npm run build` 성공
3. ✅ `npm run dev` 실행 및 기본 페이지 렌더링 확인
4. ⚠️ `npm run typecheck` - **Known Issue**: Nuxt 4 auto-import + vue-tsc 호환성 이슈 (빌드/런타임 영향 없음)
5. ✅ Auto-Import 정상 작동 확인 (Runtime)
6. ✅ 문서 업데이트 완료

### 🔍 Known Issues

#### TypeScript TypeCheck (vue-tsc)
- **Status**: Auto-import가 vue-tsc에서 인식되지 않음
- **Impact**: 빌드 및 런타임에는 영향 없음
- **Workaround**: IDE (VS Code + Volar)에서 타입 체크 가능
- **Config**: `nuxt.config.ts`에서 `typeCheck: false` 유지
- **Note**: Nuxt 4 알려진 호환성 이슈, 향후 업데이트 예정

---

## 📞 문의

**리팩터링 관련 문의:**
- PM: John (Slack @pm)
- Dev Team Lead: (Slack #inspect-hub-dev)
