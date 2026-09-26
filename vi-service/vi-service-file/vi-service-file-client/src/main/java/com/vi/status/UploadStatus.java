package com.vi.status;

/**
 * 上传状态
 *
 * @author dotm
 */
public enum UploadStatus {

    /**
     * 上传状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除
     */
    UPLOADING,

    UPLOADED,

    PROCESSING,

    FAILED,

    SUCCESS,

    CANCELED
}