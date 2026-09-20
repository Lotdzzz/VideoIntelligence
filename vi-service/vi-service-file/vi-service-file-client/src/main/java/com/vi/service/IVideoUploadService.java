package com.vi.service;

import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.VideoSliceMissionVo;

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
    VideoSliceMissionVo receiveVideoInfo(ViFileDTO videoUploadDTO);

    /**
     * 上传视频分片
     *
     * @param partUploadCompleteDTO 分片上传完成信息
     */
    boolean sliceUpload(PartUploadCompleteDTO partUploadCompleteDTO);
}
