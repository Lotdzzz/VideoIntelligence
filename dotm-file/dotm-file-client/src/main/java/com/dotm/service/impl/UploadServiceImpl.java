package com.dotm.service.impl;

import com.aliyun.oss.OSS;
import com.dotm.properties.OSSProperties;
import com.dotm.utils.FileWhiteFilterUtil;
import com.framework.exception.upload.UploadEmptyException;
import com.framework.exception.upload.UploadNameException;
import com.dotm.properties.UploadWhiteList;
import com.dotm.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * @author dotm
 * 文件上传业务
 */
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final UploadWhiteList uploadWhiteList;

    private final OSS ossClient;

    private final OSSProperties ossProperties;

    /**
     * 文件上传
     * 检测文件非空性
     * 检测文件后缀名是否在白名单中
     * 清洗文件名
     * 检测文件大小是否超过限制
     *
     * @param file 文件
     * @return 文件访问URL
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        //检测文件非空性
        if (file == null || file.isEmpty()) {
            throw new UploadEmptyException(null);
        }

        //路径穿越攻击防御 文件名非空 如果为空用空串防空指针
        String original = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        if (original.isBlank() || original.contains("..")) {
            throw new UploadNameException(null);
        }

        //白名单过滤
        if (FileWhiteFilterUtil.filter(uploadWhiteList.getList(), original)) {
            throw new UploadNameException(original);
        }

        //使用UUID去重文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "-" + original;

        // 上传文件到阿里云OSS
        return ossUpload(file, newFileName);
    }

    /**
     * 本地上传
     *
     * @param filename 文件名
     * @return 文件路径
     * @deprecated 该方法已过期
     */
    @Deprecated
    private Path localUpload(String filename) throws IOException {
        // 刷盘
        Path root = Paths.get(uploadWhiteList.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root.resolve(filename).normalize();
    }

    /**
     * 上传文件到阿里云OSS
     *
     * @param file     前端传来的文件
     * @param filename 文件名
     * @return 文件访问URL
     */
    private String ossUpload(MultipartFile file, String filename) throws IOException {
        // 上传文件到阿里云OSS
        ossClient.putObject(ossProperties.getBucketName(), filename, file.getInputStream());
        // 返回文件访问URL
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + filename;
    }
}
