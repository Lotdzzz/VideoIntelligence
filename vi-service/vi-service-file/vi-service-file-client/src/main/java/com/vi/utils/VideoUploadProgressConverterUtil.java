package com.vi.utils;

import com.framework.utils.SecurityUtils;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.VideoUploadProgress;
import com.vi.status.UploadStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 视频上传进度转换工具类
 *
 * @author dotm
 */
public class VideoUploadProgressConverterUtil {

    /**
     * 将视频信息转换为VideoUploadProgress对象
     *
     * @param videoDetailsInfoDTO 视频信息
     * @return VideoUploadProgress对象
     */
    public static VideoUploadProgress toProgressObject(ViFileDTO videoDetailsInfoDTO) {
        return VideoUploadProgress.builder()
                .fileName(videoDetailsInfoDTO.getObjectName())
                .totalParts(String.valueOf(videoDetailsInfoDTO.getChunkCount()))
                .completedParts("0")
                .fileSize(videoDetailsInfoDTO.getFileSize().toString())
                .partSize(String.valueOf(videoDetailsInfoDTO.getChunkSize()))
                .status(UploadStatus.UPLOADING.name())
                .uploadId(videoDetailsInfoDTO.getUploadId())
                .cover(videoDetailsInfoDTO.getCover())
                .originalName(videoDetailsInfoDTO.getOriginalName())
                .categoryId(String.valueOf(videoDetailsInfoDTO.getCategoryId()))
                .build();
    }

    /**
     * 将VideoUploadProgress对象转换为Map
     *
     * @param progress VideoUploadProgress对象
     * @return Map表示的VideoUploadProgress对象
     */
    public static Map<String, Object> toMap(VideoUploadProgress progress) {
        return Map.of(
                VideoUploadProgress.FILE_NAME_KEY, progress.getFileName(),
                VideoUploadProgress.TOTAL_PARTS_KEY, progress.getTotalParts(),
                VideoUploadProgress.COMPLETED_PARTS_KEY, progress.getCompletedParts(),
                VideoUploadProgress.FILE_SIZE_KEY, progress.getFileSize(),
                VideoUploadProgress.PART_SIZE_KEY, progress.getPartSize(),
                VideoUploadProgress.STATUS_KEY, progress.getStatus(),
                VideoUploadProgress.UPLOAD_ID_KEY, progress.getUploadId(),
                VideoUploadProgress.COVER_KEY, progress.getCover(),
                VideoUploadProgress.ORIGINAL_NAME_KEY, progress.getOriginalName(),
                VideoUploadProgress.CATEGORY_ID_KEY, progress.getCategoryId()
        );
    }

    /**
     * 将Map转换为VideoUploadProgress对象
     *
     * @param map Map对象
     * @return VideoUploadProgress对象
     */
    public static VideoUploadProgress fromMap(Map<String, Object> map) {
        return VideoUploadProgress.builder()
                .fileName((String) map.get(VideoUploadProgress.FILE_NAME_KEY))
                .totalParts((String) map.get(VideoUploadProgress.TOTAL_PARTS_KEY))
                .completedParts((String) map.get(VideoUploadProgress.COMPLETED_PARTS_KEY))
                .fileSize((String) map.get(VideoUploadProgress.FILE_SIZE_KEY))
                .partSize((String) map.get(VideoUploadProgress.PART_SIZE_KEY))
                .status((String) map.get(VideoUploadProgress.STATUS_KEY))
                .uploadId((String) map.get(VideoUploadProgress.UPLOAD_ID_KEY))
                .cover((String) map.get(VideoUploadProgress.COVER_KEY))
                .originalName((String) map.get(VideoUploadProgress.ORIGINAL_NAME_KEY))
                .categoryId((String) map.get(VideoUploadProgress.CATEGORY_ID_KEY))
                .build();
    }

    /**
     * 将VideoUploadProgress对象转换为ViFileDTO对象
     *
     * @param videoInfo  VideoUploadProgress对象
     * @param bucketName 存储桶名称
     * @return ViFileDTO对象
     */
    public static ViFileDTO toVIFileDTO(VideoUploadProgress videoInfo, String bucketName) {
        if (videoInfo != null) {
            ViFileDTO viFileDTO = new ViFileDTO();
            viFileDTO.setObjectName(videoInfo.getFileName());
            viFileDTO.setFileSize(Long.parseLong(videoInfo.getFileSize()));
            viFileDTO.setUploadId(videoInfo.getUploadId());
            viFileDTO.setCover(videoInfo.getCover());
            viFileDTO.setOriginalName(videoInfo.getOriginalName());
            viFileDTO.setUserId(SecurityUtils.getUserId());
            viFileDTO.setBucketName(bucketName);
            viFileDTO.setCategoryId(Long.parseLong(videoInfo.getCategoryId()));
            viFileDTO.setFileType(FileWhiteFilterUtil.getExtension(videoInfo.getFileName()));
            viFileDTO.setFileExt(FileWhiteFilterUtil.getExtension(videoInfo.getFileName()));
            viFileDTO.setStatus(FileConstants.getUploadStatus(UploadStatus.UPLOADED));
            viFileDTO.setUploadTime(LocalDateTime.now());
            return viFileDTO;
        }
        return null;
    }
}
