package com.inspecthub.auth.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 감사 로그 도메인 객체
 *
 * 모든 인증/인가 관련 이벤트를 기록
 * 금융 규정 준수를 위해 최소 5년 보관 (삭제 금지)
 *
 * MyBatis 매핑용 POJO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuditLog {

    /**
     * 고유 식별자 (ULID)
     */
    private String id;

    /**
     * 작업 타입
     * LOGIN_SUCCESS, LOGIN_FAILURE, TOKEN_REFRESH_SUCCESS, LOGOUT, etc.
     */
    private String action;

    /**
     * 사용자 ID (로그인 성공 시, nullable)
     */
    private String userId;

    /**
     * 사원ID (항상 기록)
     */
    private String employeeId;

    /**
     * 사용자명 (nullable)
     */
    private String username;

    /**
     * UTC 타임스탬프
     */
    private LocalDateTime timestamp;

    /**
     * 클라이언트 IP 주소
     * X-Forwarded-For 헤더 우선, 없으면 RemoteAddr
     */
    private String clientIp;

    /**
     * 성공 여부
     */
    private Boolean success;

    /**
     * 인증 방법 (AD, SSO, LOCAL)
     */
    private String method;

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * User-Agent 문자열
     */
    private String userAgent;

    /**
     * Referer 헤더
     */
    private String referer;

    /**
     * 실패/특이 사유
     */
    private String reason;

    /**
     * JSON 상세 정보
     * {roles, permissions, lastLoginAt, loginCount, geoLocation, device, tokenIds}
     */
    private String details;

    /**
     * 조직 ID
     */
    private String orgId;

    /**
     * 조직명
     */
    private String orgName;

    /**
     * 생성 시점 (데이터 삽입 시간)
     * timestamp와 다를 수 있음 (비동기 저장 시)
     */
    private LocalDateTime createdAt;

    /**
     * 로그인 성공 로그 생성
     */
    public static AuditLog createLoginSuccess(
        String id,
        String employeeId,
        String userId,
        String username,
        String clientIp,
        String method,
        String userAgent,
        String sessionId
    ) {
        return AuditLog.builder()
            .id(id)
            .action("LOGIN_SUCCESS")
            .employeeId(employeeId)
            .userId(userId)
            .username(username)
            .clientIp(clientIp)
            .method(method)
            .userAgent(userAgent)
            .sessionId(sessionId)
            .success(true)
            .timestamp(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * 로그인 실패 로그 생성
     */
    public static AuditLog createLoginFailure(
        String id,
        String employeeId,
        String clientIp,
        String method,
        String reason
    ) {
        return AuditLog.builder()
            .id(id)
            .action("LOGIN_FAILURE")
            .employeeId(employeeId)
            .clientIp(clientIp)
            .method(method)
            .reason(reason)
            .success(false)
            .timestamp(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }
}
