import { test, expect } from '@playwright/test'

test('Debug login page', async ({ page }) => {
  // Collect console messages
  const messages: string[] = []
  const errors: string[] = []

  page.on('console', msg => {
    messages.push(`[${msg.type()}] ${msg.text()}`)
  })

  page.on('pageerror', error => {
    errors.push(error.message)
  })

  // Navigate to login
  await page.goto('/login')
  await page.waitForTimeout(3000)

  // Log console messages
  console.log('\n=== CONSOLE MESSAGES ===')
  messages.forEach(msg => console.log(msg))

  console.log('\n=== PAGE ERRORS ===')
  errors.forEach(err => console.log(err))

  // Check DOM
  const html = await page.content()
  console.log('\n=== BUTTON CHECK ===')
  console.log('HTML includes "<button":', html.includes('<button'))
  console.log('HTML includes "Button":', html.includes('Button'))
  console.log('HTML includes "로그인":', html.includes('로그인'))

  // Find all buttons
  const buttons = await page.locator('button').all()
  console.log('\n=== BUTTONS FOUND:', buttons.length, '===')

  for (let i = 0; i < buttons.length; i++) {
    const btn = buttons[i]
    const text = await btn.textContent().catch(() => '')
    const visible = await btn.isVisible().catch(() => false)
    console.log(`Button ${i}: text="${text}", visible=${visible}`)
  }

  // Take screenshot
  await page.screenshot({ path: 'test-results/debug-login.png', fullPage: true })
  console.log('\nScreenshot saved')
})
