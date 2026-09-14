import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { PageResult } from '@/types/result';
import type { SysRoleDTO } from '@/types/user/sysRoleDTO';
import type { SysRoleVO } from '@/types/user/sysRoleVO';

// 分页查询角色（支持按名称/权限字符/状态等条件搜索）
export function listRoles(pageNum: number, pageSize: number, dto?: SysRoleDTO) {
  return request<PageResult<SysRoleVO>>({
    url: routesConstants.ROLE_LIST,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      // 未填写时传 null，便于后端条件查询处理空条件
      roleName: dto?.roleName || null,
      roleKey: dto?.roleKey || null,
      status: dto?.status || null,
    },
  });
}

// 根据角色ID查询角色
export function getRoleById(roleId: number) {
  return request<SysRoleVO>({
    url: `${routesConstants.ROLE_GET_BY_ID}/${roleId}`,
    method: 'get',
  });
}

// 新增角色
export function addRole(data: SysRoleDTO) {
  return request<void>({
    url: routesConstants.ROLE_ADD,
    method: 'post',
    data,
  });
}

// 修改角色
export function updateRole(data: SysRoleDTO) {
  return request<void>({
    url: routesConstants.ROLE_UPDATE,
    method: 'put',
    data,
  });
}

// 删除角色
export function deleteRole(roleId: number) {
  return request<void>({
    url: `${routesConstants.ROLE_DELETE}/${roleId}`,
    method: 'delete',
  });
}

// 批量删除角色
export function batchDeleteRoles(roleIds: number[]) {
  return request<void>({
    url: routesConstants.ROLE_BATCH_DELETE,
    method: 'delete',
    data: roleIds,
  });
}

// 查询所有角色列表
export function listAllRoles() {
  return request<SysRoleVO[]>({
    url: routesConstants.ROLE_LIST_ALL,
    method: 'get',
  });
}