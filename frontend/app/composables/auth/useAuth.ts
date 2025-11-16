/**
 * useAuth Composable
 * 
 * Provides authentication state and utilities
 */
export const useAuth = () => {
  const authStore = useAuthStore()
  const router = useRouter()

  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const user = computed(() => authStore.user)
  const loading = computed(() => authStore.loading)

  const checkAuth = () => {
    if (!authStore.isAuthenticated) {
      router.push('/login')
      return false
    }
    return true
  }

  const requireAuth = () => {
    if (!checkAuth()) {
      throw new Error('Authentication required')
    }
  }

  const hasRole = (role: string) => {
    return authStore.hasRole(role)
  }

  const hasPermission = (permission: string) => {
    return authStore.hasPermission(permission)
  }

  const logout = async () => {
    await authStore.logout()
    router.push('/login')
  }

  return {
    isAuthenticated,
    user,
    loading,
    checkAuth,
    requireAuth,
    hasRole,
    hasPermission,
    logout,
  }
}
