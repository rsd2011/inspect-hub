package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.AccountLockPolicy;
import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.RefreshTokenRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.repository.UserRepository;
import com.inspecthub.common.config.AuthProperties;
import com.inspecthub.common.exception.BusinessException;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 *
 * LOCAL 로그인, JWT 토큰 생성/갱신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final AuthProperties authProperties;
    private final AccountLockPolicy accountLockPolicy;

    /**
     * LOCAL 로그인 인증
     */
    @Transactional
    public TokenResponse authenticate(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> {
                    auditLogService.logLoginFailure(
                            request.getEmployeeId(), "USER_NOT_FOUND", "LOCAL"
                    );
                    return new BusinessException(
                            "AUTH_001",
                            "사용자를 찾을 수 없습니다"
                    );
                });

        // 2. 계정 로그인 가능 여부 확인 (도메인 로직)
        if (!user.canLogin()) {
            if (!user.isActive()) {
                throw new BusinessException(
                        "AUTH_003",
                        "비활성화된 계정입니다"
                );
            }
            if (user.isAccountLocked()) {
                throw new BusinessException(
                        "AUTH_004",
                        "계정이 잠금되었습니다. " +
                                (user.getLockedUntil() != null
                                        ? user.getLockedUntil().getMinute() - LocalDateTime.now().getMinute() + "분 후 다시 시도하세요."
                                        : "관리자에게 문의하세요.")
                );
            }
        }

        // 3. 비밀번호 검증 (도메인 로직)
        if (!user.isPasswordMatch(request.getPassword(), passwordEncoder)) {
            // 도메인: 로그인 실패 기록 (LOCAL만 failedAttempts 증가)
            user.recordLoginFailure();
            
            // 도메인 서비스: 계정 잠금 정책 적용
            accountLockPolicy.applyLockPolicy(user, user.getFailedAttempts());
            
            // 변경사항 저장
            userRepository.save(user);

            auditLogService.logLoginFailure(
                    request.getEmployeeId(), "INVALID_PASSWORD", "LOCAL"
            );

            throw new BusinessException(
                    "AUTH_002",
                    "비밀번호가 일치하지 않습니다"
            );
        }

        // 4. 로그인 성공 처리 (도메인 로직)
        user.recordLoginSuccess();
        userRepository.save(user);

        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 6. 감사 로그 기록
        auditLogService.logLoginSuccess(user.getEmployeeId(), "LOCAL");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(authProperties.getJwt().getAccessToken().getExpirationSeconds())
                .build();
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. Refresh Token 검증
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException(
                        "AUTH_006",
                        "유효하지 않은 Refresh Token입니다."
                );
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException(
                    "AUTH_005",
                    "만료된 Refresh Token입니다. 다시 로그인하세요."
            );
        }

        // 2. Token Type 확인
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(
                    "AUTH_006",
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        // 3. 사용자 조회
        String employeeId = jwtTokenProvider.getEmployeeId(refreshToken);
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(
                        "AUTH_001",
                        "사용자를 찾을 수 없습니다"
                ));

        // 4. 새로운 토큰 생성 (Rotation)
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(authProperties.getJwt().getAccessToken().getExpirationSeconds())
                .build();
    }

}
