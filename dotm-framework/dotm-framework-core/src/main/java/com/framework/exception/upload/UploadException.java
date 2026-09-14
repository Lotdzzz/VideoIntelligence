package com.framework.exception.upload;

import com.framework.constants.ExceptionConstants;

import java.io.Serial;

/**
 * @author dotm
 * 文件上传异常
 */
public class UploadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UploadException(String code) {
        super(ExceptionConstants.FILE_UPLOAD + code, null);
    }
}
