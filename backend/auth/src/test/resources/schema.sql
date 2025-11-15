-- Users 테이블 생성 (H2 PostgreSQL 호환 모드)
-- ULID 기반 ID 사용 (26자 VARCHAR)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(26) PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_employee_id ON users(employee_id);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- LoginPolicy 테이블 생성 (H2 PostgreSQL 호환 모드)
-- H2는 JSON 타입, PostgreSQL은 JSONB 타입 사용
CREATE TABLE IF NOT EXISTS login_policy (
    id CHAR(26) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled_methods VARCHAR(1000) NOT NULL,  -- H2/PostgreSQL 공용: JSON 문자열로 저장
    priority VARCHAR(1000) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP
);

-- 인덱스 생성
CREATE UNIQUE INDEX IF NOT EXISTS idx_login_policy_global ON login_policy(name);
