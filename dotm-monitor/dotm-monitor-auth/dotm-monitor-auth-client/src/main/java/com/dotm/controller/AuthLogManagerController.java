package com.dotm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dotm.constants.AuthLogConstants;
import com.dotm.entity.dto.SysAuthLogDTO;
import com.dotm.entity.vo.SysAuthLogVO;
import com.framework.model.Result;
import com.dotm.service.SysAuthLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * 认证登录日志管理接口
 */
@RequestMapping("/certification/monitor/authLog")
@RestController
@RequiredArgsConstructor
public class AuthLogManagerController {

    private final SysAuthLogService sysAuthLogService;

    /**
     * 分页查询认证登录日志
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('monitor:authLog:list')")
    public Result<IPage<SysAuthLogVO>> list(@RequestParam(
                                                    name = AuthLogConstants.PAGE_NUM,
                                                    defaultValue = AuthLogConstants.DEFAULT_PAGE_NUM)
                                            Integer pageNum,
                                            @RequestParam(
                                                    name = AuthLogConstants.PAGE_SIZE,
                                                    defaultValue = AuthLogConstants.DEFAULT_PAGE_SIZE)
                                            Integer pageSize,
                                            SysAuthLogDTO sysAuthLogDTO) {
        //调用 Service 层分页查询认证登录日志
        IPage<SysAuthLogVO> authLogPage = sysAuthLogService.pageAuthLogs(sysAuthLogDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(authLogPage);
    }

    /**
     * 根据日志ID查询认证登录日志
     *
     * @param id 日志ID
     * @return 日志信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('monitor:authLog:query')")
    public Result<SysAuthLogVO> getById(@PathVariable Long id) {
        //根据日志ID查询日志信息
        SysAuthLogVO sysAuthLogVO = sysAuthLogService.getAuthLogById(id);
        return Result.success(sysAuthLogVO);
    }

    /**
     * 删除认证登录日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermission('monitor:authLog:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        //根据日志ID删除日志
        sysAuthLogService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除认证登录日志
     *
     * @param ids 日志ID集合
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermission('monitor:authLog:delete')")
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        //根据日志ID集合批量删除日志
        sysAuthLogService.removeByIds(ids);
        return Result.success();
    }
}