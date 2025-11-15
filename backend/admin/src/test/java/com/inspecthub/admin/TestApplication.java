package com.inspecthub.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test Application for Integration Tests
 *
 * admin 모듈은 라이브러리 모듈이므로 @SpringBootApplication이 없음
 * Integration Test 실행을 위한 Test Application
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
