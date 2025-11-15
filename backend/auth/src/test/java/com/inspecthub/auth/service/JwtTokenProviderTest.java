package com.inspecthub.auth.service;

import com.inspecthub.auth.domain.User;
import com.inspecthub.auth.domain.UserId;
import com.inspecthub.common.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtTokenProvider TDD Tests
 * 
 * Story 2: JWT Access Token Validation
 * - Token generation with user claims
 * - Token signature validation
 * - Token expiration check
 * - Claims extraction
 */
@DisplayName("JwtTokenProvider - JWT Token Generation & Validation")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private AuthProperties authProperties;
    private User testUser;
    private String validSecretKey;

    @BeforeEach
    void setUp() {
        // Given: JWT 설정
        validSecretKey = "test-secret-key-minimum-256-bits-required-for-HS256-algorithm";

        authProperties = new AuthProperties();
        AuthProperties.JwtConfig jwtConfig = new AuthProperties.JwtConfig();
        AuthProperties.JwtConfig.TokenConfig accessTokenConfig = new AuthProperties.JwtConfig.TokenConfig();
        accessTokenConfig.setExpirationSeconds(3600); // 1 hour

        AuthProperties.JwtConfig.TokenConfig refreshTokenConfig = new AuthProperties.JwtConfig.TokenConfig();
        refreshTokenConfig.setExpirationSeconds(86400); // 24 hours

        jwtConfig.setSecret(validSecretKey);
        jwtConfig.setAccessToken(accessTokenConfig);
        jwtConfig.setRefreshToken(refreshTokenConfig);

        authProperties.setJwt(jwtConfig);

        jwtTokenProvider = new JwtTokenProvider(authProperties);

        // Given: 테스트용 사용자
        testUser = User.builder()
                .id(UserId.of("01ARZ3NDEKTSV4RRFFQ69G5FAV"))
                .employeeId("EMP001")
                .name("홍길동")
                .email("hong@example.com")
                .build();
    }

    @Nested
    @DisplayName("Access Token 생성")
    class AccessTokenGeneration {

        @Test
        @DisplayName("유효한 사용자로 Access Token을 생성한다")
        void shouldGenerateAccessToken_WithValidUser() {
            // When
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // Then: JWT 형식 검증 (header.payload.signature)
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("생성된 Access Token은 사용자 정보를 포함한다")
        void shouldIncludeUserClaims_InAccessToken() {
            // When
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // Then: Claims 추출 및 검증
            Claims claims = jwtTokenProvider.getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("EMP001");
            assertThat(claims.get("userId", String.class)).isEqualTo("01ARZ3NDEKTSV4RRFFQ69G5FAV");
            assertThat(claims.get("name", String.class)).isEqualTo("홍길동");
            assertThat(claims.get("email", String.class)).isEqualTo("hong@example.com");
            assertThat(claims.get("type", String.class)).isEqualTo("access");
        }

        @Test
        @DisplayName("Access Token은 1시간 후 만료된다")
        void shouldExpireAccessToken_After1Hour() {
            // When
            String token = jwtTokenProvider.generateAccessToken(testUser);
            Claims claims = jwtTokenProvider.getClaims(token);

            // Then: 만료 시간 검증 (1시간 = 3600초)
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();
            long durationSeconds = ChronoUnit.SECONDS.between(issuedAt, expiresAt);
            
            assertThat(durationSeconds).isEqualTo(3600);
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("유효한 사용자로 Refresh Token을 생성한다")
        void shouldGenerateRefreshToken_WithValidUser() {
            // When
            String token = jwtTokenProvider.generateRefreshToken(testUser);

            // Then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("생성된 Refresh Token은 최소 정보만 포함한다")
        void shouldIncludeMinimalClaims_InRefreshToken() {
            // When
            String token = jwtTokenProvider.generateRefreshToken(testUser);

            // Then: Refresh Token은 subject와 type만 포함
            Claims claims = jwtTokenProvider.getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("EMP001");
            assertThat(claims.get("type", String.class)).isEqualTo("refresh");
            
            // 보안상 name, email 등 추가 정보는 포함하지 않음
            assertThat(claims.get("name")).isNull();
            assertThat(claims.get("email")).isNull();
        }

        @Test
        @DisplayName("Refresh Token은 24시간 후 만료된다")
        void shouldExpireRefreshToken_After24Hours() {
            // When
            String token = jwtTokenProvider.generateRefreshToken(testUser);
            Claims claims = jwtTokenProvider.getClaims(token);

            // Then: 만료 시간 검증 (24시간 = 86400초)
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();
            long durationSeconds = ChronoUnit.SECONDS.between(issuedAt, expiresAt);
            
            assertThat(durationSeconds).isEqualTo(86400);
        }
    }

    @Nested
    @DisplayName("Token 검증")
    class TokenValidation {

        @Test
        @DisplayName("유효한 토큰을 검증한다")
        void shouldValidateToken_WhenTokenIsValid() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // When
            boolean isValid = jwtTokenProvider.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 서명의 토큰을 거부한다")
        void shouldRejectToken_WhenSignatureIsInvalid() {
            // Given: 다른 secret으로 생성된 토큰
            String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJzdWIiOiJFTVAwMDEiLCJ0eXBlIjoiYWNjZXNzIn0." +
                    "invalid_signature_here";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("잘못된 형식의 토큰을 거부한다")
        void shouldRejectToken_WhenFormatIsInvalid() {
            // Given: JWT 형식이 아닌 문자열
            String malformedToken = "not.a.valid.jwt.token";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformedToken))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("만료된 토큰을 거부한다")
        void shouldRejectToken_WhenExpired() {
            // Given: 만료 시간이 -1초인 토큰 (즉시 만료)
            AuthProperties.JwtConfig.TokenConfig expiredConfig = 
                    new AuthProperties.JwtConfig.TokenConfig();
            expiredConfig.setExpirationSeconds(-1);
            authProperties.getJwt().setAccessToken(expiredConfig);
            
            JwtTokenProvider expiredProvider = new JwtTokenProvider(authProperties);
            String expiredToken = expiredProvider.generateAccessToken(testUser);

            // When & Then: 토큰이 이미 만료됨
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("null 토큰을 거부한다")
        void shouldRejectToken_WhenNull() {
            // When
            boolean isValid = jwtTokenProvider.validateToken(null);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰을 거부한다")
        void shouldRejectToken_WhenEmpty() {
            // When
            boolean isValid = jwtTokenProvider.validateToken("");

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Claims 추출")
    class ClaimsExtraction {

        @Test
        @DisplayName("토큰에서 employeeId를 추출한다")
        void shouldExtractEmployeeId_FromToken() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // When
            String employeeId = jwtTokenProvider.getEmployeeId(token);

            // Then
            assertThat(employeeId).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("토큰에서 userId를 추출한다")
        void shouldExtractUserId_FromToken() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // When
            String userId = jwtTokenProvider.getUserId(token);

            // Then
            assertThat(userId).isEqualTo("01ARZ3NDEKTSV4RRFFQ69G5FAV");
        }

        @Test
        @DisplayName("토큰 타입을 확인한다 - Access Token")
        void shouldIdentifyTokenType_AsAccess() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // When
            String tokenType = jwtTokenProvider.getTokenType(token);

            // Then
            assertThat(tokenType).isEqualTo("access");
        }

        @Test
        @DisplayName("토큰 타입을 확인한다 - Refresh Token")
        void shouldIdentifyTokenType_AsRefresh() {
            // Given
            String token = jwtTokenProvider.generateRefreshToken(testUser);

            // When
            String tokenType = jwtTokenProvider.getTokenType(token);

            // Then
            assertThat(tokenType).isEqualTo("refresh");
        }

        @Test
        @DisplayName("토큰의 만료 여부를 확인한다")
        void shouldCheckTokenExpiration() {
            // Given
            String token = jwtTokenProvider.generateAccessToken(testUser);

            // When
            boolean isExpired = jwtTokenProvider.isTokenExpired(token);

            // Then: 방금 생성된 토큰은 만료되지 않음
            assertThat(isExpired).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Type 검증")
    class TokenTypeValidation {

        @Test
        @DisplayName("Access Token 타입이 맞는지 검증한다")
        void shouldValidateAccessTokenType() {
            // Given
            String accessToken = jwtTokenProvider.generateAccessToken(testUser);

            // When
            boolean isAccessToken = jwtTokenProvider.isAccessToken(accessToken);

            // Then
            assertThat(isAccessToken).isTrue();
        }

        @Test
        @DisplayName("Refresh Token 타입이 맞는지 검증한다")
        void shouldValidateRefreshTokenType() {
            // Given
            String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);

            // When
            boolean isRefreshToken = jwtTokenProvider.isRefreshToken(refreshToken);

            // Then
            assertThat(isRefreshToken).isTrue();
        }

        @Test
        @DisplayName("Access Token을 Refresh Token으로 사용할 수 없다")
        void shouldRejectAccessToken_AsRefreshToken() {
            // Given
            String accessToken = jwtTokenProvider.generateAccessToken(testUser);

            // When
            boolean isRefreshToken = jwtTokenProvider.isRefreshToken(accessToken);

            // Then
            assertThat(isRefreshToken).isFalse();
        }

        @Test
        @DisplayName("Refresh Token을 Access Token으로 사용할 수 없다")
        void shouldRejectRefreshToken_AsAccessToken() {
            // Given
            String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);

            // When
            boolean isAccessToken = jwtTokenProvider.isAccessToken(refreshToken);

            // Then
            assertThat(isAccessToken).isFalse();
        }
    }
}
