package com.inspecthub.auth.config;

import com.inspecthub.auth.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
class SecurityFilterChainTest {

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChainShouldBeLoaded() {
        assertThat(securityFilterChain).isNotNull();
    }
}
