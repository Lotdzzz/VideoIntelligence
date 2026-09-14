package com.dotm.service;

import com.dotm.entity.dto.SystemInfoDTO;

/**
 * @author dotm
 * 系统以及运行内存监控服务
 */
public interface SystemMonitorService {

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    SystemInfoDTO getSystemInfo();
}
