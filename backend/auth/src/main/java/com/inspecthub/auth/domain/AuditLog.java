package com.inspecthub.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 감사 로그 엔티티
 *
 * 모든 인증/인가 관련 이벤트를 기록
 * 금융 규정 준수를 위해 최소 5년 보관 (삭제 금지)
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_employee_id", columnList = "employee_id"),
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_client_ip", columnList = "client_ip")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuditLog {

    /**
     * 고유 식별자 (ULID)
     */
    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    /**
     * 작업 타입
     * LOGIN_SUCCESS, LOGIN_FAILURE, TOKEN_REFRESH_SUCCESS, LOGOUT, etc.
     */
    @Column(name = "action", length = 50, nullable = false)
    private String action;

    /**
     * 사용자 ID (로그인 성공 시, nullable)
     */
    @Column(name = "user_id", length = 26)
    private String userId;

    /**
     * 사원ID (항상 기록)
     */
    @Column(name = "employee_id", length = 50, nullable = false)
    private String employeeId;

    /**
     * 사용자명 (nullable)
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * UTC 타임스탬프
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * 클라이언트 IP 주소
     * X-Forwarded-For 헤더 우선, 없으면 RemoteAddr
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 성공 여부
     */
    @Column(name = "success", nullable = false)
    private Boolean success;

    /**
     * 인증 방법 (AD, SSO, LOCAL)
     */
    @Column(name = "method", length = 20)
    private String method;

    /**
     * 세션 ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * User-Agent 문자열
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Referer 헤더
     */
    @Column(name = "referer", length = 500)
    private String referer;

    /**
     * 실패/특이 사유
     */
    @Column(name = "reason", length = 100)
    private String reason;

    /**
     * JSON 상세 정보
     * {roles, permissions, lastLoginAt, loginCount, geoLocation, device, tokenIds}
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * 조직 ID
     */
    @Column(name = "org_id", length = 26)
    private String orgId;

    /**
     * 조직명
     */
    @Column(name = "org_name", length = 200)
    private String orgName;

    /**
     * 생성 시점 (데이터 삽입 시간)
     * timestamp와 다를 수 있음 (비동기 저장 시)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티 생성 시 타임스탬프 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * 로그인 성공 로그 생성
     */
    public static AuditLog createLoginSuccess(
        String id,
        String employeeId,
        String userId,
        String username,
        String clientIp,
        String method
    ) {
        return AuditLog.builder()
            .id(id)
            .action("LOGIN_SUCCESS")
            .employeeId(employeeId)
            .userId(userId)
            .username(username)
            .clientIp(clientIp)
            .method(method)
            .success(true)
            .timestamp(LocalDateTime.now())
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
            .build();
    }
}
