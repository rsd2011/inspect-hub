/**
 * useApi Composable
 * 
 * Provides API utilities with authentication
 */
export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()
  const router = useRouter()

  const createHeaders = (): HeadersInit => {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (authStore.accessToken) {
      headers['Authorization'] = `Bearer ${authStore.accessToken}`
    }

    return headers
  }

  const handleApiError = async (error: any) => {
    console.error('API Error:', error)

    // Handle 401 Unauthorized - try to refresh token
    if (error.status === 401 && authStore.refreshToken) {
      try {
        await authStore.refreshAccessToken()
        // Retry the original request would be handled by caller
        return
      } catch (refreshError) {
        // Refresh failed, logout
        await authStore.logout()
        router.push('/login')
        throw refreshError
      }
    }

    // Handle 403 Forbidden
    if (error.status === 403) {
      throw new Error('You do not have permission to perform this action')
    }

    throw error
  }

  const get = async <T = any>(endpoint: string, options: any = {}): Promise<T> => {
    try {
      return await $fetch<T>(`${config.public.apiBase}${endpoint}`, {
        method: 'GET',
        headers: createHeaders(),
        ...options,
      })
    } catch (error) {
      await handleApiError(error)
      throw error
    }
  }

  const post = async <T = any>(endpoint: string, body: any, options: any = {}): Promise<T> => {
    try {
      return await $fetch<T>(`${config.public.apiBase}${endpoint}`, {
        method: 'POST',
        headers: createHeaders(),
        body,
        ...options,
      })
    } catch (error) {
      await handleApiError(error)
      throw error
    }
  }

  const put = async <T = any>(endpoint: string, body: any, options: any = {}): Promise<T> => {
    try {
      return await $fetch<T>(`${config.public.apiBase}${endpoint}`, {
        method: 'PUT',
        headers: createHeaders(),
        body,
        ...options,
      })
    } catch (error) {
      await handleApiError(error)
      throw error
    }
  }

  const patch = async <T = any>(endpoint: string, body: any, options: any = {}): Promise<T> => {
    try {
      return await $fetch<T>(`${config.public.apiBase}${endpoint}`, {
        method: 'PATCH',
        headers: createHeaders(),
        body,
        ...options,
      })
    } catch (error) {
      await handleApiError(error)
      throw error
    }
  }

  const del = async <T = any>(endpoint: string, options: any = {}): Promise<T> => {
    try {
      return await $fetch<T>(`${config.public.apiBase}${endpoint}`, {
        method: 'DELETE',
        headers: createHeaders(),
        ...options,
      })
    } catch (error) {
      await handleApiError(error)
      throw error
    }
  }

  return {
    get,
    post,
    put,
    patch,
    delete: del,
  }
}
