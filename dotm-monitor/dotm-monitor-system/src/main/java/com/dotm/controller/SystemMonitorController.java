package com.dotm.controller;

import com.framework.model.Result;
import com.dotm.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dotm
 * 获取系统信息以及运行时信息的接口
 */
@RequestMapping("/os/monitor")
@RestController
@RequiredArgsConstructor
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;

    /**
     * 获取系统信息 运行时内存的接口
     *
     * @return 系统信息
     */
    @GetMapping("/info")
    public Result<Object> getSystemInfo() {
        return Result.success(systemMonitorService.getSystemInfo());
    }

}
