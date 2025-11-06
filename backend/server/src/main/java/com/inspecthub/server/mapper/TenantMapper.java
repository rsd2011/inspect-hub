package com.inspecthub.server.mapper;

import com.inspecthub.server.domain.Tenant;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 테넌트 Mapper
 */
@Mapper
public interface TenantMapper {

    /**
     * 테넌트 ID로 조회
     */
    @Select("SELECT * FROM tenant WHERE tenant_id = #{tenantId}")
    Tenant findById(@Param("tenantId") String tenantId);

    /**
     * 모든 테넌트 조회
     */
    @Select("SELECT * FROM tenant ORDER BY created_at DESC")
    List<Tenant> findAll();

    /**
     * 활성 테넌트 조회
     */
    @Select("SELECT * FROM tenant WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    List<Tenant> findActiveAll();

    /**
     * 테넌트 생성
     */
    @Insert("INSERT INTO tenant (tenant_id, tenant_name, status, created_at, updated_at, created_by) " +
            "VALUES (#{tenantId}, #{tenantName}, #{status}, NOW(), NOW(), #{createdBy})")
    int insert(Tenant tenant);

    /**
     * 테넌트 수정
     */
    @Update("UPDATE tenant SET tenant_name = #{tenantName}, status = #{status}, " +
            "updated_at = NOW(), updated_by = #{updatedBy} WHERE tenant_id = #{tenantId}")
    int update(Tenant tenant);

    /**
     * 테넌트 삭제
     */
    @Delete("DELETE FROM tenant WHERE tenant_id = #{tenantId}")
    int deleteById(@Param("tenantId") String tenantId);
}
