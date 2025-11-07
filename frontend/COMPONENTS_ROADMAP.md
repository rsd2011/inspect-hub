# Frontend Components Roadmap

프론트엔드 공통 컴포넌트 및 시스템 클래스 구현 로드맵입니다.

## Progress Tracking

- 🔴 Not Started
- 🟡 In Progress
- 🟢 Completed

## UI Components

### shared/ui/atoms (기본 UI 요소)

| Component | Status | Priority | Location |
|-----------|--------|----------|----------|
| Button | 🟢 | High | `shared/ui/atoms/Button.vue` |
| Input | 🟢 | High | `shared/ui/atoms/Input.vue` |
| Badge | 🔴 | Medium | `shared/ui/atoms/Badge.vue` |
| Icon | 🔴 | High | `shared/ui/atoms/Icon.vue` |
| Skeleton | 🔴 | Medium | `shared/ui/atoms/Skeleton.vue` |
| Avatar | 🔴 | Low | `shared/ui/atoms/Avatar.vue` |
| Label | 🟢 | High | `shared/ui/atoms/Label.vue` |
| Checkbox | 🔴 | High | `shared/ui/atoms/Checkbox.vue` |
| Radio | 🔴 | Medium | `shared/ui/atoms/Radio.vue` |
| Switch | 🔴 | Medium | `shared/ui/atoms/Switch.vue` |

### shared/ui/molecules (간단한 조합 컴포넌트)

| Component | Status | Priority | Location |
|-----------|--------|----------|----------|
| FormField | 🔴 | High | `shared/ui/molecules/FormField.vue` |
| SearchBox | 🔴 | High | `shared/ui/molecules/SearchBox.vue` |
| FileUpload | 🔴 | Medium | `shared/ui/molecules/FileUpload.vue` |
| ThemeToggle | 🔴 | Low | `shared/ui/molecules/ThemeToggle.vue` |
| DateRangePicker | 🔴 | Medium | `shared/ui/molecules/DateRangePicker.vue` |
| StatusBadge | 🔴 | Medium | `shared/ui/molecules/StatusBadge.vue` |
| ActionButtons | 🔴 | High | `shared/ui/molecules/ActionButtons.vue` |

### shared/ui/organisms (복잡한 조합 컴포넌트)

| Component | Status | Priority | Location |
|-----------|--------|----------|----------|
| DataTable | 🔴 | High | `shared/ui/organisms/DataTable.vue` |
| Modal | 🔴 | High | `shared/ui/organisms/Modal.vue` |
| Toast | 🔴 | High | `shared/ui/organisms/Toast.vue` |
| Alert | 🔴 | High | `shared/ui/organisms/Alert.vue` |
| SlidePanel | 🔴 | Medium | `shared/ui/organisms/SlidePanel.vue` |
| FileViewer | 🔴 | Medium | `shared/ui/organisms/FileViewer.vue` |
| MarkdownEditor | 🔴 | Low | `shared/ui/organisms/MarkdownEditor.vue` |
| HelpViewer | 🔴 | Low | `shared/ui/organisms/HelpViewer.vue` |
| FormBuilder | 🔴 | High | `shared/ui/organisms/FormBuilder.vue` |
| SearchPanel | 🔴 | High | `shared/ui/organisms/SearchPanel.vue` |

### widgets (대형 페이지 블록)

| Component | Status | Priority | Location |
|-----------|--------|----------|----------|
| TabManager | 🔴 | High | `widgets/tab-manager/` |
| MenuNavigation | 🟢 | High | `widgets/menu-navigation/` |
| AppHeader | 🟢 | High | `widgets/header/` |
| AppSidebar | 🟢 | High | `widgets/sidebar/` |
| AppFooter | 🔴 | Low | `widgets/footer/` |
| Breadcrumb | 🔴 | Medium | `widgets/breadcrumb/` |
| NotificationWidget | 🔴 | Medium | `widgets/notification-widget/` |
| ScreenCapture | 🔴 | Low | `widgets/screen-capture/` |

### features (사용자 기능)

| Feature | Status | Priority | Location |
|---------|--------|----------|----------|
| Attachment | 🔴 | High | `features/attachment/` |
| Memo | 🔴 | Medium | `features/memo/` |
| Theme | 🔴 | Low | `features/theme/` |
| Help | 🔴 | Low | `features/help/` |
| Notification | 🔴 | Medium | `features/notification/` |
| Hotkey | 🔴 | Low | `features/hotkey/` |

## System Management Classes

### shared/lib (시스템 관리 클래스)

| Class | Status | Priority | Location |
|-------|--------|----------|----------|
| CodeManager | 🔴 | High | `shared/lib/code-manager/` |
| ApiClient | 🟢 | High | `shared/api/client.ts` |
| SSEClient | 🔴 | Medium | `shared/lib/sse-client/` |
| SessionManager | 🟢 | High | `shared/lib/session-manager/` |
| PermissionManager | 🟢 | High | `shared/lib/permission-manager/` |
| I18nManager | 🔴 | Medium | `shared/lib/i18n-manager/` |
| PageStateManager | 🔴 | High | `shared/lib/page-state-manager/` |
| AuditLogger | 🔴 | Medium | `shared/lib/audit-logger/` |
| LoadingManager | 🟢 | High | `shared/lib/loading-manager/` |

## Implementation Priority

### Phase 1: Core Infrastructure (Sprint 1-2) ✅ COMPLETED
**Goal**: 기본 인프라 및 레이아웃 구축

- 🟢 SessionManager
- 🟢 ApiClient (enhanced)
- 🟢 PermissionManager
- 🟢 LoadingManager
- 🟢 AppHeader
- 🟢 AppSidebar
- 🟢 MenuNavigation
- 🟢 Button, Input, Label (atoms)

### Phase 2: Common UI Components (Sprint 3-4)
**Goal**: 재사용 가능한 공통 컴포넌트

- 🔴 FormField, FormBuilder
- 🔴 Modal, Toast, Alert
- 🔴 DataTable (RealGrid wrapper)
- 🔴 SearchPanel
- 🔴 Skeleton
- 🔴 Badge, Icon, Avatar

### Phase 3: Advanced Features (Sprint 5-6)
**Goal**: 고급 기능 및 사용자 편의성

- 🔴 TabManager
- 🔴 PageStateManager
- 🔴 Attachment feature
- 🔴 CodeManager
- 🔴 Breadcrumb
- 🔴 FileUpload, FileViewer

### Phase 4: Real-time & Enhancements (Sprint 7-8)
**Goal**: 실시간 기능 및 사용성 향상

- 🔴 SSEClient
- 🔴 NotificationWidget
- 🔴 Notification feature
- 🔴 I18nManager
- 🔴 Memo feature
- 🔴 AuditLogger

### Phase 5: Polish & Optimization (Sprint 9-10)
**Goal**: 최적화 및 부가 기능

- 🔴 Theme feature (Dark mode)
- 🔴 Help feature
- 🔴 Hotkey feature
- 🔴 MarkdownEditor
- 🔴 HelpViewer
- 🔴 ScreenCapture
- 🔴 Accessibility improvements

## Component Dependencies

### Critical Path
```
SessionManager → ApiClient → PermissionManager
                    ↓
              LoadingManager
                    ↓
          MenuNavigation (권한 기반)
                    ↓
              TabManager
                    ↓
          PageStateManager
                    ↓
        Feature Components
```

### UI Component Hierarchy
```
Atoms (Button, Input, Label)
  ↓
Molecules (FormField, SearchBox)
  ↓
Organisms (FormBuilder, DataTable, Modal)
  ↓
Widgets (TabManager, MenuNavigation)
  ↓
Features (Attachment, Memo, Notification)
```

## Testing Strategy

### Unit Tests
- All atoms and molecules: 100% coverage
- System classes: 90% coverage
- Critical features: 95% coverage

### Integration Tests
- API client with mock server
- Permission checks with various roles
- Tab management with routing
- Form validation flows

### E2E Tests
- Login → Navigation → CRUD operations
- File upload/download
- Real-time notifications
- Multi-tab synchronization

## Documentation Requirements

Each component/class must have:
- [ ] JSDoc comments
- [ ] TypeScript type definitions
- [ ] Usage examples
- [ ] Props/API documentation
- [ ] Storybook stories (for UI components)

## Performance Targets

- **Initial Load**: < 3 seconds
- **Route Navigation**: < 500ms
- **Form Validation**: < 100ms
- **Data Table Rendering**: < 1 second for 1000 rows
- **File Upload**: Progress feedback every 100ms
- **SSE Event Handling**: < 50ms

## Accessibility Checklist

- [ ] All components keyboard navigable
- [ ] ARIA labels on interactive elements
- [ ] Focus indicators visible
- [ ] Screen reader tested
- [ ] Color contrast WCAG AA compliant
- [ ] Error messages announced
- [ ] Loading states announced

## Browser Support

- Chrome: Latest 2 versions
- Firefox: Latest 2 versions
- Safari: Latest 2 versions
- Edge: Latest 2 versions

## Notes

- PrimeVue components를 직접 사용하지 말고, wrapper component 사용
- RealGrid는 상용 라이선스 필요, DataTable wrapper로 추상화
- SSE는 EventSource API 사용, fallback으로 polling 구현
- Dark mode는 CSS variables 기반으로 구현
- 모든 상태 관리는 Pinia store 사용
- API 요청은 반드시 shared/api/client.ts 사용
