package com.vi.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 视频的详细信息 DTO
 *
 * @author dotm
 */
@Data
public class VideoDetailsInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名（含扩展名）
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名过长")
    private String fileName;

    /**
     * 文件大小，单位：字节
     */
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小不合法")
    private Long fileSize;

    /**
     * MIME 类型，如 video/mp4
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    /**
     * 扩展名，如 mp4
     */
    @Size(max = 20)
    private String fileExt;
}
