package com.dotm.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author dotm
 * 权限列表 角色列表 路由树
 */
@Data
public class ValidVO {
    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 路由
     */
    private List<RouterVO> routers;
}
