# Story 1.5: LoginPolicy Error Handling & Integration Testing

Status: drafted

## Story

As a **backend developer**,
I want to **implement comprehensive error handling and integration test scenarios**,
so that **the system handles edge cases gracefully and all workflows are tested end-to-end**.

## Acceptance Criteria

1. Custom exceptions defined (PolicyNotFoundException, etc.)
2. GlobalExceptionHandler maps exceptions to HTTP status codes
3. Integration tests cover all scenarios (policy changes during login, session expiry, etc.)
4. Regression tests ensure existing features still work
5. All tests pass (unit + integration)

## Tasks / Subtasks

- [ ] Custom Exceptions (AC: 1)
  - [ ] PolicyNotFoundException (404)
  - [ ] MethodNotAllowedException (400)
  - [ ] InvalidMethodException (400)
  - [ ] EmptyMethodsException (400)
  - [ ] LastMethodDisableException (400)

- [ ] Error Handling (AC: 2)
  - [ ] Update GlobalExceptionHandler
  - [ ] Map custom exceptions to ApiResponse
  - [ ] HTTP status codes (404, 400, 403)
  - [ ] Error message localization (Korean)

- [ ] Integration Test Scenarios (AC: 3)
  - [ ] Scenario 1: Default login flow (SSO → AD → LOCAL fallback)
  - [ ] Scenario 2: Admin enables only SSO
  - [ ] Scenario 3: Session expiry and re-login
  - [ ] Scenario 4: Policy change during active sessions
  - [ ] All scenarios pass

- [ ] Regression Tests (AC: 4)
  - [ ] Run all existing auth module tests
  - [ ] Verify domain/service/repository tests still pass
  - [ ] Fix any breaking changes from LoginPolicy refactoring

## Dev Notes

### Error Response Format
```json
{
  "success": false,
  "message": "로그인 정책을 찾을 수 없습니다",
  "errorCode": "POLICY_NOT_FOUND",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Integration Test Example
```java
@Test
@DisplayName("Scenario 1: Default login flow with fallback")
void shouldFallbackFromSsoToAdToLocal() {
    // Given: System policy = [SSO, AD, LOCAL]
    // When: SSO fails → AD fails → LOCAL succeeds
    // Then: User logged in via LOCAL
}
```

### References

- [Source: docs/development/cross-cutting/login-policy.md#Testing-Scenarios]
- [Source: docs/development/cross-cutting/login-policy.md#Error-Handling]

## Dev Agent Record

### File List

<!-- Will be populated during implementation -->

---

**Status**: 📝 Drafted
**Depends On**: Story 1.4
**Estimate**: 4-6 hours
