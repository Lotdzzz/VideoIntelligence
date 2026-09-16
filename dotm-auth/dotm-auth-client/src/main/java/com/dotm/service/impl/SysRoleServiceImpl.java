package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysRoleDTO;
import com.dotm.entity.model.SysRole;
import com.dotm.entity.model.SysUserRole;
import com.dotm.entity.vo.SysRoleVO;
import com.framework.exception.role.RoleBindMenusException;
import com.dotm.mapper.SysRoleMapper;
import com.dotm.service.SysRoleMenuService;
import com.dotm.service.SysRoleService;
import com.dotm.service.SysUserRoleService;
import com.framework.utils.SecurityUtils;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author dotm
 * 针对表【sys_role(角色信息表)】的数据库操作Service实现
 * 创建时间：2026-08-06 17:04:42
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    private final SysUserRoleService sysUserRoleService;

    private final SysRoleMenuService sysRoleMenuService;

    /**
     * 通过用户id查询角色ids
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleService.list(queryWrapper);
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return roleIds.isEmpty() ? List.of() : listByIds(roleIds);
    }

    /**
     * 分页查询角色
     */
    @Override
    public IPage<SysRoleVO> pageRoles(SysRoleDTO sysRoleDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<SysRole> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.isNotEmpty(sysRoleDTO.getRoleName()),
                SysRole::getRoleName,
                sysRoleDTO.getRoleName()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysRoleDTO.getRoleKey()),
                SysRole::getRoleKey,
                sysRoleDTO.getRoleKey()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(sysRoleDTO.getStatus()),
                SysRole::getStatus,
                sysRoleDTO.getStatus()
        );

        //执行分页查询
        IPage<SysRole> rolePage = page(page, wrapper);

        //转换为VO分页结果
        Page<SysRoleVO> voPage = new Page<>(pageNum, pageSize, rolePage.getTotal());
        voPage.setRecords(rolePage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据角色ID查询角色
     */
    @Override
    public SysRoleVO getRoleById(Long roleId) {
        //根据ID查询角色实体
        SysRole sysRole = getById(roleId);

        //在此需要查询该角色对应的菜单id列表
        List<Long> menuIds = sysRoleMenuService.selectMenuIdsByRoleId(roleId);

        sysRole.setMenuIds(menuIds);

        //转换为VO
        return toVO(sysRole);
    }

    /**
     * 新增角色
     */
    @Override
    @Transactional
    public boolean addRole(SysRoleDTO dto) {
        //DTO转换为角色实体（忽略roleId，由数据库自增生成）
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(dto, sysRole);

        //保存角色 获取角色id
        boolean isSave = save(sysRole);

        //同时绑定角色和菜单列表
        boolean isBind = sysRoleMenuService.bindMenusByRoleId(sysRole.getRoleId(), sysRole.getMenuIds());

        if (!isBind) {
            throw new RoleBindMenusException(null);
        }

        //保存角色
        return isSave;
    }

    /**
     * 修改角色
     */
    @Override
    @Transactional
    public boolean updateRole(SysRoleDTO dto) {
        //DTO转换为角色实体
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(dto, sysRole);

        //同时绑定角色和菜单列表 先删除原有绑定关系再绑定新的菜单列表
        boolean isUnbind = sysRoleMenuService.unBindMenusByRoleId(sysRole.getRoleId());
        boolean isBind = sysRoleMenuService.bindMenusByRoleId(sysRole.getRoleId(), sysRole.getMenuIds());

        if (!isBind || !isUnbind) {
            throw new RoleBindMenusException(null);
        }

        //根据ID更新角色
        return updateById(sysRole);
    }

    /**
     * 角色实体转换为视图对象
     *
     * @param sysRole 角色实体
     * @return 角色视图对象
     */
    private SysRoleVO toVO(SysRole sysRole) {
        if (sysRole == null) {
            return null;
        }
        SysRoleVO vo = new SysRoleVO();
        BeanUtils.copyProperties(sysRole, vo);
        return vo;
    }

    /**
     * 查询所有角色列表
     */
    @Override
    public List<SysRoleVO> listAllRoles() {
        // 查询所有角色 如果用户不是管理员则不返回管理员角色
        List<SysRole> sysRoles = list();
        if (!UserConstants.ADMIN.equals(SecurityUtils.getUserId())) {
            return sysRoles
                    .stream()
                    .filter(role -> !UserConstants.ADMIN_KEY.equals(role.getRoleKey()))
                    .toList()
                    .stream()
                    .map(this::toVO)
                    .toList();
        }
        // 转换为VO列表
        return sysRoles.stream().map(this::toVO).toList();
    }
}