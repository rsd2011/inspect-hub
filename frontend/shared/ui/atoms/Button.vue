<template>
  <PrimeButton
    :type="type"
    :label="label"
    :icon="icon"
    :icon-pos="iconPos"
    :loading="loading"
    :disabled="disabled"
    :severity="severity"
    :outlined="outlined"
    :text="text"
    :raised="raised"
    :rounded="rounded"
    :size="size"
    :class="computedClass"
    @click="handleClick"
  >
    <slot />
  </PrimeButton>
</template>

<script setup lang="ts">
/**
 * Button Atom Component
 *
 * A wrapper around PrimeVue Button with consistent styling and behavior.
 * Uses Tailwind prefix (tw-) to avoid conflicts with PrimeVue/RealGrid.
 *
 * @example
 * ```vue
 * <Button label="저장" severity="success" icon="pi pi-check" @click="onSave" />
 * <Button outlined>취소</Button>
 * <Button severity="danger" :loading="isDeleting" @click="onDelete">삭제</Button>
 * ```
 */

import type { PropType } from 'vue'

export type ButtonSeverity = 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'danger' | 'help' | 'contrast'
export type ButtonSize = 'small' | 'large'
export type ButtonIconPos = 'left' | 'right' | 'top' | 'bottom'

const props = defineProps({
  /**
   * Button type attribute
   */
  type: {
    type: String as PropType<'button' | 'submit' | 'reset'>,
    default: 'button',
  },

  /**
   * Text content of the button
   */
  label: {
    type: String,
    default: undefined,
  },

  /**
   * Icon class (PrimeIcons)
   */
  icon: {
    type: String,
    default: undefined,
  },

  /**
   * Icon position
   */
  iconPos: {
    type: String as PropType<ButtonIconPos>,
    default: 'left',
  },

  /**
   * Loading state
   */
  loading: {
    type: Boolean,
    default: false,
  },

  /**
   * Disabled state
   */
  disabled: {
    type: Boolean,
    default: false,
  },

  /**
   * Button severity/variant
   */
  severity: {
    type: String as PropType<ButtonSeverity>,
    default: 'primary',
  },

  /**
   * Outlined style
   */
  outlined: {
    type: Boolean,
    default: false,
  },

  /**
   * Text-only style
   */
  text: {
    type: Boolean,
    default: false,
  },

  /**
   * Raised style (with shadow)
   */
  raised: {
    type: Boolean,
    default: false,
  },

  /**
   * Rounded style
   */
  rounded: {
    type: Boolean,
    default: false,
  },

  /**
   * Button size
   */
  size: {
    type: String as PropType<ButtonSize>,
    default: undefined,
  },

  /**
   * Additional CSS classes
   */
  class: {
    type: [String, Object, Array],
    default: undefined,
  },
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const computedClass = computed(() => {
  const classes = []

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

function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
/* Additional button styles if needed */
/* Use tw- prefix for Tailwind classes to avoid conflicts */
</style>
