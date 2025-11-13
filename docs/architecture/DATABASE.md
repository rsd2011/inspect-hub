# Database Design - Inspect-Hub

## 개요

Inspect-Hub는 PostgreSQL을 주요 데이터베이스로 사용하며, 대용량 트랜잭션 처리와 감사 로깅을 위한 최적화된 스키마 설계를 따릅니다.

## 데이터베이스 구성

### 주요 데이터베이스

| 데이터베이스 | 용도 | 특징 |
|--------------|------|------|
| **inspecthub_main** | 주요 비즈니스 데이터 | 정책, 케이스, 사용자 등 |
| **inspecthub_audit** | 감사 로그 | 100% 감사 추적 (별도 DB) |
| **inspecthub_batch** | 배치 처리 메타데이터 | Spring Batch 실행 이력 |

### 환경별 설정

- **개발 (Dev)**: H2 In-Memory
- **테스트 (Test)**: PostgreSQL Docker
- **운영 (Prod)**: PostgreSQL 15+ (HA 구성)

## 핵심 테이블 구조

### 1. 사용자 및 권한 관리

#### USER (사용자)
```sql
CREATE TABLE "user" (
    id              VARCHAR(26) PRIMARY KEY,  -- ULID
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL,     -- ACTIVE, INACTIVE, LOCKED
    org_id          VARCHAR(26) NOT NULL,
    perm_group_id   VARCHAR(26) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP,
    CONSTRAINT fk_user_org FOREIGN KEY (org_id) REFERENCES organization(id),
    CONSTRAINT fk_user_perm_group FOREIGN KEY (perm_group_id) REFERENCES permission_group(id)
);

CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_org ON "user"(org_id);
CREATE INDEX idx_user_status ON "user"(status);
```

#### ORGANIZATION (조직)
```sql
CREATE TABLE organization (
    id              VARCHAR(26) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(50) UNIQUE NOT NULL,
    parent_id       VARCHAR(26),
    org_policy_id   VARCHAR(26),
    level           INT NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_org_parent FOREIGN KEY (parent_id) REFERENCES organization(id),
    CONSTRAINT fk_org_policy FOREIGN KEY (org_policy_id) REFERENCES organization_policy(id)
);

CREATE INDEX idx_org_parent ON organization(parent_id);
CREATE INDEX idx_org_code ON organization(code);
```

#### PERMISSION_GROUP (권한 그룹)
```sql
CREATE TABLE permission_group (
    id          VARCHAR(26) PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### PERM_GROUP_FEATURE (기능 권한 매핑)
```sql
CREATE TABLE perm_group_feature (
    id              VARCHAR(26) PRIMARY KEY,
    perm_group_id   VARCHAR(26) NOT NULL,
    feature_code    VARCHAR(100) NOT NULL,
    action_code     VARCHAR(50) NOT NULL,  -- READ, WRITE, DELETE, APPROVE
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_pgf_perm_group FOREIGN KEY (perm_group_id) REFERENCES permission_group(id),
    UNIQUE(perm_group_id, feature_code, action_code)
);

CREATE INDEX idx_pgf_perm_group ON perm_group_feature(perm_group_id);
```

### 2. 정책 관리 (스냅샷 기반)

#### STANDARD_SNAPSHOT (정책 스냅샷)
```sql
CREATE TABLE standard_snapshot (
    id              VARCHAR(26) PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,  -- KYC, STR, CTR, RBA, WLF, FIU
    version         INT NOT NULL,
    effective_from  TIMESTAMP NOT NULL,
    effective_to    TIMESTAMP,
    status          VARCHAR(20) NOT NULL,  -- DRAFT, APPROVED, DEPLOYED, DEPRECATED
    prev_id         VARCHAR(26),
    next_id         VARCHAR(26),
    created_by      VARCHAR(26) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_by     VARCHAR(26),
    approved_at     TIMESTAMP,
    deployed_at     TIMESTAMP,
    config_json     JSONB NOT NULL,
    CONSTRAINT fk_snapshot_prev FOREIGN KEY (prev_id) REFERENCES standard_snapshot(id),
    CONSTRAINT fk_snapshot_next FOREIGN KEY (next_id) REFERENCES standard_snapshot(id),
    CONSTRAINT fk_snapshot_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
    CONSTRAINT fk_snapshot_approved_by FOREIGN KEY (approved_by) REFERENCES "user"(id)
);

CREATE INDEX idx_snapshot_type_status ON standard_snapshot(type, status);
CREATE INDEX idx_snapshot_effective ON standard_snapshot(effective_from, effective_to);
CREATE INDEX idx_snapshot_config ON standard_snapshot USING GIN(config_json);
```

### 3. 탐지 및 케이스 관리

#### TX_STAGING (트랜잭션 스테이징)
```sql
CREATE TABLE tx_staging (
    id              VARCHAR(26) PRIMARY KEY,
    tx_date         DATE NOT NULL,
    tx_type         VARCHAR(50) NOT NULL,
    amount          NUMERIC(18,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    customer_id     VARCHAR(50) NOT NULL,
    account_no      VARCHAR(50),
    raw_json        JSONB NOT NULL,
    processed       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (tx_date);

CREATE INDEX idx_tx_staging_date ON tx_staging(tx_date);
CREATE INDEX idx_tx_staging_customer ON tx_staging(customer_id);
CREATE INDEX idx_tx_staging_processed ON tx_staging(processed) WHERE NOT processed;

-- Partition for performance (monthly)
CREATE TABLE tx_staging_2025_01 PARTITION OF tx_staging
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

#### DETECTION_EVENT (탐지 이벤트)
```sql
CREATE TABLE detection_event (
    id              VARCHAR(26) PRIMARY KEY,
    event_type      VARCHAR(50) NOT NULL,  -- STR, CTR, WLF
    snapshot_id     VARCHAR(26) NOT NULL,
    rule_code       VARCHAR(100) NOT NULL,
    severity        VARCHAR(20) NOT NULL,  -- HIGH, MEDIUM, LOW
    score           NUMERIC(5,2),
    tx_id           VARCHAR(26),
    customer_id     VARCHAR(50),
    detected_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed        BOOLEAN NOT NULL DEFAULT FALSE,
    case_id         VARCHAR(26),
    raw_data        JSONB NOT NULL,
    CONSTRAINT fk_event_snapshot FOREIGN KEY (snapshot_id) REFERENCES standard_snapshot(id),
    CONSTRAINT fk_event_case FOREIGN KEY (case_id) REFERENCES alert_case(id)
);

CREATE INDEX idx_event_type_severity ON detection_event(event_type, severity);
CREATE INDEX idx_event_detected_at ON detection_event(detected_at DESC);
CREATE INDEX idx_event_reviewed ON detection_event(reviewed) WHERE NOT reviewed;
CREATE INDEX idx_event_customer ON detection_event(customer_id);
```

#### ALERT_CASE (조사 케이스)
```sql
CREATE TABLE alert_case (
    id              VARCHAR(26) PRIMARY KEY,
    case_no         VARCHAR(50) UNIQUE NOT NULL,
    case_type       VARCHAR(50) NOT NULL,
    status          VARCHAR(20) NOT NULL,  -- NEW, INVESTIGATING, REVIEWED, APPROVED, REJECTED, REPORTED
    priority        VARCHAR(20) NOT NULL,
    assigned_to     VARCHAR(26),
    created_by      VARCHAR(26) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_by     VARCHAR(26),
    reviewed_at     TIMESTAMP,
    approved_by     VARCHAR(26),
    approved_at     TIMESTAMP,
    conclusion      TEXT,
    CONSTRAINT fk_case_assigned_to FOREIGN KEY (assigned_to) REFERENCES "user"(id),
    CONSTRAINT fk_case_created_by FOREIGN KEY (created_by) REFERENCES "user"(id)
);

CREATE INDEX idx_case_status ON alert_case(status);
CREATE INDEX idx_case_assigned ON alert_case(assigned_to);
CREATE INDEX idx_case_created_at ON alert_case(created_at DESC);
```

#### CASE_ACTIVITY (케이스 활동)
```sql
CREATE TABLE case_activity (
    id              VARCHAR(26) PRIMARY KEY,
    case_id         VARCHAR(26) NOT NULL,
    activity_type   VARCHAR(50) NOT NULL,
    performed_by    VARCHAR(26) NOT NULL,
    performed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    comment         TEXT,
    data_json       JSONB,
    CONSTRAINT fk_activity_case FOREIGN KEY (case_id) REFERENCES alert_case(id),
    CONSTRAINT fk_activity_user FOREIGN KEY (performed_by) REFERENCES "user"(id)
);

CREATE INDEX idx_activity_case ON case_activity(case_id, performed_at DESC);
```

### 4. 보고서 관리

#### REPORT_STR (의심거래보고)
```sql
CREATE TABLE report_str (
    id              VARCHAR(26) PRIMARY KEY,
    report_no       VARCHAR(50) UNIQUE NOT NULL,
    case_id         VARCHAR(26) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    submitted_at    TIMESTAMP,
    fiu_receipt_no  VARCHAR(100),
    report_xml      TEXT,
    created_by      VARCHAR(26) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_report_str_case FOREIGN KEY (case_id) REFERENCES alert_case(id)
);

CREATE INDEX idx_report_str_status ON report_str(status);
CREATE INDEX idx_report_str_submitted ON report_str(submitted_at DESC);
```

### 5. 배치 처리 (Spring Batch 패턴)

#### INSPECTION_INSTANCE (점검 인스턴스)
```sql
CREATE TABLE inspection_instance (
    id              VARCHAR(26) PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,  -- STR_DETECTION, CTR_DETECTION, WLF_MATCHING
    snapshot_id     VARCHAR(26) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inspection_snapshot FOREIGN KEY (snapshot_id) REFERENCES standard_snapshot(id)
);
```

#### INSPECTION_EXECUTION (점검 실행)
```sql
CREATE TABLE inspection_execution (
    id              VARCHAR(26) PRIMARY KEY,
    instance_id     VARCHAR(26) NOT NULL,
    status          VARCHAR(20) NOT NULL,  -- RUNNING, COMPLETED, FAILED
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    read_count      INT,
    write_count     INT,
    skip_count      INT,
    error_message   TEXT,
    CONSTRAINT fk_execution_instance FOREIGN KEY (instance_id) REFERENCES inspection_instance(id)
);

CREATE INDEX idx_execution_status ON inspection_execution(status);
CREATE INDEX idx_execution_start ON inspection_execution(start_time DESC);
```

### 6. 감사 로그 (별도 DB)

#### AUDIT_LOG (감사 로그)
```sql
-- Database: inspecthub_audit
CREATE TABLE audit_log (
    id              VARCHAR(26) PRIMARY KEY,
    user_id         VARCHAR(26) NOT NULL,
    action_type     VARCHAR(50) NOT NULL,
    resource_type   VARCHAR(100) NOT NULL,
    resource_id     VARCHAR(26),
    before_json     JSONB,
    after_json      JSONB,
    ip_address      VARCHAR(50),
    user_agent      TEXT,
    performed_at    TIMESTAMP NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (performed_at);

CREATE INDEX idx_audit_user ON audit_log(user_id, performed_at DESC);
CREATE INDEX idx_audit_resource ON audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_action ON audit_log(action_type);

-- Monthly partitions
CREATE TABLE audit_log_2025_01 PARTITION OF audit_log
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

## 인덱스 전략

### 1. B-Tree 인덱스 (기본)
- Primary Key, Foreign Key
- 자주 조회되는 컬럼 (status, date, user_id 등)

### 2. GIN 인덱스 (JSONB)
- JSONB 컬럼 전체 검색
- 예: `config_json`, `raw_data`

### 3. Partial 인덱스
- 특정 조건의 데이터만 인덱싱
- 예: `WHERE NOT processed`, `WHERE status = 'ACTIVE'`

### 4. 복합 인덱스
- 자주 함께 조회되는 컬럼 조합
- 예: `(event_type, severity)`, `(case_id, performed_at DESC)`

## 파티셔닝 전략

### Range Partitioning (날짜 기반)

**적용 대상:**
- `tx_staging` - 월별 파티션
- `audit_log` - 월별 파티션
- `detection_event` - 분기별 파티션 (옵션)

**파티션 관리:**
```sql
-- 자동 파티션 생성 함수 (예시)
CREATE OR REPLACE FUNCTION create_monthly_partition(
    table_name TEXT,
    partition_date DATE
) RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    partition_name := table_name || '_' || TO_CHAR(partition_date, 'YYYY_MM');
    start_date := DATE_TRUNC('month', partition_date);
    end_date := start_date + INTERVAL '1 month';
    
    EXECUTE FORMAT(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
        partition_name, table_name, start_date, end_date
    );
END;
$$ LANGUAGE plpgsql;
```

**파티션 삭제 (데이터 보관 정책):**
- 트랜잭션 데이터: 3년 후 아카이브
- 감사 로그: 7년 보관 (법적 요구사항)

## 데이터 보관 정책

| 테이블 | 보관 기간 | 삭제 정책 |
|--------|-----------|-----------|
| `tx_staging` | 3년 | 파티션 단위 삭제 |
| `detection_event` | 5년 | 아카이브 후 삭제 |
| `alert_case` | 영구 | 삭제 금지 |
| `audit_log` | 7년 | 법적 요구사항 |
| `report_str` | 영구 | 삭제 금지 |

## 마이그레이션 전략

### 1. Flyway 사용
```
backend/server/src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_user_tables.sql
├── V3__add_policy_tables.sql
└── V4__add_indexes.sql
```

### 2. 버전 관리 규칙
- `V{version}__{description}.sql`
- 버전은 순차적 증가
- 운영 배포 전 테스트 환경 검증 필수

### 3. 롤백 전략
- 데이터 손실 없는 변경만 자동 적용
- 파괴적 변경은 수동 백업 + 확인 후 적용

## 성능 최적화

### 1. Connection Pool 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 2. Query 최적화
- N+1 문제 방지 (Batch Fetch)
- Covering Index 활용
- EXPLAIN ANALYZE로 실행 계획 확인

### 3. 캐싱 전략
- Redis 캐싱: 정책 스냅샷, 코드 값
- Local Cache (Caffeine): 사용자 권한

### 4. 읽기 전용 Replica
- 대량 조회는 Read Replica 사용
- 보고서 생성, 통계 조회

## 백업 전략

### 1. 풀 백업
- 주기: 매일 오전 2시
- 보관: 30일
- 도구: pg_dump + 압축

### 2. 증분 백업
- 주기: 6시간마다
- WAL 아카이빙 활용

### 3. PITR (Point-In-Time Recovery)
- WAL 보관: 7일
- 복구 시간 목표 (RTO): 1시간 이내

## 보안

### 1. 접근 제어
- 애플리케이션 전용 계정 사용
- 최소 권한 원칙
- SSL/TLS 연결 강제

### 2. 암호화
- 전송 암호화: SSL/TLS
- 저장 암호화: Transparent Data Encryption (TDE)
- 컬럼 레벨 암호화: 민감 정보 (주민번호, 계좌번호)

### 3. 감사
- 모든 DDL 로깅
- 민감 테이블 접근 로깅
- pgAudit 활용

## 모니터링

### 1. 성능 메트릭
- Connection Pool 사용률
- Query 실행 시간
- Lock 대기 시간
- Index Hit Ratio

### 2. 용량 관리
- 테이블 크기 모니터링
- 파티션 자동 생성 확인
- Vacuum/Analyze 스케줄링

### 3. 알림
- Connection Pool 부족
- Slow Query 감지
- Disk 사용률 80% 초과

## ERD (Entity-Relationship Diagram)

```
[User] 1---N [Organization]
   |
   1---N [AuditLog]
   |
   1---N [AlertCase]
   
[StandardSnapshot] 1---N [DetectionEvent]
        |
        1---N [InspectionInstance]
        
[AlertCase] 1---N [CaseActivity]
     |
     1---1 [ReportSTR]
     
[TxStaging] 1---N [DetectionEvent]

[PermissionGroup] 1---N [User]
       |
       1---N [PermGroupFeature]
```

## 참고

- **DDL Scripts**: `backend/server/src/main/resources/db/migration/`
- **MyBatis Mappers**: `backend/*/src/main/resources/mybatis/`
- **데이터 사전**: Confluence Wiki (예정)
