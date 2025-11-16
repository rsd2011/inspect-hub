/**
 * Auth Provider Plugin
 *
 * Initializes authentication and restores session on app startup
 */
export default defineNuxtPlugin(() => {
  const authStore = useAuthStore()

  // Restore session from localStorage
  authStore.initialize()
})
