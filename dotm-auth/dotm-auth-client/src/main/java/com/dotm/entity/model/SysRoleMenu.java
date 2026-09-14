package com.dotm.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author dotm
 * 角色和菜单关联表
 * @TableName sys_role_menu
 */
@TableName(value ="sys_role_menu")
@Data
public class SysRoleMenu {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}

