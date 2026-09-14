package com.dotm.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author dotm
 * 路由信息视图对象
 */
@Data
public class RouterVO {
    /**
     * 路由名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 重定向
     */
    private String redirect;

    /**
     * 组件
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 是否一直显示根菜单
     */
    private Boolean alwaysShow;

    /**
     * meta信息
     */
    private MetaVO meta;

    /**
     * 子路由
     */
    private List<RouterVO> children;
}