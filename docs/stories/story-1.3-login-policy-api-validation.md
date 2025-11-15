# Story 1.3: LoginPolicy API Endpoints & Validation

Status: completed

## Story

As a **backend developer**,
I want to **create REST API endpoints for login policy management with validation**,
so that **administrators can configure the global login policy and users can query available methods**.

## Acceptance Criteria

1. GET /api/v1/system/login-policy returns global policy (public access)
2. GET /api/v1/system/login-policy/available-methods returns enabled methods (public)
3. PUT /api/v1/system/login-policy updates entire policy (admin only)
4. PATCH /api/v1/system/login-policy/methods updates enabled methods (admin)
5. PATCH /api/v1/system/login-policy/priority updates priority (admin)
6. Request DTOs validated (Jakarta Validation)
7. Authorization enforced (ROLE_SYSTEM_ADMIN for modification)
8. All controller tests pass (WebMvcTest)

## Tasks / Subtasks

- [x] DTOs (AC: 6)
  - [x] Create LoginPolicyResponse DTO
  - [x] Create UpdateLoginPolicyRequest DTO (@NotNull, @Size validations)
  - [x] Create UpdateMethodsRequest DTO
  - [x] Create UpdatePriorityRequest DTO

- [x] Controller Implementation (AC: 1-5)
  - [x] Create SystemConfigController
  - [x] GET /api/v1/system/login-policy endpoint
  - [x] GET /api/v1/system/login-policy/available-methods endpoint
  - [x] PUT /api/v1/system/login-policy endpoint
  - [x] PATCH /api/v1/system/login-policy/methods endpoint
  - [x] PATCH /api/v1/system/login-policy/priority endpoint
  - [x] Map entities to DTOs

- [x] Authorization (AC: 7)
  - [x] @PreAuthorize("hasRole('ADMIN')") on all endpoints
  - [x] Integration with Spring Security

- [x] Validation Rules (AC: 6)
  - [x] At least 1 enabled method required
  - [x] Priority must match enabled methods (auto-adjusted in service layer)
  - [x] Valid LoginMethod values only
  - [x] Name max length 100 characters

- [x] Controller Tests (AC: 8)
  - [x] SystemConfigControllerTest (@WebMvcTest)
  - [x] Test GET endpoints (200 OK)
  - [x] Test PUT with valid data (200 OK)
  - [x] Test PUT with invalid data (400 Bad Request)
  - [x] Test PATCH endpoints (200 OK)
  - [x] All tests pass

## Dev Notes

### API Design
```
Public APIs:
  GET  /api/v1/system/login-policy              → LoginPolicyResponse
  GET  /api/v1/system/login-policy/available-methods → Set<LoginMethod>

Admin APIs (ROLE_SYSTEM_ADMIN):
  PUT   /api/v1/system/login-policy             → LoginPolicyResponse
  PATCH /api/v1/system/login-policy/methods     → LoginPolicyResponse
  PATCH /api/v1/system/login-policy/priority    → LoginPolicyResponse
```

### Validation Examples
```java
public class UpdateLoginPolicyRequest {
    @NotBlank(message = "정책 이름은 필수입니다")
    @Size(max = 100, message = "정책 이름은 100자를 초과할 수 없습니다")
    private String name;

    @NotEmpty(message = "최소 1개의 로그인 방식이 활성화되어야 합니다")
    private Set<LoginMethod> enabledMethods;

    @NotEmpty(message = "우선순위는 비어있을 수 없습니다")
    private List<LoginMethod> priority;
}
```

### References

- [Source: docs/development/cross-cutting/login-policy.md#API-Endpoints]
- [Source: docs/api/CONTRACT.md#REST-Conventions]

## Dev Agent Record

### Context Reference

<!-- Will be added by story-context workflow -->

### Agent Model Used

<!-- Will be set during implementation -->

### File List

<!-- Will be populated during implementation -->

---

**Status**: ✅ COMPLETED
**Depends On**: Story 1.2
**Estimate**: 4-6 hours
**Actual**: 4 hours (TDD workflow)
