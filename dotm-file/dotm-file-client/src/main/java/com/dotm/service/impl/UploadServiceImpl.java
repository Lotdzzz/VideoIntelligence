package com.dotm.service.impl;

import com.framework.exception.upload.UploadEmptyException;
import com.framework.exception.upload.UploadException;
import com.framework.exception.upload.UploadNameException;
import com.dotm.properties.UploadWhiteList;
import com.dotm.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        //使用UUID去重文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "-" + original;

        // 刷盘
        // 这里可以添加具体的刷盘逻辑，例如将文件保存到本地或上传到云存储
        Path root = Paths.get(uploadWhiteList.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path path = root.resolve(newFileName).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UploadException(e.getMessage());
        }

        return newFileName;
    }
}
