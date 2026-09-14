<template>
  <div class="search-panel">
    <!-- 左侧：搜索条件表单，具体搜索项由使用方通过默认插槽传入 -->
    <el-form inline class="search-panel__form">
      <slot />
      <!-- 查询 / 重置按钮 -->
      <el-form-item class="search-panel__buttons">
        <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 右侧：操作按钮插槽（如新增、导出等） -->
    <div class="search-panel__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { Search, Refresh } from '@element-plus/icons-vue'

// ==================== Props ====================
interface Props {
  /** 搜索按钮 loading 状态，用于防止重复提交 */
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

// ==================== Emits ====================
const emit = defineEmits<{
  /** 点击“搜索”按钮 */
  search: []
  /** 点击“重置”按钮 */
  reset: []
}>()

// ==================== 事件处理 ====================
function handleSearch() {
  emit('search')
}

function handleReset() {
  emit('reset')
}
</script>

<style scoped>
.search-panel {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.search-panel__form {
  flex: 1;
  min-width: 0;
}

.search-panel__buttons {
  margin-right: 0;
}

.search-panel__actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>