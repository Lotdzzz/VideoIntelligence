import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { PageResult } from '@/types/result';
import type { SysAuthLogDTO } from '@/types/monitor/sysAuthLogDTO';
import type { SysAuthLogVO } from '@/types/monitor/sysAuthLogVO';

// 分页查询认证登录日志（支持按账号/登录类型/状态/IP/时间范围等条件搜索）
export function listAuthLogs(pageNum: number, pageSize: number, dto?: SysAuthLogDTO) {
  return request<PageResult<SysAuthLogVO>>({
    url: routesConstants.AUTH_LOG_LIST,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      // 未填写时传 null，便于后端条件查询处理空条件
      username: dto?.username || null,
      loginType: dto?.loginType || null,
      // 状态使用 ?? 而非 ||，避免 0（失败）被误判为空；同时过滤掉 NaN 等非法数字
      status: typeof dto?.status === 'number' && Number.isFinite(dto.status) ? dto.status : null,
      ipAddress: dto?.ipAddress || null,
      ipLocation: dto?.ipLocation || null,
      beginTime: dto?.beginTime || null,
      endTime: dto?.endTime || null,
    },
  });
}

// 根据日志ID查询认证登录日志
export function getAuthLogById(id: number) {
  return request<SysAuthLogVO>({
    url: `${routesConstants.AUTH_LOG_GET_BY_ID}/${id}`,
    method: 'get',
  });
}

// 删除认证登录日志
export function deleteAuthLog(id: number) {
  return request<void>({
    url: `${routesConstants.AUTH_LOG_DELETE}/${id}`,
    method: 'delete',
  });
}

// 批量删除认证登录日志
export function batchDeleteAuthLogs(ids: number[]) {
  return request<void>({
    url: routesConstants.AUTH_LOG_BATCH_DELETE,
    method: 'delete',
    data: ids,
  });
}
