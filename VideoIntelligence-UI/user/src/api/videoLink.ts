import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type { APIURLsInfoVO } from '@/types/file/apiURLsInfoVO'

/** 解析外部视频链接，返回可选择的视频条目 */
export function resolveVideoLink(url: string, signal?: AbortSignal) {
  return request.post<unknown, APIURLsInfoVO[]>(
    routesConstants.FILE_LINK_RECEIVE,
    { url },
    { signal },
  )
}
