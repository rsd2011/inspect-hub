package com.inspecthub.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 인증 설정 Properties
 *
 * application-auth.yml과 매핑
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private JwtConfig jwt = new JwtConfig();

    @Data
    public static class JwtConfig {
        private String secret;
        private TokenConfig accessToken = new TokenConfig();
        private TokenConfig refreshToken = new TokenConfig();

        @Data
        public static class TokenConfig {
            private long expirationSeconds;
        }
    }
}
