# Epic 001: Login Policy System (Jenkins Style)

**Status**: In Progress
**Priority**: High
**Target**: Sprint 3-4
**Owner**: Dev Team

---

## Epic Overview

As a **system administrator**,
I want a **Jenkins-style global login policy management system**,
so that I can **centrally configure and control authentication methods for the entire organization**.

---

## Business Value

- **Centralized Control**: Single system-wide policy (no per-organization complexity)
- **Flexibility**: Enable/disable SSO, AD, LOCAL login methods dynamically
- **Security**: Enforce authentication priority and audit all changes
- **User Experience**: Automatic fallback (SSO → AD → LOCAL) for seamless login

---

## Architecture

- **Pattern**: Jenkins-style system settings
- **Scope**: Single global policy (no orgId-based multi-tenancy)
- **Priority**: SSO > AD > LOCAL (configurable)
- **Storage**: Database + Redis cache

---

## Stories

### ✅ Story 1: Domain & Application Layer
**Status**: ✅ COMPLETED (13% of total Epic)

Core domain model and service layer implementation.

**Completed Work**:
- ✅ LoginPolicy entity (id, name, enabledMethods, priority)
- ✅ LoginMethod enum (SSO, AD, LOCAL)
- ✅ LoginPolicyService (getGlobalPolicy, getAvailableMethods, getPrimaryMethod)
- ✅ Domain validation logic
- ✅ Unit tests (100% pass)

**File**: [Story 1.1 - Domain & Application Layer](../stories/story-1.1-login-policy-domain-application.md)

---

### ✅ Story 2: Repository Implementation
**Status**: ✅ COMPLETED (24% of total Epic)

MyBatis Mapper implementation for LoginPolicy persistence.

**Delivered**:
- ✅ Database schema migration (V003, V004)
- ✅ MyBatis Mapper XML (findGlobalPolicy, save, findById)
- ✅ TypeHandlers for JSON serialization
- ✅ Integration tests (5/5 passed)
- ✅ H2/PostgreSQL compatibility

**File**: [Story 1.2 - Repository Implementation](../stories/story-1.2-login-policy-repository.md)

---

### ✅ Story 3: API Endpoints & Validation
**Status**: ✅ COMPLETED (38% of total Epic)

REST API for policy management with validation.

**Delivered**:
- ✅ SystemConfigController (5 REST endpoints)
- ✅ LoginPolicyResponse DTO (immutable collections with defensive copying)
- ✅ UpdateLoginPolicyRequest, UpdateMethodsRequest, UpdatePriorityRequest DTOs
- ✅ Jakarta Bean Validation (@NotBlank, @NotEmpty, @Size)
- ✅ @PreAuthorize("hasRole('ADMIN')") for all endpoints
- ✅ LoginPolicyService update methods (updateGlobalPolicy, updateEnabledMethods, updatePriority)
- ✅ Controller integration tests (5/5 passed)
- ✅ Service unit tests (6/6 passed)
- ✅ DTO validation tests (all passed)

**File**: [Story 1.3 - API Endpoints & Validation](../stories/story-1.3-login-policy-api-validation.md)

---

### 🔨 Story 4: Caching & Audit Logging
**Status**: ⏳ TODO

Performance optimization and compliance.

**Scope**:
- Redis caching strategy
- Cache invalidation on policy changes
- Audit logging for all policy modifications
- Audit log details (before/after state)

**File**: [Story 1.4 - Caching & Audit Logging](../stories/story-1.4-login-policy-caching-audit.md)

---

### 🔨 Story 5: Error Handling & Testing Scenarios
**Status**: ⏳ TODO

Comprehensive error handling and integration testing.

**Scope**:
- Custom exceptions (PolicyNotFoundException, etc.)
- HTTP error responses (404, 400, 403)
- Integration test scenarios (session expiry, policy changes)
- Regression testing

**File**: [Story 1.5 - Error Handling & Testing](../stories/story-1.5-login-policy-error-testing.md)

---

### 🔨 Story 6: System Configuration UI
**Status**: ⏳ TODO

Admin UI for policy management (Nuxt 4 SPA).

**Scope**:
- System settings page (/admin/system/login-policy)
- Enable/disable checkboxes (SSO, AD, LOCAL)
- Drag & drop priority reordering
- Real-time preview of login page
- ROLE_SYSTEM_ADMIN permission required

**File**: [Story 1.6 - System Configuration UI](../stories/story-1.6-login-policy-ui.md)

---

## Success Criteria

- [x] Domain model supports global-only policy
- [ ] All policies stored in PostgreSQL with Redis caching
- [ ] Admin can configure login methods via UI
- [ ] Policy changes are audited (100% coverage)
- [ ] Non-admin users see only enabled login methods
- [ ] Automatic fallback works (SSO → AD → LOCAL)
- [ ] All tests pass (unit, integration, E2E)

---

## Technical Constraints

- **Backend**: Spring Boot 3.3.2, MyBatis, PostgreSQL, Redis
- **Frontend**: Nuxt 4 SPA (SSR disabled), PrimeVue
- **Testing**: JUnit 5, Mockito, Testcontainers, Playwright
- **Architecture**: DDD layers, BDD tests (Given-When-Then)

---

## Dependencies

- **Upstream**: User authentication system
- **Downstream**: Login page (frontend), AuthService integration

---

## References

- **Test Plan**: [docs/development/cross-cutting/login-policy.md](../development/cross-cutting/login-policy.md)
- **Architecture**: [docs/architecture/DDD_DESIGN.md](../architecture/DDD_DESIGN.md)
- **TDD Workflow**: [docs/development/TDD_DDD_WORKFLOW.md](../development/TDD_DDD_WORKFLOW.md)

---

**Last Updated**: 2025-01-15
