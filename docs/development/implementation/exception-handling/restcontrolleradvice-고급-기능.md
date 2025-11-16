# ⚙️ @RestControllerAdvice 고급 기능

## 1. 스코프 제어 (Scope Control)

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

## 2. 우선순위 (Precedence)

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

## 3. 여러 ExceptionHandler 조합

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

## 4. @Order를 통한 우선순위 제어

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
