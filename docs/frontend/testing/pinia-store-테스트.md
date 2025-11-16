# Pinia Store 테스트

## Auth Store 테스트

```typescript
// features/auth/model/auth.store.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import * as authApi from '../api/auth.api'

vi.mock('../api/auth.api')

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })
  
  it('초기 상태', () => {
    const store = useAuthStore()
    
    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('로그인 성공', async () => {
    const store = useAuthStore()
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'mock-token',
        refreshToken: 'mock-refresh',
        user: {
          id: '123',
          username: 'testuser',
          email: 'test@example.com'
        }
      }
    }
    
    vi.mocked(authApi.login).mockResolvedValue(mockResponse)
    
    await store.login('testuser', 'password')
    
    expect(store.accessToken).toBe('mock-token')
    expect(store.user).toEqual(mockResponse.data.user)
    expect(store.isAuthenticated).toBe(true)
  })
  
  it('로그인 실패 시 에러 throw', async () => {
    const store = useAuthStore()
    
    vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))
    
    await expect(store.login('wrong', 'wrong')).rejects.toThrow('Invalid credentials')
    
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('로그아웃', async () => {
    const store = useAuthStore()
    
    // 로그인 상태 설정
    store.$patch({
      accessToken: 'token',
      refreshToken: 'refresh',
      user: { id: '123', username: 'test' }
    })
    
    vi.mocked(authApi.logout).mockResolvedValue({ success: true })
    
    await store.logout()
    
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('토큰 갱신', async () => {
    const store = useAuthStore()
    store.$patch({
      refreshToken: 'old-refresh-token'
    })
    
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'new-access-token'
      }
    }
    
    vi.mocked(authApi.refreshToken).mockResolvedValue(mockResponse)
    
    await store.refreshAccessToken()
    
    expect(store.accessToken).toBe('new-access-token')
  })
  
  it('토큰 검증', () => {
    const store = useAuthStore()
    
    // 만료되지 않은 토큰
    const validToken = generateMockJWT({ exp: Date.now() / 1000 + 3600 })
    store.$patch({ accessToken: validToken })
    expect(store.isTokenValid).toBe(true)
    
    // 만료된 토큰
    const expiredToken = generateMockJWT({ exp: Date.now() / 1000 - 3600 })
    store.$patch({ accessToken: expiredToken })
    expect(store.isTokenValid).toBe(false)
  })
})

// Helper function
function generateMockJWT(payload: any): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.mock-signature`
}
```

---
