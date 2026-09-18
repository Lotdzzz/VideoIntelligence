/**
 * 视频（资源文件）列表项（与后端 com.vi.entity.vo.ViFileVO 字段保持一致）
 */
export interface FileVO {
  /** 文件ID */
  id: number

  /** 所属用户ID */
  userId: number

  /** 原始文件名 */
  originalName: string

  /** MinIO对象名称（用于后续绑定预签名 URL 播放 / 下载） */
  objectName: string

  /** MinIO Bucket */
  bucketName: string

  /** 文件类型，如 video/audio/image/document */
  fileType: string | null

  /** 文件扩展名，如 mp4/pdf/jpg */
  fileExt: string | null

  /** MIME类型，如 video/mp4 */
  contentType: string | null

  /** 封面图URL，适用于视频/音频/文档等（后端未生成时为 null） */
  cover: string | null

  /** 文件大小，单位：字节 */
  fileSize: number | null

  /** 文件分类ID */
  categoryId: number | null

  /** 文件状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除 */
  status: number | null

  /** 文件MD5，用于后续秒传/去重 */
  md5: string | null

  /** 上传完成时间 */
  uploadTime: string | null

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string
}
