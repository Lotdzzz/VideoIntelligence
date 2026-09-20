package com.vi.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存入redis的文件信息
 * 因为序列化和反序列化Hash类型和原子性自增Hash的操作必须指定为字符串所以此类设置为全字符串类型
 *
 * @author dotm
 */
@Data
@Builder
public class VideoUploadProgress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String fileName;

    @JsonIgnore
    public static final String FILE_NAME_KEY = "fileName";

    /**
     * 总分片数
     */
    private String totalParts;

    @JsonIgnore
    public static final String TOTAL_PARTS_KEY = "totalParts";

    /**
     * 已完成分片数
     */
    private String completedParts;

    @JsonIgnore
    public static final String COMPLETED_PARTS_KEY = "completedParts";

    /**
     * 文件总大小
     */
    private String fileSize;

    @JsonIgnore
    public static final String FILE_SIZE_KEY = "fileSize";

    /**
     * 分片大小
     */
    private String partSize;

    @JsonIgnore
    public static final String PART_SIZE_KEY = "partSize";

    /**
     * 状态
     */
    private String status;

    @JsonIgnore
    public static final String STATUS_KEY = "status";

    /**
     * 上传任务的唯一标识符
     */
    private String uploadId;

    @JsonIgnore
    public static final String UPLOAD_ID_KEY = "uploadId";

    /**
     * 文件封面
     */
    public String cover;

    @JsonIgnore
    public static final String COVER_KEY = "cover";

    /**
     * 原文件名
     */
    private String originalName;

    @JsonIgnore
    public static final String ORIGINAL_NAME_KEY = "originalName";

    /**
     * 分类id
     */
    private String categoryId;

    @JsonIgnore
    public static final String CATEGORY_ID_KEY = "categoryId";
}
