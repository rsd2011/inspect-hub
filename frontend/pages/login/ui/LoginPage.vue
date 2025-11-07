<template>
  <div class="tw-min-h-screen tw-flex tw-items-center tw-justify-center tw-bg-gray-100">
    <Card class="tw-w-full tw-max-w-md tw-shadow-lg">
      <template #header>
        <div class="tw-text-center tw-py-4">
          <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Inspect-Hub</h1>
          <p class="tw-text-sm tw-text-gray-600 tw-mt-2">AML 통합 컴플라이언스 모니터링 시스템</p>
        </div>
      </template>

      <template #content>
        <form class="tw-space-y-4" @submit.prevent="handleLogin">
          <!-- Username Field -->
          <div class="tw-space-y-2">
            <label for="username" class="tw-block tw-text-sm tw-font-medium tw-text-gray-700">
              사용자명
            </label>
            <InputText
              id="username"
              v-model="formData.username"
              placeholder="사용자명을 입력하세요"
              class="tw-w-full"
              :class="{ 'p-invalid': errors.username }"
              autofocus
            />
            <small v-if="errors.username" class="p-error">{{ errors.username }}</small>
          </div>

          <!-- Password Field -->
          <div class="tw-space-y-2">
            <label for="password" class="tw-block tw-text-sm tw-font-medium tw-text-gray-700">
              비밀번호
            </label>
            <Password
              id="password"
              v-model="formData.password"
              placeholder="비밀번호를 입력하세요"
              :feedback="false"
              toggle-mask
              class="tw-w-full"
              :class="{ 'p-invalid': errors.password }"
              input-class="tw-w-full"
            />
            <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
          </div>

          <!-- Error Message -->
          <Message v-if="errorMessage" severity="error" :closable="false">
            {{ errorMessage }}
          </Message>

          <!-- Login Button -->
          <Button
            type="submit"
            label="로그인"
            icon="pi pi-sign-in"
            class="tw-w-full"
            :loading="isLoading"
          />

          <!-- Demo Info -->
          <div class="tw-mt-4 tw-p-3 tw-bg-blue-50 tw-rounded-md tw-text-sm tw-text-blue-800">
            <p class="tw-font-semibold">데모 계정:</p>
            <p>사용자명: admin</p>
            <p>비밀번호: (임의의 비밀번호)</p>
          </div>
        </form>
      </template>

      <template #footer>
        <div class="tw-text-center tw-text-xs tw-text-gray-500">
          &copy; 2025 Inspect-Hub. All rights reserved.
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/features/auth/model/auth.store'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'

definePageMeta({
  layout: false,
})

const authStore = useAuthStore()
const router = useRouter()

// Form state
const formData = reactive({
  username: '',
  password: '',
})

const errors = reactive({
  username: '',
  password: '',
})

const isLoading = ref(false)
const errorMessage = ref('')

// Validation
function validate(): boolean {
  errors.username = ''
  errors.password = ''
  errorMessage.value = ''

  let isValid = true

  if (!formData.username) {
    errors.username = '사용자명을 입력해주세요'
    isValid = false
  }

  if (!formData.password) {
    errors.password = '비밀번호를 입력해주세요'
    isValid = false
  }

  return isValid
}

// Login handler
async function handleLogin() {
  if (!validate()) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // For SSO, login will redirect to SSO provider
    // In our mock implementation, it logs in directly
    await authStore.login({
      redirectUrl: '/',
    })

    // Login successful, redirect to home
    await router.push('/')
  } catch (error: unknown) {
    console.error('Login error:', error)
    errorMessage.value = (error as { data?: { message?: string } }).data?.message || '로그인에 실패했습니다. SSO 인증에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// Check if already logged in
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/')
  }
})
</script>

<style scoped>
/* Additional custom styles if needed */
</style>
