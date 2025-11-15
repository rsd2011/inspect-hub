package com.inspecthub.admin.loginpolicy.exception;

import com.inspecthub.common.exception.BusinessException;

/**
 * InvalidMethodException - 잘못된 로그인 방식
 *
 * HTTP Status: 400 Bad Request
 * Error Code: INVALID_METHOD
 *
 * 사용 예:
 * - LoginMethod enum에 없는 방식을 요청할 때
 * - API 요청에 허용되지 않은 로그인 방식이 포함될 때
 */
public class InvalidMethodException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_METHOD";
    private static final String DEFAULT_MESSAGE = "유효하지 않은 로그인 방식입니다";

    public InvalidMethodException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public InvalidMethodException(String methodName) {
        super(ERROR_CODE, String.format("'%s'은(는) 유효하지 않은 로그인 방식입니다. (허용: SSO, AD, LOCAL)", methodName));
    }

    public InvalidMethodException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
