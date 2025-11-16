<template>
  <div class="login-form">
    <h2 class="tw-text-2xl tw-font-bold tw-text-center tw-mb-6">{{ $t('auth.login') }}</h2>

    <form @submit.prevent="handleSubmit">
      <!-- Employee ID -->
      <Input
        v-model="credentials.employeeId"
        :label="$t('auth.employeeId')"
        type="text"
        :placeholder="$t('auth.employeeIdPlaceholder')"
        :error="errors.employeeId"
        required
        @blur="validateEmployeeId"
      />

      <!-- Password -->
      <div class="tw-mt-4">
        <Input
          v-model="credentials.password"
          :label="$t('auth.password')"
          type="password"
          :placeholder="$t('auth.passwordPlaceholder')"
          :error="errors.password"
          required
          @blur="validatePassword"
        />
      </div>

      <!-- Error Message -->
      <div v-if="loginError" class="tw-mt-4 tw-p-3 tw-bg-red-50 tw-border tw-border-red-200 tw-rounded-md">
        <p class="tw-text-sm tw-text-red-600">{{ loginError }}</p>
      </div>

      <!-- Submit Button -->
      <div class="tw-mt-6">
        <Button
          type="submit"
          variant="primary"
          size="lg"
          :loading="loading"
          :disabled="!isFormValid"
          full-width
        >
          {{ $t('auth.loginButton') }}
        </Button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  method?: 'LOCAL' | 'AD' | 'SSO'
}

const props = withDefaults(defineProps<Props>(), {
  method: 'LOCAL',
})

const { loginWithCredentials, loading, error: loginError } = useLogin()

const credentials = ref({
  employeeId: '',
  password: '',
})

const errors = ref({
  employeeId: '',
  password: '',
})

const validateEmployeeId = () => {
  if (!credentials.value.employeeId) {
    errors.value.employeeId = 'Employee ID is required'
  } else if (credentials.value.employeeId.length < 3) {
    errors.value.employeeId = 'Employee ID must be at least 3 characters'
  } else {
    errors.value.employeeId = ''
  }
}

const validatePassword = () => {
  if (!credentials.value.password) {
    errors.value.password = 'Password is required'
  } else if (credentials.value.password.length < 4) {
    errors.value.password = 'Password must be at least 4 characters'
  } else {
    errors.value.password = ''
  }
}

const isFormValid = computed(() => {
  return (
    credentials.value.employeeId.length >= 3 &&
    credentials.value.password.length >= 4 &&
    !errors.value.employeeId &&
    !errors.value.password
  )
})

const handleSubmit = async () => {
  validateEmployeeId()
  validatePassword()

  if (!isFormValid.value) return

  await loginWithCredentials(credentials.value, props.method)
}
</script>
