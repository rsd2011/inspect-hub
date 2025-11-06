package com.inspecthub.common.exception;

/**
 * 테넌트를 찾을 수 없을 때 발생하는 예외
 */
public class TenantNotFoundException extends BusinessException {

    public TenantNotFoundException(String tenantId) {
        super("TENANT_NOT_FOUND", "Tenant not found: " + tenantId);
    }
}
