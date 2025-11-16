package com.inspecthub.auth.controller;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.RefreshTokenRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.service.AuthService;
import com.inspecthub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 *
 * LOCAL 로그인, 토큰 갱신 API
 */
@Tag(name = "Authentication", description = "인증 API - 로그인 및 토큰 관리")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.inspecthub.auth.service.AdAuthenticationService adAuthenticationService;

    /**
     * LOCAL 로그인
     *
     * POST /api/v1/auth/login
     */
    @Operation(
            summary = "LOCAL 로그인",
            description = "사원번호와 비밀번호로 로그인하여 JWT Access Token과 Refresh Token을 발급받습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 사용자를 찾을 수 없거나 비밀번호가 일치하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "계정 비활성화"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "423",
                    description = "계정 잠금 - 로그인 실패 횟수 초과"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login attempt: employeeId={}", request.getEmployeeId());

        TokenResponse tokenResponse = authService.authenticate(request);

        return ResponseEntity.ok(
                ApiResponse.success(tokenResponse)
        );
    }

    /**
     * AD 로그인
     *
     * POST /api/v1/auth/login/ad
     */
    @Operation(
            summary = "AD 로그인",
            description = "Active Directory 인증을 통해 로그인하여 JWT Access Token과 Refresh Token을 발급받습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "AD 로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "AD 인증 실패 - 사용자를 찾을 수 없거나 비밀번호가 일치하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "계정 비활성화"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "423",
                    description = "계정 잠금"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "AD 서버 연결 실패"
            )
    })
    @PostMapping("/login/ad")
    public ResponseEntity<ApiResponse<TokenResponse>> loginWithAd(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("AD login attempt: employeeId={}", request.getEmployeeId());

        TokenResponse tokenResponse = adAuthenticationService.authenticate(request);

        return ResponseEntity.ok(
                ApiResponse.success(tokenResponse)
        );
    }

    /**
     * Access Token 갱신
     *
     * POST /api/v1/auth/refresh
     */
    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다. (Token Rotation)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token이 유효하지 않거나 만료됨"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Token refresh attempt");

        TokenResponse tokenResponse = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(tokenResponse)
        );
    }
}
