package com.dotm.entity.vo;

import lombok.Data;

/**
 * @author dotm
 */
@Data
public class MetaVO {
    /**
     * 标题
     */
    private String title;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否缓存
     */
    private Boolean keepAlive;

    /**
     * 权限
     */
    private String perms;

    /**
     * 外链
     */
    private Boolean frame;
}
