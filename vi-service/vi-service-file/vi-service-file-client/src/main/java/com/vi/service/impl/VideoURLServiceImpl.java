package com.vi.service.impl;

import com.vi.producer.FileURLProducer;
import com.vi.service.VideoURLService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 视频链接服务实现
 *
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class VideoURLServiceImpl implements VideoURLService {

    private final FileURLProducer fileURLProducer;

    /**
     * 上传url到消息队列
     *
     * @param url 视频链接
     */
    @Override
    public void sendVideoURL(String url) {
        fileURLProducer.send(url);
    }
}
