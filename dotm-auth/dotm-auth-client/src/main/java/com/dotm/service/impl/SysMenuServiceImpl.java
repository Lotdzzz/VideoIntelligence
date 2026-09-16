package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysMenuDTO;
import com.dotm.entity.model.SysMenu;
import com.dotm.entity.model.SysRoleMenu;
import com.dotm.entity.vo.SysMenuVO;
import com.dotm.mapper.SysMenuMapper;
import com.dotm.service.SysMenuService;
import com.dotm.service.SysRoleMenuService;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dotm
 * 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
 * 创建时间：2026-08-06 17:04:42
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    private final SysRoleMenuService sysRoleMenuService;

    /**
     * 通过角色主键集合查询对应的menu集合
     */
    @Override
    public List<SysMenu> selectMenusByRoleIds(List<Long> roleIds) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenu::getRoleId, roleIds);
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuService.list(queryWrapper);
        if (sysRoleMenus != null && !sysRoleMenus.isEmpty()) {
            List<Long> menuIds = sysRoleMenus.stream().map(SysRoleMenu::getMenuId).toList();
            return listByIds(menuIds);
        }
        return List.of();
    }

    /**
     * 将menu集合组装成树状结构
     *
     * @param sysMenus menu集合
     * @return 结果
     */
    @Override
    public List<SysMenu> buildTree(List<SysMenu> sysMenus) {
        return getChildren(sysMenus); //0为顶级节点
    }

    /**
     * 拼装根节点
     *
     * @param sysMenus 平铺集合
     * @return 结果
     */
    private List<SysMenu> getChildren(List<SysMenu> sysMenus) {
        List<SysMenu> rootNodes = new ArrayList<>();
        for (SysMenu sysMenu : sysMenus) {
            if (sysMenu.getParentId() == 0) { //获取所有根节点 再拼装子节点
                rootNodes.add(sysMenu);
                recursionFn(sysMenus, sysMenu); //参数：平铺集合，根节点
            }
        }
        return rootNodes;
    }

    /**
     * 获取子节点
     *
     * @param sysMenus 平铺集合
     * @param node     节点
     */
    private void recursionFn(List<SysMenu> sysMenus, SysMenu node) {

        List<SysMenu> childrenList = buildChildren(sysMenus, node); //封装该节点的下一级子节点 返回子节点集合

        node.setChildren(childrenList); //拼装子节点

        for (SysMenu sysMenu : childrenList) { //遍历子节点集合 封装子节点的子节点
            if (hasChild(sysMenus, sysMenu)) { //如果存在子节点 则拼装子节点
                recursionFn(sysMenus, sysMenu); //递归拼装
            }
        }
    }

    /**
     * 获取节点的子节点
     *
     * @param sysMenus 平铺集合
     * @param node     节点
     */
    private List<SysMenu> buildChildren(List<SysMenu> sysMenus, SysMenu node) {

        List<SysMenu> result = new ArrayList<>();

        for (SysMenu sysMenu : sysMenus) {
            if (sysMenu.getParentId().equals(node.getMenuId())) {
                result.add(sysMenu);
            }
        }

        return result;
    }

    /**
     * 判断节点有没有子节点
     *
     * @return 结果
     */
    private boolean hasChild(List<SysMenu> sysMenus, SysMenu node) {
        for (SysMenu sysMenu : sysMenus) {
            if (sysMenu.getMenuId().equals(node.getParentId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分页查询菜单
     */
    @Override
    public IPage<SysMenuVO> pageMenus(SysMenuDTO sysMenuDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<SysMenu> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.isNotEmpty(sysMenuDTO.getMenuName()),
                SysMenu::getMenuName,
                sysMenuDTO.getMenuName()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(sysMenuDTO.getStatus()),
                SysMenu::getStatus,
                sysMenuDTO.getStatus()
        );

        //执行分页查询
        IPage<SysMenu> menuPage = page(page, wrapper);

        //转换为VO分页结果
        Page<SysMenuVO> voPage = new Page<>(pageNum, pageSize, menuPage.getTotal());
        voPage.setRecords(menuPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据菜单ID查询菜单
     */
    @Override
    public SysMenuVO getMenuById(Long menuId) {
        //根据ID查询菜单实体
        SysMenu sysMenu = getById(menuId);
        //转换为VO
        return toVO(sysMenu);
    }

    /**
     * 新增菜单
     */
    @Override
    public boolean addMenu(SysMenuDTO dto) {
        //DTO转换为菜单实体（忽略menuId，由数据库自增生成）
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(dto, sysMenu, UserConstants.MENU_ID);
        //保存菜单
        return save(sysMenu);
    }

    /**
     * 修改菜单
     */
    @Override
    public boolean updateMenu(SysMenuDTO dto) {
        //DTO转换为菜单实体
        SysMenu sysMenu = new SysMenu();
        BeanUtils.copyProperties(dto, sysMenu);
        //根据ID更新菜单
        return updateById(sysMenu);
    }

    /**
     * 菜单实体转换为视图对象
     *
     * @param sysMenu 菜单实体
     * @return 菜单视图对象
     */
    private SysMenuVO toVO(SysMenu sysMenu) {
        if (sysMenu == null) {
            return null;
        }
        SysMenuVO vo = new SysMenuVO();
        BeanUtils.copyProperties(sysMenu, vo);
        return vo;
    }
}


