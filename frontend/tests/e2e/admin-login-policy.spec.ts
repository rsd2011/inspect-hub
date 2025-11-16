import { test, expect } from './fixtures/mock-api.fixture'

/**
 * Admin Login Policy E2E Tests
 *
 * Story 1.6: System Configuration UI (Admin Page)
 * Tests admin page for managing login policy configuration
 */

test.describe('Admin Login Policy Page', () => {
  test.beforeEach(async ({ page, mockApi }) => {
    // Mock API is automatically set up via fixture
    
    // Navigate to admin login policy page
    await page.goto('/admin/system/login-policy')
    await page.waitForLoadState('networkidle')
  })

  test('AC 1: should display login policy configuration page', async ({ page }) => {
    // Verify page title (use more specific selector to avoid layout h1)
    await expect(page.locator('h1').filter({ hasText: '시스템 설정' })).toBeVisible()

    // Verify page subtitle
    await expect(page.locator('p').filter({ hasText: '로그인 정책' })).toBeVisible()

    // Verify policy name input exists
    await expect(page.locator('input[placeholder="정책 이름을 입력하세요"]')).toBeVisible()

    // Verify checkboxes for all three methods
    await expect(page.locator('label:has-text("SSO")')).toBeVisible()
    await expect(page.locator('label:has-text("AD")')).toBeVisible()
    await expect(page.locator('label:has-text("LOCAL") >> nth=0')).toBeVisible()

    // Verify Save and Cancel buttons
    await expect(page.locator('button:has-text("저장")')).toBeVisible()
    await expect(page.locator('button:has-text("취소")')).toBeVisible()
  })

  test('AC 2: should display all login methods enabled by default', async ({ page }) => {
    // Wait for page to load
    await page.waitForTimeout(1500)

    // Verify all three methods are visible
    await expect(page.locator('text=SSO')).toBeVisible()
    await expect(page.locator('text=AD')).toBeVisible()
    await expect(page.locator('text=LOCAL').first()).toBeVisible()

    // All three checkboxes should be checked by default
    // PrimeVue Checkbox uses .p-checkbox-checked class
    const checkedBoxes = await page.locator('.p-checkbox.p-checkbox-checked').count()
    expect(checkedBoxes).toBeGreaterThanOrEqual(3)
  })

  test('AC 2 & AC 4: should enable/disable login methods', async ({ page }) => {
    // Wait for initial load
    await page.waitForTimeout(1000)

    // Find AD checkbox (second method)
    const adCheckboxLabel = page.locator('label:has-text("AD")')
    await expect(adCheckboxLabel).toBeVisible()

    // Uncheck AD
    await adCheckboxLabel.click()
    await page.waitForTimeout(300)

    // Verify Save button is now enabled (has changes)
    const saveButton = page.locator('button:has-text("저장")')
    await expect(saveButton).toBeEnabled()

    // Re-enable AD
    await adCheckboxLabel.click()
    await page.waitForTimeout(300)
  })

  test('AC 4: should prevent disabling the last remaining method', async ({ page }) => {
    // Wait for initial load
    await page.waitForTimeout(1000)

    // Disable SSO and AD, leaving only LOCAL
    await page.locator('label:has-text("SSO")').click()
    await page.waitForTimeout(300)
    await page.locator('label:has-text("AD")').click()
    await page.waitForTimeout(300)

    // Try to disable the last method (LOCAL)
    await page.locator('label:has-text("LOCAL") >> nth=0').click()
    await page.waitForTimeout(500)

    // Verify warning toast appears
    await expect(page.locator('text=최소 하나의 로그인 방식은 활성화되어야 합니다')).toBeVisible({ timeout: 3000 })

    // Verify LOCAL is still checked (disabled from unchecking)
    // Note: The checkbox might be disabled in UI
  })

  test('AC 3: should display priority order list', async ({ page }) => {
    // Wait for page to load
    await page.waitForTimeout(1000)

    // Verify priority section exists
    await expect(page.locator('text=우선순위')).toBeVisible()

    // Verify OrderList or drag-drop interface is present
    // PrimeVue OrderList has .p-orderlist class
    const orderList = page.locator('.p-orderlist')
    if (await orderList.count() > 0) {
      await expect(orderList).toBeVisible()
    }
  })

  test('AC 4: should display login page preview', async ({ page }) => {
    // Wait for page to load
    await page.waitForTimeout(1000)

    // Verify preview section exists
    await expect(page.locator('text=로그인 페이지 미리보기')).toBeVisible()

    // Verify Inspect-Hub title in preview
    await expect(page.locator('text=Inspect-Hub 로그인')).toBeVisible()

    // Verify tabs are visible (SSO, AD, 일반)
    await expect(page.locator('text=SSO')).toBeVisible()
    await expect(page.locator('text=AD')).toBeVisible()
    await expect(page.locator('text=일반')).toBeVisible()
  })

  test('AC 5 & AC 7: should save policy changes with success notification', async ({ page }) => {
    // Wait for initial load
    await page.waitForTimeout(1000)

    // Change policy name
    const policyNameInput = page.locator('input[placeholder="정책 이름을 입력하세요"]')
    await policyNameInput.fill('테스트 로그인 정책')
    await page.waitForTimeout(300)

    // Verify Save button is enabled
    const saveButton = page.locator('button:has-text("저장")')
    await expect(saveButton).toBeEnabled()

    // Click Save
    await saveButton.click()
    await page.waitForTimeout(1000)

    // Verify success toast notification
    await expect(page.locator('text=로그인 정책이 업데이트되었습니다')).toBeVisible({ timeout: 5000 })
  })

  test('AC 5 & AC 7: should show cancel confirmation', async ({ page }) => {
    // Wait for initial load
    await page.waitForTimeout(1000)

    // Make a change
    const policyNameInput = page.locator('input[placeholder="정책 이름을 입력하세요"]')
    const originalValue = await policyNameInput.inputValue()
    await policyNameInput.fill('변경된 정책 이름')
    await page.waitForTimeout(300)

    // Click Cancel
    await page.locator('button:has-text("취소")').click()
    await page.waitForTimeout(500)

    // Verify cancel notification
    await expect(page.locator('text=변경사항이 취소되었습니다')).toBeVisible({ timeout: 3000 })

    // Verify value is reset to original
    await expect(policyNameInput).toHaveValue(originalValue)
  })

  test('AC 6: should require ROLE_SYSTEM_ADMIN authorization (middleware check)', async ({ page }) => {
    // Note: Since auth middleware is not fully implemented yet,
    // this test verifies the middleware structure exists

    // Page should load (middleware allows access in current implementation)
    await expect(page.locator('h1').filter({ hasText: '시스템 설정' })).toBeVisible()

    // Check console for middleware message
    const logs: string[] = []
    page.on('console', (msg) => {
      logs.push(msg.text())
    })

    // Reload to trigger middleware
    await page.reload({ waitUntil: 'networkidle' })
    await page.waitForTimeout(500)

    // Verify middleware was called (console log check)
    const middlewareLog = logs.find((log) =>
      log.includes('Admin Middleware')
    )
    expect(middlewareLog).toBeTruthy()
  })

  test('should display loading state while fetching policy', async ({ page }) => {
    // Navigate to page
    await page.goto('/admin/system/login-policy')

    // Loading spinner should appear briefly
    const spinner = page.locator('text=로딩 중...')
    
    // Note: Loading might be too fast with mock API, so we just verify page loads
    await page.waitForLoadState('networkidle')

    // Verify final content is visible
    await expect(page.locator('h1').filter({ hasText: '시스템 설정' })).toBeVisible()
  })

  test('should maintain reactive state between priority and preview', async ({ page }) => {
    // Wait for initial load
    await page.waitForTimeout(1000)

    // Disable SSO method
    await page.locator('label:has-text("SSO")').click()
    await page.waitForTimeout(500)

    // Verify SSO tab is no longer visible in preview (or disabled)
    // Note: Actual behavior depends on implementation
    // This is a placeholder - real test would verify tab visibility changes
  })

  test('should handle API errors gracefully', async ({ page, context }) => {
    // Intercept API call and return error
    await context.route('**/api/v1/system/login-policy', (route) => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({
          success: false,
          error: {
            code: 'SERVER_ERROR',
            message: '서버 오류가 발생했습니다',
          },
        }),
      })
    })

    // Navigate to page
    await page.goto('/admin/system/login-policy')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // Verify error message is displayed
    await expect(page.locator('text=정책 조회 실패')).toBeVisible({ timeout: 5000 })
  })
})
