package com.framework.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author dotm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginBodyModel implements Serializable {
    /**
     * 用户id
     */
    public Long userId;

    /**
     * 用户名
     */
    public String username;

    /**
     * 密码
     */
    public String password;

    /**
     * 设备指纹
     */
    public String userAgent;

    /**
     * 用户真实ip
     */
    public String ip;

    /**
     * 用户唯一标识
     */
    public String uuid;
}
