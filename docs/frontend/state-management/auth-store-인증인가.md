# Auth Store (인증/인가)

## 전체 구현

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

## Auth Store Types

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
