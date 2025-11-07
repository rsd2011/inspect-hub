package com.inspecthub.auth.controller;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.SSOLoginRequest;
import com.inspecthub.auth.dto.TokenRefreshRequest;
import com.inspecthub.auth.dto.TokenRefreshResponse;
import com.inspecthub.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 인증 및 토큰 관리 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 전통적인 로그인 API (username/password)
     */
    @PostMapping("/login")
    @Operation(
        summary = "전통 방식 로그인",
        description = "사용자명과 비밀번호를 사용한 전통적인 로그인 방식입니다. 성공 시 JWT 액세스 토큰과 리프레시 토큰을 반환합니다."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Traditional login request for username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * SSO 로그인 API
     */
    @PostMapping("/sso/login")
    @Operation(
        summary = "SSO 로그인",
        description = "Single Sign-On 토큰을 사용한 로그인입니다. Okta, Azure AD 등의 SSO 제공자 토큰을 검증하여 로그인합니다."
    )
    public ResponseEntity<LoginResponse> ssoLogin(@Valid @RequestBody SSOLoginRequest request) {
        log.info("SSO login request with provider: {}", request.getProvider());
        LoginResponse response = authService.loginSSO(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "액세스 토큰 갱신",
        description = "만료된 액세스 토큰을 리프레시 토큰을 사용하여 갱신합니다. 새로운 액세스 토큰과 리프레시 토큰을 반환합니다."
    )
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        log.info("Token refresh request");
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 사용자를 로그아웃합니다. 서버 측에서 토큰을 무효화하고 세션을 종료합니다."
    )
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String userId) {
        log.info("Logout request for user: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 사용자 정보 조회 (테스트용)
     */
    @GetMapping("/me")
    @Operation(
        summary = "현재 사용자 조회",
        description = "현재 인증된 사용자의 정보를 조회합니다. 테스트 및 디버깅 목적으로 사용됩니다."
    )
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String userId) {
        log.debug("Get current user request: {}", userId);
        return ResponseEntity.ok("Authenticated user ID: " + userId);
    }
}
