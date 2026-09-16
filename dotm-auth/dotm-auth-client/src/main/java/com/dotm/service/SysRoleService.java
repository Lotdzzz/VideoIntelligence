package com.dotm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.dto.system.SysRoleDTO;
import com.dotm.entity.model.SysRole;
import com.dotm.entity.vo.SysRoleVO;

import java.util.List;

/**
* @author dotm
* @description 针对表【sys_role(角色信息表)】的数据库操作Service
* @createDate 2026-08-06 17:04:42
*/
public interface SysRoleService extends IService<SysRole> {

    /**
     * 通过用户id查询角色ids
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 分页查询角色
     *
     * @param sysRoleDTO 查询入参
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @return 分页结果
     */
    IPage<SysRoleVO> pageRoles(SysRoleDTO sysRoleDTO, long pageNum, long pageSize);

    /**
     * 根据角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    SysRoleVO getRoleById(Long roleId);

    /**
     * 新增角色
     *
     * @param dto 角色入参
     * @return 是否成功
     */
    boolean addRole(SysRoleDTO dto);

    /**
     * 修改角色
     *
     * @param dto 角色入参
     * @return 是否成功
     */
    boolean updateRole(SysRoleDTO dto);

    /**
     * 查询所有角色列表
     */
    List<SysRoleVO> listAllRoles();
}

