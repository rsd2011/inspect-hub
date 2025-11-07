# DataTable Component (RealGrid Wrapper)

RealGrid2 기반의 고성능 데이터 그리드 컴포넌트입니다.

## Overview

**Location**: `shared/ui/organisms/DataTable.vue`

**Purpose**: RealGrid2를 래핑하여 프로젝트 표준에 맞는 일관된 그리드 인터페이스 제공

**Tech Stack**:
- RealGrid2 (상용 라이선스 필요)
- Vue 3 Composition API
- TypeScript

## Features

### Core Features
- ✅ Column configuration (정렬, 필터링, 그룹핑)
- ✅ Inline editing with validation
- ✅ Row selection (single/multiple)
- ✅ Pagination
- ✅ Export (Excel, CSV)
- ✅ Virtual scrolling (대용량 데이터)
- ✅ Fixed columns & rows
- ✅ Custom cell renderers
- ✅ Context menu
- ✅ Merged cells
- ✅ Soft delete

### Advanced Features
- ✅ Dynamic column configuration
- ✅ Column filtering & search
- ✅ Custom validators
- ✅ Cell styles & formatting
- ✅ Footer aggregation
- ✅ Copy/paste support
- ✅ Keyboard navigation
- ✅ Responsive design

## Basic Usage

```vue
<template>
  <DataTable
    :columns="columns"
    :data="data"
    :options="gridOptions"
    @row-click="handleRowClick"
    @cell-edit="handleCellEdit"
  />
</template>

<script setup lang="ts">
import type { GridColumn, GridOptions } from '~/shared/ui/organisms/DataTable/types'

const columns: GridColumn[] = [
  {
    name: 'id',
    fieldName: 'id',
    header: { text: 'ID' },
    width: 80,
    editable: false
  },
  {
    name: 'name',
    fieldName: 'name',
    header: { text: '이름' },
    width: 150,
    editable: true,
    required: true
  },
  {
    name: 'status',
    fieldName: 'status',
    header: { text: '상태' },
    width: 100,
    editor: {
      type: 'dropdown',
      domainOnly: true,
      textReadOnly: true,
      dropDownWhenClick: true,
      values: ['active', 'inactive'],
      labels: ['활성', '비활성']
    }
  }
]

const data = ref([
  { id: 1, name: 'John Doe', status: 'active' },
  { id: 2, name: 'Jane Smith', status: 'inactive' }
])

const gridOptions: GridOptions = {
  edit: {
    editable: true,
    commitByCell: true,
    commitWhenLeave: true
  },
  display: {
    rowHeight: 32
  }
}

function handleRowClick(row: any) {
  console.log('Row clicked:', row)
}

function handleCellEdit(event: CellEditEvent) {
  console.log('Cell edited:', event)
}
</script>
```

## Recommended Grid Configuration

Based on RealGrid2 best practices:

### Grid Options

```typescript
const recommendedOptions: GridOptions = {
  // Editing
  edit: {
    editable: true,
    commitByCell: true,        // 셀 편집 완료 시 즉시 저장
    commitWhenLeave: true,     // 그리드 외부 클릭 시 저장
    crossWhenExitLast: true,   // 마지막 열에서 Tab 시 다음 행으로
    exceptDataClickWhenButton: true  // 버튼 클릭 시 추가 이벤트 방지
  },

  // Display
  display: {
    rowHeight: 32,
    rowResizable: false,
    editItemMerging: true,     // 병합 셀 편집 시 시각적 분리 방지
  },

  // Sorting & Filtering
  sortMode: 'explicit',        // 자동 재정렬 방지
  filterMode: 'explicit',      // 자동 재필터링 방지

  // Fixed Columns/Rows
  fixedOptions: {
    colCount: 1,               // 좌측 고정 열 수
    rowCount: 0,               // 상단 고정 행 수
    exceptFromSorting: false   // 정렬 시 고정 행도 포함
  },

  // CheckBar
  checkBar: {
    visible: true,
    syncHeadCheck: true        // 전체 선택 시 헤더 체크
  },

  // Copy/Paste
  copy: {
    enabled: true,
    lookupDisplay: true        // 드롭다운 라벨 복사
  },
  paste: {
    enabled: true,
    convertLookupLabel: true,  // 라벨을 값으로 변환
    checkDomainOnly: true,     // 드롭다운 외 값 거부
    checkReadOnly: true,       // 읽기 전용 열 건너뛰기
    numberChars: [',']         // 천단위 구분자 처리
  },

  // Footer
  footer: {
    visible: false
  },

  // Soft Delete
  softDeleting: true,          // 논리적 삭제
  hideDeletedRows: false       // 삭제된 행 표시/숨김
}
```

## Column Configuration

### Text Column

```typescript
{
  name: 'description',
  fieldName: 'description',
  header: { text: '설명' },
  width: 200,
  editable: true,
  styleName: 'left-text',
  renderer: {
    type: 'text',
    showTooltip: true
  }
}
```

### Number Column

```typescript
{
  name: 'amount',
  fieldName: 'amount',
  header: { text: '금액' },
  width: 120,
  editable: true,
  dataType: 'number',
  numberFormat: '#,##0',
  styleName: 'right-number',
  footer: {
    expression: 'sum',
    numberFormat: '#,##0'
  }
}
```

### Date Column

```typescript
{
  name: 'createdAt',
  fieldName: 'createdAt',
  header: { text: '생성일' },
  width: 150,
  dataType: 'datetime',
  datetimeFormat: 'yyyy-MM-dd HH:mm:ss',
  editor: {
    type: 'date',
    datetimeFormat: 'yyyy-MM-dd'
  }
}
```

### Dropdown Column

```typescript
{
  name: 'status',
  fieldName: 'status',
  header: { text: '상태' },
  width: 100,
  editor: {
    type: 'dropdown',
    textReadOnly: true,      // 키보드 입력 방지
    domainOnly: true,        // 목록 값만 허용
    dropDownWhenClick: true, // 클릭 시 드롭다운 열기
    values: ['pending', 'approved', 'rejected'],
    labels: ['대기', '승인', '거부']
  },
  renderer: {
    type: 'text'
  },
  lookupDisplay: true
}
```

### Checkbox Column

```typescript
{
  name: 'active',
  fieldName: 'active',
  header: { text: '활성' },
  width: 80,
  editable: false,           // 필수! (체크박스 렌더러 요구사항)
  renderer: {
    type: 'check',
    trueValues: 'Y',
    falseValues: 'N',
    editable: true
  },
  styleCallback: (grid, cell) => {
    return cell.value === 'Y' ? 'rg-renderer-check-true' : 'rg-renderer-check-false'
  }
}
```

### Button Column

```typescript
{
  name: 'actions',
  fieldName: 'actions',
  header: { text: '작업' },
  width: 120,
  editable: false,
  renderer: {
    type: 'button',
    text: '상세보기'
  }
}
```

## Custom Renderers

### Creating Custom Renderer

```typescript
// shared/ui/organisms/DataTable/renderers/StatusRenderer.ts
import { RealGrid } from 'realgrid'

export class StatusRenderer extends RealGrid.CustomCellRendererImpl {
  get styleName() {
    return 'rg-renderer custom-status-renderer'
  }

  _doInitContent(parent: HTMLElement) {
    // DOM 요소 초기화
    this._span = document.createElement('span')
    this._span.className = 'status-badge'
    parent.appendChild(this._span)
  }

  _doClearContent(parent: HTMLElement) {
    // 정리
    parent.innerHTML = ''
  }

  render(grid: any, model: any, width: number, height: number) {
    const value = model.value
    const statusClass = this.getStatusClass(value)
    const statusText = this.getStatusText(value)

    this._span.className = `status-badge ${statusClass}`
    this._span.textContent = statusText
  }

  canEdit() {
    return false
  }

  canClick(event: MouseEvent) {
    return false
  }

  get showTooltip() {
    return true
  }

  tooltip(model: any, index: any) {
    return `상태: ${this.getStatusText(model.value)}`
  }

  private getStatusClass(value: string): string {
    const classMap: Record<string, string> = {
      'pending': 'status-pending',
      'approved': 'status-approved',
      'rejected': 'status-rejected'
    }
    return classMap[value] || ''
  }

  private getStatusText(value: string): string {
    const textMap: Record<string, string> = {
      'pending': '대기',
      'approved': '승인',
      'rejected': '거부'
    }
    return textMap[value] || value
  }

  private _span!: HTMLSpanElement
}
```

### Registering Custom Renderer

```typescript
// shared/ui/organisms/DataTable/setup.ts
import { RealGrid } from 'realgrid'
import { StatusRenderer } from './renderers/StatusRenderer'

export function setupCustomRenderers() {
  RealGrid.registerCustomRenderer('status_renderer', StatusRenderer)
}
```

### Using Custom Renderer

```typescript
const columns: GridColumn[] = [
  {
    name: 'status',
    fieldName: 'status',
    header: { text: '상태' },
    width: 120,
    renderer: {
      type: 'status_renderer'
    }
  }
]
```

## Cell Styling

### Static Style

```typescript
{
  name: 'priority',
  fieldName: 'priority',
  header: { text: '우선순위' },
  width: 100,
  styleName: 'priority-cell'
}
```

### Dynamic Style (Callback)

```typescript
{
  name: 'amount',
  fieldName: 'amount',
  header: { text: '금액' },
  width: 120,
  styleCallback: (grid, cell) => {
    if (cell.value < 0) {
      return 'negative-amount'
    } else if (cell.value > 1000000) {
      return 'high-amount'
    }
    return ''
  }
}
```

### CSS Styles

```scss
// DataTable styles
.rg-renderer {
  &.priority-cell {
    font-weight: bold;
    color: #e74c3c;
  }

  &.negative-amount {
    color: #e74c3c;
    font-weight: bold;
  }

  &.high-amount {
    color: #27ae60;
    font-weight: bold;
  }

  &.custom-status-renderer {
    .status-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;

      &.status-pending {
        background-color: #fff3cd;
        color: #856404;
      }

      &.status-approved {
        background-color: #d4edda;
        color: #155724;
      }

      &.status-rejected {
        background-color: #f8d7da;
        color: #721c24;
      }
    }
  }
}
```

## Validators

### Built-in Validators

```typescript
{
  name: 'email',
  fieldName: 'email',
  header: { text: '이메일' },
  width: 200,
  editable: true,
  validators: [{
    type: 'regexp',
    regexp: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: '올바른 이메일 형식이 아닙니다.'
  }]
}
```

### Custom Validators

```typescript
{
  name: 'age',
  fieldName: 'age',
  header: { text: '나이' },
  width: 80,
  editable: true,
  validators: [{
    type: 'custom',
    validationCallback: (grid, column, inserting, value, itemIndex) => {
      if (value < 0 || value > 150) {
        return {
          level: 'error',
          message: '나이는 0~150 사이여야 합니다.'
        }
      }
      return true
    }
  }]
}
```

## Events

```vue
<template>
  <DataTable
    :columns="columns"
    :data="data"
    @row-click="onRowClick"
    @row-dblclick="onRowDblClick"
    @cell-click="onCellClick"
    @cell-dblclick="onCellDblClick"
    @cell-edit="onCellEdit"
    @cell-button-click="onCellButtonClick"
    @selection-changed="onSelectionChanged"
    @sort-changed="onSortChanged"
    @filter-changed="onFilterChanged"
  />
</template>

<script setup lang="ts">
const onRowClick = (row: any) => {
  console.log('Row clicked:', row)
}

const onCellEdit = (event: CellEditEvent) => {
  console.log('Cell edited:', event)
  // Validation
  // API call to save
}

const onCellButtonClick = (event: CellButtonClickEvent) => {
  console.log('Button clicked:', event)
  // Navigate to detail page
  // Open modal
}
</script>
```

## Methods

```vue
<script setup lang="ts">
const tableRef = ref<InstanceType<typeof DataTable>>()

// Data manipulation
const refreshData = () => {
  tableRef.value?.setData(newData)
}

const appendRow = (row: any) => {
  tableRef.value?.appendRow(row)
}

const updateRow = (index: number, row: any) => {
  tableRef.value?.updateRow(index, row)
}

const deleteRow = (index: number) => {
  tableRef.value?.deleteRow(index)
}

// Selection
const getSelectedRows = () => {
  return tableRef.value?.getSelectedRows()
}

const setSelection = (indices: number[]) => {
  tableRef.value?.setSelection(indices)
}

// Export
const exportToExcel = () => {
  tableRef.value?.exportToExcel('data.xlsx')
}

const exportToCsv = () => {
  tableRef.value?.exportToCsv('data.csv')
}

// Grid control
const setColumns = (columns: GridColumn[]) => {
  tableRef.value?.setColumns(columns)
}

const setOptions = (options: GridOptions) => {
  tableRef.value?.setOptions(options)
}
</script>
```

## Performance Optimization

### Virtual Scrolling

RealGrid2는 기본적으로 virtual scrolling을 지원합니다. 대용량 데이터도 빠르게 렌더링됩니다.

### Lazy Loading

```typescript
const loadData = async (page: number, pageSize: number) => {
  const response = await api.get('/data', {
    params: { page, pageSize }
  })

  if (page === 1) {
    tableRef.value?.setData(response.data)
  } else {
    tableRef.value?.appendRows(response.data)
  }
}
```

### Minimize Redraws

```typescript
// Bad: Multiple updates trigger multiple redraws
tableRef.value?.updateRow(0, row1)
tableRef.value?.updateRow(1, row2)
tableRef.value?.updateRow(2, row3)

// Good: Batch update
tableRef.value?.beginUpdate()
tableRef.value?.updateRow(0, row1)
tableRef.value?.updateRow(1, row2)
tableRef.value?.updateRow(2, row3)
tableRef.value?.endUpdate()
```

## Best Practices

### DO ✅

- Use `commitByCell: true` for immediate saves
- Set `sortMode` and `filterMode` to `'explicit'` to prevent unexpected re-sorting
- Enable `editItemMerging` for merged cell editing
- Use `softDeleting` for recoverable deletions
- Register custom renderers globally once
- Batch updates with `beginUpdate()`/`endUpdate()`
- Use TypeScript for type safety

### DON'T ❌

- Don't use custom renderers for input-heavy scenarios (use editors instead)
- Don't render areas larger than row height
- Don't forget to set `editable: false` for checkbox renderers
- Don't manually manipulate grid DOM
- Don't create new renderer instances for each cell
- Don't skip validation on paste operations

## Troubleshooting

### Issue: Merged cells split during editing
**Solution**: Enable `displayOptions.editItemMerging = true`

### Issue: Pasted data not converting dropdown labels
**Solution**: Set `pasteOptions.convertLookupLabel = true`

### Issue: Checkbox not clickable
**Solution**: Ensure column has `editable: false` set

### Issue: Custom renderer not showing
**Solution**: Verify renderer is registered before grid initialization

### Issue: Performance degradation with large datasets
**Solution**:
- Enable virtual scrolling (default)
- Batch updates with beginUpdate/endUpdate
- Consider server-side pagination

## License

RealGrid2는 상용 라이선스가 필요합니다. 프로젝트에 맞는 라이선스를 구매하고 설정하세요.

## References

- [RealGrid2 Official Documentation](https://docs.realgrid.com/)
- [Recommended Options](https://docs.realgrid.com/guides/tip/recommended-options)
- [Custom Renderers](https://docs.realgrid.com/guides/renderer/class-custom-renderer)
- [API Reference](https://docs.realgrid.com/api/)
