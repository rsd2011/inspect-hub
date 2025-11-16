# Case Store (사례 관리)

```typescript
// entities/case/model/case.store.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Case, CaseSearchParams, CaseStatus } from './case.types'
import * as caseApi from '../api/case.api'

export const useCaseStore = defineStore('case', () => {
  // ==================== State ====================
  const cases = ref<Case[]>([])
  const currentCase = ref<Case | null>(null)
  const isLoading = ref(false)
  const totalCount = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)
  const filters = ref<CaseSearchParams>({})

  // ==================== Getters ====================
  const casesByStatus = computed(() => (status: CaseStatus) => {
    return cases.value.filter(c => c.status === status)
  })

  const highRiskCases = computed(() => {
    return cases.value.filter(c => c.riskLevel === 'HIGH')
  })

  const myCases = computed(() => {
    const authStore = useAuthStore()
    return cases.value.filter(c => c.assignee === authStore.user?.username)
  })

  // ==================== Actions ====================
  async function fetchCases(params?: CaseSearchParams) {
    isLoading.value = true
    
    try {
      const response = await caseApi.getCases({
        page: currentPage.value,
        size: pageSize.value,
        ...filters.value,
        ...params
      })

      if (response.success) {
        cases.value = response.data.items
        totalCount.value = response.data.pagination.totalElements
      }
    } catch (error) {
      console.error('Failed to fetch cases:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCaseById(id: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.getCaseById(id)
      
      if (response.success) {
        currentCase.value = response.data
      }
    } catch (error) {
      console.error('Failed to fetch case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function createCase(caseData: Partial<Case>) {
    isLoading.value = true
    
    try {
      const response = await caseApi.createCase(caseData)
      
      if (response.success) {
        cases.value.unshift(response.data)
        totalCount.value++
      }
      
      return response
    } catch (error) {
      console.error('Failed to create case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateCase(id: string, caseData: Partial<Case>) {
    isLoading.value = true
    
    try {
      const response = await caseApi.updateCase(id, caseData)
      
      if (response.success) {
        const index = cases.value.findIndex(c => c.id === id)
        if (index >= 0) {
          cases.value[index] = { ...cases.value[index], ...response.data }
        }
        
        if (currentCase.value?.id === id) {
          currentCase.value = response.data
        }
      }
      
      return response
    } catch (error) {
      console.error('Failed to update case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateCaseStatus(id: string, status: CaseStatus, comment?: string) {
    return await updateCase(id, { status, statusComment: comment })
  }

  async function assignCase(id: string, assignee: string) {
    return await updateCase(id, { assignee })
  }

  async function approveCase(id: string, comment: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.approveCase(id, { comment })
      
      if (response.success) {
        await fetchCaseById(id)
      }
      
      return response
    } catch (error) {
      console.error('Failed to approve case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function rejectCase(id: string, reason: string) {
    isLoading.value = true
    
    try {
      const response = await caseApi.rejectCase(id, { reason })
      
      if (response.success) {
        await fetchCaseById(id)
      }
      
      return response
    } catch (error) {
      console.error('Failed to reject case:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  function setFilters(newFilters: CaseSearchParams) {
    filters.value = { ...filters.value, ...newFilters }
  }

  function clearFilters() {
    filters.value = {}
  }

  function setCurrentPage(page: number) {
    currentPage.value = page
  }

  // ==================== Return ====================
  return {
    // State
    cases,
    currentCase,
    isLoading,
    totalCount,
    currentPage,
    pageSize,
    filters,
    
    // Getters
    casesByStatus,
    highRiskCases,
    myCases,
    
    // Actions
    fetchCases,
    fetchCaseById,
    createCase,
    updateCase,
    updateCaseStatus,
    assignCase,
    approveCase,
    rejectCase,
    setFilters,
    clearFilters,
    setCurrentPage
  }
})
```

---
