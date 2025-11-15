package com.inspecthub.common.exception;

import com.inspecthub.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getErrorCode(), e.getMessage());

        HttpStatus status = mapErrorCodeToHttpStatus(e.getErrorCode());

        return ResponseEntity
                .status(status)
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

        log.warn("Validation exception: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    /**
     * Content-Type 지원하지 않는 경우 (415 Unsupported Media Type)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("Unsupported media type: {}", e.getContentType());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("UNSUPPORTED_MEDIA_TYPE", "Content-Type이 지원되지 않습니다: " + e.getContentType()));
    }

    /**
     * JSON 파싱 오류 (400 Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Message not readable: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_JSON", "요청 본문을 읽을 수 없습니다"));
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

    /**
     * 에러 코드를 HTTP 상태 코드로 매핑
     */
    private HttpStatus mapErrorCodeToHttpStatus(String errorCode) {
        return switch (errorCode) {
            // Authentication errors
            case "AUTH_001", "AUTH_002", "AUTH_005", "AUTH_006" -> HttpStatus.UNAUTHORIZED;
            case "AUTH_003" -> HttpStatus.FORBIDDEN;
            case "AUTH_004" -> HttpStatus.LOCKED;
            
            // Login Policy errors
            case "POLICY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "METHOD_NOT_ALLOWED", "INVALID_METHOD", "EMPTY_METHODS", "LAST_METHOD_DISABLE" -> HttpStatus.BAD_REQUEST;
            
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
