import type { MetaVO } from '@/types/meta'

/**
 * 路由信息视图对象
 * 对应后端 RouterVO
 */
export interface RouterVO {
  /** 路由名称 */
  name: string

  /** 路由路径 */
  path: string

  /** 是否隐藏 */
  hidden: boolean

  /** 重定向 */
  redirect: string

  /** 组件 */
  component: string

  /** 路由参数 */
  query: string

  /** 是否一直显示根菜单 */
  alwaysShow: boolean

  /** meta信息 */
  meta: MetaVO

  /** 子路由 */
  children: RouterVO[]
}