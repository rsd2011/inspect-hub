package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.LoginPolicy;
import com.inspecthub.auth.repository.LoginPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LoginPolicyService - 로그인 정책 조회 서비스
 *
 * 조직별 또는 글로벌 로그인 정책 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginPolicyService {

    private final LoginPolicyRepository loginPolicyRepository;

    /**
     * 조직별 로그인 정책 조회
     *
     * - orgId가 null이면 글로벌 정책 조회
     * - orgId가 있으면 조직 정책 조회
     * - 조직 정책이 없으면 글로벌 정책으로 Fallback
     *
     * @param orgId 조직 ID (null = 글로벌)
     * @return 로그인 정책
     */
    public LoginPolicy getPolicyByOrg(String orgId) {
        // orgId가 null이면 글로벌 정책 조회
        if (orgId == null) {
            log.debug("조회: 글로벌 로그인 정책");
            return loginPolicyRepository.findGlobalPolicy()
                .orElseThrow(() -> new IllegalStateException("글로벌 로그인 정책이 존재하지 않습니다"));
        }

        // 조직 정책 조회
        log.debug("조회: 조직별 로그인 정책 (orgId={})", orgId);
        return loginPolicyRepository.findByOrgId(orgId)
            .orElseGet(() -> {
                // 조직 정책 없으면 글로벌 정책으로 Fallback
                log.debug("조직 정책 없음, 글로벌 정책으로 Fallback (orgId={})", orgId);
                return loginPolicyRepository.findGlobalPolicy()
                    .orElseThrow(() -> new IllegalStateException("글로벌 로그인 정책이 존재하지 않습니다"));
            });
    }
}
