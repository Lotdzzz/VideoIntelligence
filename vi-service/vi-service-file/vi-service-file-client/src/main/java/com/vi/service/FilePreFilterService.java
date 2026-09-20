package com.vi.service;

import com.vi.entity.dto.ViFileDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传之后的前置检查服务
 *
 * @author dotm
 */
public interface FilePreFilterService {

    /**
     * 前置检查
     *
     * @param videoDetailsInfoDTO 视频信息
     * @return 返回检查结果，null表示通过检查，非null表示不通过检查
     */
    public String preCheckVideoInfo(ViFileDTO videoDetailsInfoDTO);

    /**
     * 预检查视频封面图片信息
     *
     * @param file 视频封面图片
     * @return 过滤后的文件名
     */
    String preCheckVideoCover(MultipartFile file);
}
