/**
 * Auth middleware - redirects to the actual middleware in features/auth
 * Nuxt requires middleware files to be in the middleware directory
 */
export { default } from '~/features/auth/model/auth.middleware'
