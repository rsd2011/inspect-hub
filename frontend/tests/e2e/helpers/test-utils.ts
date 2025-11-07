import type { Page } from '@playwright/test'

/**
 * Test Utilities
 * 
 * Common helper functions for E2E tests.
 */

/**
 * Login helper function
 */
export async function login(page: Page, username: string, password: string) {
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  
  await page.fill('input[type="text"]', username)
  await page.fill('input[type="password"]', password)
  await page.click('button[type="submit"]')
  
  // Wait for navigation
  await page.waitForURL('**/dashboard', { timeout: 10000 })
}

/**
 * Logout helper function
 */
export async function logout(page: Page) {
  // Click user menu
  await page.click('[aria-label="사용자 메뉴"]')
  
  // Click logout button
  await page.click('button:has-text("로그아웃")')
  
  // Wait for redirect to login page
  await page.waitForURL('**/login', { timeout: 10000 })
}

/**
 * Wait for API response
 */
export async function waitForAPIResponse(page: Page, urlPattern: string | RegExp) {
  return page.waitForResponse(
    response => 
      (typeof urlPattern === 'string' 
        ? response.url().includes(urlPattern) 
        : urlPattern.test(response.url())) && 
      response.status() === 200
  )
}

/**
 * Take screenshot with timestamp
 */
export async function takeScreenshot(page: Page, name: string) {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
  await page.screenshot({ 
    path: `tests/screenshots/${name}-${timestamp}.png`,
    fullPage: true 
  })
}

/**
 * Check if element is visible
 */
export async function isVisible(page: Page, selector: string, timeout: number = 5000): Promise<boolean> {
  try {
    await page.waitForSelector(selector, { state: 'visible', timeout })
    return true
  } catch {
    return false
  }
}

/**
 * Get localStorage value
 */
export async function getLocalStorage(page: Page, key: string): Promise<string | null> {
  return await page.evaluate((k) => localStorage.getItem(k), key)
}

/**
 * Set localStorage value
 */
export async function setLocalStorage(page: Page, key: string, value: string) {
  await page.evaluate(
    ({ k, v }) => localStorage.setItem(k, v),
    { k: key, v: value }
  )
}

/**
 * Clear all storage
 */
export async function clearStorage(page: Page) {
  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
  })
}
