package com.framework.service.impl;

import com.framework.model.LoginBodyAuthentication;
import com.framework.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author dotm
 */
@Service("ss")
public class AuthoritiesService {

    /**
     * 判断接口是否有该权限，如果是管理员直接放行
     *
     * @param authority 权限
     * @return 结果
     */
    public boolean hasPermission(String authority) {
        //获取用户指纹
        LoginBodyAuthentication sysUser = SecurityUtils.getLoginUser();

        //管理员直接放行
        if (SecurityUtils.isAdmin(Objects.requireNonNull(sysUser).getUserId())) {
            return true;
        }

        //开始验证权限
        for (String auth : sysUser.getPermissions()) {
            if (auth.equals(authority)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证用户角色 同理
     *
     * @param authority 角色
     * @return 结果
     */
    public boolean hasRole(String authority) {
        LoginBodyAuthentication sysUser = SecurityUtils.getLoginUser();

        if (SecurityUtils.isAdmin(Objects.requireNonNull(sysUser).getUserId())) {
            return true;
        }

        for (String auth : sysUser.getRoleList()) {
            if (auth.equals(authority)) {
                return true;
            }
        }

        return false;
    }
}
