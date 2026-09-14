package com.framework.entity.dto;

import lombok.Data;

/**
 * @author dotm
 * AI提示词新增/修改入参对象
 */
@Data
public class AiPromptDTO {
    /**
     * 主键（修改时必填）
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
}