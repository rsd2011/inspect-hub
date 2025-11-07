/**
 * Auth Middleware
 * Client-side only (SSR disabled in nuxt.config.ts)
 */
export default defineNuxtRouteMiddleware((to, _from) => {
  const authStore = useAuthStore()

  // Public routes that don't require authentication
  const publicRoutes = ['/login', '/auth/callback', '/auth/sso-callback']

  const isPublicRoute = publicRoutes.some(route => to.path.startsWith(route))

  // If accessing a public route
  if (isPublicRoute) {
    // If already authenticated, redirect to home
    if (authStore.isAuthenticated) {
      return navigateTo('/')
    }
    return
  }

  // For protected routes, check authentication
  if (!authStore.isAuthenticated) {
    return navigateTo('/login')
  }
})
