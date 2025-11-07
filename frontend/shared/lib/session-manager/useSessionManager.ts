/**
 * Session Manager Composable
 *
 * Manages session lifecycle, automatic token refresh, and activity tracking
 */

import type { SessionConfig, SessionState, SessionEvent, SessionEventHandler } from './types'
import { useAuthStore } from '~/features/auth/model/auth.store'

const DEFAULT_CONFIG: Required<SessionConfig> = {
  refreshInterval: 5 * 60 * 1000, // 5 minutes
  sessionTimeout: 30 * 60 * 1000, // 30 minutes
  warningTime: 2 * 60 * 1000, // 2 minutes before timeout
  autoRefresh: true,
  trackActivity: true,
}

export function useSessionManager(config: SessionConfig = {}) {
  const authStore = useAuthStore()
  const mergedConfig = { ...DEFAULT_CONFIG, ...config }

  // Session state
  const state = reactive<SessionState>({
    lastActivity: null,
    nextRefresh: null,
    expiresAt: null,
    isWarning: false,
    isActive: false,
  })

  // Event handlers
  const eventHandlers = new Map<SessionEventType, Set<SessionEventHandler>>()

  // Timers
  let refreshTimer: ReturnType<typeof setTimeout> | null = null
  let activityTimer: ReturnType<typeof setTimeout> | null = null
  let warningTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * Emit session event
   */
  function emit(type: SessionEventType, data?: unknown) {
    const event: SessionEvent = {
      type,
      timestamp: Date.now(),
      data,
    }

    const handlers = eventHandlers.get(type)
    if (handlers) {
      handlers.forEach(handler => handler(event))
    }

    // Also emit to 'all' handlers
    const allHandlers = eventHandlers.get('activity')
    if (allHandlers && type !== 'activity') {
      allHandlers.forEach(handler => handler(event))
    }
  }

  /**
   * Subscribe to session events
   */
  function on(type: SessionEventType, handler: SessionEventHandler) {
    if (!eventHandlers.has(type)) {
      eventHandlers.set(type, new Set())
    }
    eventHandlers.get(type)!.add(handler)

    // Return unsubscribe function
    return () => {
      eventHandlers.get(type)?.delete(handler)
    }
  }

  /**
   * Update last activity time
   */
  function updateActivity() {
    if (!mergedConfig.trackActivity || !authStore.isAuthenticated)
      return

    state.lastActivity = Date.now()
    emit('activity')

    // Reset activity timer
    if (activityTimer) {
      clearTimeout(activityTimer)
    }

    // Set timeout warning
    const timeUntilWarning = mergedConfig.sessionTimeout - mergedConfig.warningTime
    activityTimer = setTimeout(() => {
      state.isWarning = true
      emit('warning', { timeLeft: mergedConfig.warningTime })
    }, timeUntilWarning)
  }

  /**
   * Schedule token refresh
   */
  function scheduleRefresh() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
    }

    if (!mergedConfig.autoRefresh || !authStore.isAuthenticated)
      return

    state.nextRefresh = Date.now() + mergedConfig.refreshInterval

    refreshTimer = setTimeout(async () => {
      try {
        await authStore.refreshAccessToken()
        emit('refresh')
        scheduleRefresh() // Schedule next refresh
      }
      catch (error) {
        console.error('Token refresh failed:', error)
        emit('error', { type: 'refresh', error })
        // Session expired, logout
        await logout()
      }
    }, mergedConfig.refreshInterval)
  }

  /**
   * Start session
   */
  async function start() {
    if (!authStore.isAuthenticated) {
      console.warn('Cannot start session: user not authenticated')
      return
    }

    state.isActive = true
    state.lastActivity = Date.now()
    state.isWarning = false

    // Calculate expiry time (assuming 1 hour default)
    state.expiresAt = Date.now() + (60 * 60 * 1000)

    emit('login')

    // Start auto-refresh
    if (mergedConfig.autoRefresh) {
      scheduleRefresh()
    }

    // Start activity tracking
    if (mergedConfig.trackActivity) {
      updateActivity()
      setupActivityListeners()
    }
  }

  /**
   * Stop session
   */
  async function stop() {
    state.isActive = false
    state.lastActivity = null
    state.nextRefresh = null
    state.expiresAt = null
    state.isWarning = false

    // Clear all timers
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
    if (activityTimer) {
      clearTimeout(activityTimer)
      activityTimer = null
    }
    if (warningTimer) {
      clearTimeout(warningTimer)
      warningTimer = null
    }

    // Remove activity listeners
    removeActivityListeners()

    emit('logout')
  }

  /**
   * Logout and stop session
   */
  async function logout() {
    await authStore.logout()
    await stop()
  }

  /**
   * Extend session (reset timeout)
   */
  function extend() {
    if (!state.isActive)
      return

    state.isWarning = false
    updateActivity()
    emit('activity', { type: 'extend' })
  }

  /**
   * Setup activity listeners
   */
  function setupActivityListeners() {
    if (typeof window === 'undefined')
      return

    const events = ['mousedown', 'keydown', 'scroll', 'touchstart']
    events.forEach((event) => {
      window.addEventListener(event, updateActivity, { passive: true })
    })
  }

  /**
   * Remove activity listeners
   */
  function removeActivityListeners() {
    if (typeof window === 'undefined')
      return

    const events = ['mousedown', 'keydown', 'scroll', 'touchstart']
    events.forEach((event) => {
      window.removeEventListener(event, updateActivity)
    })
  }

  /**
   * Get time until session expires
   */
  function getTimeUntilExpiry(): number | null {
    if (!state.lastActivity)
      return null

    const elapsed = Date.now() - state.lastActivity
    const remaining = mergedConfig.sessionTimeout - elapsed

    return Math.max(0, remaining)
  }

  /**
   * Get time until next refresh
   */
  function getTimeUntilRefresh(): number | null {
    if (!state.nextRefresh)
      return null

    return Math.max(0, state.nextRefresh - Date.now())
  }

  /**
   * Cleanup on unmount
   */
  onUnmounted(() => {
    removeActivityListeners()
    if (refreshTimer) clearTimeout(refreshTimer)
    if (activityTimer) clearTimeout(activityTimer)
    if (warningTimer) clearTimeout(warningTimer)
  })

  return {
    // State
    state: readonly(state),

    // Methods
    start,
    stop,
    logout,
    extend,
    on,

    // Computed
    isActive: computed(() => state.isActive),
    isWarning: computed(() => state.isWarning),
    timeUntilExpiry: computed(() => getTimeUntilExpiry()),
    timeUntilRefresh: computed(() => getTimeUntilRefresh()),
  }
}

export type SessionManager = ReturnType<typeof useSessionManager>
