/**
 * Enhanced API Client
 *
 * Features:
 * - Automatic token injection
 * - Automatic token refresh on 401
 * - Request/Response interceptors
 * - Error handling
 * - Retry logic
 * - Request cancellation
 */

import { useAuthStore } from '~/features/auth/model/auth.store'
import type {
  ApiClientConfig,
  RequestConfig,
  RequestInterceptor,
  ResponseInterceptor,
  ErrorInterceptor,
  ApiResponse,
} from './types'

const DEFAULT_CONFIG: Required<Omit<ApiClientConfig, 'baseURL'>> = {
  autoAuth: true,
  autoRefresh: true,
  retry: true,
  retryAttempts: 3,
  retryDelay: 1000,
  timeout: 30000,
}

/**
 * Create enhanced API client
 */
export function createApiClient(config?: Partial<ApiClientConfig>) {
  const runtimeConfig = useRuntimeConfig()
  const authStore = useAuthStore()

  const mergedConfig: Required<ApiClientConfig> = {
    baseURL: config?.baseURL ?? runtimeConfig.public.apiBase,
    ...DEFAULT_CONFIG,
    ...config,
  }

  // Interceptors
  const requestInterceptors: RequestInterceptor[] = []
  const responseInterceptors: ResponseInterceptor[] = []
  const errorInterceptors: ErrorInterceptor[] = []

  /**
   * Add request interceptor
   */
  function addRequestInterceptor(interceptor: RequestInterceptor) {
    requestInterceptors.push(interceptor)
  }

  /**
   * Add response interceptor
   */
  function addResponseInterceptor(interceptor: ResponseInterceptor) {
    responseInterceptors.push(interceptor)
  }

  /**
   * Add error interceptor
   */
  function addErrorInterceptor(interceptor: ErrorInterceptor) {
    errorInterceptors.push(interceptor)
  }

  /**
   * Apply request interceptors
   */
  async function applyRequestInterceptors(config: RequestConfig): Promise<RequestConfig> {
    let modifiedConfig = { ...config }

    for (const interceptor of requestInterceptors) {
      modifiedConfig = await interceptor(modifiedConfig)
    }

    return modifiedConfig
  }

  /**
   * Apply response interceptors
   */
  async function applyResponseInterceptors<T>(response: T): Promise<T> {
    let modifiedResponse = response

    for (const interceptor of responseInterceptors) {
      modifiedResponse = await interceptor(modifiedResponse)
    }

    return modifiedResponse
  }

  /**
   * Apply error interceptors
   */
  async function applyErrorInterceptors(error: Error): Promise<Error> {
    let modifiedError = error

    for (const interceptor of errorInterceptors) {
      try {
        modifiedError = await interceptor(modifiedError)
      }
      catch (err) {
        throw err // Interceptor threw, use that error
      }
    }

    return modifiedError
  }

  /**
   * Build URL with query parameters
   */
  function buildURL(url: string, params?: Record<string, unknown>): string {
    const fullURL = url.startsWith('http') ? url : `${mergedConfig.baseURL}${url}`

    if (!params || Object.keys(params).length === 0) {
      return fullURL
    }

    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        searchParams.append(key, String(value))
      }
    })

    const separator = fullURL.includes('?') ? '&' : '?'
    return `${fullURL}${separator}${searchParams.toString()}`
  }

  /**
   * Make HTTP request with retry logic
   */
  async function makeRequest<T>(
    url: string,
    config: RequestConfig = {},
    attemptNumber = 1,
  ): Promise<T> {
    try {
      // Apply request interceptors
      const interceptedConfig = await applyRequestInterceptors(config)

      // Inject auth token
      if (mergedConfig.autoAuth && !interceptedConfig.skipAuth && authStore.isAuthenticated) {
        const token = authStore.getAccessToken()
        if (token) {
          interceptedConfig.headers = {
            ...interceptedConfig.headers,
            Authorization: `Bearer ${token}`,
          }
        }
      }

      // Build URL with params
      const requestURL = buildURL(url, interceptedConfig.params)

      // Set up abort controller for timeout
      const controller = new AbortController()
      const timeoutId = setTimeout(
        () => controller.abort(),
        interceptedConfig.timeout ?? mergedConfig.timeout,
      )

      const signal = interceptedConfig.signal
        ? interceptedConfig.signal
        : controller.signal

      try {
        // Make request
        const response = await $fetch<T>(requestURL, {
          ...interceptedConfig,
          signal,
        })

        clearTimeout(timeoutId)

        // Apply response interceptors
        return await applyResponseInterceptors(response)
      }
      finally {
        clearTimeout(timeoutId)
      }
    }
    catch (error: unknown) {
      const err = error as Error & { statusCode?: number, data?: unknown }

      // Handle 401 Unauthorized - try to refresh token
      if (
        err.statusCode === 401
        && mergedConfig.autoRefresh
        && !config.skipRefresh
        && authStore.isAuthenticated
      ) {
        try {
          await authStore.refreshAccessToken()
          // Retry request with new token
          return makeRequest<T>(url, { ...config, skipRefresh: true }, attemptNumber)
        }
        catch (refreshError) {
          console.error('Token refresh failed:', refreshError)
          // Logout user
          await authStore.logout()
          throw err
        }
      }

      // Retry logic for network errors
      if (
        mergedConfig.retry
        && !config.skipRetry
        && attemptNumber < mergedConfig.retryAttempts
        && isRetryableError(err)
      ) {
        await sleep(mergedConfig.retryDelay * attemptNumber)
        return makeRequest<T>(url, config, attemptNumber + 1)
      }

      // Apply error interceptors
      const processedError = await applyErrorInterceptors(err)
      throw processedError
    }
  }

  /**
   * Check if error is retryable
   */
  function isRetryableError(error: Error & { statusCode?: number }): boolean {
    // Network errors
    if (error.name === 'FetchError' || error.message.includes('fetch'))
      return true

    // Timeout errors
    if (error.name === 'AbortError' || error.message.includes('timeout'))
      return true

    // Server errors (5xx)
    if (error.statusCode && error.statusCode >= 500)
      return true

    // Too many requests (429)
    if (error.statusCode === 429)
      return true

    return false
  }

  /**
   * Sleep utility
   */
  function sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  /**
   * GET request
   */
  async function get<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    return makeRequest<T>(url, { ...config, method: 'GET' })
  }

  /**
   * POST request
   */
  async function post<T = unknown>(
    url: string,
    body?: RequestConfig['body'],
    config?: RequestConfig,
  ): Promise<T> {
    return makeRequest<T>(url, { ...config, method: 'POST', body })
  }

  /**
   * PUT request
   */
  async function put<T = unknown>(
    url: string,
    body?: RequestConfig['body'],
    config?: RequestConfig,
  ): Promise<T> {
    return makeRequest<T>(url, { ...config, method: 'PUT', body })
  }

  /**
   * PATCH request
   */
  async function patch<T = unknown>(
    url: string,
    body?: RequestConfig['body'],
    config?: RequestConfig,
  ): Promise<T> {
    return makeRequest<T>(url, { ...config, method: 'PATCH', body })
  }

  /**
   * DELETE request
   */
  async function del<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    return makeRequest<T>(url, { ...config, method: 'DELETE' })
  }

  return {
    // HTTP methods
    get,
    post,
    put,
    patch,
    delete: del,
    request: makeRequest,

    // Interceptors
    addRequestInterceptor,
    addResponseInterceptor,
    addErrorInterceptor,

    // Config
    config: readonly(mergedConfig),
  }
}

/**
 * Composable for using API client
 */
export function useApiClient(config?: Partial<ApiClientConfig>) {
  return createApiClient(config)
}

/**
 * Global API client instance
 */
let globalApiClient: ReturnType<typeof createApiClient> | null = null

/**
 * Get or create global API client
 */
export function getApiClient(): ReturnType<typeof createApiClient> {
  if (!globalApiClient) {
    globalApiClient = createApiClient()
  }
  return globalApiClient
}
