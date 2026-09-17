/**
 * 用户信息（与后端 com.dotm.entity.vo.UserInfoVO 字段保持一致）
 */
export interface UserInfo {
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
}
