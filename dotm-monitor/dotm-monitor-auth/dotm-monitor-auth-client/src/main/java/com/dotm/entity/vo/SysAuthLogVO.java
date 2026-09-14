package com.dotm.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 返回给前端的用户认证登录日志VO
 */
@Data
public class SysAuthLogVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录类型 LOGIN登录 LOGOUT退出
     */
    private String loginType;

    /**
     * 状态 1成功 0失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 登录IP
     */
    private String ipAddress;

    /**
     * IP归属地
     */
    private String ipLocation;

    /**
     * 浏览器UA
     */
    private String userAgent;

    /**
     * JWT Token唯一标识
     */
    private String uuidId;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 退出时间
     */
    private LocalDateTime logoutTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
