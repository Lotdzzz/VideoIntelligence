package com.vi.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author dotm
 * VI资源文件分类查询/新增/修改入参对象
 */
@Data
public class ViFileCategoryDTO {
    /**
     * 分类ID（修改时必填）
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
     * 创建时间-开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 创建时间-结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
