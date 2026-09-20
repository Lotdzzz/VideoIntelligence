package com.vi.service.impl;

import com.aliyun.oss.OSS;
import com.vi.properties.OSSProperties;
import com.vi.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author dotm
 * description 阿里云OSS服务实现类
 */
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSSProperties ossProperties;

    private final OSS ossClient;

    /**
     * 上传文件到阿里云OSS
     *
     * @param file     前端传来的文件
     * @param filename 文件名
     * @return 文件访问URL
     */
    @Override
    public String ossUpload(MultipartFile file, String filename) throws IOException {
        // 上传文件到阿里云OSS
        ossClient.putObject(ossProperties.getBucketName(), filename, file.getInputStream());
        // 返回文件访问URL
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + filename;
    }
}
