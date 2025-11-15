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
    enabled_methods JSONB NOT NULL,                       -- Enabled login methods: ["SSO", "AD", "LOCAL"]
    priority JSONB NOT NULL,                              -- Priority order: ["SSO", "AD", "LOCAL"]
    active BOOLEAN DEFAULT true,                          -- Is this policy active?
    created_by VARCHAR(50),                               -- User who created this policy
    created_at TIMESTAMP DEFAULT NOW(),                   -- Creation timestamp
    updated_by VARCHAR(50),                               -- User who last updated
    updated_at TIMESTAMP,                                 -- Last update timestamp
    
    -- Constraints
    CONSTRAINT chk_enabled_methods_not_empty 
        CHECK (jsonb_array_length(enabled_methods) > 0),  -- At least 1 method must be enabled
    CONSTRAINT chk_priority_not_empty 
        CHECK (jsonb_array_length(priority) > 0)          -- Priority list must not be empty
);

-- Indexes
CREATE UNIQUE INDEX idx_login_policy_global 
    ON login_policy(name) 
    WHERE name = '시스템 로그인 정책';                      -- Ensure only one global policy exists

-- Comments
COMMENT ON TABLE login_policy IS 'Jenkins-style global login policy configuration';
COMMENT ON COLUMN login_policy.id IS 'ULID primary key';
