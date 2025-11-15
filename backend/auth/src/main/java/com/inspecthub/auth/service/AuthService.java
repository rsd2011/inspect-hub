package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.RefreshTokenRequest;
import com.inspecthub.auth.dto.TokenResponse;
import com.inspecthub.auth.repository.UserRepository;
import com.inspecthub.common.config.AuthProperties;
import com.inspecthub.common.exception.BusinessException;
import com.inspecthub.common.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        // 2. 계정 상태 확인 - 비활성화
        if (!user.isActive()) {
            throw new BusinessException(
                    "AUTH_003",
                    "비활성화된 계정입니다"
            );
        }

        // 3. 계정 잠금 확인
        if (user.isLocked()) {
            throw new BusinessException(
                    "AUTH_004",
                    "계정이 잠금되었습니다. " +
                            (user.getLockedUntil() != null
                                    ? user.getLockedUntil().getMinute() - LocalDateTime.now().getMinute() + "분 후 다시 시도하세요."
                                    : "관리자에게 문의하세요.")
            );
        }

        // 4. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 실패 횟수 증가
            userRepository.incrementFailedAttempts(user.getId());

            // 계정 잠금 정책 적용
            int newFailedAttempts = (user.getFailedAttempts() != null ? user.getFailedAttempts() : 0) + 1;
            applyAccountLockPolicy(user.getId(), newFailedAttempts);

            auditLogService.logLoginFailure(
                    request.getEmployeeId(), "INVALID_PASSWORD", "LOCAL"
            );

            throw new BusinessException(
                    "AUTH_002",
                    "비밀번호가 일치하지 않습니다"
            );
        }

        // 5. 로그인 성공 처리
        userRepository.resetFailedAttempts(user.getId());
        userRepository.updateLastLoginAt(user.getId());

        // 6. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 7. 감사 로그 기록
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

    /**
     * 계정 잠금 정책 적용
     *
     * - 5회 실패: 5분 잠금
     * - 10회 실패: 30분 잠금
     * - 15회 실패: 영구 잠금
     */
    private void applyAccountLockPolicy(UserId userId, int failedAttempts) {
        if (failedAttempts >= 15) {
            userRepository.lockAccount(userId, null);  // 영구 잠금
        } else if (failedAttempts >= 10) {
            userRepository.lockAccount(userId, LocalDateTime.now().plusMinutes(30));  // 30분
        } else if (failedAttempts >= 5) {
            userRepository.lockAccount(userId, LocalDateTime.now().plusMinutes(5));   // 5분
        }
    }
}
