package com.vi.controller.user;

import com.framework.model.Result;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.VideoSliceMissionVo;
import com.vi.service.IVideoUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<VideoSliceMissionVo> getSliceInfo(@Valid @RequestBody ViFileDTO videoUploadDTO) {
        VideoSliceMissionVo result = videoUploadService.receiveVideoInfo(videoUploadDTO);
        return Result.success(result);
    }

    /**
     * 上传视频分片
     */
    @PostMapping("/slice/upload")
    public Result<String> uploadSlice(@Valid @RequestBody PartUploadCompleteDTO partUploadCompleteDTO) {
        boolean result = videoUploadService.sliceUpload(partUploadCompleteDTO);
        return result ? Result.success("分片上传成功") : Result.error("分片上传失败");
    }

}
