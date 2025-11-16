/**
 * useLogin Composable
 * 
 * Handles login logic and state
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { LoginCredentials } from '~/stores/auth'

export const useLogin = () => {
  const authStore = useAuthStore()
  const router = useRouter()
  const { t } = useI18n()

  const loading = ref(false)
  const error = ref<string | null>(null)

  const loginWithCredentials = async (
    credentials: LoginCredentials,
    method: 'LOCAL' | 'AD' | 'SSO' = 'LOCAL'
  ) => {
    loading.value = true
    error.value = null

    try {
      await authStore.login(credentials, method)
      router.push('/dashboard')
    } catch (err: any) {
      console.error('Login failed:', err)
      
      // Map error codes to user-friendly messages
      const errorMessage = err.data?.message || err.message
      
      if (err.status === 401) {
        error.value = t('auth.errors.invalidCredentials', 'Invalid employee ID or password')
      } else if (err.status === 423) {
        error.value = t('auth.errors.accountLocked', 'Account is locked due to multiple failed attempts')
      } else if (err.status === 403) {
        error.value = t('auth.errors.accountDisabled', 'Account is disabled')
      } else if (errorMessage?.includes('AD')) {
        error.value = t('auth.errors.adUnavailable', 'Active Directory server is unavailable')
      } else if (errorMessage?.includes('SSO')) {
        error.value = t('auth.errors.ssoUnavailable', 'SSO server is unavailable')
      } else {
        error.value = t('auth.errors.generic', 'Login failed. Please try again.')
      }

      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    loading,
    error,
    loginWithCredentials,
    clearError,
  }
}
