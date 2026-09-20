package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传分片完成失败异常
 *
 * @author dotm
 */
@Slf4j
public class UploadMultipartCompeteFailedException extends UploadException {

    public UploadMultipartCompeteFailedException(String message) {
        super(ExceptionConstants.FILE_PART_COMPLETE_FAILED);
        log.info("{}: {}", ExceptionConstants.FILE_PART_COMPLETE_FAILED, message);
    }

}
