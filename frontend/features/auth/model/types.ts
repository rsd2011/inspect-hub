/**
 * Authentication related types
 * Supports both traditional login and SSO login
 */

/**
 * Traditional Login Request (username/password)
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * SSO Login Request
 */
export interface SSOLoginRequest {
  ssoToken: string
  provider?: string // e.g., 'okta', 'azure-ad', 'custom-sso'
}

/**
 * Common Login Response for both traditional and SSO login
 */
export interface LoginResponse {
  userId: string
  username: string
  email?: string
  displayName?: string
  accessToken: string
  refreshToken: string
  roles: string[]
  permissions?: string[]
  organizationId?: string
  organizationName?: string
  expiresIn: number
  tokenType: string
  loginMethod: 'traditional' | 'sso'
}

/**
 * Token Refresh Request
 */
export interface TokenRefreshRequest {
  refreshToken: string
}

/**
 * Token Refresh Response
 */
export interface TokenRefreshResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

/**
 * User information
 */
export interface User {
  userId: string
  username: string
  email?: string
  displayName?: string
  roles: string[]
  permissions?: string[]
  organizationId?: string
  organizationName?: string
}
