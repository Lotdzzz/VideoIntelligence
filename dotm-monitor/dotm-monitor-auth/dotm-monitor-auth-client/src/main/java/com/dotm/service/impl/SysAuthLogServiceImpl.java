package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.entity.dto.SysAuthLogDTO;
import com.dotm.entity.model.SysAuthLog;
import com.dotm.entity.vo.SysAuthLogVO;
import com.dotm.mapper.SysAuthLogMapper;
import com.dotm.service.SysAuthLogService;
import com.framework.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author dotm
* 针对表【sys_auth_log(用户认证登录日志)】的数据库操作Service实现
* 创建时间：2026-08-15 19:30:00
*/
@Service
public class SysAuthLogServiceImpl extends ServiceImpl<SysAuthLogMapper, SysAuthLog>
    implements SysAuthLogService{

    /**
     * 分页查询认证登录日志
     */
    @Override
    public IPage<SysAuthLogVO> pageAuthLogs(SysAuthLogDTO sysAuthLogDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<SysAuthLog> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<SysAuthLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.isNotEmpty(sysAuthLogDTO.getUsername()),
                SysAuthLog::getUsername,
                sysAuthLogDTO.getUsername()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(sysAuthLogDTO.getLoginType()),
                SysAuthLog::getLoginType,
                sysAuthLogDTO.getLoginType()
        );

        wrapper.eq(
                sysAuthLogDTO.getStatus() != null,
                SysAuthLog::getStatus,
                sysAuthLogDTO.getStatus()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysAuthLogDTO.getIpAddress()),
                SysAuthLog::getIpAddress,
                sysAuthLogDTO.getIpAddress()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysAuthLogDTO.getIpLocation()),
                SysAuthLog::getIpLocation,
                sysAuthLogDTO.getIpLocation()
        );

        wrapper.between(
                sysAuthLogDTO.getBeginTime() != null && sysAuthLogDTO.getEndTime() != null,
                SysAuthLog::getLoginTime,
                sysAuthLogDTO.getBeginTime(),
                sysAuthLogDTO.getEndTime()
        );

        //按登录时间倒序
        wrapper.orderByDesc(SysAuthLog::getLoginTime);

        //执行分页查询
        IPage<SysAuthLog> authLogPage = page(page, wrapper);

        //转换为VO分页结果
        Page<SysAuthLogVO> voPage = new Page<>(pageNum, pageSize, authLogPage.getTotal());
        voPage.setRecords(authLogPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据日志ID查询认证登录日志
     */
    @Override
    public SysAuthLogVO getAuthLogById(Long id) {
        //根据ID查询日志实体
        SysAuthLog sysAuthLog = getById(id);
        //转换为VO
        return toVO(sysAuthLog);
    }

    /**
     * 日志实体转换为视图对象
     *
     * @param sysAuthLog 日志实体
     * @return 日志视图对象
     */
    private SysAuthLogVO toVO(SysAuthLog sysAuthLog) {
        if (sysAuthLog == null) {
            return null;
        }
        SysAuthLogVO vo = new SysAuthLogVO();
        BeanUtils.copyProperties(sysAuthLog, vo);
        return vo;
    }
}