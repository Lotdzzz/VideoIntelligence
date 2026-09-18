/**
 * 资源文件（视频）分类查询 / 新增 / 修改入参
 * （与后端 com.vi.entity.dto.ViFileCategoryDTO 字段保持一致）
 */
export interface FileCategoryDTO {
  /** 分类ID（修改时必填） */
  id?: number

  /** 所属用户ID（新增 / 修改时后端依赖它做同名校验，必传当前登录用户ID） */
  userId?: number

  /** 分类名称（模糊查询 / 新增 / 修改） */
  categoryName?: string

  /** 排序 */
  sort?: number

  /** 创建时间-开始（yyyy-MM-dd HH:mm:ss） */
  beginTime?: string

  /** 创建时间-结束（yyyy-MM-dd HH:mm:ss） */
  endTime?: string
}
