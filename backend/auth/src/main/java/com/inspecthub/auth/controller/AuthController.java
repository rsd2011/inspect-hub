package com.inspecthub.auth.controller;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.TokenRefreshRequest;
import com.inspecthub.auth.dto.TokenRefreshResponse;
import com.inspecthub.auth.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
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
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String userId) {
        log.info("Logout request for user: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 사용자 정보 조회 (테스트용)
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String userId) {
        log.debug("Get current user request: {}", userId);
        return ResponseEntity.ok("Authenticated user ID: " + userId);
    }
}
