package com.inspecthub.admin.loginpolicy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.loginpolicy.domain.LoginMethod;
import com.inspecthub.admin.loginpolicy.domain.LoginPolicy;
import com.inspecthub.admin.loginpolicy.exception.EmptyMethodsException;
import com.inspecthub.admin.loginpolicy.exception.PolicyNotFoundException;
import com.inspecthub.admin.loginpolicy.repository.LoginPolicyRepository;
import com.inspecthub.common.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * 시스템 전역 로그인 정책 조회
     *
     * Redis 캐시 사용:
     * - Cache Key: "system:login-policy"
     * - TTL: 1시간 (application.yml 설정)
     * - Null 값 캐싱 안 함
     *
     * @return 로그인 정책
     */
    @Cacheable(value = "system:login-policy", unless = "#result == null")
    public LoginPolicy getGlobalPolicy() {
        log.debug("조회: 시스템 전역 로그인 정책 (DB)");
        return loginPolicyRepository.findGlobalPolicy()
            .orElseThrow(() -> new PolicyNotFoundException("로그인 정책을 찾을 수 없습니다"));
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

    /**
     * 시스템 전역 로그인 정책 전체 업데이트
     *
     * 캐시 무효화: 업데이트 후 캐시 삭제
     *
     * @param name 정책 이름
     * @param enabledMethods 활성화된 로그인 방식
     * @param priority 로그인 방식 우선순위
     * @return 업데이트된 로그인 정책
     */
    @CacheEvict(value = "system:login-policy", allEntries = true)
    public LoginPolicy updateGlobalPolicy(
        String name,
        Set<LoginMethod> enabledMethods,
        List<LoginMethod> priority
    ) {
        log.debug("업데이트: 시스템 전역 로그인 정책 - name={}, methods={}, priority={}",
            name, enabledMethods, priority);

        // Validation: 최소 1개의 로그인 방식 필요
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            throw new EmptyMethodsException();
        }

        LoginPolicy current = getGlobalPolicy();
        String beforeJson = toJson(current);

        // 새로운 정책 생성 (ID는 유지)
        LoginPolicy updated = LoginPolicy.builder()
            .id(current.getId())
            .name(name)
            .enabledMethods(enabledMethods)
            .priority(priority)
            .active(current.isActive())
            .createdBy(current.getCreatedBy())
            .createdAt(current.getCreatedAt())
            .updatedBy("SYSTEM")  // TODO: SecurityContext에서 가져오기
            .updatedAt(LocalDateTime.now())
            .build();

        loginPolicyRepository.save(updated);
        String afterJson = toJson(updated);

        // Audit logging (비동기, 실패해도 main flow 방해 안 함)
        auditLogService.logPolicyChange(
            "SYSTEM_LOGIN_POLICY_UPDATED",
            "SYSTEM",  // TODO: SecurityContext에서 가져오기
            beforeJson,
            afterJson
        );

        log.info("업데이트 완료: 시스템 전역 로그인 정책 - id={}", updated.getId());

        return updated;
    }

    /**
     * 활성화된 로그인 방식만 업데이트
     *
     * 캐시 무효화: 업데이트 후 캐시 삭제
     *
     * @param enabledMethods 활성화된 로그인 방식
     * @return 업데이트된 로그인 정책
     */
    @CacheEvict(value = "system:login-policy", allEntries = true)
    public LoginPolicy updateEnabledMethods(Set<LoginMethod> enabledMethods) {
        log.debug("업데이트: 활성화된 로그인 방식 - methods={}", enabledMethods);

        // Validation: 최소 1개의 로그인 방식 필요
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            throw new EmptyMethodsException();
        }

        LoginPolicy current = getGlobalPolicy();
        String beforeJson = toJson(current);

        // enabledMethods 변경 시 priority도 자동 조정
        List<LoginMethod> newPriority = current.getPriority().stream()
            .filter(enabledMethods::contains)
            .toList();

        LoginPolicy updated = LoginPolicy.builder()
            .id(current.getId())
            .name(current.getName())
            .enabledMethods(enabledMethods)
            .priority(newPriority)
            .active(current.isActive())
            .createdBy(current.getCreatedBy())
            .createdAt(current.getCreatedAt())
            .updatedBy("SYSTEM")  // TODO: SecurityContext에서 가져오기
            .updatedAt(LocalDateTime.now())
            .build();

        loginPolicyRepository.save(updated);
        String afterJson = toJson(updated);

        // Audit logging
        auditLogService.logPolicyChange(
            "LOGIN_METHOD_ENABLED",
            "SYSTEM",
            beforeJson,
            afterJson
        );

        log.info("업데이트 완료: 활성화된 로그인 방식 - id={}, methods={}", updated.getId(), enabledMethods);

        return updated;
    }

    /**
     * 로그인 방식 우선순위만 업데이트
     *
     * 캐시 무효화: 업데이트 후 캐시 삭제
     *
     * @param priority 로그인 방식 우선순위
     * @return 업데이트된 로그인 정책
     */
    @CacheEvict(value = "system:login-policy", allEntries = true)
    public LoginPolicy updatePriority(List<LoginMethod> priority) {
        log.debug("업데이트: 로그인 방식 우선순위 - priority={}", priority);

        LoginPolicy current = getGlobalPolicy();
        String beforeJson = toJson(current);

        LoginPolicy updated = LoginPolicy.builder()
            .id(current.getId())
            .name(current.getName())
            .enabledMethods(current.getEnabledMethods())
            .priority(priority)
            .active(current.isActive())
            .createdBy(current.getCreatedBy())
            .createdAt(current.getCreatedAt())
            .updatedBy("SYSTEM")  // TODO: SecurityContext에서 가져오기
            .updatedAt(LocalDateTime.now())
            .build();

        loginPolicyRepository.save(updated);
        String afterJson = toJson(updated);

        // Audit logging
        auditLogService.logPolicyChange(
            "LOGIN_PRIORITY_UPDATED",
            "SYSTEM",
            beforeJson,
            afterJson
        );

        log.info("업데이트 완료: 로그인 방식 우선순위 - id={}, priority={}", updated.getId(), priority);

        return updated;
    }

    /**
     * LoginPolicy를 JSON 문자열로 변환
     */
    private String toJson(LoginPolicy policy) {
        try {
            return objectMapper.writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            log.warn("LoginPolicy JSON 변환 실패", e);
            return "{}";
        }
    }
}
