<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="handleOverlayClick">
        <div :class="modalClass" role="dialog" aria-modal="true">
          <!-- Header -->
          <div v-if="$slots.header || title" class="modal-header">
            <slot name="header">
              <h3 class="tw-text-lg tw-font-semibold tw-text-gray-900">
                {{ title }}
              </h3>
            </slot>
            <button
              v-if="closable"
              type="button"
              class="tw-text-gray-400 hover:tw-text-gray-600 tw-transition-colors"
              @click="handleClose"
            >
              <i class="pi pi-times tw-text-xl" />
            </button>
          </div>

          <!-- Body -->
          <div class="modal-body">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'

interface Props {
  visible?: boolean
  title?: string
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full'
  closable?: boolean
  closeOnOverlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  size: 'md',
  closable: true,
  closeOnOverlay: true,
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  close: []
}>()

const modalClass = computed(() => {
  const baseClass = 'modal-content tw-bg-white tw-rounded-lg tw-shadow-2xl tw-max-h-[90vh] tw-flex tw-flex-col'
  
  const sizeClass = {
    sm: 'tw-max-w-sm',
    md: 'tw-max-w-md',
    lg: 'tw-max-w-lg',
    xl: 'tw-max-w-xl',
    full: 'tw-max-w-full tw-m-4',
  }[props.size]
  
  return `${baseClass} ${sizeClass}`
})

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose()
  }
}

// Lock body scroll when modal is open
watch(() => props.visible, (visible) => {
  if (visible) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
.modal-overlay {
  @apply tw-fixed tw-inset-0 tw-bg-black tw-bg-opacity-50 tw-flex tw-items-center tw-justify-center tw-z-50 tw-p-4;
}

.modal-content {
  @apply tw-relative tw-w-full;
}

.modal-header {
  @apply tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-4 tw-border-b tw-border-gray-200;
}

.modal-body {
  @apply tw-px-6 tw-py-4 tw-overflow-y-auto;
}

.modal-footer {
  @apply tw-px-6 tw-py-4 tw-border-t tw-border-gray-200 tw-flex tw-justify-end tw-gap-2;
}

/* Modal transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
}
</style>
