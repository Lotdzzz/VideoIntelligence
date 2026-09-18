<script setup lang="ts">
// 首页占位页：后续首页看板 / 推荐内容在此实现
</script>

<template>
  <div class="home-page">
    <el-empty description="首页建设中，敬请期待" />
  </div>
</template>

<style scoped>
/* 与用户端整体黑白灰风格保持一致：白底 + 浅灰描边 */
.home-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}
</style>