package com.dotm.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.model.SysUserRole;

import java.util.List;

/**
 * @author dotm
 * @description 针对表【sys_user_role(用户和角色关联表)】的数据库操作Service
 * @createDate 2026-08-06 17:04:42
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 根据用户id绑定角色列表
     *
     * @param userId  用户id
     * @param roleIds 角色id列表
     */
    boolean bindRolesByUserId(Long userId, List<Long> roleIds);

    /**
     * 根据用户id解绑角色列表
     *
     * @param userId 用户id
     */
    boolean unBindRolesByUserId(Long userId);
}

