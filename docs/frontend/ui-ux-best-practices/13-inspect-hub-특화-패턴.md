# 13. Inspect-Hub 특화 패턴

## Snapshot 기반 정책 관리

**모든 정책/기준 데이터는 스냅샷 구조로 관리:**

```vue
<template>
  <Panel header="정책 버전 관리">
    <div class="tw-flex tw-items-center tw-gap-4">
      <Dropdown
        v-model="selectedSnapshot"
        :options="snapshots"
        optionLabel="versionName"
        placeholder="버전 선택"
      />
      <Badge :value="selectedSnapshot.status" :severity="getStatusSeverity(selectedSnapshot.status)" />
      <span class="tw-text-sm tw-text-gray-600">
        배포일: {{ formatDate(selectedSnapshot.deployedAt) }}
      </span>
    </div>

    <div class="tw-mt-4">
      <Button label="이전 버전으로 롤백" severity="warn" @click="confirmRollback" />
    </div>
  </Panel>
</template>
```

## Maker-Checker (SoD) 프로세스

**케이스 생성/조사/승인 프로세스:**

```vue
<template>
  <Panel header="승인 프로세스">
    <Steps :model="approvalSteps" :activeStep="currentStep" />

    <div class="tw-mt-4">
      <div v-if="canApprove">
        <Button label="승인" severity="success" @click="approve" />
        <Button label="반려" severity="danger" @click="reject" />
      </div>
      <div v-else-if="canRequest">
        <Button label="승인 요청" severity="primary" @click="requestApproval" />
      </div>
      <Message v-else severity="info">
        현재 단계에서는 승인/반려를 할 수 없습니다.
      </Message>
    </div>
  </Panel>
</template>

<script setup lang="ts">
const perm = usePermissionManager()
const canApprove = computed(() =>
  perm.hasRole('ROLE_APPROVER') && currentStep.value === 'pending_approval'
)
const canRequest = computed(() =>
  perm.hasRole('ROLE_ANALYST') && currentStep.value === 'draft'
)
</script>
```

## 감사 로깅 (100% Audit Trail)

**모든 중요 액션에 감사 로그 자동 기록:**

```typescript
// shared/lib/audit-logger/index.ts
export class AuditLogger {
  async log(action: string, target: string, beforeData?: any, afterData?: any) {
    await api.post('/api/v1/audit/logs', {
      action,          // 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT'
      target,          // 'CASE', 'POLICY', 'USER'
      targetId,        // 'CASE-001'
      beforeJson: beforeData ? JSON.stringify(beforeData) : null,
      afterJson: afterData ? JSON.stringify(afterData) : null,
      userId: sessionManager.getUserId(),
      timestamp: new Date().toISOString()
    })
  }
}
```

## RealGrid2 대용량 그리드

**대용량 데이터 테이블 (10만 건 이상):**

```vue
<template>
  <RealGrid
    :columns="columns"
    :dataSource="dataSource"
    :options="{
      checkBar: { visible: true },
      stateBar: { visible: true },
      sortMode: 'explicit',
      filterMode: 'immediate',
      export: { showProgress: true }
    }"
    @cellClicked="handleCellClick"
  />
</template>
```

---
