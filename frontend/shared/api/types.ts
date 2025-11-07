/**
 * API Client Types
 */

export interface ApiResponse<T = unknown> {
  success: boolean
  data: T
  message?: string
  errors?: ApiError[]
}

export interface ApiError {
  code: string
  message: string
  field?: string
  details?: unknown
}

export interface ApiClientConfig {
  /**
   * Base URL for API requests
   */
  baseURL: string

  /**
   * Automatically inject auth token
   * @default true
   */
  autoAuth?: boolean

  /**
   * Automatically refresh token on 401
   * @default true
   */
  autoRefresh?: boolean

  /**
   * Retry failed requests
   * @default true
   */
  retry?: boolean

  /**
   * Number of retry attempts
   * @default 3
   */
  retryAttempts?: number

  /**
   * Retry delay in milliseconds
   * @default 1000
   */
  retryDelay?: number

  /**
   * Request timeout in milliseconds
   * @default 30000 (30 seconds)
   */
  timeout?: number
}

export interface RequestConfig extends Omit<RequestInit, 'body'> {
  /**
   * Request body
   */
  body?: BodyInit | Record<string, unknown> | null

  /**
   * Query parameters
   */
  params?: Record<string, unknown>

  /**
   * Skip auth token injection
   */
  skipAuth?: boolean

  /**
   * Skip auto refresh
   */
  skipRefresh?: boolean

  /**
   * Skip retry
   */
  skipRetry?: boolean

  /**
   * Request timeout (overrides default)
   */
  timeout?: number

  /**
   * AbortSignal for cancellation
   */
  signal?: AbortSignal
}

export type RequestInterceptor = (config: RequestConfig) => RequestConfig | Promise<RequestConfig>
export type ResponseInterceptor<T = unknown> = (response: T) => T | Promise<T>
export type ErrorInterceptor = (error: Error) => Error | Promise<Error> | never
