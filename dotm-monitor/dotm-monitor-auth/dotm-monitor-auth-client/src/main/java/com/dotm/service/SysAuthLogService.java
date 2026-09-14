package com.dotm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.dto.SysAuthLogDTO;
import com.dotm.entity.model.SysAuthLog;
import com.dotm.entity.vo.SysAuthLogVO;

/**
* @author dotm
* @description 针对表【sys_auth_log(用户认证登录日志)】的数据库操作Service
* @createDate 2026-08-15 19:30:00
*/
public interface SysAuthLogService extends IService<SysAuthLog> {

    /**
     * 分页查询认证登录日志
     *
     * @param sysAuthLogDTO 查询条件
     * @param pageNum       当前页码
     * @param pageSize      每页条数
     * @return 分页结果
     */
    IPage<SysAuthLogVO> pageAuthLogs(SysAuthLogDTO sysAuthLogDTO, long pageNum, long pageSize);

    /**
     * 根据日志ID查询认证登录日志
     *
     * @param id 日志ID
     * @return 日志信息
     */
    SysAuthLogVO getAuthLogById(Long id);
}
