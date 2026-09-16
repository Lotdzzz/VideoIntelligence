package com.dotm.entity.dto.oauth;

import lombok.Data;

/**
 * github返回的授权信息
 *
 * @author dotm
 */
@Data
public class GithubOAuthCodeDTO {

    /**
     * 授权码
     */
    private String code;

    /**
     * 状态码
     */
    private String state;

    /**
     * 设备指纹
     */
    private String userAgent;
}
