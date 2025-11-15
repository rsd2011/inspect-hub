package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.LoginMethod;
import com.inspecthub.auth.domain.LoginPolicy;
import com.inspecthub.auth.repository.LoginPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * LoginPolicyService - 로그인 정책 조회 서비스
 *
 * Jenkins 스타일 전역 로그인 정책 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginPolicyService {

    private final LoginPolicyRepository loginPolicyRepository;

    /**
     * 시스템 전역 로그인 정책 조회
     *
     * @return 로그인 정책
     */
    public LoginPolicy getGlobalPolicy() {
        log.debug("조회: 시스템 전역 로그인 정책");
        return loginPolicyRepository.findGlobalPolicy()
            .orElseThrow(() -> new IllegalStateException("시스템 로그인 정책이 존재하지 않습니다"));
    }

    /**
     * 시스템의 사용 가능한 로그인 방식 리스트 조회
     *
     * @return 활성화된 로그인 방식 Set
     */
    public Set<LoginMethod> getAvailableMethods() {
        LoginPolicy policy = getGlobalPolicy();
        log.debug("사용 가능한 로그인 방식 조회: {}", policy.getEnabledMethods());
        return policy.getEnabledMethods();
    }

    /**
     * 시스템의 최우선 로그인 방식 조회
     *
     * @return 최우선 로그인 방식
     */
    public LoginMethod getPrimaryMethod() {
        LoginPolicy policy = getGlobalPolicy();
        LoginMethod primaryMethod = policy.getPrimaryMethod();
        log.debug("최우선 로그인 방식 조회: {}", primaryMethod);
        return primaryMethod;
    }
}
