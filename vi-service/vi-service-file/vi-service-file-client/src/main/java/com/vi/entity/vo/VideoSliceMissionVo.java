package com.vi.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 返回给前端的分片上传任务信息
 *
 * @author dotm
 */
@Data
public class VideoSliceMissionVo {

    /**
     * 每个分片的信息
     */
    List<VideoReturnInfoVO> videoReturnInfoVOList;

    /**
     * 告知minIO开始分片上传的uploadId
     * 这个ID是minIO生成的唯一标识，用于标识一个分片上传任务
     * 也是合并时要用到的
     */
    private String uploadId;
}
