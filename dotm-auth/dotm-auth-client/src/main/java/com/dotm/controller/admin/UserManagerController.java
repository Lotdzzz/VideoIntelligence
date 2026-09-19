package com.dotm.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.system.SysUserDTO;
import com.dotm.entity.vo.SysUserVO;
import com.framework.model.Result;
import com.dotm.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * 用户管理接口
 */
@RequestMapping("/auth/user")
@RestController
@RequiredArgsConstructor
public class UserManagerController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果（不含密码）
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public Result<IPage<SysUserVO>> list(@RequestParam(
                                                 name = UserConstants.PAGE_NUM,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_NUM)
                                         Integer pageNum,
                                         @RequestParam(
                                                 name = UserConstants.PAGE_SIZE,
                                                 defaultValue = UserConstants.DEFAULT_PAGE_SIZE)
                                         Integer pageSize,
                                         SysUserDTO sysUserDTO) {
        //调用 Service 层分页查询用户
        IPage<SysUserVO> userPage = sysUserService.pageUsers(sysUserDTO, pageNum, pageSize);
        //返回分页结果，不含密码字段
        return Result.success(userPage);
    }

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public Result<SysUserVO> getById(@PathVariable Long userId) {
        //根据用户ID查询用户信息
        SysUserVO sysUser = sysUserService.getUserById(userId);
        return Result.success(sysUser);
    }

    /**
     * 新增用户
     *
     * @param dto 用户入参
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:user:add')")
    public Result<Void> add(@RequestBody SysUserDTO dto) {
        //新增用户（密码在 Service 层加密后入库）
        sysUserService.addUser(dto);
        return Result.success();
    }

    /**
     * 修改用户
     *
     * @param dto 用户入参
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:user:edit')")
    public Result<Void> update(@RequestBody SysUserDTO dto) {
        //修改用户（新密码在 Service 层加密后入库）
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("@ss.hasPermission('system:user:remove')")
    public Result<Void> delete(@PathVariable Long userId) {
        //根据用户ID删除用户
        sysUserService.removeById(userId);
        return Result.success();
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    @PreAuthorize("@ss.hasPermission('system:user:remove')")
    public Result<Void> batchDelete(@RequestBody List<Long> userIds) {
        //根据用户ID集合批量删除用户
        sysUserService.removeByIds(userIds);
        return Result.success();
    }

    /**
     * 修改用户
     *
     * @param dto 用户入参
     * @return 操作结果
     */
    @PutMapping("/updateByOwner")
    public Result<Void> updateByOwner(@RequestBody SysUserDTO dto) {
        //检测用户归属性
        boolean isOwner = sysUserService.checkUserOwnership(dto.getUserId());
        if (!isOwner) {
            return Result.error("您无权修改该用户信息");
        }

        //修改用户（新密码在 Service 层加密后入库）
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    @GetMapping("/getByOwner/{userId}")
    public Result<SysUserVO> getByIdByOwner(@PathVariable Long userId) {
        //检测用户归属性
        boolean isOwner = sysUserService.checkUserOwnership(userId);
        if (!isOwner) {
            return Result.error("您无权获取该用户信息");
        }

        //根据用户ID查询用户信息
        SysUserVO sysUser = sysUserService.getUserById(userId);
        return Result.success(sysUser);
    }
}