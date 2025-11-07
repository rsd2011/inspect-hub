<template>
  <div class="layout-wrapper">
    <header class="tw-bg-white tw-shadow-sm">
      <div class="tw-container tw-mx-auto tw-px-4 tw-py-4">
        <h1 class="tw-text-xl tw-font-bold">Inspect Hub</h1>
      </div>
    </header>

    <main class="tw-min-h-screen tw-bg-gray-50">
      <slot />
    </main>

    <footer class="tw-bg-white tw-border-t tw-border-gray-200">
      <div class="tw-container tw-mx-auto tw-px-4 tw-py-4 tw-text-center tw-text-sm tw-text-gray-600">
        &copy; 2024 Inspect Hub. All rights reserved.
      </div>
    </footer>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
// Default layout
</script>

<style scoped>
.layout-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
}
</style>
