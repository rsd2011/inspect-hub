# 🚨 Exception Handling Design Guide

> **📚 완전한 가이드**: 예외 처리에 대한 상세하고 체계적인 내용은 **[Exception Handling 완전 가이드](./exception-handling.md)** 문서를 참조하세요.  
> 해당 문서에는 보안 고려사항, 로깅 전략, 테스트 패턴, 성능 최적화 등 실무 베스트 프랙티스가 포함되어 있습니다.

> **본 섹션은 Java/Spring 예외 처리 설계를 위한 LLM 지시문 템플릿 기반으로 작성되었습니다.**

## 1. 예외 처리 설계 철학

Inspect-Hub 프로젝트는 **엔터프라이즈급 백엔드 애플리케이션**으로, 다음 원칙을 기반으로 예외 처리를 설계합니다:

### 도메인 정보
- **도메인**: AML 통합 컴플라이언스 모니터링 (사용자 인증/권한, 탐지 엔진, 정책 관리, 보고서 생성)
- **레이어 구성**: Controller → Service → Repository → Domain(Entity, VO)
- **전역 예외 처리**: `@RestControllerAdvice` + `@ExceptionHandler` 기반
- **트랜잭션**: Service 레이어에서 `@Transactional` 사용

---

## 2. 레이어별 책임 분리

### Controller 레이어
- **책임**: HTTP 요청/응답 처리, 상태 코드 매핑
- **예외 처리**: 
  - 비즈니스 예외는 Service에서 발생한 것을 전역 핸들러로 전파
  - Validation 실패(`@Valid`)는 자동으로 전역 핸들러가 처리
- **금지사항**: Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않음

### Service 레이어
- **책임**: 유스케이스 단위 비즈니스 로직, 트랜잭션 경계, 도메인 조합
- **예외 처리**:
  - **유스케이스 중단이 필요한 실패**: `BusinessException` 던지기 (트랜잭션 롤백)
  - **정상 분기로 간주되는 실패**: `Result<T>` 또는 `Optional<T>` 반환
  - **외부 API 호출 실패**: Retry/Circuit Breaker와 함께 예외 처리

### Domain 레이어 (Entity, VO)
- **책임**: 도메인 불변식 유지
- **예외 처리**:
  - 도메인 규칙 위반 시 **즉시 예외 발생** (생성자, 상태 전이 메서드)
  - 항상 유효한 상태만 유지 (null/invalid 상태 최소화)

### Repository 레이어
- **책임**: 영속화 로직
- **예외 처리**: JPA 예외를 Service로 전파 (변환하지 않음)

---

## 3. 예외 vs 결과 객체 사용 기준

### 예외로 처리 (BusinessException)
- 트랜잭션 롤백이 필요한 경우
- 유스케이스를 중단시켜야 하는 실패
- 예시:
  - 인증 실패
  - 권한 부족
  - 필수 데이터 누락
  - 도메인 규칙 위반

### 결과 객체로 처리 (Result/Optional)
- "실패도 정상 분기"로 간주되는 경우
- 트랜잭션 롤백이 불필요한 경우
- 예시:
  - 검색 결과 없음
  - 추천 데이터 스킵
  - 선택적 기능 실패 (degradation)

---

## 4. ErrorCode + BusinessException 설계

### ErrorCode Enum 구조

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

### BusinessException 구현

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

## 5. 전역 예외 핸들러 (GlobalExceptionHandler)

**현재 구현**: `backend/common/src/main/java/com/inspecthub/common/exception/GlobalExceptionHandler.java`

### 처리 범위
1. `BusinessException` → 비즈니스 에러 (에러 코드 기반 HTTP 상태 매핑)
2. `MethodArgumentNotValidException` → Bean Validation 실패 (400)
3. `HttpMediaTypeNotSupportedException` → Content-Type 미지원 (415)
4. `HttpMessageNotReadableException` → JSON 파싱 오류 (400)
5. `Exception` → 예측하지 못한 예외 (500)

### 개선 방향
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

## 6. Service 레이어 예외 처리 패턴

### 패턴 1: BusinessException 던지기 (트랜잭션 롤백)

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

### 패턴 2: Result<T> 반환 (정상 분기)

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

### Controller에서 Result 처리

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

## 7. 도메인 규칙 위반 처리

### DomainException 정의

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

### 도메인 객체에서 규칙 검증

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

### Value Object 불변성 보장

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

## 8. 재시도/회로 차단 패턴과 예외 처리 연계

### Spring Retry 활용

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

## 9. 예외 처리 체크리스트

### 코드 작성 시
- [ ] 유스케이스 중단이 필요한가? → `BusinessException` 사용
- [ ] 실패도 정상 분기인가? → `Result<T>` 또는 `Optional<T>` 사용
- [ ] 도메인 규칙 위반인가? → `DomainException` 사용
- [ ] ErrorCode enum에 에러 코드가 정의되어 있는가?
- [ ] 전역 핸들러에서 HTTP 상태 코드가 올바르게 매핑되는가?

### 코드 리뷰 시
- [ ] Controller에서 try-catch로 비즈니스 예외를 직접 처리하지 않는가?
- [ ] Service에서 발생하는 예외가 적절한 타입인가?
- [ ] 도메인 객체가 항상 유효한 상태를 유지하는가?
- [ ] 외부 API 호출에 Retry/Circuit Breaker가 적용되어 있는가?
- [ ] 예외 메시지가 사용자에게 유용한 정보를 제공하는가?

---

## 10. 리팩터링 계획

### Phase 1: ErrorCode 통합
- [ ] 현재 분산된 에러 코드를 `ErrorCode` enum으로 통합
- [ ] `GlobalExceptionHandler`에서 `ErrorCode` 기반 매핑 적용

### Phase 2: BusinessException 표준화
- [ ] 모든 Service 레이어에서 `BusinessException` 사용
- [ ] 직접 문자열 에러 코드 사용 제거

### Phase 3: 도메인 규칙 검증 강화
- [ ] Entity/VO 생성자에서 유효성 검증 추가
- [ ] `DomainException` 도입

### Phase 4: Result 패턴 도입
- [ ] 선택적 기능에 `Result<T>` 패턴 적용
- [ ] 검색/추천 등 빈 결과 가능한 API에 적용

### Phase 5: 외부 API 안정성 강화
- [ ] Resilience4j Circuit Breaker 적용
- [ ] Spring Retry + Fallback 패턴 적용

---

**문서 작성**: 2025-11-16  
**최종 업데이트**: 2025-11-16  
**작성자**: Product Manager Agent (John)
