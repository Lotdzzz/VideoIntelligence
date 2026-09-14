<template>
  <div class="login-container">
    <!-- 隐藏的 SVG 裁剪路径：把右侧白色面板的左边缘裁成曲线，让蓝白交界贴合曲线 -->
    <svg width="0" height="0" style="position: absolute" aria-hidden="true">
      <defs>
        <clipPath id="formPanelCurve" clipPathUnits="objectBoundingBox">
          <path d="M0.1,0 C0.04,0.17 0.16,0.33 0.1,0.5 C0.04,0.67 0.16,0.83 0.1,1 L1,1 L1,0 Z" />
        </clipPath>
      </defs>
    </svg>

    <!-- ==================== 左侧品牌展示区 ==================== -->
    <div class="brand-panel">
      <div class="brand-content">
        <!-- Logo 与平台名称 -->
        <div class="brand-logo">
          <el-icon :size="40"><VideoCameraFilled /></el-icon>
        </div>
        <h1 class="brand-title">视频智能分析系统管理平台</h1>
        <p class="brand-subtitle">Video Intelligence Manager Platform</p>

        <!-- 平台特色 -->
        <div class="brand-features">
          <div class="feature-item">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <span>智能视频分析</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Monitor /></el-icon>
            <span>实时数据监控</span>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Lock /></el-icon>
            <span>安全可靠可控</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 右侧登录表单区 ==================== -->
    <div class="form-panel">
      <div class="form-wrapper">
        <!-- 登录标题 -->
        <div class="form-header">
          <h2 class="form-title">账号登录</h2>
          <p class="form-subtitle">欢迎登录视频智能分析后台管理系统</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              :loading="loading"
              type="primary"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部版权信息 -->
        <div class="form-footer">
          <span>© 2026 Video Intelligence Platform</span>
        </div>
      </div>
    </div>

    <!-- 曲线光晕描边：沿蓝白交界曲线绘制，强化分界边的视觉效果 -->
    <svg class="divider-curve" viewBox="0 0 480 1000" preserveAspectRatio="none" aria-hidden="true">
      <defs>
        <linearGradient id="dividerGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#60a5fa" stop-opacity="0" />
          <stop offset="0.15" stop-color="#60a5fa" stop-opacity="0.35" />
          <stop offset="0.5" stop-color="#93c5fd" stop-opacity="1" />
          <stop offset="0.85" stop-color="#60a5fa" stop-opacity="0.35" />
          <stop offset="1" stop-color="#60a5fa" stop-opacity="0" />
        </linearGradient>
      </defs>
      <path
        d="M48,0 C19,170 77,330 48,500 C19,670 77,830 48,1000"
        fill="none"
        stroke="url(#dividerGradient)"
        stroke-width="2"
        stroke-linecap="round"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
// ==================== 依赖导入 ====================
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, VideoCameraFilled, DataAnalysis, Monitor } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { login as loginApi } from '@/api/login'
import { useTokenStore } from '@/stores/token'
import { routesIndexConstants } from '@/constants/routesConstants'
import type { Result } from '@/types/result'

// ==================== 初始化 ====================
// 路由实例：用于登录成功后跳转
const router = useRouter()

// token 状态仓库：用于登录成功后保存 token
const tokenStore = useTokenStore()

// 表单组件实例：用于表单校验
const loginFormRef = ref<FormInstance>()

// 登录按钮 loading 状态：防止重复提交
const loading = ref(false)

// ==================== 表单数据 ====================
// 登录表单数据模型
const loginForm = reactive({
  username: '',
  password: '',
})

// ==================== 表单校验规则 ====================
// 账号、密码均为必填项，失焦时触发校验
const rules: FormRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
}

// ==================== 登录逻辑 ====================
/**
 * 处理登录提交
 * 1. 校验表单是否填写完整
 * 2. 校验通过后调用登录接口
 * 3. 登录成功后保存 token 并跳转到首页
 * 4. 登录失败则弹出错误提示
 */
async function handleLogin() {
  // 如果表单实例不存在，直接返回，避免后续调用报错
  if (!loginFormRef.value) return

  // 调用 Element Plus 表单校验，valid 表示是否通过校验
  await loginFormRef.value.validate(async (valid) => {
    // 校验未通过：不执行登录请求，直接结束
    if (!valid) return

    // 校验通过：开始登录，开启按钮 loading 状态防止重复点击
    loading.value = true

    try {
      // 调用登录接口，传入账号和密码
      const res = (await loginApi(loginForm)) as unknown as { token: string }

      // 登录成功：将接口返回的 token 保存到状态仓库中
      tokenStore.saveToken(res.token)

      // 弹出登录成功提示
      ElMessage.success('登录成功')

      // 跳转到后台管理首页
      router.push(routesIndexConstants.DASHBOARD)
    } catch (error) {
      // 登录失败：展示后端返回的错误信息（Result.msg），若无则展示默认提示
      const err = error as Result
      ElMessage.error(err.msg || '登录失败')
    } finally {
      // 无论登录成功还是失败，最终都要关闭按钮 loading 状态
      loading.value = false
    }
  })
}
</script>

<style scoped>
/* ==================== 整体容器 ==================== */
.login-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(160deg, #1e3a8a 0%, #1d4ed8 50%, #2563eb 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ==================== 左侧品牌展示区 ==================== */
.brand-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* 品牌区背景装饰圆形 */
.brand-panel::before {
  content: '';
  position: absolute;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  top: -120px;
  right: -120px;
}

.brand-panel::after {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.03);
  bottom: -80px;
  left: -80px;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  padding: 0 40px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  margin-bottom: 28px;
  color: #fff;
  backdrop-filter: blur(10px);
}

.brand-title {
  margin: 0 0 12px;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-subtitle {
  margin: 0 0 48px;
  font-size: 14px;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.7);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1px;
}

.feature-item .el-icon {
  color: #93c5fd;
}

/* ==================== 右侧登录表单区 ==================== */
.form-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  position: relative;
  /* 用曲线裁剪左边缘，使蓝白颜色分界贴合曲线 */
  clip-path: url(#formPanelCurve);
}

/* ==================== 左右分界线（蓝色与白色交界） ==================== */
/* 曲线光晕描边：覆盖在右侧白色面板上，沿曲线路径绘制，上下淡出、中部高亮 */
.divider-curve {
  position: absolute;
  right: 0;
  top: 0;
  width: 480px;
  height: 100%;
  pointer-events: none;
  z-index: 2;
  filter: drop-shadow(0 0 10px rgba(59, 130, 246, 0.55));
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
  padding: 0 20px;
}

.form-header {
  margin-bottom: 36px;
}

.form-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 1px;
}

.form-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

/* ==================== 表单样式 ==================== */
.login-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  height: 44px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #93c5fd inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}

.login-form :deep(.el-input__inner) {
  color: #1f2937;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

.login-form :deep(.el-input__prefix-inner .el-icon) {
  color: #9ca3af;
}

.login-form :deep(.el-input__suffix .el-icon) {
  color: #9ca3af;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 6px;
  font-size: 16px;
  letter-spacing: 4px;
  background: #2563eb;
  border: none;
  transition: all 0.3s ease;
  margin-top: 4px;
}

.login-btn:hover {
  background: #1d4ed8;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.3);
}

.login-btn:active {
  background: #1e40af;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}
</style>