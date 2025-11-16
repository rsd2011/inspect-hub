package com.inspecthub.auth.repository;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * User Repository - MyBatis Mapper Interface
 */
@Mapper
public interface UserRepository {

    /**
     * 사원ID로 사용자 조회
     */
    Optional<User> findByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 로그인 실패 횟수 증가
     */
    void incrementFailedAttempts(@Param("id") UserId id);

    /**
     * 로그인 실패 횟수 초기화
     */
    void resetFailedAttempts(@Param("id") UserId id);

    /**
     * 마지막 로그인 시간 업데이트
     */
    void updateLastLoginAt(@Param("id") UserId id);

    /**
     * 계정 잠금 설정
     */
    void lockAccount(@Param("id") UserId id, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);

    /**
     * 사용자 저장 (신규 또는 업데이트)
     */
    User save(@Param("user") User user);
}
