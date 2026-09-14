package com.dotm.controller;

import com.framework.model.Result;
import com.dotm.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author dotm
 * 文件上传接口
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 文件上传接口
     *
     * @param file 文件
     * @return url
     */
    @PostMapping
    public Result<Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file);
        return Result.success(url);
    }
}
