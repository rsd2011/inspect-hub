package com.inspecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 성공 응답")
public class LoginResponse {

    @Schema(description = "사용자 ID", example = "usr_01234567")
    private String userId;

    @Schema(description = "사용자명", example = "admin")
    private String username;

    @Schema(description = "이메일 주소", example = "admin@inspecthub.com")
    private String email;

    @Schema(description = "표시 이름", example = "관리자")
    private String displayName;

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "사용자 역할 목록", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private List<String> roles;

    @Schema(description = "사용자 권한 목록", example = "[\"READ_USER\", \"WRITE_USER\"]")
    private List<String> permissions;

    @Schema(description = "조직 ID", example = "org_12345")
    private String organizationId;

    @Schema(description = "조직명", example = "본사")
    private String organizationName;

    @Schema(description = "토큰 만료 시간 (밀리초)", example = "3600000")
    private Long expiresIn;  // milliseconds

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;  // e.g., "Bearer"

    @Schema(description = "로그인 방식", example = "traditional", allowableValues = {"traditional", "sso"})
    private String loginMethod;  // "traditional" or "sso"
}
