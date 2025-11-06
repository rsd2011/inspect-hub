package com.inspecthub.auth.service;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.TokenRefreshRequest;
import com.inspecthub.auth.dto.TokenRefreshResponse;
import com.inspecthub.auth.exception.InvalidCredentialsException;
import com.inspecthub.auth.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    // TODO: UserService or UserRepository will be injected from admin module

    /**
     * 로그인 처리
     */
    public LoginResponse login(LoginRequest request) {
        // TODO: DB에서 사용자 조회 (admin 모듈과 연계)
        // For now, use hardcoded test user
        String testUsername = "admin";
        String testPassword = "$2a$10$YourHashedPasswordHere"; // BCrypt hash of "admin123"
        String testUserId = "USER-001";
        List<String> testRoles = List.of("ROLE_ADMIN", "ROLE_USER");

        // For demo purposes, accept any password temporarily
        // TODO: Replace with actual DB lookup and password verification
        if (!request.getUsername().equals(testUsername)) {
            log.warn("Login failed for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // TODO: Uncomment when real user data is available
        // if (!passwordEncoder.matches(request.getPassword(), testPassword)) {
        //     throw new InvalidCredentialsException("Invalid username or password");
        // }

        String accessToken = jwtTokenProvider.createAccessToken(testUserId, testUsername, testRoles);
        String refreshToken = jwtTokenProvider.createRefreshToken(testUserId);

        log.info("User logged in successfully: {}", testUsername);

        return LoginResponse.builder()
                .userId(testUserId)
                .username(testUsername)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(testRoles)
                .expiresIn(3600000L)  // 1 hour
                .build();
    }

    /**
     * 토큰 갱신
     */
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenExpiredException("Refresh token is invalid or expired");
        }

        String userId = jwtTokenProvider.getUserId(refreshToken);

        // TODO: DB에서 사용자 정보 조회하여 새로운 access token 생성
        // For now, use hardcoded values
        String username = "admin";
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, username, roles);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        log.info("Token refreshed for user: {}", userId);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600000L)
                .build();
    }

    /**
     * 로그아웃 처리
     */
    public void logout(String userId) {
        // TODO: Refresh token을 Redis blacklist에 추가하거나 DB에서 제거
        log.info("User logged out: {}", userId);
    }
}
