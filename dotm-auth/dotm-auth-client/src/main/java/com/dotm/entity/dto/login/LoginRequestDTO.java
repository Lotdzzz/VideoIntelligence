package com.dotm.entity.dto.login;

import lombok.Builder;
import lombok.Data;

/**
 * @author dotm
 */
@Data
@Builder
public class LoginRequestDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户指纹
     */
    private String userAgent;

    /**
     * 用户真实ip
     */
    private String ip;
}
