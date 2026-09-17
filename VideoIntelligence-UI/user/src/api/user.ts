import request from '@/utils/request'
import { routesConstants } from '@/constants/routesConstants'
import type { UserInfo } from '@/types/user/userInfo'

/**
 * 获取当前登录用户信息
 * 该接口需要携带 token，token 失效时后端返回 401，
 * 由 utils/request.ts 统一清除本地凭证并把错误透传给调用方
 *
 * @returns 当前登录用户的基本信息（含昵称与头像）
 */
export function getUserInfo() {
    return request.get<unknown, UserInfo>(routesConstants.GET_USER_INFO)
}
