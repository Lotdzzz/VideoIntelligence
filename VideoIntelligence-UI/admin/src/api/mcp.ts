import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';

/**
 * 与 AI 对话（非流式，一次性返回完整结果）
 * 后端返回 Result<Object>，响应拦截器已解包出 data（即 AI 返回的完整文本）。
 *
 * @param message 用户消息
 */
export function chatWithAi(message: string) {
  return request<string>({
    url: routesConstants.MCP_CHAT,
    method: 'post',
    params: {
      prompt: message,
    },
  });
}
