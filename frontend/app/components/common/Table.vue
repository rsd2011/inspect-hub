<template>
  <div class="tw-overflow-x-auto tw-rounded-lg tw-border tw-border-gray-200">
    <table class="tw-min-w-full tw-divide-y tw-divide-gray-200">
      <thead class="tw-bg-gray-50">
        <tr>
          <th
            v-for="column in columns"
            :key="column.key"
            scope="col"
            class="tw-px-6 tw-py-3 tw-text-left tw-text-xs tw-font-medium tw-text-gray-500 tw-uppercase tw-tracking-wider"
          >
            {{ column.label }}
          </th>
        </tr>
      </thead>
      <tbody class="tw-bg-white tw-divide-y tw-divide-gray-200">
        <tr
          v-for="(row, index) in data"
          :key="index"
          class="hover:tw-bg-gray-50 tw-transition-colors"
        >
          <td
            v-for="column in columns"
            :key="column.key"
            class="tw-px-6 tw-py-4 tw-whitespace-nowrap tw-text-sm tw-text-gray-900"
          >
            <slot :name="`cell-${column.key}`" :row="row" :value="row[column.key]">
              {{ row[column.key] }}
            </slot>
          </td>
        </tr>
        <tr v-if="!data || data.length === 0">
          <td :colspan="columns.length" class="tw-px-6 tw-py-4 tw-text-center tw-text-sm tw-text-gray-500">
            {{ emptyText }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
interface Column {
  key: string
  label: string
}

interface Props {
  columns: Column[]
  data: Record<string, any>[]
  emptyText?: string
}

withDefaults(defineProps<Props>(), {
  emptyText: 'No data available',
})
</script>
