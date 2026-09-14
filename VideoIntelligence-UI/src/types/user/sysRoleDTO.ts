/**
 * 角色新增/修改入参对象
 */
export interface SysRoleDTO {
  /** 角色ID（修改时必填） */
  roleId?: number

  /** 角色名称 */
  roleName?: string

  /** 角色权限字符串 */
  roleKey?: string

  /** 显示顺序 */
  roleSort?: number

  /** 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限） */
  dataScope?: string

  /** 菜单树选择项是否关联显示 */
  menuCheckStrictly?: number

  /** 部门树选择项是否关联显示 */
  deptCheckStrictly?: number

  /** 角色状态（0正常 1停用） */
  status?: string

  /** 备注 */
  remark?: string

  /** 菜单权限列表 */
  menuIds?: number[]
}