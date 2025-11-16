<template>
  <aside class="app-sidebar tw-w-64 tw-bg-gray-900 tw-text-white tw-h-full tw-flex tw-flex-col">
    <nav class="tw-flex-1 tw-px-2 tw-py-4 tw-space-y-1">
      <NuxtLink
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="sidebar-link"
        active-class="active"
      >
        <i :class="item.icon" class="tw-mr-3" />
        {{ item.label }}
      </NuxtLink>
    </nav>
  </aside>
</template>

<script setup lang="ts">
const menuItems = [
  { path: '/dashboard', label: 'Dashboard', icon: 'pi pi-home' },
  { path: '/policy', label: 'Policy Management', icon: 'pi pi-book' },
  { path: '/detection', label: 'Detection', icon: 'pi pi-exclamation-triangle' },
  { path: '/reports', label: 'Reports', icon: 'pi pi-chart-bar' },
  { path: '/settings', label: 'Settings', icon: 'pi pi-cog' },
]
</script>

<style scoped>
.sidebar-link {
  @apply tw-flex tw-items-center tw-px-3 tw-py-2 tw-text-sm tw-font-medium tw-rounded-md tw-text-gray-300 hover:tw-bg-gray-800 hover:tw-text-white tw-transition-colors;
}

.sidebar-link.active {
  @apply tw-bg-gray-800 tw-text-white;
}
</style>
