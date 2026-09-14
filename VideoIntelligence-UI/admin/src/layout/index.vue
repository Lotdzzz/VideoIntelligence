<template>
  <div class="app-layout">
    <!-- ==================== 左侧导航栏（蓝白风） ==================== -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <!-- Logo 区域 -->
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="24"><VideoCameraFilled /></el-icon>
        </div>
        <span v-show="!isCollapsed" class="logo-text">视频智能分析</span>
        <!-- 开关边栏按钮 -->
        <div class="collapse-btn" @click="toggleSidebar">
          <el-icon :size="16">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="menu">
        <!-- 首页固定菜单 -->
        <div
          class="menu-item"
          :class="{ active: isActive(routesIndexConstants.DASHBOARD) }"
          @click="handleMenuClick(routesIndexConstants.DASHBOARD)"
        >
          <el-icon :size="18"><HomeFilled /></el-icon>
          <span v-show="!isCollapsed">首页</span>
        </div>

        <!-- 动态菜单：根据后端返回的路由树渲染 -->
        <SidebarItem
          v-for="item in menuRouters"
          :key="item.path"
          :item="item"
          :collapsed="isCollapsed"
          :base-path="item.path"
        />
      </nav>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <div class="user-profile" @click="handleProfileClick">
          <div class="user-avatar">{{ avatarText }}</div>
          <div v-show="!isCollapsed" class="user-info">
            <span class="user-name">{{ userInfo?.nickName || userInfo?.userName || '管理员' }}</span>
            <span class="user-role">系统管理员</span>
          </div>
        </div>
        <el-tooltip content="退出登录" placement="top">
          <div class="logout-btn" @click="handleLogout">
            <el-icon :size="16"><SwitchButton /></el-icon>
          </div>
        </el-tooltip>
      </div>
    </aside>

    <!-- ==================== 右侧主体 ==================== -->
    <main class="main">
      <!-- 面包屑 -->
      <div class="breadcrumb-bar">
        <el-breadcrumb separator="/" class="breadcrumb">
          <!-- 固定首页入口 -->
          <el-breadcrumb-item :to="{ path: routesIndexConstants.DASHBOARD }">
            <el-icon class="breadcrumb__home-icon"><HomeFilled /></el-icon>
            <span>首页</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbs"
            :key="item.path"
            :to="index < breadcrumbs.length - 1 ? { path: item.path } : undefined"
          >
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 页面内容 -->
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  VideoCameraFilled,
  HomeFilled,
  SwitchButton,
  Fold,
  Expand,
} from '@element-plus/icons-vue'
import { getUserInfo, logout } from '@/api/login'
import { routesIndexConstants } from '@/constants/routesConstants'
import { useTokenStore } from '@/stores/token'
import { useRoutersStore } from '@/stores/routers'
import { useUserStore } from '@/stores/user'
import SidebarItem from '@/components/SidebarItem.vue'
import type { Result } from '@/types/result'
import type { SysUserInfo as UserInfo } from '@/types/user/userInfo'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const routersStore = useRoutersStore()
const userStore = useUserStore()

// ==================== 侧边栏开关 ====================
// 折叠状态持久化 key：刷新页面后保持上次的展开/折叠状态
const SIDEBAR_COLLAPSED_KEY = 'sidebar_collapsed'

// 初始化时从 localStorage 恢复上次状态（未设置时默认展开）
const isCollapsed = ref(localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === '1')

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
  // 持久化折叠状态
  localStorage.setItem(SIDEBAR_COLLAPSED_KEY, isCollapsed.value ? '1' : '0')
}

// ==================== 用户信息 ====================
const userInfo = ref<UserInfo | null>(null)

const avatarText = computed(() => {
  const name = userInfo.value?.nickName || userInfo.value?.userName || '管理员'
  return name.slice(0, 1)
})

// ==================== 导航 ====================
// 过滤掉 hidden 的顶级菜单
const menuRouters = computed(() =>
  routersStore.routers.filter((item) => item.hidden !== true)
)

const isActive = (path: string) => {
  return path === route.path
}

const handleMenuClick = (path: string) => {
  router.push(path)
}

// 点击底部用户信息，进入个人中心
const handleProfileClick = () => {
  router.push(routesIndexConstants.PROFILE)
}

// ==================== 面包屑 ====================
const breadcrumbs = computed(() => {
  return route.matched
    .filter((item) => {
      // 只展示有实际页面的叶子路由：
      // 1）必须有标题
      // 2）排除固定首页（已单独固定展示）
      // 3）排除目录父路由（有 children，如“系统管理”），它们不直接渲染子页面
      return (
        item.meta?.title &&
        item.meta.title !== '首页' &&
        !(item.children && item.children.length > 0)
      )
    })
    .map((item) => ({
      path: item.path,
      title: item.meta.title as string,
    }))
})

// ==================== 退出登录 ====================
const handleLogout = async () => {
  try {
    // 调用后端退出登录接口，清理服务端会话/Token
    await logout()
  } catch (error) {
    // 后端退出失败也继续本地退出，避免卡住用户
  }
  tokenStore.clearToken()
  ElMessage.success('已退出登录')
  router.push(routesIndexConstants.LOGIN)
}

// ==================== 获取用户信息 ====================
const fetchUserInfo = async () => {
  try {
    const info = (await getUserInfo()) as unknown as UserInfo
    userInfo.value = info
    // 同步到全局用户 store，供个人中心等页面直接读取 userId
    userStore.setUserInfo({
      userId: info.userId,
      userName: info.userName,
      nickName: info.nickName,
      email: info.email,
      avatar: info.avatar,
    })
  } catch (error) {
    const err = error as Result
    if (err?.code === 401) {
      router.push(routesIndexConstants.LOGIN)
      return
    }
    ElMessage.error(err?.msg || '获取用户信息失败')
    router.push(routesIndexConstants.LOGIN)
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'PingFang SC', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
  box-sizing: border-box;
}

/* ==================== 左侧导航栏 ==================== */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #1e3a8a 0%, #1d4ed8 60%, #2563eb 100%);
  color: #fff;
  box-shadow: 2px 0 12px rgba(30, 58, 138, 0.18);
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 72px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
  flex: 1;
  overflow: hidden;
}

.collapse-btn {
  position: absolute;
  right: -13px;
  top: 26px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #fff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.25);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  color: #1d4ed8;
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.4);
}

.menu {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
  overflow-x: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.78);
  font-size: 14px;
  letter-spacing: 1px;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.menu-item.active {
  background: #fff;
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.14);
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.user-profile {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  border-radius: 8px;
  padding: 4px 6px;
  transition: background 0.25s ease;
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.12);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  white-space: nowrap;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.logout-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

/* ==================== 侧边栏折叠状态 ==================== */
.sidebar.collapsed .logo-area {
  padding: 22px 0;
  justify-content: center;
}

.sidebar.collapsed .menu {
  padding: 16px 10px;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 12px 0;
}

.sidebar.collapsed .sidebar-footer {
  justify-content: center;
  padding: 16px 8px;
}

.sidebar.collapsed .user-profile {
  justify-content: center;
}

.sidebar.collapsed .logout-btn {
  display: none;
}

/* ==================== 右侧主体 ==================== */
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.breadcrumb-bar {
  flex-shrink: 0;
  background: #fff;
  padding: 14px 24px;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6b7280;
  font-weight: 400;
  transition: color 0.2s ease;
}

.breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #2563eb;
}

.breadcrumb :deep(.el-breadcrumb__separator) {
  color: #cbd5e1;
  margin: 0 8px;
  font-weight: 500;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1e3a8a;
  font-weight: 700;
}

.breadcrumb__home-icon {
  color: #2563eb;
  vertical-align: -2px;
  margin-right: 2px;
}

.content-wrapper {
  flex: 1;
  padding: 0;
  box-sizing: border-box;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .sidebar {
    width: 72px;
  }

  .sidebar:not(.collapsed) .logo-text {
    display: none;
  }

  .logo-area {
    padding: 22px 0;
    justify-content: center;
  }

  .sidebar:not(.collapsed) .menu {
    padding: 16px 10px;
  }

  .sidebar:not(.collapsed) .menu-item {
    justify-content: center;
    padding: 12px 0;
  }

  .sidebar:not(.collapsed) .menu-item span {
    display: none;
  }

  .sidebar:not(.collapsed) .user-info {
    display: none;
  }

  .sidebar:not(.collapsed) .sidebar-footer {
    justify-content: center;
    padding: 16px 8px;
  }

  .sidebar:not(.collapsed) .logout-btn {
    display: none;
  }
}
</style>