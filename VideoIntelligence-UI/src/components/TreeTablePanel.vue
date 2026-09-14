<template>
  <div class="tree-table-panel">
    <!-- ==================== 工具栏 ==================== -->
    <div class="tree-table-panel__toolbar">
      <div class="tree-table-panel__toolbar-left">
        <el-button v-if="showAdd" type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button v-if="showEdit" type="warning" :icon="Edit" :disabled="selectedRows.length !== 1" @click="handleEdit">
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
        <el-button v-if="showExpandToggle" @click="toggleExpandAll">
          {{ expandAll ? '全部折叠' : '全部展开' }}
        </el-button>
      </div>

      <!-- 右侧扩展工具栏插槽（如导入/导出等） -->
      <div class="tree-table-panel__toolbar-right">
        <slot name="toolbar" />
      </div>
    </div>

    <!-- ==================== 树形数据表格 ==================== -->
    <el-table
      :key="tableKey"
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :tree-props="treeProps"
      :default-expand-all="expandAll"
      border
      stripe
      class="tree-table-panel__table"
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
  /** 树形数据（顶层为根节点数组，子级挂在 children 字段上） */
  data: Record<string, any>[]
  /** 表格加载状态 */
  loading?: boolean
  /** 是否显示新增按钮 */
  showAdd?: boolean
  /** 是否显示修改按钮 */
  showEdit?: boolean
  /** 是否显示删除按钮 */
  showDelete?: boolean
  /** 是否显示全部展开/折叠按钮 */
  showExpandToggle?: boolean
  /** 列配置 */
  columns: TableColumn[]
  /** 行唯一标识字段 */
  rowKey: string
  /** 树形表格 children / hasChildren 字段映射 */
  treeProps?: { children?: string; hasChildren?: string }
  /** 是否默认展开所有节点 */
  defaultExpandAll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showAdd: true,
  showEdit: true,
  showDelete: true,
  showExpandToggle: true,
  treeProps: () => ({ children: 'children', hasChildren: 'hasChildren' }),
  defaultExpandAll: true,
})

// ==================== Emits ====================
const emit = defineEmits<{
  add: []
  modify: [row: any]
  remove: [rows: any[]]
  selectionChange: [rows: any[]]
}>()

// ==================== 选中行 ====================
const selectedRows = ref<Record<string, any>[]>([])

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows
  emit('selectionChange', rows)
}

// ==================== 展开/折叠 ====================
const tableKey = ref(0)
const expandAll = ref(props.defaultExpandAll)

function toggleExpandAll() {
  expandAll.value = !expandAll.value
  tableKey.value++
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
</script>

<style scoped>
.tree-table-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tree-table-panel__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.tree-table-panel__toolbar-left,
.tree-table-panel__toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tree-table-panel__table {
  width: 100%;
}
</style>