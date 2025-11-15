package com.inspecthub.auth.repository;

import com.inspecthub.auth.domain.LoginPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * LoginPolicyRepository - MyBatis Mapper Interface
 *
 * LoginPolicy 엔티티 영속성 관리
 */
@Mapper
public interface LoginPolicyRepository {

    /**
     * 조직ID로 정책 조회
     *
     * @param orgId 조직 ID
     * @return 조직별 로그인 정책
     */
    Optional<LoginPolicy> findByOrgId(@Param("orgId") String orgId);

    /**
     * 글로벌 정책 조회 (orgId = null)
     *
     * @return 글로벌 로그인 정책
     */
    Optional<LoginPolicy> findGlobalPolicy();

    /**
     * 정책 저장 (INSERT or UPDATE)
     *
     * @param policy 저장할 정책
     */
    void save(@Param("policy") LoginPolicy policy);

    /**
     * 조직ID로 정책 존재 여부 확인
     *
     * @param orgId 조직 ID
     * @return 존재 여부
     */
    boolean existsByOrgId(@Param("orgId") String orgId);

    /**
     * ID로 정책 조회
     *
     * @param id 정책 ID (ULID)
     * @return 로그인 정책
     */
    Optional<LoginPolicy> findById(@Param("id") String id);
}
