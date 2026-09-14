<template>
  <div class="dashboard-page">
    <!-- 顶部标题与操作 -->
    <div class="page-header">
      <span class="page-title">系统概览</span>
      <el-button type="primary" :loading="loading || refreshing" @click="handleRefresh">刷新</el-button>
    </div>

    <div v-loading="loading" class="monitor-content">
      <!-- CPU / 内存 -->
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never" class="monitor-card">
            <template #header>
              <div class="card-header">
                <span>CPU</span>
                <el-tag type="info">{{ info?.cpu.cores ?? 0 }} 核</el-tag>
              </div>
            </template>
            <el-progress
              type="dashboard"
              :percentage="Number((info?.cpu.usage ?? 0).toFixed(2))"
              :color="usageColor(info?.cpu.usage ?? 0)"
            >
              <template #default>
                <span class="progress-text">{{ (info?.cpu.usage ?? 0).toFixed(2) }}%</span>
              </template>
            </el-progress>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-card shadow="never" class="monitor-card">
            <template #header>
              <div class="card-header">
                <span>内存</span>
                <el-tag type="info">
                  {{ formatBytes(info?.memory.used ?? 0) }} / {{ formatBytes(info?.memory.total ?? 0) }}
                </el-tag>
              </div>
            </template>
            <el-progress
              type="dashboard"
              :percentage="Number((info?.memory.usedPercent ?? 0).toFixed(2))"
              :color="usageColor(info?.memory.usedPercent ?? 0)"
            >
              <template #default>
                <span class="progress-text">{{ (info?.memory.usedPercent ?? 0).toFixed(2) }}%</span>
              </template>
            </el-progress>
          </el-card>
        </el-col>
      </el-row>

      <!-- 磁盘列表 -->
      <el-card shadow="never" class="monitor-card">
        <template #header>
          <span>磁盘信息</span>
        </template>
        <el-table :data="info?.disks ?? []" border stripe>
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="mount" label="挂载点" min-width="120" />
          <el-table-column label="总空间" min-width="110" align="right">
            <template #default="{ row }">{{ formatBytes(row.total) }}</template>
          </el-table-column>
          <el-table-column label="已用" min-width="110" align="right">
            <template #default="{ row }">{{ formatBytes(row.used) }}</template>
          </el-table-column>
          <el-table-column label="可用" min-width="110" align="right">
            <template #default="{ row }">{{ formatBytes(row.free) }}</template>
          </el-table-column>
          <el-table-column label="使用率" min-width="180">
            <template #default="{ row }">
              <el-progress :percentage="diskUsagePercent(row)" :color="usageColor(diskUsagePercent(row))" />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- JVM 信息 -->
      <el-card shadow="never" class="monitor-card">
        <template #header>
          <span>JVM 信息</span>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="名称">{{ info?.jvm.name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ info?.jvm.version ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="厂商">{{ info?.jvm.vendor ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="堆内存已用">
            {{ formatBytes(info?.jvm.heapUsed ?? 0) }} / {{ formatBytes(info?.jvm.heapMax ?? 0) }}
          </el-descriptions-item>
          <el-descriptions-item label="非堆内存已用">
            {{ formatBytes(info?.jvm.nonHeapUsed ?? 0) }} / {{ formatBytes(info?.jvm.nonHeapCommitted ?? 0) }}
          </el-descriptions-item>
          <el-descriptions-item label="运行时长">{{ formatUptime(info?.jvm.uptime ?? 0) }}</el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ formatDateTime(info?.jvm.startTime ?? 0) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemInfo } from '@/api/monitor/system'
import type { SystemMonitorInfo, DiskInfo } from '@/types/monitor/system'

// ==================== 数据状态 ====================
const loading = ref(false)    // 首次加载遮罩
const refreshing = ref(false) // 手动刷新按钮 loading
const info = ref<SystemMonitorInfo | null>(null)

// 自动刷新定时器
let timer: ReturnType<typeof setInterval> | null = null

// ==================== 格式化工具 ====================
// 字节转换为可读单位（B/KB/MB/GB/TB）
function formatBytes(bytes: number): string {
  if (!bytes || bytes <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const index = Math.floor(Math.log(bytes) / Math.log(1024))
  const value = bytes / Math.pow(1024, index)
  return `${value.toFixed(2)} ${units[index]}`
}

// 毫秒时长转换为可读文本
function formatUptime(ms: number): string {
  if (!ms || ms <= 0) return '-'
  const totalSeconds = Math.floor(ms / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  if (days > 0) return `${days} 天 ${hours} 小时 ${minutes} 分`
  if (hours > 0) return `${hours} 小时 ${minutes} 分 ${seconds} 秒`
  if (minutes > 0) return `${minutes} 分 ${seconds} 秒`
  return `${seconds} 秒`
}

// 时间戳转换为 yyyy-MM-dd HH:mm:ss
function formatDateTime(timestamp: number): string {
  if (!timestamp || timestamp <= 0) return '-'
  const date = new Date(timestamp)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 根据使用率返回进度条颜色（90% 红 / 70% 橙 / 其余绿）
function usageColor(percent: number): string {
  if (percent >= 90) return '#f56c6c'
  if (percent >= 70) return '#e6a23c'
  return '#67c23a'
}

// 磁盘使用率百分比
function diskUsagePercent(row: DiskInfo): number {
  if (!row.total || row.total <= 0) return 0
  return Number(((row.used / row.total) * 100).toFixed(2))
}

// ==================== 加载数据 ====================
// 加载数据：首次加载（尚无数据）时展示遮罩，后续刷新静默更新，避免遮罩反复出现造成闪烁
async function loadSystemInfo() {
  if (!info.value) {
    loading.value = true
  }
  try {
    const data = (await getSystemInfo()) as unknown as SystemMonitorInfo
    info.value = data
  } catch (error: any) {
    ElMessage.error(error?.msg || '加载系统监控信息失败')
  } finally {
    loading.value = false
  }
}

// 手动刷新：仅刷新按钮展示 loading 反馈
function handleRefresh() {
  refreshing.value = true
  loadSystemInfo().finally(() => {
    refreshing.value = false
  })
}

// ==================== 初始化 ====================
onMounted(() => {
  loadSystemInfo()
  // 每 5 秒自动刷新一次
  timer = setInterval(() => {
    loadSystemInfo()
  }, 5000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.dashboard-page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.monitor-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.monitor-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.progress-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>

