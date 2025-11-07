/**
 * Permission Manager Composable
 *
 * Centralized permission checking with role and permission-based access control
 */

import type { PermissionCheck, PermissionRule, PermissionContext } from './types'
import { useAuthStore } from '~/features/auth/model/auth.store'

export function usePermissionManager() {
  const authStore = useAuthStore()

  /**
   * Check if user has specific role(s)
   */
  function hasRole(check: PermissionCheck): boolean {
    if (typeof check === 'string') {
      return authStore.hasRole(check)
    }

    if (Array.isArray(check)) {
      return check.some(role => authStore.hasRole(role))
    }

    if (check.anyOf) {
      return check.anyOf.some(role => authStore.hasRole(role))
    }

    if (check.allOf) {
      return check.allOf.every(role => authStore.hasRole(role))
    }

    if (check.not) {
      const notCheck = Array.isArray(check.not) ? check.not : [check.not]
      return !notCheck.some(role => authStore.hasRole(role))
    }

    return false
  }

  /**
   * Check if user has specific permission(s)
   */
  function hasPermission(check: PermissionCheck): boolean {
    if (typeof check === 'string') {
      return authStore.hasPermission(check)
    }

    if (Array.isArray(check)) {
      return check.some(permission => authStore.hasPermission(permission))
    }

    if (check.anyOf) {
      return check.anyOf.some(permission => authStore.hasPermission(permission))
    }

    if (check.allOf) {
      return check.allOf.every(permission => authStore.hasPermission(permission))
    }

    if (check.not) {
      const notCheck = Array.isArray(check.not) ? check.not : [check.not]
      return !notCheck.some(permission => authStore.hasPermission(permission))
    }

    return false
  }

  /**
   * Check permission rule
   */
  async function checkRule(rule: PermissionRule): Promise<boolean> {
    // Check roles
    if (rule.roles && !hasRole(rule.roles)) {
      return false
    }

    // Check permissions
    if (rule.permissions && !hasPermission(rule.permissions)) {
      return false
    }

    // Check custom function
    if (rule.check) {
      const result = await rule.check()
      if (!result) {
        return false
      }
    }

    return true
  }

  /**
   * Check feature access with context
   */
  function canAccess(context: PermissionContext): boolean {
    if (!authStore.isAuthenticated) {
      return false
    }

    // Build permission string: FEATURE_ACTION
    const { feature, action } = context

    if (feature && action) {
      const permissionString = `${feature.toUpperCase()}_${action.toUpperCase()}`
      return hasPermission(permissionString)
    }

    // If only feature is provided, check if user has any permission for that feature
    if (feature) {
      const user = authStore.user
      if (!user?.permissions) {
        return false
      }

      return user.permissions.some(perm =>
        perm.startsWith(`${feature.toUpperCase()}_`),
      )
    }

    return false
  }

  /**
   * Check if user can perform action on resource
   */
  function can(action: string, feature?: string): boolean {
    return canAccess({ feature, action })
  }

  /**
   * Check if user is admin
   */
  function isAdmin(): boolean {
    return hasRole('ROLE_ADMIN')
  }

  /**
   * Check if user is superadmin
   */
  function isSuperAdmin(): boolean {
    return hasRole('ROLE_SUPERADMIN')
  }

  return {
    hasRole,
    hasPermission,
    checkRule,
    canAccess,
    can,
    isAdmin,
    isSuperAdmin,

    // Computed
    isAuthenticated: computed(() => authStore.isAuthenticated),
    user: computed(() => authStore.user),
  }
}

export type PermissionManager = ReturnType<typeof usePermissionManager>
