package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysUserDTO;
import com.dotm.entity.model.system.SysMenu;
import com.dotm.entity.model.system.SysRole;
import com.dotm.entity.model.system.SysUser;
import com.dotm.entity.vo.SysUserVO;
import com.framework.exception.role.RolesNotExistsException;
import com.dotm.mapper.SysUserMapper;
import com.dotm.service.SysMenuService;
import com.dotm.service.SysRoleService;
import com.dotm.service.SysUserRoleService;
import com.dotm.service.SysUserService;
import com.framework.utils.SecurityUtils;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dotm
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
 * @createDate 2026-08-06 17:04:42
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final SysMenuService sysMenuService;

    private final SysRoleService sysRoleService;

    private final SysUserRoleService sysUserRoleService;

    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 查询用户的平铺menu列表集合 多对多联表查询
     */
    @Override
    public List<SysMenu> selectMenusByUserId(Long userId) {
        //如果是管理员则直接获取全部权限
        if (userId == 1) {
            return sysMenuService.list();
        }

        //通过用户id查询角色ids
        List<SysRole> sysRoles = sysRoleService.selectRolesByUserId(userId);

        // 如果角色列表为空，则抛出异常
        if (sysRoles == null) {
            throw new RolesNotExistsException(null);
        }
        List<Long> roleIds = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());

        //通过角色主键集合查询对应的menu集合
        return roleIds.isEmpty() ? List.of() : sysMenuService.selectMenusByRoleIds(roleIds);
    }

    /**
     * 分页查询用户
     */
    @Override
    public IPage<SysUserVO> pageUsers(SysUserDTO sysUserDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        // 条件构造器
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.isNotEmpty(sysUserDTO.getUserName()),
                SysUser::getUserName,
                sysUserDTO.getUserName()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysUserDTO.getNickName()),
                SysUser::getNickName,
                sysUserDTO.getNickName()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(sysUserDTO.getStatus()),
                SysUser::getStatus,
                sysUserDTO.getStatus()
        );

        //执行分页查询
        IPage<SysUser> userPage = page(page, wrapper);

        //转换为VO分页结果（不含密码）
        Page<SysUserVO> voPage = new Page<>(pageNum, pageSize, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据用户ID查询用户
     */
    @Override
    public SysUserVO getUserById(Long userId) {
        //根据ID查询用户实体
        SysUser sysUser = getById(userId);
        //转换为VO（不含密码）
        return toVO(sysUser);
    }

    /**
     * 新增用户
     */
    @Override
    @Transactional
    public boolean addUser(SysUserDTO dto) {
        //DTO转换为用户实体（忽略userId，由数据库自增生成）
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(dto, sysUser, UserConstants.USER_ID);
        //密码加密后入库
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }

        //保存角色
        boolean isSave = save(sysUser);

        dto.setUserId(sysUser.getUserId());

        //添加角色列表
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            return isSave;
        }
        boolean isBind = sysUserRoleService.bindRolesByUserId(sysUser.getUserId(), dto.getRoleIds());

        //保存用户
        return isSave && isBind;
    }

    /**
     * 修改用户
     */
    @Override
    @Transactional
    public boolean updateUser(SysUserDTO dto) {
        //DTO转换为用户实体
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(dto, sysUser);
        //若传入了新密码则加密，否则清空避免覆盖原密码
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
            sysUser.setPwdUpdateDate(LocalDateTime.now());
        } else {
            sysUser.setPassword(null);
        }

        //添加角色列表 先删除角色列表
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            return updateById(sysUser);
        }
        boolean isUnBind = sysUserRoleService.unBindRolesByUserId(sysUser.getUserId());
        boolean isBind = sysUserRoleService.bindRolesByUserId(sysUser.getUserId(), dto.getRoleIds());

        //根据ID更新用户
        return updateById(sysUser) && isBind && isUnBind;
    }

    /**
     * 用户实体转换为视图对象（去除密码等敏感字段）
     *
     * @param sysUser 用户实体
     * @return 用户视图对象
     */
    private SysUserVO toVO(SysUser sysUser) {
        if (sysUser == null) {
            return null;
        }
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(sysUser, vo);
        return vo;
    }

    /**
     * 检测用户归属性
     *
     * @param userId 用户ID
     * @return true 如果用户归属当前登录用户，false 否则
     */
    @Override
    public boolean checkUserOwnership(Long userId) {
        return userId.equals(SecurityUtils.getUserId());
    }
}