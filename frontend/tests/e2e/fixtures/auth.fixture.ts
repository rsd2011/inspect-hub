import { test as base, expect } from '@playwright/test'

/**
 * Authentication Test Fixture
 * 
 * Provides authenticated user session for tests that require login.
 */

export interface AuthFixtures {
  authenticatedPage: any
}

export const test = base.extend<AuthFixtures>({
  authenticatedPage: async ({ page }, use) => {
    // Navigate to login page
    await page.goto('/login')
    
    // Wait for the page to load
    await page.waitForLoadState('networkidle')
    
    // Fill in login credentials
    // Note: These should match your test user credentials
    await page.fill('input[type="text"][placeholder*="사용자"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    
    // Click login button
    await page.click('button[type="submit"]')
    
    // Wait for navigation to complete
    await page.waitForURL('**/dashboard')
    
    // Verify we're authenticated
    await expect(page).toHaveURL(/.*dashboard/)
    
    // Use the authenticated page
    await use(page)
  },
})

export { expect }
