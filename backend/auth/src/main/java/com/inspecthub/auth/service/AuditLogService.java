package com.inspecthub.auth.service;

/**
 * 감사 로그 서비스 인터페이스
 */
public interface AuditLogService {

    /**
     * 로그인 성공 기록
     */
    void logLoginSuccess(String employeeId, String loginMethod);

    /**
     * 로그인 실패 기록
     */
    void logLoginFailure(String employeeId, String reason, String loginMethod);
}
