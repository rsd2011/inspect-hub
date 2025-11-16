package com.inspecthub.auth.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Aggregate Root
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private UserId id;
    private String employeeId;
    private String name;
    private String email;
    private String password;
    private String loginMethod;  // AD, SSO, LOCAL
    private boolean active;
    private boolean locked;
    private LocalDateTime lockedUntil;
    private Integer failedAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(toBuilder = true)
    public User(
            UserId id,
            String employeeId,
            String name,
            String email,
            String password,
            String loginMethod,
            boolean active,
            boolean locked,
            LocalDateTime lockedUntil,
            Integer failedAttempts,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.loginMethod = loginMethod;
        this.active = active;
        this.locked = locked;
        this.lockedUntil = lockedUntil;
        this.failedAttempts = failedAttempts;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
