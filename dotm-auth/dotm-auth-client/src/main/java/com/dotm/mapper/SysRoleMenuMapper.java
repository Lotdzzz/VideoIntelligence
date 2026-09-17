package com.dotm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dotm.entity.model.system.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dotm
 * @description 针对表【sys_role_menu(角色和菜单关联表)】的数据库操作Mapper
 * @createDate 2026-08-06 17:04:42
 * @Entity com.videogpt.entity.model.SysRoleMenu
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 绑定角色和菜单的关系
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean bindMenusByRoleId(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 清除本角色下所有绑定的菜单列表
     *
     * @param roleId  角色ID
     * @return 是否成功
     */
    boolean unBindMenusByRoleId(Long roleId);
}

