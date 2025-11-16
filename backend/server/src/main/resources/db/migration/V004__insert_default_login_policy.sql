-- =====================================================
-- Default LoginPolicy Data Migration
-- =====================================================
-- Description: Insert default global login policy
-- Author: Backend Team
-- Date: 2025-01-15
-- =====================================================

-- Insert default global login policy
-- ULID: 01JCXYZ1234567890ABCDEF002 (fixed for consistency)
INSERT INTO login_policy (
    id,
    name,
    enabled_methods,
    priority,
    active,
    created_by,
    created_at
) VALUES (
    '01JCXYZ1234567890ABCDEF002',                         -- Fixed ULID for global policy
    '시스템 로그인 정책',                                    -- Global policy name
    '["SSO", "AD", "LOCAL"]',                             -- All methods enabled by default (JSON string)
    '["SSO", "AD", "LOCAL"]',                             -- Default priority: SSO > AD > LOCAL (JSON string)
    true,                                                  -- Active
    'SYSTEM',                                              -- Created by system
    NOW()                                                  -- Current timestamp
);

-- Note: Verification block (DO $$) is PostgreSQL-specific and not supported in H2
-- Insertion verification is handled by foreign key constraints and application-level validation
