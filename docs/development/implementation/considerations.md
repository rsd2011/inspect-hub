## 🔍 추가 고려사항 (Additional Considerations)

### 설정 값 검증 및 타입 안전성

**설정 키별 검증 규칙:**

```java
@Component
public class ConfigValidator {
    
    public void validate(String key, String value, ValueType type) {
        switch (key) {
            case "auth.password.expiration.days":
                validateIntRange(value, 1, 3650, "비밀번호 만료 일수는 1~3650일 범위여야 합니다");
                break;
                
            case "auth.password.complexity.minLength":
                validateIntRange(value, 4, 128, "최소 길이는 4~128자 범위여야 합니다");
                break;
                
            case "auth.session.accessToken.expirationSeconds":
                validateIntRange(value, 300, 86400, "Access Token 만료는 5분~24시간 범위여야 합니다");
                break;
                
            case "auth.session.refreshToken.expirationSeconds":
                validateIntRange(value, 3600, 604800, "Refresh Token 만료는 1시간~7일 범위여야 합니다");
                break;
                
            case "auth.ip.whitelist.allowedIpRanges":
                validateCidrList(value, "CIDR 표기법이 올바르지 않습니다");
                break;
                
            case "auth.login.priority":
                validateEnum(value, List.of("LOCAL", "AD", "SSO"), "유효하지 않은 로그인 방식입니다");
                validateUnique(value, "중복된 로그인 방식이 있습니다");
                break;
                
            default:
                // 기본 타입 검증만 수행
                validateType(value, type);
        }
    }
    
    private void validateIntRange(String value, int min, int max, String message) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue < min || intValue > max) {
                throw new ConfigValidationException(message);
            }
        } catch (NumberFormatException e) {
            throw new ConfigValidationException("정수 형식이 아닙니다");
        }
    }
    
    private void validateCidrList(String value, String message) {
        List<String> cidrs = Arrays.asList(value.split(","));
        for (String cidr : cidrs) {
            if (!CidrUtils.isValidCIDR(cidr.trim())) {
                throw new ConfigValidationException(message + ": " + cidr);
            }
        }
    }
    
    private void validateEnum(String value, List<String> allowedValues, String message) {
        List<String> values = Arrays.asList(value.split(","));
        for (String val : values) {
            if (!allowedValues.contains(val.trim())) {
                throw new ConfigValidationException(message + ": " + val);
            }
        }
    }
    
    private void validateUnique(String value, String message) {
        List<String> values = Arrays.asList(value.split(","));
        Set<String> uniqueValues = new HashSet<>(values);
        if (uniqueValues.size() != values.size()) {
            throw new ConfigValidationException(message);
        }
    }
}
```

**Frontend 검증 스키마 (Zod):**

```typescript
// features/system-config/model/validation.schemas.ts

import { z } from 'zod'

export const configSchemas = {
  'auth.password.expiration.days': z.number()
    .int('정수만 입력 가능합니다')
    .min(1, '최소 1일 이상이어야 합니다')
    .max(3650, '최대 10년(3650일) 이하여야 합니다'),
    
  'auth.password.complexity.minLength': z.number()
    .int()
    .min(4, '최소 4자 이상')
    .max(128, '최대 128자 이하'),
    
  'auth.session.accessToken.expirationSeconds': z.number()
    .int()
    .min(300, '최소 5분(300초)')
    .max(86400, '최대 24시간(86400초)'),
    
  'auth.session.refreshToken.expirationSeconds': z.number()
    .int()
    .min(3600, '최소 1시간(3600초)')
    .max(604800, '최대 7일(604800초)'),
    
  'auth.ip.whitelist.allowedIpRanges': z.array(
    z.string().refine(
      (val) => /^(\d{1,3}\.){3}\d{1,3}\/\d{1,2}$/.test(val),
      'CIDR 표기법이 올바르지 않습니다 (예: 192.168.1.0/24)'
    )
  ),
  
  'auth.login.priority': z.array(
    z.enum(['LOCAL', 'AD', 'SSO'])
  ).refine(
    (arr) => new Set(arr).size === arr.length,
    '중복된 로그인 방식이 있습니다'
  )
}

export const validateConfig = (key: string, value: any) => {
  const schema = configSchemas[key]
  if (!schema) {
    return { success: true, data: value }
  }
  return schema.safeParse(value)
}
```

### 에러 처리 전략

**에러 코드 체계:**

```java
public enum ConfigErrorCode {
    // 설정 조회 에러
    CONFIG_NOT_FOUND("CFG001", "설정을 찾을 수 없습니다"),
    CONFIG_PARSE_ERROR("CFG002", "설정 값 파싱 실패"),
    
    // 설정 검증 에러
    CONFIG_VALIDATION_FAILED("CFG101", "설정 검증 실패"),
    CONFIG_INVALID_RANGE("CFG102", "설정 값이 유효 범위를 벗어났습니다"),
    CONFIG_INVALID_FORMAT("CFG103", "설정 형식이 올바르지 않습니다"),
    CONFIG_REQUIRED("CFG104", "필수 설정이 누락되었습니다"),
    
    // 설정 수정 에러
    CONFIG_READ_ONLY("CFG201", "읽기 전용 설정입니다"),
    CONFIG_UPDATE_FAILED("CFG202", "설정 업데이트 실패"),
    CONFIG_OPTIMISTIC_LOCK("CFG203", "다른 사용자가 설정을 수정했습니다"),
    
    // 캐시 에러
    CACHE_UNAVAILABLE("CFG301", "캐시 서버에 연결할 수 없습니다"),
    CACHE_INVALIDATION_FAILED("CFG302", "캐시 무효화 실패"),
    
    // 환경 변수 에러
    ENV_VAR_NOT_SET("CFG401", "필수 환경 변수가 설정되지 않았습니다"),
    ENV_VAR_INVALID("CFG402", "환경 변수 값이 유효하지 않습니다");
    
    private final String code;
    private final String message;
    
    // constructor, getters
}
```

**Exception Handling:**

```java
@RestControllerAdvice
public class ConfigExceptionHandler {
    
    @ExceptionHandler(ConfigValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ConfigValidationException ex) {
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDetails()
            ));
    }
    
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(
                ConfigErrorCode.CONFIG_OPTIMISTIC_LOCK,
                "설정이 다른 사용자에 의해 수정되었습니다. 새로고침 후 다시 시도하세요."
            ));
    }
}
```

### 성능 모니터링

**메트릭 수집:**

```java
@Service
@RequiredArgsConstructor
public class ConfigurationMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 설정 조회 시간 측정
    public void recordGetConfigTime(String key, long durationMs, String source) {
        Timer.builder("config.get.duration")
            .tag("key", sanitizeKey(key))
            .tag("source", source)  // cache, db, properties
            .register(meterRegistry)
            .record(Duration.ofMillis(durationMs));
    }
    
    // 캐시 히트율 측정
    public void recordCacheHit(String key, boolean hit) {
        Counter.builder("config.cache")
            .tag("key", sanitizeKey(key))
            .tag("result", hit ? "hit" : "miss")
            .register(meterRegistry)
            .increment();
    }
    
    // 설정 변경 빈도 측정
    public void recordConfigUpdate(String key) {
        Counter.builder("config.update")
            .tag("key", sanitizeKey(key))
            .register(meterRegistry)
            .increment();
    }
    
    private String sanitizeKey(String key) {
        // 키를 카테고리로 그룹화 (auth.password.* → auth.password)
        int lastDot = key.lastIndexOf('.');
        return lastDot > 0 ? key.substring(0, lastDot) : key;
    }
}
```

**성능 기준:**

- [ ] 설정 조회 응답 시간 - 평균 < 50ms (캐시), < 200ms (DB), < 100ms (Properties)
- [ ] 캐시 히트율 - > 95%
- [ ] 설정 업데이트 응답 시간 - < 500ms
- [ ] 동시 설정 변경 처리 - TPS 100+ (Optimistic Lock)

### 운영 가이드라인

**설정 변경 시 고려사항:**

1. **비밀번호 만료 정책 변경:**
   - [ ] 변경 전 사용자 공지 (이메일/시스템 알림)
   - [ ] 기존 사용자 영향 평가 (만료 예정자 수 확인)
   - [ ] 점진적 적용 고려 (새 사용자부터 적용)

2. **세션 타임아웃 변경:**
   - [ ] 피크 시간대 피하기 (업무 시간 외 변경 권장)
   - [ ] 기존 세션 유지 (새 로그인부터 적용)
   - [ ] 사용자 공지 (세션 만료 시간 안내)

3. **로그인 방식 변경:**
   - [ ] AD/SSO 활성화 전 연결 테스트 필수
   - [ ] Fallback 경로 확보 (LOCAL 로그인 유지)
   - [ ] 단계적 전환 (일부 사용자 파일럿)

**모니터링 체크리스트:**

- [ ] 설정 변경 후 24시간 모니터링
  - [ ] 로그인 성공률 변화
  - [ ] 로그인 실패 원인 분석
  - [ ] 세션 만료 빈도
  - [ ] 계정 잠금 발생 빈도
  - [ ] 사용자 문의 증가 여부

- [ ] 주간 리뷰
  - [ ] CONFIG_CHANGE_HISTORY 검토
  - [ ] 설정 변경 효과 분석
  - [ ] 사용자 피드백 수집

**롤백 계획:**

```sql
-- 특정 설정 롤백 (변경 이력에서 이전 값 복원)
UPDATE SYSTEM_CONFIG 
SET config_value = (
    SELECT old_value 
    FROM CONFIG_CHANGE_HISTORY 
    WHERE config_key = 'auth.password.expiration.days' 
    ORDER BY changed_at DESC 
    LIMIT 1 OFFSET 1  -- 바로 이전 값
),
version = version + 1,
updated_at = NOW(),
updated_by = 'SYSTEM_ROLLBACK'
WHERE config_key = 'auth.password.expiration.days';

-- 캐시 무효화
-- Redis: DEL config:auth.password.expiration.days
```

**재해 복구 (Disaster Recovery):**

1. **설정 백업 (일일):**
   ```bash
   # 모든 설정을 JSON으로 백업
   curl -H "Authorization: Bearer $ADMIN_TOKEN" \
        http://localhost:8090/api/v1/admin/system-config \
        > config-backup-$(date +%Y%m%d).json
   ```

2. **설정 복원:**
   ```bash
   # JSON 파일에서 설정 복원
   curl -X POST \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d @config-backup-20250113.json \
        http://localhost:8090/api/v1/admin/system-config/import
   ```

3. **Properties Fallback 활용:**
   - DB 전체 장애 시 Properties로 자동 Fallback
   - 서버 재시작으로 Properties 값 로드
   - 임시 운영 후 DB 복구

### 보안 강화 방안

**1. 설정 변경 승인 워크플로 (Optional):**

```java
@Entity
@Table(name = "CONFIG_CHANGE_REQUEST")
public class ConfigChangeRequest {
    @Id
    private String requestId;
    
    private String configKey;
    private String currentValue;
    private String requestedValue;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status;  // PENDING, APPROVED, REJECTED
    
    private String requestedBy;
    private LocalDateTime requestedAt;
    
    private String approvedBy;
    private LocalDateTime approvedAt;
    
    private String rejectionReason;
}
```

**2. 설정 값 암호화 (민감 정보 DB 저장 시):**

```java
@Component
public class ConfigEncryption {
    
    private final AesGcmEncryptor encryptor;
    
    public String encrypt(String configKey, String value) {
        if (isSensitive(configKey)) {
            return encryptor.encrypt(value);
        }
        return value;
    }
    
    public String decrypt(String configKey, String encryptedValue) {
        if (isSensitive(configKey) && encryptedValue.startsWith("ENC(")) {
            return encryptor.decrypt(extractEncryptedPart(encryptedValue));
        }
        return encryptedValue;
    }
    
    private boolean isSensitive(String configKey) {
        return configKey.contains("password") ||
               configKey.contains("secret") ||
               configKey.contains("key");
    }
}
```

**3. 설정 접근 감사:**

- [ ] 모든 설정 조회 기록 (관리자 페이지 접근)
- [ ] 민감 설정 조회 시 추가 인증 (2FA)
- [ ] 비정상적 접근 패턴 탐지 (짧은 시간 다량 조회)

### 테스트 전략

**단위 테스트:**

```java
@Test
void getConfig_DB_우선_Properties_Fallback() {
    // Given
    when(systemConfigRepository.findById("auth.password.expiration.days"))
        .thenReturn(Optional.empty());
    when(environment.getProperty("auth.password.expiration.days"))
        .thenReturn("90");
    
    // When
    String value = configService.getConfig("auth.password.expiration.days");
    
    // Then
    assertEquals("90", value);
    verify(redisTemplate).opsForValue().set(eq("config:auth.password.expiration.days"), eq("90"), any());
}

@Test
void updateConfig_낙관적_락_충돌() {
    // Given
    SystemConfig config = new SystemConfig();
    config.setConfigKey("auth.password.expiration.days");
    config.setConfigValue("90");
    config.setVersion(1);
    
    when(systemConfigRepository.findById(any()))
        .thenReturn(Optional.of(config));
    when(systemConfigRepository.save(any()))
        .thenThrow(new OptimisticLockingFailureException("Version mismatch"));
    
    // When & Then
    assertThrows(OptimisticLockingFailureException.class, () -> {
        configService.updateConfig("auth.password.expiration.days", "120", "admin");
    });
}
```

**통합 테스트:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SystemConfigControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void 설정_전체_조회_성공() throws Exception {
        mockMvc.perform(get("/api/v1/admin/system-config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data['auth.login.local.enabled']").exists());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void 설정_수정_권한_없음() throws Exception {
        mockMvc.perform(put("/api/v1/admin/system-config/auth.password.expiration.days")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"120\"}"))
            .andExpect(status().isForbidden());
    }
}
```

**E2E 테스트 (Playwright):**

```typescript
// e2e/system-settings.spec.ts

test('시스템 설정 변경 및 저장', async ({ page }) => {
  // Given: Admin으로 로그인
  await page.goto('/admin/system-settings')
  
  // When: 비밀번호 만료 일수 변경
  await page.getByLabel('비밀번호 만료 일수').fill('120')
  await page.getByRole('button', { name: 'Save All Changes' }).click()
  
  // Then: 성공 메시지 표시
  await expect(page.getByText('설정이 저장되었습니다')).toBeVisible()
  
  // And: 값이 실제로 변경되었는지 확인
  await page.reload()
  await expect(page.getByLabel('비밀번호 만료 일수')).toHaveValue('120')
})

test('유효하지 않은 값 입력 시 에러', async ({ page }) => {
  await page.goto('/admin/system-settings')
  
  // 범위 벗어난 값 입력
  await page.getByLabel('비밀번호 만료 일수').fill('5000')
  await page.getByRole('button', { name: 'Save All Changes' }).click()
  
  // 에러 메시지 확인
  await expect(page.getByText('최대 10년(3650일) 이하여야 합니다')).toBeVisible()
})
```

---

