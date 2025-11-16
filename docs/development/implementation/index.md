# Implementation Guides

> **구현에 필요한 상세 가이드 모음**

---

## 📖 개요

Inspect-Hub 프로젝트의 **Backend/Frontend 구현 가이드**, **체크리스트**, **추가 고려사항**을 포함합니다.

**상위 문서:** [Development Guide](../index.md)

---

## 📚 문서 목록

### 🔧 Backend Implementation Guide
**📄 [backend-guide.md](./backend-guide.md)** (568줄)

**주요 내용:**

#### 1. 환경 변수 설정
```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# Database
DB_URL=jdbc:postgresql://localhost:5432/inspecthub
DB_USERNAME=inspecthub_user
DB_PASSWORD=******

# JWT
JWT_SECRET=base64-encoded-secret-key
JWT_ACCESS_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=604800

# External Auth
AD_SERVER_URL=ldap://ad.example.com:389
SSO_CLIENT_ID=inspecthub-client
SSO_CLIENT_SECRET=******
```

#### 2. ConfigurationService 상세 설계
- DB + Properties 이중 소스 지원
- 계층적 조회: 조직별 → 글로벌
- 실시간 캐싱 (Caffeine + Redis)
- Flyway 마이그레이션

#### 3. Entity/Repository/API 설계
- MyBatis Mapper 패턴
- RESTful API 설계
- Swagger 자동 문서화

---

### 🚨 Exception Handling Guide
**📄 [exception-handling.md](./exception-handling.md)**

**주요 내용:**

#### 1. 예외 처리 설계 철학
- 엔터프라이즈급 백엔드 애플리케이션 예외 처리 원칙
- 레이어별 책임 분리 (Controller, Service, Domain, Repository)
- 예외 vs 결과 객체 사용 기준

#### 2. ErrorCode + BusinessException 설계
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "사용자를 찾을 수 없습니다"),
    // ... 카테고리별 에러 코드
}

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String message;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
```

#### 3. 전역 예외 핸들러
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = ErrorCode.fromCode(e.getErrorCode());
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }
}
```

#### 4. Service 레이어 예외 처리 패턴
- **BusinessException 던지기**: 트랜잭션 롤백이 필요한 경우
- **Result<T> 반환**: 실패도 정상 분기로 간주되는 경우
- **도메인 규칙 위반**: DomainException 사용

#### 5. 재시도/회로 차단 패턴
- Spring Retry with @Retryable
- Resilience4j Circuit Breaker
- Fallback 전략

#### 6. 보안 고려사항
- Stack trace 노출 방지 (환경별 응답 분기)
- 민감 정보 마스킹
- SQL Injection 방지

#### 7. 로깅 전략
- MDC (Mapped Diagnostic Context) 활용
- Trace ID 기반 분산 추적
- 구조화된 JSON 로깅

#### 8. 테스트 전략
- MockMvc를 사용한 예외 핸들러 테스트
- BDD 스타일 테스트 작성 (Given-When-Then)

#### 9. 예외 처리 안티패턴
- Exception swallowing
- 과도한 try-catch 사용
- 제네릭 Exception 사용
- 흐름 제어용 예외 사용

#### 10. 성능 고려사항
- 예외 생성 비용 최소화
- Stack trace 비활성화 (고성능 시나리오)
- 지연 로깅 (Lazy evaluation)

---

### 🎨 Frontend UI Design
**📄 [frontend-guide.md](./frontend-guide.md)** (298줄)

**주요 내용:**

#### 1. System Configuration UI
- 시스템 설정 화면 구조
- 탭 기반 네비게이션
- 실시간 유효성 검증

#### 2. 컴포넌트 구조 (FSD + Atomic Design)
```
app/features/config/
├── ui/
│   ├── ConfigPage.vue (Page)
│   ├── ConfigForm.vue (Organism)
│   ├── ConfigField.vue (Molecule)
│   └── FieldInput.vue (Atom)
├── model/
│   └── useConfigStore.ts (Pinia)
└── api/
    └── configService.ts
```

#### 3. Pinia Store
```typescript
export const useConfigStore = defineStore('config', () => {
  const configs = ref<Configuration[]>([])

  async function loadConfigs() {
    configs.value = await configApi.getAll()
  }

  async function updateConfig(key: string, value: any) {
    await configApi.update(key, value)
    await loadConfigs()
  }

  return { configs, loadConfigs, updateConfig }
})
```

#### 4. UI/UX 요구사항
- 반응형 레이아웃 (데스크톱 중심)
- Accessibility (WCAG 2.1 Level AA)
- 다국어 지원 (i18n)

---

### ✅ Implementation Checklist
**📄 [checklist.md](./checklist.md)** (49줄)

**Backend 체크리스트:**
- [ ] Entity 및 Value Object 구현
- [ ] Repository 인터페이스 및 MyBatis Mapper 구현
- [ ] Application Service 및 Domain Service 구현
- [ ] REST Controller 및 DTO 구현
- [ ] Flyway 마이그레이션 스크립트 작성
- [ ] Unit/Integration 테스트 작성 (커버리지 목표 달성)
- [ ] Swagger API 문서화 (`@Tag`, `@Operation`)
- [ ] 보안 설정 (JWT, RBAC)
- [ ] Audit Logging 추가
- [ ] 에러 처리 및 GlobalExceptionHandler 통합

**Frontend 체크리스트:**
- [ ] Atomic 컴포넌트 구현 (Atoms, Molecules)
- [ ] Organism 컴포넌트 구현 (Form, List, Detail)
- [ ] Page 컴포넌트 구현 (ConfigPage)
- [ ] Pinia Store 구현 (State Management)
- [ ] API Service 구현 (axios)
- [ ] 유효성 검증 로직 (VeeValidate + Zod)
- [ ] E2E 테스트 작성 (Playwright)
- [ ] 다국어 리소스 추가 (i18n)
- [ ] 접근성 검증 (WCAG)
- [ ] 브라우저 호환성 테스트

**Documentation 체크리스트:**
- [ ] README.md 업데이트
- [ ] API 문서 업데이트 (Swagger)
- [ ] ERD 다이어그램 업데이트
- [ ] 변경 이력 기록 (CHANGELOG.md)

---

### 🔍 Additional Considerations
**📄 [considerations.md](./considerations.md)** (502줄)

**주요 고려사항:**

#### 1. 설정 값 검증 및 타입 안전성
```java
@Component
@Validated
public class ConfigValidator {
    public void validate(String key, Object value) {
        ConfigDefinition def = getDefinition(key);
        if (!def.getType().isInstance(value)) {
            throw new IllegalArgumentException(
                "Type mismatch: " + key
            );
        }
        // 범위, 정규식, enum 검증
    }
}
```

#### 2. 에러 처리 전략
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
        BusinessException ex
    ) {
        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage()
            ));
    }
}
```

#### 3. 성능 모니터링
- Micrometer + Prometheus
- Custom Metrics (설정 조회 시간, 캐시 Hit Rate)
- APM 연동 (Datadog, New Relic)

#### 4. 운영 가이드라인
- Blue-Green 배포 전략
- 설정 변경 시 롤백 계획
- 장애 대응 매뉴얼

#### 5. 보안 강화 방안
- 민감 정보 필드 레벨 암호화
- 설정 변경 Audit Trail
- RBAC 기반 접근 제어

#### 6. 테스트 전략
- Unit Tests: JUnit 5 + Mockito
- Integration Tests: @SpringBootTest + Testcontainers
- E2E Tests: Playwright
- Performance Tests: JMeter, Gatling

---

## 🚀 빠른 시작

### Backend 구현 순서

1. **Entity 및 Value Object 구현**
   ```java
   @Entity
   @Table(name = "configurations")
   public class Configuration {
       @Id private String id;
       private String key;
       private String value;
       private ConfigType type;
       // ...
   }
   ```

2. **Repository 및 MyBatis Mapper**
   ```xml
   <mapper namespace="com.inspecthub.config.repository.ConfigurationMapper">
       <select id="findByKey" resultType="Configuration">
           SELECT * FROM configurations WHERE key = #{key}
       </select>
   </mapper>
   ```

3. **Application Service**
   ```java
   @Service
   @RequiredArgsConstructor
   public class ConfigService {
       private final ConfigurationRepository repository;

       public Configuration getConfig(String key) {
           return repository.findByKey(key)
               .orElseThrow(() -> new NotFoundException(key));
       }
   }
   ```

4. **REST Controller**
   ```java
   @RestController
   @RequestMapping("/api/v1/configs")
   @Tag(name = "Configuration", description = "설정 관리 API")
   public class ConfigController {
       @GetMapping("/{key}")
       @Operation(summary = "설정 조회")
       public ApiResponse<ConfigDTO> getConfig(
           @PathVariable String key
       ) {
           return ApiResponse.success(
               configService.getConfig(key)
           );
       }
   }
   ```

### Frontend 구현 순서

1. **Pinia Store**
2. **API Service**
3. **Atomic 컴포넌트**
4. **Organism 컴포넌트**
5. **Page 컴포넌트**
6. **라우팅 설정**
7. **E2E 테스트**

---

## 🔗 관련 문서

- **[Backend README](../../backend/README.md)**
- **[Frontend README](../../frontend/README.md)**
- **[API Contract](../../api/CONTRACT.md)**
- **[DDD Design](../../architecture/DDD_DESIGN.md)**
- **[Test Plan](../plan.md)**
