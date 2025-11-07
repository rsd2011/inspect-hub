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
@Schema(description = "전통 방식 로그인 요청")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "사용자명 또는 이메일", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "비밀번호", example = "password123")
    private String password;
}
