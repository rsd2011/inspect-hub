# Backend Testing Guide

> **Inspect-Hub Backend 테스트 전략 및 구현 가이드**
> 
> **Version**: 1.0  
> **Last Updated**: 2025-01-13  
> **Target**: Java 21 + Spring Boot 3.3.2 + MyBatis + PostgreSQL

---

## 📚 목차

1. [테스트 전략 개요](#테스트-전략-개요)
2. [테스트 환경 설정](#테스트-환경-설정)
3. [단위 테스트 (Unit Tests)](#단위-테스트-unit-tests)
4. [통합 테스트 (Integration Tests)](#통합-테스트-integration-tests)
5. [MyBatis 매퍼 테스트](#mybatis-매퍼-테스트)
6. [API 테스트](#api-테스트)
7. [보안 테스트](#보안-테스트)
8. [성능 테스트](#성능-테스트)
9. [테스트 커버리지](#테스트-커버리지)
10. [CI/CD 통합](#cicd-통합)
11. [베스트 프랙티스](#베스트-프랙티스)

---

## 테스트 전략 개요

### 테스트 피라미드

```
       /\
      /  \       E2E Tests (5%)
     /----\      - API 통합 테스트
    /      \     - 시나리오 테스트
   /--------\    
  / Integration\ Integration Tests (25%)
 /   Tests     \ - 데이터베이스 통합
/--------------\ - 외부 시스템 연동
|              |
|  Unit Tests  | Unit Tests (70%)
|              | - 비즈니스 로직
|              | - 도메인 모델
|______________|
```

### 테스트 범위

| 레이어 | 테스트 유형 | 커버리지 목표 | 도구 |
|--------|-------------|---------------|------|
| **Domain** | Unit | 90%+ | JUnit 5, AssertJ |
| **Service** | Unit + Integration | 85%+ | Mockito, Testcontainers |
| **Repository** | Integration | 80%+ | @DataJpaTest, TestContainers |
| **Controller** | Integration | 80%+ | MockMvc, RestAssured |
| **Batch** | Integration | 75%+ | Spring Batch Test |
| **Security** | Integration | 90%+ | Spring Security Test |

### 핵심 원칙

1. **F.I.R.S.T 원칙**
   - **Fast**: 빠른 실행 (<1초/테스트)
   - **Independent**: 독립적 실행 가능
   - **Repeatable**: 반복 가능
   - **Self-validating**: 자동 검증
   - **Timely**: 적시 작성 (코드 작성 전후)

2. **Given-When-Then 패턴**
   - Given: 테스트 전제 조건
   - When: 테스트 실행
   - Then: 결과 검증

3. **테스트 격리**
   - 각 테스트는 독립적
   - 데이터베이스 롤백 (@Transactional)
   - 외부 의존성 모킹

---

## 테스트 환경 설정

### Gradle 의존성

```groovy
// build.gradle
dependencies {
    // JUnit 5 (Jupiter)
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    
    // Mockito
    testImplementation 'org.mockito:mockito-core:5.5.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.5.0'
    
    // AssertJ (Fluent Assertions)
    testImplementation 'org.assertj:assertj-core:3.24.2'
    
    // Spring Boot Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    
    // Spring Security Test
    testImplementation 'org.springframework.security:spring-security-test'
    
    // Testcontainers
    testImplementation 'org.testcontainers:testcontainers:1.19.0'
    testImplementation 'org.testcontainers:postgresql:1.19.0'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
    
    // RestAssured (API Testing)
    testImplementation 'io.rest-assured:rest-assured:5.3.2'
    testImplementation 'io.rest-assured:json-path:5.3.2'
    
    // Testcontainers for Kafka (if needed)
    testImplementation 'org.testcontainers:kafka:1.19.0'
    
    // H2 Database (for lightweight unit tests)
    testRuntimeOnly 'com.h2database:h2'
}

test {
    useJUnitPlatform()
    
    // 병렬 실행 설정
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    
    // 테스트 리포트
    reports {
        html.required = true
        junitXml.required = true
    }
    
    // 테스트 실패 시 상세 출력
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = false
    }
}
```

### Test Application Properties

```yaml
# src/test/resources/application-test.yml
spring:
  profiles:
    active: test
  
  datasource:
    url: jdbc:tc:postgresql:14:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
    username: test
    password: test
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  redis:
    host: localhost
    port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092
  
  security:
    jwt:
      secret: test-secret-key-for-unit-tests-only-min-256-bits
      access-token-expiry: 3600000  # 1 hour
      refresh-token-expiry: 604800000  # 7 days

logging:
  level:
    root: INFO
    com.inspecthub: DEBUG
    org.springframework.security: DEBUG
    org.mybatis: DEBUG
```

### Base Test Classes

```java
// BaseUnitTest.java
package com.inspecthub.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for unit tests
 * - 외부 의존성 없음
 * - Mockito를 사용한 의존성 모킹
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {
    // Common setup can go here
}
```

```java
// BaseIntegrationTest.java
package com.inspecthub.test;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests
 * - Spring Context 로드
 * - Testcontainers 사용
 * - 트랜잭션 롤백
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Tag("integration")
public abstract class BaseIntegrationTest {
    
    @Container
    protected static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## 단위 테스트 (Unit Tests)

### Service Layer Unit Test

```java
package com.inspecthub.admin.service;

import com.inspecthub.admin.domain.User;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.admin.dto.UserResponse;
import com.inspecthub.admin.repository.UserRepository;
import com.inspecthub.common.exception.DuplicateResourceException;
import com.inspecthub.common.exception.ResourceNotFoundException;
import com.inspecthub.common.util.UlidGenerator;
import com.inspecthub.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("UserService 단위 테스트")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UlidGenerator ulidGenerator;
    
    @InjectMocks
    private UserService userService;
    
    private UserCreateRequest request;
    private User user;
    
    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        request = UserCreateRequest.builder()
            .username("testuser")
            .password("Password123!")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .orgId("01ARZ3NDEKTSV4RRFFQ69G5FAV")
            .permGroupCodes(List.of("PG_ADMIN"))
            .build();
        
        user = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("testuser")
            .passwordHash("$2a$12$encodedPassword")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .build();
    }
    
    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // Given
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(ulidGenerator.generate()).willReturn("01HGW2N7XKQJBZ9VFQR8X7Y3ZT");
        given(passwordEncoder.encode(anyString())).willReturn("$2a$12$encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        
        // When
        UserResponse response = userService.createUser(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 생성 - 중복 username")
    void createUser_DuplicateUsername() {
        // Given
        given(userRepository.existsByUsername(anyString())).willReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("username");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("사용자 조회 - 성공")
    void getUser_Success() {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        
        // When
        UserResponse response = userService.getUser(userId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("사용자 조회 - 존재하지 않음")
    void getUser_NotFound() {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.getUser(userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
        
        verify(userRepository).findById(userId);
    }
}
```

### Domain Model Unit Test

```java
package com.inspecthub.policy.domain;

import com.inspecthub.common.exception.InvalidStateException;
import com.inspecthub.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StandardSnapshot 도메인 테스트")
class StandardSnapshotTest extends BaseUnitTest {

    private StandardSnapshot snapshot;
    
    @BeforeEach
    void setUp() {
        snapshot = StandardSnapshot.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .type("STR")
            .version(1)
            .status("DRAFT")
            .criteriaJson("{\"threshold\": 10000000}")
            .effectiveFrom(LocalDateTime.now().plusDays(1))
            .build();
    }
    
    @Test
    @DisplayName("스냅샷 활성화 - 성공")
    void activate_Success() {
        // Given
        assertThat(snapshot.getStatus()).isEqualTo("DRAFT");
        
        // When
        snapshot.activate();
        
        // Then
        assertThat(snapshot.getStatus()).isEqualTo("ACTIVE");
        assertThat(snapshot.getActivatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("스냅샷 활성화 - 이미 활성화됨")
    void activate_AlreadyActive() {
        // Given
        snapshot.activate();
        
        // When & Then
        assertThatThrownBy(() -> snapshot.activate())
            .isInstanceOf(InvalidStateException.class)
            .hasMessageContaining("already active");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "DEPRECATED", "ROLLED_BACK"})
    @DisplayName("스냅샷 수정 - 잘못된 상태")
    void update_InvalidStatus(String status) {
        // Given
        snapshot.setStatus(status);
        
        // When & Then
        assertThatThrownBy(() -> snapshot.updateCriteria("{\"new\": \"data\"}"))
            .isInstanceOf(InvalidStateException.class)
            .hasMessageContaining("DRAFT");
    }
    
    @Test
    @DisplayName("스냅샷 버전 체인 연결")
    void linkVersionChain() {
        // Given
        StandardSnapshot prevSnapshot = StandardSnapshot.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .type("STR")
            .version(1)
            .status("ACTIVE")
            .build();
        
        // When
        snapshot.linkToPrevious(prevSnapshot);
        
        // Then
        assertThat(snapshot.getPrevId()).isEqualTo(prevSnapshot.getId());
        assertThat(prevSnapshot.getNextId()).isEqualTo(snapshot.getId());
    }
}
```

---

## 통합 테스트 (Integration Tests)

### Repository Integration Test

```java
package com.inspecthub.admin.repository;

import com.inspecthub.admin.domain.User;
import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("testuser")
            .passwordHash("$2a$12$encodedPassword")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFind() {
        // When
        User saved = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();
        
        Optional<User> found = userRepository.findById(saved.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("username으로 사용자 조회")
    void findByUsername() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
    }
    
    @Test
    @DisplayName("email 중복 체크")
    void existsByEmail() {
        // Given
        userRepository.save(testUser);
        entityManager.flush();
        
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("other@example.com");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("조직별 사용자 목록 조회")
    void findByOrgId() {
        // Given
        String orgId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZS";
        testUser.setOrgId(orgId);
        userRepository.save(testUser);
        
        User anotherUser = User.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZU")
            .username("user2")
            .passwordHash("$2a$12$hash2")
            .email("user2@example.com")
            .fullName("사용자2")
            .orgId(orgId)
            .status("ACTIVE")
            .build();
        userRepository.save(anotherUser);
        entityManager.flush();
        
        // When
        List<User> users = userRepository.findByOrgId(orgId);
        
        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
            .containsExactlyInAnyOrder("testuser", "user2");
    }
    
    @Test
    @DisplayName("Soft Delete")
    void softDelete() {
        // Given
        User saved = userRepository.save(testUser);
        String userId = saved.getId();
        entityManager.flush();
        
        // When
        saved.setDeleted(true);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("admin");
        userRepository.save(saved);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User deletedUser = userRepository.findById(userId).orElseThrow();
        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
        assertThat(deletedUser.getDeletedBy()).isEqualTo("admin");
    }
}
```

---

## MyBatis 매퍼 테스트

### MyBatis Mapper Integration Test

```java
package com.inspecthub.detection.mapper;

import com.inspecthub.detection.dto.DetectionEventSearchCriteria;
import com.inspecthub.detection.entity.DetectionEvent;
import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DetectionEventMapper 통합 테스트")
class DetectionEventMapperTest extends BaseIntegrationTest {

    @Autowired
    private DetectionEventMapper detectionEventMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    @Sql(scripts = "/sql/setup-detection-test-data.sql")
    void setUp() {
        // SQL 스크립트로 테스트 데이터 준비
    }
    
    @Test
    @DisplayName("탐지 이벤트 삽입")
    void insertDetectionEvent() {
        // Given
        DetectionEvent event = DetectionEvent.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .instanceId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .ruleCode("STR_HIGH_AMOUNT")
            .txId("TX_001")
            .customerId("CUST_001")
            .amount(BigDecimal.valueOf(50000000))
            .currency("KRW")
            .riskScore(BigDecimal.valueOf(85.5))
            .status("NEW")
            .detectedAt(LocalDateTime.now())
            .build();
        
        // When
        int inserted = detectionEventMapper.insert(event);
        
        // Then
        assertThat(inserted).isEqualTo(1);
        
        DetectionEvent found = detectionEventMapper.findById(event.getId());
        assertThat(found).isNotNull();
        assertThat(found.getRuleCode()).isEqualTo("STR_HIGH_AMOUNT");
        assertThat(found.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000000));
    }
    
    @Test
    @DisplayName("동적 쿼리 - 조건 검색")
    void searchWithDynamicQuery() {
        // Given
        DetectionEventSearchCriteria criteria = DetectionEventSearchCriteria.builder()
            .ruleCode("STR_HIGH_AMOUNT")
            .minAmount(BigDecimal.valueOf(10000000))
            .maxAmount(BigDecimal.valueOf(100000000))
            .status("NEW")
            .fromDate(LocalDateTime.now().minusDays(7))
            .toDate(LocalDateTime.now())
            .build();
        
        // When
        List<DetectionEvent> events = detectionEventMapper.search(criteria);
        
        // Then
        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> 
            e.getRuleCode().equals("STR_HIGH_AMOUNT") &&
            e.getAmount().compareTo(BigDecimal.valueOf(10000000)) >= 0 &&
            e.getAmount().compareTo(BigDecimal.valueOf(100000000)) <= 0
        );
    }
    
    @Test
    @DisplayName("페이지네이션 - Cursor 기반")
    void cursorBasedPagination() {
        // Given
        String lastId = null;
        int pageSize = 10;
        
        // When - Page 1
        List<DetectionEvent> page1 = detectionEventMapper.findByCursor(lastId, pageSize);
        
        // Then
        assertThat(page1).hasSize(10);
        
        // When - Page 2
        lastId = page1.get(page1.size() - 1).getId();
        List<DetectionEvent> page2 = detectionEventMapper.findByCursor(lastId, pageSize);
        
        // Then
        assertThat(page2).isNotEmpty();
        assertThat(page2.get(0).getId()).isNotEqualTo(page1.get(0).getId());
    }
    
    @Test
    @DisplayName("집계 쿼리 - 룰별 이벤트 수")
    void countByRuleCode() {
        // When
        List<Map<String, Object>> counts = detectionEventMapper.countGroupByRuleCode();
        
        // Then
        assertThat(counts).isNotEmpty();
        assertThat(counts.get(0)).containsKeys("rule_code", "event_count");
    }
    
    @Test
    @DisplayName("SQL Injection 방어 - 파라미터 바인딩")
    void sqlInjectionPrevention() {
        // Given - SQL Injection 시도
        String maliciousInput = "'; DROP TABLE detection_event; --";
        
        // When & Then - 예외 없이 안전하게 처리
        assertThatCode(() -> {
            DetectionEvent event = detectionEventMapper.findByTxId(maliciousInput);
            assertThat(event).isNull();  // 결과 없음 (주입 실패)
        }).doesNotThrowAnyException();
        
        // 테이블이 여전히 존재하는지 확인
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM detection_event", Integer.class
        );
        assertThat(count).isNotNull();
    }
}
```

### SQL Test Script

```sql
-- src/test/resources/sql/setup-detection-test-data.sql

-- 테스트 스냅샷 생성
INSERT INTO standard_snapshot (id, type, version, status, criteria_json, effective_from, created_at)
VALUES ('01HGW2N7XKQJBZ9VFQR8X7Y3ZS', 'STR', 1, 'ACTIVE', '{"threshold": 10000000}', NOW() - INTERVAL '1 month', NOW() - INTERVAL '1 month');

-- 테스트 인스펙션 인스턴스 생성
INSERT INTO inspection_instance (id, type, snapshot_id, created_at)
VALUES ('01HGW2N7XKQJBZ9VFQR8X7Y3ZT', 'STR', '01HGW2N7XKQJBZ9VFQR8X7Y3ZS', NOW() - INTERVAL '1 day');

-- 테스트 탐지 이벤트 생성 (100건)
INSERT INTO detection_event (id, instance_id, rule_code, tx_id, customer_id, amount, currency, risk_score, status, detected_at)
SELECT
    'E' || LPAD(generate_series::TEXT, 25, '0'),
    '01HGW2N7XKQJBZ9VFQR8X7Y3ZT',
    CASE WHEN random() < 0.5 THEN 'STR_HIGH_AMOUNT' ELSE 'STR_FREQUENT_TX' END,
    'TX_' || LPAD(generate_series::TEXT, 6, '0'),
    'CUST_' || LPAD((random() * 1000)::INTEGER::TEXT, 6, '0'),
    (random() * 100000000)::NUMERIC(15,2),
    'KRW',
    (random() * 100)::NUMERIC(5,2),
    'NEW',
    NOW() - (random() * INTERVAL '7 days')
FROM generate_series(1, 100);
```

---

## API 테스트

### MockMvc Controller Test

```java
package com.inspecthub.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.admin.dto.UserResponse;
import com.inspecthub.admin.service.UserService;
import com.inspecthub.common.exception.DuplicateResourceException;
import com.inspecthub.common.exception.ResourceNotFoundException;
import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController API 테스트")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/users - 사용자 생성 성공")
    void createUser_Success() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        UserResponse response = UserResponse.builder()
            .id("01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
            .username("newuser")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .status("ACTIVE")
            .build();
        
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("01HGW2N7XKQJBZ9VFQR8X7Y3ZT"))
            .andExpect(jsonPath("$.data.username").value("newuser"))
            .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
        
        verify(userService).createUser(any(UserCreateRequest.class));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/users - 중복 username (409 Conflict)")
    void createUser_DuplicateUsername() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("existinguser")
            .password("Password123!")
            .email("user@example.com")
            .fullName("사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        given(userService.createUser(any(UserCreateRequest.class)))
            .willThrow(new DuplicateResourceException("Username already exists: existinguser"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
            .andExpect(jsonPath("$.error.message").containsString("existinguser"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{id} - 사용자 조회 성공")
    void getUser_Success() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        UserResponse response = UserResponse.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .fullName("테스트 사용자")
            .status("ACTIVE")
            .build();
        
        given(userService.getUser(userId)).willReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(userId))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/users/{id} - 존재하지 않는 사용자 (404)")
    void getUser_NotFound() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        given(userService.getUser(userId))
            .willThrow(new ResourceNotFoundException("User not found: " + userId));
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/users/{id} - Soft Delete 성공")
    void deleteUser_Success() throws Exception {
        // Given
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        willDoNothing().given(userService).deleteUser(userId);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(userId);
    }
    
    @Test
    @DisplayName("POST /api/v1/users - 인증 없이 요청 (401)")
    void createUser_Unauthorized() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/users - 권한 없음 (403)")
    void createUser_Forbidden() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .fullName("신규 사용자")
            .build();
        
        // When & Then (USER 권한으로는 사용자 생성 불가)
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
```

### RestAssured E2E Test

```java
package com.inspecthub.e2e;

import com.inspecthub.admin.dto.LoginRequest;
import com.inspecthub.admin.dto.UserCreateRequest;
import com.inspecthub.test.BaseIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User API E2E 테스트")
class UserApiE2ETest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;
    
    private static String accessToken;
    private static String createdUserId;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1";
    }
    
    @Test
    @Order(1)
    @DisplayName("E2E: 로그인")
    void login() {
        LoginRequest request = LoginRequest.builder()
            .username("admin")
            .password("admin123")
            .build();
        
        accessToken = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.accessToken", notNullValue())
            .extract()
            .path("data.accessToken");
    }
    
    @Test
    @Order(2)
    @DisplayName("E2E: 사용자 생성")
    void createUser() {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("e2euser")
            .password("Password123!")
            .email("e2e@example.com")
            .fullName("E2E 테스트 사용자")
            .orgId("01HGW2N7XKQJBZ9VFQR8X7Y3ZS")
            .permGroupCodes(List.of("PG_USER"))
            .build();
        
        createdUserId = given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("success", is(true))
            .body("data.username", equalTo("e2euser"))
            .body("data.email", equalTo("e2e@example.com"))
            .extract()
            .path("data.id");
    }
    
    @Test
    @Order(3)
    @DisplayName("E2E: 사용자 조회")
    void getUser() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.id", equalTo(createdUserId))
            .body("data.username", equalTo("e2euser"));
    }
    
    @Test
    @Order(4)
    @DisplayName("E2E: 사용자 목록 조회")
    void listUsers() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("page", 1)
            .queryParam("size", 20)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.items", notNullValue())
            .body("data.items", hasSize(greaterThanOrEqualTo(1)))
            .body("data.pagination.page", equalTo(1))
            .body("data.pagination.size", equalTo(20));
    }
    
    @Test
    @Order(5)
    @DisplayName("E2E: 사용자 수정")
    void updateUser() {
        Map<String, Object> updateRequest = Map.of(
            "fullName", "수정된 이름",
            "email", "updated@example.com"
        );
        
        given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.fullName", equalTo("수정된 이름"))
            .body("data.email", equalTo("updated@example.com"));
    }
    
    @Test
    @Order(6)
    @DisplayName("E2E: 사용자 삭제")
    void deleteUser() {
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .delete("/users/{id}", createdUserId)
        .then()
            .statusCode(204);
        
        // 삭제 후 조회 시 deleted=true 확인
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/users/{id}", createdUserId)
        .then()
            .statusCode(200)
            .body("data.deleted", is(true))
            .body("data.deletedAt", notNullValue());
    }
}
```

---

## 보안 테스트

### JWT Authentication Test

```java
package com.inspecthub.security;

import com.inspecthub.security.jwt.JwtTokenProvider;
import com.inspecthub.security.jwt.JwtTokenValidator;
import com.inspecthub.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("JWT 인증 테스트")
class JwtAuthenticationTest extends BaseUnitTest {

    @Mock
    private JwtTokenValidator tokenValidator;
    
    @InjectMocks
    private JwtTokenProvider tokenProvider;
    
    private String userId;
    private String username;
    private List<String> roles;
    
    @BeforeEach
    void setUp() {
        userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        username = "testuser";
        roles = List.of("ROLE_USER");
    }
    
    @Test
    @DisplayName("액세스 토큰 생성")
    void generateAccessToken() {
        // When
        String token = tokenProvider.generateAccessToken(userId, username, roles);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).startsWith("eyJ");  // JWT header
        assertThat(token.split("\\.")).hasSize(3);  // header.payload.signature
    }
    
    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_Valid() {
        // Given
        String token = tokenProvider.generateAccessToken(userId, username, roles);
        given(tokenValidator.validateToken(token)).willReturn(true);
        
        // When
        boolean isValid = tokenValidator.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("토큰 검증 - 만료된 토큰")
    void validateToken_Expired() {
        // Given
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.expired.token";
        given(tokenValidator.validateToken(expiredToken)).willReturn(false);
        
        // When
        boolean isValid = tokenValidator.validateToken(expiredToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("토큰에서 사용자 정보 추출")
    void extractUserInfo() {
        // Given
        String token = tokenProvider.generateAccessToken(userId, username, roles);
        
        // When
        String extractedUserId = tokenProvider.getUserId(token);
        String extractedUsername = tokenProvider.getUsername(token);
        List<String> extractedRoles = tokenProvider.getRoles(token);
        
        // Then
        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(extractedRoles).containsExactlyInAnyOrderElementsOf(roles);
    }
}
```

### Authorization Test

```java
package com.inspecthub.security;

import com.inspecthub.test.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("권한 인가 테스트")
class AuthorizationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN 권한 - 사용자 생성 가능")
    void adminCanCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isOk());  // 400 Bad Request가 아닌 200 OK (권한은 통과)
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER 권한 - 사용자 생성 불가 (403)")
    void userCannotCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "USER", username = "user1")
    @DisplayName("자신의 정보 조회 가능")
    void userCanViewOwnProfile() throws Exception {
        // Mock setup: user1's ID
        String userId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        
        mockMvc.perform(get("/api/v1/users/{id}", userId))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "COMPLIANCE_OFFICER")
    @DisplayName("컴플라이언스 오피서 - 사례 승인 가능")
    void complianceOfficerCanApproveCases() throws Exception {
        String caseId = "01HGW2N7XKQJBZ9VFQR8X7Y3ZT";
        
        mockMvc.perform(post("/api/v1/cases/{id}/approve", caseId)
                .contentType("application/json")
                .content("{\"comment\": \"Approved\"}"))
            .andExpect(status().isOk());
    }
}
```

---

## 성능 테스트

### JMH Benchmark Test

```java
package com.inspecthub.performance;

import com.inspecthub.common.util.UlidGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * ULID 생성 성능 벤치마크
 * 
 * 실행: ./gradlew jmh
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class UlidGenerationBenchmark {

    private UlidGenerator ulidGenerator;
    
    @Setup
    public void setup() {
        ulidGenerator = new UlidGenerator();
    }
    
    @Benchmark
    public String generateUlid() {
        return ulidGenerator.generate();
    }
    
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(UlidGenerationBenchmark.class.getSimpleName())
            .build();
        
        new Runner(opt).run();
    }
}

/*
Expected Output:

Benchmark                              Mode  Cnt       Score       Error  Units
UlidGenerationBenchmark.generateUlid  thrpt    5  500000.000 ± 10000.000  ops/s

Interpretation:
- 500K ULID/sec = 충분한 성능
- 목표: >100K ops/s
*/
```

### Load Test with Gatling

```scala
// src/test/scala/com/inspecthub/simulation/UserApiLoadTest.scala
package com.inspecthub.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UserApiLoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8090")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("User API Load Test")
    .exec(
      http("Login")
        .post("/api/v1/auth/login")
        .body(StringBody("""{"username": "admin", "password": "admin123"}"""))
        .check(status.is(200))
        .check(jsonPath("$.data.accessToken").saveAs("token"))
    )
    .pause(1)
    .exec(
      http("List Users")
        .get("/api/v1/users?page=1&size=20")
        .header("Authorization", "Bearer ${token}")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get User Detail")
        .get("/api/v1/users/01HGW2N7XKQJBZ9VFQR8X7Y3ZT")
        .header("Authorization", "Bearer ${token}")
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsersPerSec(10) to 500 during (2 minutes),  // 10 → 500 users/sec
      constantUsersPerSec(500) during (5 minutes)     // 500 users/sec for 5 min
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile3.lt(1000),  // 99th percentile < 1s
     global.successfulRequests.percent.gt(99)   // Success rate > 99%
   )
}
```

---

## 테스트 커버리지

### Jacoco 설정

```groovy
// build.gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.10"
}

jacocoTestReport {
    dependsOn test
    
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/config/**',
                '**/dto/**',
                '**/entity/**',
                '**/InspectHubApplication.class'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 80% 커버리지
            }
        }
        
        rule {
            element = 'PACKAGE'
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.75  // 패키지별 75%
            }
        }
    }
}

test.finalizedBy jacocoTestReport
check.dependsOn jacocoTestCoverageVerification
```

### 커버리지 리포트 확인

```bash
# 테스트 실행 및 커버리지 생성
./gradlew clean test jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification

# HTML 리포트 확인
open build/reports/jacoco/test/html/index.html
```

### 커버리지 목표

| 레이어 | 최소 커버리지 | 목표 커버리지 |
|--------|---------------|---------------|
| **Domain** | 85% | 95% |
| **Service** | 80% | 90% |
| **Repository** | 75% | 85% |
| **Controller** | 75% | 85% |
| **Util** | 90% | 95% |
| **전체** | 80% | 85% |

**제외 대상:**
- Configuration 클래스
- DTO/Entity (데이터 클래스)
- Main 클래스
- 상수 클래스

---

## CI/CD 통합

### GitHub Actions Workflow

```yaml
# .github/workflows/test.yml
name: Backend Tests

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'backend/**'
      - '.github/workflows/test.yml'
  pull_request:
    branches: [ main, develop ]
    paths:
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'
      
      - name: Grant execute permission for gradlew
        run: chmod +x backend/gradlew
      
      - name: Run tests with coverage
        run: |
          cd backend
          ./gradlew clean test jacocoTestReport
        env:
          SPRING_PROFILES_ACTIVE: test
      
      - name: Verify coverage
        run: |
          cd backend
          ./gradlew jacocoTestCoverageVerification
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/build/reports/jacoco/test/jacocoTestReport.xml
          flags: backend
          name: backend-coverage
      
      - name: Publish Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: backend/build/test-results/test/*.xml
      
      - name: Upload Test Report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-report
          path: backend/build/reports/tests/test/
```

### Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running pre-commit checks..."

# Backend 테스트 실행
cd backend
./gradlew test

if [ $? -ne 0 ]; then
    echo "❌ Backend tests failed. Commit aborted."
    exit 1
fi

# 커버리지 검증
./gradlew jacocoTestCoverageVerification

if [ $? -ne 0 ]; then
    echo "❌ Coverage verification failed. Commit aborted."
    exit 1
fi

echo "✅ All checks passed!"
exit 0
```

---

## 베스트 프랙티스

### 1. 테스트 작성 가이드

**✅ DO:**
- 테스트 이름을 명확하게 작성 (`@DisplayName` 활용)
- Given-When-Then 패턴 사용
- 하나의 테스트는 하나의 책임만
- AssertJ의 유창한 assertions 사용
- Mockito의 BDD 스타일 사용 (`given()`, `willReturn()`)

**❌ DON'T:**
- 테스트 간 의존성 생성
- 프로덕션 데이터베이스 사용
- 하드코딩된 날짜/시간 (Clock 모킹 사용)
- Sleep/Wait 사용 (Awaitility 사용)
- 과도한 모킹 (실제 객체 우선)

### 2. 테스트 격리

```java
// ✅ Good: 트랜잭션 롤백으로 격리
@Transactional
@Test
void testMethod() {
    // 테스트 후 자동 롤백
}

// ✅ Good: @BeforeEach로 초기화
@BeforeEach
void setUp() {
    testData = createTestData();
}

// ❌ Bad: 정적 변수 공유
private static User sharedUser;  // 테스트 간 간섭
```

### 3. 가독성

```java
// ✅ Good: 명확한 변수명과 구조화
@Test
@DisplayName("사용자 생성 시 중복 이메일이면 예외 발생")
void createUser_WhenDuplicateEmail_ThrowsException() {
    // Given
    String existingEmail = "existing@example.com";
    createUserWithEmail(existingEmail);
    
    UserCreateRequest request = requestWithEmail(existingEmail);
    
    // When & Then
    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("email");
}

// ❌ Bad: 불명확한 테스트
@Test
void test1() {
    User u = new User();
    u.setEmail("test@test.com");
    assertThat(service.save(u)).isNotNull();
}
```

### 4. 테스트 데이터 빌더 패턴

```java
// TestDataBuilder.java
public class TestDataBuilder {
    
    public static User.UserBuilder aUser() {
        return User.builder()
            .id(UlidGenerator.generate())
            .username("testuser")
            .passwordHash("$2a$12$hash")
            .email("test@example.com")
            .fullName("Test User")
            .status("ACTIVE")
            .createdAt(LocalDateTime.now());
    }
    
    public static UserCreateRequest.UserCreateRequestBuilder aUserCreateRequest() {
        return UserCreateRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("new@example.com")
            .fullName("New User")
            .orgId(UlidGenerator.generate())
            .permGroupCodes(List.of("PG_USER"));
    }
}

// 사용 예시
@Test
void testWithBuilder() {
    // Given
    User user = aUser()
        .username("customuser")
        .email("custom@example.com")
        .build();
    
    // When & Then
    // ...
}
```

### 5. 비동기 테스트

```java
// Awaitility 사용
@Test
void asyncTest() {
    // When
    service.asyncMethod();
    
    // Then
    await().atMost(5, SECONDS)
        .untilAsserted(() -> {
            verify(repository).save(any());
        });
}
```

### 6. 예외 테스트

```java
// ✅ Good: AssertJ 예외 검증
assertThatThrownBy(() -> service.methodThrowingException())
    .isInstanceOf(CustomException.class)
    .hasMessageContaining("expected message")
    .hasFieldOrPropertyWithValue("errorCode", "ERR_001");

// ❌ Bad: try-catch 사용
try {
    service.methodThrowingException();
    fail("Expected exception not thrown");
} catch (CustomException e) {
    assertThat(e.getMessage()).contains("expected message");
}
```

---

## 참고 자료

### 공식 문서
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers](https://www.testcontainers.org/)
- [RestAssured](https://rest-assured.io/)

### 내부 문서
- [Backend README](./README.md)
- [API Design](../api/DESIGN.md)
- [Security Guide](../architecture/SECURITY.md)
- [Database Design](../architecture/DATABASE.md)

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | 최초 작성 | 개발팀 |

---

**문의사항이 있으시면 개발팀으로 연락 주시기 바랍니다.**