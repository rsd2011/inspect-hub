<template>
  <header class="app-header tw-bg-white tw-shadow-sm tw-border-b tw-border-gray-200">
    <div class="tw-container tw-mx-auto tw-px-4 tw-h-16 tw-flex tw-items-center tw-justify-between">
      <!-- Logo & Title -->
      <div class="tw-flex tw-items-center tw-gap-3">
        <h1 class="tw-text-xl tw-font-bold tw-text-gray-900">Inspect Hub</h1>
        <span class="tw-text-xs tw-text-gray-500 tw-px-2 tw-py-1 tw-bg-blue-100 tw-rounded">AML</span>
      </div>

      <!-- User Menu -->
      <div v-if="user" class="tw-flex tw-items-center tw-gap-4">
        <span class="tw-text-sm tw-text-gray-700">{{ user.username }}</span>
        <button
          type="button"
          class="tw-px-3 tw-py-1.5 tw-text-sm tw-text-gray-700 hover:tw-text-gray-900 hover:tw-bg-gray-100 tw-rounded tw-transition-colors"
          @click="handleLogout"
        >
          <i class="pi pi-sign-out tw-mr-1" />
          Logout
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
const authStore = useAuthStore()
const router = useRouter()

const user = computed(() => authStore.user)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 40;
}
</style>
