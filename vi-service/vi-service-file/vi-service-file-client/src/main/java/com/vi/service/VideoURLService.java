package com.vi.service;

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
    void sendVideoURL(String url);
}
