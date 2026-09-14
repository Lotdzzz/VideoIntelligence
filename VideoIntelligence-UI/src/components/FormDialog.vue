<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @update:model-value="handleVisibleChange"
  >
    <!-- 表单内容：由父页面通过默认插槽提供 el-form-item -->
    <el-form ref="formRef" :model="model" :rules="rules" :label-width="labelWidth">
      <slot />
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

// ==================== Props ====================
interface Props {
  /** 弹窗是否显示（v-model） */
  modelValue: boolean
  /** 弹窗标题 */
  title: string
  /** 表单数据模型 */
  model: Record<string, any>
  /** 表单校验规则 */
  rules?: FormRules
  /** 确定按钮加载状态 */
  loading?: boolean
  /** 弹窗宽度 */
  width?: string
  /** 标签宽度 */
  labelWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
  rules: undefined,
  loading: false,
  width: '600px',
  labelWidth: '90px',
})

// ==================== Emits ====================
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
}>()

// ==================== 表单实例 ====================
const formRef = ref<FormInstance>()

// ==================== 事件处理 ====================
function handleVisibleChange(value: boolean) {
  emit('update:modelValue', value)
  if (!value) {
    emit('cancel')
  }
}

function handleCancel() {
  emit('update:modelValue', false)
  emit('cancel')
}

/**
 * 点击确定：
 * 1. 有校验规则时先校验，未通过则不提交
 * 2. 校验通过后 emit confirm，由父页面调用接口
 */
async function handleConfirm() {
  if (!formRef.value) {
    emit('confirm')
    return
  }

  try {
    await formRef.value.validate()
    emit('confirm')
  } catch {
    // 校验失败，不提交
  }
}

// ==================== 暴露方法 ====================
defineExpose({
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields(),
  clearValidate: () => formRef.value?.clearValidate(),
})
</script>