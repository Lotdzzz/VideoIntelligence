<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElMessage,
  ElMessageBox,
  type CheckboxValueType,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import {
  VIDEO_CATEGORY_ALL_INDEX,
  VIDEO_CATEGORY_MENU_PREFIX,
  routesConstants,
} from '@/constants/routesConstants'
import {
  addFileCategory,
  batchDeleteFileCategories,
  CATEGORY_PAGE_SIZE,
  deleteFileCategory,
  listUserFileCategories,
  searchUserFileCategories,
  updateFileCategory,
} from '@/api/fileCategory'
import { useUserStore } from '@/stores/user'
import { resolveAuthErrorMessage } from '@/utils/authError'
import { resolveErrorMessage } from '@/utils/errorMessage'
import { resolveFileUrl } from '@/utils/file'
import LoginDialog from '@/components/LoginDialog.vue'
import { logout } from '@/api/Logout'
import type { FileCategoryVO } from '@/types/file/fileCategoryVO'
import type { FileCategoryDTO } from '@/types/file/fileCategoryDTO'
import type { PageResult } from '@/types/result'

const isCollapse = ref(false)

// 切换账户弹窗是否显示
const loginDialogVisible = ref(false)

const sidebarWidth = computed(() => (isCollapse.value ? '64px' : '260px'))

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// ==================== 导航与面包屑 ====================

const route = useRoute()

/** 菜单跳转使用的 router：el-menu 未开启 router 模式，跳转统一在 handleMenuSelect / handleMenuOpen 里处理 */
const router = useRouter()

/** 首页标题：首页已在面包屑首位固定展示，动态层级里需要排除它，避免出现「首页 / 首页」 */
const HOME_TITLE = '首页'

/**
 * 当前选中的视频分类ID
 * 由 URL 的 query 决定（/video?categoryId=3），空值 / 非法值统一按「全部视频」处理；
 * 这样刷新页面、直接访问链接、浏览器前进后退都能还原选中态
 */
const activeCategoryId = computed<number | null>(() => {
  const raw = route.query.categoryId
  // query 允许同名多值（?categoryId=1&categoryId=2），取第一个即可
  const value = Array.isArray(raw) ? raw[0] : raw
  if (value === undefined || value === null || value === '') {
    return null
  }
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
})

/**
 * 当前激活的菜单项
 * 1）首页等普通路由：直接取当前路由地址，刷新页面或直接访问链接时选中态都不会回退到首页
 * 2）/video：选中态落到具体分类项上（未选分类时落到「全部视频」项），
 *    否则点分类跳转后左侧高亮会回退，看不出当前在看哪个分类
 */
const activeMenu = computed(() => {
  if (route.path === routesConstants.VIDEO) {
    return activeCategoryId.value === null
      ? VIDEO_CATEGORY_ALL_INDEX
      : `${VIDEO_CATEGORY_MENU_PREFIX}${activeCategoryId.value}`
  }
  return route.path
})

/**
 * 侧边栏默认展开的菜单
 * 直接访问 /video?categoryId=x（刷新页面 / 分享链接 / 前进后退）时自动展开「视频」子菜单，
 * 让选中的分类项可见；不选分类时保持收起，与原有交互一致
 */
const defaultOpeneds = computed(() =>
  activeCategoryId.value === null ? [] : [routesConstants.VIDEO]
)

/**
 * 面包屑（动态层级）：从当前匹配的路由链中筛选需要展示的记录
 * 1）必须有 meta.title（标题在 router 中统一维护）
 * 2）排除首页（已在面包屑首位固定展示）
 * 3）排除带 children 的布局父路由（它们只负责挂载 Layout，自身不渲染页面）
 */
const breadcrumbs = computed(() =>
  route.matched
    .filter((item) => {
      const title = item.meta.title as string | undefined
      return (
        Boolean(title) &&
        title !== HOME_TITLE &&
        !(item.children && item.children.length > 0)
      )
    })
    .map((item) => ({
      // 子路由 path 为空串（如 /video 下的默认子路由）时回退为当前完整路径，保证可点击跳转
      path: item.path || route.path,
      title: item.meta.title as string,
    }))
)

// ==================== 登录态 ====================

/** 用户信息统一由 pinia store 接收（登录态、头像、昵称等都从这里读） */
const userStore = useUserStore()

/** 是否已登录 */
const isLogin = computed(() => userStore.isLogin)

/** 游客兜底展示信息 */
const GUEST_USER = {
  name: '游客',
  role: '未登录',
  avatarText: '游',
  avatarUrl: '',
}

/** 正在用本地 token 恢复登录态时的占位展示信息（避免刷新页面时先闪一下「游客」） */
const RESTORING_USER = {
  name: '加载中…',
  role: '正在获取登录信息',
  avatarText: '…',
  avatarUrl: '',
}

/**
 * 用户区展示信息
 * 1）恢复登录态中：展示加载占位，避免「游客」一闪而过
 * 2）未登录展示游客；已登录展示接口返回的头像与昵称（昵称为空时退回账号）
 */
const displayUser = computed(() => {
  if (userStore.restoring) {
    return RESTORING_USER
  }
  if (!userStore.isLogin) {
    return GUEST_USER
  }
  const info = userStore.userInfo
  const name = info.nickName || info.userName || '用户'
  return {
    name,
    // 副标题展示账号，便于区分同名昵称
    role: info.userName || '普通用户',
    avatarText: name.slice(0, 1),
    // 第三方登录返回完整地址，本地上传返回文件名，统一在这里转换
    avatarUrl: resolveFileUrl(info.avatar),
  }
})

// 用户下拉框是否展开，用于给用户框添加高亮态
const userMenuVisible = ref(false)

const handleUserMenuVisibleChange = (visible: boolean) => {
  userMenuVisible.value = visible
}

/**
 * 清除本地登录凭证与登录态
 * 注意：user 端没有 /login 路由，跳转登录页只会落到 404，
 * 因此这里只清理本地状态，页面停留原地并以游客态展示
 */
const clearLoginState = () => {
  userStore.clearLoginState()
}

/**
 * 用本地 token 恢复登录态（页面打开 / 刷新时调用）
 * 具体流程收敛在 stores/user 的 restoreLogin 里：
 * - 无 token：直接按游客处理，不发请求
 * - 有 token：只请求一次 /auth/getUserInfo，成功后写入 store 并置为登录态
 *   （布局重复挂载 / 重复调用时共享同一个请求，不会重复打接口）
 * - token 失效或接口异常：store 内已清理本地凭证，这里只负责提示用户
 */
const fetchLoginUser = async () => {
  try {
    await userStore.restoreLogin()
  } catch (error) {
    ElMessage.warning(resolveAuthErrorMessage(error))
  }
}

// 打开登录弹窗：未登录时是「登录」，已登录时是「切换账户」，两者共用同一个弹窗
const openLoginDialog = () => {
  loginDialogVisible.value = true
}

// 切换账户：弹出 GitHub 授权登录弹窗
const handleSwitchAccount = () => {
  openLoginDialog()
}

// 退出登录：先注销服务端登录信息，再清除本地凭证，最后停留当前页，用户区回落为游客态
const handleLogout = async () => {
  try {
    // 必须先调接口再清 token：
    // logout 不在网关白名单内，token 由 utils/request.ts 的请求拦截器从 localStorage 读取后
    // 注入请求头 Authorization，若先执行 clearLoginState()，请求会因缺少 token 被网关拦成 401，
    // 服务端 redis 中的登录信息也就删除不掉
    await logout()
  } catch (error) {
    // 服务端注销失败（token 已过期、网络异常等）不阻断本地登出，避免卡住用户
    // 此处静默吞掉错误：本地凭证照常清除，服务端残留的失效 token 由网关校验拦截，不会造成越权
  } finally {
    // 无论服务端结果如何都要清本地凭证，避免本地残留一个已经失效的 token
    clearLoginState()
  }
  ElMessage.success('已退出登录')
}

const handleUserCommand = (command: string) => {
  if (command === 'login') {
    openLoginDialog()
  } else if (command === 'switch') {
    handleSwitchAccount()
  } else if (command === 'logout') {
    handleLogout()
  }
}

// ==================== 侧边栏「视频」分类 ====================

/** 视频分类列表（点击侧边栏「视频」展开时按需加载；未搜索时展示的就是它） */
const videoCategories = ref<FileCategoryVO[]>([])

/**
 * 分类列表加载状态
 * idle：尚未加载 / loading：加载中 / success：已加载成功 / error：加载失败（下次展开会自动重试）
 */
const categoryStatus = ref<'idle' | 'loading' | 'success' | 'error'>('idle')

// ==================== 侧边栏「视频」分类：搜索 / 修改 / 删除 ====================

/** 分类搜索关键字（懒加载：展开搜索框并输入后才发起搜索请求） */
const categoryKeyword = ref('')

/** 搜索框是否展开（点击搜索入口滑动展开 / 再点收起） */
const searchVisible = ref(false)

/** 搜索请求是否进行中 */
const searching = ref(false)

/** 是否处于「批量删除」多选模式（点工具条上的删除按钮进入） */
const batchMode = ref(false)

/** 多选模式下已勾选的分类ID */
const selectedCategoryIds = ref<number[]>([])

/** 删除请求是否进行中 */
const deleting = ref(false)

/** 分类表单弹窗模式：create=新增，edit=修改 */
const formMode = ref<'create' | 'edit'>('create')

/** 分类表单弹窗是否显示（新增 / 修改共用） */
const formDialogVisible = ref(false)

/** 分类表单实例（提交前做一次校验） */
const formRef = ref<FormInstance>()

/** 分类表单提交中 */
const formSubmitting = ref(false)

/** 排序调整是否进行中（避免连点导致顺序错乱） */
const moving = ref(false)

/** 分类表单数据（新增时为空，修改时回填当前分类） */
const formData = reactive<{ id: number; categoryName: string; sort: number | undefined }>({
  id: 0,
  categoryName: '',
  sort: undefined,
})

/** 分类表单校验规则：名称必填且不超过 100 字符（与数据库 category_name VARCHAR(100) 对齐） */
const formRules: FormRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称不能超过 100 个字符', trigger: 'blur' },
  ],
}

/** 搜索防抖定时器（输入停止 300ms 后才真正请求，避免每敲一个字都打接口） */
let searchTimer: ReturnType<typeof setTimeout> | null = null

/** 搜索防抖延迟（ms） */
const SEARCH_DEBOUNCE_MS = 300

/**
 * 搜索结果
 * null 表示「当前没有搜索条件」，此时渲染未过滤的全量列表 videoCategories；
 * 非 null 时渲染 searchPage.records（后端分页返回）
 */
const searchPage = ref<PageResult<FileCategoryVO> | null>(null)

/** 当前渲染的分类列表：有关键字时用搜索结果，否则用全量列表 */
const displayCategories = computed<FileCategoryVO[]>(() =>
  searchPage.value ? searchPage.value.records ?? [] : videoCategories.value
)

/**
 * 搜索结果是否被每页条数截断
 * 侧边栏不做分页状态机（不加载第二页），total 大于本页条数时给出提示，避免用户误以为分类丢了
 */
const searchTruncated = computed(() =>
  searchPage.value ? searchPage.value.total > (searchPage.value.records?.length ?? 0) : false
)

/** 是否处于搜索态（用于空态文案区分「暂无分类」与「未找到匹配的分类」） */
const isSearching = computed(() => Boolean(categoryKeyword.value.trim()))

/**
 * 面包屑里展示的视频分类名（仅在 /video 且 URL 带分类ID时追加一级）
 * 分类列表尚未加载（未登录 / 未展开子菜单）或分类已被删除时查不到名字，
 * 统一回退为「分类 #id」，保证 URL 带分类ID时面包屑层级完整
 */
const activeCategoryName = computed(() => {
  if (route.path !== routesConstants.VIDEO || activeCategoryId.value === null) {
    return ''
  }
  const matched = displayCategories.value.find((item) => item.id === activeCategoryId.value)
  return matched?.categoryName ?? `分类 #${activeCategoryId.value}`
})

/**
 * 清空搜索条件（关键字 + 搜索结果）
 * 收起搜索框 / 点击输入框清空按钮 / 切换登录账户时调用，清空后回到全量列表
 */
const clearSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  categoryKeyword.value = ''
  searchPage.value = null
}

/**
 * 按 sort 升序整理分类
 * 后端 listCategoriesByUserId 未指定排序，返回顺序不保证，这里做一次前端兜底排序；
 * sort 为空时统一排到末尾，避免空值参与比较导致顺序错乱
 */
const sortCategories = (list: FileCategoryVO[]) =>
  [...list].sort((a, b) => {
    // 与后端 orderByAsc(sort, id) 对齐：排序值相同时按 id 升序，避免顺序看起来「没变」
    const sortA = a.sort ?? Number.MAX_SAFE_INTEGER
    const sortB = b.sort ?? Number.MAX_SAFE_INTEGER
    return sortA - sortB || a.id - b.id
  })

/**
 * 清空分类缓存
 * 登录用户变化（登录成功 / 切换账户 / 退出登录）时必须清空，
 * 否则会把上一个账号的分类展示给下一个用户；搜索条件一并重置，避免残留上一个账号的搜索结果
 */
const resetVideoCategories = () => {
  videoCategories.value = []
  categoryStatus.value = 'idle'
  clearSearch()
  searchVisible.value = false
  // URL 上残留的是上一个账号的分类ID，必须清掉，
  // 否则内容区会拿别人的分类ID去查资源（后端按 userId + categoryId 查询，查不到数据但 URL 会误导用户）
  if (activeCategoryId.value !== null) {
    router.replace(routesConstants.VIDEO)
  }
}

// 未登录时 userId 为 0，登录后变为真实用户ID，切换账户 / 退出登录时都会再次变化
watch(() => userStore.userInfo.userId, resetVideoCategories)

/**
 * 加载当前登录用户的视频分类
 * 1）已加载成功或正在加载：直接复用，重复展开不会重复请求
 * 2）登录态恢复中（刷新后用本地 token 校验未返回）：先等恢复完成，避免把已登录用户误判为游客
 * 3）未登录：接口依赖登录上下文，只提示不发请求（避免控制台一片 401）
 * 4）失败：记录 error 状态，再次展开「视频」会自动重试
 */
const loadVideoCategories = async () => {
  if (categoryStatus.value === 'success' || categoryStatus.value === 'loading') {
    return
  }
  if (userStore.restoring) {
    // 复用同一份恢复请求；失败时 store 已清空本地凭证，错误提示由 fetchLoginUser 统一给出
    await userStore.restoreLogin().catch(() => undefined)
    if (!isLogin.value) {
      return
    }
  }
  if (!isLogin.value) {
    ElMessage.warning('请先登录后查看视频分类')
    return
  }
  categoryStatus.value = 'loading'
  try {
    const list = await listUserFileCategories()
    videoCategories.value = sortCategories(list ?? [])
    categoryStatus.value = 'success'
  } catch (error) {
    categoryStatus.value = 'error'
    ElMessage.warning(resolveErrorMessage(error, '视频分类加载失败，请稍后重试'))
  }
}

/**
 * 侧边栏「视频」子菜单展开
 * 点标题或点箭头展开都会触发（折叠态下 element-plus 会改为弹出层展示），在这里懒加载分类列表；
 * 只负责展开与加载分类，不做任何路由跳转，右侧内容区保持不变
 */
const handleMenuOpen = (index: string) => {
  if (index !== routesConstants.VIDEO) {
    return
  }
  loadVideoCategories()
}

/**
 * 按关键字搜索分类（懒加载：仅在展开搜索框并输入后才调用）
 * 1）关键字为空：退出搜索态，回到全量列表（全量若尚未加载成功则补一次加载）
 * 2）未登录：搜索接口依赖用户ID，只提示不发请求
 * 3）失败：提示错误，保持原有列表不变
 */
const searchVideoCategories = async () => {
  const keyword = categoryKeyword.value.trim()

  if (!keyword) {
    clearSearch()
    if (categoryStatus.value !== 'success') {
      await loadVideoCategories()
    }
    return
  }

  if (userStore.restoring) {
    // 刷新后本地 token 校验未返回，先等恢复完成，避免把已登录用户误判为游客
    await userStore.restoreLogin().catch(() => undefined)
  }
  if (!isLogin.value) {
    ElMessage.warning('请先登录后搜索分类')
    return
  }

  searching.value = true
  try {
    // userId 必须传当前登录用户ID：后端按入参 userId 过滤，不传会查到其他用户的分类
    searchPage.value = await searchUserFileCategories(1, CATEGORY_PAGE_SIZE, {
      userId: userStore.userInfo.userId,
      categoryName: keyword,
    })
  } catch (error) {
    ElMessage.warning(resolveErrorMessage(error, '分类搜索失败，请稍后重试'))
  } finally {
    searching.value = false
  }
}

/** 输入框输入：停止输入 300ms 后才真正搜索，避免每敲一个字都打接口 */
const handleSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    searchTimer = null
    searchVideoCategories()
  }, SEARCH_DEBOUNCE_MS)
}

/** 输入框回车：取消防抖，立即搜索 */
const handleSearchEnter = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  searchVideoCategories()
}

/** 输入框清空按钮：回到全量列表（搜索框保持展开状态） */
const handleSearchClear = () => {
  clearSearch()
}

/**
 * 点击搜索入口：滑动展开 / 收起搜索框
 * 收起时一并清空关键字与搜索结果，回到全量列表
 */
const toggleSearch = () => {
  searchVisible.value = !searchVisible.value
  if (searchVisible.value) {
    // 搜索与多选互斥：工具条空间有限，展开搜索框时先退出批量删除模式
    if (batchMode.value) {
      exitBatchMode()
    }
    return
  }
  clearSearch()
  if (categoryStatus.value !== 'success') {
    loadVideoCategories()
  }
}

/**
 * 按当前条件重新拉取分类（修改 / 删除 / 全量删除成功后调用，保证与服务端一致）
 * 有关键字：重查搜索结果；无关键字：强制重拉全量列表
 * （loadVideoCategories 命中 success 会直接复用缓存，这里先把状态复位再加载）
 */
const refreshCategories = async () => {
  if (isSearching.value) {
    await searchVideoCategories()
    return
  }
  categoryStatus.value = 'idle'
  await loadVideoCategories()
}

/** 删除成功后本地同步移除（先给即时反馈，再由 refreshCategories 静默校准） */
const removeCategoriesLocally = (ids: number[]) => {
  const idSet = new Set(ids)
  videoCategories.value = videoCategories.value.filter((item) => !idSet.has(item.id))
  if (searchPage.value) {
    searchPage.value = {
      ...searchPage.value,
      records: (searchPage.value.records ?? []).filter((item) => !idSet.has(item.id)),
      total: Math.max(0, searchPage.value.total - ids.length),
    }
  }
}

/** 修改成功后本地同步分类名（先给即时反馈，再由 refreshCategories 静默校准） */
const updateCategoryLocally = (id: number, categoryName: string) => {
  const rename = (item: FileCategoryVO) => (item.id === id ? { ...item, categoryName } : item)
  videoCategories.value = videoCategories.value.map(rename)
  if (searchPage.value) {
    searchPage.value = {
      ...searchPage.value,
      records: (searchPage.value.records ?? []).map(rename),
    }
  }
}

/** 分类是否可上移 / 下移（首尾项、搜索态下、调整进行中不可用） */
const canMoveCategory = (category: FileCategoryVO, direction: 'up' | 'down') => {
  if (isSearching.value || moving.value) {
    return false
  }
  const index = displayCategories.value.findIndex((item) => item.id === category.id)
  if (index < 0) {
    return false
  }
  return direction === 'up' ? index > 0 : index < displayCategories.value.length - 1
}

/**
 * 调整分类顺序（上移 / 下移）
 * 排序值不在页面上手填：这里按「交换后的展示顺序」自动算出每个分类的位次（0,1,2…），
 * 只把位次发生变化的分类提交给后端；排序值本来就是连续序号时，一次移动只需 2 个请求
 */
const moveCategory = async (category: FileCategoryVO, direction: 'up' | 'down') => {
  if (moving.value) {
    return
  }
  if (isSearching.value) {
    ElMessage.warning('搜索状态下不支持调整顺序，请先清空搜索关键字')
    return
  }

  const ordered = [...displayCategories.value]
  const index = ordered.findIndex((item) => item.id === category.id)
  if (index < 0) {
    return
  }

  const targetIndex = direction === 'up' ? index - 1 : index + 1
  if (targetIndex < 0) {
    ElMessage.info('已经是最前面的分类了')
    return
  }
  if (targetIndex >= ordered.length) {
    ElMessage.info('已经是最后面的分类了')
    return
  }

  // 交换位置，再按新顺序重新编号
  const reordered = [...ordered]
  const moved = reordered.splice(index, 1)[0]
  if (!moved) {
    return
  }
  reordered.splice(targetIndex, 0, moved)

  // 只提交位次发生变化的分类，避免无谓请求
  const changes = reordered
    .map((item, order) => ({ item, order }))
    .filter(({ item, order }) => item.sort !== order)
  if (changes.length === 0) {
    return
  }

  moving.value = true
  try {
    await Promise.all(
      changes.map(({ item, order }) =>
        updateFileCategory({
          id: item.id,
          // 后端按 userId 做同名校验，必须传当前登录用户ID
          userId: userStore.userInfo.userId,
          categoryName: item.categoryName,
          sort: order,
        })
      )
    )

    // 本地同步新位次（立即反馈），随后 refreshCategories 再与服务端对齐
    const orderMap = new Map(changes.map(({ item, order }) => [item.id, order]))
    videoCategories.value = sortCategories(
      videoCategories.value.map((item) => {
        const order = orderMap.get(item.id)
        return order === undefined ? item : { ...item, sort: order }
      })
    )

    ElMessage.success(direction === 'up' ? '已上移' : '已下移')
    await refreshCategories()
  } catch (error) {
    ElMessage.warning(resolveErrorMessage(error, '顺序调整失败，请稍后重试'))
  } finally {
    moving.value = false
  }
}

/** 打开「新增分类」弹窗（排序由系统计算，不需要用户填写） */
const openCreateDialog = () => {
  formMode.value = 'create'
  formData.id = 0
  formData.categoryName = ''
  // 新分类默认排到最后：取当前全量列表里最大的排序值 + 1（列表为空时为 0）
  const maxSort = videoCategories.value.reduce((max, item) => Math.max(max, item.sort ?? -1), -1)
  formData.sort = maxSort + 1
  formDialogVisible.value = true
  // 等弹窗内容渲染完再清掉上一次残留的校验提示
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开「修改分类」弹窗并回填当前分类名称（排序由上下移按钮调整，弹窗里不再提供） */
const openEditDialog = (category: FileCategoryVO) => {
  formMode.value = 'edit'
  formData.id = category.id
  formData.categoryName = category.categoryName
  formDialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 提交分类表单（新增 / 修改共用） */
const submitCategoryForm = async () => {
  if (!formRef.value || formSubmitting.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  const isCreate = formMode.value === 'create'
  formSubmitting.value = true
  const categoryName = formData.categoryName.trim()
  try {
    const payload: FileCategoryDTO = {
      // 后端按 userId 做同名校验，必须传当前登录用户ID
      userId: userStore.userInfo.userId,
      categoryName,
    }

    if (isCreate) {
      // 排序由系统计算（排到最后），不交给用户手填
      payload.sort = formData.sort ?? 0
      await addFileCategory(payload)
    } else {
      // 修改只提交名称：不带 sort 时后端 updateById 会跳过该字段，顺序保持不变（顺序由上下移按钮调整）
      await updateFileCategory({ ...payload, id: formData.id })
    }

    formDialogVisible.value = false
    if (isCreate) {
      // 新增成功后清掉搜索条件，让新建的分类直接出现在列表里
      clearSearch()
      searchVisible.value = false
      ElMessage.success('新增成功')
    } else {
      updateCategoryLocally(formData.id, categoryName)
      ElMessage.success('修改成功')
    }
    await refreshCategories()
  } catch (error) {
    // 后端错误信息（如「该分类名称已存在」）由 resolveErrorMessage 统一取出
    ElMessage.warning(
      resolveErrorMessage(error, isCreate ? '分类新增失败，请稍后重试' : '分类修改失败，请稍后重试')
    )
  } finally {
    formSubmitting.value = false
  }
}

/** 删除单个分类 */
const handleDeleteCategory = async (category: FileCategoryVO) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${category.categoryName}」吗？删除后不可恢复`,
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

  try {
    await deleteFileCategory(category.id)
    removeCategoriesLocally([category.id])
    ElMessage.success('删除成功')
    await refreshCategories()
  } catch (error) {
    ElMessage.warning(resolveErrorMessage(error, '分类删除失败，请稍后重试'))
  }
}

/** 是否已全选（多选模式下驱动「全选」复选框） */
const isAllSelected = computed(
  () =>
    displayCategories.value.length > 0 &&
    selectedCategoryIds.value.length === displayCategories.value.length
)

/** 是否半选（部分勾选时复选框显示为横线态） */
const isIndeterminate = computed(
  () => selectedCategoryIds.value.length > 0 && !isAllSelected.value
)

/** 进入批量删除模式（点工具条上的删除按钮） */
const enterBatchMode = () => {
  batchMode.value = true
  selectedCategoryIds.value = []
  // 与搜索互斥：避免工具条同时出现搜索框与多选操作条
  if (searchVisible.value) {
    searchVisible.value = false
    clearSearch()
  }
}

/** 退出批量删除模式 */
const exitBatchMode = () => {
  batchMode.value = false
  selectedCategoryIds.value = []
}

/** 勾选 / 取消勾选某个分类 */
const toggleSelectCategory = (id: number, checked: boolean) => {
  if (checked) {
    if (!selectedCategoryIds.value.includes(id)) {
      selectedCategoryIds.value = [...selectedCategoryIds.value, id]
    }
    return
  }
  selectedCategoryIds.value = selectedCategoryIds.value.filter((item) => item !== id)
}

/** 全选 / 取消全选（只针对当前列表里展示的分类，搜索态下即「搜索结果全选」） */
const toggleSelectAll = (checked: boolean) => {
  selectedCategoryIds.value = checked ? displayCategories.value.map((item) => item.id) : []
}

/** 删除选中的分类（批量删除，复用后端 /file/category/batchDelete） */
const handleBatchDelete = async () => {
  const ids = selectedCategoryIds.value
  if (ids.length === 0 || deleting.value) {
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 个分类吗？删除后不可恢复`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    // 用户取消 / 关闭弹窗：不做任何处理
    return
  }

  deleting.value = true
  try {
    await batchDeleteFileCategories(ids)
    // 本地同步移除，再重新拉取校准
    removeCategoriesLocally(ids)
    ElMessage.success(`已删除 ${ids.length} 个分类`)
    exitBatchMode()
    await refreshCategories()
  } catch (error) {
    ElMessage.warning(resolveErrorMessage(error, '批量删除失败，请稍后重试'))
  } finally {
    deleting.value = false
  }
}

/**
 * 侧边栏菜单选中项处理
 * el-menu 未开启 router 模式，避免「分类项」被当成路由地址跳走，跳转统一在这里做：
 * 1）首页：跳首页
 * 2）「全部视频」：回到 /video（清掉 URL 上的分类ID），内容区展示该用户全部资源
 * 3）分类项（video-category-{id}）：跳到 /video?categoryId={id}，内容区据此按分类加载资源
 */
const handleMenuSelect = (index: string) => {
  if (index === routesConstants.HOME) {
    router.push(index)
    return
  }
  if (index === VIDEO_CATEGORY_ALL_INDEX || index === routesConstants.VIDEO) {
    // 已在「全部视频」时不再重复跳转，避免产生多余的导航记录
    if (route.path !== routesConstants.VIDEO || activeCategoryId.value !== null) {
      router.push(routesConstants.VIDEO)
    }
    return
  }
  if (!index.startsWith(VIDEO_CATEGORY_MENU_PREFIX)) {
    return
  }
  const categoryId = Number(index.slice(VIDEO_CATEGORY_MENU_PREFIX.length))
  if (!Number.isFinite(categoryId) || categoryId <= 0) {
    return
  }
  // 同一分类重复点击不重复跳转
  if (route.path === routesConstants.VIDEO && activeCategoryId.value === categoryId) {
    return
  }
  router.push({ path: routesConstants.VIDEO, query: { categoryId: String(categoryId) } })
}

/**
 * 直接访问 /video?categoryId=x（刷新 / 分享链接）时补齐侧边栏分类数据：
 * 分类列表平时是靠「展开视频子菜单」懒加载的，这条路径下子菜单由 defaultOpeneds 直接展开，
 * 不一定会触发 open 事件，因此这里补一次加载（loadVideoCategories 自身幂等，不会重复请求）
 */
const ensureCategoriesForActiveCategory = () => {
  if (activeCategoryId.value !== null && isLogin.value) {
    loadVideoCategories()
  }
}

// 登录态在刷新后是异步恢复的：恢复成功后再补分类数据，避免把已登录用户误判为游客
watch(isLogin, (logged) => {
  if (logged) {
    ensureCategoriesForActiveCategory()
  }
})

onMounted(async () => {
  await fetchLoginUser()
  ensureCategoriesForActiveCategory()
})
</script>

<template>
  <div class="layout">
    <aside class="sidebar" :style="{ width: sidebarWidth }">
      <div class="logo">
        <div class="logo-icon">VI</div>
        <span v-show="!isCollapse" class="logo-text">智能化视频分析平台</span>
      </div>

      <el-menu
        class="sidebar-menu"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        :collapse="isCollapse"
        :collapse-transition="false"
        @select="handleMenuSelect"
        @open="handleMenuOpen"
      >
        <el-menu-item :index="routesConstants.HOME">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 3 3 10.5V21h6v-6h6v6h6V10.5L12 3Z" />
            </svg>
          </el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <!-- 视频：展开时懒加载当前登录用户的分类列表；折叠态（64px）由 element-plus 自动改为弹出层展示 -->
        <el-sub-menu :index="routesConstants.VIDEO" popper-class="sidebar-submenu-popper">
          <template #title>
            <!-- 折叠态（64px）下子菜单改为弹出层展示，这里再挂一次加载入口，保证点图标也能拿到分类；
                 加载函数自身是幂等的（loading / success 直接返回），不会产生重复请求 -->
            <el-icon @click="loadVideoCategories">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path d="M3 6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2.2l4.55-2.28A1 1 0 0 1 21 6.82v10.36a1 1 0 0 1-1.45.9L15 15.8V18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6Z"/>
              </svg>
            </el-icon>
            <span>视频</span>
          </template>

          <!--
            工具条：搜索 + 批量删除
            这里刻意使用普通 div（而不是 el-menu-item）：el-menu-item 的点击会被 el-menu 的选中逻辑接管，
            折叠态下选中还会关闭弹出层，放进 div 里按钮与输入框的事件才能稳定生效
          -->
          <div v-if="isLogin" class="category-toolbar">
            <div class="category-tool-row">
              <button type="button" class="category-tool-btn" @click="toggleSearch">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                  <path d="M10.5 3a7.5 7.5 0 1 0 4.55 13.46l4.24 4.25 1.41-1.42-4.24-4.24A7.5 7.5 0 0 0 10.5 3Zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11Z"/>
                </svg>
                <span class="category-tool-text">搜索分类</span>
                <span class="category-search-arrow" :class="{ 'is-open': searchVisible }">
                  <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                    <path d="M12 15.5 4.5 8h15L12 15.5Z"/>
                  </svg>
                </span>
              </button>
              <!-- 新增分类：打开弹窗填写名称 / 排序 -->
              <button
                type="button"
                class="category-tool-btn is-icon"
                title="新增分类"
                @click="openCreateDialog"
              >
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                  <path d="M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6V5Z"/>
                </svg>
              </button>
              <!-- 批量删除入口：点一下进入多选模式，列表里出现复选框，再按勾选结果删除 -->
              <button
                v-if="!batchMode && displayCategories.length > 0"
                type="button"
                class="category-tool-btn is-icon is-danger"
                title="批量删除分类"
                @click="enterBatchMode"
              >
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                  <path d="M9 3h6l1 2h4v2H4V5h4l1-2Zm-3 6h12l-1 11a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 9Zm3.5 2 .5 9h1l-.5-9h-1Zm4 0-.5 9h1l.5-9h-1Z"/>
                </svg>
              </button>
            </div>

            <!-- 搜索框：点「搜索分类」后向下滑动展开；收起时清空关键字并回到全量列表 -->
            <div class="category-search-wrap" :class="{ 'is-open': searchVisible }">
              <el-input
                v-model="categoryKeyword"
                size="small"
                placeholder="输入分类名称搜索"
                clearable
                @input="handleSearchInput"
                @keydown.stop
                @keyup.enter="handleSearchEnter"
                @clear="handleSearchClear"
              >
                <template #prefix>
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path d="M10.5 3a7.5 7.5 0 1 0 4.55 13.46l4.24 4.25 1.41-1.42-4.24-4.24A7.5 7.5 0 0 0 10.5 3Zm0 2a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11Z"/>
                  </svg>
                </template>
              </el-input>
            </div>

            <!-- 批量删除操作条：全选 / 删除选中 / 取消 -->
            <div v-if="batchMode" class="category-batch-bar">
              <el-checkbox
                :model-value="isAllSelected"
                :indeterminate="isIndeterminate"
                size="small"
                @change="(checked: CheckboxValueType) => toggleSelectAll(checked === true)"
              >
                全选
              </el-checkbox>
              <div class="category-batch-actions">
                <button
                  type="button"
                  class="category-tool-btn is-danger"
                  :disabled="selectedCategoryIds.length === 0 || deleting"
                  @click="handleBatchDelete"
                >
                  删除{{ selectedCategoryIds.length ? `(${selectedCategoryIds.length})` : '' }}
                </button>
                <button type="button" class="category-tool-btn" :disabled="deleting" @click="exitBatchMode">
                  取消
                </button>
              </div>
            </div>
          </div>

          <!--
            分类区域的各类状态提示统一用 disabled 的菜单项占位：
            disabled 不会触发 el-menu 的 select，避免被误当成可跳转的分类项
          -->
          <el-menu-item v-if="searching" class="menu-tip" index="video-category-searching" disabled>
            搜索中...
          </el-menu-item>
          <el-menu-item v-else-if="categoryStatus === 'loading'" class="menu-tip" index="video-category-loading" disabled>
            加载中...
          </el-menu-item>
          <el-menu-item v-else-if="!isLogin" class="menu-tip" index="video-category-guest" disabled>
            登录后可查看分类
          </el-menu-item>
          <el-menu-item v-else-if="categoryStatus === 'error'" class="menu-tip" index="video-category-error" disabled>
            加载失败，重新展开可重试
          </el-menu-item>
          <el-menu-item v-else-if="displayCategories.length === 0" class="menu-tip" index="video-category-empty" disabled>
            {{ isSearching ? '未找到匹配的分类' : '暂无分类' }}
          </el-menu-item>

          <!--
            「全部视频」入口：不选任何分类时内容区展示该用户全部资源，
            也是从某个分类返回「全部」的显式入口（面包屑里的「视频」同样可点）
            批量删除 / 搜索态下隐藏，避免与分类项操作混淆
          -->
          <el-menu-item
            v-if="isLogin && !batchMode && !isSearching"
            :index="VIDEO_CATEGORY_ALL_INDEX"
            class="category-item is-all-item"
          >
            <el-icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path d="M4 4h6v6H4V4Zm10 0h6v6h-6V4ZM4 14h6v6H4v-6Zm10 0h6v6h-6v-6Z"/>
              </svg>
            </el-icon>
            <template #title>全部视频</template>
          </el-menu-item>

          <!--
            分类列表：hover 右侧出现「修改 / 删除」；多选模式下图标位置换成复选框；
            点击分类项跳转 /video?categoryId={id}，由右侧内容区按分类加载资源
          -->
          <el-menu-item
            v-for="category in displayCategories"
            :key="category.id"
            :index="`video-category-${category.id}`"
            class="category-item"
          >
            <!-- 多选模式：复选框（@click.stop 阻止冒泡到菜单项触发选中） -->
            <el-checkbox
              v-if="batchMode"
              class="category-checkbox"
              :model-value="selectedCategoryIds.includes(category.id)"
              @click.stop
              @change="(checked: CheckboxValueType) => toggleSelectCategory(category.id, checked === true)"
            />
            <el-icon v-else>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path d="M3 5.5A1.5 1.5 0 0 1 4.5 4h4.1c.4 0 .78.16 1.06.44l1.28 1.3c.28.28.66.44 1.06.44h6.5A1.5 1.5 0 0 1 21 7.68v9.82a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5v-12Z"/>
              </svg>
            </el-icon>
            <template #title>
              <span class="category-title">
                <span class="category-name" :title="category.categoryName">{{ category.categoryName }}</span>
                <span v-if="!batchMode" class="category-actions">
                  <!-- 上移 / 下移：自动计算位次并调用修改接口，排序值不需要手填 -->
                  <span
                    class="category-action"
                    :class="{ 'is-disabled': !canMoveCategory(category, 'up') }"
                    :title="canMoveCategory(category, 'up') ? '上移' : '上移（已是最前或在搜索状态下不可用）'"
                    @click.stop="moveCategory(category, 'up')"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path d="M12 6.5 5 14h14L12 6.5Z"/>
                    </svg>
                  </span>
                  <span
                    class="category-action"
                    :class="{ 'is-disabled': !canMoveCategory(category, 'down') }"
                    :title="canMoveCategory(category, 'down') ? '下移' : '下移（已是最后或在搜索状态下不可用）'"
                    @click.stop="moveCategory(category, 'down')"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path d="M12 17.5 5 10h14l-7 7.5Z"/>
                    </svg>
                  </span>
                  <span
                    class="category-action"
                    title="修改分类名称"
                    @click.stop="openEditDialog(category)"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25ZM20.7 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z"/>
                    </svg>
                  </span>
                  <span
                    class="category-action is-danger"
                    title="删除该分类"
                    @click.stop="handleDeleteCategory(category)"
                  >
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path d="M9 3h6l1 2h4v2H4V5h4l1-2Zm-3 6h12l-1 11a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 9Zm3.5 2 .5 9h1l-.5-9h-1Zm4 0-.5 9h1l.5-9h-1Z"/>
                    </svg>
                  </span>
                </span>
              </span>
            </template>
          </el-menu-item>

          <!-- 搜索结果被每页条数截断时的提示：侧边栏不做分页加载，引导用关键字缩小范围 -->
          <el-menu-item v-if="searchTruncated" class="menu-tip" index="video-category-truncated" disabled>
            仅显示前 {{ CATEGORY_PAGE_SIZE }} 条，请用关键词缩小范围
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="sidebar-footer">
        <!-- 恢复登录态期间禁用下拉：此时登录态尚未确定，避免用户误点到「登录 / 切换账户」 -->
        <el-dropdown
          class="user-dropdown"
          trigger="click"
          placement="top-start"
          popper-class="user-dropdown-popper"
          :disabled="userStore.restoring"
          @command="handleUserCommand"
          @visible-change="handleUserMenuVisibleChange"
        >
          <div class="user-box" :class="{ 'is-active': userMenuVisible }">
            <el-avatar class="avatar" :size="32" :src="displayUser.avatarUrl">
              {{ displayUser.avatarText }}
            </el-avatar>
            <div v-show="!isCollapse" class="user-info">
              <div class="username" :title="displayUser.name">{{ displayUser.name }}</div>
              <div class="user-role" :title="displayUser.role">{{ displayUser.role }}</div>
            </div>
            <span v-show="!isCollapse" class="user-arrow">
              <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                <path d="M12 15.5 4.5 8h15L12 15.5Z" />
              </svg>
            </span>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <!-- 未登录：只提供登录入口（与「切换账户」共用同一个登录弹窗） -->
              <el-dropdown-item v-if="!isLogin" command="login">
                <span class="user-menu-item">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path
                      d="M12 12a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm0 1.8c-3.6 0-6.6 1.9-6.6 4.3V21h13.2v-2.9c0-2.4-3-4.3-6.6-4.3Z"
                    />
                  </svg>
                  <span>登录</span>
                </span>
              </el-dropdown-item>
              <!-- 已登录：可切换账户 / 退出登录 -->
              <el-dropdown-item v-else command="switch">
                <span class="user-menu-item">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path
                      d="M12 12a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm0 1.8c-3.6 0-6.6 1.9-6.6 4.3V21h13.2v-2.9c0-2.4-3-4.3-6.6-4.3Z"
                    />
                  </svg>
                  <span>切换账户</span>
                </span>
              </el-dropdown-item>
              <el-dropdown-item v-if="isLogin" command="logout" divided>
                <span class="user-menu-item">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path
                      d="M10 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h5v-2H5V5h5V3Zm5.6 3.6-1.4 1.4L17.2 11H9v2h8.2l-3 3 1.4 1.4L21 12l-5.4-5.4Z"
                    />
                  </svg>
                  <span>退出登录</span>
                </span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <div class="main">
      <header class="main-header">
        <el-button class="collapse-btn" text @click="toggleSidebar">
          <el-icon>
            <svg v-if="isCollapse" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M9 5l7 7-7 7V5Z" />
            </svg>
            <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M15 5l-7 7 7 7V5Z" />
            </svg>
          </el-icon>
        </el-button>
        <span class="page-title">智能化视频分析平台</span>
      </header>

      <!-- 面包屑：首页固定展示，其余层级由当前路由的 meta.title 动态生成（黑白灰配色） -->
      <div class="breadcrumb-bar">
        <el-breadcrumb class="breadcrumb" separator="/">
          <el-breadcrumb-item :to="{ path: routesConstants.HOME }">
            <el-icon class="breadcrumb-home-icon">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <path d="M12 3 3 10.5V21h6v-6h6v6h6V10.5L12 3Z" />
              </svg>
            </el-icon>
            <span>首页</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbs"
            :key="item.path"
            :to="index < breadcrumbs.length - 1 ? { path: item.path } : undefined"
          >
            {{ item.title }}
          </el-breadcrumb-item>
          <!-- 视频分类层级：仅在 /video 且 URL 带分类ID时追加，展示当前正在浏览的分类名（不可点击） -->
          <el-breadcrumb-item v-if="activeCategoryName">{{ activeCategoryName }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <main class="main-content">
        <router-view />
      </main>
    </div>

    <!-- 登录 / 切换账户弹窗：使用 GitHub 账号授权登录 -->
    <LoginDialog
      v-model="loginDialogVisible"
      :title="isLogin ? '切换账户' : '登录'"
      subtitle="使用 GitHub 账号登录"
    />

    <!-- 分类表单弹窗：新增 / 修改共用（排序留空时新增按 0 排最前、修改保持原值） -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formMode === 'create' ? '新增分类' : '修改分类'"
      width="360px"
      append-to-body
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="72px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" maxlength="100" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <div class="category-form-tip">
        {{
          formMode === 'create'
            ? '新分类会排到最后，可用分类项上的 ↑ ↓ 按钮调整顺序'
            : '顺序请用分类项上的 ↑ ↓ 按钮调整'
        }}
      </div>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="submitCategoryForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

.sidebar {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.2s ease;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #f0f2f5;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 6px;
  background: #b7bec9;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  /* 导航文字统一为黑色（含默认态、hover 态、选中态） */
  --el-menu-text-color: #000000;
  --el-menu-hover-text-color: #000000;
  --el-menu-active-color: #000000;
  /* 菜单项变矮 → 「首页」与「视频」之间距离缩短 */
  --el-menu-item-height: 40px;
}

/* 兜底覆盖折叠 / 展开两种状态下菜单项的高度（父级子菜单「视频」与子项共用同一套「胶囊」外观） */
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title),
.sidebar-menu.el-menu--collapse :deep(.el-menu-item),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  height: 40px;
  line-height: 40px;
  /* 左右留边 + 圆角：让 hover / 选中态的灰底自成一个「胶囊」 */
  margin: 0 8px 4px;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* 折叠态宽度只有 64px，去掉左右留边，避免图标被挤压 */
.sidebar-menu.el-menu--collapse :deep(.el-menu-item),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  margin: 0 0 4px;
}

/* hover：浅灰底（不引入彩色，保持黑白灰画风） */
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: #f2f3f5;
}

/* 分类名过长时省略号收尾 */
.sidebar-menu :deep(.el-menu-item > span),
.sidebar-menu :deep(.el-sub-menu__title > span) {
  /* flex: 1 让 element-plus 包裹 title 插槽的外层 span 撑满整行：
     分类名过长时省略号才会生效，hover 出现的操作按钮也会贴在右侧 */
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 分类区域的状态提示项：disabled 只用于禁止点击，这里覆盖 element-plus 默认叠加的 0.25 透明度，保证提示可读 */
.sidebar-menu :deep(.menu-tip) {
  font-size: 12px;
}

.sidebar-menu :deep(.menu-tip.is-disabled) {
  opacity: 1;
  color: #909399;
  cursor: not-allowed;
}

/* ==================== 侧边栏「视频」分类：搜索 / 修改 / 删除 ==================== */

/* 分类标题行：名称占满剩余宽度（过长省略号收尾），右侧放操作按钮 */
.category-title {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
}

.category-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 搜索入口右侧的展开箭头：搜索框展开时翻转 180° */
.category-search-arrow {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  color: #909399;
  transition: transform 0.25s ease;
}

.category-search-arrow.is-open {
  transform: rotate(180deg);
}

/* 分类项的操作按钮容器：默认隐藏，hover 分类项时出现（不占位，避免挤压分类名） */
.category-actions {
  display: none;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 4px;
}

.category-item:hover .category-actions {
  display: inline-flex;
}

/* 单个操作按钮（修改 / 删除 / 全量删除），保持黑白灰画风 */
.category-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  color: #909399;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.category-action:hover {
  background: #dcdfe4;
  color: #1f2d3d;
}

.category-action.is-danger:hover {
  background: #fde2e2;
  color: #c45656;
}

/* 不可用的操作按钮（首尾项的上移 / 下移、搜索态下的移动）：灰化但仍响应点击以给出提示 */
.category-action.is-disabled {
  color: #c0c4cc;
}

.category-action.is-disabled:hover {
  background: transparent;
  color: #c0c4cc;
}

/* ==================== 侧边栏「视频」工具条：搜索 / 批量删除 ==================== */

/* 工具条容器：普通 div，不受 el-menu 菜单项样式影响 */
.category-toolbar {
  padding: 0 8px 4px;
}

.category-tool-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 搜索按钮占满剩余宽度（箭头贴右侧），图标按钮靠 margin-left: auto 贴右 */
.category-tool-row > .category-tool-btn:not(.is-icon) {
  flex: 1;
  min-width: 0;
}

/* 工具条按钮：搜索 / 批量删除入口 / 删除选中 / 取消 */
.category-tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #606266;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.category-tool-btn:hover:not(:disabled) {
  background: #f2f3f5;
  color: #1f2d3d;
}

.category-tool-btn:disabled {
  color: #c0c4cc;
  background: transparent;
  cursor: not-allowed;
}

/* 右侧的图标按钮（批量删除入口） */
.category-tool-btn.is-icon {
  margin-left: auto;
  padding: 0 7px;
}

.category-tool-btn.is-danger:hover:not(:disabled) {
  background: #fde2e2;
  color: #c45656;
}

.category-tool-text {
  flex: 1;
  min-width: 0;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 搜索框：默认高度为 0，点「搜索分类」后向下滑出 */
.category-search-wrap {
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  transition: max-height 0.25s ease, opacity 0.25s ease, margin-top 0.25s ease;
}

.category-search-wrap.is-open {
  max-height: 44px;
  margin-top: 6px;
  opacity: 1;
}

/* 批量删除操作条：全选 + 删除选中 + 取消 */
.category-batch-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  padding: 0 2px;
}

.category-batch-bar :deep(.el-checkbox) {
  height: 24px;
  font-size: 12px;
}

.category-batch-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

/* 操作条里的按钮加一层描边，与普通文字按钮区分 */
.category-batch-actions .category-tool-btn {
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  border: 1px solid #e4e7ed;
}

/* 多选模式下的复选框：对齐图标位置 */
.category-checkbox {
  margin-right: 6px;
}

/* 分类项 title 的外层 span 撑满整行：分类名过长时省略号生效、hover 操作按钮贴在右侧
   （普通 scoped 规则，元素自带 scope 属性，折叠态弹出层里同样生效） */
.category-item > span {
  flex: 1;
  min-width: 0;
}

/* 分类表单弹窗底部的说明文字 */
.category-form-tip {
  padding-left: 72px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

/* 选中：灰底 + 纯黑加粗文字（特殊显示）；子菜单「视频」下挂在当前路由时同样高亮 */
.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: #eceef0;
  color: #000000;
  font-weight: 600;
}

/* 选中：左侧黑色标记条，折叠态同样可见 */
.sidebar-menu :deep(.el-menu-item.is-active::before),
.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 18px;
  transform: translateY(-50%);
  border-radius: 0 2px 2px 0;
  background: #1f2d3d;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f0f2f5;
}

.user-dropdown {
  display: block;
  width: 100%;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  background: #f7f8fa;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s ease;
}

.user-box:hover,
.user-box.is-active {
  background: #eef4ff;
}

/* el-avatar 自带尺寸与圆形裁剪，这里只覆盖配色，保持原蓝色头像观感 */
.avatar {
  flex-shrink: 0;
  background: #a9bad3;
  color: #ffffff;
  font-size: 14px;
}

.user-info {
  flex: 1;
  min-width: 0;
  line-height: 1.3;
}

.username {
  font-size: 14px;
  color: #1f2d3d;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-arrow {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  color: #909399;
  transition: transform 0.2s ease;
}

.user-box.is-active .user-arrow {
  transform: rotate(180deg);
}

.user-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.main-header {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 32px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.collapse-btn {
  font-size: 18px;
  color: #606266;
}

/* ==================== 面包屑（黑白灰） ==================== */
.breadcrumb-bar {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  height: 44px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.breadcrumb {
  font-size: 14px;
}

/* 普通层级：中灰、常规字重 */
.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #606266;
  font-weight: 400;
  transition: color 0.2s ease;
}

/* 可点击层级 hover：加深为近黑 */
.breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #1f2d3d;
}

.breadcrumb :deep(.el-breadcrumb__separator) {
  color: #c0c4cc;
  margin: 0 8px;
  font-weight: 400;
}

/* 最后一级：当前位置，纯黑加粗且不可点击 */
.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #000000;
  font-weight: 600;
  cursor: default;
}

.breadcrumb-home-icon {
  margin-right: 2px;
  color: #606266;
  vertical-align: -2px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style>

<style>
/* 下拉框会被 teleport 到 body，因此这部分样式不能加 scoped */
.user-dropdown-popper {
  min-width: 150px;
}

.user-dropdown-popper .el-dropdown-menu__item {
  font-size: 14px;
}

/* 折叠态下「视频」子菜单以弹出层展示，弹出层会被 teleport 到 body，因此这部分样式不能加 scoped */
.sidebar-submenu-popper .el-menu,
.sidebar-submenu-popper.el-menu {
  max-height: 320px;
  overflow: auto;
}

.sidebar-submenu-popper .el-menu-item,
.sidebar-submenu-popper.el-menu-item {
  height: 36px;
  line-height: 36px;
  font-size: 13px;
}

/* 弹出层里的菜单项同样保持黑白灰画风（覆盖 element-plus 默认的浅蓝 hover 底色） */
.sidebar-submenu-popper .el-menu-item:hover,
.sidebar-submenu-popper.el-menu-item:hover {
  background: #f2f3f5;
  color: #000000;
}

/* 弹出层里的状态提示项：同样覆盖 element-plus 默认叠加的 0.25 透明度 */
.sidebar-submenu-popper .el-menu-item.is-disabled,
.sidebar-submenu-popper.el-menu-item.is-disabled {
  opacity: 1;
  color: #909399;
  font-size: 12px;
  cursor: not-allowed;
}

/* 说明：搜索框与批量删除工具条位于子菜单内容的普通 div 中（不是 el-menu-item），
   scoped 样式对 teleport 到 body 的元素同样生效，因此折叠态弹出层无需再补一份样式 */
</style>
