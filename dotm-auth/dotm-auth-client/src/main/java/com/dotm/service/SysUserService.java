package com.dotm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.dto.SysUserDTO;
import com.dotm.entity.model.SysMenu;
import com.dotm.entity.model.SysUser;
import com.dotm.entity.vo.SysUserVO;

import java.util.List;

/**
 * @author dotm
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service
 * @createDate 2026-08-06 17:04:42
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 查询用户的平铺menu列表集合 多对多联表查询
     */
    List<SysMenu> selectMenusByUserId(Long userId);

    /**
     * 分页查询用户
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果（不含密码）
     */
    IPage<SysUserVO> pageUsers(SysUserDTO sysUserDTO, long pageNum, long pageSize);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    SysUserVO getUserById(Long userId);

    /**
     * 新增用户
     *
     * @param dto 用户入参
     * @return 是否成功
     */
    boolean addUser(SysUserDTO dto);

    /**
     * 修改用户
     *
     * @param dto 用户入参
     * @return 是否成功
     */
    boolean updateUser(SysUserDTO dto);

    /**
     * 检测用户归属性
     *
     * @param userId 用户ID
     * @return true 如果用户归属当前登录用户，false 否则
     */
    boolean checkUserOwnership(Long userId);
}