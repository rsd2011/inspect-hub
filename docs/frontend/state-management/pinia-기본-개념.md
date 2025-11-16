# Pinia 기본 개념

## Store 정의 (Setup Syntax)

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

## Store 사용

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
