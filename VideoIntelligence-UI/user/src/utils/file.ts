import { routesConstants } from '@/constants/routesConstants'

/**
 * 将后端返回的文件字段（头像等）解析为可访问的完整地址
 * - GitHub 等第三方登录返回的头像本身就是完整 http(s) 地址，直接返回，避免重复拼接
 * - 本地上传返回的是文件名，静态文件目录已映射到网关 /upload，
 *   前端通过 vite 代理转发，因此拼接相对路径即可
 *
 * @param fileName 后端返回的文件名或完整地址
 * @returns 可直接用于 img / el-avatar 的地址，无值返回空串
 */
export function resolveFileUrl(fileName?: string | null): string {
  if (!fileName) {
    return ''
  }
  if (/^https?:\/\//i.test(fileName)) {
    return fileName
  }
  const name = fileName.replace(/^\/+/, '')
  return `${routesConstants.UPLOAD}/${name}`
}

/**
 * 将文件大小（字节）格式化为可读文案
 * 沿用 1024 进制与 1 位小数，避免卡片上出现「12345678」这种原始字节数
 *
 * @param bytes 后端返回的 fileSize（可能为空）
 * @returns 形如 12.3 MB 的文案，无值时返回空串
 */
export function formatFileSize(bytes?: number | null): string {
  if (bytes === null || bytes === undefined || Number.isNaN(bytes) || bytes <= 0) {
    return ''
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex += 1
  }
  // 字节数不带小数（避免出现 512.0 B）
  const value = unitIndex === 0 ? String(size) : size.toFixed(1)
  return `${value} ${units[unitIndex]}`
}
