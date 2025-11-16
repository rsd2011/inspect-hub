/**
 * Admin Authorization Middleware
 *
 * ROLE_SYSTEM_ADMIN 권한이 있는 사용자만 접근 가능
 */
export default defineNuxtRouteMiddleware((to, from) => {
  // Note: 실제 인증/권한 체크는 auth store를 통해 수행해야 함
  // 현재는 미들웨어 구조만 생성 (auth store 구현 후 연동 필요)

  // TODO: Implement actual auth check
  // const authStore = useAuthStore()
  //
  // if (!authStore.isAuthenticated) {
  //   return navigateTo('/login')
  // }
  //
  // if (!authStore.hasRole('ROLE_SYSTEM_ADMIN')) {
  //   return abortNavigation({
  //     statusCode: 403,
  //     statusMessage: 'Access Denied: ROLE_SYSTEM_ADMIN required'
  //   })
  // }

  console.log('[Admin Middleware] Access granted (auth check not implemented yet)')
})
