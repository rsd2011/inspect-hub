# Notification Store (알림)

```typescript
// features/notification/model/notification.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from './notification.types'

export const useNotificationStore = defineStore('notification', () => {
  // ==================== State ====================
  const notifications = ref<Notification[]>([])
  const isConnected = ref(false)
  const sseClient = ref<EventSource | null>(null)

  // ==================== Getters ====================
  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.read).length
  })

  const unreadNotifications = computed(() => {
    return notifications.value.filter(n => !n.read)
  })

  const recentNotifications = computed(() => {
    return notifications.value.slice(0, 10)
  })

  // ==================== Actions ====================
  function connectSSE() {
    const authStore = useAuthStore()
    
    if (!authStore.accessToken) {
      console.warn('Cannot connect SSE: No access token')
      return
    }

    const config = useRuntimeConfig()
    const url = `${config.public.apiBase}/notifications/stream`
    
    sseClient.value = new EventSource(url, {
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`
      }
    })

    sseClient.value.onopen = () => {
      isConnected.value = true
      console.log('SSE connected')
    }

    sseClient.value.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data)
        addNotification(notification)
      } catch (error) {
        console.error('Failed to parse notification:', error)
      }
    }

    sseClient.value.onerror = (error) => {
      console.error('SSE error:', error)
      isConnected.value = false
      
      // 재연결 시도 (5초 후)
      setTimeout(() => {
        if (!isConnected.value) {
          connectSSE()
        }
      }, 5000)
    }
  }

  function disconnectSSE() {
    if (sseClient.value) {
      sseClient.value.close()
      sseClient.value = null
      isConnected.value = false
    }
  }

  function addNotification(notification: Notification) {
    notifications.value.unshift(notification)
    
    // Toast 표시
    const uiStore = useUIStore()
    uiStore.showToast(notification.message, notification.type)
  }

  async function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
      
      // API 호출
      try {
        await notificationApi.markAsRead(id)
      } catch (error) {
        console.error('Failed to mark notification as read:', error)
      }
    }
  }

  async function markAllAsRead() {
    notifications.value.forEach(n => n.read = true)
    
    try {
      await notificationApi.markAllAsRead()
    } catch (error) {
      console.error('Failed to mark all as read:', error)
    }
  }

  async function deleteNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index >= 0) {
      notifications.value.splice(index, 1)
      
      try {
        await notificationApi.deleteNotification(id)
      } catch (error) {
        console.error('Failed to delete notification:', error)
      }
    }
  }

  function clearNotifications() {
    notifications.value = []
  }

  // ==================== Return ====================
  return {
    // State
    notifications,
    isConnected,
    
    // Getters
    unreadCount,
    unreadNotifications,
    recentNotifications,
    
    // Actions
    connectSSE,
    disconnectSSE,
    addNotification,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    clearNotifications
  }
})
```

---
