package com.dotm.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dotm
 * 角色信息视图对象（查询返回）
 */
@Data
public class SysRoleVO {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符串
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    private Integer menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    private Integer deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;

    /**
     * 创建时间
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

    /**
     * 菜单权限列表
     */
    private List<Long> menuIds;
}