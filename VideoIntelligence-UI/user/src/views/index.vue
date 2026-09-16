<script setup lang="ts">
const statCards = [
  { label: '今日分析任务', value: '128' },
  { label: '累计视频数量', value: '4,362' },
  { label: '告警事件', value: '36' },
  { label: '在线设备', value: '24' },
]
</script>

<template>
  <div class="home">
    <section class="welcome-card">
      <h2 class="welcome-title">欢迎使用智能化视频分析平台</h2>
      <p class="welcome-desc">
        提供视频接入、智能识别、告警管理与数据分析的一站式解决方案。
      </p>
    </section>

    <div class="stat-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  padding: 28px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.welcome-title {
  margin: 0 0 10px;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1677ff;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style>