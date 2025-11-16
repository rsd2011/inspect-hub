<template>
  <div class="tw-container tw-mx-auto tw-p-6">
    <Toast />

    <!-- Header -->
    <div class="tw-mb-6">
      <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">시스템 설정</h1>
      <p class="tw-text-gray-600 tw-mt-2">로그인 정책</p>
    </div>

    <!-- Loading State -->
    <div v-if="policyStore.loading" class="tw-text-center tw-py-12">
      <ProgressSpinner />
      <p class="tw-mt-4 tw-text-gray-600">로딩 중...</p>
    </div>

    <!-- Error State -->
    <Message v-else-if="policyStore.error" severity="error" :closable="false" class="tw-mb-4">
      {{ policyStore.error }}
    </Message>

    <!-- Main Content -->
    <Card v-else-if="policyStore.policy">
      <template #content>
        <div class="tw-space-y-6">
          <!-- Policy Name -->
          <div>
            <label class="tw-block tw-text-sm tw-font-semibold tw-mb-2">정책 이름</label>
            <InputText
              v-model="localPolicy.name"
              class="tw-w-full"
              placeholder="정책 이름을 입력하세요"
            />
          </div>

          <!-- Enabled Methods -->
          <div>
            <h3 class="tw-text-lg tw-font-semibold tw-mb-3">활성화된 로그인 방식</h3>
            <LoginMethodCheckbox
              v-for="method in allMethods"
              :key="method"
              :method="method"
              :enabled="isMethodEnabled(method)"
              :is-last-method="isLastMethod && isMethodEnabled(method)"
              @toggle="handleToggleMethod"
            />
          </div>

          <!-- Priority Order -->
          <PriorityOrderList
            :priority="localPolicy.priority"
            @update:priority="handlePriorityUpdate"
          />

          <!-- Preview -->
          <LoginPagePreview :priority="localPolicy.priority" />

          <!-- Actions -->
          <div class="tw-flex tw-justify-end tw-gap-3 tw-pt-4 tw-border-t">
            <Button
              label="취소"
              severity="secondary"
              :disabled="isSaving"
              @click="handleCancel"
            />
            <Button
              label="저장"
              severity="primary"
              :loading="isSaving"
              :disabled="!hasChanges"
              @click="handleSave"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- No Data -->
    <Message v-else severity="warn" :closable="false">
      로그인 정책을 찾을 수 없습니다
    </Message>
  </div>
</template>

<script setup lang="ts">
// Middleware
definePageMeta({
  middleware: ['admin']
})

import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import Card from 'primevue/card'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import Toast from 'primevue/toast'
import { useLoginPolicyStore } from '~/stores/loginPolicyStore'
import LoginMethodCheckbox from '~/components/admin/login-policy/LoginMethodCheckbox.vue'
import PriorityOrderList from '~/components/admin/login-policy/PriorityOrderList.vue'
import LoginPagePreview from '~/components/admin/login-policy/LoginPagePreview.vue'
import type { LoginMethod, UpdateLoginPolicyRequest } from '~/types/models'

// Composables
const policyStore = useLoginPolicyStore()
const toast = useToast()

// Local State
const allMethods: LoginMethod[] = ['SSO', 'AD', 'LOCAL']
const isSaving = ref(false)

const localPolicy = ref({
  name: '',
  enabledMethods: [] as LoginMethod[],
  priority: [] as LoginMethod[]
})

// Computed
const isLastMethod = computed(() => {
  return localPolicy.value.enabledMethods.length === 1
})

const hasChanges = computed(() => {
  if (!policyStore.policy) return false

  return (
    localPolicy.value.name !== policyStore.policy.name ||
    JSON.stringify(localPolicy.value.enabledMethods) !== JSON.stringify(policyStore.policy.enabledMethods) ||
    JSON.stringify(localPolicy.value.priority) !== JSON.stringify(policyStore.policy.priority)
  )
})

// Methods
const isMethodEnabled = (method: LoginMethod): boolean => {
  return localPolicy.value.enabledMethods.includes(method)
}

const handleToggleMethod = (method: LoginMethod) => {
  const enabledSet = new Set(localPolicy.value.enabledMethods)

  if (enabledSet.has(method)) {
    // 마지막 남은 방식은 비활성화할 수 없음
    if (enabledSet.size === 1) {
      toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '최소 하나의 로그인 방식은 활성화되어야 합니다',
        life: 3000
      })
      return
    }
    enabledSet.delete(method)
    // Priority에서도 제거
    localPolicy.value.priority = localPolicy.value.priority.filter(m => m !== method)
  } else {
    enabledSet.add(method)
    // Priority에 추가 (맨 뒤)
    localPolicy.value.priority.push(method)
  }

  localPolicy.value.enabledMethods = Array.from(enabledSet)
}

const handlePriorityUpdate = (newPriority: LoginMethod[]) => {
  localPolicy.value.priority = newPriority
}

const handleCancel = () => {
  // Reset to original policy
  if (policyStore.policy) {
    localPolicy.value = {
      name: policyStore.policy.name,
      enabledMethods: [...policyStore.policy.enabledMethods],
      priority: [...policyStore.policy.priority]
    }
  }

  toast.add({
    severity: 'info',
    summary: '취소',
    detail: '변경사항이 취소되었습니다',
    life: 3000
  })
}

const handleSave = async () => {
  isSaving.value = true

  try {
    const request: UpdateLoginPolicyRequest = {
      name: localPolicy.value.name,
      enabledMethods: localPolicy.value.enabledMethods,
      priority: localPolicy.value.priority
    }

    const success = await policyStore.updatePolicy(request)

    if (success) {
      toast.add({
        severity: 'success',
        summary: '성공',
        detail: '로그인 정책이 업데이트되었습니다',
        life: 3000
      })
    } else {
      toast.add({
        severity: 'error',
        summary: '오류',
        detail: policyStore.error || '정책 업데이트에 실패했습니다',
        life: 5000
      })
    }
  } catch (error) {
    console.error('Save error:', error)
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '정책 저장 중 오류가 발생했습니다',
      life: 5000
    })
  } finally {
    isSaving.value = false
  }
}

// Lifecycle
onMounted(async () => {
  await policyStore.fetchPolicy()

  if (policyStore.policy) {
    localPolicy.value = {
      name: policyStore.policy.name,
      enabledMethods: [...policyStore.policy.enabledMethods],
      priority: [...policyStore.policy.priority]
    }
  }
})
</script>
