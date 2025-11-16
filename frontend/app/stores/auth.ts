import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: string
  employeeId: string
  username: string
  email?: string
  orgId?: string
  orgName?: string
  roles: string[]
  permissions: string[]
}

export interface LoginCredentials {
  employeeId: string
  password: string
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const isAuthenticated = ref(false)
  const loading = ref(false)

  // Getters
  const hasRole = computed(() => (role: string) => {
    return user.value?.roles.includes(role) ?? false
  })

  const hasPermission = computed(() => (permission: string) => {
    return user.value?.permissions.includes(permission) ?? false
  })

  // Actions
  const initialize = () => {
    // Restore from localStorage
    const storedUser = localStorage.getItem('user')
    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')

    if (storedUser && storedAccessToken) {
      user.value = JSON.parse(storedUser)
      accessToken.value = storedAccessToken
      refreshToken.value = storedRefreshToken
      isAuthenticated.value = true
    }
  }

  const login = async (credentials: LoginCredentials, method: 'LOCAL' | 'AD' | 'SSO' = 'LOCAL') => {
    loading.value = true
    try {
      const config = useRuntimeConfig()
      const response = await $fetch<{
        user: User
        tokens: AuthTokens
      }>(`${config.public.apiBase}/auth/login`, {
        method: 'POST',
        body: {
          ...credentials,
          method,
        },
      })

      // Set auth data
      user.value = response.user
      accessToken.value = response.tokens.accessToken
      refreshToken.value = response.tokens.refreshToken
      isAuthenticated.value = true

      // Persist to localStorage
      localStorage.setItem('user', JSON.stringify(response.user))
      localStorage.setItem('accessToken', response.tokens.accessToken)
      localStorage.setItem('refreshToken', response.tokens.refreshToken)

      return response
    } catch (error: any) {
      throw error
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    try {
      const config = useRuntimeConfig()
      if (accessToken.value) {
        await $fetch(`${config.public.apiBase}/auth/logout`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${accessToken.value}`,
          },
        })
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // Clear state
      user.value = null
      accessToken.value = null
      refreshToken.value = null
      isAuthenticated.value = false

      // Clear localStorage
      localStorage.removeItem('user')
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    }
  }

  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }

    try {
      const config = useRuntimeConfig()
      const response = await $fetch<AuthTokens>(`${config.public.apiBase}/auth/refresh`, {
        method: 'POST',
        body: {
          refreshToken: refreshToken.value,
        },
      })

      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken

      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)

      return response
    } catch (error) {
      // Refresh failed, logout
      await logout()
      throw error
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    loading,
    // Getters
    hasRole,
    hasPermission,
    // Actions
    initialize,
    login,
    logout,
    refreshAccessToken,
  }
})
