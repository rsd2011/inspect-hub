<template>
  <div class="tw-mt-6">
    <h3 class="tw-text-lg tw-font-semibold tw-mb-3">우선순위 (드래그하여 순서 변경)</h3>
    <OrderList
      v-model="localPriority"
      data-key="method"
      @update:model-value="handleReorder"
    >
      <template #item="{ item, index }">
        <div class="tw-flex tw-items-center tw-p-3">
          <span class="tw-font-bold tw-mr-3">{{ index + 1 }}.</span>
          <span>{{ getMethodLabel(item) }}</span>
        </div>
      </template>
    </OrderList>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import OrderList from 'primevue/orderlist'
import type { LoginMethod } from '~/types/models'

interface Props {
  priority: LoginMethod[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:priority': [priority: LoginMethod[]]
}>()

const localPriority = ref<LoginMethod[]>([...props.priority])

watch(
  () => props.priority,
  (newPriority) => {
    localPriority.value = [...newPriority]
  },
  { deep: true }
)

const handleReorder = (newOrder: LoginMethod[]) => {
  emit('update:priority', newOrder)
}

const getMethodLabel = (method: LoginMethod): string => {
  const labels: Record<LoginMethod, string> = {
    SSO: 'SSO (Single Sign-On)',
    AD: 'AD (Active Directory)',
    LOCAL: '일반 로그인 (Database)'
  }
  return labels[method]
}
</script>
