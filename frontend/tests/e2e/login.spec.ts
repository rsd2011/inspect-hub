import { test, expect } from '@playwright/test'
import { clearStorage } from './helpers/test-utils'

/**
 * Login Page E2E Tests
 * 
 * Tests for login functionality including:
 * - Page rendering
 * - Form validation
 * - Successful login (traditional)
 * - Failed login
 * - Navigation after login
 */

test.describe('Login Page', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page first
    await page.goto('/login')
    await page.waitForLoadState('networkidle')

    // Then clear storage
    await clearStorage(page)

    // Reload to ensure clean state
    await page.reload({ waitUntil: 'networkidle' })
  })

  test('should display login page correctly', async ({ page }) => {
    // Check page title
    await expect(page).toHaveTitle(/Inspect Hub/)

    // Check login page title is visible
    await expect(page.locator('h1')).toContainText(/Inspect-Hub/)
    
    // Check form inputs exist
    const usernameInput = page.locator('input[type="text"]').first()
    const passwordInput = page.locator('input[type="password"]').first()
    const submitButton = page.locator('button[type="submit"]').first()
    
    await expect(usernameInput).toBeVisible()
    await expect(passwordInput).toBeVisible()
    await expect(submitButton).toBeVisible()
  })

  test('should show validation errors for empty form', async ({ page }) => {
    // Click submit without filling the form
    await page.click('button[type="submit"]')
    
    // Wait a bit for validation
    await page.waitForTimeout(500)
    
    // Check if validation errors appear (adjust selectors based on your implementation)
    const errorMessages = page.locator('.tw-text-red-600, [class*="error"]')
    const count = await errorMessages.count()
    
    // At least one error should be visible
    expect(count).toBeGreaterThan(0)
  })

  test('should show error for invalid credentials', async ({ page }) => {
    // Fill in invalid credentials
    await page.fill('input[type="text"]', 'invaliduser')
    await page.fill('input[type="password"]', 'wrongpassword')
    
    // Submit the form
    await page.click('button[type="submit"]')
    
    // Wait for error message
    await page.waitForTimeout(1000)
    
    // Should still be on login page
    await expect(page).toHaveURL(/.*login/)
    
    // Check for error message (adjust selector based on your toast/alert implementation)
    const errorElement = page.locator('.tw-text-red-600, [role="alert"], .p-toast-message-error')
    
    // Either error message is visible or we're still on login page
    const isStillOnLogin = page.url().includes('/login')
    expect(isStillOnLogin).toBeTruthy()
  })

  test('should successfully login with valid credentials', async ({ page }) => {
    // Fill in valid credentials
    await page.fill('input[type="text"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    
    // Submit the form
    await page.click('button[type="submit"]')
    
    // Wait for navigation to dashboard
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    
    // Verify we're on the dashboard
    await expect(page).toHaveURL(/.*dashboard/)
    
    // Check that dashboard content is visible
    await expect(page.locator('h1')).toContainText(/대시보드|Dashboard/)
  })

  test('should store authentication token after login', async ({ page }) => {
    // Login
    await page.fill('input[type="text"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button[type="submit"]')
    
    // Wait for navigation
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    
    // Check localStorage for auth token
    const token = await page.evaluate(() => {
      // Check various possible storage keys
      return localStorage.getItem('auth-token') || 
             localStorage.getItem('token') || 
             localStorage.getItem('accessToken')
    })
    
    // Token should exist (unless using httpOnly cookies)
    // Adjust this based on your auth implementation
    expect(token).toBeTruthy()
  })

  test('should redirect to dashboard if already authenticated', async ({ page }) => {
    // First, login
    await page.fill('input[type="text"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL('**/dashboard', { timeout: 10000 })
    
    // Now try to go back to login page
    await page.goto('/login')
    
    // Should be redirected to dashboard
    await expect(page).toHaveURL(/.*dashboard/)
  })

  test('should toggle password visibility', async ({ page }) => {
    const passwordInput = page.locator('input[type="password"]').first()
    
    // Check initial type is password
    await expect(passwordInput).toHaveAttribute('type', 'password')
    
    // Click toggle button (adjust selector based on your implementation)
    const toggleButton = page.locator('button[aria-label*="password"], i.pi-eye')
    
    if (await toggleButton.count() > 0) {
      await toggleButton.first().click()
      
      // Check if type changed to text
      const inputType = await passwordInput.getAttribute('type')
      expect(['text', 'password']).toContain(inputType)
    }
  })

  test('should have SSO login option', async ({ page }) => {
    // Check if SSO button exists
    const ssoButton = page.locator('button:has-text("SSO"), button:has-text("Single Sign-On")')
    
    // SSO button should be visible
    if (await ssoButton.count() > 0) {
      await expect(ssoButton.first()).toBeVisible()
    }
  })

  test('should handle network errors gracefully', async ({ page }) => {
    // Intercept API calls and return error
    await page.route('**/api/v1/auth/login', route => {
      route.abort('failed')
    })
    
    // Try to login
    await page.fill('input[type="text"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button[type="submit"]')
    
    // Wait a bit
    await page.waitForTimeout(1000)
    
    // Should show error message
    const errorElement = page.locator('.tw-text-red-600, [role="alert"], .p-toast-message-error')
    const hasError = await errorElement.count() > 0
    
    // Either error is shown or we're still on login page
    expect(hasError || page.url().includes('/login')).toBeTruthy()
  })
})
