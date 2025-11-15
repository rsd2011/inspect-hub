-- V002: Insert test users for development
-- Password: "Password123!" (BCrypt hashed)

-- H2 supports MERGE instead of ON CONFLICT
MERGE INTO users (employee_id, password, name, email, status, failed_attempts)
KEY(employee_id)
VALUES
    ('EMP001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '홍길동', 'hong@example.com', 'ACTIVE', 0);

MERGE INTO users (employee_id, password, name, email, status, failed_attempts)
KEY(employee_id)
VALUES
    ('EMP002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김철수', 'kim@example.com', 'ACTIVE', 0);

MERGE INTO users (employee_id, password, name, email, status, failed_attempts)
KEY(employee_id)
VALUES
    ('EMP003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '이영희', 'lee@example.com', 'INACTIVE', 0);

MERGE INTO users (employee_id, password, name, email, status, failed_attempts)
KEY(employee_id)
VALUES
    ('EMP004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '박민수', 'park@example.com', 'ACTIVE', 5);
