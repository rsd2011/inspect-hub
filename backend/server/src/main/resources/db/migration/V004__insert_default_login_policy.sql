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
    '["SSO", "AD", "LOCAL"]'::jsonb,                      -- All methods enabled by default
    '["SSO", "AD", "LOCAL"]'::jsonb,                      -- Default priority: SSO > AD > LOCAL
    true,                                                  -- Active
    'SYSTEM',                                              -- Created by system
    NOW()                                                  -- Current timestamp
);

-- Verify insertion
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM login_policy WHERE name = '시스템 로그인 정책') THEN
        RAISE EXCEPTION 'Default login policy was not inserted correctly';
    END IF;
END $$;
