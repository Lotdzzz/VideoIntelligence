/**
 * 认证登录日志查询入参对象
 */
export interface SysAuthLogDTO {
  /** 登录账号 */
  username?: string

  /** 登录类型（login登录 logout退出） */
  loginType?: string

  /** 状态（1成功 0失败） */
  status?: number

  /** 登录IP */
  ipAddress?: string

  /** IP归属地 */
  ipLocation?: string

  /** 登录时间-开始（格式 yyyy-MM-dd HH:mm:ss） */
  beginTime?: string

  /** 登录时间-结束（格式 yyyy-MM-dd HH:mm:ss） */
  endTime?: string
}
