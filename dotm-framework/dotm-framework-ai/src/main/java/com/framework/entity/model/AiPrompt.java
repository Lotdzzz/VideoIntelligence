package com.framework.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * AI提示词表
 * @TableName ai_prompt
 */
@TableName(value ="ai_prompt")
@Data
public class AiPrompt {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
    @TableField("`key`")
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
     * 软删除：0-未删除，1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}