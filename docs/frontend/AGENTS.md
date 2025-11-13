# Development Agents & Tools

This document describes the automated agents and tools available for this project.

## 🤖 Available Agents

### 1. Mock API Server (MSW)

**Purpose:** Provides mock backend API responses for E2E tests without requiring a running backend.

**Location:** `tests/mocks/`

**Files:**
- `handlers.ts` - Mock API route handlers
- `browser.ts` - MSW browser setup
- `fixtures/mock-api.fixture.ts` - Playwright fixture for E2E tests

**Usage:**

```typescript
// In E2E tests
import { test, expect } from './fixtures/mock-api.fixture'

test('login with mock API', async ({ page, mockApi }) => {
  // mockApi is automatically configured
  await page.fill('input[type="text"]', 'admin')
  await page.fill('input[type="password"]', 'admin123')
  await page.click('button[type="submit"]')

  // Mock API will return success response
  await expect(page).toHaveURL(/.*dashboard/)
})
```

**Mock API Endpoints:**
- `POST /api/v1/auth/login` - Login with credentials
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout
- `GET /api/v1/auth/me` - Get current user info
- `GET /api/v1/dashboard/stats` - Dashboard statistics
- `GET /api/v1/cases/recent` - Recent cases

**Credentials:**
- Valid: `username: admin`, `password: admin123`
- Invalid: Any other combination

### 2. Component Generator CLI

**Purpose:** Automatically generates FSD-compliant components with proper structure, types, and tests.

**Command:**
```bash
npm run generate:component -- --layer=<layer> --name=<name> [--type=<type>]
```

**Parameters:**
- `--layer` (required): Layer name (`shared`, `entities`, `features`, `widgets`, `pages`)
- `--name` (required): Component name in kebab-case (e.g., `user-card`)
- `--type` (required for shared): Type for shared layer (`atoms`, `molecules`, `organisms`)

**Examples:**

```bash
# Generate an atom component
npm run generate:component -- --layer=shared --type=atoms --name=badge

# Generate a feature component
npm run generate:component -- --layer=features --name=case-filter

# Generate a widget component
npm run generate:component -- --layer=widgets --name=case-table
```

**Generated Files:**

For `shared` layer (e.g., `--layer=shared --type=atoms --name=badge`):
```
shared/ui/atoms/
├── Badge.vue          # Vue component
├── types.ts           # TypeScript types
├── Badge.spec.ts      # Vitest unit tests
├── index.ts           # Export barrel
└── README.md          # Component documentation
```

For other layers (e.g., `--layer=features --name=case-filter`):
```
features/case-filter/ui/
├── CaseFilter.vue     # Vue component
├── types.ts           # TypeScript types
├── CaseFilter.spec.ts # Vitest unit tests
├── index.ts           # Export barrel
└── README.md          # Component documentation
```

**Component Template Features:**
- ✅ TypeScript with proper type definitions
- ✅ Props and Emits interfaces
- ✅ Tailwind CSS with `tw-` prefix
- ✅ Unit test scaffolding
- ✅ Comprehensive documentation
- ✅ Import/export barrel

### 3. Build Validator

**Purpose:** Validates that the project follows SSR-free SPA constraints.

**Command:**
```bash
npm run generate:validate
```

**Checks:**

1. **nuxt.config.ts validation**
   - ✅ `ssr: false` is set
   - ❌ No `ssr: true`
   - ✅ Nitro config is SPA-compatible (static compression only)
   - ❌ No server-side Nitro features (handlers, plugins, middleware)

2. **Directory structure**
   - ❌ No `/server` directory

3. **Code analysis**
   - ❌ No `defineEventHandler` usage
   - ❌ No `defineNitroPlugin` usage
   - ❌ No `useRuntimeConfig().private` usage
   - ⚠️  `useAsyncData` usage (warn - verify client-side only)
   - ⚠️  `useFetch` usage (warn - verify external API calls)

4. **Build output** (if exists)
   - ✅ Static files present (`index.html`, `_nuxt/*`)
   - ⚠️  No server output being deployed

**Exit Codes:**
- `0` - Validation passed (with or without warnings)
- `1` - Validation failed (errors found)

**Example Output:**

```bash
$ npm run generate:validate

🔍 Validating SPA Build Configuration...

ℹ️  Checking nuxt.config.ts...
✅ nuxt.config.ts has `ssr: false`
✅ Nitro configuration is SPA-compatible (static compression only)
ℹ️  Checking for /server directory...
✅ No /server directory found
ℹ️  Checking for server API usage...
⚠️  WARNING: features/auth/ui/LoginForm.vue uses useFetch (2 times)
ℹ️  Checking build output...
✅ Build output contains static files (index.html, _nuxt/*)
✅ No server output directory found

============================================================

⚠️  Validation PASSED with warnings - please review
```

## 📋 Workflow Integration

### Pre-commit Validation

Add to `.git/hooks/pre-commit` (or use husky):

```bash
#!/bin/bash
npm run generate:validate
if [ $? -ne 0 ]; then
  echo "Build validation failed. Commit aborted."
  exit 1
fi
```

### CI/CD Pipeline

Add to `.github/workflows/validate.yml`:

```yaml
name: Validate Build

on: [push, pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm install
      - run: npm run generate:validate
```

### Component Development Workflow

1. **Generate component:**
   ```bash
   npm run generate:component -- --layer=features --name=new-feature
   ```

2. **Implement component:**
   - Edit `features/new-feature/ui/NewFeature.vue`
   - Define types in `types.ts`
   - Write tests in `NewFeature.spec.ts`

3. **Run tests:**
   ```bash
   npm test NewFeature
   ```

4. **Validate build:**
   ```bash
   npm run generate:validate
   ```

5. **Commit changes:**
   ```bash
   git add features/new-feature
   git commit -m "feat: add new-feature component"
   ```

## 🔧 Extending Agents

### Adding New Mock Endpoints

Edit `tests/mocks/handlers.ts`:

```typescript
export const handlers = [
  // ... existing handlers

  // Add new endpoint
  http.get(`${API_BASE}/new-endpoint`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        // your mock data
      },
    })
  }),
]
```

### Customizing Component Templates

Edit `scripts/generate-component.mjs`:

```javascript
function generateComponent(name, layer, type) {
  // Modify template here
  return `<script setup lang="ts">
// Your custom template
</script>`
}
```

### Adding New Validation Rules

Edit `scripts/validate-build.mjs`:

```javascript
async function validateCustomRule() {
  info('Checking custom rule...')

  // Your validation logic
  if (violationFound) {
    error('Custom rule violation detected')
  } else {
    success('Custom rule passed')
  }
}

// Add to main()
await validateCustomRule()
```

## 📚 Best Practices

1. **Always run validation before committing:**
   ```bash
   npm run generate:validate
   ```

2. **Use component generator for consistency:**
   - Don't manually create component files
   - Always use the generator to maintain structure

3. **Test with mock API first:**
   - Write E2E tests with mock API
   - Then test against real backend

4. **Keep mock data realistic:**
   - Use actual data structures
   - Include error cases
   - Test edge cases

5. **Review warnings:**
   - Validation warnings should be reviewed
   - Document exceptions when necessary

## 🐛 Troubleshooting

### Mock API not intercepting requests

**Problem:** E2E tests still fail with "API not found" errors.

**Solution:**
1. Check that fixture is imported:
   ```typescript
   import { test, expect } from './fixtures/mock-api.fixture'
   ```
2. Verify `mockApi` is in test parameters:
   ```typescript
   test('name', async ({ page, mockApi }) => { ... })
   ```
3. Check API base URL matches mock handlers

### Component generator creates wrong structure

**Problem:** Files generated in unexpected locations.

**Solution:**
1. Verify layer and type parameters
2. Check that layer is valid: `shared`, `entities`, `features`, `widgets`, `pages`
3. For `shared` layer, `--type` is required

### Validation fails with false positives

**Problem:** Validator reports errors for valid SPA code.

**Solution:**
1. Check if using `vite.server` (allowed for dev server)
2. Verify Nitro config is static-only (compression, minify)
3. Review warning messages for `useAsyncData`/`useFetch` usage
4. Ensure client-side only data fetching

## 📖 Related Documentation

- [API Contract](../API-CONTRACT.md) ⭐ **Frontend ↔ Backend API 계약 및 동기화 계획 (필독!)**
- [Frontend README](./README.md) - Frontend setup and development
- [Component Roadmap](./COMPONENTS_ROADMAP.md) - Component implementation plan
- [Backend Agents](../backend/AGENTS.md) - Backend automation tools
- [CLAUDE.md](../CLAUDE.md) - Project guidelines for AI assistance
