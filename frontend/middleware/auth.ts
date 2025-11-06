export default defineNuxtRouteMiddleware(() => {
  // Client-side only
  if (process.server) return

  const authStore = useAuthStore()

  // Restore session from localStorage
  authStore.restoreSession()

  // Check if user is authenticated
  if (!authStore.isAuthenticated) {
    // Redirect to login page
    return navigateTo('/login')
  }
})
