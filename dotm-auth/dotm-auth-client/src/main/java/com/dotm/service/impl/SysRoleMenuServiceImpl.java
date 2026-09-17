package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.entity.model.system.SysRoleMenu;
import com.dotm.mapper.SysRoleMenuMapper;
import com.dotm.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author dotm
 * 针对表【sys_role_menu(角色和菜单关联表)】的数据库操作Service实现
 * 创建时间：2026-08-06 17:04:42
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu>
        implements SysRoleMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 查询该角色对应的菜单id列表
     */
    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenus = list(queryWrapper);
        if (!roleMenus.isEmpty()) {
            return roleMenus.stream().map(SysRoleMenu::getMenuId).toList();
        }
        return List.of();
    }

    /**
     * 绑定角色和菜单的关系
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean bindMenusByRoleId(Long roleId, List<Long> menuIds) {
        // 实现绑定角色和菜单的关系
        return sysRoleMenuMapper.bindMenusByRoleId(roleId, menuIds);
    }

    /**
     * 解绑角色和菜单的关系
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    @Override
    public boolean unBindMenusByRoleId(Long roleId) {
        return sysRoleMenuMapper.unBindMenusByRoleId(roleId);
    }
}


