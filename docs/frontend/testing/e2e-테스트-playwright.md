# E2E 테스트 (Playwright)

## 로그인 플로우 테스트

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

## 대시보드 E2E 테스트

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

## 사용자 관리 E2E 테스트

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
