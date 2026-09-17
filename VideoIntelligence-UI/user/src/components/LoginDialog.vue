<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { CheckboxValueType } from 'element-plus'
import { getGithubLoginUrl } from '@/api/oauth'
import { OAUTH_STATE_KEY } from '@/constants/oauthConstants'
import { resolveAuthErrorMessage } from '@/utils/authError'

// ==================== Props / Emits ====================

interface Props {
  /** 弹窗是否显示（v-model） */
  modelValue: boolean
  /** 弹窗标题 */
  title?: string
  /** 弹窗副标题 */
  subtitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '切换账户',
  subtitle: '使用 GitHub 账号登录',
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// ==================== 状态 ====================

/** 是否已勾选同意协议 */
const agreed = ref(false)

/** GitHub 授权地址是否正在获取中 */
const githubLoading = ref(false)

/** 协议正文弹窗是否显示 */
const agreementVisible = ref(false)

/** 当前查看的协议标识 */
const activeAgreement = ref<'service' | 'privacy'>('service')

/** 协议标题（演示假数据） */
const agreementTitles: Record<'service' | 'privacy', string> = {
  service: '用户服务协议',
  privacy: '隐私政策',
}

/** 协议正文（演示假数据，正式上线前替换为法务确认后的正文） */
const agreementContents: Record<'service' | 'privacy', string[]> = {
  service: [
    '欢迎使用智能化视频分析平台（以下简称“本平台”）。本协议是您与本平台之间就注册、登录及使用本平台服务所订立的协议。请您在使用前仔细阅读并充分理解本协议全部内容，尤其是账号规则、使用规范、数据与隐私、免责声明、责任限制及争议解决等条款。您通过第三方账号登录或实际使用本平台服务，即视为您已阅读、理解并同意本协议。',
    '1. 账号说明：您可通过 GitHub 账号登录本平台。您应妥善保管账号信息及登录凭证，不得出租、出借、转让、售卖或以其他方式允许他人使用。因您保管不善造成的损失由您自行承担；如发现账号被盗用或存在安全风险，请及时通知本平台。',
    '2. 使用规范：您应遵守法律法规、公序良俗及本平台规则，不得利用本平台从事任何违法、侵权、危害网络安全、干扰平台运行或侵犯他人合法权益的活动。您应对通过您的账号实施的全部行为及发布、上传、处理的内容负责。',
    '3. 视频与数据合规：您上传、提交或通过本平台处理的视频、图片、音频、文本等内容，应确保来源合法且已取得必要授权。您不得上传含有国家秘密、商业秘密、违法有害信息或未经授权收集的人脸、身份、隐私等个人信息的内容。本平台仅在提供智能化视频分析服务所必需的范围内对相关内容进行存储、处理、分析和展示。',
    '4. 服务内容与变更：本平台提供智能化视频分析及相关技术服务，具体功能以页面实际展示为准。本平台有权根据业务需要调整、暂停或终止部分或全部服务，并将以适当方式提前告知。免费服务不保证长期、持续、稳定提供。',
    '5. 隐私与数据保护：本平台重视用户隐私与数据安全，将按照法律法规及《隐私政策》处理您的个人信息。您使用本平台即表示您已阅读并同意《隐私政策》的相关安排。',
    '6. 知识产权：本平台及其相关软件、技术、界面、文档、商标、标识等内容的知识产权归本平台或相关权利人所有。未经书面许可，您不得复制、修改、传播、出售、出租、反向工程、反编译或以其他方式用于非本平台服务目的。',
    '7. 免责声明：本平台服务按“现状”和“可提供”状态提供，不保证服务无中断、无错误、及时、安全或满足您的全部需求。因不可抗力、网络故障、设备故障、系统维护、第三方原因、政府行为等导致的服务中断或数据丢失，本平台在法律允许范围内不承担责任。',
    '8. 违约处理：如您违反本协议或法律法规，本平台有权视情节采取警告、删除内容、限制功能、暂停或终止账号、终止服务等措施，并有权保存相关记录、依法向有关部门报告或配合调查。',
    '9. 协议变更：本平台有权根据法律法规及业务需要更新本协议，并通过公告、弹窗、站内信、邮件等方式通知您。若您继续使用服务，视为接受更新后的协议；若您不同意，应停止使用并注销账号。',
    '10. 法律适用与争议解决：本协议的订立、效力、解释、履行及争议解决适用中华人民共和国法律。因本协议或本平台服务产生争议，双方应友好协商解决；协商不成的，可向本平台运营方所在地有管辖权的人民法院提起诉讼。',
    '【生效说明】本协议自发布之日起生效。正式上线版本以法务确认后的协议正文为准。',
  ],
  privacy: [
    '我们非常重视您的个人信息保护。本政策将说明在您使用 GitHub 账号登录过程中，我们如何收集、使用与保护相关信息。',
    '1. 信息收集：通过 GitHub 授权登录时，我们会获取 GitHub 返回的昵称、头像与账号唯一标识，用于创建账号与展示登录身份。',
    '2. 信息使用：上述信息仅用于账号登录、身份识别与服务改进，不会用于其他目的。',
    '3. 信息保护：我们采用加密传输与访问控制等措施保护您的信息，不会向无关第三方提供您的个人信息。',
    '【说明】以上内容为演示用的假数据，正式上线前将替换为法务确认后的隐私政策正文。',
  ],
}

/** 当前查看的协议标题 */
const agreementTitle = computed(() => agreementTitles[activeAgreement.value])

/** 当前查看的协议正文 */
const agreementParagraphs = computed(() => agreementContents[activeAgreement.value])

// ==================== 交互逻辑 ====================

/** 未勾选协议时的统一提示 */
const showAgreementTip = () => {
  ElMessage.warning('请先阅读并同意《用户服务协议》和《隐私政策》')
}

// ==================== GitHub 跳转式登录 ====================

/**
 * GitHub 授权登录
 * 1 请求后端获取 github 授权 url（url 中带有后端生成的 state）
 * 2 把 state 暂存到 sessionStorage，供回调页做比对
 * 3 整页跳转到 github 授权页面，授权完成后 github 会重定向到前端回调页
 */
const handleGithubLogin = async () => {
  if (!agreed.value) {
    showAgreementTip()
    return
  }
  if (githubLoading.value) {
    return
  }

  githubLoading.value = true
  try {
    const url = await getGithubLoginUrl()
    if (!url) {
      githubLoading.value = false
      ElMessage.error('获取 GitHub 授权地址失败，请稍后重试')
      return
    }

    // 暂存 state：回调页会与 github 回传的 state 做比对
    const state = new URL(url).searchParams.get('state')
    if (state) {
      sessionStorage.setItem(OAUTH_STATE_KEY, state)
    }

    // 整页跳转，离开当前页面
    // 注意：这里不再复位 githubLoading，按钮保持禁用直到页面跳转，避免连点重复发起登录请求
    window.location.href = url
  } catch (error) {
    githubLoading.value = false
    // 后端 Result.error(e.getMessage()) 的 msg 原样展示，后端没给 msg 时才用统一兜底提示
    ElMessage.error(resolveAuthErrorMessage(error))
  }
}

/** 勾选 / 取消勾选同意协议 */
const handleAgreementChange = (value: CheckboxValueType) => {
  agreed.value = Boolean(value)
}

/** 点击协议说明文字同样可以勾选 / 取消勾选 */
const toggleAgreement = () => {
  agreed.value = !agreed.value
}

/** 打开协议正文弹窗 */
const handleAgreementClick = (key: 'service' | 'privacy') => {
  activeAgreement.value = key
  agreementVisible.value = true
}

/** 在协议正文弹窗中点击「我已阅读并同意」 */
const handleAgreementAccept = () => {
  agreed.value = true
  agreementVisible.value = false
}

/** 弹窗显隐变化 */
const handleVisibleChange = (value: boolean) => {
  emit('update:modelValue', value)
}

// ==================== 状态联动 ====================

/** 每次打开弹窗重置协议勾选状态，避免沿用上一次的登录意愿 */
watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    agreed.value = false
  },
)
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    class="login-dialog"
    width="420px"
    align-center
    @update:model-value="handleVisibleChange"
  >
    <!-- 头部：标题 + 说明 -->
    <template #header>
      <div class="dialog-header">
        <span class="dialog-title">{{ title }}</span>
        <span class="dialog-subtitle">{{ subtitle }}</span>
      </div>
    </template>

    <!-- GitHub：整页跳转式授权登录 -->
    <div class="github-panel">
      <svg class="github-logo" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
        <path
          d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-1.34-.82-1.53-.28-.17-.72-.38-.01-.39.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z"
        />
      </svg>
      <p class="github-title">使用 GitHub 账号登录</p>
      <p class="github-desc">点击下方按钮将跳转到 GitHub 完成授权，授权后会自动返回本平台</p>
      <el-button
        class="github-button"
        type="primary"
        size="large"
        :loading="githubLoading"
        :disabled="githubLoading"
        @click="handleGithubLogin"
      >
        跳转到 GitHub 授权登录
      </el-button>
    </div>

    <!-- 同意协议：未勾选时无法发起授权登录 -->
    <div class="agreement-row">
      <el-checkbox :model-value="agreed" @change="handleAgreementChange" />
      <span class="agreement-text" @click="toggleAgreement">
        <span>我已阅读并同意</span>
        <el-link type="primary" :underline="false" @click.stop="handleAgreementClick('service')">
          《{{ agreementTitles.service }}》
        </el-link>
        <span>与</span>
        <el-link type="primary" :underline="false" @click.stop="handleAgreementClick('privacy')">
          《{{ agreementTitles.privacy }}》
        </el-link>
      </span>
    </div>
  </el-dialog>

  <!-- 协议正文弹窗（假数据文案） -->
  <el-dialog
    v-model="agreementVisible"
    class="agreement-dialog"
    :title="agreementTitle"
    width="520px"
    align-center
    append-to-body
  >
    <div class="agreement-content">
      <p v-for="(paragraph, index) in agreementParagraphs" :key="index" class="agreement-paragraph">
        {{ paragraph }}
      </p>
    </div>
    <template #footer>
      <el-button @click="agreementVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleAgreementAccept">我已阅读并同意</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.dialog-subtitle {
  font-size: 13px;
  color: #909399;
}

/* ==================== GitHub 跳转式登录 ==================== */

.github-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 0 8px;
  text-align: center;
}

.github-logo {
  width: 44px;
  height: 44px;
  color: #24292f;
}

.github-title {
  margin: 6px 0 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.github-desc {
  margin: 0 0 12px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}

.github-button {
  width: 100%;
}

/* ==================== 协议 ==================== */

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 18px;
  font-size: 13px;
  color: #606266;
}

/* 重置 Element Plus 复选框默认的 30px 右外边距，改用弹性布局间距 */
.agreement-row :deep(.el-checkbox) {
  height: auto;
  margin-right: 4px;
}

.agreement-text {
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
}

.agreement-text :deep(.el-link) {
  font-size: 13px;
}

.agreement-content {
  max-height: 360px;
  overflow: auto;
}

.agreement-paragraph {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}
</style>

<style>
/* 弹窗会被 teleport 到 body，这部分样式不能加 scoped */
.login-dialog {
  border-radius: 12px;
}

.login-dialog .el-dialog__header {
  padding: 20px 24px 0;
  margin-right: 0;
}

.login-dialog .el-dialog__body {
  padding: 16px 24px 22px;
}

.agreement-dialog .el-dialog__body {
  padding: 8px 24px 0;
}
</style>

