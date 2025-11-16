/**
 * useLoginPolicy Composable
 * 
 * Determines which login method to use based on policy
 */
import { ref } from 'vue'

export interface LoginPolicy {
  ssoEnabled: boolean
  ssoUrl?: string
  adEnabled: boolean
  adServerUrl?: string
  localLoginEnabled: boolean
  preferredMethod: 'SSO' | 'AD' | 'LOCAL'
}

export const useLoginPolicy = () => {
  const config = useRuntimeConfig()
  const loading = ref(false)
  const policy = ref<LoginPolicy | null>(null)

  const fetchLoginPolicy = async () => {
    loading.value = true
    try {
      const response = await $fetch<LoginPolicy>(`${config.public.apiBase}/auth/login-policy`)
      policy.value = response
      return response
    } catch (error) {
      console.error('Failed to fetch login policy:', error)
      // Fallback to local login only
      policy.value = {
        ssoEnabled: false,
        adEnabled: false,
        localLoginEnabled: true,
        preferredMethod: 'LOCAL',
      }
      return policy.value
    } finally {
      loading.value = false
    }
  }

  const getAvailableMethods = computed(() => {
    if (!policy.value) return []

    const methods: Array<'SSO' | 'AD' | 'LOCAL'> = []

    if (policy.value.ssoEnabled) methods.push('SSO')
    if (policy.value.adEnabled) methods.push('AD')
    if (policy.value.localLoginEnabled) methods.push('LOCAL')

    return methods
  })

  const shouldShowSsoButton = computed(() => policy.value?.ssoEnabled ?? false)
  const shouldShowAdLogin = computed(() => policy.value?.adEnabled ?? false)
  const shouldShowLocalLogin = computed(() => policy.value?.localLoginEnabled ?? true)

  return {
    loading,
    policy,
    fetchLoginPolicy,
    getAvailableMethods,
    shouldShowSsoButton,
    shouldShowAdLogin,
    shouldShowLocalLogin,
  }
}
