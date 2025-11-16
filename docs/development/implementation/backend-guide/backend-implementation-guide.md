# 🔧 Backend Implementation Guide

> **⚠️ Java 코드 Import 규칙**
>
> 본 문서의 모든 Java 코드 예시는 가독성을 위해 import 구문을 생략하거나 축약했습니다.  
> 실제 사용 시 다음 규칙을 준수하여 필요한 import 구문을 추가하세요:
>
> - ✅ 모든 클래스는 `import` 구문 사용
> - ❌ 코드 내에서 패키지 전체 경로(`com.xxx.Yyy`) 직접 작성 금지
> - ✅ 충돌이 없는 경우 항상 간단 클래스명만 사용
>
> 자세한 내용은 **[Development Workflow](../WORKFLOW.md#코딩-스타일--명명-규칙)** 참조

---

## 환경 변수 설정 (Environment Variables)

**필수 환경 변수:**
```bash
# JWT 서명 비밀키 (최소 256비트 = 32자)
export JWT_SECRET_KEY="your-secret-key-min-32-characters-long-here-change-this"

# AD (Active Directory) LDAP Bind 비밀번호
export AD_BIND_PASSWORD="your-ad-admin-password"

# SSO OAuth2 Client Secret
export SSO_CLIENT_SECRET="your-oauth2-client-secret"
```

**환경 변수 검증 (부팅 시):**
```java
@Component
@RequiredArgsConstructor
public class EnvironmentValidator implements ApplicationRunner {
    
    private final Environment environment;
    
    private static final List<String> REQUIRED_ENV_VARS = List.of(
        "JWT_SECRET_KEY",
        "AD_BIND_PASSWORD",  // AD 활성화 시 필수
        "SSO_CLIENT_SECRET"  // SSO 활성화 시 필수
    );
    
    @Override
    public void run(ApplicationArguments args) {
        List<String> missingVars = new ArrayList<>();
        
        for (String varName : REQUIRED_ENV_VARS) {
            String value = environment.getProperty(varName);
            if (value == null || value.isBlank()) {
                missingVars.add(varName);
            }
        }
        
        if (!missingVars.isEmpty()) {
            String errorMsg = String.format(
                "Missing required environment variables: %s. " +
                "Please set them before starting the application.",
                String.join(", ", missingVars)
            );
            throw new IllegalStateException(errorMsg);
        }
        
        // JWT 키 길이 검증 (최소 256비트 = 32자)
        String jwtSecret = environment.getProperty("JWT_SECRET_KEY");
        if (jwtSecret != null && jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY must be at least 32 characters (256 bits)"
            );
        }
    }
}
```

**Docker Compose 예시:**
```yaml
version: '3.8'
services:
  backend:
    image: inspect-hub-backend:latest
    environment:
      - JWT_SECRET_KEY=${JWT_SECRET_KEY}
      - AD_BIND_PASSWORD=${AD_BIND_PASSWORD}
      - SSO_CLIENT_SECRET=${SSO_CLIENT_SECRET}
    env_file:
      - .env.production  # 또는 .env.local
```

**.env.example (버전 관리 가능):**
```bash
# JWT Secret (CHANGE THIS IN PRODUCTION!)
JWT_SECRET_KEY=change-this-to-random-32-char-string

# AD Credentials
AD_BIND_PASSWORD=your-ad-password-here

# SSO Credentials  
SSO_CLIENT_SECRET=your-sso-client-secret-here
```

**.env.production (Git 제외, 실제 값):**
```bash
JWT_SECRET_KEY=a1b2c3d4...실제32자이상비밀키
AD_BIND_PASSWORD=실제AD비밀번호
SSO_CLIENT_SECRET=실제SSO시크릿
```

**.gitignore 추가:**
```
.env
.env.local
.env.production
.env.*.local
```

**Kubernetes Secret 예시:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: inspect-hub-secrets
type: Opaque
stringData:
  JWT_SECRET_KEY: "your-actual-secret-key-here"
  AD_BIND_PASSWORD: "your-ad-password"
  SSO_CLIENT_SECRET: "your-sso-secret"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inspect-hub-backend
spec:
  template:
    spec:
      containers:
      - name: backend
        envFrom:
        - secretRef:
            name: inspect-hub-secrets
```

## ConfigurationService 상세 설계

**인터페이스:**
```java
public interface ConfigurationService {
    // 기본 조회 (우선순위 로직 적용)
    String getConfig(String key);
    String getConfig(String key, String defaultValue);
    
    // 타입 변환 조회
    Boolean getConfigAsBoolean(String key);
    Boolean getConfigAsBoolean(String key, Boolean defaultValue);
    Integer getConfigAsInt(String key);
    Integer getConfigAsInt(String key, Integer defaultValue);
    List<String> getConfigAsList(String key);
    <T> T getConfigAsJson(String key, Class<T> type);
    
    // 설정 수정 (DB만 가능, Properties는 읽기 전용)
    void updateConfig(String key, String value, String adminId);
    void updateConfig(String key, String value, String adminId, String changeReason);
    void deleteConfig(String key, String adminId); // Properties로 Fallback
    
    // 카테고리별 조회
    Map<String, ConfigItem> getAllConfigs();
    Map<String, ConfigItem> getConfigsByCategory(String category);
    
    // 캐시 관리
    void reloadCache();
    void invalidateCache(String key);
    
    // 설정 검증
    void validateConfig(String key, String value) throws ConfigValidationException;
}
```

**구현 클래스:**
```java
@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {
    
    private final SystemConfigRepository systemConfigRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Environment environment; // Properties 조회용
    
    private static final String CACHE_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    @Override
    public String getConfig(String key) {
        // 1. Redis 캐시 조회
        String cachedValue = redisTemplate.opsForValue().get(CACHE_PREFIX + key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 2. DB 조회
        Optional<SystemConfig> dbConfig = systemConfigRepository.findById(key);
        if (dbConfig.isPresent() && StringUtils.hasText(dbConfig.get().getConfigValue())) {
            String value = dbConfig.get().getConfigValue();
            cacheValue(key, value);
            return value;
        }
        
        // 3. Properties Fallback
        String propertyValue = environment.getProperty(key);
        if (propertyValue != null) {
            cacheValue(key, propertyValue);
            return propertyValue;
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public void updateConfig(String key, String value, String adminId) {
        updateConfig(key, value, adminId, null);
    }
    
    @Override
    @Transactional
    public void updateConfig(String key, String value, String adminId, String changeReason) {
        // 설정 검증
        validateConfig(key, value);
        
        // 기존 값 조회 (감사 로그용)
        String oldValue = getConfig(key);
        
        // DB 저장
        SystemConfig config = systemConfigRepository.findById(key)
            .orElse(new SystemConfig(key));
        config.setConfigValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(adminId);
        config.incrementVersion(); // Optimistic Lock
        systemConfigRepository.save(config);
        
        // 감사 로그 기록
        saveChangeHistory(key, oldValue, value, adminId, changeReason);
        
        // 캐시 무효화
        invalidateCache(key);
    }
    
    private void cacheValue(String key, String value) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + key, value, CACHE_TTL);
    }
    
    private void saveChangeHistory(String key, String oldValue, String newValue, 
                                   String changedBy, String reason) {
        ConfigChangeHistory history = ConfigChangeHistory.builder()
            .configKey(key)
            .oldValue(oldValue)
            .newValue(newValue)
            .changedBy(changedBy)
            .changedAt(LocalDateTime.now())
            .changeReason(reason)
            .build();
        configChangeHistoryRepository.save(history);
    }
}
```

## Entity 설계

**SystemConfig Entity:**
```java
@Entity
@Table(name = "SYSTEM_CONFIG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {
    
    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 20)
    private ValueType valueType;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "editable")
    private Boolean editable = true;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @Version
    @Column(name = "version")
    private Integer version = 0;
    
    public SystemConfig(String configKey) {
        this.configKey = configKey;
    }
    
    public void incrementVersion() {
        this.version = (this.version == null ? 0 : this.version) + 1;
    }
}

enum ValueType {
    BOOLEAN,
    INT,
    STRING,
    JSON,
    LIST
}
```

**ConfigChangeHistory Entity:**
```java
@Entity
@Table(name = "CONFIG_CHANGE_HISTORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigChangeHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", length = 100, nullable = false)
    private String configKey;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    @Column(name = "changed_by", length = 50, nullable = false)
    private String changedBy;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "change_reason", length = 500)
    private String changeReason;
    
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
```

## Repository 설계

**SystemConfigRepository:**
```java
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    
    List<SystemConfig> findByCategory(String category);
    
    List<SystemConfig> findByEditableTrue();
    
    @Query("SELECT c FROM SystemConfig c WHERE c.configKey LIKE :pattern")
    List<SystemConfig> findByKeyPattern(@Param("pattern") String pattern);
}
```

**ConfigChangeHistoryRepository:**
```java
public interface ConfigChangeHistoryRepository extends JpaRepository<ConfigChangeHistory, Long> {
    
    List<ConfigChangeHistory> findByConfigKeyOrderByChangedAtDesc(String configKey);
    
    List<ConfigChangeHistory> findByChangedByOrderByChangedAtDesc(String changedBy);
    
    @Query("SELECT h FROM ConfigChangeHistory h WHERE h.changedAt >= :since ORDER BY h.changedAt DESC")
    List<ConfigChangeHistory> findRecentChanges(@Param("since") LocalDateTime since);
}
```

## REST API Controller

**SystemConfigController:**
```java
@RestController
@RequestMapping("/api/v1/admin/system-config")
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "시스템 설정 관리 API")
public class SystemConfigController {
    
    private final ConfigurationService configurationService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 설정 조회", description = "카테고리별 전체 설정 조회")
    public ResponseEntity<ApiResponse<Map<String, List<ConfigItemDto>>>> getAllConfigs() {
        Map<String, ConfigItem> configs = configurationService.getAllConfigs();
        Map<String, List<ConfigItemDto>> groupedConfigs = groupByCategory(configs);
        return ResponseEntity.ok(ApiResponse.success(groupedConfigs));
    }
    
    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "개별 설정 조회")
    public ResponseEntity<ApiResponse<ConfigItemDto>> getConfig(@PathVariable String key) {
        String value = configurationService.getConfig(key);
        ConfigItemDto dto = ConfigItemDto.builder()
            .key(key)
            .value(value)
            .build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
    
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "개별 설정 수정")
    public ResponseEntity<ApiResponse<Void>> updateConfig(
            @PathVariable String key,
            @RequestBody @Valid UpdateConfigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        configurationService.updateConfig(key, request.getValue(), 
                                         userDetails.getUsername(), 
                                         request.getChangeReason());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "개별 설정 삭제", description = "Properties 기본값으로 Fallback")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(
            @PathVariable String key,
            @AuthenticationPrincipal UserDetails userDetails) {
        configurationService.deleteConfig(key, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/reload-cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "캐시 재로드")
    public ResponseEntity<ApiResponse<Void>> reloadCache() {
        configurationService.reloadCache();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "변경 이력 조회")
    public ResponseEntity<ApiResponse<List<ConfigChangeHistoryDto>>> getChangeHistory(
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<ConfigChangeHistory> history = configChangeHistoryService.getHistory(configKey, since);
        List<ConfigChangeHistoryDto> dtos = history.stream()
            .map(ConfigChangeHistoryDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
```

## Flyway 마이그레이션 스크립트

**V1__create_system_config_tables.sql:**
```sql
-- SYSTEM_CONFIG 테이블
CREATE TABLE SYSTEM_CONFIG (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    value_type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    editable BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    version INT DEFAULT 0
);

-- 인덱스
CREATE INDEX idx_system_config_category ON SYSTEM_CONFIG(category);
CREATE INDEX idx_system_config_editable ON SYSTEM_CONFIG(editable);

-- CONFIG_CHANGE_HISTORY 테이블
CREATE TABLE CONFIG_CHANGE_HISTORY (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    change_reason VARCHAR(500)
);

-- 인덱스
CREATE INDEX idx_config_history_key ON CONFIG_CHANGE_HISTORY(config_key);
CREATE INDEX idx_config_history_changed_by ON CONFIG_CHANGE_HISTORY(changed_by);
CREATE INDEX idx_config_history_changed_at ON CONFIG_CHANGE_HISTORY(changed_at DESC);

-- 코멘트
COMMENT ON TABLE SYSTEM_CONFIG IS '시스템 전역 설정 (Global Configuration)';
COMMENT ON COLUMN SYSTEM_CONFIG.config_key IS '설정 키 (점 구분 계층 구조)';
COMMENT ON COLUMN SYSTEM_CONFIG.config_value IS '설정 값 (문자열 또는 JSON)';
COMMENT ON COLUMN SYSTEM_CONFIG.value_type IS '값 타입 (BOOLEAN, INT, STRING, JSON, LIST)';
COMMENT ON COLUMN SYSTEM_CONFIG.category IS '설정 카테고리 (LOGIN, PASSWORD, SESSION, IP, ACCOUNT, ADVANCED)';
COMMENT ON COLUMN SYSTEM_CONFIG.editable IS 'UI에서 수정 가능 여부';
COMMENT ON COLUMN SYSTEM_CONFIG.version IS '낙관적 락 (Optimistic Lock)';

COMMENT ON TABLE CONFIG_CHANGE_HISTORY IS '설정 변경 이력 (Audit Log)';
```

**V2__migrate_to_global_config.sql:**
```sql
-- 기존 SecurityPolicy 테이블에서 글로벌 정책 추출 (orgId = NULL)
-- 예시: PASSWORD_EXPIRATION 정책
INSERT INTO SYSTEM_CONFIG (config_key, config_value, value_type, description, category, editable)
SELECT 
    'auth.password.expiration.enabled',
    CAST(enabled AS TEXT),
    'BOOLEAN',
    '비밀번호 만료 정책 활성화',
    'PASSWORD',
    TRUE
FROM SECURITY_POLICY
WHERE policy_type = 'PASSWORD_EXPIRATION' AND org_id IS NULL
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO SYSTEM_CONFIG (config_key, config_value, value_type, description, category, editable)
SELECT 
    'auth.password.expiration.days',
    config::jsonb->>'expirationDays',
    'INT',
    '비밀번호 만료 일수',
    'PASSWORD',
    TRUE
FROM SECURITY_POLICY
WHERE policy_type = 'PASSWORD_EXPIRATION' AND org_id IS NULL
ON CONFLICT (config_key) DO NOTHING;

-- 기타 정책들도 동일한 패턴으로 변환...

-- 조직별 정책 아카이브 (삭제하지 않고 백업)
-- ALTER TABLE SECURITY_POLICY RENAME TO SECURITY_POLICY_ARCHIVED;
```

**V3__insert_default_configs.sql:**
```sql
-- 기본 설정 값 삽입 (Properties에 없는 경우 대비)
INSERT INTO SYSTEM_CONFIG (config_key, config_value, value_type, description, category, editable)
VALUES
-- 로그인 방식
('auth.login.local.enabled', 'true', 'BOOLEAN', 'LOCAL 로그인 활성화', 'LOGIN', true),
('auth.login.ad.enabled', 'false', 'BOOLEAN', 'AD 로그인 활성화', 'LOGIN', true),
('auth.login.sso.enabled', 'false', 'BOOLEAN', 'SSO 로그인 활성화', 'LOGIN', true),
('auth.login.priority', 'SSO,AD,LOCAL', 'LIST', '로그인 우선순위', 'LOGIN', true),

-- 세션 정책
('auth.session.accessToken.expirationSeconds', '3600', 'INT', 'Access Token 만료시간 (초)', 'SESSION', true),
('auth.session.refreshToken.expirationSeconds', '86400', 'INT', 'Refresh Token 만료시간 (초)', 'SESSION', true),
('auth.session.maxConcurrentSessions', '10', 'INT', '최대 동시 세션 수', 'SESSION', true),
('auth.session.idleTimeoutMinutes', '30', 'INT', 'Idle Timeout (분)', 'SESSION', true),
('auth.session.enforceSingleDevice', 'false', 'BOOLEAN', 'Single Device 모드', 'SESSION', true),
('auth.session.requireReauthOnIpChange', 'false', 'BOOLEAN', 'IP 변경 시 재인증', 'SESSION', true)
ON CONFLICT (config_key) DO NOTHING;
```

---


