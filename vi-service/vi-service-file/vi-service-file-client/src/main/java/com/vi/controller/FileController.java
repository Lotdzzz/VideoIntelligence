package com.vi.controller;

import com.framework.model.Result;
import com.vi.entity.dto.FileUploadDTO;
import com.vi.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 文件上传接口
 *
 * @author dotm
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    /**
     * 获取文件上传预签名URL
     * 在这里根据前端传入的文件名组件唯一文件名
     * 因为文件名可能包含中文故此使用post接口
     *
     * @param fileName 文件名
     * @return url
     */
    @PostMapping("/presign")
    public Result<String> getUploadPresignedUrl(@RequestBody FileUploadDTO fileName) {
        String url = minioService.generateUploadPresignedUrl(
                UUID.randomUUID() + "-" + fileName.getFileName());
        return Result.success(url);
    }

    /**
     * 获取文件下载预签名URL
     *
     * @param fileName 文件名
     * @return url
     */
    @PostMapping("/download-presign")
    public Result<String> getDownloadPresignedUrl(@RequestBody FileUploadDTO fileName) {
        String url = minioService.generateDownloadPresignedUrl(
                UUID.randomUUID() + "-" + fileName.getFileName());
        return Result.success(url);
    }
}
