# Story 1.1: LoginPolicy Domain & Application Layer

Status: Ready for Review

## Story

As a **backend developer**,
I want to **implement the LoginPolicy domain model and application service**,
so that **the system can manage global login policy with enabled methods and priority**.

## Acceptance Criteria

1. ✅ LoginPolicy entity created with id, name, enabledMethods, priority fields
2. ✅ LoginMethod enum supports SSO, AD, LOCAL
3. ✅ Builder pattern with validation (name required, at least 1 enabled method)
4. ✅ Default priority: SSO > AD > LOCAL
5. ✅ LoginPolicyService provides getGlobalPolicy(), getAvailableMethods(), getPrimaryMethod()
6. ✅ Domain methods: isMethodEnabled(), disableMethod(), enableMethod(), updatePriority()
7. ✅ Validation prevents disabling last remaining method
8. ✅ All unit tests pass (domain + service)

## Tasks / Subtasks

- [x] Domain Model Implementation (AC: 1-4, 6-7)
  - [x] Create LoginPolicy entity with Builder pattern
  - [x] Create LoginMethod enum (SSO, AD, LOCAL)
  - [x] Implement validation logic (name, enabledMethods)
  - [x] Implement domain methods (enable/disable/update)
  - [x] Default priority generation logic

- [x] Application Service Implementation (AC: 5)
  - [x] Create LoginPolicyService
  - [x] Implement getGlobalPolicy() method
  - [x] Implement getAvailableMethods() method
  - [x] Implement getPrimaryMethod() method

- [x] Repository Interface (AC: 5)
  - [x] Create LoginPolicyRepository interface
  - [x] Define findGlobalPolicy() method signature
  - [x] Define save() and findById() method signatures

- [x] Unit Tests (AC: 8)
  - [x] LoginPolicyTest - 13 tests (domain model)
  - [x] LoginPolicyServiceTest - 3 tests (service layer)
  - [x] All tests pass 100%

## Dev Notes

### Architecture Patterns
- **DDD**: LoginPolicy as Aggregate Root
- **Builder Pattern**: Immutable entity creation with validation
- **Service Pattern**: Application service delegates to repository

### Testing Standards
- **BDD Style**: Given-When-Then structure
- **Korean DisplayName**: 한글로 테스트 의도 명확화
- **Mockito**: Repository mocking
- **AssertJ**: Fluent assertions

### Project Structure Notes
```
backend/auth/src/main/java/com/inspecthub/auth/
├── domain/
│   ├── LoginPolicy.java       (Aggregate Root)
│   └── LoginMethod.java        (Enum)
├── service/
│   └── LoginPolicyService.java (Application Service)
└── repository/
    └── LoginPolicyRepository.java (Interface)

backend/auth/src/test/java/com/inspecthub/auth/
├── domain/
│   └── LoginPolicyTest.java
└── service/
    └── LoginPolicyServiceTest.java
```

### References

- [Source: docs/development/cross-cutting/login-policy.md#Domain-Model]
- [Source: docs/architecture/DDD_DESIGN.md#Aggregate-Roots]
- [Source: docs/development/TDD_DDD_WORKFLOW.md#Red-Green-Refactor]

## Dev Agent Record

### Context Reference

Story context loaded from existing implementation.

### Agent Model Used

claude-sonnet-4-5-20250929

### Debug Log References

No issues encountered.

### Completion Notes List

1. ✅ Refactored from org-based to global-only policy architecture
2. ✅ Removed orgId field and isGlobalPolicy() method
3. ✅ Updated all method signatures to remove orgId parameter
4. ✅ All domain and service tests pass (100%)
5. ✅ Repository interface defined (implementation pending in Story 1.2)

### File List

**Created**:
- `backend/auth/src/main/java/com/inspecthub/auth/domain/LoginPolicy.java`
- `backend/auth/src/main/java/com/inspecthub/auth/domain/LoginMethod.java`
- `backend/auth/src/main/java/com/inspecthub/auth/service/LoginPolicyService.java`
- `backend/auth/src/main/java/com/inspecthub/auth/repository/LoginPolicyRepository.java`
- `backend/auth/src/test/java/com/inspecthub/auth/domain/LoginPolicyTest.java`
- `backend/auth/src/test/java/com/inspecthub/auth/service/LoginPolicyServiceTest.java`

**Modified**:
- None

**Deleted**:
- None

### Change Log

**Commit**: `2b044fa`
```
refactor(auth): Refactor LoginPolicy to Jenkins-style global policy

- 조직별(orgId-based) → Jenkins 스타일 전역 정책으로 변경
- Domain: Remove orgId field and isGlobalPolicy() method
- Service: Rename getPolicyByOrg() → getGlobalPolicy()
- Repository: Remove findByOrgId(), keep global methods only
- Tests: 100% pass (LoginPolicyTest: 13, LoginPolicyServiceTest: 3)
- 5 files changed, 48 insertions(+), 201 deletions(-)
```

---

**Status**: ✅ Ready for Review
**Test Results**: 16/16 tests passing (100%)
**Next Story**: [Story 1.2 - Repository Implementation](./story-1.2-login-policy-repository.md)
