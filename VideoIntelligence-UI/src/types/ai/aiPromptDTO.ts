/**
 * AI提示词新增/修改入参对象
 */
export interface AiPromptDTO {
  /** 主键（修改时必填） */
  id?: number

  /** 提示词名称 */
  name?: string

  /** 提示词内容 */
  content?: string

  /** 模型，如 gpt-4o */
  model?: string

  /** 模型参数，如 {"temperature":0.7} */
  modelParams?: string

  /** 版本号 */
  version?: number

  /** 状态：0-启用，1-禁用 */
  status?: number

  /** key字段：表示此提示词的唯一标识 */
  key?: string
}
