package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 로그인 방식 업데이트 요청 DTO
 */
@Schema(description = "활성화된 로그인 방식 업데이트 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMethodsRequest {

    @Schema(description = "활성화된 로그인 방식", example = "[\"SSO\", \"AD\", \"LOCAL\"]", required = true)
    @NotEmpty(message = "최소 1개의 로그인 방식이 활성화되어야 합니다")
    private Set<LoginMethod> enabledMethods;
}
