# Java/Spring 예외 처리 설계 가이드

> **Inspect-Hub AML 통합 컴플라이언스 시스템을 위한 예외 처리 베스트 프랙티스**

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

## 📚 목차

1. [개요](#-개요)
2. [역할 및 프로젝트 컨텍스트](#-역할-및-프로젝트-컨텍스트)
3. [공통 설계 원칙](#-공통-설계-원칙)
4. [레이어별 예외 처리 정책 표](#-레이어별-예외-처리-정책-표)
5. [ErrorCode + BusinessException 설계](#-errorcode--businessexception-설계)
6. [Service 레이어 예외 처리 패턴](#-service-레이어-예외-처리-패턴)
7. [도메인 규칙 위반 처리](#-도메인-규칙-위반-처리)
8. [재시도/회로 차단 패턴](#-재시도회로-차단-패턴)
9. [보안 고려사항](#-보안-고려사항)
10. [로깅 전략](#-로깅-전략)
11. [테스트 전략](#-테스트-전략)
12. [@RestControllerAdvice 고급 기능](#-restcontrolleradvice-고급-기능)
13. [예외 처리 안티패턴](#-예외-처리-안티패턴)
14. [성능 고려사항](#-성능-고려사항)
15. [예외 처리 체크리스트](#-예외-처리-체크리스트)
16. [리팩터링 계획](#-리팩터링-계획)

---

## 🎯 개요

본 문서는 **Inspect-Hub 백엔드 애플리케이션의 예외 처리 전략과 코드 구조**를 정의합니다.

**목적:**
- 일관된 예외 처리 패턴 제공
- 레이어별 책임 분리
- 유지보수성 및 디버깅 효율성 향상
- SOLID 원칙 및 함수형 스타일 적용

**대상 독자:**
- Backend 개발자
- Code Reviewer
- Tech Lead

---

## 👤 역할 및 프로젝트 컨텍스트

### 역할 (Role)

당신은 **Java 21 + Spring Boot 기반의 엔터프라이즈 백엔드 애플리케이션**을 설계·구현하는 시니어 백엔드 개발자입니다.

### 프로젝트 컨텍스트

**도메인:**
- AML(Anti-Money Laundering) 통합 컴플라이언스 모니터링
- 핵심 기능: STR/CTR/WLF 탐지, 정책 관리, 사례 조사, FIU 보고

**레이어 구성:**
```
Controller (HTTP Layer)
    ↓
Service (Business Logic Layer)
    ↓
Repository (Data Access Layer)
    ↓
Domain (Entity, VO, DDD Layer)
```

**전역 예외 처리:**
- `@RestControllerAdvice` + `@ExceptionHandler` 기반
- 위치: `backend/common/src/main/java/com/inspecthub/common/exception/GlobalExceptionHandler.java`

**트랜잭션:**
- Service 레이어에서 `@Transactional` 사용

---

## 📜 공통 설계 원칙

### 1. 레이어별 책임 분리

| 레이어 | 책임 | 예외 처리 원칙 |
|--------|------|----------------|
| **Controller** | HTTP 요청/응답, 상태 코드 매핑 | 비즈니스 예외는 Service로 위임, 전역 핸들러로 전파 |
| **Service** | 유스케이스 비즈니스 로직, 트랜잭션 경계 | 예외 vs Result 패턴 선택, 도메인 조합 |
| **Domain** | 도메인 불변식 유지 | 생성/상태 전이 시 규칙 위반 즉시 예외 발생 |
| **Repository** | 영속화 로직 | JPA 예외를 Service로 전파 (변환 없음) |

### 2. 예외 vs 결과 객체 사용 기준

#### 예외로 처리 (BusinessException)
- ✅ 유스케이스를 **중단**시키고 **롤백**되는 것이 자연스러운 실패
- ✅ 예시:
  - 인증 실패
  - 권한 부족
  - 필수 데이터 누락
  - 도메인 규칙 위반

#### 결과 객체로 처리 (Result/Optional)
- ✅ 비즈니스 상 **"실패도 정상 분기"**로 간주되는 경우
- ✅ 예시:
  - 검색 결과 없음
  - 추천 스킵
  - 선택적 기능 실패 (degradation)

### 3. 전역 예외 처리 전략

**ErrorCode + BusinessException 조합:**
- `ErrorCode` enum: HTTP 상태, 내부 코드, 메시지 포함
- `BusinessException`: 비즈니스 에러
- `MethodArgumentNotValidException`: Bean Validation 실패 → 400
- `Exception`: 예측하지 못한 예외 → 500

### 4. 도메인 규칙 처리

- **도메인 불변식 위반** (음수 가격, 잘못된 상태 전이 등)은 도메인 계층에서 예외로 **즉시 발생**
- **도메인 객체는 항상 유효한 상태만 유지** (null/invalid 상태 최소화)

### 5. 함수형/불변/클린 코드 지향

- ✅ 불변 객체 사용 (DTO, Value Object)
- ✅ Stream, Optional 등 함수형 스타일 활용
- ✅ Early return으로 if/else 중첩 최소화

---

## 📊 레이어별 예외 처리 정책 표

> **핵심 섹션**: 각 레이어에서 어떤 에러를 어떻게 처리할지 명확히 정의

### Controller 레이어

| 에러 유형 | 처리 방식 | try/catch 사용 여부 | 예시 |
|-----------|-----------|---------------------|------|
| **비즈니스 예외** | Service로 위임 → 전역 핸들러로 전파 | ❌ 사용 금지 | `UserNotFoundException`, `InvalidPasswordException` |
| **Validation 실패** | `@Valid` 자동 처리 → 전역 핸들러 | ❌ 사용 금지 | `MethodArgumentNotValidException` |
| **HTTP 파싱 오류** | 전역 핸들러 처리 | ❌ 사용 금지 | `HttpMessageNotReadableException` |
| **인증/인가 실패** | Spring Security 필터 → 전역 핸들러 | ❌ 사용 금지 | `AccessDeniedException` |

**원칙:**
- ❌ Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않음
- ✅ 모든 예외는 전역 핸들러(`GlobalExceptionHandler`)로 전파
- ✅ HTTP 상태 코드 매핑은 전역 핸들러가 담당

**Good Example:**
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    // ✅ Service에서 발생한 예외는 전역 핸들러로 자동 전파
    TokenResponse token = authService.authenticate(request);
    return ResponseEntity.ok(ApiResponse.success(token));
}
```

**Bad Example:**
```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    try {
        TokenResponse token = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(token));
    } catch (BusinessException e) {
        // ❌ Controller에서 직접 예외 처리 금지
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }
}
```

---

### Service 레이어

| 에러 유형 | 처리 방식 | try/catch 사용 여부 | 예시 |
|-----------|-----------|---------------------|------|
| **유스케이스 중단 필요** | `BusinessException` 던지기 (트랜잭션 롤백) | ❌ 발생만, catch 안 함 | 인증 실패, 권한 부족, 필수 데이터 누락 |
| **정상 분기 실패** | `Result<T>` 또는 `Optional<T>` 반환 | ✅ 사용 (내부 처리) | 검색 결과 없음, 추천 스킵 |
| **외부 API 호출 실패** | Retry/Circuit Breaker + 예외 변환 | ✅ 사용 (복구 시도) | AD 인증 실패, SSO 호출 타임아웃 |
| **데이터베이스 예외** | JPA 예외 → `BusinessException` 변환 | ✅ 사용 (선택적) | `DataIntegrityViolationException` → `DUPLICATE_KEY_ERROR` |
| **도메인 규칙 위반** | `DomainException` 발생 → Service에서 catch 또는 전파 | ✅ 사용 (선택적) | 상태 전이 불가, 음수 금액 |

**원칙:**
- ✅ 유스케이스 중단이 필요하면 `BusinessException` 던지기
- ✅ 실패도 정상 흐름이면 `Result<T>` 반환
- ✅ 외부 시스템 실패는 Retry + Fallback 고려
- ✅ 트랜잭션 롤백 여부를 기준으로 예외 vs Result 선택

**Good Example (BusinessException):**
```java
@Transactional
public TokenResponse authenticate(LoginRequest request) {
    User user = userRepository.findByEmployeeId(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001)); // ✅ 유스케이스 중단
    
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BusinessException(ErrorCode.AUTH_002); // ✅ 트랜잭션 롤백 필요
    }
    
    return jwtTokenProvider.generateToken(user);
}
```

**Good Example (Result<T>):**
```java
public Result<List<RecommendationDto>> getRecommendations(String userId) {
    try {
        List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);
        
        if (recommendations.isEmpty()) {
            return Result.success(Collections.emptyList()); // ✅ 빈 결과도 성공
        }
        
        return Result.success(toDto(recommendations));
        
    } catch (Exception e) {
        log.error("Failed to get recommendations", e);
        return Result.failure("RECOMMENDATION_ERROR", "추천 조회 실패"); // ✅ 실패도 정상 분기
    }
}
```

---

### Domain 레이어 (Entity, VO)

| 에러 유형 | 처리 방식 | try/catch 사용 여부 | 예시 |
|-----------|-----------|---------------------|------|
| **도메인 불변식 위반** | `DomainException` 즉시 발생 | ❌ 발생만 | 음수 금액, null 필수 필드, 잘못된 상태 전이 |
| **유효성 검증 실패** | `DomainException` 즉시 발생 | ❌ 발생만 | 생성자 파라미터 검증, setter 규칙 검증 |
| **상태 전이 불가** | `DomainException` 즉시 발생 | ❌ 발생만 | `CLOSED` 케이스 재종료 시도 |

**원칙:**
- ✅ **항상 유효한 상태만 유지** (null/invalid 상태 허용 안 함)
- ✅ **생성자/Factory Method에서 유효성 검증**
- ✅ **상태 전이 메서드에서 규칙 검증**
- ❌ try-catch 사용 금지 (예외는 Service로 전파)

**Good Example (Entity):**
```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetectionCase {
    
    @Id
    private String caseId;
    
    @Enumerated(EnumType.STRING)
    private CaseStatus status;
    
    /**
     * Factory Method - 유효성 검증 포함
     */
    public static DetectionCase create(String caseId, CaseType type) {
        if (caseId == null || caseId.isBlank()) {
            throw new DomainException("케이스 ID는 필수입니다"); // ✅ 즉시 예외 발생
        }
        
        DetectionCase detectionCase = new DetectionCase();
        detectionCase.caseId = caseId;
        detectionCase.status = CaseStatus.OPEN;
        detectionCase.createdAt = LocalDateTime.now();
        
        return detectionCase;
    }
    
    /**
     * 상태 전이 - 규칙 검증
     */
    public void close(String closureReason) {
        if (this.status == CaseStatus.CLOSED) {
            throw new DomainException("이미 종료된 케이스입니다"); // ✅ 상태 전이 불가
        }
        
        if (closureReason == null || closureReason.isBlank()) {
            throw new DomainException("종료 사유는 필수입니다"); // ✅ 필수 입력 검증
        }
        
        this.status = CaseStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }
}
```

**Good Example (Value Object):**
```java
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    /**
     * Factory Method - 유효성 검증
     */
    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("금액은 0 이상이어야 합니다"); // ✅ 도메인 규칙 검증
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
            throw new DomainException("통화가 일치하지 않습니다"); // ✅ 연산 규칙 검증
        }
        
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

---

### Repository 레이어

| 에러 유형 | 처리 방식 | try/catch 사용 여부 | 예시 |
|-----------|-----------|---------------------|------|
| **JPA 예외** | Service로 전파 (변환 없음) | ❌ 사용 금지 | `DataIntegrityViolationException`, `OptimisticLockException` |
| **MyBatis 예외** | Service로 전파 (변환 없음) | ❌ 사용 금지 | `PersistenceException` |
| **DB 연결 실패** | Service로 전파 | ❌ 사용 금지 | `CannotGetJdbcConnectionException` |

**원칙:**
- ✅ Repository는 **영속화 로직만 담당**
- ❌ 예외 변환 금지 (Spring이 자동 변환)
- ❌ try-catch 사용 금지
- ✅ Service에서 JPA 예외를 BusinessException으로 변환 (선택적)

**Good Example (Repository):**
```java
@Mapper
public interface PolicyMapper {
    
    // ✅ JPA 예외는 Service로 자동 전파
    Optional<Policy> findById(@Param("id") String id);
    
    // ✅ MyBatis 예외는 Service로 자동 전파
    int insert(Policy policy);
    
    // ✅ 낙관적 락 예외도 Service로 전파
    int update(Policy policy);
}
```

**Good Example (Service에서 JPA 예외 처리):**
```java
@Service
@Transactional
public class PolicyService {
    
    private final PolicyMapper policyMapper;
    
    public void createPolicy(CreatePolicyRequest request) {
        Policy policy = Policy.from(request);
        
        try {
            policyMapper.insert(policy);
        } catch (DataIntegrityViolationException e) {
            // ✅ Service에서 JPA 예외를 BusinessException으로 변환
            throw new BusinessException("POLICY_DUPLICATE", "이미 존재하는 정책입니다");
        }
    }
}
```

---

## 🔧 ErrorCode + BusinessException 설계

### ErrorCode Enum

**위치**: `backend/common/src/main/java/com/inspecthub/common/exception/ErrorCode.java`

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // ========== Authentication Errors (AUTH_xxx) ==========
    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "사용자를 찾을 수 없습니다"),
    AUTH_002(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 일치하지 않습니다"),
    AUTH_003(HttpStatus.FORBIDDEN, "AUTH_003", "비활성화된 계정입니다"),
    AUTH_004(HttpStatus.LOCKED, "AUTH_004", "계정이 잠금되었습니다"),
    AUTH_005(HttpStatus.UNAUTHORIZED, "AUTH_005", "토큰이 유효하지 않습니다"),
    
    // ========== Policy Errors (POLICY_xxx) ==========
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND", "정책을 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "METHOD_NOT_ALLOWED", "허용되지 않은 로그인 방식입니다"),
    
    // ========== Validation Errors ==========
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값 검증 실패"),
    
    // ========== Domain Errors ==========
    INVALID_DOMAIN_STATE(HttpStatus.BAD_REQUEST, "INVALID_DOMAIN_STATE", "도메인 상태가 유효하지 않습니다"),
    
    // ========== Server Errors ==========
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", "외부 API 호출 실패");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
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

### BusinessException

**위치**: `backend/common/src/main/java/com/inspecthub/common/exception/BusinessException.java`

```java
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final String message;
    
    /**
     * ErrorCode enum으로 예외 생성 (권장)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    /**
     * ErrorCode + 커스텀 메시지
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode.getCode();
        this.message = customMessage;
    }
}
```

### GlobalExceptionHandler

**위치**: `backend/common/src/main/java/com/inspecthub/common/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * BusinessException 처리
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
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : "입력값 검증 실패";
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }
    
    /**
     * DomainException 처리
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        log.warn("Domain exception: {}", e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_DOMAIN_STATE", e.getMessage()));
    }
    
    /**
     * 예측하지 못한 예외 처리
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

## 🔀 Service 레이어 예외 처리 패턴

### 패턴 1: BusinessException 던지기 (유스케이스 중단)

**사용 시기:**
- 트랜잭션 롤백이 필요한 경우
- 유스케이스를 중단시켜야 하는 실패

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
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
        
        // 4. JWT 생성
        return jwtTokenProvider.generateToken(user);
    }
}
```

### 패턴 2: Result<T> 반환 (정상 분기)

**사용 시기:**
- 실패도 정상 흐름인 경우
- 트랜잭션 롤백이 불필요한 경우

**Result 타입 정의:**
```java
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
```

**Service 구현:**
```java
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    public Result<List<RecommendationDto>> getRecommendations(String userId) {
        try {
            List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);
            
            if (recommendations.isEmpty()) {
                // ✅ 빈 결과도 성공으로 간주
                return Result.success(Collections.emptyList());
            }
            
            List<RecommendationDto> dtos = recommendations.stream()
                    .map(RecommendationDto::from)
                    .toList();
            
            return Result.success(dtos);
            
        } catch (Exception e) {
            log.error("Failed to get recommendations", e);
            return Result.failure("RECOMMENDATION_ERROR", "추천 조회 실패");
        }
    }
}
```

**Controller에서 Result 처리:**
```java
@RestController
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/recommendations")
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

## 🛡 도메인 규칙 위반 처리

### DomainException 정의

**위치**: `backend/common/src/main/java/com/inspecthub/common/exception/DomainException.java`

```java
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

### Entity에서 규칙 검증

```java
@Entity
@Table(name = "DETECTION_CASE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetectionCase {
    
    @Id
    private String caseId;
    
    @Enumerated(EnumType.STRING)
    private CaseStatus status;
    
    /**
     * Factory Method - 유효성 검증 포함
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
     * 상태 전이 검증
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
}
```

### Value Object 불변성 보장

```java
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("금액은 0 이상이어야 합니다");
        }
        
        if (currency == null) {
            throw new DomainException("통화는 필수입니다");
        }
        
        return new Money(amount, currency);
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("통화가 일치하지 않습니다");
        }
        
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

---

## 🔄 재시도/회로 차단 패턴

### Spring Retry 활용

**의존성 추가 (build.gradle):**
```gradle
implementation 'org.springframework.retry:spring-retry'
implementation 'org.springframework:spring-aspects'
```

**활성화 (@EnableRetry):**
```java
@SpringBootApplication
@EnableRetry
public class InspectHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(InspectHubApplication.class, args);
    }
}
```

**Service에서 Retry 사용:**
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

### Resilience4j Circuit Breaker 활용

**의존성 추가:**
```gradle
implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.1.0'
```

**설정 (application.yml):**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      ad-auth:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        permittedNumberOfCallsInHalfOpenState: 3
```

**Service에서 Circuit Breaker 사용:**
```java
@Service
@RequiredArgsConstructor
public class AdAuthenticationService {
    
    private final LdapTemplate ldapTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    /**
     * AD 인증 with Circuit Breaker
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

## 🔒 보안 고려사항

> **원칙**: 사용자에게 시스템 내부 정보를 노출하지 않으면서도 유용한 에러 정보 제공

### 1. 스택 트레이스 노출 방지

**문제점:**
- 프로덕션 환경에서 스택 트레이스 노출은 **보안 취약점**
- 공격자에게 시스템 구조, 사용 기술, 코드 경로 정보 제공

**해결방법:**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;
    
    /**
     * 예측하지 못한 예외 처리
     * 개발 환경: 스택 트레이스 포함
     * 프로덕션: 일반적인 에러 메시지만 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);
        
        // 개발 환경에서만 상세 정보 제공
        if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
            Map<String, Object> debugInfo = Map.of(
                "exception", e.getClass().getSimpleName(),
                "message", e.getMessage(),
                "stackTrace", Arrays.stream(e.getStackTrace())
                    .limit(10)
                    .map(StackTraceElement::toString)
                    .toList()
            );
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류", debugInfo));
        }
        
        // 프로덕션: 일반적인 메시지만
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"));
    }
}
```

### 2. 민감 정보 보호

**금지 사항:**
- ❌ 비밀번호, API 키, 토큰을 예외 메시지나 로그에 포함
- ❌ 사용자 개인정보 (주민번호, 계좌번호 전체)를 로그에 기록
- ❌ SQL 쿼리 전체를 예외 메시지에 포함
- ❌ 내부 파일 경로를 사용자에게 노출

**Good Example:**
```java
@Service
public class AuthService {
    
    public TokenResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));
        
        // ✅ 로그에 민감 정보 마스킹
        log.info("Login attempt: employeeId={}, ipAddress={}", 
                 request.getEmployeeId(), 
                 request.getIpAddress());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // ✅ 비밀번호를 로그에 기록하지 않음
            log.warn("Invalid password for employeeId={}", request.getEmployeeId());
            throw new BusinessException(ErrorCode.AUTH_002);
        }
        
        return jwtTokenProvider.generateToken(user);
    }
}
```

**Bad Example:**
```java
// ❌ 민감 정보 노출
log.error("Authentication failed: password={}, token={}", 
          request.getPassword(),  // ❌ 비밀번호 노출
          user.getAccessToken()); // ❌ 토큰 노출
```

### 3. SQL Injection 방지

**원칙:**
- ✅ MyBatis의 `#{}` 파라미터 바인딩 사용 (Prepared Statement)
- ❌ `${}` 문자열 치환 사용 금지 (SQL Injection 취약)

```xml
<!-- ✅ Good: 파라미터 바인딩 -->
<select id="findByEmployeeId" resultType="User">
    SELECT * FROM users
    WHERE employee_id = #{employeeId}
</select>

<!-- ❌ Bad: 문자열 치환 (SQL Injection 취약) -->
<select id="findByEmployeeId" resultType="User">
    SELECT * FROM users
    WHERE employee_id = '${employeeId}'
</select>
```

### 4. 에러 코드 기반 보안

**원칙**: 공격자에게 유용한 정보를 제공하지 않음

```java
// ✅ Good: 일반적인 메시지
if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new BusinessException(ErrorCode.AUTH_002); // "인증 실패"
}

// ❌ Bad: 공격자에게 유용한 정보
if (user == null) {
    throw new BusinessException("USER_NOT_FOUND"); // ❌ 사용자 존재 여부 노출
}
if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new BusinessException("INVALID_PASSWORD"); // ❌ 비밀번호만 틀렸음을 알려줌
}
```

---

## 📝 로깅 전략

> **원칙**: 디버깅에 유용한 정보를 남기되, 성능과 보안을 고려

### 1. 로그 레벨 정의

| 레벨 | 용도 | 예시 | 프로덕션 사용 |
|------|------|------|---------------|
| **ERROR** | 즉시 조치 필요한 에러 | DB 연결 실패, 외부 API 타임아웃 | ✅ 필수 |
| **WARN** | 잠재적 문제, 비정상적 상황 | 인증 실패, 유효하지 않은 요청 | ✅ 권장 |
| **INFO** | 중요한 비즈니스 이벤트 | 로그인 성공, 정책 배포 | ✅ 권장 |
| **DEBUG** | 개발자를 위한 상세 정보 | 메서드 진입/종료, 파라미터 값 | ❌ 비활성화 |
| **TRACE** | 매우 상세한 디버깅 정보 | SQL 쿼리, 네트워크 패킷 | ❌ 비활성화 |

### 2. 구조화된 로깅

**원칙:**
- ✅ 키-값 쌍으로 로그 작성 (파싱 용이)
- ✅ 상관관계 ID (Trace ID) 포함
- ✅ JSON 형식 로깅 (프로덕션)

```java
@Service
@Slf4j
public class PolicyService {
    
    @Transactional
    public Policy createPolicy(CreatePolicyRequest request) {
        // ✅ Good: 구조화된 로깅
        log.info("Creating policy: type={}, version={}, createdBy={}", 
                 request.getType(), 
                 request.getVersion(), 
                 getCurrentUserId());
        
        try {
            Policy policy = policyMapper.insert(request);
            
            // ✅ 성공 시 로그
            log.info("Policy created successfully: id={}, type={}", 
                     policy.getId(), 
                     policy.getType());
            
            return policy;
            
        } catch (DataIntegrityViolationException e) {
            // ❌ Bad: 너무 많은 정보
            // log.error("Error creating policy with request: {}", request, e);
            
            // ✅ Good: 필수 정보만
            log.error("Failed to create policy: type={}, version={}, reason=DUPLICATE_KEY", 
                      request.getType(), 
                      request.getVersion());
            
            throw new BusinessException("POLICY_DUPLICATE", "이미 존재하는 정책입니다");
        }
    }
}
```

### 3. MDC (Mapped Diagnostic Context) 활용

**목적**: 분산 시스템에서 요청 추적

```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Trace ID 생성
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        MDC.put("userId", getCurrentUserId());
        MDC.put("ipAddress", request.getRemoteAddr());
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // MDC 정리
            MDC.clear();
        }
    }
}
```

**logback.xml 설정:**
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{traceId}] [%X{userId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeMdc>true</includeMdc>
        </encoder>
    </appender>
</configuration>
```

### 4. 예외 로깅 Best Practices

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        // ✅ WARN 레벨 (예상된 예외)
        log.warn("Business exception: code={}, message={}, traceId={}", 
                 e.getErrorCode(), 
                 e.getMessage(), 
                 MDC.get("traceId"));
        
        return buildErrorResponse(e);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // ✅ ERROR 레벨 (예상치 못한 예외)
        log.error("Unexpected exception: traceId={}, userId={}", 
                  MDC.get("traceId"), 
                  MDC.get("userId"), 
                  e); // ✅ 스택 트레이스 포함
        
        return buildErrorResponse(ErrorCode.INTERNAL_ERROR);
    }
}
```

---

## 🧪 테스트 전략

> **원칙**: 예외 핸들러도 비즈니스 로직처럼 테스트

### 1. MockMvc를 사용한 예외 핸들러 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    /**
     * BusinessException 테스트
     */
    @Test
    @DisplayName("인증 실패 시 401 응답 반환")
    void shouldReturn401WhenAuthenticationFails() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("user001", "wrongPassword");
        when(authService.authenticate(any())).thenThrow(new BusinessException(ErrorCode.AUTH_002));
        
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("AUTH_002"))
            .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다"))
            .andDo(print());
    }
    
    /**
     * Validation 예외 테스트
     */
    @Test
    @DisplayName("유효하지 않은 요청 시 400 응답 반환")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest("", ""); // 빈 값
        
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andDo(print());
    }
    
    /**
     * 예측하지 못한 예외 테스트
     */
    @Test
    @DisplayName("예측하지 못한 예외 시 500 응답 반환")
    void shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {
        // Given
        when(authService.authenticate(any())).thenThrow(new RuntimeException("Database connection lost"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user001", "password"))))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
            .andDo(print());
    }
}
```

### 2. Service 레이어 예외 테스트

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("사용자를 찾을 수 없을 때 AUTH_001 예외 발생")
    void shouldThrowAuth001WhenUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password");
        when(userRepository.findByEmployeeId("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.authenticate(request));
        
        assertThat(exception.getErrorCode()).isEqualTo("AUTH_001");
        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("비밀번호 불일치 시 AUTH_002 예외 발생")
    void shouldThrowAuth002WhenPasswordMismatch() {
        // Given
        User user = User.builder()
            .employeeId("user001")
            .password("encodedPassword")
            .build();
        
        LoginRequest request = new LoginRequest("user001", "wrongPassword");
        when(userRepository.findByEmployeeId("user001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.authenticate(request));
        
        assertThat(exception.getErrorCode()).isEqualTo("AUTH_002");
    }
}
```

### 3. 도메인 예외 테스트

```java
class DetectionCaseTest {
    
    @Test
    @DisplayName("유효하지 않은 케이스 ID로 생성 시 DomainException 발생")
    void shouldThrowDomainExceptionWhenCaseIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> DetectionCase.create(null, CaseType.STR))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("케이스 ID는 필수입니다");
    }
    
    @Test
    @DisplayName("이미 종료된 케이스 종료 시 DomainException 발생")
    void shouldThrowDomainExceptionWhenClosingClosedCase() {
        // Given
        DetectionCase detectionCase = DetectionCase.create("CASE001", CaseType.STR);
        detectionCase.close("Resolved");
        
        // When & Then
        assertThatThrownBy(() -> detectionCase.close("Duplicate close"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("이미 종료된 케이스입니다");
    }
}
```

---

## ⚙️ @RestControllerAdvice 고급 기능

### 1. 스코프 제어 (Scope Control)

**기본 동작**: `@RestControllerAdvice`는 모든 `@RestController`에 적용

**제한 방법:**

```java
// 1. 특정 패키지만 적용
@RestControllerAdvice(basePackages = "com.inspecthub.policy")
public class PolicyExceptionHandler {
    // policy 모듈 예외만 처리
}

// 2. 특정 어노테이션이 있는 Controller만 적용
@RestControllerAdvice(annotations = RestController.class)
public class RestApiExceptionHandler {
    // @RestController만 처리
}

// 3. 특정 클래스만 적용
@RestControllerAdvice(assignableTypes = {PolicyController.class, DetectionController.class})
public class DomainSpecificExceptionHandler {
    // 특정 Controller만 처리
}

// 4. 여러 조건 조합
@RestControllerAdvice(
    basePackages = {"com.inspecthub.policy", "com.inspecthub.detection"},
    assignableTypes = {AdminController.class}
)
public class CombinedExceptionHandler {
    // 복합 조건
}
```

### 2. 우선순위 (Precedence)

**원칙**: Controller 레벨 > 글로벌 레벨

```java
// Controller 레벨 (우선순위 높음)
@RestController
public class PolicyController {
    
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePolicyNotFound(PolicyNotFoundException e) {
        // ✅ 이 핸들러가 글로벌 핸들러보다 우선
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("POLICY_NOT_FOUND", "특정 정책을 찾을 수 없습니다"));
    }
}

// 글로벌 레벨 (우선순위 낮음)
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePolicyNotFound(PolicyNotFoundException e) {
        // ❌ PolicyController에 핸들러가 있으면 이 메서드는 호출되지 않음
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("POLICY_NOT_FOUND", "정책을 찾을 수 없습니다"));
    }
}
```

### 3. 여러 ExceptionHandler 조합

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 여러 예외를 하나의 핸들러로 처리
     */
    @ExceptionHandler({
        UserNotFoundException.class,
        PolicyNotFoundException.class,
        CaseNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFoundExceptions(Exception e) {
        log.warn("Resource not found: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
    }
    
    /**
     * 예외 계층 구조 활용
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        // DataAccessException 하위 모든 예외 처리
        log.error("Database error", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DATABASE_ERROR", "데이터베이스 오류"));
    }
}
```

### 4. @Order를 통한 우선순위 제어

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {
    // 보안 관련 예외를 최우선 처리
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "접근 권한이 없습니다"));
    }
}

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    // 일반 예외는 낮은 우선순위
}
```

---

## ⚠️ 예외 처리 안티패턴

> **주의**: 다음 패턴들은 피해야 함

### 1. 예외 삼키기 (Exception Swallowing)

```java
// ❌ Bad: 예외를 로그 없이 무시
try {
    externalApiService.call();
} catch (Exception e) {
    // 아무것도 하지 않음 - 문제 발생 시 디버깅 불가능
}

// ✅ Good: 최소한 로그 남기기
try {
    externalApiService.call();
} catch (Exception e) {
    log.error("External API call failed", e);
    // Fallback 로직 또는 예외 재발생
}
```

### 2. 과도한 try-catch 사용

```java
// ❌ Bad: 불필요한 try-catch
public User getUser(String id) {
    try {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    } catch (UserNotFoundException e) {
        throw e; // 의미 없는 재발생
    }
}

// ✅ Good: 필요 없으면 제거
public User getUser(String id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
}
```

### 3. 일반 Exception 던지기

```java
// ❌ Bad: 일반 Exception 사용
public void processData() throws Exception {
    if (data == null) {
        throw new Exception("Data is null"); // ❌ 너무 일반적
    }
}

// ✅ Good: 구체적인 예외 사용
public void processData() {
    if (data == null) {
        throw new BusinessException(ErrorCode.DATA_NULL);
    }
}
```

### 4. 예외를 흐름 제어로 사용

```java
// ❌ Bad: 예외를 정상 흐름으로 사용
public User findUser(String id) {
    try {
        return userRepository.findById(id).orElseThrow();
    } catch (NoSuchElementException e) {
        return createDefaultUser(); // ❌ 예외로 흐름 제어
    }
}

// ✅ Good: 명시적 분기
public User findUser(String id) {
    return userRepository.findById(id)
            .orElseGet(this::createDefaultUser);
}
```

### 5. 민감한 정보 노출

```java
// ❌ Bad: 스택 트레이스 전체 노출
catch (Exception e) {
    return ApiResponse.error("ERROR", e.toString()); // ❌ 내부 정보 노출
}

// ✅ Good: 안전한 메시지만 노출
catch (Exception e) {
    log.error("Error occurred", e); // ✅ 로그에만 상세 정보
    return ApiResponse.error("INTERNAL_ERROR", "서버 오류");
}
```

### 6. Checked Exception 남용

```java
// ❌ Bad: 불필요한 Checked Exception
public User getUser(String id) throws UserNotFoundException { // ❌ Checked
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
}

// ✅ Good: RuntimeException 사용
public User getUser(String id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id)); // ✅ Unchecked
}
```

---

## ⚡ 성능 고려사항

### 1. 예외 생성 비용

**문제**: 예외 객체 생성은 비용이 높음 (스택 트레이스 생성)

**해결방법:**

```java
// ❌ Bad: 반복문에서 예외 생성
for (String id : ids) {
    try {
        processUser(id);
    } catch (UserNotFoundException e) {
        // 예외를 흐름 제어로 사용 - 성능 저하
    }
}

// ✅ Good: 예외 대신 Optional 사용
for (String id : ids) {
    userRepository.findById(id)
            .ifPresent(this::processUser);
}
```

### 2. 스택 트레이스 비활성화 (특수 상황)

**고성능이 필요한 경우에만 사용:**

```java
public class PerformanceOptimizedException extends RuntimeException {
    
    public PerformanceOptimizedException(String message) {
        super(message);
    }
    
    /**
     * 스택 트레이스 비활성화 (성능 최적화)
     * 주의: 디버깅이 어려워지므로 신중히 사용
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // 스택 트레이스 생성 건너뛰기
    }
}
```

### 3. 예외 캐싱 (Singleton Pattern)

**극단적 최적화 (일반적으로 권장하지 않음):**

```java
public class OptimizedExceptions {
    
    // ⚠️ 주의: 스택 트레이스가 재사용되어 디버깅 어려움
    public static final BusinessException USER_NOT_FOUND = 
            new BusinessException(ErrorCode.AUTH_001);
    
    public static final BusinessException INVALID_PASSWORD = 
            new BusinessException(ErrorCode.AUTH_002);
}

// 사용
throw OptimizedExceptions.USER_NOT_FOUND; // ⚠️ 스택 트레이스 부정확
```

### 4. 로깅 성능 고려

```java
// ❌ Bad: 무거운 연산을 로그 메시지에 직접 포함
log.debug("User data: {}", expensiveToStringMethod()); // toString() 항상 실행

// ✅ Good: isDebugEnabled() 체크
if (log.isDebugEnabled()) {
    log.debug("User data: {}", expensiveToStringMethod());
}

// ✅ Better: Lazy evaluation (Logback/SLF4J 지원)
log.debug("User data: {}", () -> expensiveToStringMethod());
```

---

## ✅ 예외 처리 체크리스트

### 코드 작성 시

- [ ] **유스케이스 중단이 필요한가?** → `BusinessException` 사용
- [ ] **실패도 정상 분기인가?** → `Result<T>` 또는 `Optional<T>` 사용
- [ ] **도메인 규칙 위반인가?** → `DomainException` 사용
- [ ] **ErrorCode enum에 에러 코드가 정의되어 있는가?**
- [ ] **전역 핸들러에서 HTTP 상태 코드가 올바르게 매핑되는가?**
- [ ] **예외 메시지가 사용자에게 유용한 정보를 제공하는가?**
- [ ] **민감 정보(비밀번호, 토큰)가 로그에 노출되지 않는가?**

### 코드 리뷰 시

- [ ] **Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않는가?**
- [ ] **Service에서 발생하는 예외가 적절한 타입인가?**
- [ ] **도메인 객체가 항상 유효한 상태를 유지하는가?**
- [ ] **외부 API 호출에 Retry/Circuit Breaker가 적용되어 있는가?**
- [ ] **트랜잭션 롤백 범위가 명확한가?**
- [ ] **예외 발생 시 충분한 컨텍스트가 로그에 기록되는가?**

---

## 🔧 리팩터링 계획

### Phase 1: ErrorCode 통합

**목표**: 분산된 에러 코드를 `ErrorCode` enum으로 통합

**작업 항목:**
- [ ] 현재 사용 중인 모든 에러 코드 수집
- [ ] `ErrorCode` enum에 통합 (카테고리별 분류)
- [ ] HTTP 상태 코드 매핑 검증
- [ ] 중복 에러 코드 제거

### Phase 2: BusinessException 표준화

**목표**: 모든 Service 레이어에서 `BusinessException` 사용

**작업 항목:**
- [ ] 직접 문자열 에러 코드 사용 제거
- [ ] `ErrorCode` enum 기반 예외 발생으로 변경
- [ ] `GlobalExceptionHandler`에서 `ErrorCode` 매핑 적용

### Phase 3: 도메인 규칙 검증 강화

**목표**: Entity/VO에서 유효성 검증 추가

**작업 항목:**
- [ ] `DomainException` 도입
- [ ] Entity 생성자에서 유효성 검증 추가
- [ ] 상태 전이 메서드에서 규칙 검증 추가
- [ ] Value Object 불변성 보장

### Phase 4: Result 패턴 도입

**목표**: 선택적 기능에 `Result<T>` 패턴 적용

**작업 항목:**
- [ ] `Result<T>` 타입 정의
- [ ] 검색/추천 등 빈 결과 가능한 API에 적용
- [ ] Controller에서 Result 처리 패턴 구현

### Phase 5: 외부 API 안정성 강화

**목표**: Resilience4j Circuit Breaker + Spring Retry 적용

**작업 항목:**
- [ ] Resilience4j 의존성 추가
- [ ] Circuit Breaker 설정 (application.yml)
- [ ] AD 인증, SSO 호출에 Circuit Breaker 적용
- [ ] Spring Retry + Fallback 패턴 적용

---

## 📖 참고 문서

### 프로젝트 문서

- [Backend README](../../../backend/README.md) - 백엔드 개발 가이드
- [API Contract](../../../api/CONTRACT.md) - Frontend ↔ Backend API 계약
- [DDD Design](../../../architecture/DDD_DESIGN.md) - 도메인 주도 설계
- [TDD Workflow](../../TDD_DDD_WORKFLOW.md) - TDD 개발 워크플로우

### 외부 문서

- [Spring Boot Exception Handling](https://spring.io/guides/tutorials/rest)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Retry](https://github.com/spring-projects/spring-retry)
- [Martin Fowler - Refactoring](https://refactoring.com/)

---

**문서 작성**: 2025-11-16  
**최종 업데이트**: 2025-11-16  
**작성자**: Product Manager Agent (John)  
**리뷰어**: Backend Tech Lead

---

## 🔄 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-11-16 | 예외 처리 설계 가이드 초안 작성 | PM |
