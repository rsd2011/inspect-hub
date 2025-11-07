import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth.store'
import { authApi } from '../../api/auth.api'
import type { LoginResponse } from '../types'

// Mock authApi
vi.mock('../../api/auth.api', () => ({
  authApi: {
    login: vi.fn(),
    loginSSO: vi.fn(),
    logout: vi.fn(),
    refreshToken: vi.fn(),
  },
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('Initial State', () => {
    it('should have initial state with no user', () => {
      const store = useAuthStore()

      expect(store.user).toBeNull()
      expect(store.accessToken).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(store.loginMethod).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(store.isLoading).toBe(false)
    })
  })

  describe('initialize', () => {
    it('should restore session from localStorage', () => {
      const mockUser = {
        userId: 'USER-001',
        username: 'admin',
        email: 'admin@example.com',
        displayName: 'Admin User',
        roles: ['ROLE_ADMIN'],
        permissions: ['USER_READ', 'USER_WRITE'],
      }

      localStorage.setItem('accessToken', 'mock-access-token')
      localStorage.setItem('refreshToken', 'mock-refresh-token')
      localStorage.setItem('user', JSON.stringify(mockUser))
      localStorage.setItem('loginMethod', 'traditional')

      const store = useAuthStore()
      store.initialize()

      expect(store.accessToken).toBe('mock-access-token')
      expect(store.refreshToken).toBe('mock-refresh-token')
      expect(store.user).toEqual(mockUser)
      expect(store.loginMethod).toBe('traditional')
      expect(store.isAuthenticated).toBe(true)
    })

    it('should handle missing localStorage data', () => {
      const store = useAuthStore()
      store.initialize()

      expect(store.user).toBeNull()
      expect(store.accessToken).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(store.isAuthenticated).toBe(false)
    })

    it('should handle invalid JSON in localStorage', () => {
      localStorage.setItem('accessToken', 'mock-access-token')
      localStorage.setItem('refreshToken', 'mock-refresh-token')
      localStorage.setItem('user', 'invalid-json')

      const store = useAuthStore()
      store.initialize()

      expect(store.user).toBeNull()
      expect(store.accessToken).toBeNull()
      expect(store.isAuthenticated).toBe(false)
    })
  })

  describe('loginTraditional', () => {
    it('should login successfully with username/password', async () => {
      const mockResponse: LoginResponse = {
        userId: 'USER-001',
        username: 'admin',
        email: 'admin@example.com',
        displayName: 'Admin User',
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        roles: ['ROLE_ADMIN'],
        permissions: ['USER_READ', 'USER_WRITE'],
        organizationId: 'ORG-001',
        organizationName: 'Test Org',
        expiresIn: 3600000,
        tokenType: 'Bearer',
        loginMethod: 'traditional',
      }

      vi.mocked(authApi.login).mockResolvedValue(mockResponse)

      const store = useAuthStore()
      await store.loginTraditional({ username: 'admin', password: 'password' })

      expect(authApi.login).toHaveBeenCalledWith({ username: 'admin', password: 'password' })
      expect(store.accessToken).toBe('mock-access-token')
      expect(store.refreshToken).toBe('mock-refresh-token')
      expect(store.loginMethod).toBe('traditional')
      expect(store.user).toEqual({
        userId: 'USER-001',
        username: 'admin',
        email: 'admin@example.com',
        displayName: 'Admin User',
        roles: ['ROLE_ADMIN'],
        permissions: ['USER_READ', 'USER_WRITE'],
        organizationId: 'ORG-001',
        organizationName: 'Test Org',
      })
      expect(store.isAuthenticated).toBe(true)
      expect(store.isLoading).toBe(false)

      // Verify localStorage
      expect(localStorage.getItem('accessToken')).toBe('mock-access-token')
      expect(localStorage.getItem('refreshToken')).toBe('mock-refresh-token')
      expect(localStorage.getItem('loginMethod')).toBe('traditional')
    })

    it('should handle login errors', async () => {
      vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))

      const store = useAuthStore()

      await expect(store.loginTraditional({ username: 'admin', password: 'wrong' }))
        .rejects.toThrow('Invalid credentials')

      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(store.isLoading).toBe(false)
    })
  })

  describe('loginSSO', () => {
    it('should login successfully with SSO token', async () => {
      const mockResponse: LoginResponse = {
        userId: 'SSO-USER-001',
        username: 'sso.user',
        email: 'sso@example.com',
        displayName: 'SSO User',
        accessToken: 'sso-access-token',
        refreshToken: 'sso-refresh-token',
        roles: ['ROLE_USER'],
        permissions: ['CASE_READ'],
        organizationId: 'ORG-PARTNER-001',
        organizationName: 'Partner Org',
        expiresIn: 3600000,
        tokenType: 'Bearer',
        loginMethod: 'sso',
      }

      vi.mocked(authApi.loginSSO).mockResolvedValue(mockResponse)

      const store = useAuthStore()
      await store.loginSSO({ ssoToken: 'test-sso-token', provider: 'okta' })

      expect(authApi.loginSSO).toHaveBeenCalledWith({ ssoToken: 'test-sso-token', provider: 'okta' })
      expect(store.accessToken).toBe('sso-access-token')
      expect(store.refreshToken).toBe('sso-refresh-token')
      expect(store.loginMethod).toBe('sso')
      expect(store.user?.username).toBe('sso.user')
      expect(store.isAuthenticated).toBe(true)
      expect(store.isLoading).toBe(false)

      // Verify localStorage
      expect(localStorage.getItem('accessToken')).toBe('sso-access-token')
      expect(localStorage.getItem('loginMethod')).toBe('sso')
    })

    it('should handle SSO login errors', async () => {
      vi.mocked(authApi.loginSSO).mockRejectedValue(new Error('Invalid SSO token'))

      const store = useAuthStore()

      await expect(store.loginSSO({ ssoToken: 'invalid-token' }))
        .rejects.toThrow('Invalid SSO token')

      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
    })
  })

  describe('logout', () => {
    it('should logout and clear session data', async () => {
      // Setup authenticated state
      const mockUser = {
        userId: 'USER-001',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
      }

      localStorage.setItem('accessToken', 'mock-access-token')
      localStorage.setItem('refreshToken', 'mock-refresh-token')
      localStorage.setItem('user', JSON.stringify(mockUser))

      const store = useAuthStore()
      store.initialize()

      expect(store.isAuthenticated).toBe(true)

      vi.mocked(authApi.logout).mockResolvedValue()

      await store.logout()

      expect(authApi.logout).toHaveBeenCalledWith('mock-access-token')
      expect(store.user).toBeNull()
      expect(store.accessToken).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(store.loginMethod).toBeNull()
      expect(store.isAuthenticated).toBe(false)

      // Verify localStorage is cleared
      expect(localStorage.getItem('accessToken')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
      expect(localStorage.getItem('user')).toBeNull()
      expect(localStorage.getItem('loginMethod')).toBeNull()
    })

    it('should clear session even if API call fails', async () => {
      localStorage.setItem('accessToken', 'mock-access-token')

      const store = useAuthStore()
      store.initialize()

      vi.mocked(authApi.logout).mockRejectedValue(new Error('Network error'))

      await store.logout()

      // Should still clear local session
      expect(store.user).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(localStorage.getItem('accessToken')).toBeNull()
    })
  })

  describe('refreshAccessToken', () => {
    it('should refresh tokens successfully', async () => {
      // Set up authenticated state with tokens
      localStorage.setItem('accessToken', 'old-access-token')
      localStorage.setItem('refreshToken', 'old-refresh-token')
      localStorage.setItem('user', JSON.stringify({ userId: 'USER-001', username: 'admin', roles: ['ROLE_ADMIN'] }))

      const store = useAuthStore()
      store.initialize()

      vi.mocked(authApi.refreshToken).mockResolvedValue({
        accessToken: 'new-access-token',
        refreshToken: 'new-refresh-token',
        expiresIn: 3600000,
      })

      await store.refreshAccessToken()

      expect(authApi.refreshToken).toHaveBeenCalledWith({ refreshToken: 'old-refresh-token' })
      expect(store.accessToken).toBe('new-access-token')
      expect(store.refreshToken).toBe('new-refresh-token')
      expect(localStorage.getItem('accessToken')).toBe('new-access-token')
      expect(localStorage.getItem('refreshToken')).toBe('new-refresh-token')
    })

    it('should throw error if no refresh token', async () => {
      const store = useAuthStore()

      await expect(store.refreshAccessToken()).rejects.toThrow('No refresh token available')
    })

    it('should clear session on refresh failure', async () => {
      // Set up authenticated state with tokens
      localStorage.setItem('accessToken', 'old-access-token')
      localStorage.setItem('refreshToken', 'expired-token')
      localStorage.setItem('user', JSON.stringify({ userId: 'USER-001', username: 'admin', roles: ['ROLE_ADMIN'] }))

      const store = useAuthStore()
      store.initialize()

      vi.mocked(authApi.refreshToken).mockRejectedValue(new Error('Token expired'))

      await expect(store.refreshAccessToken()).rejects.toThrow('Token expired')

      expect(store.accessToken).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(localStorage.getItem('accessToken')).toBeNull()
    })
  })

  describe('hasRole', () => {
    it('should check user role correctly', () => {
      const store = useAuthStore()
      store.user = {
        userId: 'USER-001',
        username: 'admin',
        roles: ['ROLE_ADMIN', 'ROLE_USER'],
      }

      expect(store.hasRole('ROLE_ADMIN')).toBe(true)
      expect(store.hasRole('ROLE_USER')).toBe(true)
      expect(store.hasRole('ROLE_SUPERADMIN')).toBe(false)
    })

    it('should return false when user is null', () => {
      const store = useAuthStore()

      expect(store.hasRole('ROLE_ADMIN')).toBe(false)
    })
  })

  describe('hasPermission', () => {
    it('should check user permission correctly', () => {
      const store = useAuthStore()
      store.user = {
        userId: 'USER-001',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
        permissions: ['USER_READ', 'USER_WRITE', 'CASE_READ'],
      }

      expect(store.hasPermission('USER_READ')).toBe(true)
      expect(store.hasPermission('USER_WRITE')).toBe(true)
      expect(store.hasPermission('CASE_DELETE')).toBe(false)
    })

    it('should return false when user has no permissions', () => {
      const store = useAuthStore()
      store.user = {
        userId: 'USER-001',
        username: 'admin',
        roles: ['ROLE_ADMIN'],
      }

      expect(store.hasPermission('USER_READ')).toBe(false)
    })

    it('should return false when user is null', () => {
      const store = useAuthStore()

      expect(store.hasPermission('USER_READ')).toBe(false)
    })
  })

  describe('getAccessToken', () => {
    it('should return access token', () => {
      const store = useAuthStore()
      store.accessToken = 'mock-access-token'

      expect(store.getAccessToken()).toBe('mock-access-token')
    })

    it('should return null when not authenticated', () => {
      const store = useAuthStore()

      expect(store.getAccessToken()).toBeNull()
    })
  })
})
