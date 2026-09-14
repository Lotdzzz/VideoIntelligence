package com.dotm.entity.model;

import lombok.Data;

/**
 * @author dotm
 * JVM 信息
 */
@Data
public class JvmInfo {
    /** JVM 名称 */
    private String name;

    /** JVM 版本 */
    private String version;

    /** JVM 供应商 */
    private String vendor;

    /** 启动时间 */
    private long startTime;

    /** 运行时长 */
    private long uptime;

    /** 堆内存已用 (字节) */
    private long heapUsed;

    /** 堆内存最大值 (字节) */
    private long heapMax;

    /** 非堆内存已用 (字节) */
    private long nonHeapUsed;

    /** 非堆内存已提交 (字节) */
    private long nonHeapCommitted;
}