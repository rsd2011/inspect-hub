<template>
  <div class="sso-login">
    <Button
      type="button"
      variant="outline"
      size="lg"
      :loading="loading"
      full-width
      @click="handleSsoLogin"
    >
      <i class="pi pi-sign-in tw-mr-2" />
      {{ $t('auth.ssoLogin') }}
    </Button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const config = useRuntimeConfig()
const loading = ref(false)

const handleSsoLogin = async () => {
  loading.value = true
  try {
    // Redirect to SSO endpoint
    const ssoUrl = `${config.public.apiBase}/auth/sso/login`
    window.location.href = ssoUrl
  } catch (error) {
    console.error('SSO login failed:', error)
    loading.value = false
  }
}
</script>
