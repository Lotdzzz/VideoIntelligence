package com.vi.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 返回给前端的VI资源文件分类VO
 */
@Data
public class ViFileCategoryVO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
