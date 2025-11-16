# User Store (사용자 관리)

```typescript
// entities/user/model/user.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserSearchParams, PaginatedResponse } from './user.types'
import * as userApi from '../api/user.api'

export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  const users = ref<User[]>([])
  const currentUser = ref<User | null>(null)
  const isLoading = ref(false)
  const totalCount = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // ==================== Getters ====================
  const totalPages = computed(() => {
    return Math.ceil(totalCount.value / pageSize.value)
  })

  const hasNextPage = computed(() => {
    return currentPage.value < totalPages.value
  })

  const hasPreviousPage = computed(() => {
    return currentPage.value > 1
  })

  const getUserById = computed(() => (id: string) => {
    return users.value.find(user => user.id === id)
  })

  // ==================== Actions ====================
  async function fetchUsers(params?: UserSearchParams) {
    isLoading.value = true
    
    try {
      const response = await userApi.getUsers({
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })

      if (response.success) {
        users.value = response.data.items
        totalCount.value = response.data.pagination.totalElements
      }
    } catch (error) {
      console.error('Failed to fetch users:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUserById(id: string) {
    isLoading.value = true
    
    try {
      const response = await userApi.getUserById(id)
      
      if (response.success) {
        currentUser.value = response.data
        
        // 캐시에도 추가/업데이트
        const index = users.value.findIndex(u => u.id === id)
        if (index >= 0) {
          users.value[index] = response.data
        } else {
          users.value.push(response.data)
        }
      }
    } catch (error) {
      console.error('Failed to fetch user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function createUser(userData: Partial<User>) {
    isLoading.value = true
    
    try {
      const response = await userApi.createUser(userData)
      
      if (response.success) {
        users.value.unshift(response.data)
        totalCount.value++
      }
      
      return response
    } catch (error) {
      console.error('Failed to create user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateUser(id: string, userData: Partial<User>) {
    isLoading.value = true
    
    try {
      const response = await userApi.updateUser(id, userData)
      
      if (response.success) {
        const index = users.value.findIndex(u => u.id === id)
        if (index >= 0) {
          users.value[index] = response.data
        }
        
        if (currentUser.value?.id === id) {
          currentUser.value = response.data
        }
      }
      
      return response
    } catch (error) {
      console.error('Failed to update user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function deleteUser(id: string) {
    isLoading.value = true
    
    try {
      await userApi.deleteUser(id)
      
      // 목록에서 제거
      const index = users.value.findIndex(u => u.id === id)
      if (index >= 0) {
        users.value.splice(index, 1)
        totalCount.value--
      }
      
      if (currentUser.value?.id === id) {
        currentUser.value = null
      }
    } catch (error) {
      console.error('Failed to delete user:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  function setCurrentPage(page: number) {
    currentPage.value = page
  }

  function setPageSize(size: number) {
    pageSize.value = size
  }

  function clearCurrentUser() {
    currentUser.value = null
  }

  function clearUsers() {
    users.value = []
    totalCount.value = 0
  }

  // ==================== Return ====================
  return {
    // State
    users,
    currentUser,
    isLoading,
    totalCount,
    currentPage,
    pageSize,
    
    // Getters
    totalPages,
    hasNextPage,
    hasPreviousPage,
    getUserById,
    
    // Actions
    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    setCurrentPage,
    setPageSize,
    clearCurrentUser,
    clearUsers
  }
})
```

---
