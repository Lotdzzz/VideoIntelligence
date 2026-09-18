package com.vi.entity.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author dotm
 * VI资源文件查询/新增/修改入参对象
 */
@Data
public class ViFileDTO {
    /**
     * 文件ID（修改时必填）
     */
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
