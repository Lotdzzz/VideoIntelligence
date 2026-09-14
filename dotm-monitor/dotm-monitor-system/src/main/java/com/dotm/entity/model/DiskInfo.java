package com.dotm.entity.model;

import lombok.Data;

/**
 * @author dotm
 * 磁盘信息
 */
@Data
public class DiskInfo {
    /** 磁盘名称 (如 /dev/sda1) */
    private String name;

    /** 挂载点 (如 /) */
    private String mount;

    /** 总空间 (字节) */
    private long total;

    /** 剩余空间 (字节) */
    private long free;

    /** 已用空间 (字节) */
    private long used;

    /** 读取字节数 (累计) */
    private long readBytes;

    /** 写入字节数 (累计) */
    private long writeBytes;

    /** 读取次数 (累计) */
    private long reads;

    /** 写入次数 (累计) */
    private long writes;
}