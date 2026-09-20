package com.vi.service;

import com.vi.entity.dto.FileUploadDTO;
import com.vi.entity.dto.PartUploadCompleteDTO;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.VideoSliceMissionVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    /**
     * 保存视频封面
     *
     * @param file 视频封面文件
     * @return 公开访问的封面url
     */
    String saveVideoCover(MultipartFile file) throws IOException;
}
