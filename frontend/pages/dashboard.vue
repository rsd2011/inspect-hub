<template>
  <div class="tw-min-h-screen tw-bg-gray-50">
    <AppHeader
      title="Inspect Hub"
      :show-menu-toggle="true"
      :show-notifications="true"
      :notification-count="5"
      @toggle-sidebar="sidebarOpen = !sidebarOpen"
    />
    
    <div class="tw-flex">
      <AppSidebar
        v-model:open="sidebarOpen"
        v-model:collapsed="sidebarCollapsed"
        app-version="v1.0.0"
      />
      
      <main class="tw-flex-1 tw-p-6">
        <div class="tw-mb-6">
          <h1 class="tw-text-2xl tw-font-bold tw-text-gray-800">대시보드</h1>
          <p class="tw-text-gray-600 tw-mt-1">AML 통합 컴플라이언스 모니터링</p>
        </div>

        <!-- Stats Cards -->
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-6 tw-mb-6">
          <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600">STR 의심거래</p>
                <p class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mt-2">142</p>
                <p class="tw-text-xs tw-text-green-600 tw-mt-2">+12% 전월 대비</p>
              </div>
              <div class="tw-w-12 tw-h-12 tw-bg-blue-100 tw-rounded-full tw-flex tw-items-center tw-justify-center">
                <i class="pi pi-exclamation-circle tw-text-2xl tw-text-blue-600" />
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600">CTR 고액거래</p>
                <p class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mt-2">89</p>
                <p class="tw-text-xs tw-text-red-600 tw-mt-2">-5% 전월 대비</p>
              </div>
              <div class="tw-w-12 tw-h-12 tw-bg-yellow-100 tw-rounded-full tw-flex tw-items-center tw-justify-center">
                <i class="pi pi-money-bill tw-text-2xl tw-text-yellow-600" />
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600">WLF 감시대상</p>
                <p class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mt-2">23</p>
                <p class="tw-text-xs tw-text-green-600 tw-mt-2">+8% 전월 대비</p>
              </div>
              <div class="tw-w-12 tw-h-12 tw-bg-red-100 tw-rounded-full tw-flex tw-items-center tw-justify-center">
                <i class="pi pi-shield tw-text-2xl tw-text-red-600" />
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600">처리 대기</p>
                <p class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mt-2">37</p>
                <p class="tw-text-xs tw-text-gray-600 tw-mt-2">진행중인 케이스</p>
              </div>
              <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center">
                <i class="pi pi-clock tw-text-2xl tw-text-purple-600" />
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Cases Table -->
        <div class="tw-bg-white tw-rounded-lg tw-shadow">
          <div class="tw-p-6 tw-border-b tw-border-gray-200">
            <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800">최근 케이스</h2>
          </div>
          <div class="tw-p-6">
            <table class="tw-w-full">
              <thead>
                <tr class="tw-border-b tw-border-gray-200">
                  <th class="tw-text-left tw-pb-3 tw-text-sm tw-font-medium tw-text-gray-600">케이스 ID</th>
                  <th class="tw-text-left tw-pb-3 tw-text-sm tw-font-medium tw-text-gray-600">유형</th>
                  <th class="tw-text-left tw-pb-3 tw-text-sm tw-font-medium tw-text-gray-600">고객명</th>
                  <th class="tw-text-left tw-pb-3 tw-text-sm tw-font-medium tw-text-gray-600">상태</th>
                  <th class="tw-text-left tw-pb-3 tw-text-sm tw-font-medium tw-text-gray-600">생성일</th>
                </tr>
              </thead>
              <tbody>
                <tr class="tw-border-b tw-border-gray-100">
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">STR-2025-0001</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">의심거래</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">홍길동</td>
                  <td class="tw-py-3"><span class="tw-px-2 tw-py-1 tw-bg-yellow-100 tw-text-yellow-800 tw-text-xs tw-rounded">검토중</span></td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-600">2025-11-07</td>
                </tr>
                <tr class="tw-border-b tw-border-gray-100">
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">CTR-2025-0015</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">고액거래</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">김철수</td>
                  <td class="tw-py-3"><span class="tw-px-2 tw-py-1 tw-bg-green-100 tw-text-green-800 tw-text-xs tw-rounded">승인완료</span></td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-600">2025-11-06</td>
                </tr>
                <tr class="tw-border-b tw-border-gray-100">
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">WLF-2025-0008</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">감시대상</td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-800">이영희</td>
                  <td class="tw-py-3"><span class="tw-px-2 tw-py-1 tw-bg-red-100 tw-text-red-800 tw-text-xs tw-rounded">긴급</span></td>
                  <td class="tw-py-3 tw-text-sm tw-text-gray-600">2025-11-07</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Dashboard Page
 * 
 * Main dashboard showing AML compliance monitoring overview.
 */

definePageMeta({
  middleware: 'auth',
})

const sidebarOpen = ref(false)
const sidebarCollapsed = ref(false)

// Check authentication
const authStore = useAuthStore()
if (!authStore.isAuthenticated) {
  navigateTo('/login')
}
</script>

<style scoped>
/* Dashboard specific styles */
</style>
