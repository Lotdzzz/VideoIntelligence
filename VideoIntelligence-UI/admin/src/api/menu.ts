import request from '@/utils/request';
import { routesConstants } from '@/constants/routesConstants';
import type { PageResult } from '@/types/result';
import type { SysMenuDTO } from '@/types/user/sysMenuDTO';
import type { SysMenuVO } from '@/types/user/sysMenuVO';

// 分页查询菜单（支持按名称/状态等条件搜索）
export function listMenus(pageNum: number, pageSize: number, dto?: SysMenuDTO) {
  return request<PageResult<SysMenuVO>>({
    url: routesConstants.MENU_LIST,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      // 未填写时传 null，便于后端条件查询处理空条件
      menuName: dto?.menuName || null,
      status: dto?.status || null,
    },
  });
}

// 根据菜单ID查询菜单
export function getMenuById(menuId: number) {
  return request<SysMenuVO>({
    url: `${routesConstants.MENU_GET_BY_ID}/${menuId}`,
    method: 'get',
  });
}

// 修改菜单
export function updateMenu(data: SysMenuDTO) {
  return request<void>({
    url: routesConstants.MENU_UPDATE,
    method: 'put',
    data,
  });
}

// 删除菜单
export function deleteMenu(menuId: number) {
  return request<void>({
    url: `${routesConstants.MENU_DELETE}/${menuId}`,
    method: 'delete',
  });
}