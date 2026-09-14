/**
 * 用户认证登录日志信息视图对象（查询返回）
 */
export interface SysAuthLogVO {
  /** 主键ID */
  id: number

  /** 登录账号 */
  username: string

  /** 登录类型（login登录 logout退出） */
  loginType: string

  /** 状态（1成功 0失败） */
  status: number

  /** 失败原因 */
  failReason: string

  /** 登录IP */
  ipAddress: string

  /** IP归属地 */
  ipLocation: string

  /** 浏览器UA */
  userAgent: string

  /** JWT Token唯一标识 */
  uuidId: string

  /** 登录时间 */
  loginTime: string

  /** 退出时间 */
  logoutTime: string

  /** 创建时间 */
  createTime: string
}
