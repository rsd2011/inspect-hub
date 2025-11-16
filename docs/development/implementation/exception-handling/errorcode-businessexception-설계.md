# 🔧 ErrorCode + BusinessException 설계

## ErrorCode Enum

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

## BusinessException

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

## GlobalExceptionHandler

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
