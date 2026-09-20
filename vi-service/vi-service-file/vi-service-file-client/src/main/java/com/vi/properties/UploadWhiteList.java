package com.vi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dotm
 * 文件上传后缀名白名单
 */
@ConfigurationProperties(prefix = "upload.white")
@Component
@Data
public class UploadWhiteList {

    /**
     * 视频白名单列表
     */
    private List<String> videoList;

    /**
     * 图片白名单列表
     */
    private List<String> imageList;
}
