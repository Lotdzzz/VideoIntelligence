package com.dotm.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysMenuDTO;
import com.dotm.entity.vo.SysMenuVO;
import com.framework.model.Result;
import com.dotm.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * 菜单管理接口
 */
@RequestMapping("/auth/menu")
@RestController
@RequiredArgsConstructor
public class MenuManagerController {

    private final SysMenuService sysMenuService;

    /**
     * 分页查询菜单
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('system:menu:list')")
    public Result<IPage<SysMenuVO>> list(@RequestParam(
                                                 name = UserConstants.PAGE_NUM,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_NUM)
                                         Integer pageNum,
                                         @RequestParam(
                                                 name = UserConstants.PAGE_SIZE,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_SIZE)
                                         Integer pageSize,
                                         SysMenuDTO sysMenuDTO) {
        //调用 Service 层分页查询菜单
        IPage<SysMenuVO> menuPage = sysMenuService.pageMenus(sysMenuDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(menuPage);
    }

    /**
     * 根据菜单ID查询菜单
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/{menuId}")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<SysMenuVO> getById(@PathVariable Long menuId) {
        //根据菜单ID查询菜单信息
        SysMenuVO sysMenu = sysMenuService.getMenuById(menuId);
        return Result.success(sysMenu);
    }

    /**
     * 新增菜单
     *
     * @param dto 菜单入参
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:menu:add')")
    public Result<Void> add(@RequestBody SysMenuDTO dto) {
        //新增菜单
        sysMenuService.addMenu(dto);
        return Result.success();
    }

    /**
     * 修改菜单
     *
     * @param dto 菜单入参
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:menu:edit')")
    public Result<Void> update(@RequestBody SysMenuDTO dto) {
        //修改菜单
        sysMenuService.updateMenu(dto);
        return Result.success();
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/{menuId}")
    @PreAuthorize("@ss.hasPermission('system:menu:remove')")
    public Result<Void> delete(@PathVariable Long menuId) {
        //根据菜单ID删除菜单
        sysMenuService.removeById(menuId);
        return Result.success();
    }

    /**
     * 批量删除菜单
     *
     * @param menuIds 菜单ID集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    @PreAuthorize("@ss.hasPermission('system:menu:remove')")
    public Result<Void> batchDelete(@RequestBody List<Long> menuIds) {
        //根据菜单ID集合批量删除菜单
        sysMenuService.removeByIds(menuIds);
        return Result.success();
    }
}