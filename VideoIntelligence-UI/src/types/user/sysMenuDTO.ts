/**
 * 菜单新增/修改入参对象
 */
export interface SysMenuDTO {
  /** 菜单ID（修改时必填） */
  menuId?: number

  /** 菜单名称 */
  menuName?: string

  /** 父菜单ID */
  parentId?: number

  /** 显示顺序 */
  orderNum?: number

  /** 路由地址 */
  path?: string

  /** 组件路径 */
  component?: string

  /** 路由参数 */
  query?: string

  /** 路由名称 */
  routeName?: string

  /** 是否为外链（0是 1否） */
  isFrame?: number

  /** 是否缓存（0缓存 1不缓存） */
  isCache?: number

  /** 菜单类型（M目录 C菜单 F按钮） */
  menuType?: string

  /** 菜单状态（0显示 1隐藏） */
  visible?: string

  /** 菜单状态（0正常 1停用） */
  status?: string

  /** 权限标识 */
  perms?: string

  /** 菜单图标 */
  icon?: string

  /** 备注 */
  remark?: string
}