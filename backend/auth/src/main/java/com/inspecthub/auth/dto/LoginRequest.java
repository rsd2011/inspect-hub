package com.inspecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "LOCAL 로그인 요청")
@Data
@Builder(toBuilder = true)
public class LoginRequest {

    @Schema(description = "사원번호 (로그인 ID)", example = "EMP001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사원ID는 필수입니다")
    private String employeeId;

    @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
