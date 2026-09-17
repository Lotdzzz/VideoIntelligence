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
