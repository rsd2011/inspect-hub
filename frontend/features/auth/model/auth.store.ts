import { defineStore } from 'pinia'
import type { LoginRequest, SSOLoginRequest, LoginResponse, User } from './types'
import { authApi } from '../api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const loginMethod = ref<'traditional' | 'sso' | null>(null)
  const isLoading = ref(false)

  // Computed
  const isAuthenticated = computed(() => !!accessToken.value)

  // Actions

  /**
   * Initialize - restore session from storage
   */
  function initialize(): void {
    if (typeof window !== 'undefined') {
      const storedAccessToken = localStorage.getItem('accessToken')
      const storedRefreshToken = localStorage.getItem('refreshToken')
      const storedUser = localStorage.getItem('user')
      const storedMethod = localStorage.getItem('loginMethod') as 'traditional' | 'sso' | null

      if (storedAccessToken && storedRefreshToken && storedUser) {
        try {
          accessToken.value = storedAccessToken
          refreshToken.value = storedRefreshToken
          user.value = JSON.parse(storedUser)
          loginMethod.value = storedMethod
        }
        catch {
          // Invalid stored data, clear it
          clearSession()
        }
      }
    }
  }

  /**
   * Traditional login with username/password
   */
  async function loginTraditional(credentials: LoginRequest): Promise<void> {
    try {
      isLoading.value = true
      const response: LoginResponse = await authApi.login(credentials)

      // Store tokens and user info
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      loginMethod.value = 'traditional'

      user.value = {
        userId: response.userId,
        username: response.username,
        email: response.email,
        displayName: response.displayName,
        roles: response.roles,
        permissions: response.permissions,
        organizationId: response.organizationId,
        organizationName: response.organizationName,
      }

      // Persist to localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
        localStorage.setItem('user', JSON.stringify(user.value))
        localStorage.setItem('loginMethod', 'traditional')
      }
    }
    finally {
      isLoading.value = false
    }
  }

  /**
   * SSO login with SSO token
   */
  async function loginSSO(ssoRequest: SSOLoginRequest): Promise<void> {
    try {
      isLoading.value = true
      const response: LoginResponse = await authApi.loginSSO(ssoRequest)

      // Store tokens and user info
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      loginMethod.value = 'sso'

      user.value = {
        userId: response.userId,
        username: response.username,
        email: response.email,
        displayName: response.displayName,
        roles: response.roles,
        permissions: response.permissions,
        organizationId: response.organizationId,
        organizationName: response.organizationName,
      }

      // Persist to localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
        localStorage.setItem('user', JSON.stringify(user.value))
        localStorage.setItem('loginMethod', 'sso')
      }
    }
    finally {
      isLoading.value = false
    }
  }

  /**
   * Logout
   */
  async function logout(): Promise<void> {
    try {
      isLoading.value = true

      // Call backend logout API if authenticated
      if (accessToken.value) {
        try {
          await authApi.logout(accessToken.value)
        }
        catch (error) {
          console.error('Logout API call failed:', error)
          // Continue with local logout even if API fails
        }
      }
    }
    finally {
      clearSession()
      isLoading.value = false
    }
  }

  /**
   * Refresh access token
   */
  async function refreshAccessToken(): Promise<void> {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }

    try {
      const response = await authApi.refreshToken({
        refreshToken: refreshToken.value,
      })

      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken

      // Update localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
      }
    }
    catch (error) {
      console.error('Token refresh failed:', error)
      // Clear session on refresh failure
      clearSession()
      throw error
    }
  }

  /**
   * Clear session data
   */
  function clearSession(): void {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    loginMethod.value = null

    if (typeof window !== 'undefined') {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      localStorage.removeItem('loginMethod')
    }
  }

  /**
   * Check if user has a specific role
   */
  function hasRole(role: string): boolean {
    return user.value?.roles.includes(role) ?? false
  }

  /**
   * Check if user has a specific permission
   */
  function hasPermission(permission: string): boolean {
    return user.value?.permissions?.includes(permission) ?? false
  }

  /**
   * Get access token for API calls
   */
  function getAccessToken(): string | null {
    return accessToken.value
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loginMethod,
    isLoading,
    // Computed
    isAuthenticated,
    // Actions
    initialize,
    loginTraditional,
    loginSSO,
    logout,
    refreshAccessToken,
    hasRole,
    hasPermission,
    getAccessToken,
  }
})
