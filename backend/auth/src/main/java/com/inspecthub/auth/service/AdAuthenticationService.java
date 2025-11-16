package com.inspecthub.auth.service;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

/**
 * AD (Active Directory) Authentication Service
 *
 * LDAP 기반 AD 인증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdAuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final com.inspecthub.auth.repository.UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    /**
     * AD 인증
     *
     * @param request 로그인 요청 (employeeId, password)
     * @return JWT Access Token + Refresh Token
     */
    public TokenResponse authenticate(LoginRequest request) {
        log.debug("AD 인증 시도: employeeId={}", request.getEmployeeId());

        try {
            // 1. 사용자 조회
            com.inspecthub.auth.domain.User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElse(null);

            // 2. 사용자 상태 검증 (조회된 경우)
            if (user != null) {
                if (!user.isActive()) {
                    log.warn("비활성화된 사용자 로그인 시도: employeeId={}", request.getEmployeeId());
                    throw new com.inspecthub.common.exception.BusinessException("AUTH_003", "비활성화된 계정입니다");
                }
                if (user.isLocked()) {
                    log.warn("잠긴 계정 로그인 시도: employeeId={}", request.getEmployeeId());
                    throw new com.inspecthub.common.exception.BusinessException("AUTH_004", "계정이 잠금되었습니다");
                }
            }

            // 3. LDAP 인증
            try {
                org.springframework.ldap.query.LdapQuery query = org.springframework.ldap.query.LdapQueryBuilder
                    .query()
                    .where("sAMAccountName").is(request.getEmployeeId());
                
                ldapTemplate.authenticate(query, request.getPassword());
                log.debug("LDAP 인증 성공: employeeId={}", request.getEmployeeId());
            } catch (org.springframework.ldap.AuthenticationException e) {
                log.warn("LDAP 인증 실패: employeeId={}", request.getEmployeeId(), e);
                auditLogService.logLoginFailure(request.getEmployeeId(), "INVALID_AD_PASSWORD", "AD");
                throw new com.inspecthub.common.exception.BusinessException("AUTH_002", "비밀번호가 일치하지 않습니다");
            } catch (org.springframework.ldap.CommunicationException e) {
                log.error("AD 서버 연결 실패: employeeId={}", request.getEmployeeId(), e);
                auditLogService.logLoginFailure(request.getEmployeeId(), "AD_CONNECTION_ERROR", "AD");
                throw new com.inspecthub.common.exception.BusinessException("AD_CONNECTION_ERROR", "AD 서버 연결 실패");
            }

            // 4. 사용자가 없으면 자동 생성
            if (user == null) {
                log.info("AD 인증 성공 - 신규 사용자 자동 생성: employeeId={}", request.getEmployeeId());
                
                // TODO: LDAP에서 사용자 정보 조회 (displayName, mail, department 등)
                // 현재는 기본값으로 생성
                user = com.inspecthub.auth.domain.User.builder()
                    .id(com.inspecthub.auth.domain.UserId.generate())
                    .employeeId(request.getEmployeeId())
                    .name("신규사용자") // TODO: LDAP에서 조회
                    .email("new@company.com") // TODO: LDAP에서 조회
                    .password(null) // AD 사용자는 비밀번호 저장 안 함
                    .loginMethod("AD")
                    .active(true)
                    .locked(false)
                    .lockedUntil(null)
                    .failedAttempts(0)
                    .lastLoginAt(null)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
                
                user = userRepository.save(user);
            }

            // 5. 마지막 로그인 시간 업데이트
            userRepository.updateLastLoginAt(user.getId());

            // 6. JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // 7. 감사 로그 기록
            auditLogService.logLoginSuccess(request.getEmployeeId(), "AD");

            log.info("AD 로그인 성공: employeeId={}, userId={}", request.getEmployeeId(), user.getId());

            return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        } catch (com.inspecthub.common.exception.BusinessException e) {
            // BusinessException은 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외
            log.error("AD 로그인 중 예상치 못한 오류 발생: employeeId={}", request.getEmployeeId(), e);
            auditLogService.logLoginFailure(request.getEmployeeId(), "UNKNOWN_ERROR", "AD");
            throw new com.inspecthub.common.exception.BusinessException("AUTH_999", "로그인 처리 중 오류가 발생했습니다");
        }
    }
}
