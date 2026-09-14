import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { SystemMonitorInfo } from '@/types/monitor/system';

// 获取系统信息 运行时内存
export function getSystemInfo() {
  return request<SystemMonitorInfo>({
    url: routesConstants.SYSTEM_MONITOR_INFO,
    method: 'get',
  });
}
