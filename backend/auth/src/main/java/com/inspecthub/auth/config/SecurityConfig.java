package com.inspecthub.auth.config;

import com.inspecthub.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * PasswordEncoder Bean 등록
     * BCrypt 알고리즘 사용 (strength = 10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정
     *
     * - JWT 기반 Stateless 인증
     * - CSRF 비활성화 (JWT 사용)
     * - 공개 엔드포인트: /api/v1/auth/login, /api/v1/auth/refresh
     * - 보호 엔드포인트: 나머지 모든 API (인증 필요)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (Stateless JWT 인증 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 비활성화 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 엔드포인트 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트 (인증 불필요)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()          // LOCAL 로그인
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login/ad").permitAll()        // AD 로그인
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()         // 토큰 갱신
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/request-reset").permitAll()   // 비밀번호 리셋 요청
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/validate-reset-token").permitAll()  // 리셋 토큰 검증
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password").permitAll()  // 비밀번호 리셋 실행
                        .requestMatchers("/actuator/**").permitAll()                 // Health Check

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 인증 실패 시 401 Unauthorized 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(401, "Unauthorized");
                        })
                )

                // JWT 인증 필터 추가
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
