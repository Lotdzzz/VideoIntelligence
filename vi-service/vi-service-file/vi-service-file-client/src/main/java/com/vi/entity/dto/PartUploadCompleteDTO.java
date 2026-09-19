package com.vi.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartUploadCompleteDTO {

    /**
     * 上传任务唯一 ID
     */
    @NotBlank
    private String filename;

    /**
     * MinIO Multipart Upload ID
     */
    @NotBlank
    private String uploadId;

    /**
     * 分片编号
     */
    @NotNull
    private Integer partNumber;

    /**
     * MinIO 返回的 ETag
     * <p>
     * 前端上传分片成功后，从响应 Header 中获取
     */
    @NotBlank
    private String eTag;
}
