package com.dotm.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * @author dotm
 * 用户新增/修改入参对象（可传入密码）
 */
@Data
public class SysUserDTO {
    /**
     * 用户ID（修改时必填）
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

    /**
     * 密码（新增时必填，修改时选填）
     */
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户角色ID集合
     */
    private List<Long> roleIds;
}