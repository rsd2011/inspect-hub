package com.inspecthub.auth.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 감사 로그 서비스
 *
 * 비동기 처리로 메인 플로우에 영향을 주지 않음
 * 저장 실패 시 로그만 출력하고 예외를 던지지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 로그인 성공 기록
     *
     * @param employeeId 사원ID
     * @param loginMethod 로그인 방법 (AD, SSO, LOCAL)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginSuccess(String employeeId, String loginMethod) {
        try {
            String id = UlidCreator.getUlid().toString();

            AuditLog auditLog = AuditLog.createLoginSuccess(
                id,
                employeeId,
                null,  // userId - 나중에 User 정보 추가 시 설정
                null,  // username - 나중에 User 정보 추가 시 설정
                null,  // clientIp - 나중에 HttpServletRequest에서 추출
                loginMethod
            );

            auditLogRepository.save(auditLog);

            log.info("로그인 성공 감사 로그 저장: id={}, employeeId={}, method={}",
                id, employeeId, loginMethod);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 플로우를 방해하지 않도록 로그만 출력
            log.error("로그인 성공 감사 로그 저장 실패: employeeId={}, method={}",
                employeeId, loginMethod, e);
        }
    }

    /**
     * 로그인 실패 기록
     *
     * @param employeeId 사원ID
     * @param reason 실패 사유
     * @param loginMethod 로그인 방법 (AD, SSO, LOCAL)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginFailure(String employeeId, String reason, String loginMethod) {
        try {
            String id = UlidCreator.getUlid().toString();

            AuditLog auditLog = AuditLog.createLoginFailure(
                id,
                employeeId,
                null,  // clientIp - 나중에 HttpServletRequest에서 추출
                loginMethod,
                reason
            );

            auditLogRepository.save(auditLog);

            log.warn("로그인 실패 감사 로그 저장: id={}, employeeId={}, reason={}, method={}",
                id, employeeId, reason, loginMethod);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 플로우를 방해하지 않도록 로그만 출력
            log.error("로그인 실패 감사 로그 저장 실패: employeeId={}, reason={}, method={}",
                employeeId, reason, loginMethod, e);
        }
    }
}
