#!/usr/bin/env node

/**
 * Component Generator for FSD Architecture
 *
 * Usage:
 *   npm run generate:component -- --layer=shared --type=atoms --name=Badge
 *   npm run generate:component -- --layer=features --name=case-filter
 *   npm run generate:component -- --layer=widgets --name=case-table
 *
 * Generates:
 *   - Vue component file with TypeScript
 *   - Type definitions
 *   - Test file
 *   - Story file (optional)
 */

import fs from 'fs/promises'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const ROOT_DIR = path.resolve(__dirname, '..')

// Parse command line arguments
function parseArgs() {
  const args = process.argv.slice(2)
  const options = {}

  for (const arg of args) {
    if (arg.startsWith('--')) {
      const [key, value] = arg.slice(2).split('=')
      options[key] = value
    }
  }

  return options
}

// Validate layer
function validateLayer(layer) {
  const validLayers = ['shared', 'entities', 'features', 'widgets', 'pages']
  if (!validLayers.includes(layer)) {
    console.error(`❌ Invalid layer: ${layer}`)
    console.error(`   Valid layers: ${validLayers.join(', ')}`)
    process.exit(1)
  }
}

// Validate type for shared layer
function validateSharedType(type) {
  const validTypes = ['atoms', 'molecules', 'organisms']
  if (!validTypes.includes(type)) {
    console.error(`❌ Invalid type for shared layer: ${type}`)
    console.error(`   Valid types: ${validTypes.join(', ')}`)
    process.exit(1)
  }
}

// Convert kebab-case to PascalCase
function toPascalCase(str) {
  return str
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join('')
}

// Convert kebab-case to camelCase
function toCamelCase(str) {
  const pascal = toPascalCase(str)
  return pascal.charAt(0).toLowerCase() + pascal.slice(1)
}

// Generate Vue component template
function generateComponent(name, layer, type) {
  const pascalName = toPascalCase(name)
  const camelName = toCamelCase(name)

  return `<script setup lang="ts">
/**
 * ${pascalName} Component
 *
 * Layer: ${layer}${type ? ` / ${type}` : ''}
 */
import type { ${pascalName}Props } from './types'

const props = defineProps<${pascalName}Props>()

const emit = defineEmits<{
  // Define your emits here
  // Example: click: [id: string]
}>()

// Component logic here
</script>

<template>
  <div class="tw-${camelName}">
    <slot />
  </div>
</template>

<style scoped>
/* Component styles here */
.tw-${camelName} {
  /* Styles */
}
</style>
`
}

// Generate types file
function generateTypes(name) {
  const pascalName = toPascalCase(name)

  return `/**
 * ${pascalName} Types
 */

export interface ${pascalName}Props {
  // Define your props here
  // Example: title?: string
}

export interface ${pascalName}Emits {
  // Define your emits here
  // Example: (event: 'click', id: string): void
}
`
}

// Generate test file
function generateTest(name, layer, type) {
  const pascalName = toPascalCase(name)
  const componentPath = type
    ? `@/${layer}/ui/${type}/${pascalName}.vue`
    : `@/${layer}/${name}/ui/${pascalName}.vue`

  return `import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ${pascalName} from '${componentPath}'

describe('${pascalName}', () => {
  it('renders properly', () => {
    const wrapper = mount(${pascalName}, {
      props: {
        // Add required props
      },
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('emits events correctly', async () => {
    const wrapper = mount(${pascalName})

    // Test emit logic
    // await wrapper.find('button').trigger('click')
    // expect(wrapper.emitted()).toHaveProperty('click')
  })
})
`
}

// Generate index file
function generateIndex(name) {
  const pascalName = toPascalCase(name)

  return `export { default as ${pascalName} } from './${pascalName}.vue'
export type { ${pascalName}Props, ${pascalName}Emits } from './types'
`
}

// Generate README file
function generateReadme(name, layer, type) {
  const pascalName = toPascalCase(name)

  return `# ${pascalName}

**Layer:** ${layer}${type ? ` / ${type}` : ''}

## Usage

\`\`\`vue
<script setup lang="ts">
import ${pascalName} from '@/${layer}${type ? `/ui/${type}` : `/${name}/ui`}/${pascalName}.vue'
</script>

<template>
  <${pascalName}>
    Content here
  </${pascalName}>
</template>
\`\`\`

## Props

| Name | Type | Default | Description |
|------|------|---------|-------------|
| -    | -    | -       | -           |

## Events

| Name | Payload | Description |
|------|---------|-------------|
| -    | -       | -           |

## Slots

| Name    | Description |
|---------|-------------|
| default | Main content |
`
}

// Main function
async function main() {
  const options = parseArgs()

  // Validate required options
  if (!options.layer || !options.name) {
    console.error('❌ Missing required options')
    console.error('')
    console.error('Usage:')
    console.error('  npm run generate:component -- --layer=shared --type=atoms --name=Badge')
    console.error('  npm run generate:component -- --layer=features --name=case-filter')
    console.error('  npm run generate:component -- --layer=widgets --name=case-table')
    console.error('')
    console.error('Options:')
    console.error('  --layer   Layer name (shared, entities, features, widgets, pages)')
    console.error('  --name    Component name in kebab-case')
    console.error('  --type    Type for shared layer (atoms, molecules, organisms)')
    process.exit(1)
  }

  const { layer, name, type } = options

  // Validate inputs
  validateLayer(layer)

  if (layer === 'shared' && !type) {
    console.error('❌ --type is required for shared layer')
    console.error('   Valid types: atoms, molecules, organisms')
    process.exit(1)
  }

  if (layer === 'shared') {
    validateSharedType(type)
  }

  const pascalName = toPascalCase(name)

  // Determine component directory
  let componentDir
  if (layer === 'shared') {
    componentDir = path.join(ROOT_DIR, layer, 'ui', type)
  } else {
    componentDir = path.join(ROOT_DIR, layer, name, 'ui')
  }

  // Create directory
  await fs.mkdir(componentDir, { recursive: true })

  // Generate files
  const files = [
    {
      name: `${pascalName}.vue`,
      content: generateComponent(name, layer, type),
    },
    {
      name: 'types.ts',
      content: generateTypes(name),
    },
    {
      name: `${pascalName}.spec.ts`,
      content: generateTest(name, layer, type),
    },
    {
      name: 'index.ts',
      content: generateIndex(name),
    },
    {
      name: 'README.md',
      content: generateReadme(name, layer, type),
    },
  ]

  // Write files
  for (const file of files) {
    const filePath = path.join(componentDir, file.name)
    await fs.writeFile(filePath, file.content, 'utf-8')
    console.log(`✅ Created: ${path.relative(ROOT_DIR, filePath)}`)
  }

  console.log('')
  console.log(`🎉 Component ${pascalName} created successfully!`)
  console.log('')
  console.log('Next steps:')
  console.log(`  1. Edit ${componentDir}/${pascalName}.vue`)
  console.log(`  2. Define types in ${componentDir}/types.ts`)
  console.log(`  3. Write tests in ${componentDir}/${pascalName}.spec.ts`)
  console.log(`  4. Run tests: npm test ${pascalName}`)
}

main().catch(console.error)
