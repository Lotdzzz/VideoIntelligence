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
 */
@RestController
@RequestMapping("/file/link")
@RequiredArgsConstructor
public class VideoURLUploadController {

    private final VideoURLService videoURLService;

    /**
     * 接受用户上传的url链接
     */
    @PostMapping("/receive")
    public Result<List<APIURLsInfoVO>> receiveLink(@RequestBody URLLinkUploadDTO url) {
        return videoURLService.sendVideoURL(url.getUrl());
    }
}
