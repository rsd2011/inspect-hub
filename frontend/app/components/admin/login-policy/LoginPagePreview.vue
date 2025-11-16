<template>
  <div class="tw-mt-6">
    <h3 class="tw-text-lg tw-font-semibold tw-mb-3">로그인 페이지 미리보기</h3>
    <Card class="tw-bg-gray-50">
      <template #content>
        <div class="tw-p-4">
          <h4 class="tw-text-center tw-text-xl tw-font-bold tw-mb-4">Inspect-Hub 로그인</h4>

          <TabView v-if="priority.length > 0">
            <TabPanel
              v-for="(method, index) in priority"
              :key="method"
              :header="getTabLabel(method)"
            >
              <div class="tw-p-4 tw-text-center tw-text-gray-600">
                {{ getMethodDescription(method) }}
              </div>
            </TabPanel>
          </TabView>

          <div v-else class="tw-text-center tw-text-gray-500 tw-py-8">
            활성화된 로그인 방식이 없습니다
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import Card from 'primevue/card'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import type { LoginMethod } from '~/types/models'

interface Props {
  priority: LoginMethod[]
}

defineProps<Props>()

const getTabLabel = (method: LoginMethod): string => {
  const labels: Record<LoginMethod, string> = {
    SSO: 'SSO',
    AD: 'AD',
    LOCAL: '일반'
  }
  return labels[method]
}

const getMethodDescription = (method: LoginMethod): string => {
  const descriptions: Record<LoginMethod, string> = {
    SSO: 'SSO (Single Sign-On) 인증을 사용합니다',
    AD: 'Active Directory 계정으로 로그인합니다',
    LOCAL: '사원ID와 비밀번호로 로그인합니다'
  }
  return descriptions[method]
}
</script>
