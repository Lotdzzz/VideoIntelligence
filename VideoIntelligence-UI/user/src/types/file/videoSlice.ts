/**
 * 视频分片上传相关类型
 * 与后端保持一致：
 * - com.vi.entity.dto.ViFileDTO            （slice/info 入参）
 * - com.vi.entity.vo.VideoSliceMissionVo  （slice/info 出参）
 * - com.vi.entity.vo.VideoReturnInfoVO    （单个分片的预签名信息）
 * - com.vi.entity.dto.PartUploadCompleteDTO（slice/upload 入参）
 */

/**
 * 分片上传任务入参（后端 ViFileDTO 的上传相关字段）
 * 服务端会据此做白名单校验 → 计算分片计划 → 创建 MinIO 分片任务 → 生成预签名URL
 */
export interface VideoSliceInfoDTO {
  /** 原始文件名（含扩展名），后端按扩展名做白名单校验 */
  originalName: string

  /** 文件大小，单位：字节；后端据此计算分片大小与分片数 */
  fileSize: number

  /**
   * 所属视频分类ID
   * 后端落库时会用 Long.parseLong(...) 解析该字段，因此必须传有效数字（不能为空）
   */
  categoryId: number

  /**
   * 封面地址（可暂时传空串占位）
   * 后端当前用 Map.of(...) 组装 Redis Hash，value 为 null 会抛 NPE，因此不能传 null
   */
  cover: string
}

/** 单个分片的预签名信息（后端 VideoReturnInfoVO） */
export interface VideoSlicePartVO {
  /** 分片上传的预签名URL（前端用它直传对象存储，不经过网关） */
  preSignedUrl: string

  /** 分片编号，从 1 开始（对象存储规范：分片号必须是 1 ~ 10000） */
  partNumber: number

  /** 对象存储中的对象名（= 后端生成的「UUID_原文件名」），上报分片完成时必须回传 */
  filename: string

  /** 后端预留字段（当前固定为 null） */
  videoId: number | null
}

/** 分片上传任务信息（后端 VideoSliceMissionVo） */
export interface VideoSliceMissionVO {
  /** 各分片的预签名信息（按 partNumber 升序） */
  videoReturnInfoVOList: VideoSlicePartVO[]

  /** 对象存储的分片上传任务ID，合并与上报时都要用到 */
  uploadId: string

  /**
   * 每片的字节数（后端分片计划中的 chunkSize）
   * 切片规则：第 i 片 offset=(i-1)*partSize、size=min(partSize, fileSize-offset)，最后一片会更小
   */
  partSize: number

  /** 总分片数 */
  totalParts: number
}

/** 分片上传完成上报入参（后端 PartUploadCompleteDTO） */
export interface PartUploadCompleteDTO {
  /** 对象名（= 分片信息里的 filename） */
  filename: string

  /** 分片上传任务ID（= slice/info 返回的 uploadId） */
  uploadId: string

  /** 分片编号（与直传时使用的 partNumber 保持一致） */
  partNumber: number

  /** 对象存储返回的 ETag（从直传响应头读取，原样回传） */
  eTag: string
}
