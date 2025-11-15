# Story 1.6: System Configuration UI (Admin Page)

Status: drafted

## Story

As a **system administrator**,
I want a **web UI to configure login policy**,
so that **I can enable/disable authentication methods and set priorities without touching the database**.

## Acceptance Criteria

1. Page: /admin/system/login-policy (Nuxt 4 SPA)
2. Checkboxes to enable/disable SSO, AD, LOCAL
3. Drag & drop to reorder priority
4. Real-time preview of login page
5. Save button updates policy via API
6. Only ROLE_SYSTEM_ADMIN can access
7. Success/error notifications (PrimeVue Toast)
8. Playwright E2E tests pass

## Tasks / Subtasks

- [ ] Nuxt Page Component (AC: 1)
  - [ ] Create pages/admin/system/login-policy.vue
  - [ ] Fetch current policy from API (GET /api/v1/system/login-policy)
  - [ ] Display current settings

- [ ] Enable/Disable Controls (AC: 2)
  - [ ] PrimeVue Checkbox for SSO
  - [ ] PrimeVue Checkbox for AD
  - [ ] PrimeVue Checkbox for LOCAL
  - [ ] Disable checkbox if last remaining method
  - [ ] Reactivity with Pinia store

- [ ] Priority Drag & Drop (AC: 3)
  - [ ] Use PrimeVue OrderList or custom drag & drop
  - [ ] Visual feedback during drag
  - [ ] Auto-update priority array

- [ ] Login Page Preview (AC: 4)
  - [ ] Mock login page component
  - [ ] Show/hide tabs based on enabled methods
  - [ ] Display in priority order

- [ ] Save Functionality (AC: 5, 7)
  - [ ] PUT /api/v1/system/login-policy on save
  - [ ] Loading spinner during API call
  - [ ] Success toast notification
  - [ ] Error toast notification
  - [ ] Invalidate cache after save

- [ ] Authorization (AC: 6)
  - [ ] Middleware: requireAuth with role check
  - [ ] Redirect to /login if unauthorized
  - [ ] Display "Access Denied" if wrong role

- [ ] E2E Tests (AC: 8)
  - [ ] Playwright test: Admin can enable/disable methods
  - [ ] Playwright test: Admin can reorder priority
  - [ ] Playwright test: Non-admin cannot access
  - [ ] All E2E tests pass

## Dev Notes

### Component Structure
```
frontend/pages/admin/system/login-policy.vue
frontend/components/admin/
├── LoginPolicySettings.vue        (Main settings form)
├── LoginMethodCheckbox.vue        (Checkbox for each method)
├── PriorityOrderList.vue          (Drag & drop list)
└── LoginPagePreview.vue           (Preview component)

frontend/stores/
└── loginPolicyStore.ts            (Pinia store)
```

### UI Wireframe
```
┌────────────────────────────────────────┐
│ System Configuration > Login Policy   │
├────────────────────────────────────────┤
│ ☑ SSO (Single Sign-On)                │
│ ☑ AD (Active Directory)               │
│ ☑ LOCAL (Database Authentication)     │
├────────────────────────────────────────┤
│ Priority (Drag to reorder):            │
│   1. SSO                               │
│   2. AD                                │
│   3. LOCAL                             │
├────────────────────────────────────────┤
│ Preview:                               │
│  [ SSO Tab | AD Tab | LOCAL Tab ]     │
├────────────────────────────────────────┤
│          [Cancel]  [Save Changes]      │
└────────────────────────────────────────┘
```

### Pinia Store
```typescript
export const useLoginPolicyStore = defineStore('loginPolicy', {
  state: () => ({
    policy: null as LoginPolicy | null,
    loading: false,
    error: null as string | null
  }),
  actions: {
    async fetchPolicy() { ... },
    async updatePolicy(data: UpdateLoginPolicyRequest) { ... },
    toggleMethod(method: LoginMethod) { ... },
    updatePriority(newPriority: LoginMethod[]) { ... }
  }
})
```

### References

- [Source: docs/development/cross-cutting/login-policy.md#System-Configuration-UI]
- [Source: docs/frontend/README.md#Component-Structure]
- [Source: docs/frontend/COMPONENTS_ROADMAP.md]

## Dev Agent Record

### File List

<!-- Will be populated during implementation -->

---

**Status**: 📝 Drafted
**Depends On**: Story 1.3 (API must exist first)
**Estimate**: 6-8 hours
