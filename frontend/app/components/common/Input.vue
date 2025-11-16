<template>
  <div class="tw-w-full">
    <label v-if="label" :for="inputId" class="tw-block tw-text-sm tw-font-medium tw-text-gray-700 tw-mb-1">
      {{ label }}
      <span v-if="required" class="tw-text-red-500">*</span>
    </label>
    <input
      :id="inputId"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :required="required"
      :class="inputClass"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
    >
    <p v-if="error" class="tw-mt-1 tw-text-sm tw-text-red-600">
      {{ error }}
    </p>
    <p v-else-if="hint" class="tw-mt-1 tw-text-sm tw-text-gray-500">
      {{ hint }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  label?: string
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url'
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  error?: string
  hint?: string
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  size: 'md',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const inputId = computed(() => `input-${Math.random().toString(36).substring(7)}`)

const inputClass = computed(() => {
  const baseClass = 'tw-block tw-w-full tw-rounded-md tw-border tw-shadow-sm focus:tw-outline-none focus:tw-ring-2 tw-transition-colors'
  
  const sizeClass = {
    sm: 'tw-px-2 tw-py-1 tw-text-sm',
    md: 'tw-px-3 tw-py-2 tw-text-base',
    lg: 'tw-px-4 tw-py-3 tw-text-lg',
  }[props.size]
  
  const stateClass = props.error
    ? 'tw-border-red-300 focus:tw-border-red-500 focus:tw-ring-red-500'
    : 'tw-border-gray-300 focus:tw-border-blue-500 focus:tw-ring-blue-500'
  
  const disabledClass = (props.disabled || props.readonly)
    ? 'tw-bg-gray-100 tw-cursor-not-allowed tw-text-gray-500'
    : 'tw-bg-white'
  
  return `${baseClass} ${sizeClass} ${stateClass} ${disabledClass}`
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}
</script>
