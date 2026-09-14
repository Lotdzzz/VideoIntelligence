package com.framework.exception.user;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * @author dotm
 * 用户不存在异常类
 */
@Slf4j
public class UserNotExistsException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotExistsException(String message) {
        super(ExceptionConstants.USER_NOT_EXISTS);
        log.info("{}: {}", ExceptionConstants.USER_NOT_EXISTS, message);
    }
}
