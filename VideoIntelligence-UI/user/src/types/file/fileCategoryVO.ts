/**
 * 资源文件（视频）分类（与后端 com.vi.entity.vo.ViFileCategoryVO 字段保持一致）
 */
export interface FileCategoryVO {
  /** 分类ID */
  id: number

  /** 所属用户ID */
  userId: number

  /** 分类名称 */
  categoryName: string

  /** 排序（后端未设置排序时可能为空） */
  sort: number | null

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string
}
