-- Inspect-Hub Database Schema (H2 Development)
-- Multi-Tenancy 기반 AML 시스템

-- 테넌트 테이블 (회원사/기관)
CREATE TABLE IF NOT EXISTS tenant (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    UNIQUE (tenant_id, username)
);

-- 권한 그룹 테이블
CREATE TABLE IF NOT EXISTS permission_group (
    perm_group_code VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
);

-- 스냅샷 테이블 (정책/기준 버전 관리)
CREATE TABLE IF NOT EXISTS standard_snapshot (
    snapshot_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    snapshot_type VARCHAR(50) NOT NULL, -- KYC, STR, CTR, WLF, RBA, FIU
    version VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, APPROVED, ACTIVE, ARCHIVED
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    prev_snapshot_id VARCHAR(50),
    next_snapshot_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    approved_at TIMESTAMP,
    approved_by VARCHAR(100),
    description TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    FOREIGN KEY (prev_snapshot_id) REFERENCES standard_snapshot(snapshot_id),
    UNIQUE (tenant_id, snapshot_type, version)
);

-- 탐지 이벤트 테이블
CREATE TABLE IF NOT EXISTS detection_event (
    event_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- STR, CTR, WLF
    snapshot_id VARCHAR(50) NOT NULL,
    rule_code VARCHAR(100),
    risk_score DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW', -- NEW, REVIEWED, CASE_CREATED, CLOSED
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(100),
    customer_id VARCHAR(100),
    transaction_id VARCHAR(100),
    matched_fields TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    FOREIGN KEY (snapshot_id) REFERENCES standard_snapshot(snapshot_id)
);

-- 알림 케이스 테이블
CREATE TABLE IF NOT EXISTS alert_case (
    case_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    case_number VARCHAR(100) NOT NULL,
    case_type VARCHAR(50) NOT NULL, -- STR, CTR
    snapshot_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, SUBMITTED, CLOSED
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    assigned_to VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    closed_at TIMESTAMP,
    customer_id VARCHAR(100),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    FOREIGN KEY (snapshot_id) REFERENCES standard_snapshot(snapshot_id),
    FOREIGN KEY (assigned_to) REFERENCES users(user_id),
    UNIQUE (tenant_id, case_number)
);

-- 케이스 활동 로그 테이블
CREATE TABLE IF NOT EXISTS case_activity (
    activity_id VARCHAR(50) PRIMARY KEY,
    case_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    activity_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment TEXT,
    attachment_ref TEXT,
    FOREIGN KEY (case_id) REFERENCES alert_case(case_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    FOREIGN KEY (actor) REFERENCES users(user_id)
);

-- 점검 인스턴스 테이블 (배치 작업)
CREATE TABLE IF NOT EXISTS inspection_instance (
    instance_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    instance_type VARCHAR(50) NOT NULL, -- STR, CTR, WLF, RBA
    snapshot_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED', -- CREATED, RUNNING, COMPLETED, FAILED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
    FOREIGN KEY (snapshot_id) REFERENCES standard_snapshot(snapshot_id)
);

-- 점검 실행 테이블
CREATE TABLE IF NOT EXISTS inspection_execution (
    execution_id VARCHAR(50) PRIMARY KEY,
    instance_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED', -- STARTED, COMPLETED, FAILED
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    read_count BIGINT DEFAULT 0,
    write_count BIGINT DEFAULT 0,
    skip_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    exit_message TEXT,
    FOREIGN KEY (instance_id) REFERENCES inspection_instance(instance_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
);

-- 감사 로그 테이블
CREATE TABLE IF NOT EXISTS audit_log (
    log_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, VIEW
    actor VARCHAR(100) NOT NULL,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_state TEXT,
    after_state TEXT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
);

-- 초기 테넌트 데이터 (개발용)
INSERT INTO tenant (tenant_id, tenant_name, status, created_by)
VALUES ('DEFAULT', 'Default Tenant', 'ACTIVE', 'SYSTEM');

-- 초기 사용자 데이터 (개발용, 비밀번호: admin123)
INSERT INTO users (user_id, tenant_id, username, password, email, full_name, status)
VALUES ('admin', 'DEFAULT', 'admin', '$2a$10$rGQz8pJZN5ypYs7qH9YmXuQ8Q.mZKZQ5J7m8G5fZKQH5Y0H5Y0H5Y', 'admin@inspecthub.com', 'System Admin', 'ACTIVE');
