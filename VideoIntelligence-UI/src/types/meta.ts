/**
 * 路由 meta 信息视图对象
 * 对应后端 MetaVO
 */
export interface MetaVO {
  /** 标题 */
  title: string

  /** 图标 */
  icon: string

  /** 是否缓存 */
  keepAlive: boolean

  /** 权限 */
  perms: string

  /** 外链 */
  frame: boolean
}