import { defineStore } from 'pinia'
import { authApi, type LoginRequest, type LoginResponse } from '~/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<{
    userId: string
    username: string
    roles: string[]
  } | null>(null)

  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const isAuthenticated = computed(() => !!accessToken.value)

  // Actions
  async function login(credentials: LoginRequest) {
    try {
      const response: LoginResponse = await authApi.login(credentials)

      // Store tokens
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken

      // Store user info
      user.value = {
        userId: response.userId,
        username: response.username,
        roles: response.roles,
      }

      // Save to localStorage for persistence
      if (process.client) {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
        localStorage.setItem('user', JSON.stringify(user.value))
      }

      return response
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await authApi.logout(accessToken.value)
      }
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      // Clear state
      accessToken.value = null
      refreshToken.value = null
      user.value = null

      // Clear localStorage
      if (process.client) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
      }
    }
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }

    try {
      const response = await authApi.refreshToken({ refreshToken: refreshToken.value })

      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken

      // Update localStorage
      if (process.client) {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
      }

      return response
    } catch (error) {
      console.error('Token refresh failed:', error)
      await logout()
      throw error
    }
  }

  function restoreSession() {
    if (process.client) {
      const storedAccessToken = localStorage.getItem('accessToken')
      const storedRefreshToken = localStorage.getItem('refreshToken')
      const storedUser = localStorage.getItem('user')

      if (storedAccessToken && storedRefreshToken && storedUser) {
        accessToken.value = storedAccessToken
        refreshToken.value = storedRefreshToken
        user.value = JSON.parse(storedUser)
      }
    }
  }

  function hasRole(role: string): boolean {
    return user.value?.roles.includes(role) ?? false
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    // Actions
    login,
    logout,
    refreshAccessToken,
    restoreSession,
    hasRole,
  }
})
