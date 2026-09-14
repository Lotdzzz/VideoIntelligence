/**
 * 用户信息
 */
export interface SysUser {
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

  /** 删除标志（0代表存在 2代表删除） */
  delFlag: string

  /** 最后登录IP */
  loginIp: string

  /** 最后登录时间 */
  loginDate: string

  /** 密码最后更新时间 */
  pwdUpdateDate: string

  /** 创建者 */
  createBy: string

  /** 创建时间 */
  createTime: string

  /** 更新者 */
  updateBy: string

  /** 更新时间 */
  updateTime: string

  /** 备注 */
  remark: string
}