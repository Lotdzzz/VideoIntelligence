package com.dotm.service.impl;

import com.dotm.entity.dto.SystemInfoDTO;
import com.dotm.entity.model.CpuInfo;
import com.dotm.entity.model.DiskInfo;
import com.dotm.entity.model.JvmInfo;
import com.dotm.entity.model.MemoryInfo;
import com.dotm.service.SystemMonitorService;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dotm
 * 这个业务类只负责获取系统信息不负责具体计算
 */
@Service
public class SystemMonitorServiceImpl implements SystemMonitorService {

    /**
     * OSHI 系统信息对象
     */
    private final SystemInfo si = new SystemInfo();

    /**
     * 硬件抽象层对象
     */
    private final HardwareAbstractionLayer hal = si.getHardware();

    /**
     * 操作系统对象
     */
    private final OperatingSystem os = si.getOperatingSystem();

    /**
     * cpu使用率的ticks缓存
     * 初始化第一次获取系统cpu使用率ticks
     */
    private long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    @Override
    public SystemInfoDTO getSystemInfo() {
        return SystemInfoDTO.builder()
                .cpu(getCpuInfo())
                .memory(getMemoryInfo())
                .disks(getDiskInfo())
                .jvm(getJvmInfo())
                .build();
    }

    /**
     * 获取 CPU 信息
     *
     * @return CPU 信息
     */
    private CpuInfo getCpuInfo() {
        // 获取 CPU 信息
        CentralProcessor cpu = hal.getProcessor();

        // 获取当前的cpu使用率ticks
        long[] currentCpuLoad = cpu.getSystemCpuLoadTicks();

        // 计算 CPU 使用率 如果是第一次计算则为0 之后每次计算上次存储的ticks和当前ticks的差值
        double usage = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        prevTicks = currentCpuLoad; // 更新ticks缓存

        CpuInfo info = new CpuInfo();
        info.setUsage(Math.min(usage, 100)); //使用率
        info.setCores(cpu.getLogicalProcessorCount()); //核心数
        return info;
    }

    /**
     * 获取内存信息
     */
    private MemoryInfo getMemoryInfo() {
        // 获取内存信息
        GlobalMemory memory = hal.getMemory();

        // 计算内存使用率
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        double percent = (double) used / total * 100;

        MemoryInfo info = new MemoryInfo();
        info.setTotal(total);
        info.setAvailable(available);
        info.setUsed(used);
        info.setUsedPercent(Math.min(percent, 100));
        return info;
    }

    /**
     * 获取IO磁盘信息
     */
    private List<DiskInfo> getDiskInfo() {
        List<DiskInfo> list = new ArrayList<>();
        // 获取磁盘存储（分区信息）
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        // 获取物理磁盘 I/O 统计
        List<HWDiskStore> diskStores = hal.getDiskStores();

        for (OSFileStore fs : fileStores) {
            DiskInfo info = new DiskInfo();
            info.setName(fs.getName());
            info.setMount(fs.getMount());
            long total = fs.getTotalSpace();
            long free = fs.getUsableSpace();
            info.setTotal(total);
            info.setFree(free);
            info.setUsed(total - free);

            // 尝试匹配对应的 HWDiskStore 获取 I/O 数据
            // 注意：这里简化处理，假设分区名称与磁盘名称有关联，实际可根据情况调整匹配逻辑
            for (HWDiskStore disk : diskStores) {
                if (disk.getName().contains(fs.getMount()) || fs.getMount().contains(disk.getName())) {
                    info.setReadBytes(disk.getReadBytes());
                    info.setWriteBytes(disk.getWriteBytes());
                    info.setReads(disk.getReads());
                    info.setWrites(disk.getWrites());
                    break;
                }
            }
            list.add(info);
        }
         return list;
    }

    /**
     * 获取JVM内存信息
     */
    private JvmInfo getJvmInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        JvmInfo info = new JvmInfo();
        info.setName(runtime.getVmName());
        info.setVersion(runtime.getVmVersion());
        info.setVendor(runtime.getVmVendor());
        info.setStartTime(runtime.getStartTime());
        info.setUptime(runtime.getUptime());

        MemoryUsage heap = memory.getHeapMemoryUsage();
        info.setHeapUsed(heap.getUsed());
        info.setHeapMax(heap.getMax());

        MemoryUsage nonHeap = memory.getNonHeapMemoryUsage();
        info.setNonHeapUsed(nonHeap.getUsed());
        info.setNonHeapCommitted(nonHeap.getCommitted());
         return info;
    }
}
