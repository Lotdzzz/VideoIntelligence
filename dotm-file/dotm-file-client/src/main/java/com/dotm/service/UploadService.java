package com.dotm.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author dotm
 * 文件上传服务
 */
public interface UploadService {

    /**
     * 文件上传
     */
    String upload(MultipartFile file) throws IOException;
}
