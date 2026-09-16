package com.dotm.entity.dto.oauth;

import lombok.Data;

/**
 * @author dotm
 * 第三方账号绑定查询/绑定入参对象
 */
@Data
public class SysAuthOauthDTO {
    /**
     * 第三方账号绑定ID（修改时必填）
     */
    private Long oauthId;

    /**
     * 系统用户ID
     */
    private Long userId;

    /**
     * 第三方平台（github、wechat、qq）
     */
    private String provider;

    /**
     * 第三方平台用户唯一ID
     */
    private String openId;

    /**
     * 第三方平台统一ID
     */
    private String unionId;

    /**
     * 第三方账号昵称
     */
    private String oauthName;

    /**
     * 第三方账号头像
     */
    private String oauthAvatar;

    /**
     * 第三方账号邮箱
     */
    private String oauthEmail;

    /**
     * 备注
     */
    private String remark;
}
