package com.dotm.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysRoleDTO;
import com.dotm.entity.vo.SysRoleVO;
import com.framework.model.Result;
import com.dotm.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * 角色管理接口
 */
@RequestMapping("/auth/role")
@RestController
@RequiredArgsConstructor
public class RoleManagerController {

    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('system:role:list')")
    public Result<IPage<SysRoleVO>> list(@RequestParam(
                                                 name = UserConstants.PAGE_NUM,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_NUM)
                                         Integer pageNum,
                                         @RequestParam(
                                                 name = UserConstants.PAGE_SIZE,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_SIZE)
                                         Integer pageSize,
                                         SysRoleDTO sysRoleDTO) {
        //调用 Service 层分页查询角色
        IPage<SysRoleVO> rolePage = sysRoleService.pageRoles(sysRoleDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(rolePage);
    }

    /**
     * 查询所有角色列表
     */
    @GetMapping("/get/all")
    @PreAuthorize("@ss.hasPermission('system:role:list')")
    public Result<List<SysRoleVO>> listAll() {
        //调用 Service 层查询所有角色
        List<SysRoleVO> roleList = sysRoleService.listAllRoles();
        //返回角色列表
        return Result.success(roleList);
    }

    /**
     * 根据角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public Result<SysRoleVO> getById(@PathVariable Long roleId) {
        //根据角色ID查询角色信息
        SysRoleVO sysRole = sysRoleService.getRoleById(roleId);
        return Result.success(sysRole);
    }

    /**
     * 新增角色
     *
     * @param dto 角色入参
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:role:add')")
    public Result<Void> add(@RequestBody SysRoleDTO dto) {
        //新增角色
        sysRoleService.addRole(dto);
        return Result.success();
    }

    /**
     * 修改角色
     *
     * @param dto 角色入参
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:role:edit')")
    public Result<Void> update(@RequestBody SysRoleDTO dto) {
        //修改角色
        sysRoleService.updateRole(dto);
        return Result.success();
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("@ss.hasPermission('system:role:remove')")
    public Result<Void> delete(@PathVariable Long roleId) {
        //根据角色ID删除角色
        sysRoleService.removeById(roleId);
        return Result.success();
    }

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    @PreAuthorize("@ss.hasPermission('system:role:remove')")
    public Result<Void> batchDelete(@RequestBody List<Long> roleIds) {
        //根据角色ID集合批量删除角色
        sysRoleService.removeByIds(roleIds);
        return Result.success();
    }
}