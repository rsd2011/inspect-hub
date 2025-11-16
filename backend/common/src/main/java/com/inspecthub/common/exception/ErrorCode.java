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
     * 에러 코드 문자열로부터 ErrorCode enum 찾기
     *
     * @param code 에러 코드 문자열
     * @return 매칭되는 ErrorCode enum, 없으면 INTERNAL_ERROR
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
