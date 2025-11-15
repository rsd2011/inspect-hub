package com.inspecthub.admin.systemconfig.dto;

import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 로그인 정책 응답 DTO
 */
@Schema(description = "로그인 정책 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginPolicyResponse {

    @Schema(description = "정책 ID (ULID)", example = "01JCXYZ1234567890ABCDEF002")
    private String id;

    @Schema(description = "정책 이름", example = "시스템 로그인 정책")
    private String name;

    @Schema(description = "활성화된 로그인 방식", example = "[\"SSO\", \"AD\", \"LOCAL\"]")
    private Set<LoginMethod> enabledMethods;

    @Schema(description = "로그인 방식 우선순위", example = "[\"SSO\", \"AD\", \"LOCAL\"]")
    private List<LoginMethod> priority;

    @Schema(description = "활성 상태", example = "true")
    private boolean active;

    @Schema(description = "생성자 ID", example = "SYSTEM")
    private String createdBy;

    @Schema(description = "생성 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정자 ID", example = "ADMIN001")
    private String updatedBy;

    @Schema(description = "수정 일시", example = "2025-01-15T15:45:00")
    private LocalDateTime updatedAt;

    /**
     * Builder 클래스 확장 - 방어적 복사
     */
    public static class LoginPolicyResponseBuilder {
        public LoginPolicyResponseBuilder enabledMethods(Set<LoginMethod> enabledMethods) {
            this.enabledMethods = enabledMethods != null
                ? new LinkedHashSet<>(enabledMethods)
                : new LinkedHashSet<>();
            return this;
        }

        public LoginPolicyResponseBuilder priority(List<LoginMethod> priority) {
            this.priority = priority != null
                ? new ArrayList<>(priority)
                : new ArrayList<>();
            return this;
        }
    }

    /**
     * enabledMethods를 불변 컬렉션으로 반환
     */
    public Set<LoginMethod> getEnabledMethods() {
        return enabledMethods != null
            ? new LinkedHashSet<>(enabledMethods)
            : new LinkedHashSet<>();
    }

    /**
     * priority를 불변 컬렉션으로 반환
     */
    public List<LoginMethod> getPriority() {
        return priority != null
            ? new ArrayList<>(priority)
            : new ArrayList<>();
    }

    /**
     * Entity에서 DTO로 변환
     */
    public static LoginPolicyResponse fromEntity(LoginPolicy policy) {
        return LoginPolicyResponse.builder()
            .id(policy.getId())
            .name(policy.getName())
            .enabledMethods(policy.getEnabledMethods())
            .priority(policy.getPriority())
            .active(policy.isActive())
            .createdBy(policy.getCreatedBy())
            .createdAt(policy.getCreatedAt())
            .updatedBy(policy.getUpdatedBy())
            .updatedAt(policy.getUpdatedAt())
            .build();
    }
}
