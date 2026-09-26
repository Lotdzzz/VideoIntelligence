package com.vi.controller.user;

import com.framework.model.Result;
import com.vi.entity.dto.URLLinkUploadDTO;
import com.vi.entity.vo.APIURLsInfoVO;
import com.vi.service.VideoURLService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频链接上传接口
 * 将用户上传链接发送到消息队列由python微服务消费
 *
 * @author dotm
 * @deprecated 此类仅演示用户上传视频连接之后解析链接并生成子视频集合，后续视频的真实下载与解析并不处理,此接口仅供学习与参考，\
 * 禁止用于商业用途，盈利用途
 */
@RestController
@RequestMapping("/file/link")
@RequiredArgsConstructor
@Deprecated
public class VideoURLUploadController {

    private final VideoURLService videoURLService;

    /**
     * 接受用户上传的url链接
     *
     * @deprecated 此方法仅演示用户上传视频连接之后解析链接并生成子视频集合，后续视频的真实下载与解析并不处理,此接口仅供学习与参考，\
     * 禁止用于商业用途，盈利用途
     */
    @PostMapping("/receive")
    @Deprecated
    public Result<List<APIURLsInfoVO>> receiveLink(@RequestBody URLLinkUploadDTO url) {
        return videoURLService.sendVideoURL(url.getUrl());
    }
}
