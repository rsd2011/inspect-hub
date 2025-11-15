package com.inspecthub.admin.loginpolicy.exception;

import com.inspecthub.common.exception.BusinessException;

/**
 * EmptyMethodsException - 활성화된 로그인 방식이 없음
 *
 * HTTP Status: 400 Bad Request
 * Error Code: EMPTY_METHODS
 *
 * 사용 예:
 * - 정책 업데이트 시 enabledMethods가 비어있을 때
 * - 최소 1개의 로그인 방식은 활성화되어야 함
 */
public class EmptyMethodsException extends BusinessException {

    private static final String ERROR_CODE = "EMPTY_METHODS";
    private static final String DEFAULT_MESSAGE = "최소 1개의 로그인 방식을 활성화해야 합니다";

    public EmptyMethodsException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public EmptyMethodsException(String message) {
        super(ERROR_CODE, message);
    }

    public EmptyMethodsException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
