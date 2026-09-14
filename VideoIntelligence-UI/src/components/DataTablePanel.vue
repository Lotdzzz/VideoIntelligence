<template>
  <div class="data-table-panel">
    <!-- ==================== 工具栏 ==================== -->
    <div class="data-table-panel__toolbar">
      <div class="data-table-panel__toolbar-left">
        <el-button v-if="showAdd" type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button
          v-if="showEdit"
          type="warning"
          :icon="Edit"
          :disabled="selectedRows.length !== 1"
          @click="handleEdit"
        >
          修改
        </el-button>
        <el-button
          v-if="showDelete"
          type="danger"
          :icon="Delete"
          :disabled="selectedRows.length === 0"
          @click="handleDelete"
        >
          删除
        </el-button>
      </div>

      <!-- 右侧扩展工具栏插槽（如导入/导出等） -->
      <div class="data-table-panel__toolbar-right">
        <slot name="toolbar" />
      </div>
    </div>

    <!-- ==================== 数据表格 ==================== -->
    <el-table
      v-loading="loading"
      :data="data"
      border
      stripe
      class="data-table-panel__table"
      @selection-change="handleSelectionChange"
    >
      <!-- 多选列 -->
      <el-table-column type="selection" width="50" align="center" />

      <!-- 动态列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop || column.label"
        :label="column.label"
        :prop="column.prop"
        :width="column.width"
        :min-width="column.minWidth"
        :align="column.align ?? 'left'"
        :fixed="column.fixed"
        :show-overflow-tooltip="column.showOverflowTooltip ?? true"
      >
        <template v-if="column.slot" #default="scope">
          <slot :name="column.slot" :row="scope.row" :index="scope.$index" />
        </template>
      </el-table-column>

      <!-- 行操作列 -->
      <el-table-column v-if="$slots.action" label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <slot name="action" :row="scope.row" :index="scope.$index" />
        </template>
      </el-table-column>
    </el-table>

    <!-- ==================== 分页器 ==================== -->
    <div class="data-table-panel__pagination">
      <el-pagination
        :current-page="current"
        :page-size="size"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        background
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { ref } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

// ==================== 类型定义 ====================
/** 表格列配置 */
interface TableColumn {
  /** 列标题 */
  label: string
  /** 列字段名（对应行数据的 key） */
  prop?: string
  /** 列宽 */
  width?: number | string
  /** 最小列宽 */
  minWidth?: number | string
  /** 对齐方式 */
  align?: 'left' | 'center' | 'right'
  /** 固定列 */
  fixed?: boolean | 'left' | 'right'
  /** 是否超长显示省略号提示 */
  showOverflowTooltip?: boolean
  /** 自定义插槽名称（用于渲染单元格内容） */
  slot?: string
}

// ==================== Props ====================
interface Props {
  /** 表格行数据 */
  data: Record<string, any>[]
  /** 表格加载状态 */
  loading?: boolean
  /** 总记录数 */
  total?: number
  /** 当前页码 */
  current?: number
  /** 每页条数 */
  size?: number
  /** 每页条数选项 */
  pageSizes?: number[]
  /** 列配置 */
  columns: TableColumn[]
  /** 分页布局 */
  paginationLayout?: string
  /** 是否显示新增按钮 */
  showAdd?: boolean
  /** 是否显示修改按钮 */
  showEdit?: boolean
  /** 是否显示删除按钮 */
  showDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  total: 0,
  current: 1,
  size: 10,
  pageSizes: () => [10, 20, 50, 100],
  showAdd: true,
  showEdit: true,
  showDelete: true,
})

// ==================== Emits ====================
const emit = defineEmits<{
  add: []
  modify: [row: any]
  remove: [rows: any[]]
  selectionChange: [rows: any[]]
  currentChange: [page: number]
  sizeChange: [size: number]
  paginationChange: []
}>()

// ==================== 选中行 ====================
const selectedRows = ref<Record<string, any>[]>([])

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows
  emit('selectionChange', rows)
}

// ==================== 工具栏事件 ====================
function handleAdd() {
  emit('add')
}

function handleEdit() {
  if (selectedRows.value.length !== 1) return
  emit('modify', selectedRows.value[0])
}

function handleDelete() {
  if (selectedRows.value.length === 0) return
  emit('remove', [...selectedRows.value])
}

// ==================== 分页事件 ====================
function handleCurrentChange(page: number) {
  emit('currentChange', page)
  emit('paginationChange')
}

function handleSizeChange(size: number) {
  emit('sizeChange', size)
  emit('paginationChange')
}
</script>

<style scoped>
.data-table-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.data-table-panel__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.data-table-panel__toolbar-left,
.data-table-panel__toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.data-table-panel__table {
  width: 100%;
}

.data-table-panel__pagination {
  display: flex;
  justify-content: flex-end;
}
</style>