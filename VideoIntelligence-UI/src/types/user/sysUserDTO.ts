/**
 * 用户新增/修改入参对象（可传入密码）
 */
export interface SysUserDTO {
  /** 用户ID（修改时必填） */
  userId?: number

  /** 用户账号 */
  userName?: string

  /** 用户昵称 */
  nickName?: string

  /** 用户邮箱 */
  email?: string

  /** 头像地址 */
  avatar?: string

  /** 密码（新增时必填，修改时选填） */
  password?: string

  /** 账号状态（0正常 1停用） */
  status?: string

  /** 备注 */
  remark?: string

  /** 角色列表 */
  roleIds?: number[]
}