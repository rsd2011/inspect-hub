package com.inspecthub.server.config;

import com.inspecthub.common.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 테넌트 인터셉터
 *
 * HTTP 요청에서 테넌트 ID를 추출하여 TenantContext에 저장
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String DEFAULT_TENANT = "DEFAULT";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_HEADER);

        // 헤더에 테넌트 ID가 없으면 기본값 사용
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT;
        }

        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        // 요청 처리 후 테넌트 컨텍스트 초기화
        TenantContext.clear();
    }
}
