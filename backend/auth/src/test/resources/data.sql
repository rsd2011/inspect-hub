-- 테스트용 사용자 데이터
-- 비밀번호는 BCrypt로 암호화: "Password123!" -> $2a$10$... 형식
-- ID는 ULID 기반 (26자 VARCHAR)

-- 활성 사용자 (비밀번호: Password123!)
INSERT INTO users (id, employee_id, password, name, email, status, failed_attempts, locked_until, last_login_at, created_at, updated_at)
VALUES ('01ARZ3NDEKTSV4RRFFQ69G5FAV', 'EMP001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        '홍길동', 'hong@example.com', 'ACTIVE', 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 실패 시도 있는 사용자
INSERT INTO users (id, employee_id, password, name, email, status, failed_attempts, locked_until, last_login_at, created_at, updated_at)
VALUES ('01ARZ3NDEKTSV4RRFFQ69G5FAW', 'EMP002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        '김철수', 'kim@example.com', 'ACTIVE', 3, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 잠긴 사용자 (locked_until이 미래)
INSERT INTO users (id, employee_id, password, name, email, status, failed_attempts, locked_until, last_login_at, created_at, updated_at)
VALUES ('01ARZ3NDEKTSV4RRFFQ69G5FAX', 'EMP003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        '이영희', 'lee@example.com', 'ACTIVE', 5, DATEADD('HOUR', 1, CURRENT_TIMESTAMP), NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 비활성 사용자
INSERT INTO users (id, employee_id, password, name, email, status, failed_attempts, locked_until, last_login_at, created_at, updated_at)
VALUES ('01ARZ3NDEKTSV4RRFFQ69G5FAY', 'EMP004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        '박민수', 'park@example.com', 'INACTIVE', 0, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 마지막 로그인 기록이 있는 사용자
INSERT INTO users (id, employee_id, password, name, email, status, failed_attempts, locked_until, last_login_at, created_at, updated_at)
VALUES ('01ARZ3NDEKTSV4RRFFQ69G5FAZ', 'EMP005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        '최지훈', 'choi@example.com', 'ACTIVE', 0, NULL, DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 테스트용 로그인 정책 데이터
INSERT INTO login_policy (id, name, enabled_methods, priority, active, created_by, created_at)
VALUES ('01JCXYZ1234567890ABCDEF002', '시스템 로그인 정책', 
        '["SSO", "AD", "LOCAL"]', '["SSO", "AD", "LOCAL"]', 
        true, 'SYSTEM', CURRENT_TIMESTAMP);
