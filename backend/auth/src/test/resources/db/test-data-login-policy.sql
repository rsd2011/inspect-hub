-- =====================================================
-- Test Data for LoginPolicy Integration Tests
-- =====================================================

-- Clean up existing data
DELETE FROM login_policy;

-- Insert test global login policy
INSERT INTO login_policy (
    id,
    name,
    enabled_methods,
    priority,
    active,
    created_by,
    created_at
) VALUES (
    '01JCXYZ1234567890ABCDEF002',
    '시스템 로그인 정책',
    '["SSO", "AD", "LOCAL"]'::jsonb,
    '["SSO", "AD", "LOCAL"]'::jsonb,
    true,
    'SYSTEM',
    NOW()
);
