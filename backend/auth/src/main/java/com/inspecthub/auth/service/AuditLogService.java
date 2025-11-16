package com.inspecthub.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 감사 로그 서비스
 */
@Slf4j
@Service
public class AuditLogService {

    /**
     * 로그인 성공 기록
     */
    public void logLoginSuccess(String employeeId, String loginMethod) {
        log.info("로그인 성공: employeeId={}, method={}", employeeId, loginMethod);
        // TODO: 실제 감사 로그 저장 로직 구현
    }

    /**
     * 로그인 실패 기록
     */
    public void logLoginFailure(String employeeId, String reason, String loginMethod) {
        log.warn("로그인 실패: employeeId={}, reason={}, method={}", employeeId, reason, loginMethod);
        // TODO: 실제 감사 로그 저장 로직 구현
    }
}
