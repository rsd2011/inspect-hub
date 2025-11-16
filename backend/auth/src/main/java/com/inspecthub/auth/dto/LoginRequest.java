package com.inspecthub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "LOCAL 로그인 요청")
@Data
@Builder(toBuilder = true)
public class LoginRequest {

    @Schema(description = "사원번호 (로그인 ID)", example = "202401001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사원ID는 필수입니다")
    @Pattern(regexp = "^[0-9]+$", message = "사원ID는 숫자만 입력 가능합니다")
    @Size(max = 50, message = "사원ID는 최대 50자까지 입력 가능합니다")
    private String employeeId;

    @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(max = 100, message = "비밀번호는 최대 100자까지 입력 가능합니다")
    private String password;
}
