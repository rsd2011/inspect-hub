# 상태 영속화 (Persistence)

## Pinia Plugin 설정

```typescript
// app/plugins/pinia-persist.ts
import { createPersistedState } from 'pinia-plugin-persistedstate'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.$pinia.use(
    createPersistedState({
      storage: localStorage,
      auto: false  // 자동 저장 비활성화 (명시적으로 설정)
    })
  )
})
```

## Store별 영속화 설정

```typescript
// 예시: Auth Store
export const useAuthStore = defineStore('auth', () => {
  // ... store implementation
}, {
  persist: {
    storage: localStorage,
    paths: ['accessToken', 'refreshToken', 'user'],  // 저장할 state만 선택
    beforeRestore: (ctx) => {
      console.log('Restoring auth store...')
    },
    afterRestore: (ctx) => {
      console.log('Auth store restored')
    }
  }
})

// 예시: UI Store (일부만 저장)
export const useUIStore = defineStore('ui', () => {
  // ... store implementation
}, {
  persist: {
    storage: localStorage,
    paths: ['isSidebarCollapsed', 'theme']  // 사이드바 상태와 테마만 저장
  }
})

// 예시: Case Store (영속화 없음)
export const useCaseStore = defineStore('case', () => {
  // ... store implementation
})  // persist 옵션 없음 = 영속화 안 함
```

## SessionStorage vs LocalStorage

```typescript
// SessionStorage: 브라우저 탭 닫으면 삭제
export const usePageStateStore = defineStore('page-state', () => {
  // ... store implementation
}, {
  persist: {
    storage: sessionStorage,  // 세션 스토리지 사용
    paths: ['searchFilters', 'currentPage']
  }
})

// LocalStorage: 브라우저 닫아도 유지
export const useAuthStore = defineStore('auth', () => {
  // ... store implementation
}, {
  persist: {
    storage: localStorage,  // 로컬 스토리지 사용
    paths: ['accessToken', 'refreshToken', 'user']
  }
})
```

---
