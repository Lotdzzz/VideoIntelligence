/**
 * 用户信息视图对象（查询返回，不含密码）
 */
export interface SysUserVO {
  /** 用户ID */
  userId: number

  /** 用户账号 */
  userName: string

  /** 用户昵称 */
  nickName: string

  /** 用户邮箱 */
  email: string

  /** 头像地址 */
  avatar: string

  /** 账号状态（0正常 1停用） */
  status: string

  /** 最后登录IP */
  loginIp: string

  /** 最后登录时间 */
  loginDate: string

  /** 创建时间 */
  createTime: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string

  /** 角色列表 */
  roleIds?: number[]
}