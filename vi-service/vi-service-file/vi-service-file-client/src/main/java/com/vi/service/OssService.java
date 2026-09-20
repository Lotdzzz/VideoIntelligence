package com.vi.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author dotm
 * description 阿里云OSS服务接口
 */
public interface OssService {

    /**
     * 上传文件到阿里云OSS
     *
     * @param file     前端传来的文件
     * @param filename 文件名
     * @return 文件访问URL
     */
    String ossUpload(MultipartFile file, String filename) throws IOException;
}
