<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="dialogWidth"
    class="resizable-dialog"
    :style="dialogHeight ? { height: dialogHeight } : undefined"
    draggable
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @update:model-value="handleVisibleChange"
  >
    <slot />

    <span class="resizable-dialog__handle" @mousedown="onResizeStart" />
  </el-dialog>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { ref, watch } from 'vue'

// ==================== Props ====================
interface Props {
  /** 弹窗是否显示（v-model） */
  modelValue: boolean
  /** 弹窗标题 */
  title: string
  /** 弹窗初始宽度 */
  width?: string
}

const props = withDefaults(defineProps<Props>(), {
  width: '600px',
})

// ==================== Emits ====================
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// ==================== 弹窗尺寸（可移动 + 可缩放） ====================
const dialogWidth = ref(props.width)
const dialogHeight = ref<string | undefined>(undefined)

// 打开时重置回默认尺寸
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      dialogWidth.value = props.width
      dialogHeight.value = undefined
    }
  },
)

function handleVisibleChange(value: boolean) {
  emit('update:modelValue', value)
}

// 右下角手柄按下，开始拖拽缩放
function onResizeStart(e: MouseEvent) {
  const handle = e.currentTarget as HTMLElement
  const dialog = handle.closest('.el-dialog') as HTMLElement | null
  if (!dialog) return

  const startX = e.clientX
  const startY = e.clientY
  const startWidth = dialog.offsetWidth
  const startHeight = dialog.offsetHeight

  const MIN_WIDTH = 300
  const MIN_HEIGHT = 200

  const onMove = (ev: MouseEvent) => {
    const newWidth = Math.max(MIN_WIDTH, startWidth + ev.clientX - startX)
    const newHeight = Math.max(MIN_HEIGHT, startHeight + ev.clientY - startY)
    dialogWidth.value = `${newWidth}px`
    dialogHeight.value = `${newHeight}px`
  }

  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.userSelect = ''
    document.body.style.cursor = ''
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'nwse-resize'

  e.preventDefault()
  e.stopPropagation()
}
</script>

<style scoped>
.resizable-dialog__handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 16px;
  height: 16px;
  cursor: nwse-resize;
  z-index: 10;
}

.resizable-dialog__handle::after {
  content: '';
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 8px;
  height: 8px;
  border-right: 2px solid var(--el-border-color);
  border-bottom: 2px solid var(--el-border-color);
}
</style>

<style>
/* 可缩放弹窗布局：纵向排列，内容区可滚动（弹窗被 teleport 到 body，需全局样式） */
.resizable-dialog.el-dialog {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.resizable-dialog .el-dialog__header {
  flex-shrink: 0;
}

.resizable-dialog .el-dialog__body {
  flex: 1;
  overflow: auto;
  min-height: 0;
}

.resizable-dialog .el-dialog__footer {
  flex-shrink: 0;
}
</style>
