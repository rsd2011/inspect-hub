-- =====================================================
-- LoginPolicy Table Migration
-- =====================================================
-- Description: Jenkins-style global login policy system
-- Author: Backend Team
-- Date: 2025-01-15
-- =====================================================

-- Create login_policy table
CREATE TABLE login_policy (
    id CHAR(26) PRIMARY KEY,                              -- ULID identifier
    name VARCHAR(100) NOT NULL,                           -- Policy name (e.g., "시스템 로그인 정책")
    enabled_methods VARCHAR(1000) NOT NULL,               -- Enabled login methods: JSON array string
    priority VARCHAR(1000) NOT NULL,                      -- Priority order: JSON array string
    active BOOLEAN DEFAULT true,                          -- Is this policy active?
    created_by VARCHAR(50),                               -- User who created this policy
    created_at TIMESTAMP DEFAULT NOW(),                   -- Creation timestamp
    updated_by VARCHAR(50),                               -- User who last updated
    updated_at TIMESTAMP                                  -- Last update timestamp
    
    -- Note: Validation of enabled_methods and priority (at least 1 element)
    -- is handled at application level in LoginPolicy aggregate root
);

-- Indexes
-- Note: Partial index (WHERE clause) is PostgreSQL-specific and not supported in H2
-- Uniqueness of global policy is enforced at application level
CREATE INDEX idx_login_policy_name ON login_policy(name);

-- Comments
COMMENT ON TABLE login_policy IS 'Jenkins-style global login policy configuration';
COMMENT ON COLUMN login_policy.id IS 'ULID primary key';
