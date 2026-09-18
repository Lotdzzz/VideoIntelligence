import { routesConstants } from '@/constants/routesConstants';

/**
 * 将后端返回的文件字段（头像等）解析为可直接访问的地址。
 *
 * - 后端上传接口已接入阿里云 OSS，返回的是完整访问地址
 *   （形如 https://<bucket>.<endpoint>/<uuid>-<原文件名>.jpg），此时直接透传；
 * - 兼容历史数据：仅存文件名（不含目录）时，静态文件目录已映射到网关 /upload，
 *   前端通过 vite 代理访问，拼接相对路径即可；
 * - 同时兼容协议相对地址（//cdn.xxx.com/a.jpg）与内联地址（data:/blob:）。
 *
 * @param fileName 后端返回的完整地址或历史文件名
 * @returns 可直接用于 img / el-avatar 的 src，无值返回空串
 */
export function resolveFileUrl(fileName?: string | null): string {
  if (!fileName) return '';
  const value = String(fileName).trim();
  if (!value) return '';
  // 带协议的完整地址（http://、https://）、协议相对地址（//oss.xxx.com/...）、
  // 内联地址（data:/blob:）原样返回，避免被重复拼接成非法地址
  if (/^([a-z][a-z0-9+.-]*:)?\/\//i.test(value) || /^(data|blob):/i.test(value)) {
    return value;
  }
  // 历史数据：仅存文件名，拼接网关静态资源前缀（vite 侧代理 /upload）
  const name = value.replace(/^\/+/, '');
  return `${routesConstants.UPLOAD}/${name}`;
}
