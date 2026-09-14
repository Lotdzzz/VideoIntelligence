<template>
  <div class="auth-log-page">
    <!-- 顶部搜索框架 -->
    <SearchPanel :loading="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="登录账号">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入登录账号"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="登录类型">
        <el-select v-model="queryParams.loginType" placeholder="请选择登录类型" clearable>
          <el-option label="登录" value="login" />
          <el-option label="退出" value="logout" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="成功" value="1" />
          <el-option label="失败" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="登录IP">
        <el-input
          v-model="queryParams.ipAddress"
          placeholder="请输入登录IP"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="登录时间">
        <el-date-picker
          v-model="queryParams.loginTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          clearable
        />
      </el-form-item>
    </SearchPanel>

    <!-- 数据内容框架（日志只读，无新增/修改） -->
    <DataTablePanel
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :total="total"
      :current="queryParams.pageNum"
      :size="queryParams.pageSize"
      :show-add="false"
      :show-edit="false"
      @remove="handleDelete"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
      @pagination-change="loadAuthLogData"
    >
      <!-- 登录类型列自定义渲染 -->
      <template #loginType="{ row }">
        {{ loginTypeText(row.loginType) }}
      </template>

      <!-- 状态列自定义渲染 -->
      <template #status="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
          {{ row.status === 1 ? '成功' : '失败' }}
        </el-tag>
      </template>

      <!-- 失败原因列自定义渲染（仅失败记录展示） -->
      <template #failReason="{ row }">
        {{ row.status === 0 ? row.failReason : '' }}
      </template>

      <!-- 登录时间列自定义渲染（仅登录记录展示） -->
      <template #loginTime="{ row }">
        {{ isLoginRecord(row.loginType) ? row.loginTime : '' }}
      </template>

      <!-- 退出时间列自定义渲染（仅退出记录展示） -->
      <template #logoutTime="{ row }">
        {{ isLogoutRecord(row.loginType) ? row.logoutTime : '' }}
      </template>

      <!-- 行内操作 -->
      <template #action="{ row }">
        <el-button link type="danger" @click="handleDelete([row])">删除</el-button>
      </template>
    </DataTablePanel>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SearchPanel from '@/components/SearchPanel.vue'
import DataTablePanel from '@/components/DataTablePanel.vue'
import { listAuthLogs, deleteAuthLog, batchDeleteAuthLogs } from '@/api/monitor/auth'
import type { PageResult } from '@/types/result'
import type { SysAuthLogVO } from '@/types/monitor/sysAuthLogVO'
import type { SysAuthLogDTO } from '@/types/monitor/sysAuthLogDTO'

// ==================== 搜索加载状态 ====================
const loading = ref(false)

// ==================== 查询参数 ====================
const queryParams = reactive({
  username: '',
  loginType: '',
  status: '',
  ipAddress: '',
  loginTimeRange: [] as string[],
  pageNum: 1,
  pageSize: 10,
})

// ==================== 表格数据 ====================
const tableData = ref<SysAuthLogVO[]>([])
const total = ref(0)

// ==================== 列配置 ====================
const columns = [
  { label: '登录账号', prop: 'username', minWidth: 120 },
  { label: '登录类型', prop: 'loginType', slot: 'loginType', width: 100, align: 'center' as const },
  { label: '状态', prop: 'status', slot: 'status', width: 90, align: 'center' as const },
  { label: '登录IP', prop: 'ipAddress', minWidth: 130 },
  { label: '浏览器UA', prop: 'userAgent', minWidth: 220 },
  { label: '失败原因', prop: 'failReason', slot: 'failReason', minWidth: 150 },
  { label: '登录时间', prop: 'loginTime', slot: 'loginTime', width: 180 },
  { label: '退出时间', prop: 'logoutTime', slot: 'logoutTime', width: 180 },
  { label: '创建时间', prop: 'createTime', width: 180 },
]

// ==================== 登录类型文案 ====================
function loginTypeText(type: string) {
  return type?.toLowerCase() === 'login' ? '登录' : '退出'
}

// 是否为登录记录
function isLoginRecord(type: string) {
  return type?.toLowerCase() === 'login'
}

// 是否为退出记录
function isLogoutRecord(type: string) {
  return type?.toLowerCase() === 'logout'
}

// ==================== 加载数据 ====================
async function loadAuthLogData() {
  loading.value = true
  try {
    // 组装搜索条件，传递给分页查询接口
    const dto: SysAuthLogDTO = {
      username: queryParams.username,
      loginType: queryParams.loginType,
      status: queryParams.status === '' || queryParams.status === undefined || queryParams.status === null ? undefined : Number(queryParams.status),
      ipAddress: queryParams.ipAddress,
      beginTime: queryParams.loginTimeRange?.[0] || undefined,
      endTime: queryParams.loginTimeRange?.[1] || undefined,
    }
    const page = (await listAuthLogs(
      queryParams.pageNum,
      queryParams.pageSize,
      dto,
    )) as unknown as PageResult<SysAuthLogVO>
    tableData.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载日志列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
function handleSearch() {
  queryParams.pageNum = 1
  loadAuthLogData()
}

function handleReset() {
  queryParams.username = ''
  queryParams.loginType = ''
  queryParams.status = ''
  queryParams.ipAddress = ''
  queryParams.loginTimeRange = []
  handleSearch()
}

async function handleDelete(rows: SysAuthLogVO[]) {
  if (!rows.length) return

  const ids = rows
    .map((row) => row.id)
    .filter((id): id is number => typeof id === 'number')

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${rows.length} 条日志吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )

    if (ids.length === 1) {
      await deleteAuthLog(ids[0]!)
    } else {
      await batchDeleteAuthLogs(ids)
    }

    ElMessage.success('删除成功')
    loadAuthLogData()
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
  loadAuthLogData()
})
</script>

<style scoped>
.auth-log-page {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>

