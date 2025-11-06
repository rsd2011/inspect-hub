package com.inspecthub.common.util;

/**
 * 멀티 테넌시 컨텍스트
 *
 * ThreadLocal을 사용하여 현재 요청의 테넌트 ID를 저장
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * 현재 테넌트 ID 설정
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * 현재 테넌트 ID 조회
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * 테넌트 컨텍스트 초기화
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
