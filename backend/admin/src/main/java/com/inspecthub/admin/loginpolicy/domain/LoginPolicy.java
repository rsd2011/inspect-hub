package com.inspecthub.admin.loginpolicy.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 로그인 정책 Aggregate Root
 *
 * Jenkins 스타일 전역 로그인 정책을 관리합니다.
 * - 활성화된 로그인 방식 (SSO, AD, LOCAL)
 * - 우선순위 (기본: SSO > AD > LOCAL)
 * - 시스템 전체에 단일 정책만 존재
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginPolicy {

    /**
     * 정책 ID (ULID)
     */
    private String id;

    /**
     * 정책 이름
     */
    private String name;

    /**
     * 활성화된 로그인 방식
     */
    private Set<LoginMethod> enabledMethods = new LinkedHashSet<>();

    /**
     * 로그인 방식 우선순위
     * 기본값: SSO > AD > LOCAL
     */
    private List<LoginMethod> priority = new ArrayList<>();

    /**
     * 활성 상태
     */
    private boolean active = true;

    /**
     * 생성자 ID
     */
    private String createdBy;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정자 ID
     */
    private String updatedBy;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * Private constructor for builder
     */
    private LoginPolicy(LoginPolicyBuilder builder) {
        // 검증: name 필수
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다");
        }

        // 검증: enabledMethods 필수
        if (builder.enabledMethods == null) {
            throw new IllegalArgumentException("enabledMethods는 null일 수 없습니다");
        }

        // 검증: enabledMethods 최소 1개
        if (builder.enabledMethods.isEmpty()) {
            throw new IllegalArgumentException("최소 1개의 로그인 방식이 활성화되어야 합니다");
        }

        this.id = builder.id;
        this.name = builder.name;
        this.enabledMethods = new LinkedHashSet<>(builder.enabledMethods);

        // 우선순위 설정
        if (builder.priority != null && !builder.priority.isEmpty()) {
            this.priority = new ArrayList<>(builder.priority);
        } else {
            this.priority = buildDefaultPriority(this.enabledMethods);
        }

        this.active = builder.active;
        this.createdBy = builder.createdBy;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedBy = builder.updatedBy;
        this.updatedAt = builder.updatedAt;
    }

    /**
     * enabledMethods 기반으로 기본 우선순위 생성
     */
    private List<LoginMethod> buildDefaultPriority(Set<LoginMethod> enabledMethods) {
        // 기본 우선순위에서 활성화된 방식만 필터링
        return Arrays.stream(LoginMethod.values())
            .filter(enabledMethods::contains)
            .collect(Collectors.toList());
    }

    /**
     * Builder 패턴
     */
    public static LoginPolicyBuilder builder() {
        return new LoginPolicyBuilder();
    }

    public static class LoginPolicyBuilder {
        private String id;
        private String name;
        private Set<LoginMethod> enabledMethods = new LinkedHashSet<>();
        private List<LoginMethod> priority;
        private boolean active = true;
        private String createdBy;
        private LocalDateTime createdAt;
        private String updatedBy;
        private LocalDateTime updatedAt;

        public LoginPolicyBuilder id(String id) {
            this.id = id;
            return this;
        }

        public LoginPolicyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LoginPolicyBuilder enabledMethods(Set<LoginMethod> enabledMethods) {
            this.enabledMethods = enabledMethods;
            return this;
        }

        public LoginPolicyBuilder priority(List<LoginMethod> priority) {
            this.priority = priority;
            return this;
        }

        public LoginPolicyBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public LoginPolicyBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public LoginPolicyBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LoginPolicyBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public LoginPolicyBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public LoginPolicy build() {
            return new LoginPolicy(this);
        }
    }

    /**
     * 특정 로그인 방식이 활성화되어 있는지 확인
     */
    public boolean isMethodEnabled(LoginMethod method) {
        return enabledMethods.contains(method);
    }

    /**
     * 최우선 로그인 방식 조회
     */
    public LoginMethod getPrimaryMethod() {
        return priority.stream()
            .filter(this::isMethodEnabled)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("활성화된 로그인 방식이 없습니다"));
    }

    /**
     * 특정 로그인 방식 비활성화
     */
    public void disableMethod(LoginMethod method) {
        if (enabledMethods.size() == 1 && enabledMethods.contains(method)) {
            throw new IllegalArgumentException("최소 1개의 로그인 방식이 활성화되어야 합니다");
        }
        enabledMethods.remove(method);
        updatedAt = LocalDateTime.now();
    }

    /**
     * 특정 로그인 방식 활성화
     */
    public void enableMethod(LoginMethod method) {
        enabledMethods.add(method);
        updatedAt = LocalDateTime.now();
    }

    /**
     * 우선순위 변경
     */
    public void updatePriority(List<LoginMethod> newPriority) {
        if (newPriority == null || newPriority.isEmpty()) {
            throw new IllegalArgumentException("우선순위는 비어있을 수 없습니다");
        }

        // 우선순위의 모든 항목이 활성화된 방식이어야 함
        Set<LoginMethod> prioritySet = new HashSet<>(newPriority);
        if (!enabledMethods.containsAll(prioritySet)) {
            throw new IllegalArgumentException("우선순위에 비활성화된 로그인 방식이 포함되어 있습니다");
        }

        this.priority = new ArrayList<>(newPriority);
        this.updatedAt = LocalDateTime.now();
    }
}
