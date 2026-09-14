package com.dotm.entity.model;

import lombok.Data;

/**
 * @author dotm
 * 负载信息
 */
@Data
public class LoadInfo {
    /** 1分钟平均负载 */
    private double oneMinute;

    /** 5分钟平均负载 */
    private double fiveMinutes;

    /** 15分钟平均负载 */
    private double fifteenMinutes;
}