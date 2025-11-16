/**
 * Auth Middleware
 * 
 * Protects routes that require authentication
 */
export default defineNuxtRouteMiddleware((_to, _from) => {
  const authStore = useAuthStore()

  // If user is not authenticated, redirect to login
  if (!authStore.isAuthenticated) {
    return navigateTo('/login')
  }
})
