import { defineStore } from 'pinia'
import type { LoginPolicy, UpdateLoginPolicyRequest, LoginMethod } from '~/types/models'

interface LoginPolicyState {
  policy: LoginPolicy | null
  loading: boolean
  error: string | null
}

export const useLoginPolicyStore = defineStore('loginPolicy', {
  state: (): LoginPolicyState => ({
    policy: null,
    loading: false,
    error: null
  }),

  getters: {
    enabledMethods: (state) => state.policy?.enabledMethods || [],
    priority: (state) => state.policy?.priority || [],
    policyName: (state) => state.policy?.name || ''
  },

  actions: {
    /**
     * 시스템 전역 로그인 정책 조회
     */
    async fetchPolicy() {
      this.loading = true
      this.error = null

      try {
        const { data, error } = await useFetch<{
          success: boolean
          data: LoginPolicy
          message?: string
        }>('/api/v1/system/login-policy', {
          method: 'GET'
        })

        if (error.value) {
          throw new Error(error.value.message || '정책 조회 실패')
        }

        if (data.value && data.value.success) {
          this.policy = data.value.data
        } else {
          throw new Error(data.value?.message || '정책 조회 실패')
        }
      } catch (err: any) {
        this.error = err.message || '정책 조회 중 오류가 발생했습니다'
        console.error('fetchPolicy error:', err)
      } finally {
        this.loading = false
      }
    },

    /**
     * 로그인 정책 업데이트
     */
    async updatePolicy(request: UpdateLoginPolicyRequest) {
      this.loading = true
      this.error = null

      try {
        const { data, error } = await useFetch<{
          success: boolean
          data: LoginPolicy
          message?: string
        }>('/api/v1/system/login-policy', {
          method: 'PUT',
          body: request
        })

        if (error.value) {
          throw new Error(error.value.message || '정책 업데이트 실패')
        }

        if (data.value && data.value.success) {
          this.policy = data.value.data
          return true
        } else {
          throw new Error(data.value?.message || '정책 업데이트 실패')
        }
      } catch (err: any) {
        this.error = err.message || '정책 업데이트 중 오류가 발생했습니다'
        console.error('updatePolicy error:', err)
        return false
      } finally {
        this.loading = false
      }
    },

    /**
     * 로그인 방식 활성화/비활성화 토글
     */
    toggleMethod(method: LoginMethod) {
      if (!this.policy) return

      const enabledSet = new Set(this.policy.enabledMethods)

      if (enabledSet.has(method)) {
        // 마지막 남은 방식은 비활성화할 수 없음
        if (enabledSet.size === 1) {
          this.error = '최소 하나의 로그인 방식은 활성화되어야 합니다'
          return
        }
        enabledSet.delete(method)
        // Priority에서도 제거
        this.policy.priority = this.policy.priority.filter(m => m !== method)
      } else {
        enabledSet.add(method)
        // Priority에 추가 (맨 뒤)
        this.policy.priority.push(method)
      }

      this.policy.enabledMethods = Array.from(enabledSet)
    },

    /**
     * 우선순위 업데이트
     */
    updatePriority(newPriority: LoginMethod[]) {
      if (!this.policy) return
      this.policy.priority = newPriority
    },

    /**
     * 에러 초기화
     */
    clearError() {
      this.error = null
    }
  }
})
