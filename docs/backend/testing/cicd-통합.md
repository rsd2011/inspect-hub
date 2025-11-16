# CI/CD 통합

## GitHub Actions Workflow

```yaml
# .github/workflows/test.yml
name: Backend Tests

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'backend/**'
      - '.github/workflows/test.yml'
  pull_request:
    branches: [ main, develop ]
    paths:
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'
      
      - name: Grant execute permission for gradlew
        run: chmod +x backend/gradlew
      
      - name: Run tests with coverage
        run: |
          cd backend
          ./gradlew clean test jacocoTestReport
        env:
          SPRING_PROFILES_ACTIVE: test
      
      - name: Verify coverage
        run: |
          cd backend
          ./gradlew jacocoTestCoverageVerification
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/build/reports/jacoco/test/jacocoTestReport.xml
          flags: backend
          name: backend-coverage
      
      - name: Publish Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: backend/build/test-results/test/*.xml
      
      - name: Upload Test Report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-report
          path: backend/build/reports/tests/test/
```

## Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running pre-commit checks..."

# Backend 테스트 실행
cd backend
./gradlew test

if [ $? -ne 0 ]; then
    echo "❌ Backend tests failed. Commit aborted."
    exit 1
fi

# 커버리지 검증
./gradlew jacocoTestCoverageVerification

if [ $? -ne 0 ]; then
    echo "❌ Coverage verification failed. Commit aborted."
    exit 1
fi

echo "✅ All checks passed!"
exit 0
```

---
