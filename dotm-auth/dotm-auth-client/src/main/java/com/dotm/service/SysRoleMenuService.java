package com.dotm.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.model.system.SysRoleMenu;

import java.util.List;

/**
 * @author dotm
 * @description 针对表【sys_role_menu(角色和菜单关联表)】的数据库操作Service
 * @createDate 2026-08-06 17:04:42
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 查询该角色对应的菜单id列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 绑定角色和菜单的关系
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean bindMenusByRoleId(Long roleId, List<Long> menuIds);

    /**
     * 解绑角色和菜单的关系
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean unBindMenusByRoleId(Long roleId);
}

