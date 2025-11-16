package com.inspecthub.auth.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * User Aggregate Root
 *
 * DDD Rich Domain Model:
 * - 비즈니스 로직을 도메인 객체 내부에 캡슐화
 * - 불변성 보장 (Setter 없음, 도메인 메서드로만 상태 변경)
 * - 도메인 규칙을 코드로 표현
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
        this.failedAttempts = failedAttempts != null ? failedAttempts : 0;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ==================== 도메인 로직 ====================

    /**
     * 로그인 가능 여부 확인
     *
     * 도메인 규칙:
     * - 활성화된 계정이어야 함
     * - 잠금되지 않았거나, 잠금 기간이 만료되어야 함
     */
    public boolean canLogin() {
        if (!active) {
            return false;
        }

        if (locked && lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return false;
        }

        // 잠금 기간이 만료된 경우 자동 해제
        if (locked && lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            this.locked = false;
            this.lockedUntil = null;
        }

        return true;
    }

    /**
     * 계정 잠금 상태 확인 (시간 기반)
     */
    public boolean isAccountLocked() {
        return locked && lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * 비밀번호 일치 여부 확인 (LOCAL 로그인 전용)
     *
     * @param rawPassword 평문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return 일치 여부
     */
    public boolean isPasswordMatch(String rawPassword, PasswordEncoder passwordEncoder) {
        if (!isLocalLogin()) {
            throw new IllegalStateException("AD/SSO 사용자는 비밀번호 검증 불가");
        }
        return passwordEncoder.matches(rawPassword, this.password);
    }

    /**
     * LOCAL 로그인 방식 확인
     */
    public boolean isLocalLogin() {
        return "LOCAL".equals(this.loginMethod);
    }

    /**
     * 로그인 성공 기록
     *
     * 도메인 규칙:
     * - 실패 횟수 리셋
     * - 계정 잠금 해제
     * - 마지막 로그인 시간 업데이트
     */
    public void recordLoginSuccess() {
        this.failedAttempts = 0;
        this.locked = false;
        this.lockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 로그인 실패 기록 (LOCAL 전용)
     *
     * 도메인 규칙:
     * - AD/SSO 로그인은 실패 카운트 증가 안 함 (정책 격리)
     * - LOCAL 로그인만 실패 횟수 증가
     */
    public void recordLoginFailure() {
        if (!isLocalLogin()) {
            // AD/SSO는 실패 카운트 증가 안 함
            return;
        }

        this.failedAttempts = (this.failedAttempts != null ? this.failedAttempts : 0) + 1;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 잠금
     *
     * @param duration 잠금 기간 (분)
     */
    public void lock(int durationMinutes) {
        this.locked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 영구 잠금
     */
    public void lockPermanently() {
        this.locked = true;
        this.lockedUntil = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 잠금 해제
     */
    public void unlock() {
        this.locked = false;
        this.lockedUntil = null;
        this.failedAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 정보 업데이트 (AD/SSO 동기화용)
     */
    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Factory Method: LOCAL 사용자 생성
     */
    public static User createLocalUser(
            String employeeId,
            String name,
            String email,
            String encodedPassword
    ) {
        return User.builder()
                .id(UserId.generate())
                .employeeId(employeeId)
                .name(name)
                .email(email)
                .password(encodedPassword)
                .loginMethod("LOCAL")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Factory Method: AD 사용자 생성
     */
    public static User createAdUser(
            String employeeId,
            String name,
            String email
    ) {
        return User.builder()
                .id(UserId.generate())
                .employeeId(employeeId)
                .name(name)
                .email(email)
                .password(null)  // AD 사용자는 비밀번호 저장 안 함
                .loginMethod("AD")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Factory Method: SSO 사용자 생성
     */
    public static User createSsoUser(
            String employeeId,
            String name,
            String email
    ) {
        return User.builder()
                .id(UserId.generate())
                .employeeId(employeeId)
                .name(name)
                .email(email)
                .password(null)  // SSO 사용자는 비밀번호 저장 안 함
                .loginMethod("SSO")
                .active(true)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
