package com.inspecthub.admin.loginpolicy.exception;

import com.inspecthub.common.exception.BusinessException;

/**
 * PolicyNotFoundException - 정책을 찾을 수 없음
 *
 * HTTP Status: 404 Not Found
 * Error Code: POLICY_NOT_FOUND
 *
 * 사용 예:
 * - 시스템 전역 로그인 정책이 존재하지 않을 때 (시스템 오류)
 */
public class PolicyNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "POLICY_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "로그인 정책을 찾을 수 없습니다";

    public PolicyNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public PolicyNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public PolicyNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
