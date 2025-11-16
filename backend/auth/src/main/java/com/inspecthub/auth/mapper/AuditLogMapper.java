package com.inspecthub.auth.mapper;

import com.inspecthub.auth.domain.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 MyBatis Mapper
 *
 * 모든 인증/인가 관련 이벤트를 기록하고 조회하는 Mapper
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 감사 로그 저장
     */
    void insert(AuditLog auditLog);

    /**
     * ID로 조회
     */
    AuditLog findById(@Param("id") String id);

    /**
     * 사용자별 감사 로그 조회
     */
    List<AuditLog> findByUserId(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 사원ID별 감사 로그 조회
     */
    List<AuditLog> findByEmployeeId(
        @Param("employeeId") String employeeId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 날짜 범위별 조회
     */
    List<AuditLog> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 작업 타입별 조회
     */
    List<AuditLog> findByAction(
        @Param("action") String action,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * IP 주소별 조회
     */
    List<AuditLog> findByClientIp(
        @Param("clientIp") String clientIp,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 성공/실패별 조회
     */
    List<AuditLog> findBySuccess(
        @Param("success") Boolean success,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 일별 로그인 성공 횟수 통계
     */
    Long countLoginSuccessByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 일별 로그인 실패 횟수 통계
     */
    Long countLoginFailureByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * IP별 로그인 시도 횟수
     */
    Long countLoginAttemptsByIp(
        @Param("clientIp") String clientIp,
        @Param("startDate") LocalDateTime startDate
    );
}
