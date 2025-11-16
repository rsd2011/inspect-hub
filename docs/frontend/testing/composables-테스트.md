# Composables 테스트

## usePermission Composable 테스트

```typescript
// shared/lib/composables/usePermission.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermission } from './usePermission'
import { useAuthStore } from '~/features/auth/model/auth.store'

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  
  it('hasRole - 권한 확인', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        roles: ['ROLE_ADMIN', 'ROLE_USER']
      }
    })
    
    const { hasRole } = usePermission()
    
    expect(hasRole('ROLE_ADMIN')).toBe(true)
    expect(hasRole('ROLE_USER')).toBe(true)
    expect(hasRole('ROLE_COMPLIANCE')).toBe(false)
  })
  
  it('hasPermission - 권한 확인', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read', 'user:write', 'case:read']
      }
    })
    
    const { hasPermission } = usePermission()
    
    expect(hasPermission('user:read')).toBe(true)
    expect(hasPermission('user:write')).toBe(true)
    expect(hasPermission('user:delete')).toBe(false)
  })
  
  it('hasAnyPermission - 여러 권한 중 하나라도 있으면 true', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read']
      }
    })
    
    const { hasAnyPermission } = usePermission()
    
    expect(hasAnyPermission(['user:read', 'user:write'])).toBe(true)
    expect(hasAnyPermission(['user:write', 'user:delete'])).toBe(false)
  })
  
  it('hasAllPermissions - 모든 권한이 있어야 true', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read', 'user:write']
      }
    })
    
    const { hasAllPermissions } = usePermission()
    
    expect(hasAllPermissions(['user:read', 'user:write'])).toBe(true)
    expect(hasAllPermissions(['user:read', 'user:delete'])).toBe(false)
  })
  
  it('canAccessMenu - 메뉴 접근 권한', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['menu:users', 'menu:cases']
      }
    })
    
    const { canAccessMenu } = usePermission()
    
    expect(canAccessMenu('users')).toBe(true)
    expect(canAccessMenu('cases')).toBe(true)
    expect(canAccessMenu('reports')).toBe(false)
  })
})
```

---
