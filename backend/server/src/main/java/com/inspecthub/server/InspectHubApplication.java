package com.inspecthub.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Inspect-Hub 통합 준법감시 시스템
 *
 * 자금세탁방지(AML) 시스템
 * - STR(의심거래보고)
 * - CTR(고액현금거래보고)
 * - WLF(감시대상명단)
 * - 준법감시 워크플로
 */
@SpringBootApplication(scanBasePackages = "com.inspecthub")
@MapperScan(basePackages = "com.inspecthub.*.repository")
public class InspectHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(InspectHubApplication.class, args);
    }
}
