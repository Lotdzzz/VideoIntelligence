import request from '@/utils/request'
import {routesConstants} from "@/constants/routesConstants.ts";

/**
 * 退出登录功能 删除redis中的用户信息
 */
export function logout() {
    return request.get(routesConstants.LOGOUT);
}