package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * 文件上传为空异常
 */
@Slf4j
public class UploadEmptyException extends UploadException {

    public UploadEmptyException(String message) {
        super(ExceptionConstants.FILE_NULL);
        log.info("{}: {}", ExceptionConstants.FILE_NULL, message);
    }
}
