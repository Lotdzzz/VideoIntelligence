package com.vi.controller.user;

import com.framework.model.Result;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.VideoDetailsInfoDTO;
import com.vi.entity.vo.VideoReturnInfoVO;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.service.IVideoUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件上传接口
 *
 * @author dotm
 */
@RestController
@RequestMapping("/file/user/predesign")
@RequiredArgsConstructor
public class VideoUploadController {

    private final IVideoUploadService videoUploadService;

    /**
     * 根据视频信息获取分片信息
     */
    @PostMapping("/slice/info")
    public Result<VideoSliceMissionVo> getSliceInfo(@RequestBody VideoDetailsInfoDTO videoUploadDTO) {
        VideoSliceMissionVo result = videoUploadService.receiveVideoInfo(videoUploadDTO);
        return Result.success(result);
    }

    /**
     * 上传视频分片
     */
    @PostMapping("/slice/upload")
    public Result<String> uploadSlice(@RequestBody PartUploadCompleteDTO partUploadCompleteDTO) {
        videoUploadService.sliceUpload(partUploadCompleteDTO);
        return Result.success();
    }

}
