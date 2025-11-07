/**
 * Permission Manager Types
 */

export type PermissionCheck =
  | string
  | string[]
  | {
      anyOf?: string[]
      allOf?: string[]
      not?: string | string[]
    }

export interface PermissionRule {
  /**
   * Rule name
   */
  name: string

  /**
   * Required roles
   */
  roles?: PermissionCheck

  /**
   * Required permissions
   */
  permissions?: PermissionCheck

  /**
   * Custom check function
   */
  check?: () => boolean | Promise<boolean>
}

export interface PermissionContext {
  /**
   * Feature code (e.g., 'case-management', 'reporting')
   */
  feature?: string

  /**
   * Action code (e.g., 'read', 'write', 'delete')
   */
  action?: string

  /**
   * Resource ID
   */
  resourceId?: string

  /**
   * Additional context data
   */
  data?: Record<string, unknown>
}
