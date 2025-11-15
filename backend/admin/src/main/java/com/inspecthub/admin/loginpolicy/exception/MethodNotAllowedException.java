package com.inspecthub.admin.loginpolicy.exception;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.common.exception.BusinessException;

/**
 * MethodNotAllowedException - 비활성화된 로그인 방식 사용 시도
 *
 * HTTP Status: 400 Bad Request
 * Error Code: METHOD_NOT_ALLOWED
 *
 * 사용 예:
 * - 사용자가 비활성화된 로그인 방식으로 로그인을 시도할 때
 * - 정책에서 SSO만 활성화된 상태에서 AD 로그인 시도
 */
public class MethodNotAllowedException extends BusinessException {

    private static final String ERROR_CODE = "METHOD_NOT_ALLOWED";
    private static final String DEFAULT_MESSAGE = "허용되지 않은 로그인 방식입니다";

    public MethodNotAllowedException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public MethodNotAllowedException(LoginMethod method) {
        super(ERROR_CODE, String.format("'%s' 로그인 방식은 현재 비활성화되어 있습니다", method));
    }

    public MethodNotAllowedException(String message) {
        super(ERROR_CODE, message);
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
