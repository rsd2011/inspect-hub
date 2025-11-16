# API 모킹

## MSW 핸들러 설정

```typescript
// tests/mocks/handlers.ts
import { http, HttpResponse } from 'msw'

export const handlers = [
  // Auth
  http.post('http://localhost:8090/api/v1/auth/login', async ({ request }) => {
    const { username, password } = await request.json()
    
    if (username === 'admin' && password === 'admin123') {
      return HttpResponse.json({
        success: true,
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          user: {
            id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
            username: 'admin',
            email: 'admin@example.com',
            fullName: '관리자',
            roles: ['ROLE_ADMIN'],
            permissions: ['user:read', 'user:write', 'case:read', 'case:write']
          }
        }
      })
    }
    
    return HttpResponse.json(
      {
        success: false,
        error: {
          code: 'INVALID_CREDENTIALS',
          message: '사용자명 또는 비밀번호가 올바르지 않습니다'
        }
      },
      { status: 401 }
    )
  }),
  
  // Users
  http.get('http://localhost:8090/api/v1/users', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '20')
    
    return HttpResponse.json({
      success: true,
      data: {
        items: generateMockUsers(size),
        pagination: {
          page,
          size,
          totalElements: 100,
          totalPages: Math.ceil(100 / size),
          hasNext: page < Math.ceil(100 / size),
          hasPrevious: page > 1
        }
      }
    })
  }),
  
  http.get('http://localhost:8090/api/v1/users/:id', ({ params }) => {
    return HttpResponse.json({
      success: true,
      data: {
        id: params.id,
        username: 'testuser',
        email: 'test@example.com',
        fullName: '테스트 사용자',
        status: 'ACTIVE',
        orgName: '본사',
        permGroups: ['일반 사용자']
      }
    })
  }),
  
  http.post('http://localhost:8090/api/v1/users', async ({ request }) => {
    const body = await request.json()
    
    return HttpResponse.json(
      {
        success: true,
        data: {
          id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
          ...body,
          status: 'ACTIVE',
          createdAt: new Date().toISOString()
        }
      },
      { status: 201 }
    )
  }),
  
  // Cases
  http.get('http://localhost:8090/api/v1/cases', () => {
    return HttpResponse.json({
      success: true,
      data: {
        items: generateMockCases(20),
        pagination: {
          page: 1,
          size: 20,
          totalElements: 45,
          totalPages: 3,
          hasNext: true,
          hasPrevious: false
        }
      }
    })
  })
]

// Mock data generators
function generateMockUsers(count: number) {
  return Array.from({ length: count }, (_, i) => ({
    id: `USER_${i + 1}`,
    username: `user${i + 1}`,
    email: `user${i + 1}@example.com`,
    fullName: `사용자 ${i + 1}`,
    status: 'ACTIVE',
    orgName: '본사',
    createdAt: new Date().toISOString()
  }))
}

function generateMockCases(count: number) {
  const statuses = ['NEW', 'IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED', 'REJECTED']
  const riskLevels = ['HIGH', 'MEDIUM', 'LOW']
  
  return Array.from({ length: count }, (_, i) => ({
    id: `CASE_${i + 1}`,
    caseNumber: `STR-2025-${String(i + 1).padStart(4, '0')}`,
    customerName: `고객 ${i + 1}`,
    amount: Math.floor(Math.random() * 100000000),
    riskLevel: riskLevels[Math.floor(Math.random() * riskLevels.length)],
    status: statuses[Math.floor(Math.random() * statuses.length)],
    assignee: `담당자 ${i % 5 + 1}`,
    createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
  }))
}
```

## MSW 서버 설정

```typescript
// tests/mocks/server.ts
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)
```

## 테스트에서 MSW 사용

```typescript
// tests/setup.ts 에 추가
import { beforeAll, afterEach, afterAll } from 'vitest'
import { server } from './mocks/server'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
```

---
