<script setup lang="ts">
/**
 * 用户端首页（欢迎页）
 *
 * 定位：向登录 / 未登录用户介绍 VideoIntelligence（智能化视频分析平台）是什么、
 * 当前已经具备哪些能力、平台后续要做什么，并给出进入用户端「视频资源」的入口。
 *
 * 数据说明：本页不含任何演示（假）数据——
 * 1）项目简介、平台能力、项目模块与技术栈均取自仓库既有的 README、pom.xml、package.json 与源码结构；
 * 2）「我的概览」中的资源总数 / 分类总数来自后端真实接口，
 *    未登录或接口失败时展示占位符「—」，不用假数字填充。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listResources } from '@/api/fileResource'
import { listUserFileCategories } from '@/api/fileCategory'
import { routesConstants } from '@/constants/routesConstants'
import { useUserStore } from '@/stores/user'
import { resolveFileUrl } from '@/utils/file'
import LoginDialog from '@/components/LoginDialog.vue'

// ==================== 登录态与用户信息 ====================

const router = useRouter()
const userStore = useUserStore()

/** 是否已登录（未登录只展示平台介绍与登录入口，不发依赖登录上下文的统计请求） */
const isLogin = computed(() => userStore.isLogin)

/** 登录弹窗是否显示（复用布局里同一个 GitHub 授权登录组件） */
const loginDialogVisible = ref(false)

/** 展示名：优先昵称，昵称缺失时退回账号（均来自 /auth/getUserInfo） */
const displayName = computed(() => {
  const { nickName, userName } = userStore.userInfo
  return nickName || userName
})

/** 头像地址：GitHub 返回完整地址、本地上传返回文件名，统一交给 resolveFileUrl 处理 */
const avatarUrl = computed(() => resolveFileUrl(userStore.userInfo.avatar))

// ==================== 我的概览（真实接口数据） ====================

/** 概览状态：idle 未登录 / 未加载、loading 加载中、success 加载完成、error 加载失败 */
type OverviewStatus = 'idle' | 'loading' | 'success' | 'error'

const overviewStatus = ref<OverviewStatus>('idle')

/** 当前用户的视频资源总数（取分页接口的 total；未登录或失败时为 null） */
const videoTotal = ref<number | null>(null)

/** 当前用户的分类总数（取分类列表长度；未登录或失败时为 null） */
const categoryTotal = ref<number | null>(null)

/** 概览请求是否进行中（驱动刷新按钮的 loading，并阻止重复请求） */
const overviewFetching = ref(false)

/**
 * 数字占位符
 * 未登录 / 加载中 / 加载失败统一展示「—」，避免出现「0」这种误导性数字
 */
const toCountText = (value: number | null) => (value === null ? '—' : String(value))

/** 视频资源总数文案 */
const videoTotalText = computed(() => toCountText(videoTotal.value))

/** 分类总数文案 */
const categoryTotalText = computed(() => toCountText(categoryTotal.value))

/**
 * 加载「我的概览」
 * 1）登录态恢复中（刷新页面时本地 token 校验未返回）：先等恢复完成，避免把已登录用户误判为游客
 * 2）未登录：清空数字并回到 idle，不发请求（接口依赖登录上下文，避免无意义的 401）
 * 3）已登录：并发拉取资源总数与分类列表；失败时保持占位符并提示可刷新重试
 *    （首页统计属于辅助信息，不弹全局提示，避免影响欢迎页体验）
 */
const loadOverview = async () => {
  if (overviewFetching.value) {
    return
  }
  overviewFetching.value = true
  try {
    if (userStore.restoring) {
      // 复用 store 中同一份恢复请求；失败时 store 已清空本地凭证，错误提示由 Layout 统一给出
      await userStore.restoreLogin().catch(() => undefined)
    }
    if (!isLogin.value) {
      videoTotal.value = null
      categoryTotal.value = null
      overviewStatus.value = 'idle'
      return
    }

    overviewStatus.value = 'loading'
    const [page, categories] = await Promise.all([
      // pageSize 传 1：首页只需要 total，不必把第一页记录也拉回来
      listResources(1, 1, { userId: userStore.userInfo.userId }),
      // 分类为不分页接口，列表长度即分类总数
      listUserFileCategories(),
    ])
    videoTotal.value = page.total ?? 0
    categoryTotal.value = categories.length
    overviewStatus.value = 'success'
  } catch {
    videoTotal.value = null
    categoryTotal.value = null
    overviewStatus.value = 'error'
  } finally {
    overviewFetching.value = false
  }
}

/** 进入视频资源页（与侧边栏「全部资源」一致，不带分类条件） */
const goVideo = () => {
  router.push(routesConstants.VIDEO)
}

/** 链路 / 步骤序号：1 → 01，保证序号等宽对齐 */
const stepLabel = (index: number) => String(index + 1).padStart(2, '0')

// 登录态变化：登录成功后补一次统计；退出登录清空数字，避免残留上一个账号的数据
watch(isLogin, (logged) => {
  if (logged) {
    loadOverview()
    return
  }
  videoTotal.value = null
  categoryTotal.value = null
  overviewStatus.value = 'idle'
})

onMounted(loadOverview)
// ==================== 静态介绍内容（来源：README / pom.xml / package.json / 源码结构） ====================

/** 平台能力卡片 */
interface CapabilityItem {
  /** 能力标题 */
  title: string
  /** 能力说明 */
  desc: string
  /** 图标 path（沿用项目内联 SVG 的 24 x 24 viewBox 写法） */
  icon: string
}

const CAPABILITIES: CapabilityItem[] = [
  {
    title: 'GitHub 账号登录',
    desc: 'OAuth2 授权登录，网关统一鉴权，后端签发 JWT 令牌并在 Redis 中维护登录态。',
    icon: 'M12 12a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm0 1.8c-3.6 0-6.6 1.9-6.6 4.3V21h13.2v-2.9c0-2.4-3-4.3-6.6-4.3Z',
  },
  {
    title: '视频资源检索',
    desc: '分页查询资源文件，支持按分类、文件名、类型、状态与创建时间范围过滤，结果按创建时间倒序。',
    icon: 'M3 6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2.2l4.55-2.28A1 1 0 0 1 21 6.82v10.36a1 1 0 0 1-1.45.9L15 15.8V18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6Z',
  },
  {
    title: '分类体系管理',
    desc: '每个用户拥有独立分类：新增、重命名、排序、关键字搜索与批量删除，分类按用户维度做同名校验。',
    icon: 'M3 5.5A1.5 1.5 0 0 1 4.5 4h4.1c.4 0 .78.16 1.06.44l1.28 1.3c.28.28.66.44 1.06.44h6.5A1.5 1.5 0 0 1 21 7.68v9.82a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5v-12Z',
  },
  {
    title: '对象存储直传',
    desc: '基于 MinIO 预签名 URL 的文件直传方案，资源元数据（vi_file）落库后按 user_id 隔离查询。',
    icon: 'M19.35 10.04A7.49 7.49 0 0 0 12 4C9.11 4 6.6 5.64 5.35 8.04A5.99 5.99 0 0 0 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96ZM14 13v4h-4v-4H7l5-5 5 5h-3Z',
  },
  {
    title: '大模型能力底座',
    desc: 'dotm-framework-ai 封装 Spring AI ChatClient 与 Prompt 管理，模型侧配置已接入 DeepSeek。',
    icon: 'M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9Zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12l-5.5-2.5ZM19 15l-1.25 2.75L15 19l2.75 1.25L19 23l1.25-2.75L23 19l-2.75-1.25L19 15Z',
  },
  {
    title: '可观测与治理',
    desc: '系统指标监控（CPU / 内存 / 磁盘 / JVM）、登录日志经 RabbitMQ 异步落库，Nacos 统一托管配置。',
    icon: 'M3 13h8V3H3v10Zm0 8h8v-6H3v6Zm10 0h8V11h-8v10Zm0-18v6h8V3h-8Z',
  },
]

/** 目标分析链路（来源：仓库 README 的项目简介） */
const PIPELINE_STEPS: string[] = [
  '视频上传',
  '视频预处理',
  '音视频解析',
  '语音识别 ASR',
  '字幕 / 文本生成',
  '文本切分与上下文构建',
  '大模型分析',
  '知识提取',
  '多维度内容生成',
  '结果可视化',
]

/** 项目模块（来源：根 pom.xml 的 modules 与各模块源码目录） */
interface ModuleItem {
  /** 模块中文名 */
  name: string
  /** 模块目录名（与仓库目录保持一致） */
  path: string
  /** 模块职责 */
  desc: string
}

const MODULES: ModuleItem[] = [
  {
    name: '网关',
    path: 'dotm-gateway',
    desc: '微服务统一入口，AuthGlobalFilter 做全局鉴权与登录态透传。',
  },
  {
    name: '认证授权',
    path: 'dotm-auth',
    desc: 'GitHub OAuth2 登录与 JWT 令牌签发，另含用户 / 角色 / 菜单管理接口。',
  },
  {
    name: '文件服务',
    path: 'dotm-file',
    desc: '通用文件上传（阿里云 OSS），为头像等静态资源提供统一访问地址。',
  },
  {
    name: '视频资源服务',
    path: 'vi-service/vi-service-file',
    desc: 'vi_file 资源与 vi_file_category 分类的增删改查、分页检索与 MinIO 预签名直传。',
  },
  {
    name: '监控服务',
    path: 'dotm-monitor',
    desc: 'dotm-monitor-system 采集系统指标，dotm-monitor-auth 消费登录日志并落库。',
  },
  {
    name: '基础框架',
    path: 'dotm-framework',
    desc: 'common / core / datasource / redis / mq / security / ai 等公共 starter。',
  },
]

/** 技术栈（版本取自根 pom.xml 与前端 package.json） */
interface StackGroup {
  /** 分组名 */
  name: string
  /** 该分组下的技术条目 */
  items: string[]
}

const STACK_GROUPS: StackGroup[] = [
  {
    name: '后端框架',
    items: [
      'Java 21',
      'Spring Boot 4.0.7',
      'Spring Cloud 2025.1.2',
      'Spring Cloud Alibaba 2025.1.0.0',
      'Spring AI 2.0.1',
    ],
  },
  {
    name: '数据与鉴权',
    items: ['MyBatis-Plus 3.5.17', 'Druid 1.2.28', 'MySQL', 'Redis', 'JJWT 0.13.0'],
  },
  {
    name: '前端',
    items: [
      'Vue 3.5',
      'TypeScript 6.0',
      'Vite 8.1',
      'Element Plus 2.14',
      'Pinia 4.0',
      'Vue Router 5.2',
      'Axios 1.20',
    ],
  },
  {
    name: '存储与中间件',
    items: ['Nacos', 'RabbitMQ', 'MinIO 8.6.0', '阿里云 OSS 3.18.5', 'DeepSeek（OpenAI 兼容接口）'],
  },
]

/** 使用指引（对应用户端现有交互） */
interface UsageStep {
  /** 步骤标题 */
  title: string
  /** 步骤说明 */
  desc: string
}

const USAGE_STEPS: UsageStep[] = [
  {
    title: '登录平台',
    desc: '点击「GitHub 登录」或侧边栏左下角用户区，通过 GitHub 授权建立会话，令牌由后端签发。',
  },
  {
    title: '维护分类',
    desc: '展开侧边栏「视频资源分析」，用工具条的新增分类按钮建立自己的分类体系，可搜索、排序与批量删除。',
  },
  {
    title: '浏览资源',
    desc: '点击某个分类或「全部资源」进入视频页，按分类查看资源卡片，列表每页 36 条（与后端默认分页一致）。',
  },
]
</script>

<template>
  <div class="home-page">
    <!-- ==================== 平台介绍 + 我的概览 ==================== -->
    <section class="hero">
      <div class="hero-main">
        <span class="hero-badge">Apache-2.0 开源 · Spring Cloud 微服务 · 大模型驱动</span>
        <h1 class="hero-title">
          VideoIntelligence
          <span class="hero-subtitle">智能化视频分析平台</span>
        </h1>
        <p class="hero-desc">
          对视频进行自动转码、语音识别与内容理解，并利用大模型生成知识点大纲、详细知识点、思维导图、
          架构图等多维度分析结果，把「完整看完再自己整理」变成「交给平台自动结构化」。
        </p>
        <div class="hero-actions">
          <el-button type="primary" @click="goVideo">进入视频资源</el-button>
          <el-button v-if="!isLogin" @click="loginDialogVisible = true">GitHub 登录</el-button>
        </div>
        <p class="hero-tip">
          视频上传入口、音视频解析与在线播放能力正在接入中，当前用户端已开放登录、分类管理与资源浏览。
        </p>
      </div>

      <!-- 我的概览：全部展示后端真实数据，未登录 / 加载失败时用占位符，不做假数据填充 -->
      <div class="overview-card">
        <div class="overview-header">
          <span class="overview-title">我的概览</span>
          <el-button v-if="isLogin" text :loading="overviewFetching" @click="loadOverview">
            刷新
          </el-button>
        </div>

        <!-- 未登录：只做登录引导 -->
        <div v-if="!isLogin" class="overview-guest">
          <p class="overview-guest-text">登录后即可查看自己的视频资源与分类统计。</p>
          <el-button type="primary" @click="loginDialogVisible = true">使用 GitHub 账号登录</el-button>
        </div>

        <!-- 已登录：真实账号信息 + 真实统计 -->
        <template v-else>
          <div class="overview-user">
            <el-avatar class="overview-avatar" :size="40" :src="avatarUrl">
              {{ displayName.slice(0, 1) }}
            </el-avatar>
            <div class="overview-user-text">
              <div class="overview-user-name" :title="displayName">{{ displayName }}</div>
              <div class="overview-user-account" :title="userStore.userInfo.userName">
                {{ userStore.userInfo.userName }}
              </div>
            </div>
          </div>

          <div class="overview-stats">
            <div class="overview-stat">
              <span class="overview-stat-value">{{ videoTotalText }}</span>
              <span class="overview-stat-label">视频资源</span>
            </div>
            <div class="overview-stat">
              <span class="overview-stat-value">{{ categoryTotalText }}</span>
              <span class="overview-stat-label">资源分类</span>
            </div>
          </div>

          <p class="overview-note">
            {{
              overviewStatus === 'error'
                ? '统计加载失败，可点击右上角「刷新」重试'
                : '数据来自 /file/resource/list 与 /file/category/user 接口'
            }}
          </p>
        </template>
      </div>
    </section>

    <!-- ==================== 平台能力 ==================== -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">平台能力</h2>
        <p class="section-desc">以下能力均已在当前仓库中实现，分别在用户端与后台管理端开放。</p>
      </div>
      <div class="capability-grid">
        <article v-for="item in CAPABILITIES" :key="item.title" class="capability-card">
          <span class="capability-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path :d="item.icon" />
            </svg>
          </span>
          <h3 class="capability-title">{{ item.title }}</h3>
          <p class="capability-desc">{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <!-- ==================== 目标分析链路 ==================== -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">目标分析链路</h2>
        <p class="section-desc">平台规划的处理流程：用户只需上传一个视频，后续解析、识别与分析由平台完成。</p>
      </div>
      <ol class="pipeline">
        <li v-for="(step, index) in PIPELINE_STEPS" :key="step" class="pipeline-step">
          <span class="pipeline-index">{{ stepLabel(index) }}</span>
          <span class="pipeline-name">{{ step }}</span>
        </li>
      </ol>
      <p class="section-note">
        说明：以上链路为项目 README 中定义的目标流程。当前仓库已落地账号体系、资源与分类管理，
        音视频解析与大模型分析链路仍在建设中。
      </p>
    </section>

    <!-- ==================== 项目模块 ==================== -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">项目模块</h2>
        <p class="section-desc">后端按微服务拆分，各模块独立构建，对外统一由网关收敛入口。</p>
      </div>
      <div class="module-grid">
        <article v-for="item in MODULES" :key="item.path" class="module-card">
          <div class="module-head">
            <span class="module-name">{{ item.name }}</span>
            <code class="module-path">{{ item.path }}</code>
          </div>
          <p class="module-desc">{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <!-- ==================== 技术栈 ==================== -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">技术栈</h2>
        <p class="section-desc">版本信息取自根 pom.xml 与前端 package.json。</p>
      </div>
      <div class="stack-grid">
        <div v-for="group in STACK_GROUPS" :key="group.name" class="stack-card">
          <div class="stack-name">{{ group.name }}</div>
          <div class="stack-tags">
            <span v-for="item in group.items" :key="item" class="stack-tag">{{ item }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== 使用指引 ==================== -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">快速上手</h2>
        <p class="section-desc">用户端的典型使用路径，三步即可开始。</p>
      </div>
      <div class="usage-grid">
        <article v-for="(item, index) in USAGE_STEPS" :key="item.title" class="usage-card">
          <span class="usage-index">{{ stepLabel(index) }}</span>
          <h3 class="usage-title">{{ item.title }}</h3>
          <p class="usage-desc">{{ item.desc }}</p>
        </article>
      </div>
      <p class="section-note">
        提示：视频页的「播放」按钮当前为占位（需要后端提供按文件 ID 生成预览地址的接口），
        上传入口与播放能力接入后会同步在用户端开放。
      </p>
    </section>

    <footer class="home-footer">
      VideoIntelligence · 基于 Apache-2.0 许可开源 · 本页项目信息来自仓库代码，统计数据来自后端接口
    </footer>

    <!-- 登录弹窗：与布局共用同一个 GitHub 授权登录组件 -->
    <LoginDialog
      v-model="loginDialogVisible"
      title="登录"
      subtitle="使用 GitHub 账号登录后可查看自己的视频资源"
    />
  </div>
</template>

<style scoped>
/* 与用户端整体黑白灰风格保持一致：白底 + 浅灰描边 */
.home-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ==================== 平台介绍 + 我的概览 ==================== */
.hero {
  display: grid;
  /* 左侧介绍自适应宽度，右侧概览卡固定 300px */
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
}

.hero-main {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.hero-badge {
  display: inline-block;
  padding: 2px 10px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background: #f7f8fa;
  font-size: 12px;
  color: #606266;
}

.hero-title {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0 0;
  font-size: 26px;
  font-weight: 700;
  color: #1f2d3d;
}

.hero-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.hero-desc {
  max-width: 760px;
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.hero-tip {
  margin: 12px 0 0;
  font-size: 12px;
  line-height: 1.7;
  color: #909399;
}

/* 概览卡：内容随登录态切换，尺寸不足时纵向排列 */
.overview-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.overview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.overview-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.overview-guest {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.overview-guest-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

.overview-user {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.overview-avatar {
  flex: none;
  background: #f2f3f5;
  color: #606266;
}

.overview-user-text {
  min-width: 0;
}

/* 昵称 / 账号过长时省略号收尾，避免撑破卡片 */
.overview-user-name {
  overflow: hidden;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-user-account {
  overflow: hidden;
  font-size: 12px;
  color: #909399;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.overview-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  background: #f7f8fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.overview-stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
}

.overview-stat-label {
  font-size: 12px;
  color: #909399;
}

.overview-note {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}
/* ==================== 通用区块 ==================== */
.section {
  padding: 20px 24px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.section-head {
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.section-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

/* 区块底部说明：与内容用虚线分隔，明确「补充说明」而非等权重内容 */
.section-note {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  line-height: 1.7;
  color: #909399;
}

/* ==================== 平台能力 ==================== */
.capability-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.capability-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.capability-card:hover {
  box-shadow: 0 4px 12px rgba(31, 45, 61, 0.08);
  transform: translateY(-2px);
}

.capability-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f7f8fa;
  color: #303133;
}

.capability-title {
  margin: 4px 0 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.capability-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

/* ==================== 目标分析链路 ==================== */
.pipeline {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.pipeline-step {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f7f8fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.pipeline-index {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
}

.pipeline-name {
  font-size: 13px;
  color: #303133;
}

/* ==================== 项目模块 ==================== */
.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.module-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  background: #f7f8fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.module-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.module-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.module-path {
  padding: 1px 6px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.module-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

/* ==================== 技术栈 ==================== */
.stack-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.stack-card {
  padding: 14px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.stack-name {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.stack-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stack-tag {
  padding: 2px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #f7f8fa;
  font-size: 12px;
  color: #606266;
}

/* ==================== 快速上手 ==================== */
.usage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.usage-card {
  padding: 14px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.usage-index {
  display: inline-block;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
}

.usage-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.usage-desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

/* ==================== 页脚 ==================== */
.home-footer {
  padding: 0 4px 8px;
  text-align: center;
  font-size: 12px;
  line-height: 1.7;
  color: #909399;
}

/* 窄屏（内容区被侧边栏挤压）：概览卡下移，与介绍区垂直排列 */
@media (max-width: 1200px) {
  .hero {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>