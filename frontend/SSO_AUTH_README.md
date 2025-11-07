# SSO Authentication Implementation

## Overview

This project implements **Cookie-based SSO (Single Sign-On) authentication** for the Inspect-Hub frontend application.

## Architecture

### SSO Client Interface

The SSO authentication is abstracted through a well-defined interface (`ISSOAuthClient`) located in:
```
shared/lib/sso/types.ts
```

This interface can be implemented by any SSO provider library without changing the application code.

### Current Implementation

**Mock SSO Client** (`shared/lib/sso/mock-client.ts`):
- Provides a working SSO implementation for development and testing
- Simulates SSO login flow with mock users
- Stores session in localStorage and cookies
- **Should be replaced with actual SSO library when provided**

### Key Components

1. **Auth Store** (`features/auth/model/auth.store.ts`)
   - Manages authentication state using Pinia
   - Provides methods: `login()`, `logout()`, `refreshSession()`, `validateSession()`
   - Tracks user session and authentication status
   - Uses SSO client internally

2. **Auth Middleware** (`features/auth/model/auth.middleware.ts`)
   - Protects routes requiring authentication
   - Redirects unauthenticated users to login page
   - Validates session on server-side requests

3. **Auth Provider Plugin** (`app/providers/auth.ts`)
   - Initializes SSO client on app startup
   - Restores session if available

## SSO Flow

### Login Flow

1. User accesses protected route
2. Middleware detects no authentication → redirect to `/login`
3. User clicks login button
4. `authStore.login()` is called
5. SSO client handles authentication:
   - **Mock**: Creates session directly
   - **Real SSO**: Redirects to SSO provider
6. After successful authentication:
   - Session is stored in localStorage + cookie
   - User data is saved in auth store
   - Redirect to original destination

### Session Management

- **Client-side**: Session stored in localStorage and cookies
- **Server-side**: Session validated via SSO provider (in production)
- **Auto-refresh**: Session can be refreshed before expiration
- **Logout**: Supports both local and global (SSO provider) logout

## Replacing Mock with Real SSO

To integrate with an actual SSO provider:

1. **Install SSO provider library**:
   ```bash
   npm install your-sso-library
   ```

2. **Implement ISSOAuthClient interface**:
   ```typescript
   // shared/lib/sso/real-client.ts
   import type { ISSOAuthClient } from './types'
   import YourSSOLibrary from 'your-sso-library'

   export class RealSSOClient implements ISSOAuthClient {
     private ssoLib: YourSSOLibrary

     async initialize() {
       this.ssoLib = new YourSSOLibrary({
         // Configuration
       })
       await this.ssoLib.init()
     }

     async login(options?) {
       // Redirect to SSO provider
       await this.ssoLib.login(options)
     }

     async getSession() {
       return await this.ssoLib.getSession()
     }

     // Implement other methods...
   }
   ```

3. **Update the SSO client factory**:
   ```typescript
   // shared/lib/sso/index.ts
   export { getSSOClient, resetSSOClient } from './real-client'
   // Instead of './mock-client'
   ```

## Testing

### Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run tests with UI
npm run test:ui
```

### Test Coverage

Current coverage (Auth module):
- **Auth Store**: 81% lines, 90% branches
- **Auth Middleware**: 78% lines, 70% branches

### Test Files

- `features/auth/model/__tests__/auth.store.spec.ts` - Auth store unit tests (17 tests)
- `features/auth/model/__tests__/auth.middleware.spec.ts` - Middleware tests (6 tests)

### Testing Strategy

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test auth flow end-to-end (to be added)
- **E2E Tests**: Test complete user journeys (to be added)

## Security Considerations

1. **Cookie Security**:
   - Cookies use `SameSite=Strict` to prevent CSRF
   - Set `HttpOnly` flag in production (via SSO provider)
   - Use `Secure` flag in production (HTTPS only)

2. **Session Validation**:
   - Server-side session validation on every request
   - Automatic session refresh before expiration
   - Clear session data on logout

3. **Token Storage**:
   - Access tokens should be short-lived
   - Refresh tokens should be securely stored
   - Never expose tokens in URLs or logs

## API Reference

### Auth Store Methods

```typescript
// Initialize SSO and restore session
await authStore.initialize()

// Login (redirects to SSO provider)
await authStore.login({ redirectUrl: '/dashboard' })

// Handle SSO callback (after redirect back)
await authStore.handleLoginCallback()

// Logout
await authStore.logout({ global: true }) // Global SSO logout

// Refresh session
await authStore.refreshSession()

// Validate session
const isValid = await authStore.validateSession()

// Check permissions
authStore.hasRole('admin') // boolean
authStore.hasPermission('write') // boolean
```

### SSO Client Interface

```typescript
interface ISSOAuthClient {
  initialize(): Promise<void>
  isAuthenticated(): Promise<boolean>
  getSession(): Promise<SSOSession | null>
  getUser(): Promise<SSOUser | null>
  login(options?: SSOLoginOptions): Promise<void>
  handleLoginCallback(): Promise<SSOSession>
  logout(options?: SSOLogoutOptions): Promise<void>
  refreshSession(): Promise<SSOSession>
  validateSession(): Promise<boolean>
  getAccessToken(): Promise<string | null>
}
```

## Configuration

### Environment Variables

```env
NUXT_PUBLIC_API_BASE=http://localhost:8090/api/v1
```

### SSO Provider Configuration

When integrating real SSO provider, you'll need:

```typescript
// Example configuration
{
  clientId: 'your-client-id',
  authority: 'https://sso.example.com',
  redirectUri: 'http://localhost:3000/auth/callback',
  scope: 'openid profile email',
  responseType: 'code',
}
```

## Troubleshooting

### Build Issues

If you encounter TypeScript errors during build:
```bash
# Regenerate .nuxt types
rm -rf .nuxt
npx nuxt prepare
npm run build
```

### Test Issues

If tests fail:
```bash
# Clear test cache
rm -rf node_modules/.cache
npm test
```

### Session Issues

If session is not persisting:
1. Check browser localStorage
2. Check cookies in DevTools
3. Verify SSO client initialization in console
4. Check network tab for API calls

## Future Enhancements

- [ ] Add E2E tests with Playwright
- [ ] Implement token refresh mechanism
- [ ] Add session timeout warnings
- [ ] Implement remember-me functionality
- [ ] Add biometric authentication support
- [ ] Add MFA (Multi-Factor Authentication)
- [ ] Add session analytics and monitoring

## License

UNLICENSED - Private project
