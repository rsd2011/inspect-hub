-- V001: Create USERS table for authentication
-- AML Compliance System - User Management

CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Authentication
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    -- User Information
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,

    -- Account Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE: 활성, INACTIVE: 비활성, LOCKED: 잠금

    -- Account Locking Policy
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,

    -- Audit Fields
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    CONSTRAINT chk_failed_attempts CHECK (failed_attempts >= 0)
);

-- Indexes for performance
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_email ON users(email);

-- Comments
COMMENT ON TABLE users IS '사용자 계정 정보';
COMMENT ON COLUMN users.id IS '사용자 PK (시퀀스)';
COMMENT ON COLUMN users.employee_id IS '사원번호 (로그인 ID)';
COMMENT ON COLUMN users.password IS 'BCrypt 해시 비밀번호';
COMMENT ON COLUMN users.name IS '사용자 이름';
COMMENT ON COLUMN users.email IS '이메일 주소';
COMMENT ON COLUMN users.status IS '계정 상태 (ACTIVE/INACTIVE/LOCKED)';
COMMENT ON COLUMN users.failed_attempts IS '로그인 실패 횟수';
COMMENT ON COLUMN users.locked_until IS '계정 잠금 만료 시간';
COMMENT ON COLUMN users.last_login_at IS '마지막 로그인 시간';
COMMENT ON COLUMN users.created_at IS '계정 생성 시간';
COMMENT ON COLUMN users.updated_at IS '최종 수정 시간';
