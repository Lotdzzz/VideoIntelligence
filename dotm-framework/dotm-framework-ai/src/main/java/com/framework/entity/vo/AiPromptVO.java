package com.framework.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * AI提示词视图对象（查询返回）
 */
@Data
public class AiPromptVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 提示词名称
     */
    private String name;

    /**
     * 提示词内容
     */
    private String content;

    /**
     * 模型，如 gpt-4o
     */
    private String model;

    /**
     * 模型参数，如 {"temperature":0.7}
     */
    private String modelParams;

    /**
     * 唯一标识key
     */
    private String key;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}