package com.inspecthub.server.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 테넌트(회원사/기관) 도메인 모델
 */
@Data
public class Tenant {

    /**
     * 테넌트 ID
     */
    private String tenantId;

    /**
     * 테넌트 이름
     */
    private String tenantName;

    /**
     * 상태 (ACTIVE, INACTIVE, SUSPENDED)
     */
    private String status;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    private String createdBy;

    /**
     * 수정자
     */
    private String updatedBy;
}
