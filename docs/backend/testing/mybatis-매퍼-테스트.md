# MyBatis 매퍼 테스트

## MyBatis Mapper Integration Test

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

## SQL Test Script

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
