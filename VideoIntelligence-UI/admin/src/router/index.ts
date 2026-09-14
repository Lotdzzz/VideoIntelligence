import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { routesIndexConstants } from '@/constants/routesConstants'
import { getToken } from '@/utils/token'
import { getValid } from '@/api/login'
import { usePermissionsStore } from '@/stores/permissions'
import { useRolesStore } from '@/stores/roles'
import { useRoutersStore } from '@/stores/routers'
import type { ValidVO } from '@/types/valid'
import type { RouterVO } from '@/types/routers'
import ParentView from '@/components/ParentView.vue'

// ==================== 静态路由 ====================
const routes: RouteRecordRaw[] = [
  {
    path: routesIndexConstants.LOGIN,
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: '登录',
      hidden: true
    }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: routesIndexConstants.DASHBOARD,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          requiresAuth: true
        }
      },
      {
        path: 'system/user/profile',
        name: 'Profile',
        component: () => import('@/views/system/user/profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      }

    ]
  },
  {
    path: routesIndexConstants.NOT_FOUND,
    name: '404',
    component: () => import('@/views/error/404.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==================== 组件映射 ====================
// 使用 Vite 的 import.meta.glob 自动扫描 views 目录下所有 .vue 文件，
// 建立「后端 component 字符串 -> 组件加载函数」的映射，
// 约定规则：后端 "system/user/index" 对应 @/views/system/user/index.vue
const viewModules = import.meta.glob('@/views/**/*.vue')

// 把 glob 的 key（如 /src/views/system/user/index.vue）统一规范成 system/user/index
const viewComponentMap = new Map<string, () => Promise<unknown>>()
for (const [key, loader] of Object.entries(viewModules)) {
  const normalized = key
    .replace(/^\/src\/views\//, '')
    .replace(/^\/views\//, '')
    .replace(/^@\/views\//, '')
    .replace(/\.vue$/, '')
  viewComponentMap.set(normalized, loader)
}

/**
 * 根据后端返回的 component 字符串解析出真实的组件加载函数
 * 特殊值 "Layout" 映射到布局组件
 */
function resolveComponent(component: string): NonNullable<RouteRecordRaw['component']> {
  if (component === 'Layout') {
    return () => import('@/layout/index.vue')
  }

  const normalized = component.replace(/^\/+/, '')
  const loader = viewComponentMap.get(normalized)

  if (!loader) {
    console.warn(`[router] 未找到视图组件: ${component}`)
    return () => import('@/views/error/404.vue')
  }

  return loader as NonNullable<RouteRecordRaw['component']>
}

// ==================== 动态路由生成 ====================
/**
 * 将后端 RouterVO 递归转换为 Vue Router 的 RouteRecordRaw
 * - 顶级路由 path 以 / 开头（如 /system），子路由为相对路径（如 user）
 * - redirect 为 noRedirect 时不设置，避免父路由强制跳转第一个子路由
 */
function buildRouteRecord(vo: RouterVO, depth = 0): RouteRecordRaw {
  const meta = {
    title: vo.meta?.title,
    icon: vo.meta?.icon,
    keepAlive: vo.meta?.keepAlive,
    perms: vo.meta?.perms,
    frame: vo.meta?.frame,
    hidden: vo.hidden
  }

  let record: RouteRecordRaw

  if (Array.isArray(vo.children) && vo.children.length > 0) {
    // 有子路由：作为父级路由，只需要 component + children
    // 顶层目录渲染 Layout（含侧边栏）；嵌套目录用 ParentView 透传，避免内容区重复出现侧边栏
    record = {
      path: vo.path,
      name: vo.name,
      component: depth === 0 ? resolveComponent(vo.component) : ParentView,
      meta,
      children: vo.children.map((child) => buildRouteRecord(child, depth + 1))
    }
  } else if (vo.redirect && vo.redirect !== 'noRedirect') {
    // 无子路由但有重定向：作为重定向路由
    record = {
      path: vo.path,
      name: vo.name,
      redirect: vo.redirect,
      meta
    }
  } else {
    // 普通页面路由：只有 component
    record = {
      path: vo.path,
      name: vo.name,
      component: resolveComponent(vo.component),
      meta
    }
  }

  return record
}

// 是否已拉取过权限/角色/路由信息（避免每次导航重复请求）
let isLoaded = false

// 已动态注册的路由名称，用于退出登录时移除
const dynamicRouteNames: string[] = []

/** 注册后端返回的路由树 */
function registerDynamicRoutes(routers: RouterVO[]) {
  routers.forEach((vo) => {
    const record = buildRouteRecord(vo)
    router.addRoute(record)
    dynamicRouteNames.push(vo.name)
  })
}

/** 清空动态路由与菜单，并重置加载标记 */
function resetRouter() {
  isLoaded = false
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })
  dynamicRouteNames.length = 0

  // 清空菜单数据，避免退出登录后侧边栏残留
  const routersStore = useRoutersStore()
  routersStore.clearRouters()

  // 清空权限与角色数据，避免切换账号后残留上一个账号信息
  const permissionsStore = usePermissionsStore()
  permissionsStore.clearPermissions()

  const rolesStore = useRolesStore()
  rolesStore.clearRoles()
}

// ==================== 全局前置路由守卫 ====================
// 1. 无 token 时清空动态路由，若目标需要登录则跳转登录页
// 2. 有 token 且尚未初始化时，调用 getValid 获取权限/角色/路由树并注册动态路由
router.beforeEach(async (to) => {
  const token = getToken()

  // 未登录：先清空动态路由、菜单与权限状态，避免残留上个账号信息。
  // 必须放在“登录页直接放行”判断之前执行，否则退出登录后 isLoaded 仍为 true，
  // 下一个账号登录时不会重新拉取其路由，导致菜单串号。
  if (!token) {
    resetRouter()

    // 登录页直接放行：不触发动态路由加载，
    // 避免 / 重定向到 /login 后因 token 残留反复加载造成死循环
    if (to.path === routesIndexConstants.LOGIN) {
      return true
    }

    if (to.meta.requiresAuth) {
      return routesIndexConstants.LOGIN
    }
    return true
  }

  // 已登录访问登录页：直接放行，不重复加载动态路由
  if (to.path === routesIndexConstants.LOGIN) {
    return true
  }

  // 已登录但尚未初始化：拉取权限/角色/路由树并注册动态路由。
  // 这里处理刷新场景：刷新 /system/user 时动态路由尚未注册，
  // 此时目标只会命中兜底 404，因此先加载动态路由再重试进入原目标。
  if (!isLoaded) {
    const permissionsStore = usePermissionsStore()
    const rolesStore = useRolesStore()
    const routersStore = useRoutersStore()
    try {
      const data = (await getValid()) as unknown as ValidVO
      permissionsStore.setPermissions(data.permissions ?? [])
      rolesStore.setRoles(data.roles ?? [])
      routersStore.setRouters(data.routers ?? [])
      registerDynamicRoutes(data.routers ?? [])
      isLoaded = true
      // 动态路由注册后，用完整路径重新触发一次导航，
      // 让 vue-router 重新解析并匹配到刚注册的 /system/user 等动态路由
      return to.fullPath
    } catch (error) {
      // 401 已由响应拦截器处理，这里统一回到登录页
      resetRouter()
      return routesIndexConstants.LOGIN
    }
  }

  return true
})

export default router