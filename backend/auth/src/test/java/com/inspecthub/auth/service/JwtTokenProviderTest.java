package com.inspecthub.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secretKey;
    private long accessTokenValidityInMilliseconds;
    private long refreshTokenValidityInMilliseconds;

    @BeforeEach
    void setUp() {
        secretKey = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long";
        accessTokenValidityInMilliseconds = 3600000L; // 1 hour
        refreshTokenValidityInMilliseconds = 86400000L; // 24 hours

        jwtTokenProvider = new JwtTokenProvider(
                secretKey,
                accessTokenValidityInMilliseconds,
                refreshTokenValidityInMilliseconds
        );
    }

    @Nested
    @DisplayName("Access Token 생성 테스트")
    class CreateAccessTokenTests {

        @Test
        @DisplayName("유효한 정보로 Access Token을 생성할 수 있다")
        void createAccessToken_WithValidInfo_ReturnsToken() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

            // when
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
        }

        @Test
        @DisplayName("생성된 Access Token에서 userId를 추출할 수 있다")
        void createAccessToken_ExtractUserId_ReturnsCorrectUserId() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER");

            // when
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);
            String extractedUserId = jwtTokenProvider.getUserId(token);

            // then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("생성된 Access Token에서 username을 추출할 수 있다")
        void createAccessToken_ExtractUsername_ReturnsCorrectUsername() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER");

            // when
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);
            String extractedUsername = jwtTokenProvider.getUsername(token);

            // then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("생성된 Access Token에서 roles를 추출할 수 있다")
        void createAccessToken_ExtractRoles_ReturnsCorrectRoles() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

            // when
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);
            List<String> extractedRoles = jwtTokenProvider.getRoles(token);

            // then
            assertThat(extractedRoles).containsExactlyInAnyOrderElementsOf(roles);
        }

        @Test
        @DisplayName("빈 roles 리스트로 Access Token을 생성할 수 있다")
        void createAccessToken_WithEmptyRoles_ReturnsToken() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList();

            // when
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);

            // then
            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.getRoles(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성 테스트")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("유효한 userId로 Refresh Token을 생성할 수 있다")
        void createRefreshToken_WithValidUserId_ReturnsToken() {
            // given
            String userId = "user123";

            // when
            String token = jwtTokenProvider.createRefreshToken(userId);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("생성된 Refresh Token에서 userId를 추출할 수 있다")
        void createRefreshToken_ExtractUserId_ReturnsCorrectUserId() {
            // given
            String userId = "user123";

            // when
            String token = jwtTokenProvider.createRefreshToken(userId);
            String extractedUserId = jwtTokenProvider.getUserId(token);

            // then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("Refresh Token의 유효기간은 Access Token보다 길다")
        void createRefreshToken_ExpirationTime_IsLongerThanAccessToken() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER");

            // when
            String accessToken = jwtTokenProvider.createAccessToken(userId, username, roles);
            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims accessClaims = Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload();
            Claims refreshClaims = Jwts.parser().verifyWith(key).build().parseSignedClaims(refreshToken).getPayload();

            // then
            assertThat(refreshClaims.getExpiration().getTime())
                    .isGreaterThan(accessClaims.getExpiration().getTime());
        }
    }

    @Nested
    @DisplayName("Token 검증 테스트")
    class ValidateTokenTests {

        @Test
        @DisplayName("유효한 Access Token을 검증할 수 있다")
        void validateToken_WithValidAccessToken_ReturnsTrue() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER");
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효한 Refresh Token을 검증할 수 있다")
        void validateToken_WithValidRefreshToken_ReturnsTrue() {
            // given
            String userId = "user123";
            String token = jwtTokenProvider.createRefreshToken(userId);

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 검증에 실패한다")
        void validateToken_WithExpiredToken_ReturnsFalse() {
            // given
            String userId = "user123";
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            String expiredToken = Jwts.builder()
                    .subject(userId)
                    .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                    .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                    .signWith(key)
                    .compact();

            // when
            boolean isValid = jwtTokenProvider.validateToken(expiredToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 검증에 실패한다")
        void validateToken_WithMalformedToken_ReturnsFalse() {
            // given
            String malformedToken = "invalid.token.format";

            // when
            boolean isValid = jwtTokenProvider.validateToken(malformedToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("잘못된 서명의 토큰은 검증에 실패한다")
        void validateToken_WithInvalidSignature_ReturnsFalse() {
            // given
            String differentSecretKey = "different-secret-key-for-jwt-token-generation-must-be-at-least-256-bits";
            SecretKey key = Keys.hmacShaKeyFor(differentSecretKey.getBytes(StandardCharsets.UTF_8));

            String tokenWithDifferentSignature = Jwts.builder()
                    .subject("user123")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(key)
                    .compact();

            // when
            boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSignature);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null 토큰은 검증에 실패한다")
        void validateToken_WithNullToken_ReturnsFalse() {
            // when
            boolean isValid = jwtTokenProvider.validateToken(null);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 검증에 실패한다")
        void validateToken_WithEmptyToken_ReturnsFalse() {
            // when
            boolean isValid = jwtTokenProvider.validateToken("");

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Claims 추출 테스트")
    class ExtractClaimsTests {

        @Test
        @DisplayName("유효하지 않은 토큰에서 userId 추출 시 예외가 발생한다")
        void getUserId_WithInvalidToken_ThrowsException() {
            // given
            String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.getUserId(invalidToken))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("유효하지 않은 토큰에서 username 추출 시 예외가 발생한다")
        void getUsername_WithInvalidToken_ThrowsException() {
            // given
            String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.getUsername(invalidToken))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("유효하지 않은 토큰에서 roles 추출 시 예외가 발생한다")
        void getRoles_WithInvalidToken_ThrowsException() {
            // given
            String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.getRoles(invalidToken))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("roles가 없는 토큰에서 roles 추출 시 null을 반환한다")
        void getRoles_WithTokenWithoutRoles_ReturnsNull() {
            // given
            String userId = "user123";
            String token = jwtTokenProvider.createRefreshToken(userId); // refresh token에는 roles가 없음

            // when
            List<String> roles = jwtTokenProvider.getRoles(token);

            // then
            assertThat(roles).isNull();
        }
    }

    @Nested
    @DisplayName("토큰 유효기간 테스트")
    class TokenExpirationTests {

        @Test
        @DisplayName("Access Token의 유효기간은 설정값과 일치한다")
        void accessToken_ExpirationTime_MatchesConfiguration() {
            // given
            String userId = "user123";
            String username = "testuser";
            List<String> roles = Arrays.asList("ROLE_USER");

            // when
            long beforeCreation = System.currentTimeMillis();
            String token = jwtTokenProvider.createAccessToken(userId, username, roles);
            long afterCreation = System.currentTimeMillis();

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            long expirationTime = claims.getExpiration().getTime();

            // then
            assertThat(expirationTime).isBetween(
                    beforeCreation + accessTokenValidityInMilliseconds - 1000,
                    afterCreation + accessTokenValidityInMilliseconds + 1000
            );
        }

        @Test
        @DisplayName("Refresh Token의 유효기간은 설정값과 일치한다")
        void refreshToken_ExpirationTime_MatchesConfiguration() {
            // given
            String userId = "user123";

            // when
            long beforeCreation = System.currentTimeMillis();
            String token = jwtTokenProvider.createRefreshToken(userId);
            long afterCreation = System.currentTimeMillis();

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            long expirationTime = claims.getExpiration().getTime();

            // then
            assertThat(expirationTime).isBetween(
                    beforeCreation + refreshTokenValidityInMilliseconds - 1000,
                    afterCreation + refreshTokenValidityInMilliseconds + 1000
            );
        }
    }
}
