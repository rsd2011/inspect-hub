package com.inspecthub.auth.repository;

import com.inspecthub.auth.domain.LoginPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * LoginPolicyRepository - MyBatis Mapper Interface
 *
 * Jenkins 스타일 전역 LoginPolicy 엔티티 영속성 관리
 */
@Mapper
public interface LoginPolicyRepository {

    /**
     * 시스템 전역 정책 조회
     *
     * @return 시스템 전역 로그인 정책
     */
    Optional<LoginPolicy> findGlobalPolicy();

    /**
     * 정책 저장 (INSERT or UPDATE)
     *
     * @param policy 저장할 정책
     */
    void save(@Param("policy") LoginPolicy policy);

    /**
     * ID로 정책 조회
     *
     * @param id 정책 ID (ULID)
     * @return 로그인 정책
     */
    Optional<LoginPolicy> findById(@Param("id") String id);
}
