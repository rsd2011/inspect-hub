package com.inspecthub.auth.domain;

import org.springframework.stereotype.Component;

/**
 * Account Lock Policy (Domain Service)
 *
 * 도메인 서비스:
 * - 여러 엔티티를 조합하거나, 엔티티에 속하지 않는 비즈니스 로직
 * - 계정 잠금 정책 결정 (5회→5분, 10회→30분, 15회→영구)
 */
@Component
public class AccountLockPolicy {

    /**
     * 실패 횟수에 따른 잠금 정책 적용
     *
     * 정책:
     * - 5회 실패: 5분 잠금
     * - 10회 실패: 30분 잠금
     * - 15회 이상: 영구 잠금
     *
     * @param user 사용자
     * @param failedAttempts 실패 횟수
     */
    public void applyLockPolicy(User user, int failedAttempts) {
        if (failedAttempts >= 15) {
            user.lockPermanently();
        } else if (failedAttempts >= 10) {
            user.lock(30);  // 30분
        } else if (failedAttempts >= 5) {
            user.lock(5);   // 5분
        }
    }

    /**
     * 잠금 해제 가능 여부 확인
     */
    public boolean canUnlock(User user) {
        // 영구 잠금은 관리자만 해제 가능
        return !user.isAccountLocked()
            || (user.getLockedUntil() != null
                && user.getLockedUntil().getYear() < 9999);
    }
}
