package com.inspecthub.admin.systemconfig.controller;

import com.inspecthub.admin.systemconfig.dto.LoginPolicyResponse;
import com.inspecthub.admin.systemconfig.dto.UpdateLoginPolicyRequest;
import com.inspecthub.admin.systemconfig.dto.UpdateMethodsRequest;
import com.inspecthub.admin.systemconfig.dto.UpdatePriorityRequest;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import com.inspecthub.admin.loginpolicy.service.LoginPolicyService;
import com.inspecthub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * SystemConfigController - 시스템 설정 API
 *
 * 시스템 전역 로그인 정책 관리
 */
@Tag(name = "System Configuration", description = "시스템 설정 API")
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
public class SystemConfigController {

    private final LoginPolicyService loginPolicyService;

    /**
     * GET /api/v1/system/login-policy - 전역 로그인 정책 조회
     */
    @Operation(summary = "전역 로그인 정책 조회", description = "시스템 전역 로그인 정책을 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/login-policy")
    public ResponseEntity<ApiResponse<LoginPolicyResponse>> getLoginPolicy() {
        log.debug("GET /api/v1/system/login-policy - 전역 로그인 정책 조회");

        LoginPolicy policy = loginPolicyService.getGlobalPolicy();
        LoginPolicyResponse response = LoginPolicyResponse.fromEntity(policy);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/system/login-policy/available-methods - 사용 가능한 로그인 방식 조회
     */
    @Operation(summary = "사용 가능한 로그인 방식 조회", description = "시스템에서 활성화된 로그인 방식 목록을 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/login-policy/available-methods")
    public ResponseEntity<ApiResponse<Set<LoginMethod>>> getAvailableMethods() {
        log.debug("GET /api/v1/system/login-policy/available-methods - 사용 가능한 로그인 방식 조회");

        Set<LoginMethod> methods = loginPolicyService.getAvailableMethods();

        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    /**
     * PUT /api/v1/system/login-policy - 전역 로그인 정책 전체 업데이트
     */
    @Operation(summary = "전역 로그인 정책 전체 업데이트", description = "시스템 전역 로그인 정책을 전체 업데이트합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/login-policy")
    public ResponseEntity<ApiResponse<LoginPolicyResponse>> updateLoginPolicy(
        @Valid @RequestBody UpdateLoginPolicyRequest request
    ) {
        log.debug("PUT /api/v1/system/login-policy - 전역 로그인 정책 업데이트: {}", request);

        LoginPolicy updated = loginPolicyService.updateGlobalPolicy(
            request.getName(),
            request.getEnabledMethods(),
            request.getPriority()
        );

        LoginPolicyResponse response = LoginPolicyResponse.fromEntity(updated);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/v1/system/login-policy/methods - 활성화된 로그인 방식 업데이트
     */
    @Operation(summary = "활성화된 로그인 방식 업데이트", description = "활성화된 로그인 방식만 업데이트합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/login-policy/methods")
    public ResponseEntity<ApiResponse<LoginPolicyResponse>> updateMethods(
        @Valid @RequestBody UpdateMethodsRequest request
    ) {
        log.debug("PATCH /api/v1/system/login-policy/methods - 활성화된 로그인 방식 업데이트: {}", request);

        LoginPolicy updated = loginPolicyService.updateEnabledMethods(request.getEnabledMethods());

        LoginPolicyResponse response = LoginPolicyResponse.fromEntity(updated);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PATCH /api/v1/system/login-policy/priority - 로그인 방식 우선순위 업데이트
     */
    @Operation(summary = "로그인 방식 우선순위 업데이트", description = "로그인 방식 우선순위만 업데이트합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/login-policy/priority")
    public ResponseEntity<ApiResponse<LoginPolicyResponse>> updatePriority(
        @Valid @RequestBody UpdatePriorityRequest request
    ) {
        log.debug("PATCH /api/v1/system/login-policy/priority - 로그인 방식 우선순위 업데이트: {}", request);

        LoginPolicy updated = loginPolicyService.updatePriority(request.getPriority());

        LoginPolicyResponse response = LoginPolicyResponse.fromEntity(updated);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
