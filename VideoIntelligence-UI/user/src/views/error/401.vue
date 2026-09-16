<script setup lang="ts">
import { useRouter } from 'vue-router'
import { routesConstants } from '@/constants/routesConstants'
import unauthorizedImg from '@/assets/401_images/401.gif'

const router = useRouter()

const goHome = () => {
  router.push(routesConstants.HOME)
}
</script>

<template>
  <div class="error-page">
    <img class="error-img" :src="unauthorizedImg" alt="401 Unauthorized" />
    <p class="error-text">抱歉，您暂无权限访问该页面</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #ffffff;
  gap: 24px;
}

.error-img {
  width: 50%;
  max-width: 480px;
  display: block;
}

.error-text {
  margin: 0;
  font-size: 16px;
  color: #606266;
}
</style>