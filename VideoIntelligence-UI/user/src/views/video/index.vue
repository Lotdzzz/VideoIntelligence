<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RESOURCE_PAGE_SIZE, listResources } from '@/api/fileResource'
import { routesConstants } from '@/constants/routesConstants'
import { useUserStore } from '@/stores/user'
import { resolveErrorMessage } from '@/utils/errorMessage'
import { formatFileSize, resolveFileUrl } from '@/utils/file'
import type { FileVO } from '@/types/file/fileVO'

/**
 * 视频内容区
 *
 * 数据来源：侧边栏点击分类 → 跳转 /video?categoryId={id} → 本页读取 query → 调用 /file/resource/list
 * （后端按 userId + categoryId 分页查询；query 不带 categoryId 时展示当前用户的全部视频）
 *
 * 页面按「外壳」结构搭好（工具栏 / 骨架屏 / 空态 / 卡片网格 / 分页），
 * 播放能力待后端提供「按文件ID生成预览地址」的接口后再接入，卡片上的播放按钮先置灰占位
 */

/** 面板状态：idle 未加载、loading 加载中、success 加载完成、error 加载失败 */
type PanelStatus = 'idle' | 'loading' | 'success' | 'error'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 当前选中的分类ID：由侧边栏点击分类写入 URL query；为空表示「全部视频」 */
const categoryId = computed<number | null>(() => {
  const raw = route.query.categoryId
  // query 允许同名多值（?categoryId=1&categoryId=2），取第一个即可
  const value = Array.isArray(raw) ? raw[0] : raw
  if (value === undefined || value === null || value === '') {
    return null
  }
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
})

/** 是否已登录（未登录时接口依赖登录上下文，只展示登录提示） */
const isLogin = computed(() => userStore.isLogin)

/** 是否按分类筛选（决定空态文案与「查看全部视频」入口） */
const inCategory = computed(() => categoryId.value !== null)

/** 面板状态 */
const status = ref<PanelStatus>('idle')

/** 当前页资源列表 */
const resources = ref<FileVO[]>([])

/** 总条数（分页组件用） */
const total = ref(0)

/** 当前页码 */
const pageNum = ref(1)

/** 请求是否进行中：既驱动刷新按钮的 loading，也阻止登录态恢复与首次加载并发造成的重复请求 */
const fetching = ref(false)

/** 骨架屏展示条件：已登录且请求尚未完成（避免内容区一闪而空白） */
const showSkeleton = computed(
  () => isLogin.value && (status.value === 'loading' || status.value === 'idle')
)

/** 骨架卡片占位数量（铺满一屏即可，无需按整页条数渲染） */
const SKELETON_COUNT = 8

/** 封面加载失败的资源ID：失败时回落到占位图标，避免出现裂图 */
const brokenCovers = ref<number[]>([])

/** 文件状态文案映射（与后端 ViFile.status 对齐：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除） */
const STATUS_TEXT: Record<number, string> = {
  0: '上传中',
  1: '已上传',
  2: '处理中',
  3: '处理完成',
  4: '上传失败',
  5: '已删除',
}

/** 状态文案：后端未返回状态时展示空串（模板据此不渲染状态角标） */
const statusText = (status?: number | null) =>
  status === null || status === undefined ? '' : (STATUS_TEXT[status] ?? '未知')

/** 封面地址：后端未生成封面时为 null，resolveFileUrl 会返回空串，模板据此展示占位图标 */
const coverUrl = (item: FileVO) => resolveFileUrl(item.cover)

/** 是否展示封面图（无封面或加载失败时展示占位图标） */
const showCover = (item: FileVO) =>
  Boolean(coverUrl(item)) && !brokenCovers.value.includes(item.id)

/** 封面加载失败：记入失败列表，模板改用占位图标 */
const handleCoverError = (id: number) => {
  if (!brokenCovers.value.includes(id)) {
    brokenCovers.value = [...brokenCovers.value, id]
  }
}

/** 创建时间只展示日期部分（后端返回格式为 yyyy-MM-dd HH:mm:ss） */
const createDate = (value?: string | null) => (value ? value.slice(0, 10) : '')

/**
 * 加载当前分类下的资源
 * 1）登录态恢复中（刷新后本地 token 校验未返回）：先等恢复完成，避免把已登录用户误判为游客
 * 2）未登录：清空列表并展示登录提示，不发请求（接口依赖登录上下文，避免控制台一片 401）
 * 3）失败：切到错误态并给出重试入口
 */
const fetchResources = async () => {
  if (fetching.value) {
    return
  }
  fetching.value = true
  try {
    if (userStore.restoring) {
      // 复用同一份恢复请求；失败时 store 已清空本地凭证，错误提示由 Layout 统一给出
      await userStore.restoreLogin().catch(() => undefined)
    }
    if (!isLogin.value) {
      resources.value = []
      total.value = 0
      brokenCovers.value = []
      status.value = 'idle'
      return
    }

    status.value = 'loading'
    // userId 必须传当前登录用户ID：后端按入参过滤，不传会查到其他用户的资源
    const page = await listResources(pageNum.value, RESOURCE_PAGE_SIZE, {
      userId: userStore.userInfo.userId,
      categoryId: categoryId.value,
    })
    resources.value = page.records ?? []
    total.value = page.total ?? 0
    brokenCovers.value = []
    status.value = 'success'
  } catch (error) {
    status.value = 'error'
    ElMessage.warning(resolveErrorMessage(error, '视频列表加载失败，请稍后重试'))
  } finally {
    fetching.value = false
  }
}

/** 翻页：页码由分页组件传入，切换后重新拉当前页 */
const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchResources()
}

/** 手动刷新当前页（错误态下的「重新加载」也复用它） */
const handleRefresh = () => {
  fetchResources()
}

/** 回到「全部视频」：清掉 URL 上的分类ID，与侧边栏「全部视频」入口行为一致 */
const goAllVideos = () => {
  router.push(routesConstants.VIDEO)
}

// 分类切换：回到第一页再加载（同一组件内切换分类不会重挂载，页码必须显式重置）
watch(categoryId, () => {
  pageNum.value = 1
  resources.value = []
  total.value = 0
  fetchResources()
})

// 登录态变化：登录成功补一次加载；退出登录清空列表，避免残留上一个账号的资源
watch(isLogin, (logged) => {
  if (logged) {
    fetchResources()
    return
  }
  resources.value = []
  total.value = 0
  brokenCovers.value = []
  status.value = 'idle'
})

onMounted(fetchResources)
</script>

<template>
  <div class="video-page">
    <!-- 工具栏：分类筛选状态 + 总数 + 刷新（后续视频上传 / 搜索入口也放在这一行） -->
    <div class="video-toolbar">
      <div class="video-title">
        <span class="video-title-text">视频资源</span>
        <span v-if="status === 'success'" class="video-count">共 {{ total }} 个</span>
        <span v-if="inCategory" class="video-filter-chip">已按左侧分类筛选</span>
      </div>
      <el-button class="video-refresh" text :loading="fetching" @click="handleRefresh">
        <el-icon>
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M12 5V2L7 6l5 4V7a5 5 0 1 1-5 5H5a7 7 0 1 0 7-7Z"/>
          </svg>
        </el-icon>
        <span>刷新</span>
      </el-button>
    </div>

    <div class="video-body">
      <!-- 骨架屏：首次加载 / 翻页 / 切换分类时占位，避免内容区闪空白 -->
      <div v-if="showSkeleton" class="video-grid">
        <div v-for="n in SKELETON_COUNT" :key="n" class="resource-card is-skeleton">
          <div class="resource-cover"></div>
          <div class="resource-lines">
            <div class="skeleton-line"></div>
            <div class="skeleton-line is-short"></div>
          </div>
        </div>
      </div>

      <!-- 未登录：接口依赖登录上下文，这里只做引导 -->
      <el-empty v-else-if="!isLogin" description="请先登录后查看视频资源" />

      <!-- 加载失败：保留重试入口 -->
      <el-empty v-else-if="status === 'error'" description="视频列表加载失败">
        <el-button @click="handleRefresh">重新加载</el-button>
      </el-empty>

      <!-- 空态：区分「该分类下暂无视频」与「暂无视频资源」 -->
      <el-empty
        v-else-if="status === 'success' && resources.length === 0"
        :description="inCategory ? '该分类下暂无视频' : '暂无视频资源'"
      >
        <el-button v-if="inCategory" @click="goAllVideos">查看全部视频</el-button>
      </el-empty>

      <!-- 资源卡片：封面 + 文件名 + 大小 / 创建时间 + 状态角标 -->
      <div v-else class="video-grid">
        <article v-for="item in resources" :key="item.id" class="resource-card">
          <div class="resource-cover">
            <img
              v-if="showCover(item)"
              :src="coverUrl(item)"
              :alt="item.originalName"
              @error="handleCoverError(item.id)"
            />
            <!-- 无封面 / 封面加载失败时的占位图标 -->
            <svg
              v-else
              class="resource-cover-placeholder"
              viewBox="0 0 24 24"
              width="30"
              height="30"
              fill="currentColor"
            >
              <path
                d="M3 6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2.2l4.55-2.28A1 1 0 0 1 21 6.82v10.36a1 1 0 0 1-1.45.9L15 15.8V18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6Z"
              />
            </svg>
            <span v-if="statusText(item.status)" class="resource-status">
              {{ statusText(item.status) }}
            </span>
          </div>

          <div class="resource-info">
            <div class="resource-name" :title="item.originalName">{{ item.originalName }}</div>
            <div class="resource-meta">
              <span v-if="formatFileSize(item.fileSize)">{{ formatFileSize(item.fileSize) }}</span>
              <span v-if="createDate(item.createTime)">{{ createDate(item.createTime) }}</span>
            </div>
          </div>

          <div class="resource-actions">
            <!--
              播放能力待接入：需要后端提供「按文件ID生成预览/下载预签名地址」的接口
              （现有 /file/download-presign 是按请求里的文件名重新拼一个随机对象名签 URL，取不到已存文件）
            -->
            <el-button
              class="resource-play"
              text
              disabled
              title="播放能力待接入：需后端提供按文件ID生成预览地址的接口"
            >
              播放
            </el-button>
          </div>
        </article>
      </div>
    </div>

    <!-- 分页：只有一页时隐藏（每页条数与后端 DEFAULT_PAGE_SIZE 对齐） -->
    <div v-if="status === 'success' && total > 0" class="video-pager">
      <el-pagination
        :current-page="pageNum"
        :page-size="RESOURCE_PAGE_SIZE"
        :total="total"
        layout="prev, pager, next"
        background
        hide-on-single-page
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
/* 与用户端整体黑白灰风格保持一致：白底 + 浅灰描边 */
.video-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 360px;
}

/* ==================== 工具栏 ==================== */
.video-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.video-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.video-title-text {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.video-count {
  font-size: 12px;
  color: #909399;
}

/* 分类筛选标记：说明当前列表已按侧边栏选中的分类过滤 */
.video-filter-chip {
  padding: 1px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #f7f8fa;
  font-size: 12px;
  color: #606266;
}

.video-refresh {
  color: #606266;
}

/* ==================== 内容区 ==================== */
.video-body {
  flex: 1;
  min-height: 0;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

/* 卡片网格：按内容区宽度自适应列数（侧边栏折叠 / 展开都能适配） */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.resource-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.resource-card:hover {
  box-shadow: 0 4px 12px rgba(31, 45, 61, 0.08);
  transform: translateY(-2px);
}

/* 封面：固定 16:9，无封面时展示占位图标 */
.resource-cover {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16 / 9;
  background: #f5f7fa;
  color: #c0c4cc;
}

.resource-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 状态角标：叠在封面右上角，保持黑白灰配色 */
.resource-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(31, 45, 61, 0.7);
  color: #ffffff;
  font-size: 12px;
}

.resource-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px 0;
  min-width: 0;
}

/* 文件名最多两行，超出省略号收尾 */
.resource-name {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  font-size: 13px;
  color: #1f2d3d;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #909399;
}

.resource-actions {
  display: flex;
  justify-content: flex-end;
  padding: 4px 6px 6px;
}

.resource-play {
  font-size: 12px;
}

/* ==================== 骨架屏 ==================== */
.resource-card.is-skeleton {
  pointer-events: none;
}

.resource-card.is-skeleton .resource-cover,
.skeleton-line {
  background: #f0f2f5;
  animation: card-pulse 1.2s ease-in-out infinite;
}

.resource-lines {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}

.skeleton-line {
  height: 12px;
  border-radius: 6px;
}

.skeleton-line.is-short {
  width: 55%;
}

@keyframes card-pulse {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

/* ==================== 分页 ==================== */
.video-pager {
  display: flex;
  justify-content: center;
}

/* 空态：上下留白与卡片区域保持一致 */
.video-body :deep(.el-empty) {
  padding: 40px 0;
}

</style>

