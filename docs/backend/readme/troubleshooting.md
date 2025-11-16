# 🔧 Troubleshooting

## 빌드 오류

**Problem:** `Could not find method compile()`

**Solution:** Gradle 7.0+ 사용. `compile` → `implementation` 변경

**Problem:** `Module 'common' not found`

**Solution:** 모듈 의존성 확인
```bash
./gradlew :common:build
./gradlew :server:dependencies
```

## 실행 오류

**Problem:** `Unable to connect to database`

**Solution:** 
1. PostgreSQL 실행 여부 확인
2. 연결 정보 확인 (.env 또는 환경 변수)
3. 데이터베이스 생성 확인

**Problem:** `Port 8090 is already in use`

**Solution:**
```bash
# 포트 사용 프로세스 확인
lsof -i :8090
# 또는
netstat -tulpn | grep 8090

# 프로세스 종료
kill -9 <PID>
```

## 테스트 오류

**Problem:** `No tests found`

**Solution:** Test 클래스/메서드에 `@Test` annotation 확인

**Problem:** `MockitoException: Cannot mock final class`

**Solution:** `build.gradle`에 추가:
```gradle
testImplementation 'org.mockito:mockito-inline:5.x.x'
```

---
