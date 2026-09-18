package com.vi.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author dotm
 * VI资源文件
 * @TableName vi_file
 */
@TableName(value = "vi_file")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViFile {
    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * MinIO对象名称
     */
    private String objectName;

    /**
     * MinIO Bucket
     */
    private String bucketName;

    /**
     * 文件类型，如 video/audio/image/document
     */
    private String fileType;

    /**
     * 文件扩展名，如 mp4/pdf/jpg
     */
    private String fileExt;

    /**
     * MIME类型，如 video/mp4
     */
    private String contentType;

    /**
     * 封面图URL，适用于视频/音频/文档等
     */
    private String cover;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 文件分类ID
     */
    private Long categoryId;

    /**
     * 文件状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除
     */
    private Integer status;

    /**
     * 文件MD5，用于后续秒传/去重
     */
    private String md5;

    /**
     * 上传完成时间
     */
    private LocalDateTime uploadTime;

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

    /**
     * 逻辑删除：0否 1是
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
