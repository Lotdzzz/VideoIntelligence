import { routesConstants } from '@/constants/routesConstants';

/**
 * 将后端返回的文件名解析为可访问的完整地址。
 * 后端上传接口仅返回文件名（不含目录），静态文件目录已映射到网关 /upload，
 * 前端通过 vite 代理将 /upload 转发到网关，因此拼接相对路径即可。
 * 若传入的已是完整 http(s) 地址则直接返回，避免重复拼接。
 */
export function resolveFileUrl(fileName?: string | null): string {
  if (!fileName) return '';
  if (/^https?:\/\//i.test(fileName)) return fileName;
  const name = fileName.replace(/^\/+/, '');
  return `${routesConstants.UPLOAD}/${name}`;
}
