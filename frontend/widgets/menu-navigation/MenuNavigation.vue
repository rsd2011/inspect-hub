<template>
  <nav class="menu-navigation">
    <ul class="tw-space-y-1 tw-p-2">
      <template v-for="item in visibleMenuItems" :key="item.id">
        <li v-if="!item.children || item.children.length === 0">
          <router-link
            :to="item.path"
            class="menu-item"
            :class="{ 'active': isActive(item.path) }"
          >
            <i v-if="item.icon" :class="['pi', item.icon, 'menu-icon']" />
            <Transition name="fade">
              <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
            </Transition>
            <span
              v-if="item.badge && !collapsed"
              class="menu-badge"
            >
              {{ item.badge }}
            </span>
          </router-link>
        </li>

        <li v-else class="menu-group">
          <button
            type="button"
            class="menu-item menu-group-toggle"
            :class="{ 'active': isGroupActive(item) }"
            @click="toggleGroup(item.id)"
          >
            <i v-if="item.icon" :class="['pi', item.icon, 'menu-icon']" />
            <Transition name="fade">
              <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
            </Transition>
            <i
              v-if="!collapsed"
              :class="['pi', expandedGroups.has(item.id) ? 'pi-chevron-down' : 'pi-chevron-right', 'menu-chevron']"
            />
          </button>

          <Transition name="expand">
            <ul
              v-if="expandedGroups.has(item.id) && !collapsed"
              class="tw-space-y-1 tw-mt-1 tw-ml-4"
            >
              <li v-for="child in item.children.filter(c => hasPermission(c))" :key="child.id">
                <router-link
                  :to="child.path"
                  class="menu-item menu-child"
                  :class="{ 'active': isActive(child.path) }"
                >
                  <i v-if="child.icon" :class="['pi', child.icon, 'menu-icon']" />
                  <span class="menu-label">{{ child.label }}</span>
                  <span
                    v-if="child.badge"
                    class="menu-badge"
                  >
                    {{ child.badge }}
                  </span>
                </router-link>
              </li>
            </ul>
          </Transition>
        </li>
      </template>
    </ul>
  </nav>
</template>

<script setup lang="ts">
/**
 * MenuNavigation Widget
 *
 * Hierarchical menu with permission-based filtering.
 * Supports collapsible groups and active state highlighting.
 *
 * @example
 * ```vue
 * <MenuNavigation :collapsed="sidebarCollapsed" />
 * ```
 */

export interface MenuItem {
  id: string
  label: string
  path?: string
  icon?: string
  badge?: string | number
  roles?: string[]
  permissions?: string[]
  children?: MenuItem[]
}

const props = defineProps({
  /**
   * Collapsed state (icons only)
   */
  collapsed: {
    type: Boolean,
    default: false,
  },
})

const router = useRouter()
const route = useRoute()
const permissionManager = usePermissionManager()

const expandedGroups = ref<Set<string>>(new Set())

/**
 * Menu structure
 * TODO: This should be fetched from the backend based on user permissions
 */
const menuItems: MenuItem[] = [
  {
    id: 'dashboard',
    label: '대시보드',
    path: '/dashboard',
    icon: 'pi-home',
    roles: ['ROLE_USER'],
  },
  {
    id: 'cases',
    label: '사례 관리',
    icon: 'pi-folder',
    roles: ['ROLE_USER'],
    children: [
      {
        id: 'str-cases',
        label: 'STR 의심거래',
        path: '/cases/str',
        icon: 'pi-exclamation-circle',
        permissions: ['STR_READ'],
      },
      {
        id: 'ctr-cases',
        label: 'CTR 고액거래',
        path: '/cases/ctr',
        icon: 'pi-money-bill',
        permissions: ['CTR_READ'],
      },
      {
        id: 'wlf-cases',
        label: 'WLF 감시대상',
        path: '/cases/wlf',
        icon: 'pi-shield',
        permissions: ['WLF_READ'],
      },
    ],
  },
  {
    id: 'detection',
    label: '탐지 관리',
    icon: 'pi-eye',
    roles: ['ROLE_ANALYST'],
    children: [
      {
        id: 'rules',
        label: '탐지 규칙',
        path: '/detection/rules',
        icon: 'pi-filter',
        permissions: ['RULE_READ'],
      },
      {
        id: 'policies',
        label: '정책 관리',
        path: '/detection/policies',
        icon: 'pi-book',
        permissions: ['POLICY_READ'],
      },
      {
        id: 'snapshots',
        label: '스냅샷 관리',
        path: '/detection/snapshots',
        icon: 'pi-camera',
        permissions: ['SNAPSHOT_READ'],
      },
    ],
  },
  {
    id: 'reports',
    label: '보고서',
    icon: 'pi-file',
    roles: ['ROLE_USER'],
    children: [
      {
        id: 'str-reports',
        label: 'STR 보고서',
        path: '/reports/str',
        icon: 'pi-file-export',
        permissions: ['REPORT_READ'],
      },
      {
        id: 'ctr-reports',
        label: 'CTR 보고서',
        path: '/reports/ctr',
        icon: 'pi-file-export',
        permissions: ['REPORT_READ'],
      },
      {
        id: 'analytics',
        label: '분석 리포트',
        path: '/reports/analytics',
        icon: 'pi-chart-bar',
        permissions: ['ANALYTICS_READ'],
      },
    ],
  },
  {
    id: 'admin',
    label: '관리',
    icon: 'pi-cog',
    roles: ['ROLE_ADMIN'],
    children: [
      {
        id: 'users',
        label: '사용자 관리',
        path: '/admin/users',
        icon: 'pi-users',
        permissions: ['USER_READ'],
      },
      {
        id: 'organizations',
        label: '조직 관리',
        path: '/admin/organizations',
        icon: 'pi-building',
        permissions: ['ORG_READ'],
      },
      {
        id: 'permissions',
        label: '권한 관리',
        path: '/admin/permissions',
        icon: 'pi-lock',
        permissions: ['PERMISSION_READ'],
      },
      {
        id: 'audit',
        label: '감사 로그',
        path: '/admin/audit',
        icon: 'pi-history',
        permissions: ['AUDIT_READ'],
      },
    ],
  },
]

const visibleMenuItems = computed(() => {
  return menuItems.filter(item => hasPermission(item))
})

function hasPermission(item: MenuItem): boolean {
  // Check role requirements
  if (item.roles && item.roles.length > 0) {
    if (!permissionManager.hasRole(item.roles)) {
      return false
    }
  }

  // Check permission requirements
  if (item.permissions && item.permissions.length > 0) {
    if (!permissionManager.hasPermission(item.permissions)) {
      return false
    }
  }

  return true
}

function isActive(path?: string): boolean {
  if (!path) return false
  return route.path === path || route.path.startsWith(path + '/')
}

function isGroupActive(item: MenuItem): boolean {
  if (!item.children) return false
  return item.children.some(child => isActive(child.path))
}

function toggleGroup(groupId: string) {
  if (expandedGroups.value.has(groupId)) {
    expandedGroups.value.delete(groupId)
  } else {
    expandedGroups.value.add(groupId)
  }
}

// Auto-expand active group
watch(() => route.path, () => {
  menuItems.forEach((item) => {
    if (item.children && isGroupActive(item)) {
      expandedGroups.value.add(item.id)
    }
  })
}, { immediate: true })
</script>

<style scoped>
.menu-item {
  @apply tw-flex tw-items-center tw-gap-3 tw-px-3 tw-py-2 tw-rounded-md tw-text-gray-700 tw-no-underline tw-cursor-pointer tw-transition-colors;
  @apply hover:tw-bg-gray-100;
}

.menu-item.active {
  @apply tw-bg-primary-100 tw-text-primary-700;
}

.menu-item.menu-group-toggle {
  @apply tw-w-full tw-text-left tw-border-none tw-bg-transparent;
}

.menu-item.menu-child {
  @apply tw-text-sm;
}

.menu-icon {
  @apply tw-text-lg tw-flex-shrink-0;
}

.menu-label {
  @apply tw-flex-1 tw-truncate;
}

.menu-chevron {
  @apply tw-text-gray-400 tw-text-xs tw-ml-auto;
}

.menu-badge {
  @apply tw-px-2 tw-py-0.5 tw-bg-red-500 tw-text-white tw-text-xs tw-rounded-full tw-ml-auto;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
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
