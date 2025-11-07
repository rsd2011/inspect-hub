/**
 * Mock API Helper for E2E Tests
 *
 * Enables MSW in Playwright tests
 */
import type { Page } from '@playwright/test'

/**
 * Enable mock API for the page
 *
 * This injects MSW into the page and starts intercepting API calls
 */
export async function enableMockApi(page: Page) {
  // Add route to serve MSW worker script
  await page.route('**/mockServiceWorker.js', async (route) => {
    const response = await route.fetch()
    await route.fulfill({
      status: response.status(),
      headers: {
        ...response.headers(),
        'Service-Worker-Allowed': '/',
      },
      body: await response.body(),
    })
  })

  // Wait for page to load
  await page.goto('/', { waitUntil: 'domcontentloaded' })

  // Inject MSW setup script
  await page.addInitScript(() => {
    // This will run before any other scripts on the page
    ;(window as any).__MSW_ENABLED__ = true
  })

  // Start MSW
  await page.evaluate(async () => {
    const { worker } = await import('/tests/mocks/browser.ts')
    await worker.start({
      onUnhandledRequest: 'bypass',
      quiet: false,
    })
  })

  // Wait for service worker to be ready
  await page.waitForTimeout(500)
}

/**
 * Disable mock API for the page
 */
export async function disableMockApi(page: Page) {
  await page.evaluate(async () => {
    const { worker } = await import('/tests/mocks/browser.ts')
    worker.stop()
  })
}

/**
 * Mock specific API endpoint with custom response
 */
export async function mockApiEndpoint(
  page: Page,
  url: string,
  response: any,
  options?: {
    status?: number
    delay?: number
  }
) {
  await page.route(url, async (route) => {
    if (options?.delay) {
      await page.waitForTimeout(options.delay)
    }

    await route.fulfill({
      status: options?.status || 200,
      contentType: 'application/json',
      body: JSON.stringify(response),
    })
  })
}

/**
 * Reset all route mocks
 */
export async function resetMocks(page: Page) {
  await page.unrouteAll({ behavior: 'ignoreErrors' })
}
