# Store 간 통신

## Store에서 다른 Store 사용

```typescript
// entities/case/model/case.store.ts
import { useAuthStore } from '~/features/auth/model/auth.store'
import { useUIStore } from '~/shared/lib/ui-store/ui.store'

export const useCaseStore = defineStore('case', () => {
  const authStore = useAuthStore()
  const uiStore = useUIStore()

  async function fetchMyCases() {
    // Auth store의 데이터 사용
    const userId = authStore.user?.id
    
    if (!userId) {
      uiStore.showToast('로그인이 필요합니다', 'error')
      return
    }

    // UI 로딩 표시
    uiStore.showLoading('내 사례 조회 중...')
    
    try {
      const response = await caseApi.getMyCases(userId)
      // ...
    } finally {
      uiStore.hideLoading()
    }
  }

  return {
    fetchMyCases
  }
})
```

## Composable을 통한 Store 조합

```typescript
// shared/lib/composables/useCaseManagement.ts
export function useCaseManagement() {
  const caseStore = useCaseStore()
  const authStore = useAuthStore()
  const uiStore = useUIStore()

  async function approveCaseWithNotification(caseId: string, comment: string) {
    // 권한 확인
    if (!authStore.hasPermission('case:approve')) {
      uiStore.showToast('승인 권한이 없습니다', 'error')
      return
    }

    try {
      await caseStore.approveCase(caseId, comment)
      uiStore.showToast('사례가 승인되었습니다', 'success')
    } catch (error) {
      uiStore.showToast('승인 중 오류가 발생했습니다', 'error')
      throw error
    }
  }

  return {
    approveCaseWithNotification
  }
}
```

---
