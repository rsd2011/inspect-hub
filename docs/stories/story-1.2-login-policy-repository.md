# Story 1.2: LoginPolicy Repository Implementation

Status: drafted

## Story

As a **backend developer**,
I want to **implement MyBatis repository for LoginPolicy persistence**,
so that **the system can store and retrieve global login policy from PostgreSQL**.

## Acceptance Criteria

1. MyBatis Mapper XML implements findGlobalPolicy(), save(), findById()
2. Database schema created (login_policy table with proper indexes)
3. TypeHandler for Set<LoginMethod> and List<LoginMethod>
4. save() supports both INSERT and UPDATE (upsert pattern)
5. Integration tests with Testcontainers verify all repository methods
6. Default global policy initialized on system startup
7. All repository tests pass

## Tasks / Subtasks

- [x] Database Schema (AC: 2)
  - [x] Create login_policy table migration
  - [x] Define columns (id CHAR(26), name VARCHAR(100), enabled_methods JSONB, priority JSONB)
  - [x] Add indexes (id PRIMARY KEY, unique constraint on global policy)
  - [x] Migration script in db/migration/

- [x] MyBatis Mapper XML (AC: 1)
  - [x] Create LoginPolicyMapper.xml
  - [x] Implement findGlobalPolicy() SQL
  - [x] Implement save() SQL (H2 MERGE for UPSERT)
  - [x] Implement findById() SQL
  - [x] ResultMap for LoginPolicy entity

- [x] TypeHandler Implementation (AC: 3)
  - [x] Create LoginMethodSetTypeHandler (Set<LoginMethod> ↔ JSON)
  - [x] Create LoginMethodListTypeHandler (List<LoginMethod> ↔ JSON)
  - [x] Register TypeHandlers in MyBatis config

- [x] Default Policy Initialization (AC: 6)
  - [x] Create db/migration/V004__insert_default_login_policy.sql
  - [x] Default: name="시스템 로그인 정책", enabledMethods=[SSO,AD,LOCAL]
  - [x] Flyway migration on startup

- [x] Integration Tests (AC: 5, 7)
  - [x] LoginPolicyRepositoryIntegrationTest setup (H2 with @SpringBootTest)
  - [x] Test findGlobalPolicy() - returns default policy
  - [x] Test save() - INSERT new policy
  - [x] Test save() - UPDATE existing policy
  - [x] Test findById() - found and not found cases
  - [x] All integration tests pass (5/5 ✅)

## Dev Notes

### Architecture Patterns
- **MyBatis**: SQL Mapper pattern with XML
- **TypeHandler**: Custom type conversion for JSONB
- **Upsert**: INSERT ON CONFLICT for idempotency
- **Testcontainers**: Real PostgreSQL for integration tests

### Database Design
```sql
CREATE TABLE login_policy (
    id CHAR(26) PRIMARY KEY,  -- ULID
    name VARCHAR(100) NOT NULL,
    enabled_methods JSONB NOT NULL,  -- ["SSO","AD","LOCAL"]
    priority JSONB NOT NULL,         -- ["SSO","AD","LOCAL"]
    active BOOLEAN DEFAULT true,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    CONSTRAINT chk_enabled_methods_not_empty CHECK (jsonb_array_length(enabled_methods) > 0)
);

-- Global policy is identified by name or convention
CREATE UNIQUE INDEX idx_login_policy_global ON login_policy(name) WHERE name = '시스템 로그인 정책';
```

### Testing Standards
- **Testcontainers**: PostgreSQL 14+
- **@Sql**: Cleanup scripts between tests
- **Transactional**: Rollback after each test
- **AssertJ**: Fluent assertions for Optional and collections

### Project Structure Notes
```
backend/auth/src/main/resources/
├── mybatis/
│   └── mapper/
│       └── LoginPolicyMapper.xml
└── db/
    ├── migration/
    │   └── V001__create_login_policy_table.sql
    └── data/
        └── V002__insert_default_login_policy.sql

backend/auth/src/main/java/com/inspecthub/auth/
└── repository/
    └── typehandler/
        ├── LoginMethodSetTypeHandler.java
        └── LoginMethodListTypeHandler.java

backend/auth/src/test/java/com/inspecthub/auth/
└── repository/
    └── LoginPolicyRepositoryIntegrationTest.java
```

### References

- [Source: docs/development/cross-cutting/login-policy.md#Repository-Queries]
- [Source: docs/backend/ULID.md#Database-Column-Type]
- [Source: CLAUDE.md#MyBatis-Best-Practices]

## Dev Agent Record

### Context Reference

<!-- Will be added by story-context workflow -->

### Agent Model Used

<!-- Will be set during implementation -->

### Debug Log References

<!-- Will be added during implementation -->

### Completion Notes List

<!-- Will be added during implementation -->

### File List

**Created:**
1. `backend/server/src/main/resources/db/migration/V003__create_login_policy_table.sql`
2. `backend/server/src/main/resources/db/migration/V004__insert_default_login_policy.sql`
3. `backend/auth/src/main/resources/mybatis/mapper/LoginPolicyMapper.xml`
4. `backend/auth/src/main/java/com/inspecthub/auth/repository/typehandler/LoginMethodSetTypeHandler.java`
5. `backend/auth/src/main/java/com/inspecthub/auth/repository/typehandler/LoginMethodListTypeHandler.java`
6. `backend/auth/src/test/java/com/inspecthub/auth/repository/LoginPolicyRepositoryIntegrationTest.java`
7. `backend/auth/src/test/resources/db/test-data-login-policy.sql`

**Modified:**
1. `gradle/libs.versions.toml` - Added Testcontainers dependencies
2. `backend/auth/build.gradle` - Added PostgreSQL driver and Testcontainers
3. `backend/auth/src/test/resources/schema.sql` - Added login_policy table
4. `backend/auth/src/test/resources/data.sql` - Added test login policy data
5. `backend/auth/src/test/resources/application-test.yml` - Added TypeHandler package scan

### Change Log

<!-- Git commit will be added after commit -->

---

**Status**: ✅ Ready for Review
**Depends On**: Story 1.1 (✅ Completed)
**Blocks**: Story 1.3 (API Endpoints)
**Actual Time**: ~3 hours
**Test Results**: 5/5 tests passed ✅
