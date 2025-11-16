# UI Store (UI 상태)

```typescript
// shared/lib/ui-store/ui.store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // ==================== State ====================
  const isSidebarCollapsed = ref(false)
  const isMobileMenuOpen = ref(false)
  const isLoading = ref(false)
  const loadingMessage = ref('')
  const breadcrumbs = ref<{ label: string; to?: string }[]>([])

  // Modal/Dialog 상태
  const activeModals = ref<Set<string>>(new Set())
  
  // Toast 알림
  interface Toast {
    id: string
    message: string
    type: 'success' | 'error' | 'warning' | 'info'
    duration?: number
  }
  const toasts = ref<Toast[]>([])

  // ==================== Actions ====================
  function toggleSidebar() {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }

  function setSidebarCollapsed(collapsed: boolean) {
    isSidebarCollapsed.value = collapsed
  }

  function toggleMobileMenu() {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
  }

  function closeMobileMenu() {
    isMobileMenuOpen.value = false
  }

  function showLoading(message = '로딩 중...') {
    isLoading.value = true
    loadingMessage.value = message
  }

  function hideLoading() {
    isLoading.value = false
    loadingMessage.value = ''
  }

  function setBreadcrumbs(items: { label: string; to?: string }[]) {
    breadcrumbs.value = items
  }

  function openModal(modalId: string) {
    activeModals.value.add(modalId)
  }

  function closeModal(modalId: string) {
    activeModals.value.delete(modalId)
  }

  function isModalOpen(modalId: string): boolean {
    return activeModals.value.has(modalId)
  }

  function showToast(message: string, type: Toast['type'] = 'info', duration = 3000) {
    const id = `toast-${Date.now()}-${Math.random()}`
    const toast: Toast = { id, message, type, duration }
    
    toasts.value.push(toast)
    
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }
    
    return id
  }

  function removeToast(id: string) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index >= 0) {
      toasts.value.splice(index, 1)
    }
  }

  function clearToasts() {
    toasts.value = []
  }

  // ==================== Return ====================
  return {
    // State
    isSidebarCollapsed,
    isMobileMenuOpen,
    isLoading,
    loadingMessage,
    breadcrumbs,
    activeModals,
    toasts,
    
    // Actions
    toggleSidebar,
    setSidebarCollapsed,
    toggleMobileMenu,
    closeMobileMenu,
    showLoading,
    hideLoading,
    setBreadcrumbs,
    openModal,
    closeModal,
    isModalOpen,
    showToast,
    removeToast,
    clearToasts
  }
}, {
  persist: {
    storage: localStorage,
    paths: ['isSidebarCollapsed']
  }
})
```

---
