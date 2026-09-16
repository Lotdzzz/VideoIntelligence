<script setup lang="ts">
import { useRouter } from 'vue-router'
import { routesConstants } from '@/constants/routesConstants'
import notFoundImg from '@/assets/404_images/404.png'

const router = useRouter()

const goHome = () => {
  router.push(routesConstants.HOME)
}
</script>

<template>
  <div class="error-page">
    <img class="error-img" :src="notFoundImg" alt="404 Not Found" />
    <p class="error-text">抱歉，您访问的页面不存在</p>
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