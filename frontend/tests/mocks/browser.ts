/**
 * MSW Browser Setup
 *
 * Sets up Mock Service Worker for browser/E2E tests
 */
import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

// Create the worker with our handlers
export const worker = setupWorker(...handlers)

// Export setup function for E2E tests
export async function startMockServer() {
  await worker.start({
    onUnhandledRequest: 'bypass', // Allow requests to real backend if not mocked
  })
  console.log('[MSW] Mock API server started')
}

// Export stop function
export async function stopMockServer() {
  worker.stop()
  console.log('[MSW] Mock API server stopped')
}
