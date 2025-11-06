package com.inspecthub.auth.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auth 모듈 설정
 *
 * 이 Configuration을 통해 auth 모듈의 모든 컴포넌트를 Spring에 등록합니다.
 */
@Configuration
@ComponentScan(basePackages = "com.inspecthub.auth")
public class AuthModuleConfig {
    // Auth 모듈의 모든 컴포넌트(@Controller, @Service, @Component 등)를 자동으로 스캔
}
