package com.inspecthub.auth.service;

import com.inspecthub.auth.dto.LoginRequest;
import com.inspecthub.auth.dto.LoginResponse;
import com.inspecthub.auth.dto.SSOLoginRequest;
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
     * 전통적인 로그인 처리 (username/password)
     */
    public LoginResponse login(LoginRequest request) {
        // TODO: DB에서 사용자 조회 (admin 모듈과 연계)
        // For now, use hardcoded test user
        String testUsername = "admin";
        String testPassword = "$2a$10$YourHashedPasswordHere"; // BCrypt hash of "admin123"
        String testUserId = "USER-001";
        List<String> testRoles = List.of("ROLE_ADMIN", "ROLE_USER");
        List<String> testPermissions = List.of("USER_READ", "USER_WRITE", "CASE_READ", "CASE_WRITE");

        // For demo purposes, accept any password temporarily
        // TODO: Replace with actual DB lookup and password verification
        if (!request.getUsername().equals(testUsername)) {
            log.warn("Traditional login failed for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // TODO: Uncomment when real user data is available
        // if (!passwordEncoder.matches(request.getPassword(), testPassword)) {
        //     throw new InvalidCredentialsException("Invalid username or password");
        // }

        String accessToken = jwtTokenProvider.createAccessToken(testUserId, testUsername, testRoles);
        String refreshToken = jwtTokenProvider.createRefreshToken(testUserId);

        log.info("User logged in successfully via traditional method: {}", testUsername);

        return LoginResponse.builder()
                .userId(testUserId)
                .username(testUsername)
                .email("admin@inspecthub.com")
                .displayName("System Administrator")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(testRoles)
                .permissions(testPermissions)
                .organizationId("ORG-001")
                .organizationName("Inspect-Hub Inc.")
                .expiresIn(3600000L)  // 1 hour
                .tokenType("Bearer")
                .loginMethod("traditional")
                .build();
    }

    /**
     * SSO 로그인 처리
     */
    public LoginResponse loginSSO(SSOLoginRequest request) {
        // TODO: SSO 토큰 검증 (외부 SSO 라이브러리 사용 예정)
        // For now, accept any SSO token and return test user data

        String ssoToken = request.getSsoToken();
        String provider = request.getProvider() != null ? request.getProvider() : "default";

        log.info("SSO login request with provider: {}", provider);

        // TODO: Validate SSO token with external SSO provider
        // This is where the SSO library will be integrated
        // For now, simulate successful SSO validation

        if (ssoToken == null || ssoToken.isBlank()) {
            throw new InvalidCredentialsException("Invalid SSO token");
        }

        // TODO: Extract user information from validated SSO token
        // For now, use hardcoded test SSO user
        String testUserId = "SSO-USER-001";
        String testUsername = "sso.user";
        String testEmail = "sso.user@partner.com";
        String testDisplayName = "SSO Test User";
        List<String> testRoles = List.of("ROLE_USER");
        List<String> testPermissions = List.of("CASE_READ", "REPORT_READ");
        String testOrgId = "ORG-PARTNER-001";
        String testOrgName = "Partner Organization";

        String accessToken = jwtTokenProvider.createAccessToken(testUserId, testUsername, testRoles);
        String refreshToken = jwtTokenProvider.createRefreshToken(testUserId);

        log.info("User logged in successfully via SSO: {} (provider: {})", testUsername, provider);

        return LoginResponse.builder()
                .userId(testUserId)
                .username(testUsername)
                .email(testEmail)
                .displayName(testDisplayName)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(testRoles)
                .permissions(testPermissions)
                .organizationId(testOrgId)
                .organizationName(testOrgName)
                .expiresIn(3600000L)  // 1 hour
                .tokenType("Bearer")
                .loginMethod("sso")
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
