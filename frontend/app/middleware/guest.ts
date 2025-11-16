/**
 * Guest Middleware
 * 
 * Redirects authenticated users away from guest-only pages (like login)
 */
export default defineNuxtRouteMiddleware((_to, _from) => {
  const authStore = useAuthStore()

  // If user is authenticated, redirect to dashboard
  if (authStore.isAuthenticated) {
    return navigateTo('/dashboard')
  }
})
