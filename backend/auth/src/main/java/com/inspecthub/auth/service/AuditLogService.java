package com.inspecthub.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.UlidCreator;
import com.inspecthub.auth.domain.AuditLog;
import com.inspecthub.auth.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ObjectMapper objectMapper;

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
                null,  // userAgent - 나중에 HttpServletRequest에서 추출
                null,  // sessionId - 나중에 HttpServletRequest에서 추출
                null   // referer - 나중에 HttpServletRequest에서 추출
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
                null,  // userAgent - 나중에 HttpServletRequest에서 추출
                null,  // sessionId - 나중에 HttpServletRequest에서 추출
                null   // referer - 나중에 HttpServletRequest에서 추출
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
            String sessionId = extractSessionId(request);
            String referer = request.getHeader("Referer");
            
            // details JSON 생성 (roles, permissions, orgName 포함)
            String details = createLoginDetailsJson(user);

            AuditLog auditLog = AuditLog.createLoginSuccess(
                id,
                user.getEmployeeId(),
                user.getId().getValue(),
                user.getName(),
                clientIp,
                loginMethod,
                userAgent,
                sessionId,
                referer,
                details
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
     * 로그인 상세 정보 JSON 생성
     *
     * @param user User 도메인 객체
     * @return JSON 문자열 (roles, permissions, orgName 포함)
     */
    private String createLoginDetailsJson(com.inspecthub.auth.domain.User user) {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // TODO: User/Organization/Permission 엔티티 구현 후 실제 데이터로 대체
            details.put("roles", List.of());           // 사용자 역할 목록 (TODO: 추후 구현)
            details.put("permissions", List.of());     // 사용자 권한 목록 (TODO: 추후 구현)
            details.put("orgName", null);              // 소속 조직명 (TODO: 조직 정보 조회 기능 구현 후 추가)
            
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            log.error("로그인 상세 정보 JSON 생성 실패: employeeId={}", user.getEmployeeId(), e);
            return "{}";  // 실패 시 빈 JSON 객체 반환
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
     * 세션 ID 추출
     *
     * 기존 세션이 있으면 세션 ID 반환
     * 없으면 null 반환 (세션 생성하지 않음)
     *
     * @param request HTTP 요청
     * @return 세션 ID (없으면 null)
     */
    private String extractSessionId(HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
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
                reason,
                null,  // userAgent - 나중에 HttpServletRequest에서 추출
                null,  // sessionId - 나중에 HttpServletRequest에서 추출
                null   // referer - 나중에 HttpServletRequest에서 추출
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

    /**
     * 로그인 실패 기록 (HttpServletRequest 버전)
     *
     * @param employeeId 사원ID
     * @param reason 실패 사유
     * @param loginMethod 로그인 방법 (AD, SSO, LOCAL)
     * @param request HTTP 요청 (clientIp, userAgent, sessionId, referer 추출용)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginFailure(String employeeId, String reason, String loginMethod, HttpServletRequest request) {
        try {
            String id = UlidCreator.getUlid().toString();
            String clientIp = extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String sessionId = extractSessionId(request);
            String referer = request.getHeader("Referer");

            AuditLog auditLog = AuditLog.createLoginFailure(
                id,
                employeeId,
                clientIp,
                loginMethod,
                reason,
                userAgent,
                sessionId,
                referer
            );

            auditLogMapper.insert(auditLog);

            log.warn("로그인 실패 감사 로그 저장: id={}, employeeId={}, clientIp={}, reason={}, method={}",
                id, employeeId, clientIp, reason, loginMethod);
        } catch (Exception e) {
            log.error("로그인 실패 감사 로그 저장 실패: employeeId={}, reason={}, method={}",
                employeeId, reason, loginMethod, e);
        }
    }
}
