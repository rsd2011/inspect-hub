package com.inspecthub.common.service;

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

    /**
     * 정책 변경 기록
     *
     * @param action 액션 타입 (SYSTEM_LOGIN_POLICY_UPDATED, LOGIN_METHOD_ENABLED, etc.)
     * @param userId 관리자 사용자 ID
     * @param beforeJson 변경 전 정책 JSON
     * @param afterJson 변경 후 정책 JSON
     */
    void logPolicyChange(String action, String userId, String beforeJson, String afterJson);
}
