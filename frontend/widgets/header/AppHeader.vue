<template>
  <header class="app-header tw-bg-white tw-shadow-sm tw-border-b tw-border-gray-200">
    <div class="tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-3">
      <!-- Left: Logo and Title -->
      <div class="tw-flex tw-items-center tw-gap-4">
        <button
          v-if="showMenuToggle"
          type="button"
          class="tw-p-2 tw-rounded-md hover:tw-bg-gray-100"
          aria-label="메뉴 토글"
          @click="toggleSidebar"
        >
          <i class="pi pi-bars tw-text-xl" />
        </button>
        <div class="tw-flex tw-items-center tw-gap-3">
          <img
            v-if="logo"
            :src="logo"
            alt="Logo"
            class="tw-h-8"
          >
          <h1 class="tw-text-xl tw-font-semibold tw-text-gray-800">
            {{ title }}
          </h1>
        </div>
      </div>

      <!-- Right: Actions and User Menu -->
      <div class="tw-flex tw-items-center tw-gap-4">
        <!-- Language Selector -->
        <div v-if="showLanguageSelector" class="tw-flex tw-items-center tw-gap-2">
          <button
            type="button"
            :class="['tw-px-3 tw-py-1 tw-rounded-md tw-text-sm', locale === 'ko' ? 'tw-bg-primary-100 tw-text-primary-700' : 'tw-text-gray-600 hover:tw-bg-gray-100']"
            @click="switchLocale('ko')"
          >
            한국어
          </button>
          <button
            type="button"
            :class="['tw-px-3 tw-py-1 tw-rounded-md tw-text-sm', locale === 'en' ? 'tw-bg-primary-100 tw-text-primary-700' : 'tw-text-gray-600 hover:tw-bg-gray-100']"
            @click="switchLocale('en')"
          >
            English
          </button>
        </div>

        <!-- Notifications -->
        <button
          v-if="showNotifications"
          type="button"
          class="tw-relative tw-p-2 tw-rounded-md hover:tw-bg-gray-100"
          aria-label="알림"
          @click="toggleNotifications"
        >
          <i class="pi pi-bell tw-text-xl tw-text-gray-600" />
          <span
            v-if="notificationCount > 0"
            class="tw-absolute tw-top-1 tw-right-1 tw-bg-red-500 tw-text-white tw-text-xs tw-rounded-full tw-w-5 tw-h-5 tw-flex tw-items-center tw-justify-center"
          >
            {{ notificationCount > 99 ? '99+' : notificationCount }}
          </span>
        </button>

        <!-- User Menu -->
        <div class="tw-relative">
          <button
            type="button"
            class="tw-flex tw-items-center tw-gap-3 tw-px-3 tw-py-2 tw-rounded-md hover:tw-bg-gray-100"
            aria-label="사용자 메뉴"
            @click="toggleUserMenu"
          >
            <div class="tw-text-right">
              <div class="tw-text-sm tw-font-medium tw-text-gray-800">
                {{ user?.displayName || user?.username || '사용자' }}
              </div>
              <div class="tw-text-xs tw-text-gray-500">
                {{ user?.organizationName || '' }}
              </div>
            </div>
            <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-primary-100 tw-flex tw-items-center tw-justify-center">
              <i class="pi pi-user tw-text-primary-600" />
            </div>
            <i :class="['pi', userMenuOpen ? 'pi-chevron-up' : 'pi-chevron-down', 'tw-text-gray-400']" />
          </button>

          <!-- User Dropdown Menu -->
          <Transition name="fade">
            <div
              v-if="userMenuOpen"
              class="tw-absolute tw-right-0 tw-mt-2 tw-w-64 tw-bg-white tw-rounded-lg tw-shadow-lg tw-border tw-border-gray-200 tw-py-2 tw-z-50"
            >
              <div class="tw-px-4 tw-py-3 tw-border-b tw-border-gray-100">
                <div class="tw-text-sm tw-font-medium tw-text-gray-800">
                  {{ user?.displayName || user?.username }}
                </div>
                <div class="tw-text-xs tw-text-gray-500 tw-mt-1">
                  {{ user?.email }}
                </div>
                <div v-if="user?.roles?.length" class="tw-mt-2 tw-flex tw-flex-wrap tw-gap-1">
                  <span
                    v-for="role in user.roles.slice(0, 2)"
                    :key="role"
                    class="tw-px-2 tw-py-0.5 tw-bg-gray-100 tw-text-gray-600 tw-text-xs tw-rounded"
                  >
                    {{ role.replace('ROLE_', '') }}
                  </span>
                </div>
              </div>

              <button
                type="button"
                class="tw-w-full tw-px-4 tw-py-2 tw-text-left tw-text-sm tw-text-gray-700 hover:tw-bg-gray-50 tw-flex tw-items-center tw-gap-3"
                @click="goToProfile"
              >
                <i class="pi pi-user" />
                <span>프로필</span>
              </button>

              <button
                type="button"
                class="tw-w-full tw-px-4 tw-py-2 tw-text-left tw-text-sm tw-text-gray-700 hover:tw-bg-gray-50 tw-flex tw-items-center tw-gap-3"
                @click="goToSettings"
              >
                <i class="pi pi-cog" />
                <span>설정</span>
              </button>

              <div class="tw-border-t tw-border-gray-100 tw-my-2" />

              <button
                type="button"
                class="tw-w-full tw-px-4 tw-py-2 tw-text-left tw-text-sm tw-text-red-600 hover:tw-bg-red-50 tw-flex tw-items-center tw-gap-3"
                @click="handleLogout"
              >
                <i class="pi pi-sign-out" />
                <span>로그아웃</span>
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
/**
 * AppHeader Widget
 *
 * Main application header with logo, user menu, notifications, and language selector.
 * Integrates with auth store and session management.
 *
 * @example
 * ```vue
 * <AppHeader
 *   title="Inspect Hub"
 *   :show-menu-toggle="true"
 *   :show-notifications="true"
 *   @toggle-sidebar="onToggleSidebar"
 * />
 * ```
 */

const props = defineProps({
  /**
   * Application title
   */
  title: {
    type: String,
    default: 'Inspect Hub',
  },

  /**
   * Logo image URL
   */
  logo: {
    type: String,
    default: undefined,
  },

  /**
   * Show menu toggle button
   */
  showMenuToggle: {
    type: Boolean,
    default: true,
  },

  /**
   * Show notifications button
   */
  showNotifications: {
    type: Boolean,
    default: true,
  },

  /**
   * Show language selector
   */
  showLanguageSelector: {
    type: Boolean,
    default: true,
  },

  /**
   * Notification count
   */
  notificationCount: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits<{
  'toggle-sidebar': []
  'toggle-notifications': []
  logout: []
}>()

const router = useRouter()
const { locale, setLocale } = useI18n()
const authStore = useAuthStore()

const userMenuOpen = ref(false)

const user = computed(() => authStore.user)

function toggleSidebar() {
  emit('toggle-sidebar')
}

function toggleNotifications() {
  emit('toggle-notifications')
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
}

function switchLocale(newLocale: string) {
  setLocale(newLocale)
}

function goToProfile() {
  userMenuOpen.value = false
  router.push('/profile')
}

function goToSettings() {
  userMenuOpen.value = false
  router.push('/settings')
}

async function handleLogout() {
  userMenuOpen.value = false
  await authStore.logout()
  emit('logout')
  router.push('/login')
}

// Close user menu when clicking outside
onMounted(() => {
  document.addEventListener('click', (event) => {
    const target = event.target as HTMLElement
    if (userMenuOpen.value && !target.closest('.tw-relative')) {
      userMenuOpen.value = false
    }
  })
})
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
