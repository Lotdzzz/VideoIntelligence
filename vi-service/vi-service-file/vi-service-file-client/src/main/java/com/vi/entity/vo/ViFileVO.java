package com.vi.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 返回给前端的VI资源文件VO
 */
@Data
public class ViFileVO {
    /**
     * 文件ID
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
