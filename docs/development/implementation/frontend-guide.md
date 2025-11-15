## 🎨 Frontend UI Design - System Configuration

### 화면 구조

**페이지 경로:** `/admin/system-settings`

**권한:** ROLE_ADMIN 필수

**레이아웃:**
```
┌──────────────────────────────────────────────────────────┐
│  System Settings                            [Save All]   │
├──────────────────────────────────────────────────────────┤
│  ┌────────┬────────┬────────┬──────┬──────┬──────────┐  │
│  │ Login  │Password│Session │  IP  │Account│Advanced │  │
│  └────────┴────────┴────────┴──────┴──────┴──────────┘  │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  🔐 Login Methods                                        │
│  ┌─────────────────────────────────────────────────────┐│
│  │ ☑ LOCAL Login              [Enabled]  [Configure] ││
│  │ ☐ Active Directory (AD)    [Disabled] [Configure] ││
│  │ ☐ Single Sign-On (SSO)     [Disabled] [Configure] ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  🎯 Login Priority                                       │
│  ┌─────────────────────────────────────────────────────┐│
│  │ 1. SSO  [↑] [↓]                                    ││
│  │ 2. AD   [↑] [↓]                                    ││
│  │ 3. LOCAL [↑] [↓]                                   ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  [Test AD Connection]  [Test SSO Authorization]         │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

### 컴포넌트 구조 (FSD + Atomic Design)

**pages/admin/system-settings/ui/SystemSettingsPage.vue:**
```vue
<template>
  <div class="tw-p-6">
    <PageHeader 
      title="System Settings" 
      subtitle="Configure global system policies and authentication"
    />
    
    <PrimeTabView v-model:activeIndex="activeTab" class="tw-mt-4">
      <PrimeTabPanel header="Login">
        <LoginSettingsPanel />
      </PrimeTabPanel>
      
      <PrimeTabPanel header="Password">
        <PasswordSettingsPanel />
      </PrimeTabPanel>
      
      <PrimeTabPanel header="Session">
        <SessionSettingsPanel />
      </PrimeTabPanel>
      
      <PrimeTabPanel header="IP">
        <IpSettingsPanel />
      </PrimeTabPanel>
      
      <PrimeTabPanel header="Account">
        <AccountSettingsPanel />
      </PrimeTabPanel>
      
      <PrimeTabPanel header="Advanced">
        <AdvancedSettingsPanel />
      </PrimeTabPanel>
    </PrimeTabView>
    
    <div class="tw-flex tw-justify-end tw-gap-3 tw-mt-6">
      <Button label="Reset All to Defaults" severity="danger" outlined @click="resetAll" />
      <Button label="Reload Cache" severity="secondary" outlined @click="reloadCache" />
      <Button label="Save All Changes" severity="success" @click="saveAll" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useSystemConfigStore } from '~/features/system-config/model/system-config.store'

const systemConfigStore = useSystemConfigStore()
const activeTab = ref(0)

const saveAll = async () => {
  await systemConfigStore.saveAll()
  // Show success toast
}

const resetAll = async () => {
  // Show confirmation dialog
  await systemConfigStore.resetAll()
}

const reloadCache = async () => {
  await systemConfigStore.reloadCache()
  // Show success toast
}
</script>
```

**features/system-config/ui/LoginSettingsPanel.vue:**
```vue
<template>
  <div class="tw-space-y-6">
    <!-- Login Methods -->
    <SettingSection title="Login Methods" icon="pi pi-sign-in">
      <div class="tw-space-y-4">
        <SettingToggle
          label="LOCAL Login"
          description="Username and password authentication"
          v-model="config['auth.login.local.enabled']"
        >
          <template #extra>
            <Button label="Configure" size="small" outlined />
          </template>
        </SettingToggle>
        
        <SettingToggle
          label="Active Directory (AD)"
          description="LDAP-based authentication"
          v-model="config['auth.login.ad.enabled']"
        >
          <template #extra>
            <Button label="Configure" size="small" outlined @click="openAdConfig" />
            <Button label="Test Connection" size="small" outlined @click="testAdConnection" />
          </template>
        </SettingToggle>
        
        <SettingToggle
          label="Single Sign-On (SSO)"
          description="OAuth2/OIDC authentication"
          v-model="config['auth.login.sso.enabled']"
        >
          <template #extra>
            <Button label="Configure" size="small" outlined @click="openSsoConfig" />
            <Button label="Test Authorization" size="small" outlined @click="testSsoAuth" />
          </template>
        </SettingToggle>
      </div>
    </SettingSection>
    
    <!-- Login Priority -->
    <SettingSection title="Login Priority" icon="pi pi-sort-alt">
      <LoginPriorityList v-model="loginPriority" />
    </SettingSection>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSystemConfigStore } from '~/features/system-config/model/system-config.store'

const systemConfigStore = useSystemConfigStore()
const config = computed(() => systemConfigStore.configs)

const loginPriority = computed({
  get: () => config.value['auth.login.priority'].split(','),
  set: (value) => systemConfigStore.updateConfig('auth.login.priority', value.join(','))
})

const testAdConnection = async () => {
  // Call test API
}
</script>
```

**shared/ui/molecules/SettingToggle.vue:**
```vue
<template>
  <div class="tw-flex tw-items-center tw-justify-between tw-p-4 tw-border tw-rounded">
    <div class="tw-flex-1">
      <div class="tw-font-semibold">{{ label }}</div>
      <div class="tw-text-sm tw-text-gray-600">{{ description }}</div>
    </div>
    
    <div class="tw-flex tw-items-center tw-gap-3">
      <slot name="extra" />
      
      <PrimeInputSwitch 
        :modelValue="modelValue" 
        @update:modelValue="$emit('update:modelValue', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  description: string
  modelValue: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>
```

### Pinia Store

**features/system-config/model/system-config.store.ts:**
```typescript
import { defineStore } from 'pinia'
import { useApiClient } from '~/shared/api/client'

export const useSystemConfigStore = defineStore('systemConfig', () => {
  const api = useApiClient()
  
  const configs = ref<Record<string, any>>({})
  const originalConfigs = ref<Record<string, any>>({})
  const isDirty = ref(false)
  
  const fetchAll = async () => {
    const response = await api.get('/api/v1/admin/system-config')
    configs.value = response.data
    originalConfigs.value = { ...response.data }
  }
  
  const updateConfig = (key: string, value: any) => {
    configs.value[key] = value
    isDirty.value = true
  }
  
  const saveAll = async () => {
    const changes = Object.keys(configs.value).filter(
      key => configs.value[key] !== originalConfigs.value[key]
    )
    
    for (const key of changes) {
      await api.put(`/api/v1/admin/system-config/${key}`, {
        value: configs.value[key]
      })
    }
    
    await fetchAll()
    isDirty.value = false
  }
  
  const resetAll = async () => {
    await api.post('/api/v1/admin/system-config/reset')
    await fetchAll()
  }
  
  const reloadCache = async () => {
    await api.post('/api/v1/admin/system-config/reload-cache')
  }
  
  return {
    configs,
    isDirty,
    fetchAll,
    updateConfig,
    saveAll,
    resetAll,
    reloadCache
  }
})
```

### UI/UX 요구사항

**1. 실시간 변경 감지:**
- 모든 설정 변경 시 isDirty 플래그 true
- 페이지 떠날 때 "저장되지 않은 변경사항" 경고

**2. 검증:**
- 숫자 입력 필드: 최소/최대값 검증
- JSON 입력: 실시간 JSON 파싱 검증
- CIDR 입력: IP 범위 형식 검증

**3. 도움말:**
- 각 설정 항목에 ?아이콘 hover시 상세 설명 표시
- "기본값" 버튼으로 Properties 값 확인 가능

**4. 변경 이력:**
- 각 설정 항목 옆 "History" 아이콘
- 클릭 시 모달로 변경 이력 표시 (누가, 언제, 이전값 → 새값)

**5. 연동 테스트:**
- AD Connection Test: LDAP 연결 테스트 + 결과 표시
- SSO Authorization Test: OAuth2 흐름 테스트 + 결과 표시
- 테스트 성공/실패 명확한 피드백

**6. 일괄 작업:**
- "Save All Changes" 버튼: 모든 변경사항 한번에 저장
- "Reset All to Defaults" 버튼: Properties 기본값으로 리셋
- "Export Config" 버튼: JSON 파일로 내보내기
- "Import Config" 버튼: JSON 파일에서 가져오기

---

