package com.inspecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSO 로그인 요청")
public class SSOLoginRequest {

    @NotBlank(message = "SSO token is required")
    @Schema(description = "SSO 인증 토큰", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String ssoToken;

    /**
     * Optional SSO provider identifier
     * e.g., 'okta', 'azure-ad', 'custom-sso'
     */
    @Schema(description = "SSO 제공자 식별자 (선택사항)", example = "okta", allowableValues = {"okta", "azure-ad", "custom-sso"})
    private String provider;
}
