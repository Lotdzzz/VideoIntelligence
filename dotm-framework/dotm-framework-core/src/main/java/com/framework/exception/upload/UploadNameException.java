package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * 文件名不合法异常
 */
@Slf4j
public class UploadNameException extends UploadException {

    public UploadNameException(String message) {
        super(ExceptionConstants.FILE_NAME_INVALID);
        log.info("{}: {}", ExceptionConstants.FILE_NAME_INVALID, message);
    }
}
