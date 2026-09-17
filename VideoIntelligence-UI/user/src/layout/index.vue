<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { routesConstants } from '@/constants/routesConstants'
import { getUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { resolveAuthErrorMessage } from '@/utils/authError'
import { resolveFileUrl } from '@/utils/file'
import { getToken, removeToken } from '@/utils/token'
import LoginDialog from '@/components/LoginDialog.vue'
import type { UserInfo } from '@/types/user/userInfo'

const isCollapse = ref(false)

// 切换账户弹窗是否显示
const loginDialogVisible = ref(false)

const sidebarWidth = computed(() => (isCollapse.value ? '64px' : '260px'))

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

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

/**
 * 用户区展示信息
 * 未登录展示游客；已登录展示接口返回的头像与昵称（昵称为空时退回账号）
 */
const displayUser = computed(() => {
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
  removeToken()
  userStore.clearUserInfo()
}

/**
 * 拉取当前登录用户信息
 * - 无 token：直接按游客处理，不发请求
 * - 有 token：token 失效或接口异常时清理凭证并降级为游客，同时提示用户
 */
const fetchLoginUser = async () => {
  if (!getToken()) {
    clearLoginState()
    return
  }
  try {
    const info = (await getUserInfo()) as unknown as UserInfo
    // 统一存入 pinia，左下角用户区与后续页面都从这里读取
    userStore.setUserInfo(info)
  } catch (error) {
    clearLoginState()
    ElMessage.warning(resolveAuthErrorMessage(error))
  }
}

// 打开登录弹窗：未登录时是「登录」，已登录时是「切换账户」，两者共用同一个弹窗
const openLoginDialog = () => {
  loginDialogVisible.value = true
}

// 切换账户：弹出扫码登录弹窗（演示阶段，弹窗内点击二维码即模拟登录成功）
const handleSwitchAccount = () => {
  openLoginDialog()
}

// 退出登录：清除凭证后停留当前页，用户区回落为游客态
const handleLogout = () => {
  clearLoginState()
  ElMessage.success('已退出登录')
}

// 登录弹窗内登录成功（演示渠道）：重新读取一次登录态
const handleLoginSuccess = () => {
  fetchLoginUser()
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

onMounted(() => {
  fetchLoginUser()
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
        :default-active="routesConstants.HOME"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
      >
        <el-menu-item :index="routesConstants.HOME">
          <el-icon>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 3 3 10.5V21h6v-6h6v6h6V10.5L12 3Z" />
            </svg>
          </el-icon>
          <template #title>首页</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-dropdown
          class="user-dropdown"
          trigger="click"
          placement="top-start"
          popper-class="user-dropdown-popper"
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

      <main class="main-content">
        <router-view />
      </main>
    </div>

    <!-- 登录 / 切换账户弹窗（演示阶段：扫码登录为本地模拟；GitHub 为真实跳转式授权） -->
    <LoginDialog
      v-model="loginDialogVisible"
      :title="isLogin ? '切换账户' : '登录'"
      :subtitle="isLogin ? '使用第三方账号扫码登录' : '使用第三方账号登录'"
      @success="handleLoginSuccess"
    />
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
  background: #1677ff;
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
  background: #1677ff;
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
  height: 64px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.collapse-btn {
  font-size: 18px;
  color: #606266;
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
</style>
