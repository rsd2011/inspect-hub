<template>
  <div class="dashboard-page">
    <h1 class="tw-text-3xl tw-font-bold tw-mb-6">Dashboard</h1>
    
    <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6">
      <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow">
        <h3 class="tw-text-lg tw-font-semibold tw-mb-2">STR Cases</h3>
        <p class="tw-text-3xl tw-font-bold tw-text-blue-600">12</p>
      </div>
      
      <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow">
        <h3 class="tw-text-lg tw-font-semibold tw-mb-2">CTR Cases</h3>
        <p class="tw-text-3xl tw-font-bold tw-text-green-600">8</p>
      </div>
      
      <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow">
        <h3 class="tw-text-lg tw-font-semibold tw-mb-2">WLF Alerts</h3>
        <p class="tw-text-3xl tw-font-bold tw-text-red-600">3</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: 'auth',
})
</script>

<style scoped>
.dashboard-page {
  padding: 2rem;
}
</style>
