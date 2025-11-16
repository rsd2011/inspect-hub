<template>
  <div class="tw-flex tw-items-center tw-mb-3">
    <Checkbox
      :id="`method-${method}`"
      v-model="isEnabled"
      :binary="true"
      :disabled="isLastMethod"
      @update:model-value="handleToggle"
    />
    <label
      :for="`method-${method}`"
      class="tw-ml-2 tw-cursor-pointer"
      :class="{ 'tw-text-gray-400': isLastMethod }"
    >
      {{ methodLabel }}
    </label>
    <span
      v-if="isLastMethod"
      class="tw-ml-2 tw-text-xs tw-text-gray-500"
    >
      (최소 1개 필요)
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Checkbox from 'primevue/checkbox'
import type { LoginMethod } from '~/types/models'

interface Props {
  method: LoginMethod
  enabled: boolean
  isLastMethod: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  toggle: [method: LoginMethod]
}>()

const isEnabled = computed({
  get: () => props.enabled,
  set: () => {} // handled by handleToggle
})

const methodLabel = computed(() => {
  const labels: Record<LoginMethod, string> = {
    SSO: 'SSO (Single Sign-On)',
    AD: 'AD (Active Directory)',
    LOCAL: '일반 로그인 (Database)'
  }
  return labels[props.method]
})

const handleToggle = () => {
  if (!props.isLastMethod) {
    emit('toggle', props.method)
  }
}
</script>
