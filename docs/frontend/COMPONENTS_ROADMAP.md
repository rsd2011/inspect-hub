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

| Component | Status | Priority | Location | Description |
|-----------|--------|----------|----------|-------------|
| DataTable | 🔴 | High | `shared/ui/organisms/DataTable/` | RealGrid2 wrapper, 대용량 그리드 |
| Modal | 🔴 | High | `shared/ui/organisms/Modal.vue` | Dialog/Modal 컴포넌트 |
| Toast | 🔴 | High | `shared/ui/organisms/Toast.vue` | Notification toast |
| Alert | 🔴 | High | `shared/ui/organisms/Alert.vue` | Inline alert component |
| SlidePanel | 🔴 | Medium | `shared/ui/organisms/SlidePanel.vue` | Side panel overlay |
| FileViewer | 🔴 | Medium | `shared/ui/organisms/FileViewer.vue` | PDF/Image viewer |
| MarkdownEditor | 🔴 | Medium | `shared/ui/organisms/MarkdownEditor.vue` | 마크다운 에디터 (업무 메모용) |
| HelpViewer | 🔴 | Low | `shared/ui/organisms/HelpViewer.vue` | Context-sensitive help |
| FormBuilder | 🔴 | High | `shared/ui/organisms/FormBuilder.vue` | Dynamic form generator |
| SearchPanel | 🔴 | High | `shared/ui/organisms/SearchPanel.vue` | Advanced search filters |

### widgets (대형 페이지 블록)

| Component | Status | Priority | Location | Description |
|-----------|--------|----------|----------|-------------|
| TabManager | 🔴 | High | `widgets/tab-manager/` | VS Code 스타일 탭 UI, URL 기반 열림 |
| MenuNavigation | 🟢 | High | `widgets/menu-navigation/` | Permission-based hierarchical menu |
| AppHeader | 🟢 | High | `widgets/header/` | Application header |
| AppSidebar | 🟢 | High | `widgets/sidebar/` | Sidebar navigation |
| AppFooter | 🔴 | Low | `widgets/footer/` | Application footer |
| Breadcrumb | 🔴 | Medium | `widgets/breadcrumb/` | Navigation breadcrumb |
| NotificationWidget | 🔴 | Medium | `widgets/notification-widget/` | Real-time notification center |
| ScreenCapture | 🔴 | Low | `widgets/screen-capture/` | 화면 캡처 기능 |
| UrlCopyWidget | 🔴 | Low | `widgets/url-copy/` | URL 복사 기능 (현재 페이지 링크) |

### features (사용자 기능)

| Feature | Status | Priority | Location | Description |
|---------|--------|----------|----------|-------------|
| Attachment | 🔴 | High | `features/attachment/` | 페이지별 첨부파일 업로드/다운로드 |
| Memo | 🔴 | High | `features/memo/` | 페이지별 개인 업무 메모 (마크다운) |
| Theme | 🔴 | Low | `features/theme/` | Dark/Light mode toggle |
| Help | 🔴 | Medium | `features/help/` | Context-aware help system |
| Notification | 🔴 | Medium | `features/notification/` | SSE-based real-time notifications |
| Hotkey | 🔴 | Low | `features/hotkey/` | Global hotkey mapping |

### Page Templates (페이지 템플릿 구조)

| Template | Status | Priority | Location | Description |
|----------|--------|----------|----------|-------------|
| BasePage | 🔴 | High | `shared/ui/templates/BasePage.vue` | 모든 페이지의 기본 레이아웃 |
| ListPage | 🔴 | High | `shared/ui/templates/ListPage.vue` | 목록 페이지 (검색/그리드) |
| FormPage | 🔴 | High | `shared/ui/templates/FormPage.vue` | 작성/수정 페이지 |
| DetailPage | 🔴 | High | `shared/ui/templates/DetailPage.vue` | 조회 페이지 (읽기 전용) |
| DashboardPage | 🔴 | Medium | `shared/ui/templates/DashboardPage.vue` | 대시보드 레이아웃 |
| WizardPage | 🔴 | Low | `shared/ui/templates/WizardPage.vue` | 다단계 입력 페이지 |

**페이지 템플릿 공통 기능:**
- URL 기반 자동 탭 생성 및 열림
- 페이지 상태 유지 (작성 중 데이터 보존)
- 첨부파일 영역 통합
- 개인 메모 영역 통합
- URL 복사 버튼
- 도움말 버튼
- 화면 캡처 버튼
- Breadcrumb 자동 생성

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

### Phase 2: Common UI Components (Sprint 3-4) 🎯 CURRENT PHASE
**Goal**: 재사용 가능한 공통 컴포넌트 구현

**우선순위 1 (Critical):**
- 🔴 Skeleton (로딩 상태 표시)
- 🔴 Badge, Icon (기본 UI 요소)
- 🔴 Modal, Toast, Alert (사용자 피드백)
- 🔴 DataTable (RealGrid wrapper) - **핵심 컴포넌트**

**우선순위 2 (High):**
- 🔴 FormField, FormBuilder
- 🔴 SearchPanel
- 🔴 Avatar
- 🔴 BasePage, ListPage, FormPage, DetailPage (페이지 템플릿)

### Phase 3: Tab & Page State Management (Sprint 5-6)
**Goal**: 탭 기반 UI 및 페이지 상태 관리

**우선순위 1 (Critical):**
- 🔴 TabManager (VS Code 스타일) - **URL 기반 탭 열림**
- 🔴 PageStateManager - **페이지 상태 유지 (작성 중 데이터)**
- 🔴 Breadcrumb (자동 생성)
- 🔴 UrlCopyWidget (현재 URL 복사)

**우선순위 2 (High):**
- 🔴 CodeManager (공통 코드 관리)
- 🔴 Attachment feature (첨부파일 CRUD)
- 🔴 Memo feature (페이지별 개인 메모)
- 🔴 FileUpload, FileViewer

### Phase 4: Real-time & Enhancements (Sprint 7-8)
**Goal**: 실시간 기능 및 사용성 향상

- 🔴 SSEClient
- 🔴 NotificationWidget
- 🔴 Notification feature
- 🔴 I18nManager
- 🔴 Memo feature
- 🔴 AuditLogger

### Phase 5: Polish & User Experience (Sprint 9-10)
**Goal**: 사용자 편의성 및 접근성 향상

**우선순위 1 (High):**
- 🔴 MarkdownEditor (메모/Help 작성)
- 🔴 Help feature (Context-aware)
- 🔴 HelpViewer
- 🔴 ScreenCapture (화면 캡처)

**우선순위 2 (Medium):**
- 🔴 Theme feature (Dark mode)
- 🔴 Hotkey feature (전역 단축키)
- 🔴 Accessibility improvements (WCAG AA)
- 🔴 Performance optimization

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

---

## 🎯 Phase 2 구현 가이드 (현재 단계)

### 1. Skeleton UI Component

**목적**: 로딩 중 콘텐츠 플레이스홀더 표시

**구현 사항:**
```vue
<!-- shared/ui/atoms/Skeleton.vue -->
<template>
  <div
    :class="['skeleton', `skeleton-${variant}`, { 'skeleton-animated': animated }]"
    :style="{ width, height, borderRadius }"
  />
</template>

<script setup lang="ts">
interface Props {
  variant?: 'text' | 'circle' | 'rect'
  width?: string
  height?: string
  borderRadius?: string
  animated?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'rect',
  animated: true
})
</script>
```

**사용 예시:**
```vue
<Skeleton variant="circle" width="40px" height="40px" />
<Skeleton variant="text" width="100%" />
<Skeleton variant="rect" width="100%" height="200px" />
```

### 2. DataTable (RealGrid2 Wrapper) - 핵심 컴포넌트

**위치**: `shared/ui/organisms/DataTable/`

**구조:**
```
DataTable/
├── index.vue (Main component)
├── README.md (Detailed guide)
├── composables/
│   ├── useGridConfig.ts
│   ├── useGridData.ts
│   └── useGridEvents.ts
├── renderers/ (Custom cell renderers)
│   ├── StatusRenderer.ts
│   └── ButtonRenderer.ts
└── types.ts
```

**README.md 참조**: `/home/rsd/workspace/inspect-hub/CLAUDE.md`의 "RealGrid2 Integration" 섹션 참조

**핵심 기능:**
- Column configuration (types, editors, validators)
- Inline editing with commitByCell
- Sorting & Filtering (explicit mode)
- Export (Excel, CSV)
- Copy/Paste with lookup conversion
- Custom renderers (class-based)
- Virtual scrolling for performance

### 3. PageStateManager 클래스

**목적**: 탭별 페이지 상태 저장/복원

**위치**: `shared/lib/page-state-manager/index.ts`

**API 설계:**
```typescript
class PageStateManager {
  // 페이지 상태 저장 (SessionStorage)
  saveState(tabId: string, state: any): void

  // 페이지 상태 복원
  restoreState(tabId: string): any | null

  // Dirty 상태 관리 (미저장 변경사항)
  markDirty(tabId: string, isDirty: boolean): void
  isDirty(tabId: string): boolean

  // 페이지 이탈 가드 (unsaved changes 경고)
  registerBeforeLeave(tabId: string, handler: () => boolean | Promise<boolean>): () => void

  // 상태 초기화
  clearState(tabId: string): void
}
```

**사용 예시:**
```typescript
// FormPage.vue
const tabId = useRoute().fullPath
const pageState = usePageStateManager()

// 상태 저장
watch(() => formData.value, (newData) => {
  pageState.saveState(tabId, newData)
  pageState.markDirty(tabId, true)
}, { deep: true })

// 페이지 복원
onMounted(() => {
  const savedData = pageState.restoreState(tabId)
  if (savedData) {
    formData.value = savedData
  }
})

// 이탈 가드
pageState.registerBeforeLeave(tabId, () => {
  if (pageState.isDirty(tabId)) {
    return confirm('저장하지 않은 변경사항이 있습니다. 페이지를 나가시겠습니까?')
  }
  return true
})
```

### 4. TabManager Widget

**목적**: VS Code 스타일 탭 UI, URL 기반 탭 생성

**위치**: `widgets/tab-manager/`

**구조:**
```
tab-manager/
├── ui/
│   ├── TabManager.vue (Main component)
│   ├── Tab.vue (Single tab)
│   └── TabContextMenu.vue
├── model/
│   ├── tab-manager.store.ts (Pinia store)
│   └── types.ts
└── README.md
```

**핵심 기능:**
- URL → Tab 매핑 (자동 탭 생성)
- Tab persistence (SessionStorage)
- Drag & Drop 재정렬
- Context menu (Close, Close Others, Close All)
- Dirty state indicator (●)
- Maximum tab limit
- Tab activation/deactivation

**Store 설계:**
```typescript
// tab-manager.store.ts
export const useTabStore = defineStore('tabs', () => {
  const tabs = ref<Tab[]>([])
  const activeTabId = ref<string | null>(null)

  // URL로부터 탭 생성
  function openTab(url: string, meta?: TabMeta): Tab

  // 탭 닫기
  function closeTab(tabId: string): void

  // 탭 활성화
  function activateTab(tabId: string): void

  // Dirty 상태 설정
  function setDirty(tabId: string, isDirty: boolean): void

  // 탭 persistence
  function saveTabs(): void
  function restoreTabs(): void

  return {
    tabs,
    activeTabId,
    openTab,
    closeTab,
    activateTab,
    setDirty,
    saveTabs,
    restoreTabs
  }
})
```

### 5. BasePage Template

**목적**: 모든 페이지의 공통 레이아웃 및 기능

**위치**: `shared/ui/templates/BasePage.vue`

**구조:**
```vue
<template>
  <div class="base-page">
    <!-- Breadcrumb -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Page Header -->
    <div class="page-header">
      <slot name="header">
        <h1>{{ title }}</h1>
      </slot>

      <!-- Action Buttons -->
      <div class="page-actions">
        <button @click="copyUrl">
          <Icon name="link" /> URL 복사
        </button>
        <button @click="captureScreen">
          <Icon name="camera" /> 화면 캡처
        </button>
        <button @click="toggleHelp">
          <Icon name="help" /> 도움말
        </button>
        <button @click="toggleMemo">
          <Icon name="note" /> 메모
        </button>
        <slot name="actions" />
      </div>
    </div>

    <!-- Main Content -->
    <div class="page-content">
      <slot />
    </div>

    <!-- Attachment Area (optional) -->
    <AttachmentPanel
      v-if="showAttachments"
      :page-id="pageId"
    />

    <!-- Memo Panel (slide-in) -->
    <SlidePanel v-model="showMemoPanel" position="right">
      <MemoEditor :page-id="pageId" />
    </SlidePanel>

    <!-- Help Panel -->
    <Modal v-model="showHelpModal">
      <HelpViewer :context="helpContext" />
    </Modal>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  pageId?: string
  showAttachments?: boolean
  helpContext?: string
}

const props = withDefaults(defineProps<Props>(), {
  showAttachments: false
})

// URL Copy
const copyUrl = () => {
  navigator.clipboard.writeText(window.location.href)
  toast.success('URL이 복사되었습니다')
}

// Screen Capture
const captureScreen = () => {
  // Implementation using html2canvas or similar
}

// Breadcrumb auto-generation
const breadcrumbItems = computed(() => {
  // Generate from route meta
})
</script>
```

### 6. Memo Feature (페이지별 개인 메모)

**위치**: `features/memo/`

**구조:**
```
memo/
├── ui/
│   ├── MemoEditor.vue (마크다운 에디터)
│   └── MemoList.vue
├── model/
│   ├── memo.store.ts
│   └── types.ts
└── api/
    └── memo.api.ts
```

**API:**
```typescript
// memo.api.ts
export const memoApi = {
  // 페이지별 메모 조회
  getMemo(pageId: string): Promise<Memo | null>

  // 메모 저장 (auto-save 지원)
  saveMemo(pageId: string, content: string): Promise<void>

  // 메모 삭제
  deleteMemo(pageId: string): Promise<void>

  // 메모 검색
  searchMemos(query: string): Promise<Memo[]>
}
```

---

## 🔧 개발 가이드라인

### 컴포넌트 작성 규칙

1. **TypeScript 필수**: 모든 props, emits, composables에 타입 정의
2. **Composition API 사용**: `<script setup>` 권장
3. **단일 책임 원칙**: 컴포넌트는 하나의 역할만 수행
4. **Props validation**: required, default, validator 활용
5. **Emit events**: 명확한 이벤트 네이밍 (update:modelValue, change, submit 등)

### Atomic Design 적용

```
Atoms (Button, Input)
  - 재사용 가능한 최소 단위
  - Props로만 동작, 상태 없음

Molecules (FormField = Label + Input + Error)
  - 2-3개 Atoms 조합
  - 최소한의 로직

Organisms (FormBuilder, DataTable)
  - 복잡한 기능
  - 비즈니스 로직 포함 가능
  - Composables 활용

Templates (BasePage, ListPage)
  - 페이지 레이아웃 정의
  - Slot으로 유연성 제공

Pages (pages/ 디렉토리)
  - Template 사용
  - Feature 통합
  - 라우팅 연결
```

### Performance Best Practices

1. **Virtual Scrolling**: 대용량 리스트는 가상 스크롤 적용
2. **Lazy Loading**: 이미지, 컴포넌트 lazy load
3. **Debounce/Throttle**: 검색, 스크롤 이벤트 최적화
4. **Memo/Computed**: 계산 비용 높은 로직 캐싱
5. **Code Splitting**: Route-level code splitting

---

## 📅 스프린트별 작업 계획

### Sprint 3 (2주)
**목표**: Core UI Components

- [ ] Skeleton, Badge, Icon
- [ ] Modal, Toast, Alert
- [ ] BasePage template
- [ ] Testing & Documentation

### Sprint 4 (2주)
**목표**: DataTable & Form Components

- [ ] DataTable (RealGrid wrapper)
- [ ] FormField, FormBuilder
- [ ] SearchPanel
- [ ] ListPage, FormPage templates

### Sprint 5 (2주)
**Goal**: Tab Management

- [ ] TabManager widget
- [ ] PageStateManager
- [ ] UrlCopyWidget
- [ ] Breadcrumb

### Sprint 6 (2주)
**Goal**: Features

- [ ] CodeManager
- [ ] Attachment feature
- [ ] Memo feature
- [ ] DetailPage template
