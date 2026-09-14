package com.dotm.entity.vo;

import lombok.Data;

/**
 * @author dotm
 */
@Data
public class UserInfoVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 头像地址
     */
    private String avatar;
}
