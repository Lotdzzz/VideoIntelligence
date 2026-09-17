<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
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
  subtitle: '使用第三方账号扫码登录',
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  /** 模拟扫码登录成功（可选的对外事件，父组件需要时可监听） */
  success: []
}>()

// ==================== 渠道配置（演示数据，后续可替换为接口数据） ====================

type ChannelKey = 'wechat' | 'qq' | 'github'

interface Channel {
  /** 渠道标识 */
  key: ChannelKey
  /** 渠道名称 */
  name: string
  /** 渠道品牌色 */
  color: string
  /** 扫码提示文案 */
  hint: string
}

/** 渠道配置表：key → 配置，可直接按 key 取值而无需判空 */
const channelMap: Record<ChannelKey, Channel> = {
  wechat: { key: 'wechat', name: '微信', color: '#07c160', hint: '请使用微信扫一扫登录' },
  qq: { key: 'qq', name: 'QQ', color: '#12b7f5', hint: '请使用手机 QQ 扫一扫登录' },
  github: { key: 'github', name: 'GitHub', color: '#24292f', hint: '点击下方按钮跳转 GitHub 完成授权' },
}

/** 渠道列表，数组顺序即渠道切换的展示顺序 */
const channelList: Channel[] = [channelMap.wechat, channelMap.qq, channelMap.github]

/** 二维码有效期（秒） */
const QR_VALID_SECONDS = 120

/** 假二维码矩阵边长（模块数） */
const QR_MODULE_SIZE = 25

/** 定位角标边长（模块数） */
const QR_FINDER_SIZE = 7

// ==================== 状态 ====================

/** 当前选中的渠道 */
const activeChannel = ref<ChannelKey>('wechat')

/** 当前渠道配置 */
const currentChannel = computed(() => channelMap[activeChannel.value])

/** 是否已勾选同意协议 */
const agreed = ref(false)

/** 二维码种子，刷新时自增用于重新生成图案 */
const qrSeed = ref(1)

/** 二维码剩余有效秒数，0 表示已失效 */
const countdown = ref(QR_VALID_SECONDS)

/** 是否已模拟扫到码、等待手机端确认 */
const isScanning = ref(false)

/** 二维码是否已失效 */
const isExpired = computed(() => countdown.value <= 0)

/** 当前是否为 GitHub 渠道：GitHub 为整页跳转式授权，不使用二维码 */
const isGithubChannel = computed(() => activeChannel.value === 'github')

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
    '1. 账号说明：您可通过微信、QQ、GitHub 等第三方账号扫码登录本平台。您应妥善保管账号信息及登录凭证，不得出租、出借、转让、售卖或以其他方式允许他人使用。因您保管不善造成的损失由您自行承担；如发现账号被盗用或存在安全风险，请及时通知本平台。',
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
    '我们非常重视您的个人信息保护。本政策将说明在您使用扫码登录过程中，我们如何收集、使用与保护相关信息。',
    '1. 信息收集：扫码登录时，我们会获取第三方平台返回的昵称、头像与账号唯一标识，用于创建账号与展示登录身份。',
    '2. 信息使用：上述信息仅用于账号登录、身份识别与服务改进，不会用于其他目的。',
    '3. 信息保护：我们采用加密传输与访问控制等措施保护您的信息，不会向无关第三方提供您的个人信息。',
    '【说明】以上内容为演示用的假数据，正式上线前将替换为法务确认后的隐私政策正文。',
  ],
}

/** 当前查看的协议标题 */
const agreementTitle = computed(() => agreementTitles[activeAgreement.value])

/** 当前查看的协议正文 */
const agreementParagraphs = computed(() => agreementContents[activeAgreement.value])

// ==================== 假二维码生成 ====================

/**
 * 判断某个模块是否落在三个定位角标（回字形）内
 * 返回 null 表示不属于任何角标，由伪随机决定
 */
const getFinderModule = (row: number, col: number): boolean | null => {
  const inTopLeft = row < QR_FINDER_SIZE && col < QR_FINDER_SIZE
  const inTopRight = row < QR_FINDER_SIZE && col >= QR_MODULE_SIZE - QR_FINDER_SIZE
  const inBottomLeft = row >= QR_MODULE_SIZE - QR_FINDER_SIZE && col < QR_FINDER_SIZE

  if (!inTopLeft && !inTopRight && !inBottomLeft) {
    return null
  }

  // 换算成角标内部坐标（角标边长统一为 QR_FINDER_SIZE）
  const finderRow = inBottomLeft ? row - (QR_MODULE_SIZE - QR_FINDER_SIZE) : row
  const finderCol = inTopRight ? col - (QR_MODULE_SIZE - QR_FINDER_SIZE) : col
  const isBorder =
    finderRow === 0 || finderRow === QR_FINDER_SIZE - 1 || finderCol === 0 || finderCol === QR_FINDER_SIZE - 1
  const isCore = finderRow >= 2 && finderRow <= 4 && finderCol >= 2 && finderCol <= 4

  return isBorder || isCore
}

/**
 * 生成假二维码矩阵（演示用，不引入二维码库）
 * 以种子文本驱动线性同余伪随机，保证同一文本每次渲染出的图案一致
 */
const createQrModules = (seedText: string): boolean[] => {
  let seed = 1
  for (let index = 0; index < seedText.length; index += 1) {
    seed = (seed * 131 + seedText.charCodeAt(index)) % 2147483647
  }
  if (seed <= 0) {
    seed = 2147483646
  }

  const modules: boolean[] = []
  for (let row = 0; row < QR_MODULE_SIZE; row += 1) {
    for (let col = 0; col < QR_MODULE_SIZE; col += 1) {
      const finder = getFinderModule(row, col)
      if (finder !== null) {
        // 定位角标固定图案，不参与随机
        modules.push(finder)
        continue
      }
      seed = (seed * 16807) % 2147483647
      modules.push(seed / 2147483647 > 0.45)
    }
  }
  return modules
}

/** 当前二维码矩阵：渠道或种子变化时重新生成 */
const qrModules = computed(() => createQrModules(`${activeChannel.value}-${qrSeed.value}`))

// ==================== 定时器 ====================

let countdownTimer: ReturnType<typeof setInterval> | null = null
let scanTimer: ReturnType<typeof setTimeout> | null = null

const clearCountdownTimer = () => {
  if (countdownTimer !== null) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const clearScanTimer = () => {
  if (scanTimer !== null) {
    clearTimeout(scanTimer)
    scanTimer = null
  }
}

/** 启动二维码有效期倒计时 */
const startCountdown = () => {
  clearCountdownTimer()
  countdownTimer = setInterval(() => {
    if (countdown.value <= 1) {
      countdown.value = 0
      clearCountdownTimer()
      return
    }
    countdown.value -= 1
  }, 1000)
}

/** 重新生成二维码并清空扫码状态 */
const resetQrCode = () => {
  clearScanTimer()
  isScanning.value = false
  qrSeed.value += 1
  countdown.value = QR_VALID_SECONDS
}

// ==================== 交互逻辑 ====================

/** 二维码下方主提示文案 */
const statusText = computed(() => (isScanning.value ? '已扫码，请在手机上确认登录' : currentChannel.value.hint))

/** 二维码状态副文案 */
const countdownText = computed(() => {
  if (!agreed.value) {
    return '勾选下方协议后开始计时'
  }
  if (isScanning.value) {
    return '等待手机端确认…'
  }
  if (isExpired.value) {
    return '二维码已失效，点击二维码可刷新'
  }
  return `二维码 ${countdown.value} 秒后失效`
})

/** 未勾选协议时的统一提示 */
const showAgreementTip = () => {
  ElMessage.warning('请先阅读并同意《用户服务协议》和《隐私政策》')
}

/** 切换渠道：二维码重新生成 */
const handleChannelChange = (key: ChannelKey) => {
  if (key === activeChannel.value) {
    return
  }
  activeChannel.value = key
  resetQrCode()
  // GitHub 为跳转式授权，不涉及二维码倒计时
  if (key !== 'github' && agreed.value) {
    startCountdown()
  }
}

/** 点击失效二维码：重新生成并继续倒计时 */
const handleRefreshQrCode = () => {
  resetQrCode()
  startCountdown()
}

/** 点击二维码：演示环境直接模拟「扫码 → 登录成功」 */
const handleQrCodeClick = () => {
  if (!agreed.value) {
    showAgreementTip()
    return
  }
  if (isExpired.value || isScanning.value) {
    return
  }

  isScanning.value = true
  scanTimer = setTimeout(() => {
    scanTimer = null
    clearCountdownTimer()
    ElMessage.success('登录成功')
    emit('success')
    emit('update:modelValue', false)
  }, 900)
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

    clearCountdownTimer()
    clearScanTimer()
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
  if (!value) {
    clearCountdownTimer()
    clearScanTimer()
  }
}

// ==================== 状态联动 ====================

/** 勾选协议后开始计时；取消勾选则暂停倒计时并放弃当前扫码状态 */
watch(agreed, (value) => {
  if (value) {
    // GitHub 为跳转式授权，无需二维码倒计时
    if (isGithubChannel.value) {
      return
    }
    if (isExpired.value) {
      resetQrCode()
    }
    startCountdown()
    return
  }
  clearCountdownTimer()
  clearScanTimer()
  isScanning.value = false
})

/** 每次打开弹窗重置协议与二维码状态，避免沿用上一次的扫码结果 */
watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    agreed.value = false
    clearCountdownTimer()
    resetQrCode()
  },
)

onBeforeUnmount(() => {
  clearCountdownTimer()
  clearScanTimer()
})
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

    <!-- 渠道切换：手写分段控件，图标沿用项目内联 SVG 风格 -->
    <div class="channel-switcher">
      <button
        v-for="channel in channelList"
        :key="channel.key"
        type="button"
        class="channel-item"
        :class="{ 'is-active': channel.key === activeChannel }"
        @click="handleChannelChange(channel.key)"
      >
        <svg class="channel-icon" viewBox="0 0 24 24" :style="{ color: channel.color }" aria-hidden="true">
          <!-- 微信：双气泡 -->
          <template v-if="channel.key === 'wechat'">
            <path
              fill="currentColor"
              d="M9.2 3C5.2 3 2 5.7 2 9.1c0 1.9 1.1 3.5 2.8 4.6l-.7 2.3 2.6-1.3c.8.2 1.6.3 2.4.3h.5c-.1-.4-.2-.9-.2-1.4 0-3.1 2.9-5.6 6.5-5.6h.4C15.7 4.9 12.7 3 9.2 3Z"
            />
            <circle cx="6.9" cy="7.6" r="1.05" fill="#ffffff" />
            <circle cx="11.5" cy="7.6" r="1.05" fill="#ffffff" />
            <path
              fill="currentColor"
              d="M15.7 9.6c-3.1 0-5.6 2.2-5.6 4.9 0 2.7 2.5 4.9 5.6 4.9.6 0 1.3-.1 1.9-.3l2.3 1.2-.6-2.1c1.2-.9 2-2.3 2-3.7 0-2.7-2.5-4.9-5.6-4.9Z"
            />
            <circle cx="13.9" cy="13.8" r="0.9" fill="#ffffff" />
            <circle cx="17.6" cy="13.8" r="0.9" fill="#ffffff" />
          </template>
          <!-- QQ：企鹅剪影 -->
          <template v-else-if="channel.key === 'qq'">
            <path
              fill="currentColor"
              d="M12 2.3c-2.9 0-5 2.2-5 5.1 0 .6.1 1.2.2 1.7-1.4 2-2.2 4.3-2.2 6.1 0 1.2.6 2.1 1.6 2.5.9.4 2 0 3-.7.6.4 1.5.7 2.4.7s1.8-.3 2.4-.7c1 .7 2.1 1.1 3 .7 1-.4 1.6-1.3 1.6-2.5 0-1.8-.8-4.1-2.2-6.1.1-.5.2-1.1.2-1.7 0-2.9-2.1-5.1-5-5.1Z"
            />
            <circle cx="9.9" cy="7" r="1.05" fill="#ffffff" />
            <circle cx="14.1" cy="7" r="1.05" fill="#ffffff" />
            <ellipse cx="12" cy="15.4" rx="3" ry="3.2" fill="#ffffff" opacity="0.9" />
          </template>
          <!-- GitHub：官方 Octicons mark 路径（16×16 放大到 24×24） -->
          <path
            v-else
            fill="currentColor"
            transform="scale(1.5)"
            d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-1.34-.82-1.53-.28-.17-.72-.38-.01-.39.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z"
          />
        </svg>
        <span class="channel-name">{{ channel.name }}</span>
      </button>
    </div>

    <!-- GitHub：整页跳转式授权登录（不使用二维码） -->
    <div v-if="isGithubChannel" class="github-panel">
      <svg class="github-logo" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
        <path
          d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-1.34-.82-1.53-.28-.17-.72-.38-.01-.39.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z"
        />
      </svg>
      <p class="github-title">使用 GitHub 账号登录</p>
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

    <!-- 二维码区域：演示环境点击二维码即模拟扫码成功 -->
    <div v-else class="qr-panel">
      <div
        class="qr-box"
        :class="{ 'is-clickable': agreed && !isExpired && !isScanning }"
        @click="handleQrCodeClick"
      >
        <!-- 假二维码：由种子伪随机生成的矩阵 + 三个定位角标 -->
        <svg
          class="qr-svg"
          :viewBox="`0 0 ${QR_MODULE_SIZE} ${QR_MODULE_SIZE}`"
          shape-rendering="crispEdges"
          aria-hidden="true"
        >
          <template v-for="(module, index) in qrModules" :key="index">
            <rect
              v-if="module"
              :x="index % QR_MODULE_SIZE"
              :y="Math.floor(index / QR_MODULE_SIZE)"
              width="1"
              height="1"
            />
          </template>
        </svg>

        <!-- 二维码中心渠道标识 -->
        <div class="qr-brand">
          <span class="qr-brand-text" :style="{ background: currentChannel.color }">
            {{ currentChannel.name.charAt(0) }}
          </span>
        </div>

        <!-- 二维码失效 -->
        <div v-if="agreed && isExpired" class="qr-mask" @click.stop="handleRefreshQrCode">
          <span class="qr-mask-text">二维码已失效</span>
          <span class="qr-mask-action">点击刷新</span>
        </div>
        <!-- 未勾选协议：遮罩拦截扫码 -->
        <div v-else-if="!agreed" class="qr-mask" @click.stop="showAgreementTip">
          <svg class="qr-mask-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path
              d="M12 2a5 5 0 0 0-5 5v2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-1V7a5 5 0 0 0-5-5Zm-3 7V7a3 3 0 1 1 6 0v2H9Z"
            />
          </svg>
          <span class="qr-mask-text">请先阅读并同意协议</span>
        </div>
      </div>

      <p class="qr-status" :class="{ 'is-active': isScanning }">{{ statusText }}</p>
      <p class="qr-countdown" :class="{ 'is-warning': agreed && isExpired }">{{ countdownText }}</p>
    </div>

    <!-- 同意协议：未勾选时二维码区域会被遮罩拦截 -->
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

/* ==================== 渠道切换 ==================== */

.channel-switcher {
  display: flex;
  gap: 8px;
  padding: 4px;
  margin-bottom: 18px;
  background: #f5f7fa;
  border-radius: 8px;
}

.channel-item {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 38px;
  padding: 0 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.channel-item:hover {
  color: #303133;
}

.channel-item.is-active {
  background: #ffffff;
  color: #1f2d3d;
  font-weight: 500;
  box-shadow: 0 1px 4px rgba(31, 45, 61, 0.12);
}

.channel-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

/* ==================== 二维码 ==================== */

.qr-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-box {
  position: relative;
  width: 192px;
  height: 192px;
  padding: 8px;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  user-select: none;
}

.qr-box.is-clickable {
  cursor: pointer;
}

.qr-svg {
  display: block;
  width: 100%;
  height: 100%;
  fill: #1f2d3d;
}

.qr-brand {
  position: absolute;
  top: 50%;
  left: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #f0f2f5;
  transform: translate(-50%, -50%);
}

.qr-brand-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
}

.qr-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  color: #909399;
  cursor: pointer;
}

.qr-mask-icon {
  width: 22px;
  height: 22px;
  color: #c0c4cc;
}

.qr-mask-text {
  font-size: 13px;
}

.qr-mask-action {
  font-size: 13px;
  color: #1677ff;
}

.qr-status {
  margin: 12px 0 0;
  font-size: 14px;
  color: #303133;
}

.qr-status.is-active {
  color: #1677ff;
  font-weight: 500;
}

.qr-countdown {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.qr-countdown.is-warning {
  color: #e6a23c;
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

.dialog-tip {
  margin: 12px 0 0;
  text-align: center;
  font-size: 12px;
  line-height: 1.6;
  color: #c0c4cc;
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

