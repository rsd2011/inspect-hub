package com.inspecthub.auth.filter;

import com.inspecthub.auth.service.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 *
 * Spring Security FilterChain에서 요청마다 JWT 토큰을 검증하고
 * 유효한 토큰인 경우 SecurityContext에 Authentication 설정
 *
 * OncePerRequestFilter를 상속하여 요청당 한 번만 실행 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 특정 경로는 JWT 필터를 건너뛰기
     *
     * 로그인, 토큰 갱신 등 인증이 필요 없는 엔드포인트는 필터 적용 제외
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // /api/v1/auth/** 경로는 필터 건너뛰기 (SecurityConfig에서 접근 제어)
        return path.startsWith("/api/v1/auth/") ||  
               path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            // 2. 토큰이 존재하고 유효한 경우
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. Access Token인지 확인 (Refresh Token은 인증에 사용 불가)
                if (jwtTokenProvider.isAccessToken(token)) {
                    // 4. EmployeeId 추출
                    String employeeId = jwtTokenProvider.getEmployeeId(token);
                    String userId = jwtTokenProvider.getUserId(token);

                    // 5. SecurityContext에 인증 정보 설정
                    setAuthentication(request, employeeId, userId);

                    log.debug("JWT authentication successful for employeeId: {}", employeeId);
                } else {
                    log.warn("Refresh token cannot be used for authentication");
                }
            }

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            request.setAttribute("exception", "TOKEN_EXPIRED");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            request.setAttribute("exception", "INVALID_SIGNATURE");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            request.setAttribute("exception", "MALFORMED_TOKEN");
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
            request.setAttribute("exception", "AUTHENTICATION_ERROR");
        }

        // 6. 다음 필터로 진행 (인증 실패해도 계속 진행 - SecurityConfig에서 접근 제어)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     *
     * Authorization: Bearer {token} 형식
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * SecurityContext에 인증 정보 설정
     *
     * UsernamePasswordAuthenticationToken 사용
     * - Principal: employeeId
     * - Credentials: null (토큰 기반 인증이므로 비밀번호 불필요)
     * - Authorities: 기본 "ROLE_USER" (추후 UserDetailsService에서 실제 권한 로드)
     */
    private void setAuthentication(
            HttpServletRequest request,
            String employeeId,
            String userId
    ) {
        // 간단한 Principal 객체 (employeeId + userId)
        JwtPrincipal principal = new JwtPrincipal(employeeId, userId);

        // 기본 권한 설정 (추후 실제 권한으로 교체 필요)
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Authentication 토큰 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        // Request details 설정
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * JWT Principal
     *
     * SecurityContext에 저장할 Principal 객체
     * - employeeId와 userId를 함께 저장
     */
    public record JwtPrincipal(String employeeId, String userId) {
        @Override
        public String toString() {
            return employeeId;
        }
    }
}
