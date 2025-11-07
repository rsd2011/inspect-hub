# E2E Tests with Playwright

Playwright를 사용한 End-to-End 테스트입니다.

## 테스트 구조

```
tests/e2e/
├── fixtures/          # 테스트 픽스처
│   └── auth.fixture.ts    # 인증 픽스처
├── helpers/           # 헬퍼 함수
│   └── test-utils.ts      # 공통 유틸리티
├── login.spec.ts      # 로그인 페이지 테스트
└── dashboard.spec.ts  # 대시보드 페이지 테스트
```

## 테스트 실행

### 모든 테스트 실행 (헤드리스 모드)
```bash
npm run test:e2e
```

### UI 모드로 테스트 실행 (권장)
```bash
npm run test:e2e:ui
```

### 브라우저를 보면서 테스트 실행
```bash
npm run test:e2e:headed
```

### 디버그 모드로 테스트 실행
```bash
npm run test:e2e:debug
```

### 특정 테스트만 실행
```bash
npx playwright test login.spec.ts
npx playwright test dashboard.spec.ts
```

### 특정 브라우저에서만 실행
```bash
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit
```

### 테스트 리포트 보기
```bash
npm run test:e2e:report
```

## 테스트 케이스

### Login Page Tests (login.spec.ts)
- ✅ 로그인 페이지 올바른 표시
- ✅ 빈 폼 검증 에러 표시
- ✅ 잘못된 인증 정보 에러 처리
- ✅ 유효한 인증 정보로 로그인 성공
- ✅ 로그인 후 토큰 저장 확인
- ✅ 이미 인증된 경우 대시보드로 리다이렉트
- ✅ 비밀번호 가시성 토글
- ✅ SSO 로그인 옵션 표시
- ✅ 네트워크 에러 처리

### Dashboard Page Tests (dashboard.spec.ts)
- ✅ 로그인 후 대시보드 표시
- ✅ 4개의 통계 카드 표시
- ✅ 통계 숫자 올바른 표시
- ✅ 최근 케이스 테이블 표시
- ✅ 테이블 데이터 표시
- ✅ 헤더 컴포넌트 표시
- ✅ 사이드바 컴포넌트 표시
- ✅ 사이드바 토글
- ✅ 사용자 메뉴 열기 및 옵션 표시
- ✅ 로그아웃 기능
- ✅ 사이드바 메뉴를 통한 네비게이션
- ✅ 알림 카운트 표시
- ✅ 언어 전환
- ✅ 모바일 반응형
- ✅ 미인증 접근 차단
- ✅ 로딩 상태 표시
- ✅ 아이콘 올바른 표시

## 주의사항

### 백엔드 API Mock
현재 테스트는 실제 백엔드 API가 필요합니다. Mock API를 사용하려면:

1. MSW (Mock Service Worker) 설치
2. Mock handlers 작성
3. 테스트에서 MSW 활성화

### 테스트 데이터
테스트용 계정:
- Username: `admin`
- Password: `admin123`

### CI/CD 통합
GitHub Actions 또는 다른 CI/CD 파이프라인에서 실행:

```yaml
# .github/workflows/e2e-tests.yml
- name: Install dependencies
  run: npm ci

- name: Install Playwright browsers
  run: npx playwright install --with-deps

- name: Run E2E tests
  run: npm run test:e2e

- name: Upload test results
  uses: actions/upload-artifact@v3
  if: always()
  with:
    name: playwright-report
    path: playwright-report/
```

## 문제 해결

### 테스트가 타임아웃되는 경우
```typescript
// playwright.config.ts에서 타임아웃 증가
timeout: 60 * 1000, // 60초
```

### 개발 서버가 시작되지 않는 경우
```bash
# 수동으로 개발 서버 시작
npm run dev

# 다른 터미널에서 테스트 실행 (webServer 옵션 비활성화)
npx playwright test --config=playwright.config.ts
```

### 브라우저가 설치되지 않은 경우
```bash
npx playwright install
```

## 참고 문서

- [Playwright 공식 문서](https://playwright.dev)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Playwright API Reference](https://playwright.dev/docs/api/class-playwright)
