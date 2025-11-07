/**
 * MSW Request Handlers
 *
 * Mock API responses for E2E tests
 */
import { http, HttpResponse } from 'msw'

const API_BASE = 'http://localhost:8090/api/v1'

export const handlers = [
  // Authentication - Login
  http.post(`${API_BASE}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string }

    // Valid credentials
    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({
        success: true,
        data: {
          accessToken: 'mock-jwt-token-12345',
          refreshToken: 'mock-refresh-token-67890',
          user: {
            id: 'user-001',
            username: 'admin',
            name: '관리자',
            email: 'admin@inspecthub.com',
            roles: ['ROLE_ADMIN', 'ROLE_USER'],
            permissions: ['*'],
          },
        },
      })
    }

    // Invalid credentials
    return HttpResponse.json(
      {
        success: false,
        error: {
          code: 'AUTH_001',
          message: '로그인에 실패했습니다. SSO 인증에 실패했습니다.',
        },
      },
      { status: 401 }
    )
  }),

  // Authentication - Refresh Token
  http.post(`${API_BASE}/auth/refresh`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        accessToken: 'mock-jwt-token-new-12345',
        refreshToken: 'mock-refresh-token-new-67890',
      },
    })
  }),

  // Authentication - Logout
  http.post(`${API_BASE}/auth/logout`, () => {
    return HttpResponse.json({
      success: true,
      message: '로그아웃되었습니다.',
    })
  }),

  // User Info
  http.get(`${API_BASE}/auth/me`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: 'user-001',
        username: 'admin',
        name: '관리자',
        email: 'admin@inspecthub.com',
        roles: ['ROLE_ADMIN', 'ROLE_USER'],
        permissions: ['*'],
      },
    })
  }),

  // Dashboard Stats
  http.get(`${API_BASE}/dashboard/stats`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        str: {
          total: 1234,
          pending: 45,
          approved: 1150,
          rejected: 39,
          trend: 12.5,
        },
        ctr: {
          total: 5678,
          pending: 89,
          approved: 5520,
          rejected: 69,
          trend: -3.2,
        },
        wlf: {
          total: 234,
          pending: 12,
          approved: 210,
          rejected: 12,
          trend: 5.8,
        },
        cases: {
          total: 890,
          pending: 67,
          investigating: 123,
          completed: 700,
        },
      },
    })
  }),

  // Recent Cases
  http.get(`${API_BASE}/cases/recent`, () => {
    return HttpResponse.json({
      success: true,
      data: [
        {
          id: 'CASE-2025-001',
          type: 'STR',
          status: 'PENDING',
          priority: 'HIGH',
          customerName: '홍길동',
          amount: 50000000,
          currency: 'KRW',
          createdAt: '2025-01-07T10:30:00Z',
          assignee: '김철수',
        },
        {
          id: 'CASE-2025-002',
          type: 'CTR',
          status: 'INVESTIGATING',
          priority: 'MEDIUM',
          customerName: '이영희',
          amount: 15000000,
          currency: 'KRW',
          createdAt: '2025-01-07T09:15:00Z',
          assignee: '박영수',
        },
        {
          id: 'CASE-2025-003',
          type: 'WLF',
          status: 'COMPLETED',
          priority: 'HIGH',
          customerName: 'John Smith',
          amount: 100000,
          currency: 'USD',
          createdAt: '2025-01-06T14:20:00Z',
          assignee: '최민정',
        },
      ],
    })
  }),

  // Cases List with Pagination
  http.get(`${API_BASE}/cases`, ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '20')

    return HttpResponse.json({
      success: true,
      data: {
        items: Array.from({ length: size }, (_, i) => ({
          id: `CASE-2025-${String(page * 100 + i).padStart(3, '0')}`,
          type: ['STR', 'CTR', 'WLF'][i % 3],
          status: ['PENDING', 'INVESTIGATING', 'COMPLETED'][i % 3],
          priority: ['HIGH', 'MEDIUM', 'LOW'][i % 3],
          customerName: `고객 ${page * 100 + i}`,
          amount: 10000000 + i * 1000000,
          currency: 'KRW',
          createdAt: new Date(Date.now() - i * 3600000).toISOString(),
          assignee: ['김철수', '박영수', '최민정'][i % 3],
        })),
        total: 1000,
        page,
        size,
        totalPages: Math.ceil(1000 / size),
      },
    })
  }),
]
