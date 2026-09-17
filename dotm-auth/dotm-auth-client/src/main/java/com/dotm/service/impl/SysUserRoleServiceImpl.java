package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.entity.model.system.SysUserRole;
import com.dotm.mapper.SysUserRoleMapper;
import com.dotm.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author dotm
* 针对表【sys_user_role(用户和角色关联表)】的数据库操作Service实现
* 创建时间：2026-08-06 17:04:42
*/
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
    implements SysUserRoleService{

    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 根据用户id绑定角色列表
     *
     * @param userId  用户id
     * @param roleIds 角色id列表
     */
    @Override
    public boolean bindRolesByUserId(Long userId, List<Long> roleIds) {
        return sysUserRoleMapper.bindRolesByUserId(userId, roleIds);
    }

    /**
     * 根据用户id解绑角色列表
     *
     * @param userId 用户id
     */
    @Override
    public boolean unBindRolesByUserId(Long userId) {
        // 根据用户id解绑角色列表
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        return remove(queryWrapper);
    }
}


