package com.inspecthub.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 감사 로그 서비스 구현
 *
 * @Async 비동기 실행
 * 실패 시에도 main flow 방해하지 않음
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Override
    public void logLoginSuccess(String employeeId, String loginMethod) {
        log.info("로그인 성공: employeeId={}, method={}", employeeId, loginMethod);
        // TODO: 실제 감사 로그 저장 로직 구현
    }

    @Override
    public void logLoginFailure(String employeeId, String reason, String loginMethod) {
        log.warn("로그인 실패: employeeId={}, reason={}, method={}", employeeId, reason, loginMethod);
        // TODO: 실제 감사 로그 저장 로직 구현
    }

    @Override
    @Async
    public void logPolicyChange(String action, String userId, String beforeJson, String afterJson) {
        try {
            log.info("정책 변경: action={}, userId={}", action, userId);
            log.debug("Before: {}", beforeJson);
            log.debug("After: {}", afterJson);
            // TODO: 실제 감사 로그 저장 로직 구현
        } catch (Exception e) {
            log.error("감사 로그 저장 실패 - action: {}, userId: {}", action, userId, e);
            // DO NOT rethrow - main flow must continue
        }
    }
}
