package com.inspecthub.auth.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
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

    private final AuditLogMapper auditLogMapper;

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
                loginMethod,
                null   // userAgent - 나중에 HttpServletRequest에서 추출
            );

            auditLogMapper.insert(auditLog);

            log.info("로그인 성공 감사 로그 저장: id={}, employeeId={}, method={}",
                id, employeeId, loginMethod);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 플로우를 방해하지 않도록 로그만 출력
            log.error("로그인 성공 감사 로그 저장 실패: employeeId={}, method={}",
                employeeId, loginMethod, e);
        }
    }

    /**
     * 로그인 성공 기록 (User 객체 버전)
     *
     * @param user User 도메인 객체 (userId, employeeId, username 포함)
     * @param loginMethod 로그인 방법 (AD, SSO, LOCAL)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginSuccess(com.inspecthub.auth.domain.User user, String loginMethod) {
        try {
            String id = UlidCreator.getUlid().toString();

            AuditLog auditLog = AuditLog.createLoginSuccess(
                id,
                user.getEmployeeId(),
                user.getId().getValue(),  // UserId value object에서 실제 값 추출
                user.getName(),           // username
                null,  // clientIp - 나중에 HttpServletRequest에서 추출
                loginMethod,
                null   // userAgent - 나중에 HttpServletRequest에서 추출
            );

            auditLogMapper.insert(auditLog);

            log.info("로그인 성공 감사 로그 저장: id={}, userId={}, employeeId={}, username={}, method={}",
                id, user.getId().getValue(), user.getEmployeeId(), user.getName(), loginMethod);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 플로우를 방해하지 않도록 로그만 출력
            log.error("로그인 성공 감사 로그 저장 실패: employeeId={}, method={}",
                user.getEmployeeId(), loginMethod, e);
        }
    }

    /**
     * 로그인 성공 기록 (User + HttpServletRequest 버전)
     *
     * @param user User 도메인 객체
     * @param request HTTP 요청 (clientIp 추출용)
     * @param loginMethod 로그인 방법 (AD, SSO, LOCAL)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginSuccess(com.inspecthub.auth.domain.User user, HttpServletRequest request, String loginMethod) {
        try {
            String id = UlidCreator.getUlid().toString();
            String clientIp = extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            AuditLog auditLog = AuditLog.createLoginSuccess(
                id,
                user.getEmployeeId(),
                user.getId().getValue(),
                user.getName(),
                clientIp,
                loginMethod,
                userAgent
            );

            auditLogMapper.insert(auditLog);

            log.info("로그인 성공 감사 로그 저장: id={}, userId={}, employeeId={}, clientIp={}, method={}",
                id, user.getId().getValue(), user.getEmployeeId(), clientIp, loginMethod);
        } catch (Exception e) {
            log.error("로그인 성공 감사 로그 저장 실패: employeeId={}, method={}",
                user.getEmployeeId(), loginMethod, e);
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     *
     * X-Forwarded-For 헤더 우선 (프록시/로드밸런서를 통한 요청)
     * 없으면 RemoteAddr 사용
     * 여러 IP가 있는 경우 첫 번째 IP 반환 (원본 클라이언트 IP)
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // 여러 IP가 콤마로 구분된 경우 첫 번째 IP 사용
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
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

            auditLogMapper.insert(auditLog);

            log.warn("로그인 실패 감사 로그 저장: id={}, employeeId={}, reason={}, method={}",
                id, employeeId, reason, loginMethod);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 플로우를 방해하지 않도록 로그만 출력
            log.error("로그인 실패 감사 로그 저장 실패: employeeId={}, reason={}, method={}",
                employeeId, reason, loginMethod, e);
        }
    }
}
