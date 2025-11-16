/**
 * 로그인 방식 열거형
 */
export type LoginMethod = 'SSO' | 'AD' | 'LOCAL'

/**
 * 로그인 정책 (Domain Model)
 */
export interface LoginPolicy {
  id: string
  name: string
  enabledMethods: LoginMethod[]
  priority: LoginMethod[]
  active: boolean
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
}

/**
 * 로그인 정책 업데이트 요청 DTO
 */
export interface UpdateLoginPolicyRequest {
  name?: string
  enabledMethods?: LoginMethod[]
  priority?: LoginMethod[]
}

/**
 * API Response 공통 포맷
 */
export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  errorCode?: string
  timestamp?: string
}
