package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 로그인 정책 전체 업데이트 요청 DTO
 */
@Schema(description = "로그인 정책 전체 업데이트 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLoginPolicyRequest {

    @Schema(description = "정책 이름", example = "시스템 로그인 정책", required = true)
    @NotBlank(message = "정책 이름은 필수입니다")
    @Size(max = 100, message = "정책 이름은 100자를 초과할 수 없습니다")
    private String name;

    @Schema(description = "활성화된 로그인 방식", example = "[\"SSO\", \"AD\", \"LOCAL\"]", required = true)
    @NotEmpty(message = "최소 1개의 로그인 방식이 활성화되어야 합니다")
    private Set<LoginMethod> enabledMethods;

    @Schema(description = "로그인 방식 우선순위", example = "[\"SSO\", \"AD\", \"LOCAL\"]", required = true)
    @NotEmpty(message = "우선순위는 비어있을 수 없습니다")
    private List<LoginMethod> priority;
}
