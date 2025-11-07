<template>
  <label
    :for="for"
    :class="computedClass"
  >
    <slot>{{ text }}</slot>
    <span
      v-if="required"
      class="tw-text-red-600 tw-ml-1"
      aria-label="필수 항목"
    >*</span>
  </label>
</template>

<script setup lang="ts">
/**
 * Label Atom Component
 *
 * A simple label component with consistent styling and required indicator.
 * Uses Tailwind prefix (tw-) to avoid conflicts with PrimeVue/RealGrid.
 *
 * @example
 * ```vue
 * <Label for="username" text="사용자명" required />
 * <Label for="email">이메일</Label>
 * <Label for="phone" text="전화번호" class="tw-font-bold" />
 * ```
 */

import type { PropType } from 'vue'

export type LabelSize = 'small' | 'medium' | 'large'

const props = defineProps({
  /**
   * ID of the form element this label is for
   */
  for: {
    type: String,
    default: undefined,
  },

  /**
   * Label text content
   */
  text: {
    type: String,
    default: undefined,
  },

  /**
   * Show required indicator (*)
   */
  required: {
    type: Boolean,
    default: false,
  },

  /**
   * Label size
   */
  size: {
    type: String as PropType<LabelSize>,
    default: 'medium',
  },

  /**
   * Additional CSS classes
   */
  class: {
    type: [String, Object, Array],
    default: undefined,
  },
})

const computedClass = computed(() => {
  const classes = ['tw-inline-block', 'tw-text-gray-700']

  // Size classes
  switch (props.size) {
    case 'small':
      classes.push('tw-text-sm')
      break
    case 'large':
      classes.push('tw-text-lg', 'tw-font-medium')
      break
    default:
      classes.push('tw-text-base')
  }

  // Add custom class prop
  if (props.class) {
    if (typeof props.class === 'string') {
      classes.push(props.class)
    } else if (Array.isArray(props.class)) {
      classes.push(...props.class)
    } else {
      classes.push(props.class)
    }
  }

  return classes
})
</script>

<style scoped>
/* Additional label styles if needed */
/* Use tw- prefix for Tailwind classes to avoid conflicts */
</style>
