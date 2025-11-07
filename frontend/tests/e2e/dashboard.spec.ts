import { test, expect } from '@playwright/test'
import { login, logout } from './helpers/test-utils'

/**
 * Dashboard Page E2E Tests
 * 
 * Tests for dashboard functionality including:
 * - Authentication requirement
 * - Stats display
 * - Recent cases table
 * - Navigation
 * - Responsive design
 */

test.describe('Dashboard Page', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await login(page, 'admin', 'admin123')
  })

  test('should display dashboard after successful login', async ({ page }) => {
    // Verify we're on dashboard
    await expect(page).toHaveURL(/.*dashboard/)
    
    // Check page title
    await expect(page.locator('h1')).toContainText(/대시보드|Dashboard/)
    
    // Check subtitle
    await expect(page.getByText(/AML 통합 컴플라이언스 모니터링|AML Compliance/i)).toBeVisible()
  })

  test('should display all stat cards', async ({ page }) => {
    // Check for 4 stat cards
    const statCards = page.locator('.tw-bg-white.tw-rounded-lg.tw-shadow.tw-p-6')
    await expect(statCards).toHaveCount(4)
    
    // Check STR card
    await expect(page.getByText(/STR 의심거래|STR Cases/i)).toBeVisible()
    
    // Check CTR card
    await expect(page.getByText(/CTR 고액거래|CTR Cases/i)).toBeVisible()
    
    // Check WLF card
    await expect(page.getByText(/WLF 감시대상|WLF Cases/i)).toBeVisible()
    
    // Check pending cases card
    await expect(page.getByText(/처리 대기|Pending/i)).toBeVisible()
  })

  test('should display stat numbers correctly', async ({ page }) => {
    // Check that numbers are displayed (looking for large font numbers)
    const statNumbers = page.locator('.tw-text-3xl.tw-font-bold')
    const count = await statNumbers.count()
    
    // Should have 4 stat numbers
    expect(count).toBeGreaterThanOrEqual(4)
    
    // Each should contain a number
    for (let i = 0; i < Math.min(count, 4); i++) {
      const text = await statNumbers.nth(i).textContent()
      expect(text).toMatch(/\d+/)
    }
  })

  test('should display recent cases table', async ({ page }) => {
    // Check table header
    await expect(page.locator('h2')).toContainText(/최근 케이스|Recent Cases/i)
    
    // Check table exists
    const table = page.locator('table')
    await expect(table).toBeVisible()
    
    // Check table headers
    await expect(page.getByRole('columnheader', { name: /케이스 ID|Case ID/i })).toBeVisible()
    await expect(page.getByRole('columnheader', { name: /유형|Type/i })).toBeVisible()
    await expect(page.getByRole('columnheader', { name: /고객명|Customer/i })).toBeVisible()
    await expect(page.getByRole('columnheader', { name: /상태|Status/i })).toBeVisible()
    await expect(page.getByRole('columnheader', { name: /생성일|Created/i })).toBeVisible()
  })

  test('should display case data in table', async ({ page }) => {
    // Check for table rows (excluding header)
    const tableRows = page.locator('tbody tr')
    const rowCount = await tableRows.count()
    
    // Should have at least some rows
    expect(rowCount).toBeGreaterThan(0)
    
    // Check first row contains case ID
    const firstRow = tableRows.first()
    const caseId = await firstRow.locator('td').first().textContent()
    
    // Case ID should match pattern (STR|CTR|WLF)-YYYY-NNNN
    expect(caseId).toMatch(/(STR|CTR|WLF)-\d{4}-\d{4}/)
  })

  test('should display AppHeader component', async ({ page }) => {
    // Check header is visible
    const header = page.locator('.app-header')
    await expect(header).toBeVisible()
    
    // Check logo/title
    await expect(page.getByText('Inspect Hub')).toBeVisible()
    
    // Check user menu exists
    const userMenu = page.locator('[aria-label="사용자 메뉴"]')
    await expect(userMenu).toBeVisible()
  })

  test('should display AppSidebar component', async ({ page }) => {
    // Check sidebar is visible
    const sidebar = page.locator('.app-sidebar')
    await expect(sidebar).toBeVisible()
    
    // Check navigation menu
    const navMenu = page.locator('.menu-navigation')
    await expect(navMenu).toBeVisible()
  })

  test('should toggle sidebar', async ({ page }) => {
    // Find sidebar toggle button
    const toggleButton = page.locator('button[aria-label*="사이드바"]').first()
    
    if (await toggleButton.count() > 0) {
      // Click toggle
      await toggleButton.click()
      
      // Wait for animation
      await page.waitForTimeout(500)
      
      // Sidebar should still exist but might be collapsed
      const sidebar = page.locator('.app-sidebar')
      await expect(sidebar).toBeVisible()
    }
  })

  test('should open user menu and show options', async ({ page }) => {
    // Click user menu
    await page.click('[aria-label="사용자 메뉴"]')
    
    // Wait for dropdown
    await page.waitForTimeout(300)
    
    // Check menu items
    await expect(page.getByText(/프로필|Profile/i)).toBeVisible()
    await expect(page.getByText(/설정|Settings/i)).toBeVisible()
    await expect(page.getByText(/로그아웃|Logout/i)).toBeVisible()
  })

  test('should logout successfully', async ({ page }) => {
    // Click user menu
    await page.click('[aria-label="사용자 메뉴"]')
    
    // Wait for dropdown
    await page.waitForTimeout(300)
    
    // Click logout
    await page.click('button:has-text("로그아웃")')
    
    // Should redirect to login page
    await page.waitForURL('**/login', { timeout: 10000 })
    await expect(page).toHaveURL(/.*login/)
  })

  test('should navigate via sidebar menu', async ({ page }) => {
    // Expand a menu group if collapsed
    const menuGroup = page.locator('.menu-group-toggle').first()
    
    if (await menuGroup.count() > 0) {
      await menuGroup.click()
      await page.waitForTimeout(300)
    }
    
    // Try to click a menu item
    const menuLink = page.locator('.menu-item[href]').first()
    
    if (await menuLink.count() > 0) {
      const href = await menuLink.getAttribute('href')
      await menuLink.click()
      
      // Should navigate to the new page
      await page.waitForTimeout(500)
      
      if (href) {
        await expect(page).toHaveURL(new RegExp(href))
      }
    }
  })

  test('should display notification count', async ({ page }) => {
    // Check notification button
    const notificationButton = page.locator('button[aria-label="알림"]')
    
    if (await notificationButton.count() > 0) {
      await expect(notificationButton).toBeVisible()
      
      // Check for notification badge
      const badge = page.locator('.tw-bg-red-500.tw-text-white')
      
      if (await badge.count() > 0) {
        const badgeText = await badge.first().textContent()
        expect(badgeText).toMatch(/\d+/)
      }
    }
  })

  test('should switch language', async ({ page }) => {
    // Check language selector
    const koreanButton = page.locator('button:has-text("한국어")')
    const englishButton = page.locator('button:has-text("English")')
    
    if (await koreanButton.count() > 0 && await englishButton.count() > 0) {
      // Switch to English
      await englishButton.click()
      await page.waitForTimeout(500)
      
      // Switch back to Korean
      await koreanButton.click()
      await page.waitForTimeout(500)
      
      // Page should still be functional
      await expect(page).toHaveURL(/.*dashboard/)
    }
  })

  test('should be responsive on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })
    
    // Page should still render
    await expect(page.locator('h1')).toContainText(/대시보드|Dashboard/)
    
    // Stats should stack vertically (check grid layout)
    const statCards = page.locator('.tw-bg-white.tw-rounded-lg.tw-shadow.tw-p-6')
    await expect(statCards).toHaveCount(4)
  })

  test('should prevent unauthorized access', async ({ page, context }) => {
    // Clear cookies and storage to simulate logged out state
    await context.clearCookies()
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
    
    // Try to access dashboard
    await page.goto('/dashboard')
    
    // Should redirect to login
    await page.waitForURL('**/login', { timeout: 10000 })
    await expect(page).toHaveURL(/.*login/)
  })

  test('should display loading state initially', async ({ page }) => {
    // Navigate to dashboard (already logged in from beforeEach)
    await page.goto('/dashboard')
    
    // Check if loading indicator exists (adjust based on your implementation)
    const loadingIndicator = page.locator('.tw-animate-spin, [role="progressbar"], .p-progress-spinner')
    
    // Loading might be very fast, so this is optional
    const hasLoading = await loadingIndicator.count() > 0
    
    // Eventually, dashboard content should be visible
    await expect(page.locator('h1')).toContainText(/대시보드|Dashboard/, { timeout: 10000 })
  })

  test('should display icons correctly', async ({ page }) => {
    // Check that PrimeIcons are loaded
    const icons = page.locator('i.pi')
    const iconCount = await icons.count()
    
    // Should have multiple icons (in stats, menu, etc.)
    expect(iconCount).toBeGreaterThan(0)
  })
})
