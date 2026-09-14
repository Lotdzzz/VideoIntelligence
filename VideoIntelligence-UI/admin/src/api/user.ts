import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { PageResult } from '@/types/result';
import type { SysUserDTO } from '@/types/user/sysUserDTO';
import type { SysUserVO } from '@/types/user/sysUserVO';

// 分页查询用户（支持按账号/昵称/状态等条件搜索）
export function listUsers(pageNum: number, pageSize: number, dto?: SysUserDTO) {
  return request<PageResult<SysUserVO>>({
    url: routesConstants.USER_LIST,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      // 未填写时传 null，便于后端条件查询处理空条件
      userName: dto?.userName || null,
      nickName: dto?.nickName || null,
      status: dto?.status || null,
    },
  });
}

// 根据用户ID查询用户
export function getUserById(userId: number) {
  return request<SysUserVO>({
    url: `${routesConstants.USER_GET_BY_ID}/${userId}`,
    method: 'get',
  });
}

// 根据用户ID查询本人用户（归属校验）
export function getUserByOwner(userId: number) {
  return request<SysUserVO>({
    url: `${routesConstants.USER_GET_BY_OWNER}/${userId}`,
    method: 'get',
  });
}

// 新增用户
export function addUser(data: SysUserDTO) {
  return request<void>({
    url: routesConstants.USER_ADD,
    method: 'post',
    data,
  });
}

// 修改用户
export function updateUser(data: SysUserDTO) {
  return request<void>({
    url: routesConstants.USER_UPDATE,
    method: 'put',
    data,
  });
}

// 修改本人用户（归属校验）
export function updateUserByOwner(data: SysUserDTO) {
  return request<void>({
    url: routesConstants.USER_UPDATE_BY_OWNER,
    method: 'put',
    data,
  });
}

// 删除用户
export function deleteUser(userId: number) {
  return request<void>({
    url: `${routesConstants.USER_DELETE}/${userId}`,
    method: 'delete',
  });
}

// 批量删除用户
export function batchDeleteUsers(userIds: number[]) {
  return request<void>({
    url: routesConstants.USER_BATCH_DELETE,
    method: 'delete',
    data: userIds,
  });
}