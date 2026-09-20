package com.vi.service;

import com.vi.entity.dto.ViFileDTO;

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
}
