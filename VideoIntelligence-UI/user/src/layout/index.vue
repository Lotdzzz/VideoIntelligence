<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { routesConstants, routesIndexConstants } from '@/constants/routesConstants'
import { removeToken } from '@/utils/token'
import LoginDialog from '@/components/LoginDialog.vue'

const isCollapse = ref(false)

// 切换账户弹窗是否显示
const loginDialogVisible = ref(false)

const sidebarWidth = computed(() => (isCollapse.value ? '64px' : '260px'))

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 当前登录用户信息，后续可替换为接口 / 状态管理中的数据
const userInfo = ref({
  name: '用户',
  role: '普通用户',
  avatarText: '用',
})

// 用户下拉框是否展开，用于给用户框添加高亮态
const userMenuVisible = ref(false)

const handleUserMenuVisibleChange = (visible: boolean) => {
  userMenuVisible.value = visible
}

// 清除本地登录凭证
const clearLoginState = () => {
  removeToken()
}

// 回到登录页（与 utils/request.ts 中 401 的处理方式保持一致）
const goLoginPage = () => {
  window.location.href = routesIndexConstants.LOGIN
}

// 退出登录
const handleLogout = () => {
  clearLoginState()
  ElMessage.success('已退出登录')
  goLoginPage()
}

// 切换账户：弹出扫码登录弹窗（演示阶段，弹窗内点击二维码即模拟登录成功）
const handleSwitchAccount = () => {
  loginDialogVisible.value = true
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'switch') {
    handleSwitchAccount()
  }
}
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
            <div class="avatar">{{ userInfo.avatarText }}</div>
            <div v-show="!isCollapse" class="user-info">
              <div class="username">{{ userInfo.name }}</div>
              <div class="user-role">{{ userInfo.role }}</div>
            </div>
            <span v-show="!isCollapse" class="user-arrow">
              <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                <path d="M12 15.5 4.5 8h15L12 15.5Z" />
              </svg>
            </span>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="switch">
                <span class="user-menu-item">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path
                      d="M12 12a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9Zm0 1.8c-3.6 0-6.6 1.9-6.6 4.3V21h13.2v-2.9c0-2.4-3-4.3-6.6-4.3Z"
                    />
                  </svg>
                  <span>切换账户</span>
                </span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
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

    <!-- 切换账户弹窗（演示阶段：扫码登录为本地模拟，未接入后端） -->
    <LoginDialog v-model="loginDialogVisible" />
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

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 50%;
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
