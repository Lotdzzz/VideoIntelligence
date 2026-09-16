package com.dotm.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 第三方账号绑定视图对象（查询返回）
 */
@Data
public class SysAuthOauthVO {
    /**
     * 第三方账号绑定ID
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
     * 绑定时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
