package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录超时异常
 *
 * @author dotm
 */
@Slf4j
public class LoginTimeOutException extends LoginException {

    public LoginTimeOutException(String message) {
        this(message, null);
    }

    public LoginTimeOutException(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_TIME_OUT, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_TIME_OUT, message, cause);
    }

}
