package com.dotm.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 */
@Data
public class AuthLogDTO {
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
     * 设备指纹
     */
    private String userAgent;

    /**
     * 登录IP
     */
    private String ipAddress;

    /**
     * IP归属地
     */
    private String ipLocation;

    /**
     * 错误原因
     */
    private String failReason;

    /**
     * 退出时间
     */
    private LocalDateTime logoutTime;
}
