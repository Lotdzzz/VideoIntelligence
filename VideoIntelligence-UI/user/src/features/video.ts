import {computed, onScopeDispose, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  RESOURCE_PAGE_SIZE,
  batchDeleteResources,
  deleteResource,
  listResources,
} from '@/api/fileResource'
import {resolveErrorMessage} from '@/utils/errorMessage'
import type {FileDTO} from '@/types/file/fileDTO'
import type {FileVO} from '@/types/file/fileVO'

/**
 * 视频操作外壳（features 层）
 *
 * 把「视频」这个业务对象的操作收敛到一处，页面只负责传条件与绑定模板：
 * - 列表：分页查询 /file/resource/list（支持文件名模糊查询、按分类、按登录用户过滤）
 * - 删除：DELETE /file/resource/{id}
 * - 批量删除：DELETE /file/resource/batchDelete（进入多选模式 → 勾选 → 批量删除）
 *
 * 交互约定（与侧边栏分类管理保持一致）：
 * 1）列表状态机：idle 未加载 / loading 加载中 / success 完成 / error 失败
 * 2）查询与多选互斥：工具条空间有限，同一时间只保留一种
 * 3）删除成功后先本地移除给即时反馈，再重新拉取与服务端校准；本页被删空时自动回退一页
 * 4）请求去重：条件指纹（页码 + 用户 + 分类 + 关键字）与上次已成功的请求一致时不再发起，
 *    避免同一次操作触发多次刷新（element-plus 输入框点清空按钮会同时抛出 clear 与 input 两个事件）
 */

/** 面板状态：idle 未加载、loading 加载中、success 加载完成、error 加载失败 */
export type VideoPanelStatus = 'idle' | 'loading' | 'success' | 'error'

/** 查询防抖延迟（ms）：停止输入后才发起请求，避免每敲一个字都打接口 */
const SEARCH_DEBOUNCE_MS = 300

export interface UseVideoListOptions {
  /**
   * 组装列表查询条件（userId / categoryId 等由页面提供）
   * 每次请求前调用，页面上的条件（如 URL 上的分类ID）变化后无需通知外壳
   */
  buildQuery?: () => FileDTO

  /** 是否具备请求条件（页面传入「是否已登录」；返回 false 时清空列表且不发请求） */
  canFetch?: () => boolean
}

/** 列表请求选项 */
export interface FetchListOptions {
  /**
   * 条件指纹与上次成功请求一致时也强制发起
   * 用于「手动刷新」「整批重载（切分类）」「本地已改数据后与服务端校准（删除）」等场景
   */
  force?: boolean
}

export function useVideoList(options: UseVideoListOptions = {}) {
  const buildQuery = options.buildQuery ?? ((): FileDTO => ({}))
  const canFetch = options.canFetch ?? (() => true)

  // ==================== 列表状态 ====================

  /** 面板状态 */
  const status = ref<VideoPanelStatus>('idle')

  /** 当前页视频列表 */
  const records = ref<FileVO[]>([])

  /** 总条数（分页组件用） */
  const total = ref(0)

  /** 当前页码 */
  const pageNum = ref(1)

  /** 请求是否进行中：既驱动刷新按钮的 loading，也阻止重复请求 */
  const fetching = ref(false)

  /** 最近一次请求的条件指纹（页码 + 用户 + 分类 + 关键字）：用于识别重复请求 */
  let lastFetchKey = ''

  /** 请求飞行期间条件是否变过：本次请求结束后需要按最新条件补查一次 */
  let refetchPending = false

  // ==================== 查询条件 ====================

  /** 查询关键字（后端按 original_name 模糊匹配） */
  const keyword = ref('')

  /** 查询输入框是否展开（点工具栏「查询」展开 / 收起） */
  const searchVisible = ref(false)

  /** 查询防抖定时器 */
  let searchTimer: ReturnType<typeof setTimeout> | null = null

  // ==================== 多选（批量删除） ====================

  /** 是否处于多选模式（点工具栏「批量删除」进入） */
  const batchMode = ref(false)

  /** 多选模式下已勾选的视频ID */
  const selectedIds = ref<number[]>([])

  /** 删除请求是否进行中（单条 / 批量共用，避免重复提交） */
  const deleting = ref(false)

  // ==================== 计算属性 ====================

  /** 是否处于查询态（用于空态文案区分「暂无视频」与「未找到匹配的视频」） */
  const isSearching = computed(() => keyword.value.trim() !== '')

  /** 是否已全选当前页（驱动多选操作条上的「全选」复选框） */
  const isAllSelected = computed(
      () => records.value.length > 0 && selectedIds.value.length === records.value.length
  )

  /** 是否半选（部分勾选时复选框显示为横线态） */
  const isIndeterminate = computed(
      () => selectedIds.value.length > 0 && !isAllSelected.value
  )

  /** 组装完整查询条件：页面提供的基础条件 + 外壳维护的文件名关键词 */
  const buildQueryParams = (): FileDTO => ({
    ...buildQuery(),
    originalName: keyword.value.trim() || null,
  })

  /**
   * 当前条件指纹：页码 + 用户 + 分类 + 关键字
   * 指纹一致即「要查的数据完全一样」，重复触发（如输入框清空时 clear 与 input 先后触发）无需再打接口
   */
  const buildRequestKey = () => {
    const query = buildQueryParams()
    return JSON.stringify([
      pageNum.value,
      query.userId ?? null,
      query.categoryId ?? null,
      query.originalName ?? null,
    ])
  }

  // ==================== 列表请求 ====================

  /** 清空列表与分页（未登录 / 退出登录时调用） */
  const reset = () => {
    records.value = []
    total.value = 0
    pageNum.value = 1
    status.value = 'idle'
  }

  /**
   * 拉取当前页：关键词 / 页码 / 分类条件变化后都由它统一发起请求
   *
   * 去重规则（避免同一次操作刷新多次）：
   * 1）条件指纹与上次已成功的请求一致：直接跳过（force 可绕过，如工具栏「刷新」）
   * 2）请求飞行中：条件相同视为重复触发直接忽略；条件已变则记一笔待补查，
   *    等本次请求结束后自动按最新条件再查一次，避免列表停在旧条件的数据上
   */
  const fetchList = async ({force = false}: FetchListOptions = {}) => {
    if (!canFetch()) {
      // 接口依赖登录上下文，未登录不发请求（避免控制台一片 401）
      reset()
      return
    }

    const requestKey = buildRequestKey()

    if (fetching.value) {
      refetchPending = refetchPending || requestKey !== lastFetchKey
      return
    }

    if (!force && requestKey === lastFetchKey && status.value === 'success') {
      // 条件没变且已查过：不再重复请求，避免列表骨架屏闪两次
      return
    }

    fetching.value = true
    lastFetchKey = requestKey
    status.value = 'loading'
    try {
      const page = await listResources(pageNum.value, RESOURCE_PAGE_SIZE, buildQueryParams())
      records.value = page.records ?? []
      total.value = page.total ?? 0
      status.value = 'success'
    } catch (error) {
      status.value = 'error'
      ElMessage.warning(resolveErrorMessage(error, '视频列表加载失败，请稍后重试'))
    } finally {
      fetching.value = false
      if (refetchPending) {
        refetchPending = false
        // 请求期间条件变过：补一次，保证列表对应最新条件（此时 fetching 已复位）
        await fetchList()
      }
    }
  }

  /**
   * 刷新当前页（工具栏「刷新」与错误态「重新加载」共用）
   * 手动刷新不参与请求去重：条件未变也要重新拉一次
   */
  const refresh = () => fetchList({force: true})

  /** 翻页：页码由分页组件传入，切换后重新拉当前页 */
  const changePage = (page: number) => {
    pageNum.value = page
    // 多选模式下翻页：上一页的勾选已经不可见，统一清空避免误删
    if (batchMode.value) {
      selectedIds.value = []
    }
    fetchList()
  }

  /** 按当前条件从第一页重新加载（切换分类等场景调用） */
  const reload = async () => {
    pageNum.value = 1
    records.value = []
    total.value = 0
    // 切换分类后列表整批换掉，多选状态同样不再可信，一并清空
    if (batchMode.value) {
      selectedIds.value = []
    }
    // 本地列表已清空，条件指纹可能与上次相同，这里必须强制请求
    await fetchList({force: true})
  }

  // ==================== 查询（按文件名） ====================

  /** 按关键字查询：回到第一页（清空关键字等价于查询全部） */
  const query = async () => {
    pageNum.value = 1
    await fetchList()
  }

  /** 取消防抖定时器（回车 / 清空 / 收起搜索框时调用） */
  const cancelSearchTimer = () => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
  }

  /** 输入框输入：停止输入 300ms 后才真正查询 */
  const handleKeywordInput = () => {
    cancelSearchTimer()
    searchTimer = setTimeout(() => {
      searchTimer = null
      query()
    }, SEARCH_DEBOUNCE_MS)
  }

  /** 输入框回车：取消防抖，立即查询 */
  const handleKeywordEnter = () => {
    cancelSearchTimer()
    query()
  }

  /**
   * 输入框清空按钮：清掉关键字并立即回到全部视频
   * element-plus 的 clear 会紧接着再抛一次 input 事件，那次会走防抖查询；
   * 条件指纹相同，外壳会识别为重复请求直接跳过，所以点击清空只会实际请求一次
   */
  const handleKeywordClear = () => {
    keyword.value = ''
    cancelSearchTimer()
    query()
  }

  /**
   * 点工具栏「查询」：展开 / 收起查询输入框
   * 展开时退出多选模式（与批量删除互斥）；收起时清空关键字并回到全部视频
   */
  const toggleSearch = () => {
    searchVisible.value = !searchVisible.value
    if (searchVisible.value) {
      if (batchMode.value) {
        exitBatchMode()
      }
      return
    }
    keyword.value = ''
    cancelSearchTimer()
    query()
  }

  // ==================== 删除 ====================

  /** 本地同步移除（先给即时反馈，再由 fetchList 静默校准） */
  const removeLocally = (ids: number[]) => {
    const idSet = new Set(ids)
    records.value = records.value.filter((item) => !idSet.has(item.id))
    total.value = Math.max(0, total.value - ids.length)
  }

  /** 删除成功后收尾：本地移除 + 页码校正 + 重新拉取 */
  const syncAfterRemoved = async (ids: number[]) => {
    removeLocally(ids)
    // 当前页被删空且不在第一页：回退一页，避免停在空白页
    if (records.value.length === 0 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    // 本地已移除记录，条件指纹可能没变，但必须与服务端重新校准
    await fetchList({force: true})
  }

  /** 删除单个视频（卡片上的「删除」按钮） */
  const remove = async (item: FileVO) => {
    if (deleting.value) {
      return
    }
    try {
      await ElMessageBox.confirm(
          `确定删除视频「${item.originalName}」吗？删除后不可恢复`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
    } catch {
      // 用户取消 / 关闭弹窗：不做任何处理
      return
    }

    deleting.value = true
    try {
      await deleteResource(item.id)
      ElMessage.success('删除成功')
      await syncAfterRemoved([item.id])
    } catch (error) {
      ElMessage.warning(resolveErrorMessage(error, '视频删除失败，请稍后重试'))
    } finally {
      deleting.value = false
    }
  }

  // ==================== 多选（批量删除） ====================

  /** 进入多选模式（点工具栏「批量删除」）：列表出现复选框 */
  const enterBatchMode = () => {
    batchMode.value = true
    selectedIds.value = []
    // 与查询互斥：避免工具条同时出现输入框与多选操作条（此时回到全部视频）
    if (searchVisible.value) {
      searchVisible.value = false
      keyword.value = ''
      cancelSearchTimer()
      query()
    }
  }

  /** 退出多选模式（清空勾选） */
  const exitBatchMode = () => {
    batchMode.value = false
    selectedIds.value = []
  }

  /** 勾选 / 取消勾选单个视频 */
  const toggleSelect = (id: number, checked: boolean) => {
    if (checked) {
      if (!selectedIds.value.includes(id)) {
        selectedIds.value = [...selectedIds.value, id]
      }
      return
    }
    selectedIds.value = selectedIds.value.filter((item) => item !== id)
  }

  /** 全选 / 取消全选（只针对当前页展示的记录） */
  const toggleSelectAll = (checked: boolean) => {
    selectedIds.value = checked ? records.value.map((item) => item.id) : []
  }

  /** 批量删除选中的视频（DELETE /file/resource/batchDelete） */
  const removeSelected = async () => {
    const ids = [...selectedIds.value]
    if (ids.length === 0 || deleting.value) {
      return
    }
    try {
      await ElMessageBox.confirm(
          `确定删除选中的 ${ids.length} 个视频吗？删除后不可恢复`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
    } catch {
      // 用户取消 / 关闭弹窗：不做任何处理
      return
    }

    deleting.value = true
    try {
      await batchDeleteResources(ids)
      ElMessage.success(`已删除 ${ids.length} 个视频`)
      exitBatchMode()
      await syncAfterRemoved(ids)
    } catch (error) {
      ElMessage.warning(resolveErrorMessage(error, '批量删除失败，请稍后重试'))
    } finally {
      deleting.value = false
    }
  }

  // 组件卸载时清掉未触发的防抖请求，避免内存泄漏
  onScopeDispose(cancelSearchTimer)

  return {
    // 列表
    status,
    records,
    total,
    pageNum,
    fetching,
    // 查询
    keyword,
    searchVisible,
    isSearching,
    // 多选
    batchMode,
    selectedIds,
    deleting,
    isAllSelected,
    isIndeterminate,
    // 方法
    fetchList,
    refresh,
    changePage,
    reload,
    query,
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
  }
}
