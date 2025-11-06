<template>
  <div class="tw-container tw-mx-auto tw-p-6">
    <h1 class="tw-text-3xl tw-font-bold tw-mb-4">
      {{ $t('menu.dashboard') }}
    </h1>
    <Card>
      <template #title>
        Welcome to Inspect Hub
      </template>
      <template #content>
        <p>AML Integrated Compliance Monitoring System</p>
        <div class="tw-mt-4">
          <p>로그인한 사용자: {{ authStore.user?.username }}</p>
          <Button label="로그아웃" @click="handleLogout" class="tw-mt-2" />
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({
  middleware: ['auth']
})

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>
