import {getSliceInfo, reportSliceCompleted} from '@/api/videoUpload'
import {putSliceToMinio} from '@/utils/minioPartUpload'
import {resolveErrorMessage} from '@/utils/errorMessage'
import type {VideoSlicePartVO} from '@/types/file/videoSlice'

/**
 * 视频上传（分片直传）编排
 *
 * 整体链路（每个文件）：
 * 1. 前端先做预校验（扩展名白名单 / 空文件），避免把无效文件送到后端
 * 2. POST /file/user/predesign/slice/info 取回 uploadId、每片大小、总分片数、各分片预签名URL
 * 3. 按 partSize 切片，并发直传对象存储（每个分片 PUT 到自己的预签名URL，读响应头 ETag）
 * 4. 每片直传成功后串行上报 POST /file/user/predesign/slice/upload
 *    （服务端按上报次数累加完成分片数，完成数等于总分片数的那一次触发合并与落库）
 * 5. 单个文件失败不中断整批，最终汇总「成功 / 失败」交给页面展示
 *
 * 刻意不做的能力（后端当前没有对应接口）：
 * - 暂停 / 续传：每次 slice/info 都会生成新的对象名，无法复用已上传的分片
 * - 取消并清理：后端没有 abort 接口，中断后对象存储会残留未完成分片（由服务端过期清理）
 */

/**
 * 允许上传的视频扩展名
 * 必须与后端 Nacos 配置（VI_GROUP/application-file.yaml → upload.white.list）保持一致，
 * 否则会出现「前端放行、后端拒绝」的不一致体验
 */
export const VIDEO_EXTENSIONS = [
  'mp4',
  'mov',
  'avi',
  'mkv',
  'webm',
  'flv',
  'wmv',
  'm4v',
  'mpeg',
  'mpg',
  '3gp',
  '3g2',
  'ts',
  'm2ts',
  'ogv',
]

/** el-upload 的 accept 取值（按扩展名过滤，比 video/* 更贴合后端白名单） */
export const VIDEO_ACCEPT = VIDEO_EXTENSIONS.map((ext) => `.${ext}`).join(',')

/** 同一文件内并发直传的分片数：过高会挤占带宽并拖慢单片的反馈 */
const PART_CONCURRENCY = 3

/** 分片直传失败重试次数（不含首次） */
const PUT_RETRY_TIMES = 2

/** 分片完成上报失败重试次数（不含首次） */
const REPORT_RETRY_TIMES = 2

/** 重试基础间隔（毫秒），按 1s / 2s 递增退避 */
const RETRY_BASE_DELAY_MS = 1000

/** 单个文件的上传状态 */
export type UploadTaskStatus = 'pending' | 'uploading' | 'success' | 'failed'

/** 单个文件的上传进度（页面按下标绑定展示） */
export interface UploadTaskProgress {
  /** 文件名 */
  name: string

  /** 上传百分比（0 ~ 100） */
  percent: number

  /** 已完成上报的分片数 */
  finishedParts: number

  /** 总分片数（取到分片任务前为 0） */
  totalParts: number

  /** 上传状态 */
  status: UploadTaskStatus

  /** 失败原因（status 为 failed 时有值） */
  error?: string
}

/** 上传选项 */
export interface UploadVideosOptions {
  /** 目标分类ID（后端按分类落库，必须传有效数字） */
  categoryId: number

  /** 进度回调：index 为文件在本次上传列表中的下标（同名文件也能区分） */
  onProgress?: (task: UploadTaskProgress, index: number) => void
}

/** 上传汇总结果 */
export interface UploadSummary {
  /** 上传成功的文件名 */
  succeeded: string[]

  /** 上传失败的文件名与原因 */
  failed: { name: string; reason: string }[]
}

/** 读取扩展名（不含点，统一小写）；无扩展名时返回空串 */
function getExtension(fileName: string): string {
  const dotIndex = fileName.lastIndexOf('.')
  if (dotIndex <= 0 || dotIndex === fileName.length - 1) {
    return ''
  }
  return fileName.slice(dotIndex + 1).toLowerCase()
}

/**
 * 单文件预校验（与后端白名单、空文件限制对齐）
 *
 * @returns 校验通过返回 null，否则返回可直接展示的错误文案
 */
export function validateVideoFile(file: File): string | null {
  const extension = getExtension(file.name)
  if (!VIDEO_EXTENSIONS.includes(extension)) {
    return extension ? `不支持的视频格式：.${extension}` : '文件名缺少扩展名，无法识别视频格式'
  }
  if (file.size <= 0) {
    return '文件内容为空，无法上传'
  }
  return null
}

/** 解析失败原因：优先展示后端 msg，其次 Error.message，最后兜底文案 */
function resolveUploadError(error: unknown): string {
  const backendMessage = resolveErrorMessage(error, '')
  if (backendMessage) {
    return backendMessage
  }
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '上传失败，请稍后重试'
}

const delay = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms))

/** 带退避的重试：全部失败后抛出最后一次错误（交给调用方决定是否中断） */
async function withRetry<T>(task: () => Promise<T>, times: number): Promise<T> {
  let lastError: unknown = null
  for (let attempt = 0; attempt <= times; attempt += 1) {
    try {
      return await task()
    } catch (error) {
      lastError = error
      if (attempt < times) {
        await delay(RETRY_BASE_DELAY_MS * 2 ** attempt)
      }
    }
  }
  throw lastError
}

/** 限定并发数的批量执行（worker 内部自行吞掉异常，便于「出错后不再领取新任务」） */
async function runWithConcurrency<T>(
    items: T[],
    limit: number,
    worker: (item: T) => Promise<void>,
): Promise<void> {
  let cursor = 0
  const runners = Array.from({length: Math.min(limit, items.length)}, async () => {
    while (cursor < items.length) {
      const item = items[cursor]
      cursor += 1
      if (item === undefined) {
        return
      }
      await worker(item)
    }
  })
  await Promise.all(runners)
}

/**
 * 上传单个文件（直传对象存储 + 逐片上报）
 *
 * @param file       待上传文件
 * @param categoryId 目标分类ID
 * @param notify     进度回调（每次传入新对象，页面直接替换即可触发刷新）
 */
async function uploadOneFile(
    file: File,
    categoryId: number,
    notify: (task: UploadTaskProgress) => void,
): Promise<void> {
  const task: UploadTaskProgress = {
    name: file.name,
    percent: 0,
    finishedParts: 0,
    totalParts: 0,
    status: 'uploading',
  }
  notify({...task})

  // 1. 取分片任务：服务端算好分片大小 / 分片数，并逐个签好预签名URL
  const mission = await getSliceInfo({
    originalName: file.name,
    fileSize: file.size,
    categoryId,
    // 封面暂缺：后端用 Map.of 组装缓存，value 为 null 会抛 NPE，因此传空串占位
    // （待「封面由前端截帧 / 由后端转码生成」方案确定后替换）
    cover: '',
  })

  const parts = mission.videoReturnInfoVOList ?? []
  const partSize = mission.partSize
  const totalParts = mission.totalParts

  if (parts.length === 0 || !partSize || !totalParts) {
    throw new Error('分片任务信息不完整，请稍后重试')
  }
  if (parts.length !== totalParts) {
    throw new Error('分片任务信息不一致，请稍后重试')
  }
  // 防御：后端分片计划必须满足「前 n-1 片等长、最后一片补余数」的不变式
  // （对象存储要求除最后一片外每片不小于 5MB，不满足说明分片大小不可信，直接中止而不是发出错误分片）
  if (!(partSize * (totalParts - 1) < file.size && file.size <= partSize * totalParts)) {
    throw new Error('分片大小异常，请稍后重试')
  }

  task.totalParts = totalParts
  notify({...task})

  // 各分片已上传字节（含单片内的实时进度），用于计算整体百分比
  const partBytes = new Map<number, number>()

  const syncProgress = () => {
    let uploaded = 0
    for (const value of partBytes.values()) {
      uploaded += value
    }
    // 上限 99%：给「分片已传完但仍在逐片上报」的收尾阶段留一点空间，
    // 全部上报结束后再置为 100%（避免进度条先满、状态却还显示上传中）
    const percent = Math.min(99, Math.floor((uploaded / file.size) * 100))
    if (percent !== task.percent) {
      task.percent = percent
      notify({...task})
    }
  }

  /**
   * 上报串行锁
   * 服务端按「上报次数」累加完成分片数，并发上报可能让完成数超过分片数而错过合并时机，
   * 因此把上报排成队列，保证同一时刻只有一个 slice/upload 请求
   */
  let reportLock: Promise<unknown> = Promise.resolve()

  const reportPart = async (part: VideoSlicePartVO, eTag: string): Promise<void> => {
    const send = async () => {
      await withRetry(
          () =>
              reportSliceCompleted({
                filename: part.filename,
                uploadId: mission.uploadId,
                partNumber: part.partNumber,
                eTag,
              }),
          REPORT_RETRY_TIMES,
      )
    }

    // 无论前一次上报成功还是失败都继续执行本次上报（失败会抛给本次分片的调用方）
    const current = reportLock.then(send, send)
    reportLock = current.catch(() => undefined)
    await current

    task.finishedParts += 1
    notify({...task})
  }

  let firstError: unknown = null

  await runWithConcurrency(parts, PART_CONCURRENCY, async (part) => {
    if (firstError) {
      return
    }
    try {
      // 切片规则：第 i 片 offset=(i-1)*partSize、size=min(partSize, fileSize-offset)
      const offset = (part.partNumber - 1) * partSize
      const size = Math.min(partSize, file.size - offset)
      const blob = file.slice(offset, offset + size)

      partBytes.set(part.partNumber, 0)

      const eTag = await withRetry(
          () =>
              putSliceToMinio(part.preSignedUrl, blob, {
                onProgress: (loadedBytes) => {
                  partBytes.set(part.partNumber, loadedBytes)
                  syncProgress()
                },
              }),
          PUT_RETRY_TIMES,
      )

      partBytes.set(part.partNumber, size)
      syncProgress()

      await reportPart(part, eTag)
    } catch (error) {
      // 记录首个错误并停止领取新分片：上报缺失会让服务端永远等不到合并
      firstError = firstError ?? error
    }
  })

  if (firstError) {
    throw firstError
  }

  task.percent = 100
  task.status = 'success'
  notify({...task})
}

/**
 * 批量上传视频（逐文件串行：避免多个文件同时进入服务端合并阶段）
 *
 * @param files   已通过选择器拿到的本地文件
 * @param options 目标分类与进度回调
 * @returns 成功与失败清单（单个文件失败不中断整批）
 */
export async function uploadVideos(
    files: File[],
    options: UploadVideosOptions,
): Promise<UploadSummary> {
  const summary: UploadSummary = {succeeded: [], failed: []}

  for (let index = 0; index < files.length; index += 1) {
    const file = files[index]
    if (!file) {
      continue
    }
    const notify = (task: UploadTaskProgress) => options.onProgress?.(task, index)

    try {
      const invalidReason = validateVideoFile(file)
      if (invalidReason) {
        throw new Error(invalidReason)
      }
      await uploadOneFile(file, options.categoryId, notify)
      summary.succeeded.push(file.name)
    } catch (error) {
      const reason = resolveUploadError(error)
      summary.failed.push({name: file.name, reason})
      notify({
        name: file.name,
        percent: 0,
        finishedParts: 0,
        totalParts: 0,
        status: 'failed',
        error: reason,
      })
    }
  }

  return summary
}
