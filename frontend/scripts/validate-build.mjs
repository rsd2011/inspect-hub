#!/usr/bin/env node

/**
 * Build Validator
 *
 * Validates that the project follows SSR-free SPA constraints:
 * 1. nuxt.config.ts has ssr: false
 * 2. No /server directory exists
 * 3. No server API usage (useAsyncData, useFetch with server-side)
 * 4. Build output is static files only
 *
 * Usage:
 *   npm run generate:validate
 */

import fs from 'fs/promises'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const ROOT_DIR = path.resolve(__dirname, '..')

let hasErrors = false
let hasWarnings = false

function error(message) {
  console.error(`❌ ERROR: ${message}`)
  hasErrors = true
}

function warning(message) {
  console.warn(`⚠️  WARNING: ${message}`)
  hasWarnings = true
}

function success(message) {
  console.log(`✅ ${message}`)
}

function info(message) {
  console.log(`ℹ️  ${message}`)
}

// Check 1: Validate nuxt.config.ts has ssr: false
async function validateNuxtConfig() {
  info('Checking nuxt.config.ts...')

  const configPath = path.join(ROOT_DIR, 'nuxt.config.ts')

  try {
    const content = await fs.readFile(configPath, 'utf-8')

    // Check for ssr: false
    const ssrFalseRegex = /ssr\s*:\s*false/
    const ssrTrueRegex = /ssr\s*:\s*true/

    if (ssrTrueRegex.test(content)) {
      error('nuxt.config.ts has `ssr: true` - MUST be `ssr: false`')
    } else if (!ssrFalseRegex.test(content)) {
      warning('nuxt.config.ts does not explicitly set `ssr: false`')
    } else {
      success('nuxt.config.ts has `ssr: false`')
    }

    // Check for server directory usage (but allow vite.server which is dev server config)
    const hasServerConfig = /(?<!vite\s*:\s*\{[^}]*)\bserver\s*:/g.test(content) &&
                            !content.includes('vite:')

    if (content.includes('serverDir') || (hasServerConfig && !content.includes('vite'))) {
      error('nuxt.config.ts contains server configuration - not allowed for SPA mode')
    }

    // Check for Nitro configuration (allow static compression/minify)
    if (content.includes('nitro:')) {
      const nitroBlock = content.match(/nitro\s*:\s*\{([^}]*)\}/s)?.[1] || ''

      // Check for server-side Nitro features
      const serverFeatures = ['handlers', 'plugins', 'middleware', 'routes']
      const hasServerFeatures = serverFeatures.some(feature => nitroBlock.includes(feature))

      if (hasServerFeatures) {
        error('nuxt.config.ts contains server-side Nitro features - not allowed for SPA mode')
      } else {
        success('Nitro configuration is SPA-compatible (static compression only)')
      }
    }
  } catch (err) {
    error(`Failed to read nuxt.config.ts: ${err.message}`)
  }
}

// Check 2: Validate no /server directory exists
async function validateNoServerDirectory() {
  info('Checking for /server directory...')

  const serverPath = path.join(ROOT_DIR, 'server')

  try {
    await fs.access(serverPath)
    error('/server directory exists - MUST be removed for SPA mode')
  } catch {
    success('No /server directory found')
  }
}

// Check 3: Search for server API usage
async function validateNoServerApiUsage() {
  info('Checking for server API usage...')

  const forbiddenPatterns = [
    { pattern: /defineEventHandler/g, name: 'defineEventHandler' },
    { pattern: /defineNitroPlugin/g, name: 'defineNitroPlugin' },
    { pattern: /useRuntimeConfig\(\)\.private/g, name: 'useRuntimeConfig().private' },
  ]

  const suspiciousPatterns = [
    { pattern: /useAsyncData/g, name: 'useAsyncData (verify it\'s client-side only)' },
    { pattern: /useFetch\s*\(/g, name: 'useFetch (verify it\'s for external APIs)' },
  ]

  const directories = ['app', 'pages', 'features', 'widgets', 'entities', 'shared', 'composables']

  for (const dir of directories) {
    const dirPath = path.join(ROOT_DIR, dir)

    try {
      await fs.access(dirPath)
      await scanDirectory(dirPath, forbiddenPatterns, suspiciousPatterns)
    } catch {
      // Directory doesn't exist, skip
    }
  }
}

async function scanDirectory(dirPath, forbiddenPatterns, suspiciousPatterns) {
  const entries = await fs.readdir(dirPath, { withFileTypes: true })

  for (const entry of entries) {
    const fullPath = path.join(dirPath, entry.name)

    if (entry.isDirectory()) {
      // Skip node_modules, .nuxt, .output, etc.
      if (entry.name === 'node_modules' || entry.name.startsWith('.')) {
        continue
      }
      await scanDirectory(fullPath, forbiddenPatterns, suspiciousPatterns)
    } else if (entry.name.endsWith('.ts') || entry.name.endsWith('.vue') || entry.name.endsWith('.js')) {
      await scanFile(fullPath, forbiddenPatterns, suspiciousPatterns)
    }
  }
}

async function scanFile(filePath, forbiddenPatterns, suspiciousPatterns) {
  const content = await fs.readFile(filePath, 'utf-8')
  const relativePath = path.relative(ROOT_DIR, filePath)

  // Check forbidden patterns
  for (const { pattern, name } of forbiddenPatterns) {
    if (pattern.test(content)) {
      error(`${relativePath} uses ${name} - server-side API is forbidden`)
    }
  }

  // Check suspicious patterns
  for (const { pattern, name } of suspiciousPatterns) {
    const matches = content.match(pattern)
    if (matches) {
      warning(`${relativePath} uses ${name} (${matches.length} times)`)
    }
  }
}

// Check 4: Validate build output (if exists)
async function validateBuildOutput() {
  info('Checking build output...')

  const outputPath = path.join(ROOT_DIR, '.output', 'public')

  try {
    await fs.access(outputPath)

    // Check for static files
    const entries = await fs.readdir(outputPath)

    const hasIndexHtml = entries.includes('index.html')
    const hasNuxtDir = entries.some(entry => entry.startsWith('_nuxt'))

    if (hasIndexHtml && hasNuxtDir) {
      success('Build output contains static files (index.html, _nuxt/*)')
    } else {
      warning('Build output structure is unexpected')
    }

    // Check for server directory in output
    const serverOutputPath = path.join(ROOT_DIR, '.output', 'server')
    try {
      await fs.access(serverOutputPath)
      warning('.output/server exists - verify it\'s not being deployed')
    } catch {
      success('No server output directory found')
    }
  } catch {
    info('No build output found (run `npm run build` to generate)')
  }
}

// Main validation function
async function main() {
  console.log('🔍 Validating SPA Build Configuration...\n')

  await validateNuxtConfig()
  await validateNoServerDirectory()
  await validateNoServerApiUsage()
  await validateBuildOutput()

  console.log('\n' + '='.repeat(60))

  if (hasErrors) {
    console.error('\n❌ Validation FAILED - please fix the errors above')
    process.exit(1)
  } else if (hasWarnings) {
    console.warn('\n⚠️  Validation PASSED with warnings - please review')
    process.exit(0)
  } else {
    console.log('\n✅ Validation PASSED - SPA configuration is correct!')
    process.exit(0)
  }
}

main().catch((err) => {
  console.error('Validation script failed:', err)
  process.exit(1)
})
