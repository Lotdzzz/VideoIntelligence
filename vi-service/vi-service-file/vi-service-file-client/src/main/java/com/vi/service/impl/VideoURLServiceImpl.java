package com.vi.service.impl;

import com.framework.model.Result;
import com.vi.api.PythonFileController;
import com.vi.entity.dto.APIURLLinkUploadDTO;
import com.vi.entity.vo.APIURLsInfoVO;
import com.vi.service.VideoURLService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频链接服务实现
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class VideoURLServiceImpl implements VideoURLService {

    private final PythonFileController pythonFileController;

    /**
     * 调用python微服务解析视频链接
     *
     * @param url 视频链接
     */
    @Override
    public Result<List<APIURLsInfoVO>> sendVideoURL(String url) {
        // 调用openfeign访问python微服务 同步响应
        return pythonFileController.getURLsByVideoURL(APIURLLinkUploadDTO.builder().url(url).build());
    }
}
