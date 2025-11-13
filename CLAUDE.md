# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Inspect-Hub** is a comprehensive AML (Anti-Money Laundering) integrated compliance monitoring system for financial institutions in Korea. The system detects suspicious transactions (STR), currency transaction reports (CTR), watch-list filtering (WLF), and manages compliance workflows with regulatory compliance for Korean financial law and FATF guidelines.

**Current Status:** Planning/POC Phase - comprehensive PRD split into 15 documents in `/docs/prd/`, minimal code implementation.

## 📚 Documentation Structure

**All project documentation is centralized in the `/docs` directory:**

- **[Documentation Center](./docs/index.md)** - Start here for complete navigation
- **[PRD (Product Requirements)](./docs/prd/index.md)** - 15 documents covering all requirements
- **[Frontend Guide](./docs/frontend/README.md)** - Nuxt 3, components (675-line roadmap)
- **[Backend Guide](./docs/backend/AGENTS.md)** - Spring Boot, API development
- **[API Contract](./docs/api/CONTRACT.md)** - Frontend ↔ Backend communication
- **[Architecture](./docs/architecture/DDD_DESIGN.md)** - System design, DDD patterns
- **[Development Guide](./docs/development/AGENTS.md)** - Coding rules, testing, builds

## Tech Stack

**Backend:**
- Java 21 + Spring Boot 3.3.2
- Spring Security for authentication/authorization
- MyBatis for database access
- Gradle multi-module build system
- PostgreSQL (primary DB) + Redis (caching) + Kafka (messaging)
- Spring Batch for batch processing

**Frontend:**
- Nuxt 3 (Vue 3 Composition API) - **⚠️ SPA MODE ONLY - SSR STRICTLY FORBIDDEN** (`ssr: false`)
- Vite build system
- UI: PrimeVue + RealGrid (commercial grid) + Tailwind CSS (with prefix `tw-`)
- State: Pinia
- Validation: VeeValidate + Zod
- Charts: Apache ECharts
- i18n: @nuxtjs/i18n
- **Deployment:** Static resources only (Nginx/Apache hosting)

**Project Structure:**
```
inspect-hub/
├── docs/                           # 📚 Central documentation
│   ├── index.md                    # Documentation center (start here)
│   ├── prd/                        # Product requirements (15 files)
│   ├── frontend/                   # Frontend guides
│   ├── backend/                    # Backend guides
│   ├── api/                        # API contract
│   ├── architecture/               # System design
│   └── development/                # Development rules
├── backend/                        # Spring Boot multi-module
│   ├── common/                     # Shared entities, DTOs, utilities
│   ├── policy/, detection/, ...    # Domain modules
│   └── server/                     # Main application
└── frontend/                       # Nuxt 3 application (SPA only)
    ├── app/                        # App layer
    ├── pages/                      # Pages (routing)
    ├── widgets/                    # Large blocks
    ├── features/                   # User features
    ├── entities/                   # Business entities
    └── shared/                     # Shared (UI, API, utils)
```

## Frontend Architecture

**Architecture Pattern:** FSD (Feature-Sliced Design) + Atomic Design

### FSD Layer Structure

```
frontend/
├── app/                    # App layer - Application initialization & setup
│   ├── config/            # App-level configuration
│   ├── providers/         # Global providers (plugins)
│   │   └── primevue.ts
│   ├── styles/            # Global styles
│   │   ├── main.css
│   │   ├── tailwind.css
│   │   └── _variables.scss
│   └── layouts/           # Application layouts
│       └── default.vue
├── i18n/                   # Internationalization (Nuxt i18n module requirement)
│   └── locales/           # Locale files
│       ├── ko.json        # Korean translations
│       └── en.json        # English translations
├── pages/                  # Pages layer - Route pages (Nuxt auto-routing)
│   ├── index/
│   │   └── ui/
│   │       └── IndexPage.vue
│   └── login/
│       └── ui/
│           └── LoginPage.vue
├── widgets/                # Widgets layer - Large page blocks
│   ├── header/
│   │   ├── ui/
│   │   │   └── AppHeader.vue
│   │   └── model/
│   ├── sidebar/
│   │   ├── ui/
│   │   └── model/
│   └── footer/
│       └── ui/
├── features/               # Features layer - User scenarios & features
│   ├── auth/
│   │   ├── ui/            # Login form, logout button, etc.
│   │   ├── model/         # Pinia store, middleware, composables
│   │   │   ├── auth.store.ts
│   │   │   └── auth.middleware.ts
│   │   └── api/           # API calls
│   │       └── auth.api.ts
│   └── detection/
│       ├── ui/
│       ├── model/
│       └── api/
├── entities/               # Entities layer - Business entities
│   ├── user/
│   │   ├── ui/            # User card, avatar, etc.
│   │   ├── model/         # User store, types
│   │   └── api/           # User API
│   └── transaction/
│       ├── ui/
│       ├── model/
│       └── api/
└── shared/                 # Shared layer - Reusable code
    ├── ui/                 # Atomic Design components
    │   ├── atoms/          # Basic UI elements
    │   │   ├── Button.vue
    │   │   ├── Input.vue
    │   │   ├── Label.vue
    │   │   └── Badge.vue
    │   ├── molecules/      # Simple composite components
    │   │   ├── FormField.vue
    │   │   ├── SearchBox.vue
    │   │   └── CardHeader.vue
    │   └── organisms/      # Complex composite components
    │       ├── DataTable.vue
    │       ├── LoginForm.vue
    │       └── Navbar.vue
    ├── api/                # API base configuration
    │   ├── client.ts       # HTTP client setup (axios/ofetch)
    │   └── interceptors.ts # Request/response interceptors
    ├── lib/                # Utilities & composables
    │   ├── utils/
    │   └── composables/
    ├── config/             # Shared configuration
    │   └── constants.ts
    └── types/              # Common TypeScript types
        └── index.ts
```

**Note on i18n directory:** The `i18n/locales/` directory structure is required by @nuxtjs/i18n v10 module, which defaults to looking for locale files in this specific path. This is an acceptable exception to the strict FSD pattern for framework compatibility.

## Frontend Critical Constraints

### ⚠️ SSR/Node.js API STRICTLY FORBIDDEN

**This frontend MUST be deployed as static resources only. SSR is absolutely prohibited.**

#### What is FORBIDDEN:

```typescript
// ❌ NEVER do this - SSR configuration
export default defineNuxtConfig({
  ssr: true,  // FORBIDDEN - must always be false
})

// ❌ NEVER create server directory
/server/
  /api/
    hello.ts  // FORBIDDEN - No Nuxt Server API routes

// ❌ NEVER use server-side only features
const { data } = await useAsyncData('users', () => $fetch('/api/users'))  // Server API forbidden
const { data } = await useFetch('/api/users')  // Server API forbidden

// ❌ NEVER use Nitro server features
export default defineNitroPlugin((nitroApp) => {  // FORBIDDEN
  // Server-side logic forbidden
})

// ❌ NEVER use server middleware
export default defineEventHandler((event) => {  // FORBIDDEN
  // Server routes forbidden
})
```

#### What is ALLOWED:

```typescript
// ✅ SPA mode configuration
export default defineNuxtConfig({
  ssr: false,  // REQUIRED - must always be false
})

// ✅ Client-side API calls to backend
const api = useApiClient()
const users = await api.get('/api/v1/users')  // Calls Spring Boot backend

// ✅ Client-side data fetching
const data = ref(null)
onMounted(async () => {
  data.value = await api.get('/data')
})

// ✅ Browser APIs
localStorage.setItem('key', 'value')
sessionStorage.getItem('token')
window.addEventListener('resize', handler)
```

#### Deployment Architecture:

```
┌─────────────────────┐         ┌──────────────────────┐
│   Nginx/Apache      │         │   Spring Boot        │
│   (Static hosting)  │────────▶│   (Backend API)      │
│                     │  CORS   │                      │
│   - index.html      │         │   /api/v1/*          │
│   - _nuxt/*.js      │         │                      │
│   - _nuxt/*.css     │         │   PostgreSQL         │
│                     │         │   Redis              │
└─────────────────────┘         │   Kafka              │
                                 └──────────────────────┘
```

#### Build & Deployment:

```bash
# Build for production (generates static files)
npm run build

# Output: .output/public/ contains:
#   - index.html
#   - _nuxt/*.js (bundled JavaScript)
#   - _nuxt/*.css (bundled CSS)
#   - assets/* (images, fonts, etc.)

# Deploy .output/public/ to:
#   - Nginx/Apache web server
#   - AWS S3 + CloudFront
#   - Netlify/Vercel (static hosting)
#   - Any CDN or static file server
```

#### API Communication:

- **Backend Base URL:** `http://localhost:8090/api/v1` (development)
- **Production URL:** `https://api.inspecthub.example.com/api/v1`
- **All API calls** must go through `shared/api/client.ts`
- **CORS handling** is done on Spring Boot backend
- **Authentication** uses JWT tokens (stored in Pinia store)
- **No server-side rendering** - all data fetching happens in browser

#### Checklist for Every Feature:

- [ ] Does this feature use `ssr: false`?
- [ ] Does this avoid creating `/server` directory?
- [ ] Does this use `shared/api/client.ts` for API calls?
- [ ] Does this work without Node.js server running?
- [ ] Can this be deployed as static HTML/CSS/JS?
- [ ] Does this avoid `useAsyncData` with server-side logic?
- [ ] Does this avoid Nitro/server middleware?

**See `frontend/README.md` for detailed constraints and examples.**

### ⚠️ Web Browser Focus - Desktop/Laptop Priority

**This project targets desktop/laptop web browsers, NOT mobile devices.**

#### UI Design Priorities:

- 🖥️ **Primary Target:** Desktop/Laptop web browsers (Chrome, Firefox, Safari)
- ❌ **NOT Mobile-First:** Mobile-specific UI/UX is not required
- ✅ **Responsive Design:** Must support various desktop resolutions

#### Target Screen Resolutions:

```
Primary Support:
- 1920x1080 (Full HD) - Most common
- 1680x1050 (WSXGA+)
- 1600x900 (HD+)
- 1366x768 (HD) - Minimum supported resolution

Optional:
- 2560x1440 (QHD)
- 3840x2160 (4K)
```

#### Design Guidelines:

```vue
<!-- ✅ Use desktop-focused layouts -->
<div class="tw-grid tw-grid-cols-4 tw-gap-6">
  <!-- Multi-column layouts for wide screens -->
</div>

<!-- ✅ Use minimum width for consistent experience -->
<div class="tw-min-w-[1280px] tw-mx-auto">
  <!-- Prevent layouts from breaking on small screens -->
</div>

<!-- ❌ Avoid mobile-specific patterns -->
<!-- - Touch gestures (swipe, pinch-zoom) -->
<!-- - Bottom navigation bars -->
<!-- - Hamburger menus (use sidebar instead) -->
<!-- - Mobile-first breakpoints -->
```

#### What to Avoid:

- ❌ Mobile touch gestures and interactions
- ❌ Mobile-first CSS breakpoints (sm, md)
- ❌ Bottom tab navigation
- ❌ Hamburger menus for primary navigation
- ❌ Single-column mobile layouts

#### What to Emphasize:

- ✅ Mouse + keyboard interactions
- ✅ Multi-column layouts (sidebars, grids)
- ✅ Data tables and grids (RealGrid)
- ✅ Desktop-class navigation (persistent sidebar)
- ✅ Rich data visualizations (ECharts)
- ✅ Keyboard shortcuts and accessibility

#### Responsive Breakpoints:

Use `lg`, `xl`, `2xl` Tailwind breakpoints for desktop resolution variations:

```javascript
// tailwind.config.js
screens: {
  'lg': '1024px',   // 1366px+ screens
  'xl': '1280px',   // 1680px+ screens
  '2xl': '1536px',  // 1920px+ screens
}
```

**See `frontend/README.md` section "웹 브라우저 중심 UI 설계" for detailed guidelines.**

### Architecture Guidelines

#### 1. FSD Layer Rules

**Dependency Direction:** Lower layers CANNOT import from upper layers
```
app → pages → widgets → features → entities → shared
```

**Import Rules:**
- ✅ `features/auth` can import from `entities/user` and `shared/ui`
- ✅ `pages/login` can import from `features/auth` and `widgets/header`
- ❌ `entities/user` CANNOT import from `features/auth`
- ❌ `shared/ui` CANNOT import from `entities` or `features`

**Lateral Imports:**
- Features CANNOT import from other features
- Entities CANNOT import from other entities
- Use shared layer for cross-cutting concerns

#### 2. Atomic Design Guidelines

**Atoms:** Pure presentational components
- No business logic
- Only receive props and emit events
- Examples: Button, Input, Label, Icon

**Molecules:** Simple combinations of atoms
- Minimal business logic
- Composable units
- Examples: FormField (Label + Input + Error), SearchBox (Input + Button)

**Organisms:** Complex functional components
- Can contain business logic
- Standalone sections
- Examples: LoginForm, DataTable, Navbar

**Note:** Templates and Pages are handled by FSD's `pages` layer

#### 3. Nuxt Auto-Import Configuration

**Components Auto-Import:**
```typescript
// nuxt.config.ts
components: [
  { path: '~/app/layouts', pathPrefix: false },
  { path: '~/widgets', pathPrefix: false },
  { path: '~/shared/ui/atoms', pathPrefix: false },
  { path: '~/shared/ui/molecules', pathPrefix: false },
  { path: '~/shared/ui/organisms', pathPrefix: false }
]
```

**Imports Auto-Import:**
```typescript
// nuxt.config.ts
imports: {
  dirs: [
    'shared/lib/composables',
    'shared/lib/utils',
    'features/*/model',
    'entities/*/model'
  ]
}
```

#### 4. File Naming Conventions

**Components:**
- PascalCase: `LoginPage.vue`, `AppHeader.vue`, `Button.vue`
- Page components suffix: `*Page.vue`
- Widget components prefix: `App*` or descriptive name

**Stores:**
- kebab-case with suffix: `auth.store.ts`, `user.store.ts`
- Use `defineStore('storeName', () => {})` composition API style

**API files:**
- kebab-case with suffix: `auth.api.ts`, `transaction.api.ts`

**Composables:**
- camelCase with prefix: `useAuth.ts`, `usePermissions.ts`

**Middleware:**
- kebab-case with suffix: `auth.middleware.ts`

#### 5. Best Practices

**State Management:**
- Feature-specific state → `features/*/model/store.ts`
- Entity-specific state → `entities/*/model/store.ts`
- Global app state → `app/config/` or shared store

**API Calls:**
- Feature-specific APIs → `features/*/api/`
- Entity-specific APIs → `entities/*/api/`
- Shared API client → `shared/api/client.ts`

**Composables:**
- Feature-specific → `features/*/model/composables/`
- Shared utilities → `shared/lib/composables/`

**Types:**
- Feature types → `features/*/model/types.ts`
- Entity types → `entities/*/model/types.ts`
- Shared types → `shared/types/`

**Styling:**
- Use Tailwind CSS with `tw-` prefix
- Global styles → `app/styles/`
- Component-scoped styles → `<style scoped>`
- PrimeVue theme → `app/providers/primevue.ts`

#### 6. Migration Strategy

When refactoring existing code:
1. Start with `shared/` layer (most reusable)
2. Move to `entities/` (business entities)
3. Then `features/` (user features)
4. Finally `widgets/` and `pages/`

**Example Migration:**
```
Old: components/LoginForm.vue
New: features/auth/ui/LoginForm.vue

Old: stores/auth.ts
New: features/auth/model/auth.store.ts

Old: api/auth.ts
New: features/auth/api/auth.api.ts

Old: components/Button.vue
New: shared/ui/atoms/Button.vue
```

## Package Management (Frontend)

### Configuration Files

**package.json:**
- Contains all project dependencies and scripts
- Use semantic versioning (^major.minor.patch)
- Separate dependencies from devDependencies

**.npmrc:**
- NPM configuration for consistent behavior across team
- Configures audit, peer dependencies, and caching

**.nvmrc:**
- Specifies Node.js version (20.18.0)
- Ensures consistent Node.js version across environments

**renovate.json:**
- Automated dependency updates via Renovate Bot
- Groups related packages for easier review
- Auto-merges minor/patch updates

### Dependencies Classification

**dependencies** (Production runtime):
- Framework: `nuxt`, `@nuxtjs/*`
- UI: `primevue`, `@primevue/themes`
- State: `pinia`, `@pinia/nuxt`
- Validation: `vee-validate`, `zod`
- Charts: `echarts`, `vue-echarts`
- i18n: `@nuxtjs/i18n`

**devDependencies** (Development only):
- Build tools: `typescript`, `vue-tsc`
- Linting: `eslint`, `@nuxt/eslint`
- Styling: `tailwindcss`, `postcss`, `autoprefixer`
- Types: `@types/node`
- Tools: `@nuxt/devtools`

### NPM Scripts Reference

**Development:**
```bash
npm run dev              # Start dev server (0.0.0.0:3000)
npm run dev:open         # Start dev + auto-open browser
```

**Build & Preview:**
```bash
npm run build            # Production build
npm run build:analyze    # Build with bundle analyzer
npm run generate         # Generate static site
npm run preview          # Preview production build
npm run start            # Start production server
```

**Code Quality:**
```bash
npm run lint             # Check code quality
npm run lint:fix         # Fix linting issues
npm run typecheck        # TypeScript type checking
npm test                 # Run tests (not configured yet)
```

**Maintenance:**
```bash
npm run clean            # Clean build artifacts
npm run clean:modules    # Clean & reinstall node_modules
npm run audit            # Security audit (production only)
npm run audit:fix        # Fix security issues
npm run outdated         # Check outdated packages
npm run update:check     # Check available updates (npm-check-updates)
npm run update:interactive # Interactive update selection
```

### Version Management Best Practices

**Node.js Version:**
- Use exact version in `.nvmrc` (20.18.0)
- Minimum version in `package.json` engines (>=20.0.0)
- Use `nvm use` to switch to project version

**Package Updates:**
1. **Regular Updates (Weekly)**:
   ```bash
   npm run outdated           # Check outdated packages
   npm run update:interactive # Select packages to update
   npm run build              # Test build
   npm run lint               # Check for issues
   ```

2. **Security Updates (Immediate)**:
   ```bash
   npm run audit              # Check vulnerabilities
   npm run audit:fix          # Auto-fix if possible
   ```

3. **Major Version Updates**:
   - Review breaking changes in changelog
   - Update one major package at a time
   - Test thoroughly before merging

### Dependency Installation Rules

**Adding New Dependencies:**
```bash
# Production dependency
npm install package-name

# Development dependency
npm install -D package-name

# Exact version (for critical packages)
npm install --save-exact package-name@1.2.3
```

**Before Installing:**
1. Check package popularity and maintenance status
2. Review package size (use bundlephobia.com)
3. Check for TypeScript support
4. Verify license compatibility
5. Look for security vulnerabilities

### Performance Optimization

**Chunk Splitting (nuxt.config.ts):**
```typescript
vite: {
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'primevue': ['primevue'],
          'charts': ['echarts', 'vue-echarts'],
          'vendor': ['pinia', 'zod', 'vee-validate']
        }
      }
    }
  }
}
```

**Bundle Analysis:**
```bash
npm run build:analyze
# Opens visual bundle analyzer
```

**Optimization Checklist:**
- [ ] Code splitting configured for large libraries
- [ ] Tree-shaking enabled (default in Vite)
- [ ] CSS purging enabled (Tailwind)
- [ ] Image optimization configured
- [ ] Lazy loading for routes and components
- [ ] Asset compression enabled (Nitro)

### Security Best Practices

**Regular Audits:**
- Run `npm audit` weekly
- Fix critical/high vulnerabilities immediately
- Review moderate vulnerabilities

**Lock File:**
- Always commit `package-lock.json`
- Never manually edit lock file
- Run `npm ci` in CI/CD (not `npm install`)

**Private Packages:**
- Use `.npmrc` for authentication
- Never commit credentials
- Use environment variables for tokens

### Troubleshooting

**Common Issues:**

1. **Dependency conflicts:**
   ```bash
   npm run clean:modules
   ```

2. **Build cache issues:**
   ```bash
   npm run clean
   npm run build
   ```

3. **Type errors after update:**
   ```bash
   npm run typecheck
   # Fix errors in code
   ```

4. **Peer dependency warnings:**
   - Check if packages are compatible
   - Update related packages together
   - Use `--legacy-peer-deps` only as last resort

## Common Components & System Requirements

### UI Components Roadmap

Components are organized by FSD layers and Atomic Design principles.

#### shared/ui/atoms (Basic UI Elements)
- **Button**: Primary, secondary, icon buttons with loading states
- **Input**: Text, number, date inputs with validation states
- **Badge**: Status badges, notification counts
- **Icon**: Icon wrapper component
- **Skeleton**: Loading skeleton UI elements
- **Avatar**: User avatar component
- **Label**: Form labels with required indicator
- **Checkbox/Radio**: Form controls
- **Switch**: Toggle switches

#### shared/ui/molecules (Simple Composites)
- **FormField**: Label + Input + Error message combo
- **SearchBox**: Search input with icon and clear button
- **FileUpload**: File upload with drag-drop support
- **ThemeToggle**: Dark/light mode switch
- **DateRangePicker**: Start/end date selector
- **StatusBadge**: Status with icon and text
- **ActionButtons**: Common action button groups (Save, Cancel, Delete)

#### shared/ui/organisms (Complex Components)
- **DataTable**: RealGrid2 wrapper component with:
  - **Column Types**: Text, Number, Date, Dropdown, Checkbox, Button, Custom
  - **Editing**: Inline editing with validators, commitByCell for immediate save
  - **Sorting & Filtering**: Explicit mode to prevent auto-resorting
  - **Selection**: Single/multiple row selection with checkbar
  - **Export**: Excel, CSV export with customizable options
  - **Copy/Paste**: Smart copy/paste with lookup value conversion
  - **Fixed Columns/Rows**: Freeze columns and rows for scrolling
  - **Merged Cells**: Cell merging with batch edit support
  - **Soft Delete**: Logical deletion with show/hide option
  - **Custom Renderers**: Extensible rendering with class-based approach
  - **Virtual Scrolling**: High performance for large datasets
  - **Footer Aggregation**: Sum, avg, min, max calculations
  - **Validation**: Built-in and custom validators
  - **Styling**: Static and dynamic cell styling
  - **Context Menu**: Right-click menu support
  - See [DataTable README](frontend/shared/ui/organisms/DataTable/README.md) for details
- **Modal**: Dialog component with:
  - Customizable header/footer
  - Size variants (sm, md, lg, xl, full)
  - Draggable option
  - Nested modal support
- **Toast**: Notification toast with:
  - Success, error, warning, info types
  - Auto-dismiss with configurable duration
  - Action buttons support
  - Queue management
- **Alert**: Inline alert component
- **SlidePanel**: Side panel overlay (left/right)
- **FileViewer**: Multi-format file viewer
  - Image preview (jpg, png, gif, webp)
  - PDF viewer
  - Document preview
- **MarkdownEditor**: WYSIWYG markdown editor
  - Live preview
  - Toolbar for formatting
  - Image upload support
- **HelpViewer**: Context-sensitive help panel
  - Markdown-based help content
  - Search functionality
  - Related topics
- **FormBuilder**: Dynamic form generator
  - Schema-based rendering
  - Field validation with VeeValidate + Zod
  - Conditional fields
  - Custom field templates
- **SearchPanel**: Collapsible search filter panel
  - Save/load search presets
  - Clear all filters
  - Advanced filter builder

#### widgets (Large Page Blocks)
- **TabManager**: VS Code-style tab system
  - URL-based tab creation
  - Tab persistence (session storage)
  - Tab reordering (drag-drop)
  - Dirty state indicator
  - Tab context menu (close, close others, close all)
  - Maximum tab limit
- **MenuNavigation**: Hierarchical menu with:
  - Permission-based visibility
  - Active state tracking
  - Favorites/bookmarks
  - Search functionality
  - Collapsible/expandable
- **AppHeader**: Application header with:
  - Logo and branding
  - User profile dropdown
  - Notifications bell
  - Quick actions
  - Theme toggle
- **AppSidebar**: Sidebar navigation
  - Collapsible menu
  - Breadcrumb integration
  - Resize handle
- **AppFooter**: Application footer
  - Version info
  - Copyright
  - Quick links
- **Breadcrumb**: Navigation breadcrumb trail
  - Auto-generated from route
  - Clickable navigation
  - Home icon
- **NotificationWidget**: Real-time notification center
  - SSE-based updates
  - Unread count badge
  - Mark as read/unread
  - Notification history
  - Action links
- **ScreenCapture**: Screen capture utility
  - Capture viewport
  - Capture element
  - Download as image
  - Copy to clipboard

#### features (User Features)
- **features/attachment/**
  - File upload/download
  - File list management
  - Drag-drop support
  - Progress tracking
  - File type validation
  - Size limit enforcement
- **features/memo/**
  - Page-specific memos
  - Personal business notes
  - Auto-save draft
  - Markdown support
  - Search memos
- **features/theme/**
  - Dark/light mode toggle
  - Theme persistence
  - System preference detection
  - Custom theme support
- **features/help/**
  - Help page routing
  - Context-sensitive help
  - FAQ search
  - Help articles management
- **features/notification/**
  - Real-time notifications (SSE)
  - Notification preferences
  - Notification history
  - Mark as read/unread
- **features/hotkey/**
  - Global hotkey mapping
  - Hotkey customization
  - Hotkey help overlay
  - Conflict detection

### System Management Classes

Located in `shared/lib/` for cross-cutting concerns.

#### shared/lib/code-manager
**Purpose**: Common code value management and caching

**Features:**
- Fetch code groups from backend
- In-memory cache with TTL
- Code value lookup by key
- Multi-language code labels
- Code hierarchy support (parent-child)

**API:**
```typescript
class CodeManager {
  getCodeGroup(groupCode: string): Promise<Code[]>
  getCodeValue(groupCode: string, codeValue: string): Code | undefined
  getCodeLabel(groupCode: string, codeValue: string, locale?: string): string
  invalidateCache(groupCode?: string): void
}
```

#### shared/lib/api-client
**Purpose**: Standardized API request/response handling

**Features:**
- Request/response interceptors
- Automatic token injection
- Token refresh on 401
- Unified error handling
- Loading state management
- Request cancellation
- Response caching

**API:**
```typescript
class ApiClient {
  get<T>(url: string, options?: RequestOptions): Promise<ApiResponse<T>>
  post<T>(url: string, data: any, options?: RequestOptions): Promise<ApiResponse<T>>
  put<T>(url: string, data: any, options?: RequestOptions): Promise<ApiResponse<T>>
  delete<T>(url: string, options?: RequestOptions): Promise<ApiResponse<T>>
  upload(url: string, files: File[], options?: RequestOptions): Promise<ApiResponse<UploadResult>>
}
```

#### shared/lib/sse-client
**Purpose**: Server-Sent Events (SSE) management for real-time updates

**Features:**
- Auto-reconnection on disconnect
- Event subscription/unsubscription
- Event type routing
- Heartbeat monitoring
- Error recovery

**API:**
```typescript
class SSEClient {
  connect(url: string): void
  disconnect(): void
  subscribe(eventType: string, handler: (data: any) => void): () => void
  isConnected(): boolean
}
```

#### shared/lib/session-manager
**Purpose**: User session and authentication state management

**Features:**
- User info storage (Pinia store)
- Token refresh automation
- Session expiry warning (5min, 1min before)
- Multi-tab synchronization (BroadcastChannel)
- Auto-logout on expiry
- "Remember me" support

**API:**
```typescript
class SessionManager {
  getCurrentUser(): User | null
  refreshToken(): Promise<void>
  logout(): Promise<void>
  syncAcrossTabs(): void
  onSessionExpiring(callback: (remainingSeconds: number) => void): () => void
}
```

#### shared/lib/permission-manager
**Purpose**: Fine-grained access control

**Features:**
- Menu visibility control
- Button/action permission checking
- Permission directive (v-permission)
- Composable (usePermission)
- Role-based + permission-based

**API:**
```typescript
class PermissionManager {
  hasPermission(permission: string): boolean
  hasRole(role: string): boolean
  hasAnyPermission(permissions: string[]): boolean
  hasAllPermissions(permissions: string[]): boolean
  canAccessMenu(menuCode: string): boolean
  canPerformAction(featureCode: string, actionCode: string): boolean
}
```

#### shared/lib/i18n-manager
**Purpose**: Internationalization and label management

**Features:**
- Dynamic label loading
- Locale switching
- Fallback to default locale
- Label caching
- Pluralization support
- Date/number formatting

**API:**
```typescript
class I18nManager {
  getLabel(key: string, params?: Record<string, any>): string
  setLocale(locale: string): void
  getAvailableLocales(): Locale[]
  formatDate(date: Date, format?: string): string
  formatNumber(value: number, options?: NumberFormatOptions): string
}
```

#### shared/lib/page-state-manager
**Purpose**: Maintain page state across navigation and tabs

**Features:**
- Tab-specific data persistence
- Dirty state detection (unsaved changes)
- Navigation guard (confirm before leave)
- State restoration on tab switch
- Auto-save draft support

**API:**
```typescript
class PageStateManager {
  saveState(tabId: string, state: any): void
  restoreState(tabId: string): any | null
  markDirty(tabId: string, isDirty: boolean): void
  isDirty(tabId: string): boolean
  clearState(tabId: string): void
  registerBeforeLeave(tabId: string, handler: () => boolean | Promise<boolean>): () => void
}
```

#### shared/lib/audit-logger
**Purpose**: Client-side action logging for audit trail

**Features:**
- User action tracking
- Page view logging
- Data access logging
- Batch log submission
- Offline log queue

**API:**
```typescript
class AuditLogger {
  logAction(action: string, target: string, details?: any): void
  logPageView(page: string, params?: any): void
  logDataAccess(resource: string, action: 'read' | 'create' | 'update' | 'delete', recordId?: string): void
  flush(): Promise<void>
}
```

#### shared/lib/loading-manager
**Purpose**: Centralized loading state management

**Features:**
- Global loading overlay
- Partial/local loading states
- Multiple concurrent loaders
- Minimum display time (prevent flicker)
- Loading message customization

**API:**
```typescript
class LoadingManager {
  showGlobal(message?: string): () => void
  showLocal(key: string, message?: string): () => void
  hideGlobal(): void
  hideLocal(key: string): void
  isLoading(key?: string): boolean
}
```

### Feature Requirements Implementation

#### URL-based Tab Management
**Location**: `widgets/tab-manager/`

**Implementation:**
- Router integration for tab creation
- Tab state in Pinia store
- URL change triggers new tab
- Tab close cleanup

#### Data Persistence
**Location**: `shared/lib/page-state-manager`

**Implementation:**
- SessionStorage for tab data
- Dirty flag tracking
- Confirmation dialog on close

#### Personal Memos
**Location**: `features/memo/`

**Implementation:**
- Page-scoped memo component
- LocalStorage or backend API
- Markdown editor integration

#### Permission-based Menu
**Location**: `widgets/menu-navigation/`

**Implementation:**
- Filter menu items by permission
- Dynamic menu loading from backend
- Cache menu structure

#### Common Error Handling
**Location**: `shared/lib/api-client` + `shared/ui/organisms/Toast`

**Implementation:**
- Axios/fetch interceptor
- Error code to message mapping
- Auto-display toast/modal

#### Attachment CRUD
**Location**: `features/attachment/`

**Implementation:**
- Upload component with progress
- File list component
- Download/delete actions
- Page-scoped file association

#### SSE Notifications
**Location**: `features/notification/` + `shared/lib/sse-client`

**Implementation:**
- SSE client connection
- Event dispatcher
- Notification widget UI
- Unread count management

#### Help Integration
**Location**: `features/help/` + `shared/ui/organisms/HelpViewer`

**Implementation:**
- Context-aware help lookup
- Help modal/panel
- Markdown content rendering

#### Dark Mode
**Location**: `features/theme/`

**Implementation:**
- CSS variable switching
- Persistence in localStorage
- System preference detection
- Tailwind dark mode integration

#### Skeleton UI
**Location**: `shared/ui/atoms/Skeleton` + page components

**Implementation:**
- Skeleton component variants
- Loading state detection
- Automatic fallback rendering

#### Form/Grid/Search Commonization
**Location**: `shared/ui/organisms/FormBuilder`, `DataTable`, `SearchPanel`

**Implementation:**
- Schema-driven rendering
- Reusable templates
- Standard validation
- Consistent styling

#### Page State Management
**Location**: `shared/lib/page-state-manager` + `widgets/tab-manager`

**Implementation:**
- State machine (Idle, Dirty, Loading, Error)
- Status indicators
- Error boundaries
- Retry mechanisms

### Recommended Additional Features

#### Breadcrumb
**Location**: `widgets/breadcrumb/`

**Features:**
- Auto-generated from route meta
- Manual override support
- Click navigation
- Customizable separator

#### Layout Container
**Location**: `app/layouts/default.vue`

**Features:**
- Responsive header/sidebar/footer
- Collapsible sidebar
- Sticky header
- Footer auto-hide on scroll

#### Lazy Loading
**Implementation**: Nuxt auto-imports + dynamic imports

**Usage:**
```vue
<script setup>
// Lazy load heavy component
const HeavyComponent = defineAsyncComponent(() =>
  import('~/features/detection/ui/DetectionChart.vue')
)
</script>
```

#### Infinite Scroll
**Location**: `shared/lib/composables/useInfiniteScroll.ts`

**Features:**
- Intersection Observer API
- Page-based loading
- Loading state management
- End of list detection

#### Accessibility (A11y)
**Implementation**: Throughout all components

**Requirements:**
- ARIA labels and roles
- Keyboard navigation support
- Focus management
- Screen reader compatibility
- Color contrast compliance (WCAG AA)
- Skip to main content link

**Checklist:**
- [ ] All interactive elements keyboard accessible
- [ ] Proper heading hierarchy (h1-h6)
- [ ] Form labels associated with inputs
- [ ] Error messages announced
- [ ] Loading states announced
- [ ] Modal focus trap
- [ ] Toast announcements

## RealGrid2 Integration

### Overview

RealGrid2는 프로젝트의 핵심 데이터 그리드 솔루션입니다.

**License**: Commercial license required
**Version**: RealGrid2
**Wrapper Location**: `shared/ui/organisms/DataTable.vue`

### Recommended Grid Configuration

모든 DataTable 컴포넌트는 다음 권장 설정을 기본값으로 사용합니다:

```typescript
const defaultGridOptions = {
  // Editing
  edit: {
    editable: true,
    commitByCell: true,        // ✅ 셀 편집 완료 즉시 저장
    commitWhenLeave: true,     // ✅ 그리드 외부 클릭 시 저장
    crossWhenExitLast: true,   // ✅ Tab으로 마지막 열 벗어날 때 다음 행으로
    exceptDataClickWhenButton: true  // ✅ 버튼 클릭 시 추가 이벤트 방지
  },

  // Display
  display: {
    rowHeight: 32,
    rowResizable: false,
    editItemMerging: true      // ✅ 병합 셀 편집 시 분리 방지
  },

  // Sorting & Filtering
  sortMode: 'explicit',        // ✅ 자동 재정렬 방지
  filterMode: 'explicit',      // ✅ 자동 재필터링 방지

  // CheckBar
  checkBar: {
    visible: true,
    syncHeadCheck: true        // ✅ 전체 선택 시 헤더 체크
  },

  // Copy/Paste
  copy: {
    enabled: true,
    lookupDisplay: true        // ✅ 드롭다운 라벨 복사
  },
  paste: {
    enabled: true,
    convertLookupLabel: true,  // ✅ 라벨을 값으로 변환
    checkDomainOnly: true,     // ✅ 드롭다운 외 값 거부
    checkReadOnly: true,       // ✅ 읽기 전용 열 건너뛰기
    numberChars: [',']         // ✅ 천단위 구분자 처리
  },

  // Soft Delete
  softDeleting: true,          // ✅ 논리적 삭제
  hideDeletedRows: false       // 삭제된 행 표시
}
```

### Column Types & Best Practices

#### Dropdown Column
```typescript
{
  name: 'status',
  fieldName: 'status',
  header: { text: '상태' },
  editor: {
    type: 'dropdown',
    textReadOnly: true,      // ✅ 필수: 키보드 입력 방지
    domainOnly: true,        // ✅ 필수: 목록 값만 허용
    dropDownWhenClick: true, // ✅ 권장: 클릭 시 드롭다운 열기
    values: ['active', 'inactive'],
    labels: ['활성', '비활성']
  },
  lookupDisplay: true        // ✅ 라벨 표시
}
```

#### Checkbox Column
```typescript
{
  name: 'enabled',
  fieldName: 'enabled',
  header: { text: '활성화' },
  editable: false,           // ✅ 필수! (체크박스 렌더러 요구사항)
  renderer: {
    type: 'check',
    trueValues: 'Y',
    falseValues: 'N',
    editable: true
  }
}
```

#### Number Column with Footer
```typescript
{
  name: 'amount',
  fieldName: 'amount',
  header: { text: '금액' },
  dataType: 'number',
  numberFormat: '#,##0',
  styleName: 'right-number',
  footer: {
    expression: 'sum',       // sum, avg, min, max, count
    numberFormat: '#,##0'
  }
}
```

### Custom Renderers

#### Creating Custom Renderer

```typescript
// shared/ui/organisms/DataTable/renderers/StatusRenderer.ts
import { RealGrid } from 'realgrid'

export class StatusRenderer extends RealGrid.CustomCellRendererImpl {
  get styleName() {
    return 'rg-renderer custom-status-renderer'
  }

  _doInitContent(parent: HTMLElement) {
    this._span = document.createElement('span')
    this._span.className = 'status-badge'
    parent.appendChild(this._span)
  }

  _doClearContent(parent: HTMLElement) {
    parent.innerHTML = ''
  }

  render(grid: any, model: any, width: number, height: number) {
    const value = model.value
    this._span.className = `status-badge status-${value}`
    this._span.textContent = this.getLabel(value)
  }

  canEdit() { return false }
  canClick() { return false }

  get showTooltip() { return true }
  tooltip(model: any) { return `상태: ${this.getLabel(model.value)}` }

  private getLabel(value: string): string {
    const labels: Record<string, string> = {
      'pending': '대기',
      'approved': '승인',
      'rejected': '거부'
    }
    return labels[value] || value
  }

  private _span!: HTMLSpanElement
}
```

#### Registration

```typescript
// app/providers/realgrid.ts
import { RealGrid } from 'realgrid'
import { StatusRenderer } from '~/shared/ui/organisms/DataTable/renderers/StatusRenderer'

export default defineNuxtPlugin(() => {
  // Register custom renderers once
  RealGrid.registerCustomRenderer('status_renderer', StatusRenderer)
})
```

#### Usage

```typescript
const columns = [{
  name: 'status',
  fieldName: 'status',
  renderer: { type: 'status_renderer' }
}]
```

### Performance Best Practices

#### Batch Updates

```typescript
// ❌ Bad: Multiple redraws
grid.updateRow(0, data1)
grid.updateRow(1, data2)
grid.updateRow(2, data3)

// ✅ Good: Single redraw
grid.beginUpdate()
grid.updateRow(0, data1)
grid.updateRow(1, data2)
grid.updateRow(2, data3)
grid.endUpdate()
```

#### Virtual Scrolling

RealGrid2는 기본적으로 virtual scrolling을 지원하여 대용량 데이터(10만+ 행)도 빠르게 렌더링합니다.

#### Lazy Loading

```typescript
const loadMore = async () => {
  const newData = await fetchData(page++)
  grid.appendRows(newData)
}
```

### Common Issues & Solutions

#### Issue: 병합 셀이 편집 시 분리됨
**Solution**: `displayOptions.editItemMerging = true` 설정

#### Issue: 붙여넣기 시 드롭다운 라벨이 변환되지 않음
**Solution**: `pasteOptions.convertLookupLabel = true` 설정

#### Issue: 체크박스를 클릭할 수 없음
**Solution**: 컬럼에 `editable: false` 설정 (필수)

#### Issue: 커스텀 렌더러가 표시되지 않음
**Solution**: 그리드 초기화 전에 렌더러 등록 확인

### DataTable Wrapper API

```typescript
// Props
interface DataTableProps {
  columns: GridColumn[]
  data: any[]
  options?: GridOptions
  height?: string | number
  showCheckBar?: boolean
  showStateBar?: boolean
}

// Events
interface DataTableEmits {
  'row-click': (row: any) => void
  'cell-click': (event: CellClickEvent) => void
  'cell-edit': (event: CellEditEvent) => void
  'cell-button-click': (event: CellButtonClickEvent) => void
  'selection-changed': (selection: any[]) => void
}

// Methods
interface DataTableMethods {
  setData(data: any[]): void
  appendRow(row: any): void
  updateRow(index: number, row: any): void
  deleteRow(index: number): void
  getSelectedRows(): any[]
  exportToExcel(filename: string): void
  exportToCsv(filename: string): void
}
```

### Resources

- **Detailed Guide**: [DataTable README](frontend/shared/ui/organisms/DataTable/README.md)
- **Official Docs**: [RealGrid2 Documentation](https://docs.realgrid.com/)
- **Recommended Options**: [Grid Settings Guide](https://docs.realgrid.com/guides/tip/recommended-options)
- **Custom Renderers**: [Renderer Guide](https://docs.realgrid.com/guides/renderer/class-custom-renderer)

## Core Architecture Principles

### ~~1. Multi-Tenancy (Highest Priority)~~

~~**Complete isolation per financial institution (tenant):**~~
- ~~Tenant-specific policies, rules, detection criteria, thresholds, and snapshots~~
- ~~Per-tenant admin controls, configurations, and reporting~~
- ~~Network/data complete separation (VPC/Subnet or Schema-based)~~

~~**All tables must include tenant isolation:**~~
- ~~Add `tenant_id` column to every table~~
- ~~Enforce tenant filtering in all queries via MyBatis interceptor or row-level security~~
- ~~Cache keys must include tenant prefix~~
- ~~File storage must use tenant-specific paths~~

### 2. Snapshot-Based Versioning

**All policy/standard data uses snapshot structure:**
- Similar to Spring Batch's `BATCH_JOB_INSTANCE` / `BATCH_JOB_EXECUTION` pattern
- Core table: `STANDARD_SNAPSHOT` with fields:
  - `id`, `type` (KYC/STR/CTR/RBA/WLF/FIU)
  - `version`, `effective_from`, `effective_to`
  - `status`, `created_by`, `approved_by`
  - `prev_id`, `next_id` (version chain)

**Version control requirements:**
- Immutable once approved/deployed
- Support rollback to previous snapshot versions
- What-if analysis using draft snapshots
- Audit trail of all version changes

### 3. Audit Requirements (100% Coverage)

**Every action must be logged:**
- User access logs (login, page views, data access)
- Data modification logs (before/after state)
- Configuration changes (policy updates, rule deployments)
- Approval workflows (who approved/rejected what, when)

**Implementation:**
- `AUDIT_LOG` table with: `who`, `when`, `what`, `before_json`, `after_json`
- Use AOP interceptors for automatic audit logging
- Never delete audit logs (retention policy: regulatory requirement, typically 5-10 years)

### 4. Separation of Duties (SoD)

**Maker-Checker principle:**
- Same user cannot both create and approve (STR/CTR cases, policy changes, etc.)
- Enforce at application level via Role-Based Access Control (RBAC)
- Approval lines configured by organization + permission group

### 5. Detection Engine Architecture

**Core data flow:**
```
Data Ingestion → Detection (STR/CTR/WLF) → Event Generation → Case Creation →
Investigation → Approval Workflow → Reporting → FIU Submission
```

**Detection tables:**
- `TX_STAGING` - Incoming transaction data
- `DETECTION_EVENT` - Rule matches (linked to `snapshot_ver`)
- `ALERT_CASE` - Investigation cases
- `CASE_ACTIVITY` - Case actions/comments
- `REPORT_STR` / `REPORT_CTR` - Final reports

**Batch processing structure (Spring Batch pattern):**
- `INSPECTION_INSTANCE` - Job instance (type, snapshot_ver, created_ts)
- `INSPECTION_EXECUTION` - Job execution (status, start/end time, read/write/skip counts)

## Key Technical Requirements

### Performance Targets

- **Batch processing:** ≥10 million transactions/day (overnight window)
- **Real-time API:** 300-500 TPS peak load
- **UI response:** <1 second (queries), <3 seconds (complex searches)
- **Availability:** 99.9% SLA

### Security Requirements

- **Encryption:** TLS in transit, field-level encryption for PII at rest
- **Authentication:** Spring Security + JWT/OAuth2
- **Authorization:** RBAC + SoD enforcement
- **Data masking:** Configurable by role (via `DATA_POLICY` table)
- **Access control:** Menu permissions managed in `MENU` table with `PERM_GROUP_FEATURE` mapping

### Database Design Patterns

**User & Permission Structure:**
```
USER → belongs to → ORGANIZATION → references → ORGANIZATION_POLICY
USER → belongs to → PERMISSION_GROUP → has → MENU + FEATURE_ACTION permissions
```

**Approval Line:**
- Template-based by business type
- Each step references `PERMISSION_GROUP` (not individual users)
- Example: `STEP1=PG.REVIEWER`, `STEP2=PG.APPROVER`, `STEP3=PG.HEAD_COMPLIANCE`
- Self-approval prevention (same user cannot be requester and approver)

**Data Policy (Row-Level + Field-Level):**
```sql
DATA_POLICY(
  policy_id, feature_code, action_code, perm_group_code, org_policy_id,
  business_type, row_scope, default_mask_rule, priority, active
)
```
- `row_scope`: OWN | ORG | ALL | CUSTOM
- `default_mask_rule`: NONE | PARTIAL | FULL | HASH

## Korean Terminology Reference

Key terms from PRD (Korean ↔ English):
- **자금세탁방지** = Anti-Money Laundering (AML)
- **의심거래보고** = Suspicious Transaction Report (STR)
- **고액현금거래보고** = Currency Transaction Report (CTR)
- **감시대상명단** = Watch List Filtering (WLF)
- **준법감시** = Compliance Monitoring
- **금융정보분석원** = Financial Intelligence Unit (FIU)
- **스냅샷** = Snapshot (versioning system)
- **결재선** = Approval Line
- **권한그룹** = Permission Group

## Development Commands

**Once the project is initialized, standard commands will be:**

### Backend (Gradle)
```bash
# Build all modules
./gradlew clean build

# Run tests
./gradlew test

# Run specific test
./gradlew test --tests com.example.ClassName.methodName

# Start local server
./gradlew bootRun

# Check dependencies
./gradlew dependencies
```

### Frontend (Nuxt)
```bash
# Install dependencies
npm install

# Development server
npm run dev

# Build for production (SPA mode)
npm run build

# Preview production build
npm run preview

# Run tests
npm run test

# Lint
npm run lint
```

## Important Implementation Notes

1. **⚠️ SSR STRICTLY FORBIDDEN:** Frontend MUST use `ssr: false` in Nuxt config - NEVER enable SSR, NEVER use `/server` directory, NEVER use Nuxt Server API. Deploy as static resources only (Nginx/Apache). See "Frontend Critical Constraints" section for details.
2. **🖥️ WEB BROWSER FOCUS:** UI targets desktop/laptop browsers (1366px+ resolutions). Mobile-specific design NOT required. Responsive design IS required for various desktop resolutions. Use desktop-class navigation, multi-column layouts, data grids. See "Web Browser Focus" section for details.
3. **Tailwind Prefix:** Use `tw-` prefix to avoid conflicts with PrimeVue/RealGrid CSS
4. **Field Naming:** Use snake_case for database columns, camelCase for Java/TypeScript
5. **ID Strategy:** Use ULID or UUID for global identifiers, avoid sequential integers for security
6. **MyBatis TypeHandlers:** Create custom handlers for LocalDateTime, JSON columns, encrypted fields
7. **Cache Strategy:** ~~Redis with tenant-prefixed keys,~~ Redis cache with Caffeine for local L1 cache
8. **API Versioning:** Use `/api/v1/` prefix, prepare for future versions
9. **Error Handling:** Unified exception handler with i18n error codes
10. **Logging:** Structured JSON logs with trace IDs for distributed tracing
11. **Testing:** Minimum 80% code coverage for business logic

## MVP Scope (Priority)

**Must-Have Features:**
1. ~~Multi-tenancy management (tenant isolation, admin controls)~~
2. Policy & criteria management (snapshot-based, KYC/STR/CTR/WLF rules)
3. Detection engine (STR/CTR/WLF with configurable rules)
4. Investigation & workflow (case management, approval workflows)
5. Reporting module (FIU-compliant report generation)
6. User & access control (RBAC, SoD enforcement, approval lines)
7. Audit logging (100% coverage)
8. Basic dashboard (detection metrics, case status)

**Future Enhancements:**
- Risk simulation module (what-if analysis, backtesting)
- Advanced WLF matching algorithms (fuzzy matching, ensemble scoring)
- SMTP email system for product risk surveys
- ML-based rule optimization
- Regulatory change notifications

## Reference

**📚 All documentation is centralized in `/docs` directory. See [docs/index.md](./docs/index.md) for complete navigation.**

### Core Documentation

- **[Documentation Center](./docs/index.md)** - Start here, complete navigation guide
- **[PRD Index](./docs/prd/index.md)** - Product requirements (15 documents, ~7,000 lines total)
  - Original `PRD.md` (486 lines) split into structured documents
- **[API Contract](./docs/api/CONTRACT.md)** ⭐ Frontend ↔ Backend API 계약 및 동기화 계획

### Frontend

- **[Frontend README](./docs/frontend/README.md)** - SPA constraints, coding rules, deployment (449 lines)
- **[Components Roadmap](./docs/frontend/COMPONENTS_ROADMAP.md)** ⭐ Implementation progress (675 lines)
  - Atomic Design structure, Phase 2 implementation guide
- **[Frontend Agents](./docs/frontend/AGENTS.md)** - Mock API, Component Generator, Build Validator

### Backend

- **[Backend Agents](./docs/backend/AGENTS.md)** - API Generator, Module Validator
- **[ULID Guide](./docs/backend/ULID.md)** - Identifier strategy

### Architecture

- **[DDD Design](./docs/architecture/DDD_DESIGN.md)** - Domain-Driven Design layers, testing strategy

### Development

- **[Development Guide](./docs/development/AGENTS.md)** - Coding style, testing, commit rules

### Timeline

- **MVP Go-Live Target:** January 20, 2026
