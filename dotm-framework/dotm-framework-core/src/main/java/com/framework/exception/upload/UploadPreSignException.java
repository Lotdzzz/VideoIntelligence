package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * 文件上传预签名异常
 */
@Slf4j
public class UploadPreSignException extends UploadException {

    public UploadPreSignException(String message) {
        super(ExceptionConstants.FILE_PRE_SIGN_ERROR);
        log.info("{}: {}", ExceptionConstants.FILE_PRE_SIGN_ERROR, message);
    }

}
