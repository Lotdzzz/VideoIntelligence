/**
 * AI提示词视图对象（查询返回）
 */
export interface AiPromptVO {
  /** 主键 */
  id: number

  /** 提示词名称 */
  name: string

  /** 提示词内容 */
  content: string

  /** 模型，如 gpt-4o */
  model: string

  /** 模型参数，如 {"temperature":0.7} */
  modelParams: string

  /** 版本号 */
  version: number

  /** 状态：0-启用，1-禁用 */
  status: number

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** key字段：表示此提示词的唯一标识 */
  key?: string
}
