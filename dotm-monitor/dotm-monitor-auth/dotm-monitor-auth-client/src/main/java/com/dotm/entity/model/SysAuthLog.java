package com.dotm.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 用户认证登录日志
 * @TableName sys_auth_log
 */
@TableName(value ="sys_auth_log")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysAuthLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * token过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic(value = "0", delval = "1")
    private String delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}