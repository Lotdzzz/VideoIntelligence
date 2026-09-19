package com.vi.entity.vo;

import lombok.Data;

/**
 * 用来设置返回给前端的分片信息
 */
@Data
public class VideoReturnInfoVO {

    /**
     * 分片上传的预签名URL
     */
    private String preSignedUrl;

    /**
     * 分片标识
     */
    private Long partNumber;

    /**
     * 视频文件的唯一标识，用于断点续传
     */
    private String filename;

    /**
     * 文件所属的视频标识
     */
    private Long videoId;
}
