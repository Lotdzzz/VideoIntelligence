package com.dotm.entity.dto;

import com.dotm.entity.model.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author dotm
 */
@Data
@Builder
public class SystemInfoDTO {
    /**
     * cpu信息
     */
    private CpuInfo cpu;

    /**
     * 内存信息
     */
    private MemoryInfo memory;

    /**
     * 磁盘信息
     */
    private List<DiskInfo> disks;

    /**
     * 负载信息
     */
    private LoadInfo load;

    /**
     * JVM信息
     */
    private JvmInfo jvm;
}
