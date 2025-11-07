/**
 * SSO Authentication Library Interface
 *
 * This interface defines the contract for SSO authentication.
 * The actual implementation will be provided by an external library.
 */

/**
 * SSO User information returned after successful authentication
 */
export interface SSOUser {
  userId: string
  username: string
  email?: string
  displayName?: string
  roles: string[]
  permissions?: string[]
  organizationId?: string
  organizationName?: string
  metadata?: Record<string, unknown>
}

/**
 * SSO Session information
 */
export interface SSOSession {
  sessionId: string
  user: SSOUser
  expiresAt: number // Unix timestamp
  createdAt: number // Unix timestamp
  lastActivityAt: number // Unix timestamp
}

/**
 * SSO Login options
 */
export interface SSOLoginOptions {
  redirectUrl?: string
  state?: string
  locale?: string
}

/**
 * SSO Logout options
 */
export interface SSOLogoutOptions {
  redirectUrl?: string
  global?: boolean // Global logout from all applications
}

/**
 * SSO Authentication Error
 */
export class SSOAuthError extends Error {
  constructor(
    message: string,
    public code: string,
    public details?: Record<string, unknown>
  ) {
    super(message)
    this.name = 'SSOAuthError'
  }
}

/**
 * SSO Authentication Library Interface
 *
 * This will be implemented by the external SSO library.
 */
export interface ISSOAuthClient {
  /**
   * Initialize SSO client
   */
  initialize(): Promise<void>

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): Promise<boolean>

  /**
   * Get current session
   */
  getSession(): Promise<SSOSession | null>

  /**
   * Get current user
   */
  getUser(): Promise<SSOUser | null>

  /**
   * Login with SSO
   * This typically redirects to SSO provider's login page
   */
  login(options?: SSOLoginOptions): Promise<void>

  /**
   * Handle login callback after redirect from SSO provider
   * This should be called in the callback route
   */
  handleLoginCallback(): Promise<SSOSession>

  /**
   * Logout
   */
  logout(options?: SSOLogoutOptions): Promise<void>

  /**
   * Refresh session
   */
  refreshSession(): Promise<SSOSession>

  /**
   * Validate session
   */
  validateSession(): Promise<boolean>

  /**
   * Get access token for API calls
   */
  getAccessToken(): Promise<string | null>
}
