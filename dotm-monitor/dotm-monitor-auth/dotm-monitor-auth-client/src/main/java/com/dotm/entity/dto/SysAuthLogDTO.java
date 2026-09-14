package com.dotm.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 认证登录日志查询入参对象
 */
@Data
public class SysAuthLogDTO {
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
     * 登录IP
     */
    private String ipAddress;

    /**
     * IP归属地
     */
    private String ipLocation;

    /**
     * 登录时间-开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 登录时间-结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}