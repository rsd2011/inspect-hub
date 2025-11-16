<template>
  <footer class="app-footer tw-bg-white tw-border-t tw-border-gray-200">
    <div class="tw-container tw-mx-auto tw-px-4 tw-py-4">
      <div class="tw-flex tw-flex-col md:tw-flex-row tw-justify-between tw-items-center tw-text-sm tw-text-gray-600">
        <p>&copy; {{ currentYear }} Inspect Hub. All rights reserved.</p>
        <div class="tw-flex tw-gap-4 tw-mt-2 md:tw-mt-0">
          <a href="#" class="hover:tw-text-gray-900 tw-transition-colors">Privacy Policy</a>
          <a href="#" class="hover:tw-text-gray-900 tw-transition-colors">Terms of Service</a>
          <a href="#" class="hover:tw-text-gray-900 tw-transition-colors">Support</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>
