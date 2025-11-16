# Frontend Testing Guide

> **Inspect-Hub Frontend 테스트 전략 및 구현 가이드**
>
> **Version**: 1.0
> **Last Updated**: 2025-01-13
> **Target**: Nuxt 4 + Vue 3 + Vitest + Playwright

---

## 📚 목차

1. [테스트 전략 개요](#테스트-전략-개요)
2. [테스트 환경 설정](#테스트-환경-설정)
3. [단위 테스트 (Unit Tests)](#단위-테스트-unit-tests)
4. [컴포넌트 테스트](#컴포넌트-테스트)
5. [통합 테스트](#통합-테스트)
6. [E2E 테스트 (Playwright)](#e2e-테스트-playwright)
7. [Skills 활용 (Playwright Skill)](#skills-활용-playwright-skill)
8. [MCP Playwright Server 활용](#mcp-playwright-server-활용)
9. [Pinia Store 테스트](#pinia-store-테스트)
10. [Composables 테스트](#composables-테스트)
11. [API 모킹](#api-모킹)
12. [테스트 커버리지](#테스트-커버리지)
13. [CI/CD 통합](#cicd-통합)
14. [베스트 프랙티스](#베스트-프랙티스)

---

## 테스트 전략 개요

### 테스트 피라미드

```
       /\
      /  \       E2E Tests (10%)
     /----\      - Playwright
    /      \     - 사용자 시나리오
   /--------\    
  / Integration\ Integration Tests (20%)
 /   Tests     \ - API 통합
/--------------\ - Store 통합
|              |
|  Unit Tests  | Unit Tests (70%)
|              | - Composables
|              | - Utils
|              | - 비즈니스 로직
|______________|
```

### 테스트 범위

| 레이어 | 테스트 유형 | 커버리지 목표 | 도구 |
|--------|-------------|---------------|------|
| **Composables** | Unit | 90%+ | Vitest |
| **Utils** | Unit | 95%+ | Vitest |
| **Components (Atoms)** | Unit | 85%+ | Vitest + Testing Library |
| **Components (Molecules/Organisms)** | Integration | 80%+ | Vitest + Testing Library |
| **Stores** | Unit | 85%+ | Vitest + Pinia Testing |
| **Pages** | E2E | 75%+ | Playwright |
| **User Flows** | E2E | 100% | Playwright |

### 핵심 원칙

#### 1. TDD (Test-Driven Development) - 테스트 설계 기준 ⭐

**새로운 기능 구현 시 반드시 TDD 사이클을 준수한다:**

- **Red (실패하는 테스트 작성)**: 기능 구현 전 먼저 실패하는 테스트를 작성
- **Green (최소 코드로 통과)**: 테스트가 통과할 수 있는 최소한의 프로덕션 코드 작성
- **Refactor (리팩터링)**: 테스트가 통과한 상태에서 코드 개선

```typescript
// 1. Red: 실패하는 테스트 먼저 작성
describe('할인 계산기', () => {
  it('VIP 고객에게 20% 할인율을 적용한다', () => {
    // Given
    const vipUser = { membershipLevel: 'VIP' }

    // When
    const discount = calculateDiscount(vipUser)

    // Then
    expect(discount.rate).toBe(0.20)  // 아직 구현 안됨 → 테스트 실패
  })
})

// 2. Green: 최소 코드로 통과
export function calculateDiscount(user) {
  if (user.membershipLevel === 'VIP') {
    return { rate: 0.20 }  // 하드코딩도 OK
  }
  return { rate: 0.0 }
}

// 3. Refactor: 개선 (테스트는 계속 통과)
const DISCOUNT_RATES = {
  VIP: 0.20,
  GOLD: 0.15,
  SILVER: 0.10
}

export function calculateDiscount(user) {
  const rate = DISCOUNT_RATES[user.membershipLevel] || 0.0
  return { rate }
}
```

#### 2. BDD (Behavior-Driven Development) - 테스트 표현 방식 ⭐

**각 테스트는 하나의 행동(Behavior)을 검증하도록 설계한다:**

**Given / When / Then 구조:**
- **Given (준비)**: 테스트 전제 조건 및 데이터 준비
- **When (실행)**: 테스트 대상 메서드/컴포넌트 실행
- **Then (검증)**: 예상 결과 검증

**테스트 명명 규칙:**
- ✅ **시나리오 문장 형태**로 작성
- ✅ `it()` 또는 `test()` 설명은 **한글 비즈니스 시나리오**로 작성
- ✅ 함수명은 `should_[예상결과]_when_[조건]()` 형식 (선택사항)

```typescript
// ✅ Good: BDD 스타일
describe('로그인 폼', () => {
  it('유효한 자격증명으로 로그인 시 대시보드로 이동한다', async () => {
    // Given (준비): 로그인 폼 렌더링
    const { getByLabel, getByRole } = render(LoginForm)
    const user = userEvent.setup()

    // When (실행): 사용자 입력 및 제출
    await user.type(getByLabel('사용자명'), 'admin')
    await user.type(getByLabel('비밀번호'), 'admin123')
    await user.click(getByRole('button', { name: '로그인' }))

    // Then (검증): 대시보드로 리다이렉트
    expect(mockRouter.push).toHaveBeenCalledWith('/dashboard')
  })

  it('비밀번호가 5회 이상 틀리면 계정이 잠긴다', async () => {
    // Given, When, Then...
  })
})

// ❌ Bad: 기술적 용어, 불명확한 시나리오
describe('LoginForm', () => {
  it('should work', () => {
    const wrapper = mount(LoginForm)
    expect(wrapper.exists()).toBe(true)
  })
})
```

**추가 예시:**
```typescript
it('만료된 토큰으로 API 호출 시 401 에러를 반환한다', () => {})
it('중복된 이메일로 가입 시 검증 에러를 표시한다', () => {})
it('파일 업로드 진행률이 실시간으로 업데이트된다', () => {})
```

#### 3. F.I.R.S.T 원칙

- **Fast**: 빠른 실행 (<1초/테스트)
- **Independent**: 독립적 실행 가능
- **Repeatable**: 반복 가능
- **Self-validating**: 자동 검증
- **Timely**: 적시 작성 (TDD - 코드 작성 전!)

#### 4. 테스트 우선순위

- **High**: 비즈니스 로직, 데이터 변환, 보안 관련
- **Medium**: UI 컴포넌트, 사용자 인터랙션
- **Low**: 스타일, 애니메이션

#### 5. 사용자 중심 테스트

- 구현 세부사항이 아닌 사용자 관점에서 테스트
- 접근성 우선 (role, label 기반 쿼리)
- 실제 사용자 행동 시뮬레이션

---

## 테스트 환경 설정

### Package.json 의존성

```json
{
  "devDependencies": {
    "@nuxt/test-utils": "^3.11.0",
    "@vue/test-utils": "^2.4.4",
    "@vitest/ui": "^1.2.0",
    "vitest": "^1.2.0",
    "happy-dom": "^13.3.8",
    
    "@testing-library/vue": "^8.0.1",
    "@testing-library/user-event": "^14.5.2",
    "@testing-library/dom": "^9.3.4",
    
    "playwright": "^1.41.0",
    "@playwright/test": "^1.41.0",
    
    "msw": "^2.0.11",
    "c8": "^9.1.0"
  },
  "scripts": {
    "test": "vitest",
    "test:unit": "vitest run --reporter=verbose",
    "test:watch": "vitest watch",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest run --coverage",
    
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui",
    "test:e2e:headed": "playwright test --headed",
    "test:e2e:debug": "playwright test --debug",
    "test:e2e:report": "playwright show-report"
  }
}
```

### Vitest 설정

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  
  test: {
    globals: true,
    environment: 'happy-dom',
    setupFiles: ['./tests/setup.ts'],
    
    coverage: {
      provider: 'c8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData',
        'dist/'
      ],
      statements: 80,
      branches: 75,
      functions: 80,
      lines: 80
    },
    
    include: ['**/*.{test,spec}.{js,ts,jsx,tsx}'],
    exclude: ['node_modules', 'dist', '.nuxt', '.output'],
    
    mockReset: true,
    restoreMocks: true,
    clearMocks: true
  },
  
  resolve: {
    alias: {
      '~': resolve(__dirname, './'),
      '@': resolve(__dirname, './')
    }
  }
})
```

### Test Setup

```typescript
// tests/setup.ts
import { beforeAll, afterEach, afterAll, vi } from 'vitest'
import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

// Global test configuration
beforeAll(() => {
  // Pinia setup
  setActivePinia(createPinia())
  
  // Mock Nuxt auto-imports
  global.defineNuxtConfig = vi.fn()
  global.useNuxtApp = vi.fn()
  global.useRuntimeConfig = vi.fn(() => ({
    public: {
      apiBase: 'http://localhost:8090/api/v1'
    }
  }))
  
  // Mock window.matchMedia
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn()
    }))
  })
  
  // Mock IntersectionObserver
  global.IntersectionObserver = class IntersectionObserver {
    constructor() {}
    disconnect() {}
    observe() {}
    takeRecords() { return [] }
    unobserve() {}
  }
})

afterEach(() => {
  vi.clearAllMocks()
})

afterAll(() => {
  vi.restoreAllMocks()
})

// Vue Test Utils global config
config.global.mocks = {
  $t: (key: string) => key,  // i18n mock
  $router: {
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn()
  },
  $route: {
    params: {},
    query: {},
    path: '/'
  }
}
```

### Playwright 설정

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'test-results.json' }],
    ['junit', { outputFile: 'test-results.xml' }]
  ],
  
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    
    // 웹 브라우저 중심 설정
    viewport: { width: 1920, height: 1080 },
    ignoreHTTPSErrors: true,
    
    // 타임아웃
    actionTimeout: 10000,
    navigationTimeout: 30000
  },
  
  projects: [
    // 주요 데스크톱 브라우저
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] }
    },
    
    // 다양한 해상도 테스트 (선택적)
    {
      name: 'Desktop 1366x768',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1366, height: 768 }
      }
    },
    {
      name: 'Desktop 1920x1080',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 }
      }
    }
  ],
  
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000
  }
})
```

---

## 단위 테스트 (Unit Tests)

### Utility Functions 테스트

```typescript
// shared/lib/utils/formatters.test.ts
import { describe, it, expect } from 'vitest'
import { formatCurrency, formatDate, formatPhoneNumber } from './formatters'

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('숫자를 한국 원화 형식으로 변환', () => {
      expect(formatCurrency(1000000)).toBe('1,000,000')
      expect(formatCurrency(0)).toBe('0')
      expect(formatCurrency(-5000)).toBe('-5,000')
    })
    
    it('소수점 처리', () => {
      expect(formatCurrency(1234.56)).toBe('1,235')  // 반올림
      expect(formatCurrency(1234.56, true)).toBe('1,234.56')  // 소수점 유지
    })
    
    it('null/undefined 처리', () => {
      expect(formatCurrency(null)).toBe('0')
      expect(formatCurrency(undefined)).toBe('0')
    })
  })
  
  describe('formatDate', () => {
    it('Date 객체를 YYYY-MM-DD 형식으로 변환', () => {
      const date = new Date('2025-01-13T10:30:00')
      expect(formatDate(date)).toBe('2025-01-13')
    })
    
    it('커스텀 형식 지원', () => {
      const date = new Date('2025-01-13T10:30:00')
      expect(formatDate(date, 'YYYY년 MM월 DD일')).toBe('2025년 01월 13일')
    })
    
    it('ISO 문자열 처리', () => {
      expect(formatDate('2025-01-13T10:30:00Z')).toBe('2025-01-13')
    })
  })
  
  describe('formatPhoneNumber', () => {
    it('한국 전화번호 형식 변환', () => {
      expect(formatPhoneNumber('01012345678')).toBe('010-1234-5678')
      expect(formatPhoneNumber('0212345678')).toBe('02-1234-5678')
    })
    
    it('이미 형식화된 번호 유지', () => {
      expect(formatPhoneNumber('010-1234-5678')).toBe('010-1234-5678')
    })
    
    it('잘못된 입력 처리', () => {
      expect(formatPhoneNumber('123')).toBe('123')
      expect(formatPhoneNumber('')).toBe('')
    })
  })
})
```

### Validation Functions 테스트

```typescript
// shared/lib/utils/validators.test.ts
import { describe, it, expect } from 'vitest'
import { validateEmail, validatePassword, validateSSN } from './validators'

describe('validators', () => {
  describe('validateEmail', () => {
    it('유효한 이메일 검증', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name+tag@example.co.kr')).toBe(true)
    })
    
    it('잘못된 이메일 거부', () => {
      expect(validateEmail('invalid')).toBe(false)
      expect(validateEmail('test@')).toBe(false)
      expect(validateEmail('@example.com')).toBe(false)
      expect(validateEmail('')).toBe(false)
    })
  })
  
  describe('validatePassword', () => {
    it('비밀번호 강도 검증 (8자 이상, 대소문자, 숫자, 특수문자)', () => {
      expect(validatePassword('Password123!')).toBe(true)
      expect(validatePassword('Abcd1234!@#$')).toBe(true)
    })
    
    it('약한 비밀번호 거부', () => {
      expect(validatePassword('short')).toBe(false)  // 너무 짧음
      expect(validatePassword('password123')).toBe(false)  // 대문자 없음
      expect(validatePassword('PASSWORD123')).toBe(false)  // 소문자 없음
      expect(validatePassword('Password')).toBe(false)  // 숫자 없음
      expect(validatePassword('Password123')).toBe(false)  // 특수문자 없음
    })
  })
  
  describe('validateSSN', () => {
    it('주민등록번호 형식 검증 (마스킹된 형식)', () => {
      expect(validateSSN('123456-1******')).toBe(true)
      expect(validateSSN('000101-3******')).toBe(true)
    })
    
    it('잘못된 형식 거부', () => {
      expect(validateSSN('123456-1234567')).toBe(false)  // 전체 노출
      expect(validateSSN('12345-1******')).toBe(false)  // 앞자리 부족
      expect(validateSSN('1234567-1******')).toBe(false)  // 앞자리 초과
    })
  })
})
```

---

## 컴포넌트 테스트

### Atom 컴포넌트 테스트

```typescript
// shared/ui/atoms/Button.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from './Button.vue'

describe('Button', () => {
  it('기본 렌더링', () => {
    const wrapper = mount(Button, {
      props: { label: '저장' }
    })
    
    expect(wrapper.text()).toBe('저장')
    expect(wrapper.find('button').exists()).toBe(true)
  })
  
  it('클릭 이벤트 emit', async () => {
    const wrapper = mount(Button, {
      props: { label: '클릭' }
    })
    
    await wrapper.find('button').trigger('click')
    
    expect(wrapper.emitted('click')).toHaveLength(1)
  })
  
  it('disabled 상태에서 클릭 불가', async () => {
    const wrapper = mount(Button, {
      props: {
        label: '버튼',
        disabled: true
      }
    })
    
    await wrapper.find('button').trigger('click')
    
    expect(wrapper.emitted('click')).toBeUndefined()
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })
  
  it('loading 상태 표시', () => {
    const wrapper = mount(Button, {
      props: {
        label: '저장',
        loading: true
      }
    })
    
    expect(wrapper.find('.spinner').exists()).toBe(true)
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })
  
  it('severity variant 적용', () => {
    const variants = ['primary', 'secondary', 'danger', 'success']
    
    variants.forEach(severity => {
      const wrapper = mount(Button, {
        props: { label: '버튼', severity }
      })
      
      expect(wrapper.find('button').classes()).toContain(`btn-${severity}`)
    })
  })
  
  it('아이콘 슬롯 렌더링', () => {
    const wrapper = mount(Button, {
      props: { label: '저장' },
      slots: {
        icon: '<i class="pi pi-check"></i>'
      }
    })
    
    expect(wrapper.find('.pi-check').exists()).toBe(true)
  })
})
```

### Molecule 컴포넌트 테스트

```typescript
// shared/ui/molecules/FormField.test.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FormField from './FormField.vue'

describe('FormField', () => {
  it('label과 input 렌더링', () => {
    const wrapper = mount(FormField, {
      props: {
        label: '사용자명',
        modelValue: '',
        name: 'username'
      }
    })
    
    expect(wrapper.find('label').text()).toBe('사용자명')
    expect(wrapper.find('input').exists()).toBe(true)
  })
  
  it('v-model 양방향 바인딩', async () => {
    const wrapper = mount(FormField, {
      props: {
        label: '이메일',
        modelValue: '',
        'onUpdate:modelValue': (value: string) => wrapper.setProps({ modelValue: value })
      }
    })
    
    const input = wrapper.find('input')
    await input.setValue('test@example.com')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['test@example.com'])
  })
  
  it('required 표시', () => {
    const wrapper = mount(FormField, {
      props: {
        label: '비밀번호',
        modelValue: '',
        required: true
      }
    })
    
    expect(wrapper.find('.required-indicator').exists()).toBe(true)
    expect(wrapper.find('label').text()).toContain('*')
  })
  
  it('에러 메시지 표시', () => {
    const errorMessage = '이메일 형식이 올바르지 않습니다'
    const wrapper = mount(FormField, {
      props: {
        label: '이메일',
        modelValue: 'invalid',
        error: errorMessage
      }
    })
    
    expect(wrapper.find('.error-message').text()).toBe(errorMessage)
    expect(wrapper.find('input').classes()).toContain('error')
  })
  
  it('도움말 텍스트 표시', () => {
    const helpText = '8자 이상 입력하세요'
    const wrapper = mount(FormField, {
      props: {
        label: '비밀번호',
        modelValue: '',
        help: helpText
      }
    })
    
    expect(wrapper.find('.help-text').text()).toBe(helpText)
  })
  
  it('disabled 상태', () => {
    const wrapper = mount(FormField, {
      props: {
        label: '입력',
        modelValue: 'value',
        disabled: true
      }
    })
    
    expect(wrapper.find('input').attributes('disabled')).toBeDefined()
  })
})
```

### Testing Library를 사용한 컴포넌트 테스트

```typescript
// shared/ui/organisms/LoginForm.test.ts
import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/vue'
import userEvent from '@testing-library/user-event'
import LoginForm from './LoginForm.vue'

describe('LoginForm', () => {
  it('로그인 폼 렌더링', () => {
    render(LoginForm)
    
    expect(screen.getByLabelText('사용자명')).toBeInTheDocument()
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument()
  })
  
  it('사용자 입력 처리', async () => {
    const user = userEvent.setup()
    render(LoginForm)
    
    const usernameInput = screen.getByLabelText('사용자명')
    const passwordInput = screen.getByLabelText('비밀번호')
    
    await user.type(usernameInput, 'testuser')
    await user.type(passwordInput, 'password123')
    
    expect(usernameInput).toHaveValue('testuser')
    expect(passwordInput).toHaveValue('password123')
  })
  
  it('폼 제출 시 onSubmit 호출', async () => {
    const user = userEvent.setup()
    const handleSubmit = vi.fn()
    
    render(LoginForm, {
      props: {
        onSubmit: handleSubmit
      }
    })
    
    await user.type(screen.getByLabelText('사용자명'), 'admin')
    await user.type(screen.getByLabelText('비밀번호'), 'admin123')
    await user.click(screen.getByRole('button', { name: '로그인' }))
    
    await waitFor(() => {
      expect(handleSubmit).toHaveBeenCalledWith({
        username: 'admin',
        password: 'admin123'
      })
    })
  })
  
  it('빈 필드로 제출 시 검증 에러', async () => {
    const user = userEvent.setup()
    render(LoginForm)
    
    await user.click(screen.getByRole('button', { name: '로그인' }))
    
    await waitFor(() => {
      expect(screen.getByText('사용자명을 입력하세요')).toBeInTheDocument()
      expect(screen.getByText('비밀번호를 입력하세요')).toBeInTheDocument()
    })
  })
  
  it('로그인 중 로딩 상태 표시', async () => {
    const user = userEvent.setup()
    const slowSubmit = vi.fn(() => new Promise(resolve => setTimeout(resolve, 1000)))
    
    render(LoginForm, {
      props: {
        onSubmit: slowSubmit
      }
    })
    
    await user.type(screen.getByLabelText('사용자명'), 'admin')
    await user.type(screen.getByLabelText('비밀번호'), 'admin123')
    await user.click(screen.getByRole('button', { name: '로그인' }))
    
    expect(screen.getByRole('button', { name: '로그인 중...' })).toBeDisabled()
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument()
  })
})
```

---

## 통합 테스트

### API 통합 테스트

```typescript
// shared/api/client.test.ts
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setupServer } from 'msw/node'
import { http, HttpResponse } from 'msw'
import { createApiClient } from './client'

const server = setupServer()

beforeEach(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('ApiClient', () => {
  const apiClient = createApiClient({
    baseURL: 'http://localhost:8090/api/v1'
  })
  
  it('GET 요청 성공', async () => {
    server.use(
      http.get('http://localhost:8090/api/v1/users/123', () => {
        return HttpResponse.json({
          success: true,
          data: {
            id: '123',
            username: 'testuser',
            email: 'test@example.com'
          }
        })
      })
    )
    
    const response = await apiClient.get('/users/123')
    
    expect(response.success).toBe(true)
    expect(response.data.username).toBe('testuser')
  })
  
  it('POST 요청 성공', async () => {
    server.use(
      http.post('http://localhost:8090/api/v1/users', async ({ request }) => {
        const body = await request.json()
        return HttpResponse.json({
          success: true,
          data: {
            id: '456',
            ...body
          }
        }, { status: 201 })
      })
    )
    
    const newUser = {
      username: 'newuser',
      email: 'new@example.com'
    }
    
    const response = await apiClient.post('/users', newUser)
    
    expect(response.success).toBe(true)
    expect(response.data.username).toBe('newuser')
  })
  
  it('401 에러 시 토큰 갱신 후 재시도', async () => {
    let requestCount = 0
    
    server.use(
      http.get('http://localhost:8090/api/v1/protected', () => {
        requestCount++
        if (requestCount === 1) {
          return HttpResponse.json(
            { success: false, error: { code: 'UNAUTHORIZED' } },
            { status: 401 }
          )
        }
        return HttpResponse.json({ success: true, data: 'protected data' })
      }),
      http.post('http://localhost:8090/api/v1/auth/refresh', () => {
        return HttpResponse.json({
          success: true,
          data: { accessToken: 'new-token' }
        })
      })
    )
    
    const response = await apiClient.get('/protected')
    
    expect(requestCount).toBe(2)  // 초기 요청 + 재시도
    expect(response.success).toBe(true)
  })
  
  it('네트워크 에러 처리', async () => {
    server.use(
      http.get('http://localhost:8090/api/v1/data', () => {
        return HttpResponse.error()
      })
    )
    
    await expect(apiClient.get('/data')).rejects.toThrow()
  })
  
  it('요청 헤더에 인증 토큰 포함', async () => {
    let capturedHeaders: Headers | undefined
    
    server.use(
      http.get('http://localhost:8090/api/v1/data', ({ request }) => {
        capturedHeaders = request.headers
        return HttpResponse.json({ success: true, data: {} })
      })
    )
    
    apiClient.setToken('test-token')
    await apiClient.get('/data')
    
    expect(capturedHeaders?.get('Authorization')).toBe('Bearer test-token')
  })
})
```

---

## E2E 테스트 (Playwright)

### 로그인 플로우 테스트

```typescript
// tests/e2e/auth/login.spec.ts
import { test, expect } from '@playwright/test'

test.describe('로그인 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })
  
  test('로그인 페이지 렌더링', async ({ page }) => {
    await expect(page.locator('h1')).toHaveText('로그인')
    await expect(page.getByLabel('사용자명')).toBeVisible()
    await expect(page.getByLabel('비밀번호')).toBeVisible()
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible()
  })
  
  test('유효한 자격증명으로 로그인 성공', async ({ page }) => {
    // Given
    await page.getByLabel('사용자명').fill('admin')
    await page.getByLabel('비밀번호').fill('admin123')
    
    // When
    await page.getByRole('button', { name: '로그인' }).click()
    
    // Then
    await expect(page).toHaveURL('/dashboard')
    await expect(page.getByText('admin')).toBeVisible()  // 사용자명 표시
  })
  
  test('잘못된 자격증명으로 로그인 실패', async ({ page }) => {
    // Given
    await page.getByLabel('사용자명').fill('wronguser')
    await page.getByLabel('비밀번호').fill('wrongpass')
    
    // When
    await page.getByRole('button', { name: '로그인' }).click()
    
    // Then
    await expect(page.getByText('사용자명 또는 비밀번호가 올바르지 않습니다')).toBeVisible()
    await expect(page).toHaveURL('/login')
  })
  
  test('빈 필드로 제출 시 검증 에러', async ({ page }) => {
    // When
    await page.getByRole('button', { name: '로그인' }).click()
    
    // Then
    await expect(page.getByText('사용자명을 입력하세요')).toBeVisible()
    await expect(page.getByText('비밀번호를 입력하세요')).toBeVisible()
  })
  
  test('비밀번호 표시/숨김 토글', async ({ page }) => {
    const passwordInput = page.getByLabel('비밀번호')
    const toggleButton = page.getByRole('button', { name: '비밀번호 표시' })
    
    // 초기 상태: 비밀번호 숨김
    await expect(passwordInput).toHaveAttribute('type', 'password')
    
    // 클릭: 비밀번호 표시
    await toggleButton.click()
    await expect(passwordInput).toHaveAttribute('type', 'text')
    
    // 다시 클릭: 비밀번호 숨김
    await toggleButton.click()
    await expect(passwordInput).toHaveAttribute('type', 'password')
  })
  
  test('Remember Me 체크박스', async ({ page }) => {
    const checkbox = page.getByLabel('로그인 상태 유지')
    
    await checkbox.check()
    await expect(checkbox).toBeChecked()
    
    await checkbox.uncheck()
    await expect(checkbox).not.toBeChecked()
  })
  
  test('Enter 키로 폼 제출', async ({ page }) => {
    await page.getByLabel('사용자명').fill('admin')
    await page.getByLabel('비밀번호').fill('admin123')
    await page.getByLabel('비밀번호').press('Enter')
    
    await expect(page).toHaveURL('/dashboard')
  })
})
```

### 대시보드 E2E 테스트

```typescript
// tests/e2e/dashboard/dashboard.spec.ts
import { test, expect } from '@playwright/test'

test.describe('대시보드', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인
    await page.goto('/login')
    await page.getByLabel('사용자명').fill('admin')
    await page.getByLabel('비밀번호').fill('admin123')
    await page.getByRole('button', { name: '로그인' }).click()
    await expect(page).toHaveURL('/dashboard')
  })
  
  test('대시보드 위젯 렌더링', async ({ page }) => {
    // 통계 카드
    await expect(page.getByTestId('total-cases')).toBeVisible()
    await expect(page.getByTestId('pending-cases')).toBeVisible()
    await expect(page.getByTestId('high-risk-alerts')).toBeVisible()
    
    // 차트
    await expect(page.getByTestId('cases-trend-chart')).toBeVisible()
    await expect(page.getByTestId('risk-distribution-chart')).toBeVisible()
    
    // 최근 사례 테이블
    await expect(page.getByRole('table')).toBeVisible()
  })
  
  test('통계 카드 데이터 표시', async ({ page }) => {
    const totalCases = page.getByTestId('total-cases')
    await expect(totalCases.locator('.value')).toHaveText(/^\d+$/)  // 숫자
    await expect(totalCases.locator('.label')).toHaveText('전체 사례')
  })
  
  test('날짜 범위 필터', async ({ page }) => {
    // 날짜 선택기 열기
    await page.getByLabel('기간 선택').click()
    
    // 지난 7일 선택
    await page.getByRole('button', { name: '지난 7일' }).click()
    
    // 데이터 로딩 대기
    await page.waitForLoadState('networkidle')
    
    // 차트 업데이트 확인
    await expect(page.getByTestId('cases-trend-chart')).toBeVisible()
  })
  
  test('최근 사례 테이블 정렬', async ({ page }) => {
    const table = page.getByRole('table')
    
    // 날짜 컬럼 헤더 클릭 (오름차순)
    await table.locator('th:has-text("생성일")').click()
    await page.waitForTimeout(500)
    
    // 다시 클릭 (내림차순)
    await table.locator('th:has-text("생성일")').click()
    await page.waitForTimeout(500)
    
    // 정렬 아이콘 확인
    await expect(table.locator('th:has-text("생성일") .sort-icon')).toBeVisible()
  })
  
  test('사례 상세 페이지 이동', async ({ page }) => {
    const firstRow = page.getByRole('table').locator('tbody tr').first()
    await firstRow.click()
    
    await expect(page).toHaveURL(/\/cases\/.*/)
    await expect(page.locator('h1')).toContainText('사례 상세')
  })
  
  test('실시간 알림 수신', async ({ page }) => {
    // SSE 연결 대기
    await page.waitForTimeout(1000)
    
    // 알림 트리거 (개발용 버튼)
    if (await page.getByTestId('trigger-notification').isVisible()) {
      await page.getByTestId('trigger-notification').click()
      
      // 토스트 알림 표시 확인
      await expect(page.getByRole('alert')).toBeVisible()
      await expect(page.getByRole('alert')).toContainText('새로운 알림')
    }
  })
})
```

### 사용자 관리 E2E 테스트

```typescript
// tests/e2e/admin/user-management.spec.ts
import { test, expect } from '@playwright/test'

test.describe('사용자 관리', () => {
  test.beforeEach(async ({ page }) => {
    // ADMIN 권한으로 로그인
    await page.goto('/login')
    await page.getByLabel('사용자명').fill('admin')
    await page.getByLabel('비밀번호').fill('admin123')
    await page.getByRole('button', { name: '로그인' }).click()
    
    // 사용자 관리 페이지 이동
    await page.goto('/admin/users')
  })
  
  test('사용자 목록 조회', async ({ page }) => {
    await expect(page.getByRole('heading', { name: '사용자 관리' })).toBeVisible()
    await expect(page.getByRole('table')).toBeVisible()
    
    // 최소 1개 이상의 사용자 존재
    const rows = page.getByRole('table').locator('tbody tr')
    await expect(rows).toHaveCount(await rows.count())
  })
  
  test('사용자 생성', async ({ page }) => {
    // 생성 버튼 클릭
    await page.getByRole('button', { name: '사용자 추가' }).click()
    
    // 모달 표시 확인
    await expect(page.getByRole('dialog')).toBeVisible()
    
    // 폼 입력
    await page.getByLabel('사용자명').fill('newuser')
    await page.getByLabel('비밀번호').fill('Password123!')
    await page.getByLabel('이메일').fill('newuser@example.com')
    await page.getByLabel('이름').fill('신규 사용자')
    await page.getByLabel('조직').selectOption({ label: '본사' })
    await page.getByLabel('권한 그룹').selectOption({ label: '일반 사용자' })
    
    // 저장
    await page.getByRole('button', { name: '저장' }).click()
    
    // 성공 메시지
    await expect(page.getByText('사용자가 생성되었습니다')).toBeVisible()
    
    // 목록에 추가 확인
    await expect(page.getByRole('cell', { name: 'newuser' })).toBeVisible()
  })
  
  test('사용자 검색', async ({ page }) => {
    const searchBox = page.getByPlaceholder('사용자명 또는 이메일 검색')
    
    await searchBox.fill('admin')
    await searchBox.press('Enter')
    
    // 로딩 대기
    await page.waitForLoadState('networkidle')
    
    // 검색 결과 확인
    const rows = page.getByRole('table').locator('tbody tr')
    await expect(rows.first()).toContainText('admin')
  })
  
  test('사용자 수정', async ({ page }) => {
    // 첫 번째 사용자의 수정 버튼 클릭
    await page.getByRole('table').locator('tbody tr').first()
      .getByRole('button', { name: '수정' }).click()
    
    // 모달 표시
    await expect(page.getByRole('dialog')).toBeVisible()
    
    // 이름 수정
    const nameInput = page.getByLabel('이름')
    await nameInput.clear()
    await nameInput.fill('수정된 이름')
    
    // 저장
    await page.getByRole('button', { name: '저장' }).click()
    
    // 성공 메시지
    await expect(page.getByText('사용자 정보가 수정되었습니다')).toBeVisible()
  })
  
  test('사용자 삭제', async ({ page }) => {
    // 첫 번째 사용자의 삭제 버튼 클릭
    await page.getByRole('table').locator('tbody tr').first()
      .getByRole('button', { name: '삭제' }).click()
    
    // 확인 다이얼로그
    await expect(page.getByText('정말 삭제하시겠습니까?')).toBeVisible()
    await page.getByRole('button', { name: '확인' }).click()
    
    // 성공 메시지
    await expect(page.getByText('사용자가 삭제되었습니다')).toBeVisible()
  })
  
  test('페이지네이션', async ({ page }) => {
    // 페이지 2로 이동
    await page.getByRole('button', { name: '다음 페이지' }).click()
    
    // URL 파라미터 확인
    await expect(page).toHaveURL(/page=2/)
    
    // 데이터 로딩 확인
    await expect(page.getByRole('table')).toBeVisible()
  })
})
```

---

## Skills 활용 (Playwright Skill)

### Playwright Skill 소개

Playwright Skill은 AI 기반 E2E 테스트 코드 생성 도구입니다. 자연어 시나리오를 입력하면 BDD 스타일의 Playwright 테스트 코드를 자동 생성합니다.

### Skill 활성화

```bash
# Claude Code에서 Playwright Skill 활성화
/skill playwright
```

### 사용 시나리오

#### 1. 새로운 E2E 테스트 생성

**입력:**
```
Playwright Skill을 사용하여 사용자 관리 페이지 E2E 테스트를 작성해주세요.

시나리오:
1. 관리자로 로그인
2. 사용자 관리 페이지 이동
3. 새 사용자 추가 버튼 클릭
4. 사용자 정보 입력 (사용자명, 이메일, 이름, 조직)
5. 저장 버튼 클릭
6. 성공 메시지 확인
7. 목록에서 생성된 사용자 확인
```

**출력 (자동 생성):**
```typescript
// tests/e2e/admin/user-create.spec.ts
import { test, expect } from '@playwright/test'

test.describe('사용자 관리 - 생성', () => {
  test.beforeEach(async ({ page }) => {
    // 관리자 로그인
    await page.goto('/login')
    await page.getByLabel('사용자명').fill('admin')
    await page.getByLabel('비밀번호').fill('admin123')
    await page.getByRole('button', { name: '로그인' }).click()
    await expect(page).toHaveURL('/dashboard')

    // 사용자 관리 페이지 이동
    await page.goto('/admin/users')
  })

  test('새 사용자를 성공적으로 생성한다', async ({ page }) => {
    // Given: 사용자 관리 페이지
    await expect(page.getByRole('heading', { name: '사용자 관리' })).toBeVisible()

    // When: 새 사용자 추가
    await page.getByRole('button', { name: '사용자 추가' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()

    await page.getByLabel('사용자명').fill('newuser')
    await page.getByLabel('이메일').fill('newuser@example.com')
    await page.getByLabel('이름').fill('신규 사용자')
    await page.getByLabel('조직').selectOption({ label: '본사' })

    await page.getByRole('button', { name: '저장' }).click()

    // Then: 성공 메시지 및 목록 확인
    await expect(page.getByText('사용자가 생성되었습니다')).toBeVisible()
    await expect(page.getByRole('cell', { name: 'newuser' })).toBeVisible()
  })
})
```

#### 2. 복잡한 사용자 플로우 테스트

**입력:**
```
사례 처리 워크플로우 E2E 테스트를 작성해주세요:
- 새로운 STR 사례 생성
- 담당자 배정
- 조사 내용 입력
- 증빙 파일 첨부
- 승인 요청
- 승인자로 로그인하여 승인
- 상태가 '승인됨'으로 변경 확인
```

**출력:** BDD 스타일의 완전한 E2E 테스트 코드 생성

#### 3. 데이터 기반 테스트 생성

**입력:**
```
로그인 실패 시나리오를 다양한 케이스로 테스트하고 싶습니다:
- 빈 사용자명
- 빈 비밀번호
- 잘못된 사용자명
- 잘못된 비밀번호
- 계정 잠금 (5회 실패)
```

**출력:** `test.each()` 또는 개별 테스트 케이스로 구성된 데이터 기반 테스트

### Skill 활용 Best Practices

**✅ DO:**
- 명확한 시나리오를 한글로 작성
- Given-When-Then 구조로 설명
- 페이지별, 기능별로 분리하여 요청
- 생성된 코드 검토 및 프로젝트에 맞게 수정

**❌ DON'T:**
- 모든 테스트를 한 번에 요청 (너무 복잡)
- 구현 세부사항 포함 (예: "클릭 이벤트 핸들러 호출")
- 기술적 용어만 나열 (비즈니스 관점 우선)

---

## MCP Playwright Server 활용

### MCP Playwright Server 소개

MCP (Model Context Protocol) Playwright Server는 실시간 브라우저 자동화 도구로, Claude Code가 직접 브라우저를 제어하여 웹 애플리케이션을 테스트하고 검증할 수 있습니다.

### 사용 가능한 MCP 도구

#### 1. 브라우저 탐색

```javascript
// 페이지 이동
mcp__playwright__browser_navigate({
  url: 'http://localhost:3000/login'
})

// 뒤로 가기
mcp__playwright__browser_navigate_back()
```

#### 2. 페이지 스냅샷 (구조 분석)

```javascript
// 현재 페이지의 접근성 트리 캡처 (권장)
mcp__playwright__browser_snapshot()

// 결과: 페이지의 구조화된 텍스트 표현
// - 모든 클릭 가능한 요소
// - 입력 필드
// - 텍스트 콘텐츠
// - ARIA 정보
```

**사용 사례:**
- 페이지 구조 이해
- 테스트 작성 전 요소 확인
- 동적 콘텐츠 검증

#### 3. 요소 상호작용

```javascript
// 클릭
mcp__playwright__browser_click({
  element: '로그인 버튼',  // 사람이 읽을 수 있는 설명
  ref: 'button[name="login"]'  // 실제 선택자 (snapshot에서 가져옴)
})

// 텍스트 입력
mcp__playwright__browser_type({
  element: '사용자명 입력',
  ref: 'input[name="username"]',
  text: 'admin',
  submit: false  // Enter 키 누르지 않음
})

// 드래그 앤 드롭
mcp__playwright__browser_drag({
  startElement: '작업 카드',
  startRef: '[data-task-id="123"]',
  endElement: '완료 컬럼',
  endRef: '[data-column="done"]'
})

// 호버
mcp__playwright__browser_hover({
  element: '도움말 아이콘',
  ref: '.help-icon'
})
```

#### 4. 스크린샷 캡처

```javascript
// 전체 페이지 스크린샷
mcp__playwright__browser_take_screenshot({
  filename: 'dashboard-overview.png',
  fullPage: true,
  type: 'png'
})

// 특정 요소만 스크린샷
mcp__playwright__browser_take_screenshot({
  element: '통계 차트',
  ref: '[data-testid="statistics-chart"]',
  filename: 'chart.png'
})
```

#### 5. 디버깅 도구

```javascript
// 콘솔 메시지 확인
mcp__playwright__browser_console_messages({
  onlyErrors: true  // 에러만 표시
})

// 네트워크 요청 확인
mcp__playwright__browser_network_requests()

// JavaScript 실행
mcp__playwright__browser_evaluate({
  function: `() => {
    return {
      userCount: document.querySelectorAll('.user-item').length,
      appVersion: window.__APP_VERSION__
    }
  }`
})
```

### 실전 활용 사례

#### 사례 1: 수동 UI 검증

```typescript
// 시나리오: 새로운 대시보드 위젯 배치 확인

// 1. 페이지 이동
mcp__playwright__browser_navigate({ url: 'http://localhost:3000/dashboard' })

// 2. 페이지 구조 분석
const snapshot = mcp__playwright__browser_snapshot()
// → 모든 위젯의 위치와 내용 확인

// 3. 특정 위젯 스크린샷
mcp__playwright__browser_take_screenshot({
  element: '매출 차트 위젯',
  ref: '[data-widget="revenue-chart"]',
  filename: 'revenue-widget.png'
})

// 4. 콘솔 에러 확인
const errors = mcp__playwright__browser_console_messages({ onlyErrors: true })
// → React 에러, API 에러 등 확인
```

#### 사례 2: 인터랙티브 디버깅

```typescript
// 시나리오: 폼 제출 시 에러 재현

// 1. 로그인
mcp__playwright__browser_navigate({ url: 'http://localhost:3000/login' })
mcp__playwright__browser_type({
  element: '사용자명',
  ref: 'input[name="username"]',
  text: 'testuser'
})
mcp__playwright__browser_type({
  element: '비밀번호',
  ref: 'input[name="password"]',
  text: 'password123',
  submit: true  // Enter 키 자동 입력
})

// 2. 사용자 생성 페이지 이동
mcp__playwright__browser_navigate({ url: 'http://localhost:3000/admin/users/new' })

// 3. 스냅샷으로 폼 구조 확인
mcp__playwright__browser_snapshot()

// 4. 폼 입력
mcp__playwright__browser_click({
  element: '조직 선택',
  ref: 'select[name="organization"]'
})

// 5. 네트워크 요청 모니터링
const requests = mcp__playwright__browser_network_requests()
// → API 호출 실패 원인 파악
```

#### 사례 3: 시각적 회귀 테스트

```typescript
// 시나리오: UI 변경 전후 비교

// Before 스크린샷
mcp__playwright__browser_navigate({ url: 'http://localhost:3000/reports' })
mcp__playwright__browser_take_screenshot({
  filename: 'reports-before.png',
  fullPage: true
})

// ... 코드 변경 후 재시작 ...

// After 스크린샷
mcp__playwright__browser_take_screenshot({
  filename: 'reports-after.png',
  fullPage: true
})

// → 수동으로 이미지 비교 또는 픽셀 diff 도구 사용
```

### MCP vs Playwright Skill 비교

| 기능 | MCP Playwright Server | Playwright Skill |
|------|----------------------|------------------|
| **실시간 브라우저 제어** | ✅ 가능 | ❌ 코드만 생성 |
| **디버깅** | ✅ 즉시 확인 | ❌ 테스트 실행 필요 |
| **테스트 코드 생성** | ❌ 수동 작성 | ✅ 자동 생성 |
| **자동화** | ❌ 수동 조작 | ✅ CI/CD 통합 |
| **사용 시점** | 개발/디버깅 중 | 테스트 작성 시 |

**권장 워크플로우:**
1. **MCP로 탐색** → 페이지 구조 파악, 버그 재현
2. **Skill로 테스트 생성** → E2E 테스트 자동 작성
3. **CI/CD에서 실행** → 지속적 검증

---

## Pinia Store 테스트

### Auth Store 테스트

```typescript
// features/auth/model/auth.store.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import * as authApi from '../api/auth.api'

vi.mock('../api/auth.api')

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })
  
  it('초기 상태', () => {
    const store = useAuthStore()
    
    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('로그인 성공', async () => {
    const store = useAuthStore()
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'mock-token',
        refreshToken: 'mock-refresh',
        user: {
          id: '123',
          username: 'testuser',
          email: 'test@example.com'
        }
      }
    }
    
    vi.mocked(authApi.login).mockResolvedValue(mockResponse)
    
    await store.login('testuser', 'password')
    
    expect(store.accessToken).toBe('mock-token')
    expect(store.user).toEqual(mockResponse.data.user)
    expect(store.isAuthenticated).toBe(true)
  })
  
  it('로그인 실패 시 에러 throw', async () => {
    const store = useAuthStore()
    
    vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))
    
    await expect(store.login('wrong', 'wrong')).rejects.toThrow('Invalid credentials')
    
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('로그아웃', async () => {
    const store = useAuthStore()
    
    // 로그인 상태 설정
    store.$patch({
      accessToken: 'token',
      refreshToken: 'refresh',
      user: { id: '123', username: 'test' }
    })
    
    vi.mocked(authApi.logout).mockResolvedValue({ success: true })
    
    await store.logout()
    
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
  
  it('토큰 갱신', async () => {
    const store = useAuthStore()
    store.$patch({
      refreshToken: 'old-refresh-token'
    })
    
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'new-access-token'
      }
    }
    
    vi.mocked(authApi.refreshToken).mockResolvedValue(mockResponse)
    
    await store.refreshAccessToken()
    
    expect(store.accessToken).toBe('new-access-token')
  })
  
  it('토큰 검증', () => {
    const store = useAuthStore()
    
    // 만료되지 않은 토큰
    const validToken = generateMockJWT({ exp: Date.now() / 1000 + 3600 })
    store.$patch({ accessToken: validToken })
    expect(store.isTokenValid).toBe(true)
    
    // 만료된 토큰
    const expiredToken = generateMockJWT({ exp: Date.now() / 1000 - 3600 })
    store.$patch({ accessToken: expiredToken })
    expect(store.isTokenValid).toBe(false)
  })
})

// Helper function
function generateMockJWT(payload: any): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.mock-signature`
}
```

---

## Composables 테스트

### usePermission Composable 테스트

```typescript
// shared/lib/composables/usePermission.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermission } from './usePermission'
import { useAuthStore } from '~/features/auth/model/auth.store'

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  
  it('hasRole - 권한 확인', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        roles: ['ROLE_ADMIN', 'ROLE_USER']
      }
    })
    
    const { hasRole } = usePermission()
    
    expect(hasRole('ROLE_ADMIN')).toBe(true)
    expect(hasRole('ROLE_USER')).toBe(true)
    expect(hasRole('ROLE_COMPLIANCE')).toBe(false)
  })
  
  it('hasPermission - 권한 확인', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read', 'user:write', 'case:read']
      }
    })
    
    const { hasPermission } = usePermission()
    
    expect(hasPermission('user:read')).toBe(true)
    expect(hasPermission('user:write')).toBe(true)
    expect(hasPermission('user:delete')).toBe(false)
  })
  
  it('hasAnyPermission - 여러 권한 중 하나라도 있으면 true', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read']
      }
    })
    
    const { hasAnyPermission } = usePermission()
    
    expect(hasAnyPermission(['user:read', 'user:write'])).toBe(true)
    expect(hasAnyPermission(['user:write', 'user:delete'])).toBe(false)
  })
  
  it('hasAllPermissions - 모든 권한이 있어야 true', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['user:read', 'user:write']
      }
    })
    
    const { hasAllPermissions } = usePermission()
    
    expect(hasAllPermissions(['user:read', 'user:write'])).toBe(true)
    expect(hasAllPermissions(['user:read', 'user:delete'])).toBe(false)
  })
  
  it('canAccessMenu - 메뉴 접근 권한', () => {
    const authStore = useAuthStore()
    authStore.$patch({
      user: {
        permissions: ['menu:users', 'menu:cases']
      }
    })
    
    const { canAccessMenu } = usePermission()
    
    expect(canAccessMenu('users')).toBe(true)
    expect(canAccessMenu('cases')).toBe(true)
    expect(canAccessMenu('reports')).toBe(false)
  })
})
```

---

## API 모킹

### MSW 핸들러 설정

```typescript
// tests/mocks/handlers.ts
import { http, HttpResponse } from 'msw'

export const handlers = [
  // Auth
  http.post('http://localhost:8090/api/v1/auth/login', async ({ request }) => {
    const { username, password } = await request.json()
    
    if (username === 'admin' && password === 'admin123') {
      return HttpResponse.json({
        success: true,
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          user: {
            id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
            username: 'admin',
            email: 'admin@example.com',
            fullName: '관리자',
            roles: ['ROLE_ADMIN'],
            permissions: ['user:read', 'user:write', 'case:read', 'case:write']
          }
        }
      })
    }
    
    return HttpResponse.json(
      {
        success: false,
        error: {
          code: 'INVALID_CREDENTIALS',
          message: '사용자명 또는 비밀번호가 올바르지 않습니다'
        }
      },
      { status: 401 }
    )
  }),
  
  // Users
  http.get('http://localhost:8090/api/v1/users', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const size = parseInt(url.searchParams.get('size') || '20')
    
    return HttpResponse.json({
      success: true,
      data: {
        items: generateMockUsers(size),
        pagination: {
          page,
          size,
          totalElements: 100,
          totalPages: Math.ceil(100 / size),
          hasNext: page < Math.ceil(100 / size),
          hasPrevious: page > 1
        }
      }
    })
  }),
  
  http.get('http://localhost:8090/api/v1/users/:id', ({ params }) => {
    return HttpResponse.json({
      success: true,
      data: {
        id: params.id,
        username: 'testuser',
        email: 'test@example.com',
        fullName: '테스트 사용자',
        status: 'ACTIVE',
        orgName: '본사',
        permGroups: ['일반 사용자']
      }
    })
  }),
  
  http.post('http://localhost:8090/api/v1/users', async ({ request }) => {
    const body = await request.json()
    
    return HttpResponse.json(
      {
        success: true,
        data: {
          id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
          ...body,
          status: 'ACTIVE',
          createdAt: new Date().toISOString()
        }
      },
      { status: 201 }
    )
  }),
  
  // Cases
  http.get('http://localhost:8090/api/v1/cases', () => {
    return HttpResponse.json({
      success: true,
      data: {
        items: generateMockCases(20),
        pagination: {
          page: 1,
          size: 20,
          totalElements: 45,
          totalPages: 3,
          hasNext: true,
          hasPrevious: false
        }
      }
    })
  })
]

// Mock data generators
function generateMockUsers(count: number) {
  return Array.from({ length: count }, (_, i) => ({
    id: `USER_${i + 1}`,
    username: `user${i + 1}`,
    email: `user${i + 1}@example.com`,
    fullName: `사용자 ${i + 1}`,
    status: 'ACTIVE',
    orgName: '본사',
    createdAt: new Date().toISOString()
  }))
}

function generateMockCases(count: number) {
  const statuses = ['NEW', 'IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED', 'REJECTED']
  const riskLevels = ['HIGH', 'MEDIUM', 'LOW']
  
  return Array.from({ length: count }, (_, i) => ({
    id: `CASE_${i + 1}`,
    caseNumber: `STR-2025-${String(i + 1).padStart(4, '0')}`,
    customerName: `고객 ${i + 1}`,
    amount: Math.floor(Math.random() * 100000000),
    riskLevel: riskLevels[Math.floor(Math.random() * riskLevels.length)],
    status: statuses[Math.floor(Math.random() * statuses.length)],
    assignee: `담당자 ${i % 5 + 1}`,
    createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
  }))
}
```

### MSW 서버 설정

```typescript
// tests/mocks/server.ts
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)
```

### 테스트에서 MSW 사용

```typescript
// tests/setup.ts 에 추가
import { beforeAll, afterEach, afterAll } from 'vitest'
import { server } from './mocks/server'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
```

---

## 테스트 커버리지

### 커버리지 설정 (vitest.config.ts)

```typescript
test: {
  coverage: {
    provider: 'c8',
    reporter: ['text', 'json', 'html', 'lcov'],
    
    // 제외 대상
    exclude: [
      'node_modules/',
      'tests/',
      '**/*.d.ts',
      '**/*.config.*',
      '**/mockData',
      'dist/',
      '.nuxt/',
      '.output/',
      'pages/**',  // E2E로 커버
      'app/layouts/**',  // E2E로 커버
      'app/providers/**'  // 설정 파일
    ],
    
    // 커버리지 목표
    statements: 80,
    branches: 75,
    functions: 80,
    lines: 80,
    
    // 경고로 처리 (빌드 실패 방지)
    all: true,
    skipFull: false
  }
}
```

### 커버리지 리포트 생성

```bash
# 커버리지 생성
npm run test:coverage

# HTML 리포트 확인
open coverage/index.html
```

### 커버리지 목표

| 레이어 | 최소 커버리지 | 목표 커버리지 |
|--------|---------------|---------------|
| **Composables** | 85% | 95% |
| **Utils** | 90% | 95% |
| **Stores** | 80% | 90% |
| **Components (Atoms)** | 80% | 90% |
| **Components (Molecules)** | 75% | 85% |
| **Components (Organisms)** | 70% | 80% |
| **전체** | 80% | 85% |

---

## CI/CD 통합

### GitHub Actions Workflow

```yaml
# .github/workflows/frontend-test.yml
name: Frontend Tests

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-test.yml'
  pull_request:
    branches: [ main, develop ]
    paths:
      - 'frontend/**'

jobs:
  unit-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      
      - name: Run unit tests with coverage
        run: |
          cd frontend
          npm run test:coverage
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/lcov.info
          flags: frontend
          name: frontend-coverage
      
      - name: Upload coverage report
        uses: actions/upload-artifact@v3
        with:
          name: coverage-report
          path: frontend/coverage/

  e2e-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      
      - name: Install Playwright Browsers
        run: |
          cd frontend
          npx playwright install --with-deps
      
      - name: Run E2E tests
        run: |
          cd frontend
          npm run test:e2e
        env:
          BASE_URL: http://localhost:3000
      
      - name: Upload Playwright Report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: playwright-report
          path: frontend/playwright-report/
          retention-days: 30
```

---

## 베스트 프랙티스

### 필수 준수 사항 (MUST)

#### TDD + BDD 필수 준수 체크리스트

**모든 새로운 기능은 다음을 따라야 합니다:**

- [ ] **Red**: 실패하는 테스트를 먼저 작성했는가?
- [ ] **Green**: 최소한의 코드로 테스트를 통과시켰는가?
- [ ] **Refactor**: 테스트가 통과한 상태에서 코드를 개선했는가?
- [ ] **Given-When-Then**: 각 테스트가 명확한 3단계 구조를 따르는가?
- [ ] **한글 시나리오**: 테스트 설명이 비즈니스 요구사항을 반영하는가?
- [ ] **사용자 관점**: 구현이 아닌 행동을 검증하는가?

### 10가지 실용적인 가이드라인

#### 1. 테스트 이름은 시나리오로 작성

**✅ Good:**
```typescript
it('사용자가 잘못된 비밀번호를 5회 입력하면 계정이 잠긴다', () => {})
it('VIP 고객에게 20% 할인이 자동으로 적용된다', () => {})
it('만료된 세션으로 API 호출 시 로그인 페이지로 리다이렉트된다', () => {})
```

**❌ Bad:**
```typescript
it('should work', () => {})
it('test login', () => {})
it('password validation', () => {})
```

#### 2. Given-When-Then 주석 활용

**✅ Good:**
```typescript
it('장바구니에 상품 추가 시 총액이 업데이트된다', () => {
  // Given: 빈 장바구니
  const cart = useCartStore()
  expect(cart.items).toHaveLength(0)

  // When: 상품 추가
  cart.addItem({ id: '123', name: '노트북', price: 1000000 })

  // Then: 총액 확인
  expect(cart.total).toBe(1000000)
  expect(cart.items).toHaveLength(1)
})
```

**❌ Bad:**
```typescript
it('cart test', () => {
  const cart = useCartStore()
  cart.addItem({ id: '123', name: '노트북', price: 1000000 })
  expect(cart.total).toBe(1000000)
})
```

#### 3. 접근성 쿼리 우선 사용

**✅ Good (사용자 관점):**
```typescript
// 역할 기반
screen.getByRole('button', { name: '저장' })
screen.getByRole('textbox', { name: '이메일' })
screen.getByRole('heading', { name: '대시보드' })

// 레이블 기반
screen.getByLabelText('비밀번호')
screen.getByLabelText('생년월일')

// 텍스트 기반
screen.getByText('로그인 성공')
```

**❌ Bad (구현 세부사항):**
```typescript
wrapper.find('.btn-primary')
wrapper.find('#email-input')
wrapper.vm.$refs.saveButton
```

#### 4. 비동기 처리는 waitFor 사용

**✅ Good:**
```typescript
it('API 응답 후 사용자 목록을 표시한다', async () => {
  render(UserList)

  await waitFor(() => {
    expect(screen.getByText('홍길동')).toBeInTheDocument()
    expect(screen.getByText('김철수')).toBeInTheDocument()
  })
})
```

**❌ Bad (플래키 테스트):**
```typescript
it('user list test', async () => {
  render(UserList)
  await new Promise(resolve => setTimeout(resolve, 1000))  // ❌ 고정 딜레이
  expect(screen.getByText('홍길동')).toBeInTheDocument()
})
```

#### 5. 테스트 데이터는 명확하고 의미 있게

**✅ Good:**
```typescript
const validUser = {
  username: 'testuser',
  email: 'test@example.com',
  password: 'ValidPass123!'
}

const invalidEmail = 'not-an-email'
const expiredToken = 'eyJhbGciOiJIUzI1NiJ9.expired.signature'
```

**❌ Bad:**
```typescript
const user = { u: 'a', e: 'b', p: 'c' }
const data = 'test'
const val = 123
```

#### 6. 모킹은 최소화, 필요한 경우만 사용

**✅ Good (실제 컴포넌트 테스트):**
```typescript
it('버튼 클릭 시 카운터가 증가한다', async () => {
  const { getByRole, getByText } = render(Counter)
  const button = getByRole('button', { name: '증가' })

  await userEvent.click(button)

  expect(getByText('카운트: 1')).toBeInTheDocument()
})
```

**❌ Bad (과도한 모킹):**
```typescript
it('counter test', () => {
  const mockIncrement = vi.fn()
  const wrapper = mount(Counter, {
    props: { onIncrement: mockIncrement }
  })
  wrapper.vm.increment()
  expect(mockIncrement).toHaveBeenCalled()
})
```

#### 7. E2E 테스트는 명시적 대기 사용

**✅ Good:**
```typescript
test('로그인 후 대시보드 표시', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('사용자명').fill('admin')
  await page.getByLabel('비밀번호').fill('admin123')
  await page.getByRole('button', { name: '로그인' }).click()

  // 명시적 대기
  await expect(page).toHaveURL('/dashboard')
  await expect(page.getByRole('heading', { name: '대시보드' })).toBeVisible()
})
```

**❌ Bad:**
```typescript
test('login', async ({ page }) => {
  await page.goto('/login')
  await page.fill('#username', 'admin')
  await page.fill('#password', 'admin123')
  await page.click('.submit')
  await page.waitForTimeout(2000)  // ❌ 임의의 대기
  expect(page.url()).toContain('dashboard')
})
```

#### 8. 테스트 격리 보장

**✅ Good:**
```typescript
describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())  // 매 테스트마다 새로운 Pinia
  })

  it('사용자 추가', () => {
    const store = useUserStore()
    store.addUser({ id: '1', name: '홍길동' })
    expect(store.users).toHaveLength(1)
  })

  it('사용자 삭제', () => {
    const store = useUserStore()
    // 이전 테스트와 독립적
    expect(store.users).toHaveLength(0)
  })
})
```

**❌ Bad:**
```typescript
const sharedStore = useUserStore()  // ❌ 전역 상태

it('test 1', () => {
  sharedStore.addUser({ id: '1', name: '홍길동' })
})

it('test 2', () => {
  // test 1의 영향을 받음
  expect(sharedStore.users).toHaveLength(1)
})
```

#### 9. 예외 처리 검증

**✅ Good:**
```typescript
it('중복된 이메일로 가입 시 에러를 표시한다', async () => {
  const { getByLabel, getByRole, getByText } = render(SignupForm)

  // Given: 기존 사용자와 동일한 이메일
  await userEvent.type(getByLabel('이메일'), 'existing@example.com')

  // When: 가입 시도
  await userEvent.click(getByRole('button', { name: '가입' }))

  // Then: 에러 메시지 표시
  await waitFor(() => {
    expect(getByText('이미 사용 중인 이메일입니다')).toBeInTheDocument()
  })
})
```

**❌ Bad:**
```typescript
it('error test', async () => {
  render(SignupForm)
  // 에러 시나리오 미검증
  expect(wrapper.exists()).toBe(true)
})
```

#### 10. 테스트 가독성 우선

**✅ Good:**
```typescript
describe('할인 계산', () => {
  const createVIPUser = () => ({ level: 'VIP', purchases: 100 })
  const createNormalUser = () => ({ level: 'NORMAL', purchases: 10 })

  it('VIP 고객은 20% 할인을 받는다', () => {
    const user = createVIPUser()
    const discount = calculateDiscount(user)
    expect(discount).toBe(0.20)
  })

  it('일반 고객은 할인이 없다', () => {
    const user = createNormalUser()
    const discount = calculateDiscount(user)
    expect(discount).toBe(0.0)
  })
})
```

**❌ Bad:**
```typescript
describe('discount', () => {
  it('test1', () => {
    expect(calc({ l: 'V', p: 100 })).toBe(0.2)
  })

  it('test2', () => {
    expect(calc({ l: 'N', p: 10 })).toBe(0)
  })
})
```

### 추가 권장 사항

**✅ DO:**
- 사용자 관점에서 테스트 작성
- 명확한 테스트 이름 사용 (`describe`, `it`)
- Given-When-Then 패턴 적용
- 접근성 우선 쿼리 사용 (`getByRole`, `getByLabel`)
- 모킹은 최소화, 실제 구현 우선
- 테스트 실패 시 원인을 명확히 파악할 수 있도록 작성

**❌ DON'T:**
- 구현 세부사항 테스트 (내부 state, private method)
- 과도한 스냅샷 테스트
- 여러 테스트 간 상태 공유
- 하드코딩된 타임아웃 사용
- 플래키(Flaky) 테스트 방치
- 한 테스트에서 여러 개념 검증

### 2. 컴포넌트 테스트 팁

```typescript
// ✅ Good: 사용자 관점 테스트
test('로그인 성공 시 대시보드로 이동', async () => {
  const { getByLabel, getByRole } = render(LoginForm)
  
  await user.type(getByLabel('사용자명'), 'admin')
  await user.type(getByLabel('비밀번호'), 'admin123')
  await user.click(getByRole('button', { name: '로그인' }))
  
  expect(mockRouter.push).toHaveBeenCalledWith('/dashboard')
})

// ❌ Bad: 구현 세부사항 테스트
test('loginMethod 호출 시 username과 password 전달', async () => {
  const wrapper = mount(LoginForm)
  wrapper.vm.loginMethod('admin', 'admin123')  // 내부 메서드 직접 호출
  expect(wrapper.vm.username).toBe('admin')  // 내부 state 검증
})
```

### 3. 비동기 테스트

```typescript
// ✅ Good: waitFor 사용
test('데이터 로딩 후 표시', async () => {
  render(UserList)
  
  await waitFor(() => {
    expect(screen.getByText('testuser')).toBeInTheDocument()
  })
})

// ❌ Bad: 고정 타임아웃
test('데이터 로딩', async () => {
  render(UserList)
  await new Promise(resolve => setTimeout(resolve, 1000))  // 플래키!
  expect(screen.getByText('testuser')).toBeInTheDocument()
})
```

### 4. E2E 테스트 안정성

```typescript
// ✅ Good: 명시적 대기
await page.waitForSelector('[data-testid="user-list"]')
await page.click('button[name="save"]')

// ❌ Bad: 임의의 타임아웃
await page.waitForTimeout(1000)
await page.click('button')
```

### 5. 테스트 데이터 관리

```typescript
// tests/fixtures/users.ts
export const mockUsers = {
  admin: {
    id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
    username: 'admin',
    email: 'admin@example.com',
    roles: ['ROLE_ADMIN']
  },
  user: {
    id: '01HGW2N7XKQJBZ9VFQR8X7Y3ZU',
    username: 'user',
    email: 'user@example.com',
    roles: ['ROLE_USER']
  }
}

// 테스트에서 사용
import { mockUsers } from '../fixtures/users'

test('관리자 권한 확인', () => {
  const store = useAuthStore()
  store.$patch({ user: mockUsers.admin })
  
  expect(store.hasRole('ROLE_ADMIN')).toBe(true)
})
```

---

## 참고 자료

### 공식 문서
- [Vitest Documentation](https://vitest.dev/)
- [Vue Test Utils](https://test-utils.vuejs.org/)
- [Testing Library](https://testing-library.com/docs/vue-testing-library/intro/)
- [Playwright Documentation](https://playwright.dev/)
- [MSW Documentation](https://mswjs.io/)
- [Pinia Testing](https://pinia.vuejs.org/cookbook/testing.html)

### 내부 문서
- [Frontend README](./README.md)
- [Components Roadmap](./COMPONENTS_ROADMAP.md)
- [API Contract](../api/CONTRACT.md)
- [State Management](./STATE_MANAGEMENT.md)

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-16 | TDD+BDD 핵심 원칙 추가, Skills & MCP 활용 섹션 추가, 베스트 프랙티스 강화 | 개발팀 |
| 2025-01-13 | 최초 작성 | 개발팀 |

---

**문의사항이 있으시면 개발팀으로 연락 주시기 바랍니다.**