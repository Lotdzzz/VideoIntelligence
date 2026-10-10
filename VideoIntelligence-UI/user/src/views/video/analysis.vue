<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import jsMind from 'jsmind'
import 'jsmind/style/jsmind.css'
import { getAnalysisResult } from '@/api/analysisResult'
import { routesConstants } from '@/constants/routesConstants'
import { resolveErrorMessage } from '@/utils/errorMessage'
import type { AnalysisResult } from '@/types/file/analysisResult'

const route = useRoute()
const router = useRouter()

const result = ref<AnalysisResult | null>(null)
const loading = ref(true)
const failed = ref(false)
const mindMapRef = ref<HTMLElement | null>(null)

const queryValue = (value: unknown): string | undefined => {
  const first = Array.isArray(value) ? value[0] : value
  return typeof first === 'string' ? first : undefined
}

const parseResultState = (): AnalysisResult | null => {
  const raw = window.history.state?.analysisResult
  if (typeof raw !== 'string') {
    return null
  }
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed as AnalysisResult : null
  } catch {
    return null
  }
}

const renderMindMap = async () => {
  if (!result.value?.mind?.data) {
    return
  }
  await nextTick()
  if (!mindMapRef.value) {
    return
  }
  const mindMap = new jsMind({
    container: mindMapRef.value,
    editable: false,
    theme: 'primary',
    mode: 'full',
  })
  mindMap.show(result.value.mind)
  mindMap.disable_edit()
}

const loadResult = async () => {
  loading.value = true
  failed.value = false
  const stateResult = parseResultState()
  if (stateResult) {
    result.value = stateResult
    loading.value = false
    await renderMindMap()
    return
  }

  const userId = Number(queryValue(route.query.userId))
  const taskId = Number(queryValue(route.query.taskId))
  if (!Number.isFinite(userId) || userId <= 0 || !Number.isFinite(taskId) || taskId <= 0) {
    failed.value = true
    loading.value = false
    return
  }

  try {
    result.value = await getAnalysisResult(userId, taskId)
    loading.value = false
    await renderMindMap()
  } catch (error) {
    failed.value = true
    ElMessage.error(resolveErrorMessage(error, '分析结果加载失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

const goVideos = () => router.push(routesConstants.VIDEO)

onMounted(loadResult)
</script>

<template>
  <div class="analysis-page">
    <div class="analysis-toolbar">
      <div>
        <div class="analysis-kicker">VIDEO INTELLIGENCE</div>
        <h1>视频分析结果</h1>
      </div>
      <el-button @click="goVideos">返回视频</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="failed" description="分析结果加载失败">
      <el-button type="primary" @click="loadResult">重新加载</el-button>
    </el-empty>
    <el-empty v-else-if="!result" description="该视频暂未生成分析结果">
      <el-button @click="goVideos">返回视频列表</el-button>
    </el-empty>
    <div v-else class="analysis-grid">
      <section class="knowledge-panel">
        <div class="panel-heading">
          <span class="panel-index">01</span>
          <div>
            <h2>知识点大纲</h2>
            <p>从视频内容中提炼的结构化知识</p>
          </div>
        </div>
        <div v-if="result.knowledge" class="knowledge-content">
          <p v-for="(paragraph, index) in result.knowledge.split('\n')" :key="index">
            {{ paragraph }}
          </p>
        </div>
        <el-empty v-else description="暂无知识点大纲" />
      </section>

      <section class="mind-panel">
        <div class="panel-heading">
          <span class="panel-index">02</span>
          <div>
            <h2>{{ result.mind?.meta?.name || '思维导图' }}</h2>
            <p>拖动画布查看知识结构</p>
          </div>
        </div>
        <div v-if="result.mind?.data" ref="mindMapRef" class="mind-map" />
        <el-empty v-else description="暂无思维导图" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.analysis-page {
  --ink: #18212b;
  --muted: #718096;
  --line: #e1e7ec;
  --accent: #d65a3a;
  min-height: 520px;
  color: var(--ink);
}

.analysis-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.analysis-kicker {
  color: var(--accent);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.5px;
}

h1, h2, p { margin: 0; }
h1 { margin-top: 4px; font-size: 24px; letter-spacing: 0; }

.analysis-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 18px;
}

.knowledge-panel, .mind-panel {
  min-width: 0;
  min-height: 560px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 8px;
}

.panel-heading {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--line);
}

.panel-index { color: var(--accent); font-size: 12px; font-weight: 700; }
.panel-heading h2 { font-size: 18px; }
.panel-heading p { margin-top: 4px; color: var(--muted); font-size: 12px; }

.knowledge-content {
  height: 480px;
  overflow: auto;
  padding: 20px 22px 28px;
  white-space: pre-wrap;
  line-height: 1.75;
  color: #3d4854;
  font-size: 14px;
}

.knowledge-content p { margin-bottom: 8px; }
.mind-map { height: 480px; width: 100%; background: #fbfcfd; }

@media (max-width: 900px) {
  .analysis-grid { grid-template-columns: 1fr; }
  .knowledge-panel, .mind-panel { min-height: 420px; }
  .knowledge-content, .mind-map { height: 360px; }
}
</style>