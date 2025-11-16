<template>
  <div class="password-input tw-relative">
    <Input
      :model-value="modelValue"
      :label="label"
      :type="isVisible ? 'text' : 'password'"
      :placeholder="placeholder"
      :error="error"
      :required="required"
      :disabled="disabled"
      @update:model-value="$emit('update:modelValue', $event)"
      @blur="$emit('blur', $event)"
      @focus="$emit('focus', $event)"
    />
    <button
      type="button"
      class="tw-absolute tw-right-3 tw-top-9 tw-text-gray-400 hover:tw-text-gray-600"
      @click="toggleVisibility"
    >
      <i :class="isVisible ? 'pi pi-eye-slash' : 'pi pi-eye'" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  modelValue?: string
  label?: string
  placeholder?: string
  error?: string
  required?: boolean
  disabled?: boolean
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const isVisible = ref(false)

const toggleVisibility = () => {
  isVisible.value = !isVisible.value
}
</script>
