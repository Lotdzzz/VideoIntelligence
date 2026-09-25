package com.vi.service;

import com.framework.model.Result;
import com.vi.entity.vo.APIURLsInfoVO;

import java.util.List;

/**
 * 视频链接服务
 *
 * @author dotm
 */
public interface VideoURLService {

    /**
     * 上传url到消息队列
     *
     * @param url 视频链接
     */
    Result<List<APIURLsInfoVO>> sendVideoURL(String url);
}
