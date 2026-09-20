package com.vi.service.impl;

import com.framework.exception.upload.UploadEmptyException;
import com.framework.exception.upload.UploadNameException;
import com.vi.entity.dto.ViFileDTO;
import com.vi.properties.UploadWhiteList;
import com.vi.service.FilePreFilterService;
import com.vi.utils.FileWhiteFilterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 文件上传之后的前置检查服务实现类
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class FilePreFilterServiceImpl implements FilePreFilterService {

    private final UploadWhiteList uploadWhiteList;

    /**
     * 预检查视频信息
     *
     * @param videoDetailsInfoDTO 视频信息
     */
    @Override
    public String preCheckVideoInfo(ViFileDTO videoDetailsInfoDTO) {
        //检测文件非空性
        if (videoDetailsInfoDTO == null
                || videoDetailsInfoDTO.getOriginalName() == null
                || videoDetailsInfoDTO.getFileSize() == null) {
            throw new UploadEmptyException(null);
        }

        //路径穿越攻击防御 文件名非空 如果为空用空串防空指针
        String original = StringUtils.cleanPath(videoDetailsInfoDTO.getOriginalName());
        if (original.isBlank() || original.contains("..")) {
            throw new UploadNameException(null);
        }

        // 白名单过滤器
        if (FileWhiteFilterUtil.whiteFilter(uploadWhiteList.getVideoList(), original)) {
            throw new UploadNameException(null);
        }
        return original;
    }

    /**
     * 预检查视频封面图片信息
     *
     * @return 过滤后的文件名
     */
    @Override
    public String preCheckVideoCover(MultipartFile file) {
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

        // 白名单过滤器
        if (FileWhiteFilterUtil.whiteFilter(uploadWhiteList.getImageList(), original)) {
            throw new UploadNameException(null);
        }
        return original;
    }
}
