import { vi } from 'vitest'
import { ref, computed } from 'vue'

// Make Vue reactivity available globally
global.ref = ref
global.computed = computed

// Mock import.meta.client for browser environment simulation
Object.defineProperty(import.meta, 'client', {
  value: true,
  writable: true,
  configurable: true,
})

Object.defineProperty(import.meta, 'server', {
  value: false,
  writable: true,
  configurable: true,
})

// Mock Nuxt auto-imports
global.defineNuxtRouteMiddleware = vi.fn((middleware) => middleware)
global.navigateTo = vi.fn()
global.defineNuxtPlugin = vi.fn((plugin) => plugin)
global.useRuntimeConfig = vi.fn(() => ({
  public: {
    apiBase: 'http://localhost:8090/api/v1',
  },
}))
global.useCookie = vi.fn()
global.useRouter = vi.fn(() => ({
  push: vi.fn(),
  replace: vi.fn(),
  go: vi.fn(),
  back: vi.fn(),
}))
global.useRoute = vi.fn(() => ({
  path: '/',
  query: {},
  params: {},
}))
global.$fetch = vi.fn()

// Mock auth store for middleware tests
global.useAuthStore = vi.fn()
