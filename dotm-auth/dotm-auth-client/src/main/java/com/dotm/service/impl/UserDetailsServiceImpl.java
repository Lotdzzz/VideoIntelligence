package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dotm.entity.model.SysMenu;
import com.dotm.entity.model.SysRole;
import com.dotm.entity.model.SysUser;
import com.framework.exception.user.UserNotExistsException;
import com.framework.model.LoginBodyAuthentication;
import com.dotm.service.SysRoleService;
import com.dotm.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;

    private final SysRoleService sysRoleService;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UserNotExistsException {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username);
        SysUser sysUser = sysUserService.getOne(queryWrapper);

        if (sysUser == null) {
            throw new UserNotExistsException(null);
        }
        //查询用户的平铺menu列表集合 多对多联表查询
        List<SysMenu> sysMenus = sysUserService.selectMenusByUserId(sysUser.getUserId());

        //获取角色列表
        List<SysRole> sysRoles = sysRoleService.selectRolesByUserId(sysUser.getUserId());

        //获取菜单ids方便拼装路由树
        List<Long> menuIds = sysMenus.stream().map(SysMenu::getMenuId).toList();

        //获取权限字符集合
        List<String> authorities = sysMenus.stream().map(SysMenu::getPerms).filter(StringUtils::hasText).collect(Collectors.toList());

        //获取角色字符集合 因为判断的时候security会自动在角色前加一个前缀ROLE_所以我们也要加上ROLE_保持一致
        List<String> roles = sysRoles.stream().map(SysRole::getRoleKey).filter(StringUtils::hasText).toList();
        List<String> prefixRoles = roles.stream().map(role -> "ROLE_" + role).collect(Collectors.toList());

        LoginBodyAuthentication loginBody = new LoginBodyAuthentication();
        BeanUtils.copyProperties(sysUser, loginBody);
        loginBody.setUsername(sysUser.getUserName());

        //设置权限 这样返回的authentication就有getAuthorities了
        loginBody.setPermissions(authorities);

        //设置角色 可以在@PreAuthorize(hasRole)判断有没有角色
        loginBody.setRoleList(prefixRoles);

        //设置菜单ids 方便前端拼装路由树
        loginBody.setMenuIds(menuIds);

        return loginBody;
    }
}
