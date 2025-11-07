<template>
  <span class="tw-relative tw-inline-block tw-w-full">
    <PrimeInputText
      :id="id"
      v-model="internalValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :invalid="invalid || !!errorMessage"
      :size="size"
      :class="computedClass"
      :autocomplete="autocomplete"
      :maxlength="maxlength"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
      @keydown.enter="handleEnter"
    />
    <small
      v-if="errorMessage"
      :id="`${id}-error`"
      class="tw-block tw-mt-1 tw-text-red-600 tw-text-sm"
    >
      {{ errorMessage }}
    </small>
    <small
      v-else-if="hint"
      :id="`${id}-hint`"
      class="tw-block tw-mt-1 tw-text-gray-500 tw-text-sm"
    >
      {{ hint }}
    </small>
  </span>
</template>

<script setup lang="ts">
/**
 * Input Atom Component
 *
 * A wrapper around PrimeVue InputText with validation support.
 * Uses Tailwind prefix (tw-) to avoid conflicts with PrimeVue/RealGrid.
 *
 * @example
 * ```vue
 * <Input v-model="username" placeholder="사용자명 입력" />
 * <Input v-model="email" type="email" :error-message="emailError" />
 * <Input v-model="password" type="password" autocomplete="current-password" />
 * ```
 */

import type { PropType } from 'vue'

export type InputType = 'text' | 'password' | 'email' | 'number' | 'tel' | 'url' | 'search'
export type InputSize = 'small' | 'large'

const props = defineProps({
  /**
   * Input ID (for accessibility)
   */
  id: {
    type: String,
    default: () => `input-${Math.random().toString(36).substring(2, 9)}`,
  },

  /**
   * v-model binding
   */
  modelValue: {
    type: [String, Number],
    default: '',
  },

  /**
   * Input type
   */
  type: {
    type: String as PropType<InputType>,
    default: 'text',
  },

  /**
   * Placeholder text
   */
  placeholder: {
    type: String,
    default: undefined,
  },

  /**
   * Disabled state
   */
  disabled: {
    type: Boolean,
    default: false,
  },

  /**
   * Readonly state
   */
  readonly: {
    type: Boolean,
    default: false,
  },

  /**
   * Invalid state (visual indicator)
   */
  invalid: {
    type: Boolean,
    default: false,
  },

  /**
   * Error message to display
   */
  errorMessage: {
    type: String,
    default: undefined,
  },

  /**
   * Hint text (shown when no error)
   */
  hint: {
    type: String,
    default: undefined,
  },

  /**
   * Input size
   */
  size: {
    type: String as PropType<InputSize>,
    default: undefined,
  },

  /**
   * Autocomplete attribute
   */
  autocomplete: {
    type: String,
    default: undefined,
  },

  /**
   * Maximum character length
   */
  maxlength: {
    type: Number,
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
  'update:modelValue': [value: string | number]
  input: [event: Event]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
  enter: [event: KeyboardEvent]
}>()

const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const computedClass = computed(() => {
  const classes = ['tw-w-full']

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

function handleInput(event: Event) {
  emit('input', event)
}

function handleBlur(event: FocusEvent) {
  emit('blur', event)
}

function handleFocus(event: FocusEvent) {
  emit('focus', event)
}

function handleEnter(event: KeyboardEvent) {
  emit('enter', event)
}
</script>

<style scoped>
/* Additional input styles if needed */
/* Use tw- prefix for Tailwind classes to avoid conflicts */
</style>
