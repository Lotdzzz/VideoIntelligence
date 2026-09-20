package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * 上传异常类
 */
@Slf4j
public class UploadPutException extends UploadException {
    public UploadPutException(String message) {
        super(ExceptionConstants.UPLOAD_PUT_ERROR);
        log.info("{}: {}", ExceptionConstants.UPLOAD_PUT_ERROR, message);
    }
}
