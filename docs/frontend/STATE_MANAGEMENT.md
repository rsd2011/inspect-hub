# State Management Guide

> **Inspect-Hub Frontend 상태 관리 가이드**
> 
> **Version**: 1.0  
> **Last Updated**: 2025-01-13  
> **Target**: Nuxt 3 + Pinia + Composition API

---

## 📚 목차

1. [상태 관리 전략 개요](#상태-관리-전략-개요)
2. [Pinia 기본 개념](#pinia-기본-개념)
3. [Store 구조 및 조직화](#store-구조-및-조직화)
4. [Auth Store (인증/인가)](#auth-store-인증인가)
5. [User Store (사용자 관리)](#user-store-사용자-관리)
6. [Case Store (사례 관리)](#case-store-사례-관리)
7. [UI Store (UI 상태)](#ui-store-ui-상태)
8. [Notification Store (알림)](#notification-store-알림)
9. [상태 영속화 (Persistence)](#상태-영속화-persistence)
10. [Store 간 통신](#store-간-통신)
11. [성능 최적화](#성능-최적화)
12. [베스트 프랙티스](#베스트-프랙티스)

---

## 상태 관리 전략 개요

### 상태 분류

| 상태 유형 | 저장 위치 | 지속성 | 예시 |
|-----------|-----------|--------|------|
| **전역 상태** | Pinia Store | Session/Local Storage | 인증, 사용자 정보, 권한 |
| **페이지 상태** | PageStateManager | Session Storage (탭별) | 검색 필터, 페이지네이션, 정렬 |
| **컴포넌트 상태** | ref/reactive | 메모리 (휘발성) | 폼 입력, 모달 표시 여부 |
| **서버 상태** | API + Cache | 없음 (요청 시 갱신) | 사례 목록, 통계 데이터 |

### 상태 관리 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                     Components                          │
│  (View Layer - 상태 소비)                               │
└────────────────┬───────────────────────────────────────┘
                 │
      ┌──────────┼──────────┐
      │          │          │
┌─────▼─────┐ ┌─▼──────┐ ┌─▼────────┐
│  Pinia    │ │ Page   │ │ Local    │
│  Stores   │ │ State  │ │ State    │
│ (Global)  │ │Manager │ │(Component│
└─────┬─────┘ └────────┘ └──────────┘
      │
      │ Actions (API 호출)
      │
┌─────▼─────────────────────────────────┐
│         API Client Layer              │
│  (HTTP 요청, 인터셉터, 에러 처리)      │
└───────────────────────────────────────┘
```

### Pinia vs Vuex

**Pinia를 선택한 이유:**
- ✅ Vue 3 Composition API와 완벽한 통합
- ✅ TypeScript 지원 우수
- ✅ Devtools 지원
- ✅ 단순한 API (mutations 불필요)
- ✅ 모듈 자동 분리
- ✅ SSR 지원 (향후 필요 시)

---

## Pinia 기본 개념

### Store 정의 (Setup Syntax)

```typescript
// features/auth/model/auth.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginCredentials } from '../types'
import * as authApi from '../api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const isLoading = ref(false)

  // Getters (Computed)
  const isAuthenticated = computed(() => !!accessToken.value)
  const hasRole = computed(() => (role: string) => {
    return user.value?.roles?.includes(role) ?? false
  })

  // Actions
  async function login(credentials: LoginCredentials) {
    isLoading.value = true
    try {
      const response = await authApi.login(credentials)
      accessToken.value = response.data.accessToken
      refreshToken.value = response.data.refreshToken
      user.value = response.data.user
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      accessToken.value = null
      refreshToken.value = null
      user.value = null
    }
  }

  // Return everything that should be exposed
  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    
    // Getters
    isAuthenticated,
    hasRole,
    
    // Actions
    login,
    logout
  }
})
```

### Store 사용

```vue
<script setup lang="ts">
import { useAuthStore } from '~/features/auth/model/auth.store'

const authStore = useAuthStore()

// State 접근
const user = computed(() => authStore.user)
const isAuth = computed(() => authStore.isAuthenticated)

// Action 호출
async function handleLogin() {
  await authStore.login({ username: 'admin', password: 'admin123' })
}

// Getter 사용
const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'))
</script>

<template>
  <div v-if="isAuth">
    <p>Welcome, {{ user?.fullName }}</p>
    <button v-if="isAdmin" @click="handleAdminAction">Admin Only</button>
  </div>
</template>
```

---

## Store 구조 및 조직화

### FSD 기반 Store 조직화

```
frontend/
├── features/
│   ├── auth/
│   │   └── model/
│   │       ├── auth.store.ts          # 인증/인가
│   │       └── auth.types.ts
│   ├── notification/
│   │   └── model/
│   │       ├── notification.store.ts  # 알림
│   │       └── notification.types.ts
│   └── theme/
│       └── model/
│           └── theme.store.ts         # 테마 설정
├── entities/
│   ├── user/
│   │   └── model/
│   │       ├── user.store.ts          # 사용자 엔티티
│   │       └── user.types.ts
│   ├── case/
│   │   └── model/
│   │       ├── case.store.ts          # 사례 엔티티
│   │       └── case.types.ts
│   └── detection/
│       └── model/
│           ├── detection.store.ts     # 탐지 이벤트
│           └── detection.types.ts
└── shared/
    └── lib/
        ├── ui-store/                  # UI 전역 상태
        │   └── ui.store.ts
        └── app-store/                 # 앱 설정
            └── app.store.ts
```

### Store 명명 규칙

**파일명:**
- `{domain}.store.ts` (예: `auth.store`, `user.store`)

**Store ID:**
- kebab-case 사용
- 도메인 이름 사용 (예: `'auth'`, `'user'`, `'case-management'`)

**State 변수:**
- camelCase 사용
- 명확한 이름 (예: `isLoading`, `currentUser`, `selectedCase`)

---

## Auth Store (인증/인가)

### 전체 구현

```typescript
// features/auth/model/auth.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { User, LoginCredentials, TokenResponse } from './auth.types'
import * as authApi from '../api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // ==================== State ====================
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const isLoading = ref(false)
  const lastActivity = ref<Date>(new Date())

  // ==================== Getters ====================
  const isAuthenticated = computed(() => {
    return !!accessToken.value && !!user.value
  })

  const isTokenValid = computed(() => {
    if (!accessToken.value) return false
    
    try {
      const payload = parseJWT(accessToken.value)
      const now = Date.now() / 1000
      return payload.exp > now
    } catch {
      return false
    }
  })

  const hasRole = computed(() => (role: string) => {
    return user.value?.roles?.includes(role) ?? false
  })

  const hasPermission = computed(() => (permission: string) => {
    return user.value?.permissions?.includes(permission) ?? false
  })

  const hasAnyRole = computed(() => (roles: string[]) => {
    return roles.some(role => hasRole.value(role))
  })

  const hasAllPermissions = computed(() => (permissions: string[]) => {
    return permissions.every(perm => hasPermission.value(perm))
  })

  // ==================== Actions ====================
  async function login(credentials: LoginCredentials) {
    isLoading.value = true
    
    try {
      const response = await authApi.login(credentials)
      
      if (response.success) {
        setAuthData(response.data)
        updateLastActivity()
        
        // 로그인 성공 후 대시보드로 이동
        router.push('/dashboard')
      }
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout API call failed:', error)
    } finally {
      clearAuthData()
      router.push('/login')
    }
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }

    try {
      const response = await authApi.refreshToken(refreshToken.value)
      
      if (response.success) {
        accessToken.value = response.data.accessToken
        updateLastActivity()
      }
    } catch (error) {
      console.error('Token refresh failed:', error)
      // 리프레시 실패 시 로그아웃
      await logout()
      throw error
    }
  }

  function setAuthData(data: TokenResponse) {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
  }

  function clearAuthData() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
  }

  function updateLastActivity() {
    lastActivity.value = new Date()
  }

  // JWT 파싱 유틸리티
  function parseJWT(token: string) {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  }

  // 세션 타임아웃 체크 (30분)
  function checkSessionTimeout() {
    const TIMEOUT_MS = 30 * 60 * 1000 // 30분
    const now = new Date()
    const elapsed = now.getTime() - lastActivity.value.getTime()
    
    if (elapsed > TIMEOUT_MS) {
      logout()
    }
  }

  // ==================== Return ====================
  return {
    // State
    user,
    accessToken,
    refreshToken,
    isLoading,
    lastActivity,
    
    // Getters
    isAuthenticated,
    isTokenValid,
    hasRole,
    hasPermission,
    hasAnyRole,
    hasAllPermissions,
    
    // Actions
    login,
    logout,
    refreshAccessToken,
    setAuthData,
    clearAuthData,
    updateLastActivity,
    checkSessionTimeout
  }
}, {
  persist: {
    storage: localStorage,
    paths: ['accessToken', 'refreshToken', 'user']
  }
})
```

### Auth Store Types

```typescript
// features/auth/model/auth.types.ts
export interface User {
  id: string
  username: string
  email: string
  fullName: string
  roles: string[]
  permissions: string[]
  orgId?: string
  orgName?: string
}

export interface LoginCredentials {
  username: string
  password: string
  rememberMe?: boolean
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  user: User
}
```

---

## User Store (사용자 관리)

```typescript
// entities/user/model/user.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserSearchParams, PaginatedResponse } from './user.types'
import * as userApi from '../api/user.api'

export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  const users = ref<User[]>([])
  const currentUser = ref<User | null>(null)
  const isLoading = ref(false)
  const totalCount = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // ==================== Getters ====================
  const totalPages = computed(() => {
    return Math.ceil(totalCount.value / pageSize.value)
  })

  const hasNextPage = computed(() => {
    return currentPage.value < totalPages.value
  })

  const hasPreviousPage = computed(() => {
    return currentPage.value > 1
  })

  const getUserById = computed(() => (id: string) => {
    return users.value.find(user => user.id === id)
  })

  // ==================== Actions ====================
  async function fetchUsers(params?: UserSearchParams) {
    isLoading.value = true
    
    try {
      const response = await userApi.getUsers({
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })

      if (response.success) {
        users.value = response.data.items
        totalCount.value = response.data.pagination.totalElements
      }
    } catch (error) {
      console.error('Failed to fetch users:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUserById(id: string) {
    isLoading.value = true
    
    try {
      const response = await userApi.getUserById(id)
      
      if (response.success) {
        currentUser.value = response.data
        
        // 캐시에도 추가/업데이트
        const index = users.value.findIndex(u => u.id === id)
        if (index >= 0) {
          users.value[index] = response.data
        } else {
          users.value.push(response.data)
        }
      }
    } catch (error) {
      console.error('Failed to fetch user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function createUser(userData: Partial<User>) {
    isLoading.value = true
    
    try {
      const response = await userApi.createUser(userData)
      
      if (response.success) {
        users.value.unshift(response.data)
        totalCount.value++
      }
      
      return response
    } catch (error) {
      console.error('Failed to create user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateUser(id: string, userData: Partial<User>) {
    isLoading.value = true
    
    try {
      const response = await userApi.updateUser(id, userData)
      
      if (response.success) {
        const index = users.value.findIndex(u => u.id === id)
        if (index >= 0) {
          users.value[index] = response.data
        }
        
        if (currentUser.value?.id === id) {
          currentUser.value = response.data
        }
      }
      
      return response
    } catch (error) {
      console.error('Failed to update user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function deleteUser(id: string) {
    isLoading.value = true
    
    try {
      await userApi.deleteUser(id)
      
      // 목록에서 제거
      const index = users.value.findIndex(u => u.id === id)
      if (index >= 0) {
        users.value.splice(index, 1)
        totalCount.value--
      }
      
      if (currentUser.value?.id === id) {
        currentUser.value = null
      }
    } catch (error) {
      console.error('Failed to delete user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  function setCurrentPage(page: number) {
    currentPage.value = page
  }

  function setPageSize(size: number) {
    pageSize.value = size
  }

  function clearCurrentUser() {
    currentUser.value = null
  }

  function clearUsers() {
    users.value = []
    totalCount.value = 0
  }

  // ==================== Return ====================
  return {
    // State
    users,
    currentUser,
    isLoading,
    totalCount,
    currentPage,
    pageSize,
    
    // Getters
    totalPages,
    hasNextPage,
    hasPreviousPage,
    getUserById,
    
    // Actions
    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    setCurrentPage,
    setPageSize,
    clearCurrentUser,
    clearUsers
  }
})
```

---

## Case Store (사례 관리)

```typescript
// entities/case/model/case.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Case, CaseSearchParams, CaseStatus } from './case.types'
import * as caseApi from '../api/case.api'

export const useCaseStore = defineStore('case', () => {
  // ==================== State ====================
  const cases = ref<Case[]>([])
  const currentCase = ref<Case | null>(null)
  const isLoading = ref(false)
  const totalCount = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)
  const filters = ref<CaseSearchParams>({})

  // ==================== Getters ====================
  const casesByStatus = computed(() => (status: CaseStatus) => {
    return cases.value.filter(c => c.status === status)
  })

  const highRiskCases = computed(() => {
    return cases.value.filter(c => c.riskLevel === 'HIGH')
  })

  const myCases = computed(() => {
    const authStore = useAuthStore()
    return cases.value.filter(c => c.assignee === authStore.user?.username)
  })

  // ==================== Actions ====================
  async function fetchCases(params?: CaseSearchParams) {
    isLoading.value = true
    
    try {
      const response = await caseApi.getCases({
        page: currentPage.value,
        size: pageSize.value,
        ...filters.value,
        ...params
      })

      if (response.success) {
        cases.value = response.data.items
        totalCount.value = response.data.pagination.totalElements
      }
    } catch (error) {
      console.error('Failed to fetch cases:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCaseById(id: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.getCaseById(id)
      
      if (response.success) {
        currentCase.value = response.data
      }
    } catch (error) {
      console.error('Failed to fetch case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function createCase(caseData: Partial<Case>) {
    isLoading.value = true
    
    try {
      const response = await caseApi.createCase(caseData)
      
      if (response.success) {
        cases.value.unshift(response.data)
        totalCount.value++
      }
      
      return response
    } catch (error) {
      console.error('Failed to create case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateCase(id: string, caseData: Partial<Case>) {
    isLoading.value = true
    
    try {
      const response = await caseApi.updateCase(id, caseData)
      
      if (response.success) {
        const index = cases.value.findIndex(c => c.id === id)
        if (index >= 0) {
          cases.value[index] = { ...cases.value[index], ...response.data }
        }
        
        if (currentCase.value?.id === id) {
          currentCase.value = response.data
        }
      }
      
      return response
    } catch (error) {
      console.error('Failed to update case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateCaseStatus(id: string, status: CaseStatus, comment?: string) {
    return await updateCase(id, { status, statusComment: comment })
  }

  async function assignCase(id: string, assignee: string) {
    return await updateCase(id, { assignee })
  }

  async function approveCase(id: string, comment: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.approveCase(id, { comment })
      
      if (response.success) {
        await fetchCaseById(id)
      }
      
      return response
    } catch (error) {
      console.error('Failed to approve case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function rejectCase(id: string, reason: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.rejectCase(id, { reason })
      
      if (response.success) {
        await fetchCaseById(id)
      }
      
      return response
    } catch (error) {
      console.error('Failed to reject case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  function setFilters(newFilters: CaseSearchParams) {
    filters.value = { ...filters.value, ...newFilters }
  }

  function clearFilters() {
    filters.value = {}
  }

  function setCurrentPage(page: number) {
    currentPage.value = page
  }

  // ==================== Return ====================
  return {
    // State
    cases,
    currentCase,
    isLoading,
    totalCount,
    currentPage,
    pageSize,
    filters,
    
    // Getters
    casesByStatus,
    highRiskCases,
    myCases,
    
    // Actions
    fetchCases,
    fetchCaseById,
    createCase,
    updateCase,
    updateCaseStatus,
    assignCase,
    approveCase,
    rejectCase,
    setFilters,
    clearFilters,
    setCurrentPage
  }
})
```

---

## UI Store (UI 상태)

```typescript
// shared/lib/ui-store/ui.store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // ==================== State ====================
  const isSidebarCollapsed = ref(false)
  const isMobileMenuOpen = ref(false)
  const isLoading = ref(false)
  const loadingMessage = ref('')
  const breadcrumbs = ref<{ label: string; to?: string }[]>([])

  // Modal/Dialog 상태
  const activeModals = ref<Set<string>>(new Set())
  
  // Toast 알림
  interface Toast {
    id: string
    message: string
    type: 'success' | 'error' | 'warning' | 'info'
    duration?: number
  }
  const toasts = ref<Toast[]>([])

  // ==================== Actions ====================
  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  function setSidebarCollapsed(collapsed: boolean) {
    isSidebarCollapsed.value = collapsed
  }

  function toggleMobileMenu() {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
  }

  function closeMobileMenu() {
    isMobileMenuOpen.value = false
  }

  function showLoading(message = '로딩 중...') {
    isLoading.value = true
    loadingMessage.value = message
  }

  function hideLoading() {
    isLoading.value = false
    loadingMessage.value = ''
  }

  function setBreadcrumbs(items: { label: string; to?: string }[]) {
    breadcrumbs.value = items
  }

  function openModal(modalId: string) {
    activeModals.value.add(modalId)
  }

  function closeModal(modalId: string) {
    activeModals.value.delete(modalId)
  }

  function isModalOpen(modalId: string): boolean {
    return activeModals.value.has(modalId)
  }

  function showToast(message: string, type: Toast['type'] = 'info', duration = 3000) {
    const id = `toast-${Date.now()}-${Math.random()}`
    const toast: Toast = { id, message, type, duration }
    
    toasts.value.push(toast)
    
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }
    
    return id
  }

  function removeToast(id: string) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index >= 0) {
      toasts.value.splice(index, 1)
    }
  }

  function clearToasts() {
    toasts.value = []
  }

  // ==================== Return ====================
  return {
    // State
    isSidebarCollapsed,
    isMobileMenuOpen,
    isLoading,
    loadingMessage,
    breadcrumbs,
    activeModals,
    toasts,
    
    // Actions
    toggleSidebar,
    setSidebarCollapsed,
    toggleMobileMenu,
    closeMobileMenu,
    showLoading,
    hideLoading,
    setBreadcrumbs,
    openModal,
    closeModal,
    isModalOpen,
    showToast,
    removeToast,
    clearToasts
  }
}, {
  persist: {
    storage: localStorage,
    paths: ['isSidebarCollapsed']
  }
})
```

---

## Notification Store (알림)

```typescript
// features/notification/model/notification.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from './notification.types'

export const useNotificationStore = defineStore('notification', () => {
  // ==================== State ====================
  const notifications = ref<Notification[]>([])
  const isConnected = ref(false)
  const sseClient = ref<EventSource | null>(null)

  // ==================== Getters ====================
  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.read).length
  })

  const unreadNotifications = computed(() => {
    return notifications.value.filter(n => !n.read)
  })

  const recentNotifications = computed(() => {
    return notifications.value.slice(0, 10)
  })

  // ==================== Actions ====================
  function connectSSE() {
    const authStore = useAuthStore()
    
    if (!authStore.accessToken) {
      console.warn('Cannot connect SSE: No access token')
      return
    }

    const config = useRuntimeConfig()
    const url = `${config.public.apiBase}/notifications/stream`
    
    sseClient.value = new EventSource(url, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      }
    })

    sseClient.value.onopen = () => {
      isConnected.value = true
      console.log('SSE connected')
    }

    sseClient.value.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data)
        addNotification(notification)
      } catch (error) {
        console.error('Failed to parse notification:', error)
      }
    }

    sseClient.value.onerror = (error) => {
      console.error('SSE error:', error)
      isConnected.value = false
      
      // 재연결 시도 (5초 후)
      setTimeout(() => {
        if (!isConnected.value) {
          connectSSE()
        }
      }, 5000)
    }
  }

  function disconnectSSE() {
    if (sseClient.value) {
      sseClient.value.close()
      sseClient.value = null
      isConnected.value = false
    }
  }

  function addNotification(notification: Notification) {
    notifications.value.unshift(notification)
    
    // Toast 표시
    const uiStore = useUIStore()
    uiStore.showToast(notification.message, notification.type)
  }

  async function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
      
      // API 호출
      try {
        await notificationApi.markAsRead(id)
      } catch (error) {
        console.error('Failed to mark notification as read:', error)
      }
    }
  }

  async function markAllAsRead() {
    notifications.value.forEach(n => n.read = true)
    
    try {
      await notificationApi.markAllAsRead()
    } catch (error) {
      console.error('Failed to mark all as read:', error)
    }
  }

  async function deleteNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index >= 0) {
      notifications.value.splice(index, 1)
      
      try {
        await notificationApi.deleteNotification(id)
      } catch (error) {
        console.error('Failed to delete notification:', error)
      }
    }
  }

  function clearNotifications() {
    notifications.value = []
  }

  // ==================== Return ====================
  return {
    // State
    notifications,
    isConnected,
    
    // Getters
    unreadCount,
    unreadNotifications,
    recentNotifications,
    
    // Actions
    connectSSE,
    disconnectSSE,
    addNotification,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearNotifications
  }
})
```

---

## 상태 영속화 (Persistence)

### Pinia Plugin 설정

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

### Store별 영속화 설정

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

### SessionStorage vs LocalStorage

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

## Store 간 통신

### Store에서 다른 Store 사용

```typescript
// entities/case/model/case.store.ts
import { useAuthStore } from '~/features/auth/model/auth.store'
import { useUIStore } from '~/shared/lib/ui-store/ui.store'

export const useCaseStore = defineStore('case', () => {
  const authStore = useAuthStore()
  const uiStore = useUIStore()

  async function fetchMyCases() {
    // Auth store의 데이터 사용
    const userId = authStore.user?.id
    
    if (!userId) {
      uiStore.showToast('로그인이 필요합니다', 'error')
      return
    }

    // UI 로딩 표시
    uiStore.showLoading('내 사례 조회 중...')
    
    try {
      const response = await caseApi.getMyCases(userId)
      // ...
    } finally {
      uiStore.hideLoading()
    }
  }

  return {
    fetchMyCases
  }
})
```

### Composable을 통한 Store 조합

```typescript
// shared/lib/composables/useCaseManagement.ts
export function useCaseManagement() {
  const caseStore = useCaseStore()
  const authStore = useAuthStore()
  const uiStore = useUIStore()

  async function approveCaseWithNotification(caseId: string, comment: string) {
    // 권한 확인
    if (!authStore.hasPermission('case:approve')) {
      uiStore.showToast('승인 권한이 없습니다', 'error')
      return
    }

    try {
      await caseStore.approveCase(caseId, comment)
      uiStore.showToast('사례가 승인되었습니다', 'success')
    } catch (error) {
      uiStore.showToast('승인 중 오류가 발생했습니다', 'error')
      throw error
    }
  }

  return {
    approveCaseWithNotification
  }
}
```

---

## 성능 최적화

### 1. Computed 캐싱 활용

```typescript
// ✅ Good: Computed 사용 (캐싱됨)
const expensiveComputation = computed(() => {
  return users.value
    .filter(u => u.active)
    .map(u => ({
      ...u,
      displayName: `${u.fullName} (${u.username})`
    }))
    .sort((a, b) => a.displayName.localeCompare(b.displayName))
})

// ❌ Bad: 매번 재계산
function getActiveUsers() {
  return users.value
    .filter(u => u.active)
    .map(u => ({ /* ... */ }))
    .sort(/* ... */)
}
```

### 2. 선택적 구독 (Storelets)

```typescript
// ✅ Good: 필요한 state만 구독
const { user, isAuthenticated } = storeToRefs(useAuthStore())

// ❌ Bad: 전체 store 반응형으로 만들기
const authStore = toRefs(useAuthStore())  // 모든 state가 반응형
```

### 3. 대용량 리스트 관리

```typescript
// entities/case/model/case.store.ts
export const useCaseStore = defineStore('case', () => {
  const cases = ref<Case[]>([])
  const caseMap = ref<Map<string, Case>>(new Map())  // 빠른 조회용

  async function fetchCases() {
    const response = await caseApi.getCases()
    
    if (response.success) {
      cases.value = response.data.items
      
      // Map으로도 저장 (O(1) 조회)
      response.data.items.forEach(c => {
        caseMap.value.set(c.id, c)
      })
    }
  }

  // ✅ O(1) 조회
  function getCaseById(id: string) {
    return caseMap.value.get(id)
  }

  // ❌ O(n) 조회 (피하기)
  // function getCaseById(id: string) {
  //   return cases.value.find(c => c.id === id)
  // }

  return {
    cases,
    fetchCases,
    getCaseById
  }
})
```

### 4. Debounce/Throttle

```typescript
// shared/lib/composables/useSearch.ts
import { debounce } from 'lodash-es'

export function useSearch() {
  const searchQuery = ref('')
  const results = ref([])

  const debouncedSearch = debounce(async (query: string) => {
    const response = await searchApi.search(query)
    results.value = response.data
  }, 300)

  watch(searchQuery, (newQuery) => {
    debouncedSearch(newQuery)
  })

  return {
    searchQuery,
    results
  }
}
```

---

## 베스트 프랙티스

### 1. Store 분리 원칙

**✅ DO:**
- 도메인별로 Store 분리
- 단일 책임 원칙 (SRP) 적용
- 5-10개의 action으로 제한

**❌ DON'T:**
- 모든 것을 하나의 큰 Store에 담기
- Store 간 강한 결합
- 너무 많은 state 관리

### 2. Action 설계

```typescript
// ✅ Good: 명확한 action 이름
async function fetchUserById(id: string) { /* ... */ }
async function updateUserProfile(id: string, data: Partial<User>) { /* ... */ }
async function deleteUser(id: string) { /* ... */ }

// ❌ Bad: 모호한 이름
async function get(id: string) { /* ... */ }
async function save(data: any) { /* ... */ }
async function remove(id: string) { /* ... */ }
```

### 3. 에러 처리

```typescript
export const useUserStore = defineStore('user', () => {
  const error = ref<Error | null>(null)
  const isLoading = ref(false)

  async function fetchUsers() {
    isLoading.value = true
    error.value = null  // 이전 에러 초기화
    
    try {
      const response = await userApi.getUsers()
      // ...
    } catch (err) {
      error.value = err as Error
      
      // UI Store를 통한 사용자 알림
      const uiStore = useUIStore()
      uiStore.showToast('사용자 목록을 불러오는데 실패했습니다', 'error')
      
      throw err  // 상위로 전파
    } finally {
      isLoading.value = false
    }
  }

  return {
    error,
    isLoading,
    fetchUsers
  }
})
```

### 4. TypeScript 타입 안정성

```typescript
// ✅ Good: 타입 정의
interface UserState {
  users: User[]
  currentUser: User | null
  isLoading: boolean
}

const state = ref<UserState>({
  users: [],
  currentUser: null,
  isLoading: false
})

// ❌ Bad: any 사용
const state = ref<any>({
  users: [],
  currentUser: null
})
```

### 5. 초기화 및 정리

```typescript
// app.vue
<script setup lang="ts">
import { useAuthStore } from '~/features/auth/model/auth.store'
import { useNotificationStore } from '~/features/notification/model/notification.store'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// 앱 초기화
onMounted(() => {
  if (authStore.isAuthenticated) {
    notificationStore.connectSSE()
  }
})

// 정리
onBeforeUnmount(() => {
  notificationStore.disconnectSSE()
})
</script>
```

### 6. Devtools 활용

```typescript
// 개발 환경에서 Devtools 활성화
export const useUserStore = defineStore('user', () => {
  // ... store implementation
  
  // Devtools에서 확인하기 쉬운 이름 설정
  if (import.meta.dev) {
    console.log('[Store] User store initialized')
  }
  
  return { /* ... */ }
})
```

---

## 참고 자료

### 공식 문서
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)
- [Nuxt 3 State Management](https://nuxt.com/docs/getting-started/state-management)

### 내부 문서
- [Frontend README](./README.md)
- [Components Roadmap](./COMPONENTS_ROADMAP.md)
- [Frontend Testing Guide](./TESTING.md)
- [API Contract](../api/CONTRACT.md)

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | 최초 작성 | 개발팀 |

---

**문의사항이 있으시면 개발팀으로 연락 주시기 바랍니다.**