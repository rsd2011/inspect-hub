# MCP Playwright Server 활용

## MCP Playwright Server 소개

MCP (Model Context Protocol) Playwright Server는 실시간 브라우저 자동화 도구로, Claude Code가 직접 브라우저를 제어하여 웹 애플리케이션을 테스트하고 검증할 수 있습니다.

## 사용 가능한 MCP 도구

### 1. 브라우저 탐색

```javascript
// 페이지 이동
mcp__playwright__browser_navigate({
  url: 'http://localhost:3000/login'
})

// 뒤로 가기
mcp__playwright__browser_navigate_back()
```

### 2. 페이지 스냅샷 (구조 분석)

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

### 3. 요소 상호작용

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

### 4. 스크린샷 캡처

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

### 5. 디버깅 도구

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

## 실전 활용 사례

### 사례 1: 수동 UI 검증

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

### 사례 2: 인터랙티브 디버깅

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

### 사례 3: 시각적 회귀 테스트

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

## MCP vs Playwright Skill 비교

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
