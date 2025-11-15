package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 로그인 우선순위 업데이트 요청 DTO
 */
@Schema(description = "로그인 방식 우선순위 업데이트 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriorityRequest {

    @Schema(description = "로그인 방식 우선순위", example = "[\"SSO\", \"AD\", \"LOCAL\"]", required = true)
    @NotEmpty(message = "우선순위는 비어있을 수 없습니다")
    private List<LoginMethod> priority;
}
