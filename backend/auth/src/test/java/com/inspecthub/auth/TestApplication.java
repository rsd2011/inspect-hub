package com.inspecthub.auth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Test Application for Integration Tests
 */
@SpringBootApplication(scanBasePackages = {
    "com.inspecthub.auth",
    "com.inspecthub.common"
})
@ConfigurationPropertiesScan(basePackages = "com.inspecthub.common.config")
public class TestApplication {
    // Test context configuration
}
