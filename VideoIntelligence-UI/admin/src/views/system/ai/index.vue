<template>
  <div class="ai-prompt-page">
    <!-- 顶部搜索框架 -->
    <SearchPanel :loading="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="提示词名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入提示词名称"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="Key">
        <el-input
          v-model="queryParams.key"
          placeholder="请输入Key"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="启用" :value="0" />
          <el-option label="禁用" :value="1" />
        </el-select>
      </el-form-item>
    </SearchPanel>

    <!-- 新增/修改弹窗 -->
    <FormDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :model="formData"
      :rules="formRules"
      :loading="submitting"
      @confirm="handleSubmit"
      @cancel="handleCancel"
    >
      <el-form-item label="提示词名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入提示词名称" />
      </el-form-item>
      <el-form-item label="Key" prop="key">
        <el-input v-model="formData.key" placeholder="请输入Key" />
      </el-form-item>
      <el-form-item label="提示词内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="8"
          placeholder="请输入提示词内容（支持 Markdown）"
        />
      </el-form-item>
      <el-form-item label="模型" prop="model">
        <el-input v-model="formData.model" placeholder="请输入模型，如 gpt-4o" />
      </el-form-item>
      <el-form-item label="模型参数">
        <div class="model-params">
          <div v-for="(pair, index) in modelParamsList" :key="index" class="model-params__row">
            <el-input v-model="pair.key" placeholder="参数名" class="model-params__key" />
            <el-input v-model="pair.value" placeholder="参数值" class="model-params__value" />
            <el-button link type="danger" @click="removeParamRow(index)">删除</el-button>
          </div>
          <el-button link type="primary" @click="addParamRow">+ 添加参数</el-button>
        </div>
      </el-form-item>
      <el-form-item label="版本号" prop="version">
        <el-select v-model="formData.version" placeholder="请选择版本号">
          <el-option v-for="v in versionOptions" :key="v" :label="String(v)" :value="v" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="启用" :value="0" />
          <el-option label="禁用" :value="1" />
        </el-select>
      </el-form-item>
    </FormDialog>

    <!-- 数据内容框架 -->
    <DataTablePanel
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :current="queryParams.pageNum"
      :size="queryParams.pageSize"
      @add="handleAdd"
      @modify="handleEdit"
      @remove="handleDelete"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
      @pagination-change="loadAiPromptData"
    >
      <!-- 状态列自定义渲染 -->
      <template #status="{ row }">
        <el-tag :type="row.status === 0 ? 'success' : 'danger'">
          {{ row.status === 0 ? '启用' : '禁用' }}
        </el-tag>
      </template>

      <!-- 行内操作 -->
      <template #action="{ row }">
        <el-button link type="primary" @click="handleView(row)">查看</el-button>
        <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
        <el-button link type="danger" @click="handleDelete([row])">删除</el-button>
      </template>
    </DataTablePanel>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" :title="viewTitle" width="600px">
      <div class="content-view">{{ viewContent }}</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormRules } from 'element-plus'
import SearchPanel from '@/components/SearchPanel.vue'
import DataTablePanel from '@/components/DataTablePanel.vue'
import FormDialog from '@/components/FormDialog.vue'
import {
  listAiPrompts,
  getAiPromptById,
  addAiPrompt,
  updateAiPrompt,
  deleteAiPrompt,
  batchDeleteAiPrompts,
} from '@/api/ai'
import type { PageResult } from '@/types/result'
import type { AiPromptVO } from '@/types/ai/aiPromptVO'
import type { AiPromptDTO } from '@/types/ai/aiPromptDTO'

// ==================== 搜索加载状态 ====================
const loading = ref(false)

// ==================== 查询参数 ====================
const queryParams = reactive({
  name: '',
  key: '',
  status: '' as number | '',
  pageNum: 1,
  pageSize: 10,
})

// ==================== 表格数据 ====================
const tableData = ref<AiPromptVO[]>([])
const total = ref(0)

// ==================== 新增/修改弹窗 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)

// 版本号下拉选项（1~10）
const versionOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

const defaultFormData = (): AiPromptDTO => ({
  name: '',
  key: '',
  content: '',
  model: '',
  modelParams: '',
  version: 1,
  status: 0,
})

const formData = ref<AiPromptDTO>(defaultFormData())

const formRules: FormRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入提示词名称' }],
  content: [{ required: true, trigger: 'blur', message: '请输入提示词内容' }],
}

// ==================== 模型参数（键值对编辑器） ====================
interface ModelParamPair {
  key: string
  value: string
}

const modelParamsList = ref<ModelParamPair[]>([])

function addParamRow() {
  modelParamsList.value.push({ key: '', value: '' })
}

function removeParamRow(index: number) {
  modelParamsList.value.splice(index, 1)
}

// 把 value 字符串智能转换为 number / boolean / string
function parseParamValue(raw: string): unknown {
  const v = raw.trim()
  if (v === '') return ''
  if (v === 'true') return true
  if (v === 'false') return false
  const num = Number(v)
  if (!Number.isNaN(num)) return num
  return v
}

// 键值对列表 -> JSON 字符串
function serializeModelParams(): string {
  const obj: Record<string, unknown> = {}
  for (const pair of modelParamsList.value) {
    const key = pair.key.trim()
    if (!key) continue
    obj[key] = parseParamValue(pair.value)
  }
  return JSON.stringify(obj)
}

// JSON 字符串 -> 键值对列表
function parseModelParams(json?: string): ModelParamPair[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json)
    if (obj && typeof obj === 'object' && !Array.isArray(obj)) {
      return Object.entries(obj).map(([key, value]) => ({
        key,
        value: typeof value === 'string' ? value : JSON.stringify(value),
      }))
    }
    return []
  } catch {
    return []
  }
}

// ==================== 查看弹窗 ====================
const viewVisible = ref(false)
const viewTitle = ref('')
const viewContent = ref('')

// ==================== 列配置 ====================
const columns = [
  { label: '提示词名称', prop: 'name' },
  { label: 'Key', prop: 'key' },
  { label: '模型', prop: 'model', width: 160 },
  { label: '版本号', prop: 'version', width: 100, align: 'center' as const },
  { label: '状态', prop: 'status', slot: 'status', width: 100, align: 'center' as const },
]

// ==================== 加载数据 ====================
async function loadAiPromptData() {
  loading.value = true
  try {
    const dto: AiPromptDTO = {
      name: queryParams.name,
      key: queryParams.key,
      status: queryParams.status === '' ? undefined : queryParams.status,
    }
    const page = (await listAiPrompts(
      queryParams.pageNum,
      queryParams.pageSize,
      dto,
    )) as unknown as PageResult<AiPromptVO>
    tableData.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载提示词列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
function handleSearch() {
  queryParams.pageNum = 1
  loadAiPromptData()
}

function handleReset() {
  queryParams.name = ''
  queryParams.key = ''
  queryParams.status = ''
  handleSearch()
}

function handleAdd() {
  formData.value = defaultFormData()
  modelParamsList.value = []
  dialogTitle.value = '新增提示词'
  dialogVisible.value = true
}

async function handleEdit(row: AiPromptVO) {
  try {
    const detail = (await getAiPromptById(row.id)) as unknown as AiPromptVO
    formData.value = {
      id: detail.id,
      name: detail.name,
      key: detail.key,
      content: detail.content,
      model: detail.model,
      modelParams: detail.modelParams,
      version: detail.version,
      status: detail.status,
    }
    modelParamsList.value = parseModelParams(detail.modelParams)
    dialogTitle.value = '修改提示词'
    dialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.msg || '获取提示词详情失败')
  }
}

function handleCancel() {
  dialogVisible.value = false
}

async function handleSubmit() {
  submitting.value = true
  try {
    formData.value.modelParams = serializeModelParams()
    if (formData.value.id) {
      await updateAiPrompt(formData.value)
      ElMessage.success('修改成功')
    } else {
      await addAiPrompt(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadAiPromptData()
  } catch (error: any) {
    ElMessage.error(error?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleView(row: AiPromptVO) {
  viewTitle.value = `查看提示词：${row.name}`
  viewContent.value = row.content
  viewVisible.value = true
}

async function handleDelete(rows: AiPromptVO[]) {
  if (!rows.length) return

  const ids = rows
    .map((row) => row.id)
    .filter((id): id is number => typeof id === 'number')

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${rows.length} 个提示词吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )

    if (ids.length === 1) {
      await deleteAiPrompt(ids[0]!)
    } else {
      await batchDeleteAiPrompts(ids)
    }

    ElMessage.success('删除成功')
    loadAiPromptData()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.msg || '删除失败')
  }
}

function handleCurrentChange(page: number) {
  queryParams.pageNum = page
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
}

// ==================== 初始化 ====================
onMounted(() => {
  loadAiPromptData()
})
</script>

<style scoped>
.ai-prompt-page {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.model-params {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.model-params__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-params__key {
  width: 40%;
}

.model-params__value {
  flex: 1;
}

.content-view {
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 60vh;
  overflow: auto;
  line-height: 1.6;
  color: #303133;
}
</style>

