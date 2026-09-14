import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { PageResult } from '@/types/result';
import type { AiPromptDTO } from '@/types/ai/aiPromptDTO';
import type { AiPromptVO } from '@/types/ai/aiPromptVO';

// 分页查询AI提示词（支持按名称/状态等条件搜索）
export function listAiPrompts(pageNum: number, pageSize: number, dto?: AiPromptDTO) {
  return request<PageResult<AiPromptVO>>({
    url: routesConstants.AI_PROMPT_LIST,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      // 未填写时传 null，便于后端条件查询处理空条件
      name: dto?.name || null,
      key: dto?.key || null,
      status: dto?.status === undefined ? null : dto.status,
    },
  });
}

// 根据ID查询AI提示词
export function getAiPromptById(id: number) {
  return request<AiPromptVO>({
    url: `${routesConstants.AI_PROMPT_GET_BY_ID}/${id}`,
    method: 'get',
  });
}

// 新增AI提示词
export function addAiPrompt(data: AiPromptDTO) {
  return request<void>({
    url: routesConstants.AI_PROMPT_ADD,
    method: 'post',
    data,
  });
}

// 修改AI提示词
export function updateAiPrompt(data: AiPromptDTO) {
  return request<void>({
    url: routesConstants.AI_PROMPT_UPDATE,
    method: 'put',
    data,
  });
}

// 删除AI提示词
export function deleteAiPrompt(id: number) {
  return request<void>({
    url: `${routesConstants.AI_PROMPT_DELETE}/${id}`,
    method: 'delete',
  });
}

// 批量删除AI提示词
export function batchDeleteAiPrompts(ids: number[]) {
  return request<void>({
    url: routesConstants.AI_PROMPT_BATCH_DELETE,
    method: 'delete',
    data: ids,
  });
}
