import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { LoginBody } from '@/types/LoginBody';
import type { SysUserInfo } from '@/types/user/userInfo';
import type { ValidVO } from '@/types/valid';

// 登录方法
export function login(data: LoginBody) {
  return request({
    url: routesConstants.LOGIN,
    headers: {
      //自定义config.headers.isToken属性，告诉请求拦截器：
      //这个请求不需要在请求头里加 Token
      isToken: false,
    },
    method: 'post',
    data: data,
  });
}

//获取用户信息
export function getUserInfo() {
  return request<SysUserInfo>({
    url: routesConstants.GET_USER_INFO,
    method: 'get',
  });
}

//获取路由信息（权限列表 角色列表 路由树）
export function getValid() {
  return request<ValidVO>({
    url: routesConstants.GET_VALID,
    method: 'get',
  });
}

// 退出登录
export function logout() {
  return request<void>({
    url: routesConstants.LOGOUT,
    method: 'get',
  });
}
