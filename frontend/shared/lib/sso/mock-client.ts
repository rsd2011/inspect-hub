/**
 * Mock SSO Client Implementation
 *
 * This is a temporary implementation for development and testing.
 * Replace this with the actual SSO library when available.
 */

import type {
  ISSOAuthClient,
  SSOSession,
  SSOUser,
  SSOLoginOptions,
  SSOLogoutOptions,
} from './types'
import { SSOAuthError } from './types'

const SESSION_COOKIE_NAME = 'sso_session_id'
const SESSION_STORAGE_KEY = 'sso_session'

export class MockSSOClient implements ISSOAuthClient {
  private session: SSOSession | null = null
  private initialized = false

  async initialize(): Promise<void> {
    if (this.initialized) return

    // Try to restore session from storage
    if (import.meta.client) {
      const sessionData = localStorage.getItem(SESSION_STORAGE_KEY)
      if (sessionData) {
        try {
          this.session = JSON.parse(sessionData)
          // Validate session expiration
          if (this.session && this.session.expiresAt < Date.now()) {
            this.session = null
            localStorage.removeItem(SESSION_STORAGE_KEY)
          }
        }
        catch {
          // Invalid session data
          localStorage.removeItem(SESSION_STORAGE_KEY)
        }
      }
    }

    this.initialized = true
  }

  async isAuthenticated(): Promise<boolean> {
    await this.initialize()
    return this.session !== null && this.session.expiresAt > Date.now()
  }

  async getSession(): Promise<SSOSession | null> {
    await this.initialize()
    if (this.session && this.session.expiresAt < Date.now()) {
      this.session = null
      if (import.meta.client) {
        localStorage.removeItem(SESSION_STORAGE_KEY)
      }
      return null
    }
    return this.session
  }

  async getUser(): Promise<SSOUser | null> {
    const session = await this.getSession()
    return session?.user ?? null
  }

  async login(_options?: SSOLoginOptions): Promise<void> {
    // Mock login - in real implementation, this would redirect to SSO provider
    // For now, we'll simulate with a mock user
    const mockUser: SSOUser = {
      userId: 'mock-user-001',
      username: 'testuser',
      email: 'testuser@example.com',
      displayName: 'Test User',
      roles: ['user', 'admin'],
      permissions: ['read', 'write', 'admin'],
      organizationId: 'org-001',
      organizationName: 'Test Organization',
    }

    const now = Date.now()
    this.session = {
      sessionId: `session-${now}`,
      user: mockUser,
      expiresAt: now + 8 * 60 * 60 * 1000, // 8 hours
      createdAt: now,
      lastActivityAt: now,
    }

    if (import.meta.client) {
      localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(this.session))
      // Set cookie for server-side validation
      document.cookie = `${SESSION_COOKIE_NAME}=${this.session.sessionId}; path=/; max-age=${8 * 60 * 60}; samesite=strict`
    }
  }

  async handleLoginCallback(): Promise<SSOSession> {
    // Mock callback handler
    // In real implementation, this would validate the callback from SSO provider
    if (!this.session) {
      throw new SSOAuthError('No session found', 'NO_SESSION')
    }
    return this.session
  }

  async logout(options?: SSOLogoutOptions): Promise<void> {
    this.session = null

    if (import.meta.client) {
      localStorage.removeItem(SESSION_STORAGE_KEY)
      // Clear cookie
      document.cookie = `${SESSION_COOKIE_NAME}=; path=/; max-age=0`
    }

    // In real implementation, redirect to SSO provider's logout if global logout
    if (options?.global) {
      // Redirect to SSO logout endpoint
    }
  }

  async refreshSession(): Promise<SSOSession> {
    if (!this.session) {
      throw new SSOAuthError('No session to refresh', 'NO_SESSION')
    }

    // Mock refresh - extend session
    const now = Date.now()
    this.session = {
      ...this.session,
      expiresAt: now + 8 * 60 * 60 * 1000, // 8 hours
      lastActivityAt: now,
    }

    if (import.meta.client) {
      localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(this.session))
      document.cookie = `${SESSION_COOKIE_NAME}=${this.session.sessionId}; path=/; max-age=${8 * 60 * 60}; samesite=strict`
    }

    return this.session
  }

  async validateSession(): Promise<boolean> {
    const session = await this.getSession()
    return session !== null && session.expiresAt > Date.now()
  }

  async getAccessToken(): Promise<string | null> {
    const session = await this.getSession()
    if (!session) return null
    // In real implementation, return actual access token
    return `mock-token-${session.sessionId}`
  }
}

// Singleton instance
let ssoClientInstance: ISSOAuthClient | null = null

/**
 * Get or create SSO client instance
 */
export function getSSOClient(): ISSOAuthClient {
  if (!ssoClientInstance) {
    ssoClientInstance = new MockSSOClient()
  }
  return ssoClientInstance
}

/**
 * Reset SSO client (for testing)
 */
export function resetSSOClient(): void {
  ssoClientInstance = null
}
