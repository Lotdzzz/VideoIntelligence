package com.dotm.entity.model;

import lombok.Data;

/**
 * @author dotm
 * 内存信息
 */
@Data
public class MemoryInfo {
    /** 总内存 (字节) */
    private long total;

    /** 可用内存 (字节) */
    private long available;

    /** 已用内存 (字节) */
    private long used;

    /** 使用率 (%) */
    private double usedPercent;
}