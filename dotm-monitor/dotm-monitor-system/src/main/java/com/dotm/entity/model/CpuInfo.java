package com.dotm.entity.model;

import lombok.Data;

/**
 * @author dotm
 * CPU 信息
 */
@Data
public class CpuInfo {
    /** CPU 实时使用率 (0~100) */
    private double usage;

    /** CPU 核心数 */
    private int cores;
}