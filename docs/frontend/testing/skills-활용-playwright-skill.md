# Skills 활용 (Playwright Skill)

## Playwright Skill 소개

Playwright Skill은 AI 기반 E2E 테스트 코드 생성 도구입니다. 자연어 시나리오를 입력하면 BDD 스타일의 Playwright 테스트 코드를 자동 생성합니다.

## Skill 활성화

```bash
# Claude Code에서 Playwright Skill 활성화
/skill playwright
```

## 사용 시나리오

### 1. 새로운 E2E 테스트 생성

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

### 2. 복잡한 사용자 플로우 테스트

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

### 3. 데이터 기반 테스트 생성

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

## Skill 활용 Best Practices

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
