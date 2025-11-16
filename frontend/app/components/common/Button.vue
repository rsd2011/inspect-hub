<template>
  <button
    :type="type"
    :class="buttonClass"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="tw-mr-2">
      <i class="pi pi-spinner pi-spin" />
    </span>
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'success' | 'outline'
  size?: 'sm' | 'md' | 'lg'
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  loading?: boolean
  fullWidth?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  disabled: false,
  loading: false,
  fullWidth: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClass = computed(() => {
  const baseClass = 'tw-inline-flex tw-items-center tw-justify-center tw-font-medium tw-rounded-md tw-transition-colors tw-duration-200 focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-offset-2'
  
  const variantClass = {
    primary: 'tw-bg-blue-600 tw-text-white hover:tw-bg-blue-700 focus:tw-ring-blue-500 disabled:tw-bg-blue-300',
    secondary: 'tw-bg-gray-600 tw-text-white hover:tw-bg-gray-700 focus:tw-ring-gray-500 disabled:tw-bg-gray-300',
    danger: 'tw-bg-red-600 tw-text-white hover:tw-bg-red-700 focus:tw-ring-red-500 disabled:tw-bg-red-300',
    success: 'tw-bg-green-600 tw-text-white hover:tw-bg-green-700 focus:tw-ring-green-500 disabled:tw-bg-green-300',
    outline: 'tw-border-2 tw-border-blue-600 tw-text-blue-600 hover:tw-bg-blue-50 focus:tw-ring-blue-500 disabled:tw-border-blue-300 disabled:tw-text-blue-300',
  }[props.variant]
  
  const sizeClass = {
    sm: 'tw-px-3 tw-py-1.5 tw-text-sm',
    md: 'tw-px-4 tw-py-2 tw-text-base',
    lg: 'tw-px-6 tw-py-3 tw-text-lg',
  }[props.size]
  
  const widthClass = props.fullWidth ? 'tw-w-full' : ''
  const disabledClass = (props.disabled || props.loading) ? 'tw-cursor-not-allowed tw-opacity-50' : 'tw-cursor-pointer'
  
  return `${baseClass} ${variantClass} ${sizeClass} ${widthClass} ${disabledClass}`
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>
