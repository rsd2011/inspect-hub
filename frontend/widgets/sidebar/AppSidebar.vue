<template>
  <aside
    :class="[
      'app-sidebar tw-bg-white tw-border-r tw-border-gray-200 tw-transition-all tw-duration-300',
      isCollapsed ? 'tw-w-16' : 'tw-w-64',
      isMobile && !isOpen ? 'tw--translate-x-full' : 'tw-translate-x-0'
    ]"
  >
    <!-- Sidebar Header -->
    <div class="tw-flex tw-items-center tw-justify-between tw-px-4 tw-py-3 tw-border-b tw-border-gray-200">
      <Transition name="fade">
        <span
          v-if="!isCollapsed"
          class="tw-text-sm tw-font-medium tw-text-gray-600"
        >
          Navigation
        </span>
      </Transition>
      <button
        v-if="!isMobile"
        type="button"
        class="tw-p-2 tw-rounded-md hover:tw-bg-gray-100"
        :aria-label="isCollapsed ? '사이드바 펼치기' : '사이드바 접기'"
        @click="toggleCollapse"
      >
        <i :class="['pi', isCollapsed ? 'pi-angle-right' : 'pi-angle-left', 'tw-text-gray-600']" />
      </button>
    </div>

    <!-- Menu Navigation -->
    <div class="tw-overflow-y-auto tw-h-[calc(100vh-120px)]">
      <MenuNavigation :collapsed="isCollapsed" />
    </div>

    <!-- Sidebar Footer (Optional) -->
    <div
      v-if="showFooter"
      class="tw-absolute tw-bottom-0 tw-left-0 tw-right-0 tw-border-t tw-border-gray-200 tw-bg-white tw-p-4"
    >
      <Transition name="fade">
        <div v-if="!isCollapsed" class="tw-text-xs tw-text-gray-500 tw-text-center">
          <div>{{ appVersion }}</div>
          <div class="tw-mt-1">© 2025 Inspect Hub</div>
        </div>
      </Transition>
    </div>

    <!-- Mobile Overlay -->
    <Transition name="fade">
      <div
        v-if="isMobile && isOpen"
        class="tw-fixed tw-inset-0 tw-bg-black tw-bg-opacity-50 tw-z-40"
        @click="close"
      />
    </Transition>
  </aside>
</template>

<script setup lang="ts">
/**
 * AppSidebar Widget
 *
 * Collapsible sidebar with navigation menu.
 * Responsive design with mobile overlay support.
 *
 * @example
 * ```vue
 * <AppSidebar
 *   v-model:open="sidebarOpen"
 *   v-model:collapsed="sidebarCollapsed"
 *   :show-footer="true"
 * />
 * ```
 */

const props = defineProps({
  /**
   * Sidebar open state (mobile)
   */
  open: {
    type: Boolean,
    default: false,
  },

  /**
   * Sidebar collapsed state
   */
  collapsed: {
    type: Boolean,
    default: false,
  },

  /**
   * Show sidebar footer
   */
  showFooter: {
    type: Boolean,
    default: true,
  },

  /**
   * Application version
   */
  appVersion: {
    type: String,
    default: 'v1.0.0',
  },
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'update:collapsed': [value: boolean]
}>()

const isMobile = ref(false)

const isOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const isCollapsed = computed({
  get: () => props.collapsed,
  set: (value) => emit('update:collapsed', value),
})

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

function close() {
  isOpen.value = false
}

// Detect mobile viewport
function checkMobile() {
  isMobile.value = window.innerWidth < 768
  // Auto-collapse on mobile
  if (isMobile.value && !isCollapsed.value) {
    isCollapsed.value = true
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-sidebar {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

@media (max-width: 767px) {
  .app-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 50;
    height: 100vh;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
