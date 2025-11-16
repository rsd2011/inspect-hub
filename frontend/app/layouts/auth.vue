<template>
  <div class="auth-layout tw-min-h-screen tw-bg-gradient-to-br tw-from-blue-50 tw-to-indigo-100">
    <div class="tw-flex tw-items-center tw-justify-center tw-min-h-screen tw-px-4">
      <div class="tw-w-full tw-max-w-md">
        <!-- Logo / Title -->
        <div class="tw-text-center tw-mb-8">
          <h1 class="tw-text-3xl tw-font-bold tw-text-gray-900">Inspect Hub</h1>
          <p class="tw-text-sm tw-text-gray-600 tw-mt-2">AML Compliance System</p>
        </div>

        <!-- Content Slot -->
        <div class="tw-bg-white tw-rounded-lg tw-shadow-xl tw-p-8">
          <slot />
        </div>

        <!-- Footer -->
        <div class="tw-text-center tw-mt-6">
          <p class="tw-text-xs tw-text-gray-500">
            &copy; {{ new Date().getFullYear() }} Inspect Hub. All rights reserved.
          </p>
        </div>
      </div>
    </div>

    <Toast />
  </div>
</template>

<script setup lang="ts">
// Auth layout for login pages
</script>

<style scoped>
.auth-layout {
  font-family: 'Noto Sans KR', sans-serif;
}
</style>
