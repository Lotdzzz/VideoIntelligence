<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  ElMessage,
  genFileId,
  type CheckboxValueType,
  type FormInstance,
  type FormRules,
  type UploadRawFile,
  type UploadUserFile,
} from 'element-plus'
import {RESOURCE_PAGE_SIZE} from '@/api/fileResource'
import {listUserFileCategories} from '@/api/fileCategory'
import {resolveVideoLink} from '@/api/videoLink'
import {useVideoList} from '@/features/video'
import {
  VIDEO_ACCEPT,
  uploadVideos,
  type UploadSummary,
  type UploadTaskProgress,
} from '@/features/videoUpload'
import {routesConstants} from '@/constants/routesConstants'
import {useUserStore} from '@/stores/user'
import {resolveErrorMessage} from '@/utils/errorMessage'
import {formatFileSize, resolveFileUrl} from '@/utils/file'
import type {FileCategoryVO} from '@/types/file/fileCategoryVO'
import type {FileVO} from '@/types/file/fileVO'
import type {APIURLsInfoVO} from '@/types/file/apiURLsInfoVO'

/**
 * 视频内容区
 *
 * 数据来源：侧边栏点击分类 → 跳转 /video?categoryId={id} → 本页读取 query → 调用 /file/resource/list
 * （后端按 userId + categoryId 分页查询；query 不带 categoryId 时展示当前用户的全部视频）
 *
 * 列表 / 查询 / 删除 / 批量删除统一由 features/video.ts 的 useVideoList（视频操作外壳）提供，
 * 本页只负责：读取 URL 上的分类ID、登录态恢复、封面与状态展示、模板绑定
 *
 * 交互（与侧边栏分类管理保持一致）：
 * - 查询：点「查询」展开文件名输入框（输入防抖 300ms，回车立即查询），与批量删除互斥；
 *   点输入框清空按钮立即回到全部视频，且只会真正刷新一次（重复触发由外壳按条件指纹去重）
 * - 删除：卡片操作区的「删除」按钮，二次确认后调用 /file/resource/{id}
 * - 批量删除：点「批量删除」进入多选模式，卡片出现复选框 + 顶部「全选 / 删除选中 / 取消」
 *
 * - 添加：点「添加视频资源」弹出选择框，可切换「添加视频链接 / 上传单个视频 / 上传视频集合（无序）」；
 *   上传走「分片直传对象存储」链路（编排见 features/videoUpload.ts）：
 *   slice/info 取分片任务与预签名URL → 并发直传对象存储 → 逐片上报 slice/upload → 服务端合并并落库；
 *   上传必须归属分类，因此弹窗内提供「所属分类」必选下拉（「全部视频」下由用户当场选择）
 *
 * 播放能力待后端提供「按文件ID生成预览地址」的接口后再接入，卡片上的播放按钮先置灰占位
 */

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

/**
 * 视频操作外壳：列表状态 / 分页 / 查询 / 多选 / 删除统一收敛在 features/video.ts
 * buildQuery：每次请求前组装条件（userId 必须传当前登录用户ID，后端按入参过滤，不传会查到其他用户的资源）
 * canFetch：未登录时外壳会清空列表且不发请求（接口依赖登录上下文，避免控制台一片 401）
 */
const {
  status,
  records: resources,
  total,
  pageNum,
  fetching,
  keyword,
  searchVisible,
  isSearching,
  batchMode,
  selectedIds,
  deleting,
  isAllSelected,
  isIndeterminate,
  fetchList,
  refresh,
  changePage,
  reload,
  toggleSearch,
  handleKeywordInput,
  handleKeywordEnter,
  handleKeywordClear,
  enterBatchMode,
  exitBatchMode,
  toggleSelect,
  toggleSelectAll,
  remove,
  removeSelected,
  reset,
} = useVideoList({
  buildQuery: () => ({
    userId: userStore.userInfo.userId,
    categoryId: categoryId.value,
  }),
  canFetch: () => isLogin.value,
})

/** 骨架屏展示条件：已登录且请求尚未完成（避免内容区一闪而空白） */
const showSkeleton = computed(
    () => isLogin.value && (status.value === 'loading' || status.value === 'idle')
)

/**
 * 骨架卡片占位数量：与每页条数保持一致（6 列 × 6 行 = 36），
 * 骨架屏与真实列表的行列数对齐，数据加载完成时高度不跳动
 */
const SKELETON_COUNT = RESOURCE_PAGE_SIZE

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
 * 列表每次变化（重新加载 / 翻页 / 删除）都重置一次封面失败记录，
 * 避免已失败（或已被删除）的旧ID一直压在 brokenCovers 里
 */
watch(resources, () => {
  brokenCovers.value = []
})

/** 回到「全部视频」：清掉 URL 上的分类ID，与侧边栏「全部视频」入口行为一致 */
const goAllVideos = () => {
  router.push(routesConstants.VIDEO)
}

// 分类切换：回到第一页再加载（同一组件内切换分类不会重挂载，页码必须显式重置）
watch(categoryId, () => {
  reload()
})

// 登录态变化：登录成功补一次加载；退出登录清空列表，避免残留上一个账号的资源
watch(isLogin, (logged) => {
  if (logged) {
    fetchList()
    return
  }
  reset()
})

onMounted(async () => {
  // 首次加载：刷新后本地 token 校验未返回时先等恢复完成，避免把已登录用户误判为游客
  if (userStore.restoring) {
    // 复用同一份恢复请求；失败时 store 已清空本地凭证，错误提示由 Layout 统一给出
    await userStore.restoreLogin().catch(() => undefined)
  }
  fetchList()
})

// ==================== 添加视频资源弹窗（演示） ====================

/**
 * 添加方式
 * link   添加视频链接（填写外部视频直链）
 * single 上传单个视频（只选 1 个本地文件）
 * batch  上传视频集合（无序：一次选多个文件，不区分先后顺序）
 */
type AddMode = 'link' | 'single' | 'batch'

/** 添加视频资源弹窗是否显示 */
const addDialogVisible = ref(false)

/** 分类列表（弹窗内「所属分类」下拉的数据源，首次打开弹窗时懒加载） */
const categories = ref<FileCategoryVO[]>([])

/** 分类列表是否正在加载（驱动下拉的 loading 态） */
const categoryLoading = ref(false)

/** 分类列表是否已成功加载过：避免每次打开弹窗都重复请求 */
const categoryLoaded = ref(false)

/**
 * 本次添加的目标分类ID
 * 后端落库时会按 Long.parseLong(...) 解析该字段，必须传有效数字，
 * 因此「全部视频」下（无分类上下文）需要用户在弹窗内手动选择
 */
const addCategoryId = ref<number | null>(null)

/** 是否正在上传：上传期间禁止改动表单、切换方式与关闭弹窗 */
const uploading = ref(false)

/** 本次上传的文件进度（与「确定」时提交的文件列表一一对应） */
const uploadTasks = ref<UploadTaskProgress[]>([])

/** 当前选择的添加方式：默认「添加视频链接」 */
const addMode = ref<AddMode>('link')

/** 视频链接表单：url 必填，name 选填（留空时按链接自动命名） */
const addLinkForm = ref<{ url: string; name: string }>({ url: '', name: '' })

/** 视频链接表单实例：点「确定」时手动触发校验 */
const addLinkFormRef = ref<FormInstance>()

/** 链接解析结果是否正在请求：解析期间锁定整个链接交互区 */
const resolvingLink = ref(false)

/** 链接解析结果 */
const linkResults = ref<APIURLsInfoVO[]>([])

/** 默认全选解析结果；使用结果下标避免重复 URL 造成选中状态串联 */
const selectedLinkIndexes = ref<number[]>([])

/** 最近一次解析请求的序号，避免超时/关闭后的旧响应污染当前弹窗 */
let linkRequestSequence = 0

/** 解析请求超时时间：后端包含外部平台解析，给足等待时间但避免无限等待 */
const LINK_REQUEST_TIMEOUT_MS = 30_000

/** 视频链接校验规则：必填 + 必须是 http(s) 链接 */
const addLinkRules: FormRules = {
  url: [
    { required: true, message: '请输入视频链接', trigger: 'blur' },
    { pattern: /^https?:\/\/[^\s]+$/i, message: '请输入有效的 http:// 或 https:// 链接', trigger: 'blur' },
  ],
}

const selectedLinkCount = computed(() => selectedLinkIndexes.value.length)

const allLinksSelected = computed(
    () => linkResults.value.length > 0 && selectedLinkCount.value === linkResults.value.length,
)

const linkSelectionIndeterminate = computed(
    () => selectedLinkCount.value > 0 && !allLinksSelected.value,
)

/** 粘贴链接后自动解析；普通输入不会因为每次按键触发请求 */
const handleLinkPaste = () => {
  window.setTimeout(() => {
    if (!resolvingLink.value) {
      resolveLink()
    }
  }, 0)
}

const toggleLinkSelection = (index: number, checked: CheckboxValueType) => {
  const selected = new Set(selectedLinkIndexes.value)
  if (checked === true) {
    selected.add(index)
  } else {
    selected.delete(index)
  }
  selectedLinkIndexes.value = [...selected].sort((a, b) => a - b)
}

const toggleAllLinkSelection = (checked: CheckboxValueType) => {
  selectedLinkIndexes.value = checked === true ? linkResults.value.map((_, index) => index) : []
}

/** 请求结束后清理解析状态，但只允许当前请求修改页面 */
const resetLinkResolution = (requestId: number) => {
  if (requestId === linkRequestSequence) {
    resolvingLink.value = false
  }
}

/** 调用后端解析链接；超时后允许用户保留链接并重新解析 */
const resolveLink = async () => {
  if (resolvingLink.value) {
    return
  }
  const valid = await addLinkFormRef.value?.validateField('url').catch(() => false)
  if (valid === false) {
    return
  }
  const url = addLinkForm.value.url.trim()
  if (!url) {
    return
  }

  const requestId = ++linkRequestSequence
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), LINK_REQUEST_TIMEOUT_MS)
  resolvingLink.value = true
  linkResults.value = []
  selectedLinkIndexes.value = []

  try {
    const results = await resolveVideoLink(url, controller.signal)
    if (requestId !== linkRequestSequence) {
      return
    }
    linkResults.value = Array.isArray(results) ? results : []
    selectedLinkIndexes.value = linkResults.value.map((_, index) => index)
    if (linkResults.value.length === 0) {
      ElMessage.warning('未解析到视频内容，请检查链接后重试')
    } else {
      ElMessage.success(`已解析 ${linkResults.value.length} 个视频条目，默认全部选中`)
    }
  } catch (error) {
    if (requestId !== linkRequestSequence) {
      return
    }
    if (controller.signal.aborted) {
      ElMessage.warning('解析等待时间较长，已停止等待，请稍后重试')
    } else {
      ElMessage.error(resolveErrorMessage(error, '视频链接解析失败，请检查链接后重试'))
    }
  } finally {
    window.clearTimeout(timeoutId)
    resetLinkResolution(requestId)
  }
}

/** 方式二已选择的单个视频（auto-upload 关闭，不会真实上传） */
const singleFiles = ref<UploadUserFile[]>([])

/** 方式三已选择的视频集合（无序，可多选） */
const collectionFiles = ref<UploadUserFile[]>([])

/** 三种添加方式的说明卡（切换方式后下方的表单区域跟着变） */
const ADD_MODE_OPTIONS: { value: AddMode; title: string; desc: string; icon: string }[] = [
  {
    value: 'link',
    title: '添加视频链接',
    desc: '填写视频直链，由平台按链接添加',
    icon: 'M10.6 13.4l2.8-2.8M8.9 8.9l1.6-1.6a3.1 3.1 0 0 1 4.4 4.4l-1.6 1.6M15.1 15.1l-1.6 1.6a3.1 3.1 0 0 1-4.4-4.4l1.6-1.6',
  },
  {
    value: 'single',
    title: '上传单个视频',
    desc: '选择 1 个本地视频文件',
    icon: 'M4.5 6.5A2 2 0 0 1 6.5 4.5h11a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-11a2 2 0 0 1-2-2v-11ZM11 9.8l4.2 2.2-4.2 2.2V9.8Z',
  },
  {
    value: 'batch',
    title: '上传视频集合',
    desc: '一次选择多个视频，不分先后顺序',
    icon: 'M12 3.8l8.2 4.1-8.2 4.1-8.2-4.1L12 3.8ZM4.2 12.4l7.8 3.9 7.8-3.9M4.2 16.3l7.8 3.9 7.8-3.9',
  },
]

/**
 * 工具栏按钮
 * 添加：打开「添加视频资源」弹窗（视频链接 / 上传单个视频 / 上传视频集合（无序））
 * 查询：展开 / 收起文件名查询输入框
 * 批量删除：进入多选模式，卡片出现复选框（全选 / 删除选中在操作条上）
 */
const buttons = [
  {type: 'danger', text: '添加视频资源', action: 'add'},
  {type: 'info', text: '查询', action: 'query'},
  {type: 'warning', text: '批量删除', action: 'batchRemove'},
] as const

/** 加载当前登录用户的分类列表（失败时保留已有数据，仅提示，不阻断弹窗使用） */
const loadCategories = async () => {
  if (categoryLoading.value) {
    return
  }
  categoryLoading.value = true
  try {
    const list = await listUserFileCategories()
    categories.value = list ?? []
    categoryLoaded.value = true
  } catch (error) {
    ElMessage.warning(resolveErrorMessage(error, '分类列表加载失败，请稍后重试'))
  } finally {
    categoryLoading.value = false
  }
}

/**
 * 添加视频资源：未登录时列表依赖登录上下文，先引导登录，再打开弹窗
 * 上传必须归属分类：从侧边栏进入某分类时预选当前分类，「全部视频」下由用户当场选择
 */
const handleAdd = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录后再添加视频资源')
    return
  }
  addCategoryId.value = categoryId.value
  addDialogVisible.value = true
  if (!categoryLoaded.value) {
    await loadCategories()
  }
}

/** 单个视频模式：已有 1 个文件时再次选择，用新文件替换而不是提示超出数量 */
const handleSingleFileExceed = (files: File[]) => {
  const file = files[0] as UploadRawFile | undefined
  if (!file) {
    return
  }
  // genFileId：element-plus 提供的自增唯一标识，替换后列表不会出现重复 key
  file.uid = genFileId()
  singleFiles.value = [{name: file.name, raw: file}]
}

/**
 * 确定添加
 * 1）link   调用链接解析接口并展示可选结果；当前为演示模式，不执行落库
 * 2）single 走分片直传链路（features/videoUpload.ts）：单个文件
 * 3）batch  同一条链路，多个文件按顺序逐个上传（服务端按各自上传完成时间落库）
 */
const submitAdd = async () => {
  if (uploading.value || resolvingLink.value) {
    return
  }
  // 上传必须归属分类：后端落库时会解析 categoryId，为空会上传失败
  if (!addCategoryId.value) {
    ElMessage.warning('请选择所属分类')
    return
  }
  const targetCategoryId = addCategoryId.value

  if (addMode.value === 'link') {
    if (linkResults.value.length === 0) {
      await resolveLink()
      return
    }
    if (selectedLinkCount.value === 0) {
      ElMessage.warning('请至少选择一个视频条目')
      return
    }
    ElMessage.success(`演示完成：已选择 ${selectedLinkCount.value} 个视频条目`)
    addDialogVisible.value = false
    return
  }

  const files = collectSelectedFiles()
  if (files.length === 0) {
    ElMessage.warning(addMode.value === 'single' ? '请选择 1 个视频文件' : '请选择要上传的视频文件')
    return
  }

  // 进度列表与提交的文件一一对应（回调按 index 直接替换，同名文件也能区分）
  uploadTasks.value = files.map((file) => ({
    name: file.name,
    percent: 0,
    finishedParts: 0,
    totalParts: 0,
    status: 'pending' as const,
  }))
  uploading.value = true

  try {
    const summary = await uploadVideos(files, {
      categoryId: targetCategoryId,
      onProgress: (task, index) => {
        uploadTasks.value[index] = task
      },
    })
    await handleUploadResult(summary, targetCategoryId)
  } finally {
    uploading.value = false
  }
}

/** 弹窗关闭后重置：回到默认方式并清空表单 / 已选文件 / 上传进度，避免下次打开残留上一次的内容 */
const resetAddDialog = () => {
  linkRequestSequence++
  resolvingLink.value = false
  linkResults.value = []
  selectedLinkIndexes.value = []
  addMode.value = 'link'
  addLinkForm.value = {url: '', name: ''}
  singleFiles.value = []
  collectionFiles.value = []
  uploadTasks.value = []
  addCategoryId.value = null
  addLinkFormRef.value?.clearValidate()
}

/** 取当前方式下已选择的本地文件（el-upload 关闭了自动上传，raw 才是真实 File 对象） */
const collectSelectedFiles = (): File[] => {
  const list = addMode.value === 'single' ? singleFiles.value : collectionFiles.value
  return list.map((file) => file.raw).filter((raw): raw is UploadRawFile => Boolean(raw))
}

/** 切换添加方式（上传中禁止切换，避免表单状态与上传中的任务错位） */
const handleModeChange = (mode: AddMode) => {
  if (uploading.value || resolvingLink.value) {
    return
  }
  addMode.value = mode
}

/** 进度条状态：成功 / 失败用 el-progress 内置配色，其余走默认配色 */
const uploadProgressStatus = (task: UploadTaskProgress): 'success' | 'exception' | '' => {
  if (task.status === 'success') {
    return 'success'
  }
  if (task.status === 'failed') {
    return 'exception'
  }
  return ''
}

/** 上传进度行右侧的状态文案 */
const uploadStateText = (task: UploadTaskProgress) => {
  if (task.status === 'failed') {
    return task.error ? `失败：${task.error}` : '失败'
  }
  if (task.status === 'success') {
    return '已完成'
  }
  if (task.status === 'uploading') {
    return task.totalParts > 0 ? `上传中 ${task.finishedParts}/${task.totalParts} 片` : '准备中'
  }
  return '排队中'
}

/**
 * 上传结束后的提示与列表刷新
 * 目标分类与当前视图分类不一致时（例如在分类 A 下选了分类 B），额外提示一次，
 * 避免用户以为上传失败（其实只是当前视图不显示该分类的内容）
 */
const handleUploadResult = async (summary: UploadSummary, targetCategoryId: number) => {
  const targetName =
      categories.value.find((item) => item.id === targetCategoryId)?.categoryName ?? ''
  const categoryLabel = targetName ? `分类「${targetName}」` : '所选分类'

  if (summary.failed.length === 0) {
    ElMessage.success(`已上传 ${summary.succeeded.length} 个视频到${categoryLabel}`)
  } else if (summary.succeeded.length === 0) {
    ElMessage.error(`上传失败：${summary.failed[0]?.reason ?? '请稍后重试'}`)
    return
  } else {
    const failedDetail = summary.failed
        .map((item) => `${item.name}（${item.reason}）`)
        .join('；')
    ElMessage.warning(
        `成功 ${summary.succeeded.length} 个，失败 ${summary.failed.length} 个：${failedDetail}`,
    )
  }

  const categoryChanged = targetCategoryId !== categoryId.value
  // 关闭弹窗（触发 resetAddDialog）后再强制刷新列表，绕过外壳的条件指纹去重
  addDialogVisible.value = false
  await refresh()

  if (categoryChanged) {
    ElMessage.info(`新视频已归入${categoryLabel}，当前视图不显示该分类内容`)
  }
}

/** 动作名 → 具体处理函数；新增按钮时只改这里和 buttons，模板不用动 */
const actionMap: Record<string, () => void> = {
  add: handleAdd,
  query: toggleSearch,
  batchRemove: enterBatchMode,
}

/** 统一入口：按 action 派发，避免在模板里写一堆 if-else */
const handleAction = (action: string) => {
  actionMap[action]?.()
}
</script>

<template>
  <div class="video-page">
    <!--
      工具栏：
      第一行：标题 + 总数 + 条件标记 + 操作按钮（添加 / 查询 / 批量删除）+ 刷新
      第二行：查询输入框（点「查询」滑动展开，与批量删除互斥）
      第三行：批量删除操作条（进入多选模式后出现：全选本页 / 删除选中 / 取消）
    -->
    <div class="video-toolbar">
      <div class="video-toolbar-row">
        <div class="video-toolbar-left">
          <div class="video-title">
            <span class="video-title-text">视频资源</span>
            <span v-if="status === 'success'" class="video-count">共 {{ total }} 个</span>
            <span v-if="inCategory" class="video-filter-chip">已按左侧分类筛选</span>
            <span v-if="isSearching" class="video-filter-chip">文件名含「{{ keyword.trim() }}」</span>
          </div>
          <el-button class="video-operation"
                     v-for="button in buttons"
                     :key="button.text"
                     :type="button.type"
                     text
                     bg
                     @click="handleAction(button.action)"
          >
            {{ button.text }}
          </el-button>
        </div>
        <el-button class="video-refresh" text :loading="fetching" @click="refresh">
          <el-icon>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M12 5V2L7 6l5 4V7a5 5 0 1 1-5 5H5a7 7 0 1 0 7-7Z"/>
            </svg>
          </el-icon>
          <span>刷新</span>
        </el-button>
      </div>

      <!--
        查询输入框：点「查询」后向下滑动展开；收起时清空关键字并回到全部视频
        element-plus 点清空按钮时会依次抛出 clear 与 input：clear 立即刷新、input 走防抖，
        两个事件都保留，外壳按条件指纹去重，实际只会请求一次
      -->
      <div class="video-search-wrap" :class="{ 'is-open': searchVisible }">
        <el-input
            v-model="keyword"
            size="small"
            placeholder="输入视频文件名查询"
            clearable
            class="video-search-input"
            @input="handleKeywordInput"
            @keyup.enter="handleKeywordEnter"
            @clear="handleKeywordClear"
        >
          <template #prefix>
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M10.5 3a7.5 7.5 0 1 0 4.55 13.46l4.24 4.25 1.41-1.42-4.24-4.24A7.5 7.5 0 0 0 10.5 3Zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11Z"/>
            </svg>
          </template>
        </el-input>
      </div>

      <!-- 批量删除操作条：全选本页 / 删除选中 / 取消（未勾选时删除按钮不可点） -->
      <div v-if="batchMode" class="video-batch-bar">
        <el-checkbox
            :model-value="isAllSelected"
            :indeterminate="isIndeterminate"
            @change="(checked: CheckboxValueType) => toggleSelectAll(checked === true)"
        >
          全选本页
        </el-checkbox>
        <span class="video-batch-tip">已选 {{ selectedIds.length }} 个</span>
        <div class="video-batch-actions">
          <el-button
              type="danger"
              size="small"
              :disabled="selectedIds.length === 0 || deleting"
              :loading="deleting"
              @click="removeSelected"
          >
            删除选中{{ selectedIds.length ? `(${selectedIds.length})` : '' }}
          </el-button>
          <el-button size="small" :disabled="deleting" @click="exitBatchMode">取消</el-button>
        </div>
      </div>
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
      <el-empty v-else-if="!isLogin" description="请先登录后查看视频资源"/>

      <!-- 加载失败：保留重试入口 -->
      <el-empty v-else-if="status === 'error'" description="视频列表加载失败">
        <el-button @click="refresh">重新加载</el-button>
      </el-empty>

      <!-- 空态：区分「未找到匹配的视频」「该分类下暂无视频」「暂无视频资源」 -->
      <el-empty
          v-else-if="status === 'success' && resources.length === 0"
          :description="isSearching ? '未找到匹配的视频' : (inCategory ? '该分类下暂无视频' : '暂无视频资源')"
      >
        <el-button v-if="!isSearching && inCategory" @click="goAllVideos">查看全部视频</el-button>
      </el-empty>

      <!-- 资源卡片：封面 + 文件名 + 大小 / 创建时间 + 状态角标 -->
      <div v-else class="video-grid">
        <article
            v-for="item in resources"
            :key="item.id"
            class="resource-card"
            :class="{ 'is-selected': batchMode && selectedIds.includes(item.id) }"
        >
          <div class="resource-cover">
            <!-- 多选模式：封面左上角出现复选框（@click.stop 避免冒泡触发卡片其他行为） -->
            <el-checkbox
                v-if="batchMode"
                class="resource-checkbox"
                :model-value="selectedIds.includes(item.id)"
                @change="(checked: CheckboxValueType) => toggleSelect(item.id, checked === true)"
                @click.stop
            />
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
            <!-- 非多选模式才展示单条操作：多选模式下只用封面复选框勾选，避免操作混淆 -->
            <template v-if="!batchMode">
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
              <!-- 删除单条：二次确认后调用 DELETE /file/resource/{id} -->
              <el-button
                  class="resource-delete"
                  text
                  :disabled="deleting"
                  @click="remove(item)"
              >
                删除
              </el-button>
            </template>
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
          @current-change="changePage"
      />
    </div>

    <!--
      添加视频资源弹窗：
      第一步选择「所属分类」与添加方式（添加视频链接 / 上传单个视频 / 上传视频集合（无序）），
      第二步按所选方式展示对应表单；上传走后端分片直传链路（编排见 features/videoUpload.ts）
    -->
    <el-dialog
        v-model="addDialogVisible"
        title="添加视频资源"
        width="640px"
        :close-on-click-modal="false"
        :close-on-press-escape="!uploading && !resolvingLink"
        :show-close="!uploading && !resolvingLink"
        @closed="resetAddDialog"
    >
      <!-- 第一步：选择添加方式（单选卡片） -->
      <div class="add-mode-list">
        <div
            v-for="mode in ADD_MODE_OPTIONS"
            :key="mode.value"
            class="add-mode-card"
            :class="{'is-active': addMode === mode.value, 'is-disabled': uploading}"
            @click="handleModeChange(mode.value)"
        >
          <svg
              class="add-mode-icon"
              viewBox="0 0 24 24"
              width="20"
              height="20"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
          >
            <path :d="mode.icon"/>
          </svg>
          <span class="add-mode-title">{{ mode.title }}</span>
          <span class="add-mode-desc">{{ mode.desc }}</span>
        </div>
      </div>

      <!-- 所属分类：上传必须归属分类（后端按分类落库），「全部视频」下需要用户当场选择 -->
      <div class="add-category">
        <span class="add-category-label">所属分类</span>
        <el-select
            v-model="addCategoryId"
            class="add-category-select"
            placeholder="请选择所属分类"
            :loading="categoryLoading"
            :disabled="uploading"
        >
          <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
          />
        </el-select>
        <span v-if="categoryLoaded && categories.length === 0" class="add-category-tip">
          暂无分类，请先在左侧创建一个分类
        </span>
        <span v-else-if="uploading" class="add-category-tip">上传中，请勿关闭弹窗</span>
      </div>

      <!-- 第二步：按所选方式展示对应内容 -->
      <!-- 方式一：添加视频链接 -->
      <div v-if="addMode === 'link'" class="add-panel">
        <el-form ref="addLinkFormRef" :model="addLinkForm" :rules="addLinkRules" label-width="72px">
          <el-form-item label="视频链接" prop="url">
            <el-input
                v-model="addLinkForm.url"
                maxlength="500"
                clearable
                :disabled="resolvingLink"
                placeholder="粘贴视频链接后自动解析"
                @paste="handleLinkPaste"
            />
          </el-form-item>
          <el-form-item label="视频名称" prop="name">
            <el-input
                v-model="addLinkForm.name"
                maxlength="100"
                clearable
                :disabled="resolvingLink"
                placeholder="选填，留空时按链接自动命名"
            />
          </el-form-item>
        </el-form>
        <div v-if="resolvingLink" class="add-panel-tip is-loading">
          正在解析视频链接，请耐心等待；解析期间不能修改或关闭弹窗（最长等待 30 秒）
        </div>
        <div v-else class="add-panel-tip">
          支持 http / https 链接；粘贴后自动解析，也可以点击「确定」开始解析
        </div>
        <div v-if="linkResults.length > 0" class="link-result-panel">
          <div class="link-result-head">
            <span>解析结果（已选 {{ selectedLinkCount }} / {{ linkResults.length }}）</span>
            <el-checkbox
                :model-value="allLinksSelected"
                :indeterminate="linkSelectionIndeterminate"
                :disabled="resolvingLink"
                @change="toggleAllLinkSelection"
            >
              全选
            </el-checkbox>
          </div>
          <div class="link-result-list">
            <div
                v-for="(item, index) in linkResults"
                :key="`${item.url}-${index}`"
                class="link-result-item"
                :class="{ 'is-selected': selectedLinkIndexes.includes(index) }"
            >
              <el-checkbox
                  :model-value="selectedLinkIndexes.includes(index)"
                  :disabled="resolvingLink"
                  @change="(checked: CheckboxValueType) => toggleLinkSelection(index, checked)"
              />
              <div class="link-result-info">
                <div class="link-result-title" :title="item.title">{{ item.title || '未命名视频' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 方式二：上传单个视频 -->
      <div v-else-if="addMode === 'single'" class="add-panel">
        <el-upload
            v-model:file-list="singleFiles"
            class="add-upload"
            action="#"
            drag
            :accept="VIDEO_ACCEPT"
            :auto-upload="false"
            :limit="1"
            :disabled="uploading"
            :on-exceed="handleSingleFileExceed"
        >
          <div class="add-upload-inner">
            <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor"
                 stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 16V4m0 0L7.5 8.5M12 4l4.5 4.5M4.5 19.5h15"/>
            </svg>
            <p class="add-upload-text">将视频拖到此处，或<em>点击选择</em></p>
            <p class="add-upload-tip">仅支持 1 个视频文件，再次选择会替换已选文件</p>
          </div>
        </el-upload>
        <div class="add-panel-tip">上传前会从视频中随机截取画面作为封面</div>
      </div>

      <!-- 方式三：上传视频集合（无序） -->
      <div v-else class="add-panel">
        <el-upload
            v-model:file-list="collectionFiles"
            class="add-upload"
            action="#"
            drag
            multiple
            :accept="VIDEO_ACCEPT"
            :auto-upload="false"
            :disabled="uploading"
        >
          <div class="add-upload-inner">
            <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor"
                 stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3.8l8.2 4.1-8.2 4.1-8.2-4.1L12 3.8ZM4.2 12.4l7.8 3.9 7.8-3.9M4.2 16.3l7.8 3.9 7.8-3.9"/>
            </svg>
            <p class="add-upload-text">将多个视频拖到此处，或<em>点击选择</em></p>
            <p class="add-upload-tip">可一次选择多个视频，不区分先后顺序</p>
          </div>
        </el-upload>
        <div class="add-panel-tip">
          集合按「无序」处理：不排序、不分组，逐个上传并按完成时间展示
          <template v-if="collectionFiles.length > 0">（已选择 {{ collectionFiles.length }} 个）</template>
        </div>
      </div>

      <!-- 上传进度：每个文件一条，上传中 / 完成 / 失败都在这里反馈 -->
      <div v-if="uploadTasks.length > 0" class="add-progress">
        <div
            v-for="(task, index) in uploadTasks"
            :key="`${task.name}-${index}`"
            class="add-progress-item"
        >
          <div class="add-progress-head">
            <span class="add-progress-name" :title="task.name">{{ task.name }}</span>
            <span
                class="add-progress-state"
                :class="{'is-failed': task.status === 'failed'}"
            >{{ uploadStateText(task) }}</span>
          </div>
          <el-progress
              :percentage="task.percent"
              :stroke-width="6"
              :status="uploadProgressStatus(task)"
          />
        </div>
      </div>

      <template #footer>
        <el-button :disabled="uploading || resolvingLink" @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading || resolvingLink" @click="submitAdd">
          {{ addMode === 'link' && linkResults.length > 0 ? '完成' : '确定' }}
        </el-button>
      </template>
    </el-dialog>
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
/* 纵向排列：第一行 标题 + 操作按钮 + 刷新；下面按需展开 查询输入框 / 批量删除操作条 */
.video-toolbar {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

/* 第一行：左侧分组靠左，刷新按钮靠右 */
.video-toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

/* 左侧分组：标题 + 操作按钮，整体靠左 */
.video-toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap; /* 窄屏时按钮换行，不挤压标题 */
}

.video-operation {
  margin-left: 0; /* 清掉 Element Plus 相邻按钮的默认左边距，交给 gap 控制 */
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

/* 查询输入框：默认高度为 0，点「查询」后向下滑出（与侧边栏搜索框交互一致） */
.video-search-wrap {
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  transition: max-height 0.25s ease, opacity 0.25s ease, margin-top 0.25s ease;
}

.video-search-wrap.is-open {
  max-height: 44px;
  margin-top: 4px;
  opacity: 1;
}

/* 输入框限宽：避免在宽屏上被拉成整行 */
.video-search-input {
  max-width: 280px;
}

/* 批量删除操作条：全选本页 + 已选数量 + 删除选中 / 取消 */
.video-batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  padding-top: 6px;
  border-top: 1px dashed #e4e7ed;
}

.video-batch-bar :deep(.el-checkbox) {
  height: 24px;
  font-size: 12px;
}

.video-batch-tip {
  font-size: 12px;
  color: #909399;
}

.video-batch-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
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

/* 卡片网格：一行固定 6 列，每页 36 条刚好 6 行（侧边栏折叠 / 展开时列数不变） */
.video-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.resource-card {
  display: flex;
  flex-direction: column;
  min-width: 0;
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
  flex-wrap: wrap;
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

/* 删除单条的按钮：默认灰字，hover 变红（保持整体黑白灰基底） */
.resource-delete {
  font-size: 12px;
  color: #909399;
}

.resource-delete:hover:not(.is-disabled) {
  color: #c45656;
}

/* 多选模式下的复选框：叠在封面左上角，加半透明白底保证在封面上清晰可见 */
.resource-checkbox {
  position: absolute;
  top: 6px;
  left: 6px;
  z-index: 1;
  height: 24px;
  margin-right: 0;
  padding: 0 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.9);
}

/* 已勾选的卡片：描边加深，与未选中区分 */
.resource-card.is-selected {
  border-color: #1f2d3d;
  box-shadow: 0 0 0 1px rgba(31, 45, 61, 0.15);
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

/* ==================== 添加视频资源弹窗（演示） ==================== */
/* 第一步：三种添加方式并排展示，选中的卡片加深描边 */
.add-mode-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.add-mode-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.add-mode-card:hover {
  border-color: #c0c4cc;
}

.add-mode-card.is-active {
  border-color: #1f2d3d;
  background: #f7f8fa;
  box-shadow: 0 0 0 1px rgba(31, 45, 61, 0.15);
}

/* 上传中禁止切换方式：卡片置灰并禁用 hover 反馈 */
.add-mode-card.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.add-mode-card.is-disabled:hover {
  border-color: #e4e7ed;
}

.add-mode-icon {
  color: #909399;
}

.add-mode-card.is-active .add-mode-icon {
  color: #1f2d3d;
}

.add-mode-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.add-mode-desc {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

/* 所属分类：下拉与说明文字同一行排布（上传必须归属分类） */
.add-category {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
}

.add-category-label {
  flex: none;
  font-size: 13px;
  color: #606266;
}

.add-category-select {
  width: 220px;
}

.add-category-tip {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

/* 上传进度：文件名 + 状态 + 进度条，文件较多时限高滚动 */
.add-progress {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 190px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
  overflow: auto;
}

.add-progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.add-progress-name {
  flex: 1;
  overflow: hidden;
  font-size: 12px;
  color: #1f2d3d;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-progress-state {
  flex: none;
  max-width: 55%;
  overflow: hidden;
  font-size: 12px;
  color: #909399;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-progress-state.is-failed {
  color: #f56c6c;
}

/* 第二步：表单 / 上传区域 */
.add-panel {
  margin-top: 14px;
}

.add-panel-tip {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.add-panel-tip.is-loading {
  color: #409eff;
}

/* 链接解析结果：限制高度，避免分集过多时撑满弹窗 */
.link-result-panel {
  margin-top: 14px;
  border-top: 1px solid #f0f2f5;
  padding-top: 12px;
}

.link-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}

.link-result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 260px;
  overflow: auto;
}

.link-result-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #ffffff;
}

.link-result-item.is-selected {
  border-color: #1f2d3d;
  background: #f7f8fa;
}

.link-result-cover {
  flex: none;
  width: 72px;
  height: 42px;
  border-radius: 4px;
  object-fit: cover;
  background: #f5f7fa;
}

.link-result-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 11px;
}

.link-result-info {
  min-width: 0;
}

.link-result-title,
.link-result-url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-result-title {
  font-size: 13px;
  color: #1f2d3d;
}

.link-result-url {
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
}

/* 上传区域：撑满弹窗宽度，拖拽区收紧内边距避免弹窗过高 */
.add-upload {
  width: 100%;
}

.add-upload :deep(.el-upload-dragger) {
  padding: 18px 12px;
}

/* 文件列表限高：集合模式下文件较多时弹窗不无限变高 */
.add-upload :deep(.el-upload-list) {
  max-height: 180px;
  overflow: auto;
}

.add-upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #909399;
}

.add-upload-text {
  margin: 0;
  font-size: 13px;
  color: #606266;
}

.add-upload-text em {
  font-style: normal;
  color: #1f2d3d;
}

.add-upload-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

</style>
