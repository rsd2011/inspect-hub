/**
 * Session Manager Types
 *
 * Manages user session lifecycle, token refresh, and activity tracking
 */

export interface SessionConfig {
  /**
   * Token refresh interval in milliseconds
   * @default 300000 (5 minutes)
   */
  refreshInterval?: number

  /**
   * Session timeout in milliseconds (inactive timeout)
   * @default 1800000 (30 minutes)
   */
  sessionTimeout?: number

  /**
   * Warning time before session expires (in milliseconds)
   * @default 120000 (2 minutes)
   */
  warningTime?: number

  /**
   * Auto refresh token when near expiration
   * @default true
   */
  autoRefresh?: boolean

  /**
   * Track user activity
   * @default true
   */
  trackActivity?: boolean
}

export interface SessionState {
  /**
   * Last activity timestamp
   */
  lastActivity: number | null

  /**
   * Next token refresh time
   */
  nextRefresh: number | null

  /**
   * Session expiry time
   */
  expiresAt: number | null

  /**
   * Is session warning shown
   */
  isWarning: boolean

  /**
   * Is session active
   */
  isActive: boolean
}

export type SessionEventType =
  | 'login'
  | 'logout'
  | 'refresh'
  | 'expire'
  | 'warning'
  | 'activity'
  | 'error'

export interface SessionEvent {
  type: SessionEventType
  timestamp: number
  data?: unknown
}

export type SessionEventHandler = (event: SessionEvent) => void
