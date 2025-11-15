package com.inspecthub.admin.loginpolicy.exception;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.common.exception.BusinessException;

/**
 * LastMethodDisableException - 마지막 남은 로그인 방식 비활성화 시도
 *
 * HTTP Status: 400 Bad Request
 * Error Code: LAST_METHOD_DISABLE
 *
 * 사용 예:
 * - 단 1개의 로그인 방식만 활성화된 상태에서 그 방식을 비활성화하려고 할 때
 * - 최소 1개의 로그인 방식은 활성화되어야 함
 */
public class LastMethodDisableException extends BusinessException {

    private static final String ERROR_CODE = "LAST_METHOD_DISABLE";
    private static final String DEFAULT_MESSAGE = "마지막 남은 로그인 방식은 비활성화할 수 없습니다";

    public LastMethodDisableException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public LastMethodDisableException(LoginMethod lastMethod) {
        super(ERROR_CODE, String.format("'%s'은(는) 마지막 남은 로그인 방식이므로 비활성화할 수 없습니다", lastMethod));
    }

    public LastMethodDisableException(String message) {
        super(ERROR_CODE, message);
    }

    public LastMethodDisableException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
