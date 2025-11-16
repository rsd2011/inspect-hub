package com.inspecthub.auth.repository;

import com.inspecthub.auth.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 저장소
 *
 * 모든 인증/인가 관련 이벤트를 기록하고 조회하는 Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * 사용자별 감사 로그 조회
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * 사원ID별 감사 로그 조회
     */
    Page<AuditLog> findByEmployeeIdOrderByTimestampDesc(String employeeId, Pageable pageable);

    /**
     * 날짜 범위별 조회
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * 작업 타입별 조회
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * IP 주소별 조회
     */
    Page<AuditLog> findByClientIpOrderByTimestampDesc(String clientIp, Pageable pageable);

    /**
     * 성공/실패별 조회
     */
    Page<AuditLog> findBySuccessOrderByTimestampDesc(Boolean success, Pageable pageable);

    /**
     * 일별 로그인 성공 횟수 통계
     */
    @Query("SELECT COUNT(a) FROM AuditLog a " +
           "WHERE a.action = 'LOGIN_SUCCESS' " +
           "AND a.timestamp >= :startDate " +
           "AND a.timestamp < :endDate")
    Long countLoginSuccessByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 일별 로그인 실패 횟수 통계
     */
    @Query("SELECT COUNT(a) FROM AuditLog a " +
           "WHERE a.action = 'LOGIN_FAILURE' " +
           "AND a.timestamp >= :startDate " +
           "AND a.timestamp < :endDate")
    Long countLoginFailureByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * IP별 로그인 시도 횟수
     */
    @Query("SELECT COUNT(a) FROM AuditLog a " +
           "WHERE a.clientIp = :clientIp " +
           "AND a.action LIKE 'LOGIN%' " +
           "AND a.timestamp >= :startDate")
    Long countLoginAttemptsByIp(
        @Param("clientIp") String clientIp,
        @Param("startDate") LocalDateTime startDate
    );
}
