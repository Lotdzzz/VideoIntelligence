package com.vi.service;

import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.VideoDetailsInfoDTO;
import com.vi.entity.vo.VideoReturnInfoVO;
import com.vi.entity.vo.VideoSliceMissionVo;

import java.util.List;

/**
 * @author dotm
 * 视频上传服务接口
 */
public interface IVideoUploadService {

    /**
     * 接收视频信息，返回minIO的预签名集合 用于分片上传
     *
     * @param videoUploadDTO 视频信息
     * @return 返回minIO的预签名集合 用于分片上传
     */
    VideoSliceMissionVo receiveVideoInfo(VideoDetailsInfoDTO videoUploadDTO);

    /**
     * 预检查视频信息
     *
     * @param videoUploadDTO 视频信息
     */
    String preCheckVideoInfo(VideoDetailsInfoDTO videoUploadDTO);

    /**
     * 上传视频分片
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     */
    void sliceUpload(PartUploadCompleteDTO partUploadCompleteDTO);
}
