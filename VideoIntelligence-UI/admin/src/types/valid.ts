import type { RouterVO } from './routers'

/**
 * 权限列表 角色列表 路由树
 * 对应后端 ValidVO
 */
export interface ValidVO {
  /** 权限列表 */
  permissions: string[]

  /** 角色列表 */
  roles: string[]

  /** 路由 */
  routers: RouterVO[]
}