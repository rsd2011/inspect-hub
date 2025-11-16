/**
 * Mock API Fixture for Playwright
 *
 * Provides mock API responses for E2E tests
 */
import { test as base } from '@playwright/test'
import type { Page } from '@playwright/test'

const API_BASE = 'http://localhost:8090/api/v1'

/**
 * Setup mock API routes for the page
 */
async function setupMockApi(page: Page) {
  // Authentication - Login
  await page.route(`${API_BASE}/auth/login`, async (route) => {
    const request = route.request()
    const body = request.postDataJSON()

    if (body.username === 'admin' && body.password === 'admin123') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
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
        }),
      })
    } else {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          success: false,
          error: {
            code: 'AUTH_001',
            message: '로그인에 실패했습니다. SSO 인증에 실패했습니다.',
          },
        }),
      })
    }
  })

  // Authentication - Refresh Token
  await page.route(`${API_BASE}/auth/refresh`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          accessToken: 'mock-jwt-token-new-12345',
          refreshToken: 'mock-refresh-token-new-67890',
        },
      }),
    })
  })

  // User Info
  await page.route(`${API_BASE}/auth/me`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          id: 'user-001',
          username: 'admin',
          name: '관리자',
          email: 'admin@inspecthub.com',
          roles: ['ROLE_ADMIN', 'ROLE_USER'],
          permissions: ['*'],
        },
      }),
    })
  })

  // Dashboard Stats
  await page.route(`${API_BASE}/dashboard/stats`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
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
      }),
    })
  })

  // Recent Cases
  await page.route(`${API_BASE}/cases/recent`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
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
      }),
    })
  })

  // Login Policy - GET
  await page.route(`${API_BASE}/system/login-policy`, async (route) => {
    const request = route.request()
    
    if (request.method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: '01JCXYZ1234567890ABCDEF002',
            name: '시스템 로그인 정책',
            enabledMethods: ['SSO', 'AD', 'LOCAL'],
            priority: ['SSO', 'AD', 'LOCAL'],
            active: true,
            createdBy: 'SYSTEM',
            createdAt: '2025-01-15T00:00:00Z',
            updatedBy: null,
            updatedAt: null,
          },
        }),
      })
    } else if (request.method() === 'PUT') {
      const body = request.postDataJSON()
      
      // Validation: at least one method must be enabled
      if (body.enabledMethods && body.enabledMethods.length === 0) {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            error: {
              code: 'POLICY_VALIDATION_ERROR',
              message: '최소 하나의 로그인 방식은 활성화되어야 합니다',
            },
          }),
        })
        return
      }
      
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: '01JCXYZ1234567890ABCDEF002',
            name: body.name || '시스템 로그인 정책',
            enabledMethods: body.enabledMethods || ['SSO', 'AD', 'LOCAL'],
            priority: body.priority || ['SSO', 'AD', 'LOCAL'],
            active: true,
            createdBy: 'SYSTEM',
            createdAt: '2025-01-15T00:00:00Z',
            updatedBy: 'admin',
            updatedAt: new Date().toISOString(),
          },
        }),
      })
    }
  })

  console.log('[Mock API] Routes configured')
}

// Extend base test with mock API fixture
export const test = base.extend<{ mockApi: void }>({
  mockApi: async ({ page }, use) => {
    await setupMockApi(page)
    await use()
  },
})

export { expect } from '@playwright/test'
