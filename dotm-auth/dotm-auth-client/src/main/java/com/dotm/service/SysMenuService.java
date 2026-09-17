package com.dotm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.dto.system.SysMenuDTO;
import com.dotm.entity.model.system.SysMenu;
import com.dotm.entity.vo.SysMenuVO;

import java.util.List;

/**
* @author dotm
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service
* @createDate 2026-08-06 17:04:42
*/
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 通过角色主键集合查询对应的menu集合
     */
    List<SysMenu> selectMenusByRoleIds(List<Long> roleIds);

    /**
     * 将menu集合组装成树状结构
     *
     * @param sysMenus menu集合
     * @return 结果
     */
    List<SysMenu> buildTree(List<SysMenu> sysMenus);

    /**
     * 分页查询菜单
     *
     * @param sysMenuDTO 查询入参
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @return 分页结果
     */
    IPage<SysMenuVO> pageMenus(SysMenuDTO sysMenuDTO, long pageNum, long pageSize);

    /**
     * 根据菜单ID查询菜单
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    SysMenuVO getMenuById(Long menuId);

    /**
     * 新增菜单
     *
     * @param dto 菜单入参
     * @return 是否成功
     */
    boolean addMenu(SysMenuDTO dto);

    /**
     * 修改菜单
     *
     * @param dto 菜单入参
     * @return 是否成功
     */
    boolean updateMenu(SysMenuDTO dto);
}

