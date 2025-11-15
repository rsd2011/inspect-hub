package com.inspecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 요청 DTO
 */
@Schema(description = "Refresh Token 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @Schema(description = "Refresh Token", example = "01JCXYZ1234567890ABCDEF001", required = true)
    @NotBlank(message = "Refresh Token은 필수입니다")
    private String refreshToken;
}
