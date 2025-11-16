## 🔧 Backend Implementation Guide

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

### 환경 변수 설정 (Environment Variables)

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

### ConfigurationService 상세 설계

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

### Entity 설계

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

### Repository 설계

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

### REST API Controller

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

### Flyway 마이그레이션 스크립트

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



## 🚨 Exception Handling Design Guide

> **📚 완전한 가이드**: 예외 처리에 대한 상세하고 체계적인 내용은 **[Exception Handling 완전 가이드](./exception-handling.md)** 문서를 참조하세요.  
> 해당 문서에는 보안 고려사항, 로깅 전략, 테스트 패턴, 성능 최적화 등 실무 베스트 프랙티스가 포함되어 있습니다.

> **본 섹션은 Java/Spring 예외 처리 설계를 위한 LLM 지시문 템플릿 기반으로 작성되었습니다.**

### 1. 예외 처리 설계 철학

Inspect-Hub 프로젝트는 **엔터프라이즈급 백엔드 애플리케이션**으로, 다음 원칙을 기반으로 예외 처리를 설계합니다:

#### 도메인 정보
- **도메인**: AML 통합 컴플라이언스 모니터링 (사용자 인증/권한, 탐지 엔진, 정책 관리, 보고서 생성)
- **레이어 구성**: Controller → Service → Repository → Domain(Entity, VO)
- **전역 예외 처리**: `@RestControllerAdvice` + `@ExceptionHandler` 기반
- **트랜잭션**: Service 레이어에서 `@Transactional` 사용

---

### 2. 레이어별 책임 분리

#### Controller 레이어
- **책임**: HTTP 요청/응답 처리, 상태 코드 매핑
- **예외 처리**: 
  - 비즈니스 예외는 Service에서 발생한 것을 전역 핸들러로 전파
  - Validation 실패(`@Valid`)는 자동으로 전역 핸들러가 처리
- **금지사항**: Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않음

#### Service 레이어
- **책임**: 유스케이스 단위 비즈니스 로직, 트랜잭션 경계, 도메인 조합
- **예외 처리**:
  - **유스케이스 중단이 필요한 실패**: `BusinessException` 던지기 (트랜잭션 롤백)
  - **정상 분기로 간주되는 실패**: `Result<T>` 또는 `Optional<T>` 반환
  - **외부 API 호출 실패**: Retry/Circuit Breaker와 함께 예외 처리

#### Domain 레이어 (Entity, VO)
- **책임**: 도메인 불변식 유지
- **예외 처리**:
  - 도메인 규칙 위반 시 **즉시 예외 발생** (생성자, 상태 전이 메서드)
  - 항상 유효한 상태만 유지 (null/invalid 상태 최소화)

#### Repository 레이어
- **책임**: 영속화 로직
- **예외 처리**: JPA 예외를 Service로 전파 (변환하지 않음)

---

### 3. 예외 vs 결과 객체 사용 기준

#### 예외로 처리 (BusinessException)
- 트랜잭션 롤백이 필요한 경우
- 유스케이스를 중단시켜야 하는 실패
- 예시:
  - 인증 실패
  - 권한 부족
  - 필수 데이터 누락
  - 도메인 규칙 위반

#### 결과 객체로 처리 (Result/Optional)
- "실패도 정상 분기"로 간주되는 경우
- 트랜잭션 롤백이 불필요한 경우
- 예시:
  - 검색 결과 없음
  - 추천 데이터 스킵
  - 선택적 기능 실패 (degradation)

---

### 4. ErrorCode + BusinessException 설계

#### ErrorCode Enum 구조

```java
package com.inspecthub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 통합 에러 코드 정의
 * 
 * 각 에러 코드는 다음 정보를 포함:
 * - HTTP 상태 코드
 * - 내부 에러 코드 (문자열)
 * - 사용자 메시지
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // ========== Authentication Errors (AUTH_xxx) ==========
    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "사용자를 찾을 수 없습니다"),
    AUTH_002(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 일치하지 않습니다"),
    AUTH_003(HttpStatus.FORBIDDEN, "AUTH_003", "비활성화된 계정입니다"),
    AUTH_004(HttpStatus.LOCKED, "AUTH_004", "계정이 잠금되었습니다"),
    AUTH_005(HttpStatus.UNAUTHORIZED, "AUTH_005", "토큰이 유효하지 않습니다"),
    AUTH_006(HttpStatus.UNAUTHORIZED, "AUTH_006", "토큰이 만료되었습니다"),
    AUTH_007(HttpStatus.FORBIDDEN, "AUTH_007", "계정이 만료되었습니다"),
    AUTH_008(HttpStatus.UNAUTHORIZED, "AUTH_008", "비밀번호가 만료되었습니다"),
    
    // ========== Policy Errors (POLICY_xxx) ==========
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND", "정책을 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "METHOD_NOT_ALLOWED", "허용되지 않은 로그인 방식입니다"),
    INVALID_METHOD(HttpStatus.BAD_REQUEST, "INVALID_METHOD", "유효하지 않은 로그인 방식입니다"),
    EMPTY_METHODS(HttpStatus.BAD_REQUEST, "EMPTY_METHODS", "최소 하나의 로그인 방식이 활성화되어야 합니다"),
    LAST_METHOD_DISABLE(HttpStatus.BAD_REQUEST, "LAST_METHOD_DISABLE", "마지막 로그인 방식은 비활성화할 수 없습니다"),
    
    // ========== Validation Errors ==========
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값 검증 실패"),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "INVALID_JSON", "요청 본문을 읽을 수 없습니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Content-Type이 지원되지 않습니다"),
    
    // ========== Domain Errors ==========
    INVALID_DOMAIN_STATE(HttpStatus.BAD_REQUEST, "INVALID_DOMAIN_STATE", "도메인 상태가 유효하지 않습니다"),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", "유효하지 않은 상태 전이입니다"),
    
    // ========== Server Errors ==========
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", "외부 API 호출 실패"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "데이터베이스 오류가 발생했습니다");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    /**
     * 에러 코드로부터 ErrorCode enum 찾기
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }
}
```

#### BusinessException 구현

```java
package com.inspecthub.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 * 
 * Service 레이어에서 비즈니스 규칙 위반 시 발생
 * GlobalExceptionHandler에서 ErrorCode 기반으로 HTTP 응답 생성
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final String message;
    
    /**
     * 에러 코드와 메시지로 예외 생성
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    /**
     * ErrorCode enum으로 예외 생성 (권장)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    /**
     * ErrorCode enum + 커스텀 메시지로 예외 생성
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode.getCode();
        this.message = customMessage;
    }
}
```

---

### 5. 전역 예외 핸들러 (GlobalExceptionHandler)

**현재 구현**: `backend/common/src/main/java/com/inspecthub/common/exception/GlobalExceptionHandler.java`

#### 처리 범위
1. `BusinessException` → 비즈니스 에러 (에러 코드 기반 HTTP 상태 매핑)
2. `MethodArgumentNotValidException` → Bean Validation 실패 (400)
3. `HttpMediaTypeNotSupportedException` → Content-Type 미지원 (415)
4. `HttpMessageNotReadableException` → JSON 파싱 오류 (400)
5. `Exception` → 예측하지 못한 예외 (500)

#### 개선 방향
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * BusinessException 처리
     * ErrorCode enum 기반으로 HTTP 상태 코드 매핑
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getErrorCode(), e.getMessage());
        
        ErrorCode errorCode = ErrorCode.fromCode(e.getErrorCode());
        
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }
    
    /**
     * Validation 예외 처리 (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : "입력값 검증 실패";
        
        log.warn("Validation exception: {}", message);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }
    
    /**
     * 도메인 예외 처리 (DomainException)
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        log.warn("Domain exception: {}", e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_DOMAIN_STATE", e.getMessage()));
    }
    
    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"));
    }
}
```

---

### 6. Service 레이어 예외 처리 패턴

#### 패턴 1: BusinessException 던지기 (트랜잭션 롤백)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 로그인 인증
     * 실패 시 BusinessException으로 유스케이스 중단
     */
    public TokenResponse authenticate(LoginRequest request) {
        // 1. 사용자 조회 - 없으면 예외
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));
        
        // 2. 비밀번호 검증 - 실패 시 예외
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_002);
        }
        
        // 3. 계정 상태 검증
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }
        
        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.AUTH_004);
        }
        
        // 4. JWT 토큰 생성
        return jwtTokenProvider.generateToken(user);
    }
}
```

#### 패턴 2: Result<T> 반환 (정상 분기)

```java
/**
 * Result 타입 정의
 * 실패도 정상 흐름인 경우 사용
 */
@Getter
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String errorMessage;
    
    private Result(boolean success, T data, String errorCode, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }
    
    public static <T> Result<T> failure(String errorCode, String errorMessage) {
        return new Result<>(false, null, errorCode, errorMessage);
    }
    
    public boolean isFailure() {
        return !success;
    }
}

@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    /**
     * 추천 데이터 조회
     * 결과 없음도 정상 분기로 처리 (트랜잭션 롤백 불필요)
     */
    public Result<List<RecommendationDto>> getRecommendations(String userId) {
        try {
            List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);
            
            if (recommendations.isEmpty()) {
                // 빈 결과도 성공으로 간주
                return Result.success(Collections.emptyList());
            }
            
            List<RecommendationDto> dtos = recommendations.stream()
                    .map(RecommendationDto::from)
                    .collect(Collectors.toList());
            
            return Result.success(dtos);
            
        } catch (Exception e) {
            log.error("Failed to get recommendations", e);
            return Result.failure("RECOMMENDATION_ERROR", "추천 조회 실패");
        }
    }
}
```

#### Controller에서 Result 처리

```java
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Result<List<RecommendationDto>> result = 
                recommendationService.getRecommendations(userDetails.getUsername());
        
        if (result.isFailure()) {
            // Result 실패를 예외로 변환하여 전역 핸들러로 전파
            throw new BusinessException(result.getErrorCode(), result.getErrorMessage());
        }
        
        return ResponseEntity.ok(ApiResponse.success(result.getData()));
    }
}
```

---

### 7. 도메인 규칙 위반 처리

#### DomainException 정의

```java
package com.inspecthub.common.exception;

/**
 * 도메인 불변식 위반 예외
 * 
 * Entity/VO 생성 또는 상태 전이 시 규칙 위반을 즉시 감지
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### 도메인 객체에서 규칙 검증

```java
@Entity
@Table(name = "DETECTION_CASE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetectionCase {
    
    @Id
    private String caseId;
    
    @Enumerated(EnumType.STRING)
    private CaseStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    
    /**
     * 케이스 생성 (Factory Method)
     * 유효성 검증을 생성자에서 수행
     */
    public static DetectionCase create(String caseId, CaseType type) {
        if (caseId == null || caseId.isBlank()) {
            throw new DomainException("케이스 ID는 필수입니다");
        }
        
        DetectionCase detectionCase = new DetectionCase();
        detectionCase.caseId = caseId;
        detectionCase.status = CaseStatus.OPEN;
        detectionCase.createdAt = LocalDateTime.now();
        
        return detectionCase;
    }
    
    /**
     * 케이스 종료
     * 상태 전이 규칙 검증
     */
    public void close(String closureReason) {
        if (this.status == CaseStatus.CLOSED) {
            throw new DomainException("이미 종료된 케이스입니다");
        }
        
        if (closureReason == null || closureReason.isBlank()) {
            throw new DomainException("종료 사유는 필수입니다");
        }
        
        this.status = CaseStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }
    
    /**
     * 케이스 재개
     */
    public void reopen() {
        if (this.status != CaseStatus.CLOSED) {
            throw new DomainException("종료된 케이스만 재개할 수 있습니다");
        }
        
        this.status = CaseStatus.OPEN;
        this.closedAt = null;
    }
}

enum CaseStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED
}
```

#### Value Object 불변성 보장

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    /**
     * Factory Method - 유효성 검증 포함
     */
    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("금액은 0 이상이어야 합니다");
        }
        
        if (currency == null) {
            throw new DomainException("통화는 필수입니다");
        }
        
        return new Money(amount, currency);
    }
    
    /**
     * 불변 연산 - 새 객체 반환
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("통화가 일치하지 않습니다");
        }
        
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

---

### 8. 재시도/회로 차단 패턴과 예외 처리 연계

#### Spring Retry 활용

```java
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    
    private final RestTemplate restTemplate;
    
    /**
     * 외부 API 호출 with Retry
     * 
     * - 최대 3회 재시도
     * - 1초 간격
     * - IOException, TimeoutException만 재시도
     */
    @Retryable(
        value = {IOException.class, TimeoutException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public ExternalApiResponse callExternalApi(String endpoint) {
        try {
            return restTemplate.getForObject(endpoint, ExternalApiResponse.class);
        } catch (RestClientException e) {
            log.error("External API call failed: {}", endpoint, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
    
    /**
     * Retry 실패 시 Fallback
     */
    @Recover
    public ExternalApiResponse recoverFromApiFailure(Exception e, String endpoint) {
        log.error("All retry attempts failed for: {}", endpoint, e);
        
        // Fallback: 기본 응답 반환
        return ExternalApiResponse.empty();
    }
}
```

#### Resilience4j Circuit Breaker 활용

```java
@Service
@RequiredArgsConstructor
public class AdAuthenticationService {
    
    private final LdapTemplate ldapTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    /**
     * AD 인증 with Circuit Breaker
     * 
     * - 실패율 50% 이상 시 Circuit Open
     * - Half-Open 후 성공 시 Circuit Close
     */
    public TokenResponse authenticate(LoginRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("ad-auth");
        
        try {
            return circuitBreaker.executeSupplier(() -> performAdAuthentication(request));
        } catch (CallNotPermittedException e) {
            // Circuit이 Open 상태
            log.error("AD authentication circuit is OPEN");
            throw new BusinessException("AUTH_009", "AD 서버가 일시적으로 사용 불가능합니다");
        }
    }
    
    private TokenResponse performAdAuthentication(LoginRequest request) {
        try {
            // LDAP 인증 수행
            ldapTemplate.authenticate(query()
                    .where("uid").is(request.getEmployeeId()),
                request.getPassword());
            
            return jwtTokenProvider.generateToken(request.getEmployeeId());
            
        } catch (Exception e) {
            log.error("AD authentication failed", e);
            throw new BusinessException(ErrorCode.AUTH_002);
        }
    }
}
```

---

### 9. 예외 처리 체크리스트

#### 코드 작성 시
- [ ] 유스케이스 중단이 필요한가? → `BusinessException` 사용
- [ ] 실패도 정상 분기인가? → `Result<T>` 또는 `Optional<T>` 사용
- [ ] 도메인 규칙 위반인가? → `DomainException` 사용
- [ ] ErrorCode enum에 에러 코드가 정의되어 있는가?
- [ ] 전역 핸들러에서 HTTP 상태 코드가 올바르게 매핑되는가?

#### 코드 리뷰 시
- [ ] Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않는가?
- [ ] Service에서 발생하는 예외가 적절한 타입인가?
- [ ] 도메인 객체가 항상 유효한 상태를 유지하는가?
- [ ] 외부 API 호출에 Retry/Circuit Breaker가 적용되어 있는가?
- [ ] 예외 메시지가 사용자에게 유용한 정보를 제공하는가?

---

### 10. 리팩터링 계획

#### Phase 1: ErrorCode 통합
- [ ] 현재 분산된 에러 코드를 `ErrorCode` enum으로 통합
- [ ] `GlobalExceptionHandler`에서 `ErrorCode` 기반 매핑 적용

#### Phase 2: BusinessException 표준화
- [ ] 모든 Service 레이어에서 `BusinessException` 사용
- [ ] 직접 문자열 에러 코드 사용 제거

#### Phase 3: 도메인 규칙 검증 강화
- [ ] Entity/VO 생성자에서 유효성 검증 추가
- [ ] `DomainException` 도입

#### Phase 4: Result 패턴 도입
- [ ] 선택적 기능에 `Result<T>` 패턴 적용
- [ ] 검색/추천 등 빈 결과 가능한 API에 적용

#### Phase 5: 외부 API 안정성 강화
- [ ] Resilience4j Circuit Breaker 적용
- [ ] Spring Retry + Fallback 패턴 적용

---

**문서 작성**: 2025-11-16  
**최종 업데이트**: 2025-11-16  
**작성자**: Product Manager Agent (John)
