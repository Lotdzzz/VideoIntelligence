import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type {
  PartUploadCompleteDTO,
  VideoSliceInfoDTO,
  VideoSliceMissionVO,
} from '@/types/file/videoSlice'

/**
 * 获取视频分片上传任务
 *
 * 后端在该接口内完成：扩展名白名单校验 → 计算分片大小与分片数 → 创建对象存储分片上传任务
 * → 逐个分片生成预签名URL → 把上传进度写入缓存
 *
 * 该接口需要携带 token（网关 /file/** 不在游客白名单内），token 失效时后端返回 401，
 * 由 utils/request.ts 统一清除本地凭证并把错误透传给调用方
 *
 * @param data 视频信息（原文件名 / 文件大小 / 分类ID / 封面占位）
 * @returns 分片上传任务（uploadId / 分片大小 / 总分片数 / 各分片预签名URL）
 */
export function getSliceInfo(data: VideoSliceInfoDTO) {
  return request.post<unknown, VideoSliceMissionVO>(routesConstants.FILE_PREDESIGN_SLICE_INFO, data)
}

/**
 * 上报某个分片上传完成
 *
 * 分片字节由前端直传对象存储，服务端通过该接口累加完成分片数；
 * 当完成数与总分片数相等时，服务端会合并分片并把文件信息写入资源库（vi_file）
 *
 * 注意：必须携带直传响应头里的 ETag，且同一文件的分片上报要串行进行（服务端按上报次数累加）
 *
 * @param data 分片完成信息（对象名 / uploadId / 分片编号 / ETag）
 * @returns 服务端返回的成功文案（合并未完成时也会返回成功）
 */
export function reportSliceCompleted(data: PartUploadCompleteDTO) {
  return request.post<unknown, string>(routesConstants.FILE_PREDESIGN_SLICE_COMPLETE, data)
}
