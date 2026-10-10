import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type { AnalysisResult } from '@/types/file/analysisResult'

/** 查询当前用户某个视频的分析结果；结果尚未生成时后端返回 null。 */
export function getAnalysisResult(userId: number, taskId: number) {
  return request.get<unknown, AnalysisResult | null>(
    `${routesConstants.ANALYSIS_RESULT}/${userId}/${taskId}`,
  )
}