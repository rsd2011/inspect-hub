<template>
  <div class="login-page">
    <NuxtLayout name="auth">
      <div class="tw-space-y-6">
        <!-- SSO Login (if enabled) -->
        <SsoLoginButton v-if="shouldShowSsoButton" />

        <!-- Divider -->
        <div v-if="shouldShowSsoButton && (shouldShowAdLogin || shouldShowLocalLogin)" class="tw-relative">
          <div class="tw-absolute tw-inset-0 tw-flex tw-items-center">
            <div class="tw-w-full tw-border-t tw-border-gray-300" />
          </div>
          <div class="tw-relative tw-flex tw-justify-center tw-text-sm">
            <span class="tw-px-2 tw-bg-white tw-text-gray-500">{{ $t('auth.orLoginWith') }}</span>
          </div>
        </div>

        <!-- Login Form -->
        <LoginForm v-if="shouldShowLocalLogin || shouldShowAdLogin" :method="preferredMethod" />
      </div>
    </NuxtLayout>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

definePageMeta({
  layout: false,
  middleware: 'guest',
})

const { 
  fetchLoginPolicy, 
  shouldShowSsoButton, 
  shouldShowAdLogin, 
  shouldShowLocalLogin,
  policy,
} = useLoginPolicy()

const preferredMethod = computed(() => {
  if (policy.value?.adEnabled) return 'AD'
  return 'LOCAL'
})

// Fetch login policy on mount
onMounted(async () => {
  await fetchLoginPolicy()
})
</script>

<style scoped>
.login-page {
  font-family: 'Noto Sans KR', sans-serif;
}
</style>
