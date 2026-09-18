/**
 * 视频（资源文件）查询入参
 * （与后端 com.vi.entity.dto.ViFileDTO 字段保持一致，仅保留列表查询用得到的条件）
 */
export interface FileDTO {
  /** 文件ID（修改时必填，列表查询不使用） */
  id?: number

  /** 所属用户ID（必传当前登录用户ID，避免查到其他用户的文件；传 null 表示不按用户过滤） */
  userId?: number | null

  /** 文件分类ID（为空 / null 表示不按分类过滤，即「全部视频」） */
  categoryId?: number | null

  /** 原始文件名（模糊查询） */
  originalName?: string | null

  /** 文件类型，如 video/audio/image/document */
  fileType?: string | null

  /** 文件扩展名，如 mp4/pdf/jpg */
  fileExt?: string | null

  /** 文件状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除 */
  status?: number | null

  /** 创建时间-开始（yyyy-MM-dd HH:mm:ss） */
  beginTime?: string | null

  /** 创建时间-结束（yyyy-MM-dd HH:mm:ss） */
  endTime?: string | null
}
