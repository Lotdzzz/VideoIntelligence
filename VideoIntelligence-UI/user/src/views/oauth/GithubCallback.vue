<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {githubCallback} from '@/api/oauth'
import {OAUTH_STATE_KEY} from '@/constants/oauthConstants'
import {routesConstants} from '@/constants/routesConstants'
import {AUTH_ERROR_FALLBACK_MESSAGE, resolveAuthErrorMessage} from '@/utils/authError'
import {hasExchangeStarted, markExchangeStarted} from '@/utils/oauthExchangeGuard'
import {setToken} from '@/utils/token'

const route = useRoute()
const router = useRouter()

/** 是否正在换取令牌 */
const loading = ref(true)

/** 错误提示文案，非空即代表登录失败 */
const errorMessage = ref('')

/** 从 query 中安全读取字符串参数 */
const readQueryValue = (value: unknown): string => (typeof value === 'string' ? value : '')

/**
 * 登录失败提示统一由 utils/authError 解析：
 * 后端 Result.error(e.getMessage()) 的 msg 原样展示，不做任何前端映射；
 * 后端未提供 msg（401 / 未被全局异常处理器覆盖的异常 / 网络异常）时才使用统一兜底提示
 */

/** 回到首页，首页右上角可再次发起 GitHub 登录 */
const handleBackHome = () => {
  router.replace(routesConstants.HOME)
}

onMounted(async () => {
  // 先取出参数，再清理地址栏，避免用户刷新时重复使用一次性授权码
  const code = readQueryValue(route.query.code)
  const state = readQueryValue(route.query.state)
  const githubError = readQueryValue(route.query.error)
  const githubErrorDescription = readQueryValue(route.query.error_description)

  await router.replace({query: {}})

  // 用户在 github 页面拒绝授权
  if (githubError !== '') {
    loading.value = false
    errorMessage.value = githubErrorDescription !== '' ? githubErrorDescription : '已取消 GitHub 授权'
    return
  }

  if (code === '' || state === '') {
    loading.value = false
    errorMessage.value = '缺少授权参数，请重新登录'
    return
  }

  // 一次性闸门：同一个 state 只允许兑换一次
  // 刷新 / 重新挂载 / 标签恢复 / 多标签 / HMR 都不会再打后端
  if (hasExchangeStarted(state)) {
    loading.value = false
    errorMessage.value = '该授权码已使用，请重新登录'
    return
  }

  // 校验 state：与发起登录时暂存的值比对（后端还会再校验一次 redis 中的 state）
  const expectedState = sessionStorage.getItem(OAUTH_STATE_KEY)
  sessionStorage.removeItem(OAUTH_STATE_KEY)
  if (expectedState !== null && expectedState !== state) {
    loading.value = false
    errorMessage.value = '登录状态校验失败，请重新登录'
    return
  }

  // 发请求之前先打标记，保证即使请求过程中页面被重新加载也不会重复请求
  markExchangeStarted(state)

  try {
    // 交给后端换取 jwt
    const token = await githubCallback({code, state})
    if (!token) {
      loading.value = false
      errorMessage.value = AUTH_ERROR_FALLBACK_MESSAGE
      return
    }
    setToken(token)
    await router.replace(routesConstants.HOME)
  } catch (error) {
    loading.value = false
    errorMessage.value = resolveAuthErrorMessage(error)
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div class="callback-card">
      <template v-if="loading">
        <span class="callback-spinner" aria-hidden="true"></span>
        <p class="callback-title">正在登录中…</p>
        <p class="callback-desc">正在与 GitHub 校验授权信息，请稍候</p>
      </template>

      <template v-else>
        <svg class="callback-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
          <path
              d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm0 4.5a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0-2.5ZM13.25 17h-2.5v-.9h.75v-3.6h-.75v-.9h1.75v4.5h.75v.9Z"
          />
        </svg>
        <p class="callback-title">登录失败</p>
        <p class="callback-desc">{{ errorMessage }}</p>
        <el-button type="primary" @click="handleBackHome">返回首页重新登录</el-button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.oauth-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: #f5f7fa;
}

.callback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 360px;
  padding: 36px 28px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  text-align: center;
}

.callback-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e8f0ff;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: callback-rotate 0.8s linear infinite;
}

.callback-icon {
  width: 36px;
  height: 36px;
  color: #e6a23c;
}

.callback-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.callback-desc {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #909399;
  word-break: break-all;
}

@keyframes callback-rotate {
  to {
    transform: rotate(360deg);
  }
}
</style>
